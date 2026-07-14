package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.blocks.BlockSet;
import jadx.core.utils.exceptions.JadxOverflowException;

import static jadx.core.utils.BlockUtils.getNextBlock;

public class RegionMaker {
	private final MethodNode mth;
	private final RegionStack stack;

	private final IfRegionMaker ifMaker;
	private final LoopRegionMaker loopMaker;

	private final BlockSet processedBlocks;
	private final Map<BlockNode, List<Set<BlockNode>>> activeRegionStates = new HashMap<>();
	private final BlockSet recursiveRegionBlocks;
	private final BlockSet traversalCycleBlocks;
	private final int regionsLimit;

	private int regionsCount;
	private int duplicatedBlocksCount;
	private boolean hasUnsafeDuplicatedBlocks;
	private @Nullable BlockNode firstDuplicatedBlock;

	public RegionMaker(MethodNode mth) {
		this.mth = mth;
		this.stack = new RegionStack(mth);
		this.ifMaker = new IfRegionMaker(mth, this);
		this.loopMaker = new LoopRegionMaker(mth, this, ifMaker);
		this.processedBlocks = BlockSet.empty(mth);
		this.recursiveRegionBlocks = BlockSet.empty(mth);
		this.traversalCycleBlocks = BlockSet.empty(mth);
		this.regionsLimit = mth.getBasicBlocks().size() * 400;
	}

	public Region makeMthRegion() {
		Region region = makeRegion(mth.getEnterBlock());
		restoreLinearSyntheticMoveBlocks(region);
		if (duplicatedBlocksCount != 0 && hasUnsafeDuplicatedBlocks) {
			BlockNode firstBlock = Objects.requireNonNull(firstDuplicatedBlock);
			mth.addWarnComment("Code duplicated in " + duplicatedBlocksCount
					+ " blocks, first: " + firstBlock + ' ' + firstBlock.getAttributesString());
		}
		return region;
	}

	private void restoreLinearSyntheticMoveBlocks(Region rootRegion) {
		for (BlockNode block : mth.getBasicBlocks()) {
			if (!block.contains(AFlag.SYNTHETIC)
					|| block.getPredecessors().size() != 1
					|| block.getCleanSuccessors().size() != 1
					|| block.getInstructions().isEmpty()
					|| block.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)
					|| containsContainer(rootRegion, block)) {
				continue;
			}
			insertAfterPredecessor(rootRegion, block.getPredecessors().get(0), block);
		}
	}

	private static boolean containsContainer(IRegion region, IContainer target) {
		for (IContainer container : region.getSubBlocks()) {
			if (container == target) {
				return true;
			}
			if (container instanceof IRegion && containsContainer((IRegion) container, target)) {
				return true;
			}
		}
		return false;
	}

	private static void insertAfterPredecessor(IRegion region, BlockNode predecessor, BlockNode block) {
		List<IContainer> subBlocks = region.getSubBlocks();
		if (region instanceof Region) {
			for (int i = subBlocks.size() - 1; i >= 0; i--) {
				IContainer container = subBlocks.get(i);
				if (container == predecessor
						|| container instanceof IfRegion
								&& ((IfRegion) container).getConditionBlocks().contains(predecessor)) {
					subBlocks.add(i + 1, block);
				}
			}
		}
		for (IContainer container : new ArrayList<>(subBlocks)) {
			if (container instanceof IRegion) {
				insertAfterPredecessor((IRegion) container, predecessor, block);
			}
		}
	}

	Region makeRegion(BlockNode startBlock) {
		Objects.requireNonNull(startBlock);
		Region region = new Region(stack.peekRegion());
		if (stack.containsExit(startBlock)) {
			insertEdgeInsns(region, startBlock);
			return region;
		}
		Set<BlockNode> exits = new HashSet<>();
		stack.getExits().forEach(exits::add);
		List<Set<BlockNode>> activeStates = activeRegionStates.computeIfAbsent(startBlock, k -> new ArrayList<>());
		if (activeStates.contains(exits)) {
			if (!recursiveRegionBlocks.addChecked(startBlock)) {
				mth.addWarnComment("Recursive region processing prevented at block: " + startBlock);
			}
			return region;
		}
		activeStates.add(exits);
		try {
			if (processedBlocks.addChecked(startBlock)) {
				// Add block to multiple regions (duplicate the instructions in decompiled code)
				// and allow processing to continue
				if (!startBlock.contains(AFlag.DUPLICATED)) {
					if (firstDuplicatedBlock == null) {
						firstDuplicatedBlock = startBlock;
					}
					duplicatedBlocksCount++;
					hasUnsafeDuplicatedBlocks |= !isSafeLoopDuplication(startBlock);
					startBlock.add(AFlag.DUPLICATED);
				}
			}
			BlockSet regionBlocks = BlockSet.empty(mth);
			BlockNode next = startBlock;
			while (next != null) {
				if (regionBlocks.addChecked(next)) {
					if (!traversalCycleBlocks.addChecked(next)) {
						mth.addWarnComment("Region traversal cycle prevented at block: " + next);
					}
					break;
				}
				next = traverse(region, next);
				regionsCount++;
				if (regionsCount > regionsLimit) {
					throw new JadxOverflowException("Regions count limit reached at block " + startBlock);
				}
			}
			return region;
		} finally {
			activeStates.remove(exits);
			if (activeStates.isEmpty()) {
				activeRegionStates.remove(startBlock);
			}
		}
	}

	private boolean isSafeLoopDuplication(BlockNode block) {
		if (isSafeLocalAssignmentDuplication(block)) {
			return true;
		}
		if (mth.getLoopForBlock(block) == null) {
			return false;
		}
		for (InsnNode insn : block.getInstructions()) {
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			switch (insn.getType()) {
				case MOVE:
				case CONST:
				case ARITH:
				case IGET:
				case SGET:
				case CAST:
				case CHECK_CAST:
					break;
				case INVOKE:
					InvokeNode invoke = (InvokeNode) insn;
					String declClass = invoke.getCallMth().getDeclClass().getFullName();
					String name = invoke.getCallMth().getName();
					if (!declClass.equals("java.lang.Math") || !(name.equals("min") || name.equals("max"))) {
						return false;
					}
					break;
				default:
					return false;
			}
		}
		return true;
	}

	static boolean isSafeLocalAssignmentDuplication(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		if (insns.isEmpty()) {
			return false;
		}
		for (InsnNode insn : insns) {
			InsnType type = insn.getType();
			if ((type != InsnType.CONST && type != InsnType.MOVE)
					|| insn.getResult() == null
					|| insn.getArgsCount() != 1) {
				return false;
			}
		}
		return true;
	}

	Region makeRegionAfterRemovingLoop(BlockNode startBlock) {
		List<Set<BlockNode>> outerStates = activeRegionStates.remove(startBlock);
		try {
			return makeRegion(startBlock);
		} finally {
			if (outerStates != null && !outerStates.isEmpty()) {
				activeRegionStates.put(startBlock, outerStates);
			}
		}
	}

	/**
	 * Recursively traverse all blocks from 'block' until block from 'exits'
	 */
	private @Nullable BlockNode traverse(Region r, BlockNode block) {
		if (block.contains(AFlag.MTH_EXIT_BLOCK)) {
			return null;
		}
		BlockNode next = null;
		boolean processed = false;

		List<LoopInfo> loops = block.getAll(AType.LOOP);
		int loopCount = loops.size();
		if (loopCount != 0 && block.contains(AFlag.LOOP_START)) {
			if (loopCount == 1) {
				next = loopMaker.process(r, loops.get(0), stack);
				processed = true;
			} else {
				for (LoopInfo loop : loops) {
					if (loop.getStart() == block) {
						next = loopMaker.process(r, loop, stack);
						processed = true;
						break;
					}
				}
			}
		}

		InsnNode insn = BlockUtils.getLastInsn(block);
		if (!processed && insn != null) {
			switch (insn.getType()) {
				case IF:
					next = ifMaker.process(r, block, (IfNode) insn, stack);
					processed = true;
					break;

				case SWITCH:
					SwitchRegionMaker switchMaker = new SwitchRegionMaker(mth, this);
					next = switchMaker.process(r, block, (SwitchInsn) insn, stack);
					processed = true;
					break;

				case MONITOR_ENTER:
					SynchronizedRegionMaker syncMaker = new SynchronizedRegionMaker(mth, this);
					next = syncMaker.process(r, block, insn, stack);
					processed = true;
					break;
			}
		}
		if (!processed) {
			r.add(block);
			next = getNextBlock(block);
		}
		if (next != null && !stack.containsExit(block) && !stack.containsExit(next)) {
			return next;
		}
		return null;
	}

	private void insertEdgeInsns(Region region, BlockNode exitBlock) {
		List<EdgeInsnAttr> edgeInsns = exitBlock.getAll(AType.EDGE_INSN);
		if (edgeInsns.isEmpty()) {
			return;
		}
		List<InsnNode> insns = new ArrayList<>(edgeInsns.size());
		addOneInsnOfType(insns, edgeInsns, InsnType.BREAK);
		addOneInsnOfType(insns, edgeInsns, InsnType.CONTINUE);
		region.add(new InsnContainer(insns));
	}

	private void addOneInsnOfType(List<InsnNode> insns, List<EdgeInsnAttr> edgeInsns, InsnType insnType) {
		for (EdgeInsnAttr edgeInsn : edgeInsns) {
			InsnNode insn = edgeInsn.getInsn();
			if (insn.getType() == insnType) {
				insns.add(insn);
				return;
			}
		}
	}

	RegionStack getStack() {
		return stack;
	}

	boolean isProcessed(BlockNode block) {
		return processedBlocks.contains(block);
	}

	void clearBlockProcessedState(BlockNode block) {
		processedBlocks.remove(block);
	}
}
