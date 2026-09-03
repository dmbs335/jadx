package jadx.core.dex.visitors.regions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.attributes.nodes.LoopLabelAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.TryCatchRegion;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.RegionUtils;

/**
 * Restore catch exits which disappear when exception regions stop at a loop boundary.
 *
 * This runs after loop normalization. At that point a for-loop update has been detached from the
 * loop-end block, while statements which must execute after a catch remain there. This distinction
 * is essential: emitting {@code continue} before loop normalization can skip a shared state update.
 */
final class RestoreExceptionHandlerLoopExits {

	private RestoreExceptionHandlerLoopExits() {
	}

	static void process(MethodNode mth) {
		IRegion region = mth.getRegion();
		// Native and abstract declarations intentionally have no code region.
		// LoopRegionVisitor still visits these methods as part of a class pass,
		// so this optional post-processing step must follow the same no-body
		// contract as DepthRegionTraversal and the code generator.
		if (region == null) {
			return;
		}
		processRegion(mth, region);
	}

	private static void processRegion(MethodNode mth, IRegion region) {
		if (region instanceof TryCatchRegion) {
			TryCatchRegion tryCatchRegion = (TryCatchRegion) region;
			for (Map.Entry<ExceptionHandler, IContainer> entry : tryCatchRegion.getCatchRegions().entrySet()) {
				if (entry.getValue() instanceof IRegion) {
					restoreHandlerExit(mth, entry.getKey(), (IRegion) entry.getValue());
				}
			}
		}
		for (IContainer container : region.getSubBlocks()) {
			if (container instanceof IRegion) {
				processRegion(mth, (IRegion) container);
			}
		}
	}

	private static void restoreHandlerExit(MethodNode mth, ExceptionHandler handler, IRegion handlerRegion) {
		if (RegionUtils.hasExitBlock(handlerRegion)) {
			return;
		}
		List<LoopInfo> sourceLoops = mth.getAllLoopsForBlock(handler.getTryBlock().getTopSplitter());
		if (sourceLoops.isEmpty()) {
			return;
		}

		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(handlerRegion, handlerBlocks);
		Set<BlockNode> boundaryTargets = collectBoundaryTargets(handlerBlocks);
		if (boundaryTargets.isEmpty()) {
			return;
		}

		LoopInfo continueLoop = findContinueLoop(sourceLoops, boundaryTargets);
		if (continueLoop != null && canSkipLoopTail(continueLoop, boundaryTargets)) {
			InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
			continueInsn.add(AFlag.SYNTHETIC);
			addLoopLabelIfNeeded(sourceLoops, continueLoop, continueInsn);
			handlerRegion.getSubBlocks().add(new InsnContainer(continueInsn));
			return;
		}

		LoopInfo breakLoop = findBreakLoop(sourceLoops, boundaryTargets);
		if (breakLoop != null) {
			InsnNode breakInsn = new InsnNode(InsnType.BREAK, 0);
			breakInsn.add(AFlag.SYNTHETIC);
			breakInsn.addAttr(AType.LOOP, breakLoop);
			addLoopLabelIfNeeded(sourceLoops, breakLoop, breakInsn);
			handlerRegion.getSubBlocks().add(new InsnContainer(breakInsn));
		}
	}

	private static Set<BlockNode> collectBoundaryTargets(Set<IBlock> handlerBlocks) {
		Set<BlockNode> boundaryTargets = new HashSet<>();
		for (IBlock handlerBlock : handlerBlocks) {
			if (!(handlerBlock instanceof BlockNode)) {
				continue;
			}
			for (BlockNode successor : ((BlockNode) handlerBlock).getCleanSuccessors()) {
				if (!handlerBlocks.contains(successor)) {
					boundaryTargets.add(successor);
				}
			}
		}
		return boundaryTargets;
	}

	private static @Nullable LoopInfo findContinueLoop(
			List<LoopInfo> sourceLoops, Set<BlockNode> boundaryTargets) {
		LoopInfo result = null;
		for (BlockNode target : boundaryTargets) {
			LoopInfo targetLoop = null;
			BlockNode targetEnd = BlockUtils.followEmptyPath(target);
			for (LoopInfo loop : sourceLoops) {
				if (target == loop.getStart()
						|| target == loop.getEnd()
						|| targetEnd == loop.getStart()
						|| targetEnd == loop.getEnd()) {
					if (targetLoop != null && targetLoop != loop) {
						return null;
					}
					targetLoop = loop;
				}
			}
			if (targetLoop == null || result != null && result != targetLoop) {
				return null;
			}
			result = targetLoop;
		}
		return result;
	}

	private static boolean canSkipLoopTail(LoopInfo loop, Set<BlockNode> boundaryTargets) {
		boolean targetsLoopEnd = boundaryTargets.stream()
				.anyMatch(target -> target == loop.getEnd()
						|| BlockUtils.followEmptyPath(target) == loop.getEnd());
		if (!targetsLoopEnd) {
			return true;
		}
		// LoopRegionVisitor removes a recognized for-loop increment from this block. Anything left
		// is a real shared tail which a newly emitted continue would incorrectly bypass.
		return loop.getEnd().getInstructions().stream()
				.noneMatch(insn -> !insn.contains(AFlag.DONT_GENERATE));
	}

	private static @Nullable LoopInfo findBreakLoop(
			List<LoopInfo> sourceLoops, Set<BlockNode> boundaryTargets) {
		Set<LoopInfo> exitedLoops = new HashSet<>();
		for (LoopInfo loop : sourceLoops) {
			for (BlockNode target : boundaryTargets) {
				BlockNode targetEnd = BlockUtils.followEmptyPath(target);
				if (!loop.getLoopBlocks().contains(target)
						&& !loop.getLoopBlocks().contains(targetEnd)) {
					exitedLoops.add(loop);
					break;
				}
			}
		}
		if (exitedLoops.isEmpty()) {
			return null;
		}
		for (BlockNode target : boundaryTargets) {
			BlockNode targetEnd = BlockUtils.followEmptyPath(target);
			for (LoopInfo loop : sourceLoops) {
				boolean exits = !loop.getLoopBlocks().contains(target)
						&& !loop.getLoopBlocks().contains(targetEnd);
				if (exits != exitedLoops.contains(loop)) {
					return null;
				}
			}
		}
		LoopInfo outermostExited = null;
		for (LoopInfo loop : exitedLoops) {
			LoopInfo parent = loop.getParentLoop();
			if (parent == null || !exitedLoops.contains(parent)) {
				if (outermostExited != null) {
					return null;
				}
				outermostExited = loop;
			}
		}
		return outermostExited;
	}

	private static void addLoopLabelIfNeeded(
			List<LoopInfo> sourceLoops, LoopInfo targetLoop, InsnNode exitInsn) {
		if (sourceLoops.size() < 2) {
			return;
		}
		LoopInfo innermost = null;
		for (LoopInfo loop : sourceLoops) {
			if (innermost == null || loop.hasParent(innermost)) {
				innermost = loop;
			}
		}
		if (innermost == targetLoop) {
			return;
		}
		LoopLabelAttr labelAttr = new LoopLabelAttr(targetLoop);
		exitInsn.addAttr(labelAttr);
		targetLoop.getStart().addAttr(labelAttr);
	}
}
