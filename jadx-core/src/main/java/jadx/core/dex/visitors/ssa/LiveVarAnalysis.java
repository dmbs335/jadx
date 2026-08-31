package jadx.core.dex.visitors.ssa;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.EmptyBitSet;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class LiveVarAnalysis {
	private static final Logger LOG = LoggerFactory.getLogger(LiveVarAnalysis.class);

	private final MethodNode mth;

	private BitSet[] uses;
	private BitSet[] defs;
	private BitSet[] liveIn;
	private BitSet[] assignBlocks;
	private BitSet[] undefinedReachable;

	public LiveVarAnalysis(MethodNode mth) {
		this.mth = mth;
	}

	public void runAnalysis() {
		int bbCount = mth.getBasicBlocks().size();
		int regsCount = mth.getRegsCount();
		this.uses = new BitSet[bbCount];
		this.defs = new BitSet[bbCount];
		this.assignBlocks = new BitSet[regsCount];
		fillBasicBlockInfo();
		processLiveInfo();
	}

	public BitSet getAssignBlocks(int regNum) {
		BitSet blocks = assignBlocks[regNum];
		return blocks == null ? EmptyBitSet.EMPTY : blocks;
	}

	public boolean isLive(int blockId, int regNum) {
		if (blockId >= liveIn.length) {
			LOG.warn("LiveVarAnalysis: out of bounds block: {}, max: {}", blockId, liveIn.length);
			return false;
		}
		BitSet blockLiveIn = liveIn[blockId];
		return blockLiveIn != null && blockLiveIn.get(regNum);
	}

	public boolean isLive(BlockNode block, int regNum) {
		return isLive(block.getId(), regNum);
	}

	public boolean isDefinedOnAllPaths(BlockNode block, int regNum) {
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg != null && thisArg.getRegNum() == regNum) {
			return true;
		}
		for (RegisterArg arg : mth.getArgRegs()) {
			if (arg.getRegNum() == regNum) {
				return true;
			}
		}
		if (undefinedReachable == null) {
			undefinedReachable = new BitSet[mth.getRegsCount()];
		}
		BitSet reachable = undefinedReachable[regNum];
		if (reachable == null) {
			reachable = collectUndefinedReachable(regNum);
			undefinedReachable[regNum] = reachable;
		}
		return !reachable.get(block.getId());
	}

	private BitSet collectUndefinedReachable(int regNum) {
		BitSet reachable = new BitSet(mth.getBasicBlocks().size());
		Deque<BlockNode> queue = new ArrayDeque<>();
		queue.add(mth.getEnterBlock());
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			int blockId = block.getId();
			if (reachable.get(blockId)) {
				continue;
			}
			reachable.set(blockId);
			BitSet blockDefs = defs[blockId];
			if (blockDefs != null && blockDefs.get(regNum)) {
				continue;
			}
			queue.addAll(block.getSuccessors());
		}
		return reachable;
	}

	private void fillBasicBlockInfo() {
		List<BlockNode> blocks = mth.getBasicBlocks();
		int blocksCount = blocks.size();
		for (int blockIndex = 0; blockIndex < blocksCount; blockIndex++) {
			BlockNode block = blocks.get(blockIndex);
			int blockId = block.getId();
			BitSet gen = uses[blockId];
			BitSet kill = defs[blockId];
			List<InsnNode> insns = block.getInstructions();
			int insnsCount = insns.size();
			for (int insnIndex = 0; insnIndex < insnsCount; insnIndex++) {
				InsnNode insn = insns.get(insnIndex);
				int argsCount = insn.getArgsCount();
				for (int argIndex = 0; argIndex < argsCount; argIndex++) {
					InsnArg arg = insn.getArg(argIndex);
					if (arg.isRegister()) {
						int regNum = ((RegisterArg) arg).getRegNum();
						if (kill == null || !kill.get(regNum)) {
							if (gen == null) {
								gen = new BitSet();
								uses[blockId] = gen;
							}
							gen.set(regNum);
						}
					}
				}
				RegisterArg result = insn.getResult();
				if (result != null) {
					int regNum = result.getRegNum();
					if (kill == null) {
						kill = new BitSet();
						defs[blockId] = kill;
					}
					kill.set(regNum);
					BitSet assignedBlocks = assignBlocks[regNum];
					if (assignedBlocks == null) {
						assignedBlocks = new BitSet(blocksCount);
						assignBlocks[regNum] = assignedBlocks;
					}
					assignedBlocks.set(blockId);
				}
			}
		}
	}

	private void processLiveInfo() {
		int bbCount = mth.getBasicBlocks().size();
		int regsCount = mth.getRegsCount();
		BitSet[] liveInBlocks = new BitSet[bbCount];
		List<BlockNode> blocks = mth.getBasicBlocks();
		int blocksCount = blocks.size();
		int iterationsLimit = blocksCount * 10;
		BitSet newIn = new BitSet(regsCount);
		boolean changed;
		int k = 0;
		do {
			changed = false;
			// Liveness flows from successors to predecessors. Basic blocks are normally ordered
			// in the forward CFG direction, so walking them backwards propagates values through
			// linear regions in one pass instead of one block per fixed-point iteration.
			for (int blockIndex = blocksCount - 1; blockIndex >= 0; blockIndex--) {
				BlockNode block = blocks.get(blockIndex);
				int blockId = block.getId();
				BitSet prevIn = liveInBlocks[blockId];
				newIn.clear();
				List<BlockNode> successors = block.getSuccessors();
				int successorsCount = successors.size();
				for (int successorIndex = 0; successorIndex < successorsCount; successorIndex++) {
					BlockNode successor = successors.get(successorIndex);
					BitSet successorLiveIn = liveInBlocks[successor.getId()];
					if (successorLiveIn != null) {
						newIn.or(successorLiveIn);
					}
				}
				BitSet blockDefs = defs[blockId];
				if (blockDefs != null) {
					newIn.andNot(blockDefs);
				}
				BitSet blockUses = uses[blockId];
				if (blockUses != null) {
					newIn.or(blockUses);
				}
				if (prevIn == null) {
					if (newIn.isEmpty()) {
						continue;
					}
					changed = true;
					liveInBlocks[blockId] = (BitSet) newIn.clone();
				} else if (!prevIn.equals(newIn)) {
					changed = true;
					liveInBlocks[blockId] = newIn;
					newIn = prevIn;
				}
			}
			if (k++ > iterationsLimit) {
				throw new JadxRuntimeException("Live variable analysis reach iterations limit, blocks count: " + blocksCount);
			}
		} while (changed);

		this.liveIn = liveInBlocks;
	}

}
