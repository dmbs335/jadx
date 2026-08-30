package jadx.core.dex.visitors.regions.maker;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.ConstClassNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SynchronizedRegion;
import jadx.core.dex.visitors.regions.CleanRegions;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.Utils;

import static jadx.core.utils.BlockUtils.getNextBlock;
import static jadx.core.utils.BlockUtils.isPathExists;

public class SynchronizedRegionMaker {
	private static final Logger LOG = LoggerFactory.getLogger(SynchronizedRegionMaker.class);
	private final MethodNode mth;
	private final RegionMaker regionMaker;

	SynchronizedRegionMaker(MethodNode mth, RegionMaker regionMaker) {
		this.mth = mth;
		this.regionMaker = regionMaker;
	}

	BlockNode process(IRegion curRegion, BlockNode block, InsnNode insn, RegionStack stack) {
		SynchronizedRegion synchRegion = new SynchronizedRegion(curRegion, insn);
		synchRegion.getSubBlocks().add(block);
		curRegion.getSubBlocks().add(synchRegion);

		Set<BlockNode> exits = new LinkedHashSet<>();
		Set<BlockNode> cacheSet = new HashSet<>();
		traverseMonitorExits(synchRegion, insn.getArg(0), block, exits, cacheSet);
		for (InsnNode exitInsn : synchRegion.getExitInsns()) {
			BlockNode insnBlock = BlockUtils.getBlockByInsn(mth, exitInsn);
			if (insnBlock != null) {
				insnBlock.add(AFlag.DONT_GENERATE);
			}
			// remove arg from MONITOR_EXIT to allow inline in MONITOR_ENTER
			exitInsn.removeArg(0);
			exitInsn.add(AFlag.DONT_GENERATE);
		}

		BlockNode body = getNextBlock(block);
		if (body == null) {
			mth.addWarn("Unexpected end of synchronized block");
			return null;
		}
		BlockNode exit = null;
		Set<BlockNode> equivalentExitAliases = new LinkedHashSet<>();
		if (exits.size() == 1) {
			exit = getNextBlock(exits.iterator().next());
		} else if (exits.size() > 1) {
			exit = selectSharedPostMonitorExit(mth, exits, equivalentExitAliases);
			if (exit == null) {
				cacheSet.clear();
				exit = traverseMonitorExitsCross(body, exits, cacheSet);
			}
			if (exit == null) {
				exit = selectUniqueLoopContinuation(block, exits);
			}
		}
		stack.push(synchRegion);
		if (exit != null) {
			stack.addExit(exit);
			for (BlockNode alias : equivalentExitAliases) {
				// Keep the duplicated guard and its shared branches outside the synchronized body.
				// The canonical guard is emitted once after the monitor; a duplicate terminal return
				// is represented by the canonical branch and must not be reported as lost code.
				alias.add(AFlag.DONT_GENERATE);
				stack.addExit(alias);
				for (BlockNode successor : alias.getCleanSuccessors()) {
					if (BlockUtils.isExitBlock(mth, successor)) {
						successor.add(AFlag.DONT_GENERATE);
					}
					stack.addExit(successor);
				}
			}
		} else {
			for (BlockNode exitBlock : exits) {
				// don't add exit blocks which leads to method end blocks ('return', 'throw', etc)
				List<BlockNode> list = BlockUtils.buildSimplePath(exitBlock);
				if (list.isEmpty() || !BlockUtils.isExitBlock(mth, Utils.last(list))) {
					stack.addExit(exitBlock);
					// we can still try using this as an exit block to make sure it's visited.
					exit = exitBlock;
				}
			}
		}
		synchRegion.getSubBlocks().add(regionMaker.makeRegion(body));
		stack.pop();
		return exit;
	}

	private @Nullable BlockNode selectUniqueLoopContinuation(BlockNode syncBlock, Set<BlockNode> exits) {
		List<LoopInfo> loops = mth.getAllLoopsForBlock(syncBlock);
		if (loops.isEmpty()) {
			return null;
		}
		LoopInfo innerLoop = loops.get(0);
		for (LoopInfo loop : loops) {
			if (loop.getLoopBlocks().size() < innerLoop.getLoopBlocks().size()) {
				innerLoop = loop;
			}
		}
		BlockNode continuation = null;
		Set<BlockNode> directLoopBacks = new LinkedHashSet<>();
		for (BlockNode exitBlock : exits) {
			BlockNode next = getNextBlock(exitBlock);
			if (next == null || !innerLoop.getLoopBlocks().contains(next)) {
				continue;
			}
			if (isDirectLoopBack(next, innerLoop)) {
				// Keep a branch which only releases the monitor and continues the loop inside
				// the synchronized region. A different exit can still have real work which
				// must be emitted after the monitor (for example Okio's watchdog callback).
				directLoopBacks.add(next);
				continue;
			}
			// A synchronized block in a value-returning method can still have one monitor
			// exit that returns and another that refreshes loop state. Return type does not
			// determine the shape; only a unique successor inside this loop is a valid
			// continuation.
			if (continuation != null && continuation != next) {
				return null;
			}
			continuation = next;
		}
		if (continuation != null) {
			for (BlockNode directLoopBack : directLoopBacks) {
				insertExplicitContinue(directLoopBack);
			}
		}
		return continuation;
	}

	private static void insertExplicitContinue(BlockNode block) {
		if (block.getInstructions().stream().anyMatch(insn -> insn.getType() == InsnType.CONTINUE)) {
			return;
		}
		InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
		continueInsn.add(AFlag.SYNTHETIC);
		block.getInstructions().add(continueInsn);
	}

	private static boolean isDirectLoopBack(BlockNode start, LoopInfo loop) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 16 && visited.add(block)) {
			if (block == loop.getStart() || block == loop.getEnd()) {
				return true;
			}
			if (block.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))) {
				return false;
			}
			List<BlockNode> successors = block.getCleanSuccessors().stream()
					.filter(loop.getLoopBlocks()::contains)
					.toList();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	/**
	 * Select a cleanup continuation shared by several monitor exits while allowing other exits to
	 * terminate directly. This shape appears after equivalent {@code finally} tails are merged:
	 * early-return paths still return in the synchronized body, but the non-terminal paths converge
	 * on one cleanup which must be emitted after the synchronized block.
	 */
	private static @Nullable BlockNode selectSharedPostMonitorExit(
			MethodNode mth, Set<BlockNode> exits, Set<BlockNode> equivalentExitAliases) {
		BlockNode candidate = null;
		int nonTerminalExitCount = 0;
		for (BlockNode exitBlock : exits) {
			BlockNode next = getNextBlock(exitBlock);
			if (next == null || isSimpleTerminalPath(mth, exitBlock)) {
				continue;
			}
			nonTerminalExitCount++;
			if (candidate == null) {
				candidate = next;
				continue;
			}
			if (candidate != next) {
				if (!isEquivalentPostMonitorGuard(candidate, next)) {
					equivalentExitAliases.clear();
					return null;
				}
				equivalentExitAliases.add(next);
			}
		}
		if (nonTerminalExitCount < 2) {
			equivalentExitAliases.clear();
			return null;
		}
		return candidate;
	}

	private static boolean isEquivalentPostMonitorGuard(BlockNode first, BlockNode second) {
		if (first == second) {
			return true;
		}
		if (first.getInstructions().size() != 1 || second.getInstructions().size() != 1) {
			return false;
		}
		InsnNode firstInsn = first.getInstructions().get(0);
		InsnNode secondInsn = second.getInstructions().get(0);
		if (!(firstInsn instanceof IfNode) || !(secondInsn instanceof IfNode)) {
			return false;
		}
		IfNode firstIf = (IfNode) firstInsn;
		IfNode secondIf = (IfNode) secondInsn;
		if (firstIf.getOp() != secondIf.getOp()
				|| firstIf.getArgsCount() != secondIf.getArgsCount()
				|| !samePhysicalOrConstantArgs(firstIf, secondIf)) {
			return false;
		}
		return BlockUtils.isEqualPaths(firstIf.getThenBlock(), secondIf.getThenBlock())
				&& BlockUtils.isEqualPaths(firstIf.getElseBlock(), secondIf.getElseBlock());
	}

	private static boolean samePhysicalOrConstantArgs(IfNode first, IfNode second) {
		for (int index = 0; index < first.getArgsCount(); index++) {
			InsnArg firstArg = first.getArg(index);
			InsnArg secondArg = second.getArg(index);
			if (firstArg.isRegister() && secondArg.isRegister()) {
				if (((RegisterArg) firstArg).getRegNum() != ((RegisterArg) secondArg).getRegNum()) {
					return false;
				}
			} else if (!firstArg.isSameConst(secondArg)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSimpleTerminalPath(MethodNode mth, BlockNode start) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			if (BlockUtils.containsExitInsn(block) || BlockUtils.isExitBlock(mth, block)) {
				return true;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	/**
	 * Traverse from monitor-enter thru successors and collect blocks contains monitor-exit
	 */
	private static void traverseMonitorExits(SynchronizedRegion region, InsnArg arg, BlockNode block, Set<BlockNode> exits,
			Set<BlockNode> visited) {
		visited.add(block);
		for (InsnNode insn : block.getInstructions()) {
			if (insn.getType() == InsnType.MONITOR_EXIT
					&& insn.getArgsCount() > 0
					&& insn.getArg(0).equals(arg)) {
				exits.add(block);
				region.getExitInsns().add(insn);
				return;
			}
		}
		for (BlockNode node : block.getSuccessors()) {
			if (!visited.contains(node)) {
				traverseMonitorExits(region, arg, node, exits, visited);
			}
		}
	}

	/**
	 * Traverse from monitor-enter thru successors and search for exit paths cross
	 */
	private static BlockNode traverseMonitorExitsCross(BlockNode block, Set<BlockNode> exits, Set<BlockNode> visited) {
		visited.add(block);
		for (BlockNode node : block.getCleanSuccessors()) {
			boolean cross = true;
			for (BlockNode exitBlock : exits) {
				boolean p = isPathExists(exitBlock, node);
				if (!p) {
					cross = false;
					break;
				}
			}
			if (cross) {
				return node;
			}
			if (!visited.contains(node)) {
				BlockNode res = traverseMonitorExitsCross(node, exits, visited);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	public static void removeSynchronized(MethodNode mth) {
		Region startRegion = mth.getRegion();
		List<IContainer> subBlocks = startRegion.getSubBlocks();
		if (!subBlocks.isEmpty() && subBlocks.get(0) instanceof SynchronizedRegion) {
			SynchronizedRegion synchRegion = (SynchronizedRegion) subBlocks.get(0);
			InsnNode syncInsn = synchRegion.getEnterInsn();
			if (canRemoveSyncBlock(mth, syncInsn)) {
				// replace synchronized block with an inner region
				startRegion.getSubBlocks().set(0, synchRegion.getRegion());
				// remove 'monitor-enter' instruction
				InsnRemover.remove(mth, syncInsn);
				// remove 'monitor-exit' instruction
				for (InsnNode exit : synchRegion.getExitInsns()) {
					InsnRemover.remove(mth, exit);
				}
				// run region cleaner again
				CleanRegions.process(mth);
				// assume that CodeShrinker will be run after this
			}
		}
	}

	private static boolean canRemoveSyncBlock(MethodNode mth, InsnNode synchInsn) {
		InsnArg syncArg = synchInsn.getArg(0);
		if (mth.getAccessFlags().isStatic()) {
			if (syncArg.isInsnWrap() && syncArg.isConst()) {
				InsnNode constInsn = syncArg.unwrap();
				if (constInsn.getType() == InsnType.CONST_CLASS) {
					ArgType clsType = ((ConstClassNode) constInsn).getClsType();
					ArgType parentType = mth.getParentClass().getType();
					if (clsType.equals(parentType)
							|| clsType.isObject() && parentType.isObject() && clsType.getObject().equals(parentType.getObject())) {
						return true;
					}
				}
			}
			mth.addWarnComment("In static synchronized method top region not synchronized by class const: " + syncArg);
		} else {
			if (syncArg.isThis()) {
				return true;
			}
			mth.addWarnComment("In synchronized method top region not synchronized by 'this': " + syncArg);
		}
		return false;
	}
}
