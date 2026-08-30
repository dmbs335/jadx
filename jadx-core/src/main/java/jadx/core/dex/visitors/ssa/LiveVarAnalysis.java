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
		this.uses = initBitSetArray(bbCount, regsCount);
		this.defs = initBitSetArray(bbCount, regsCount);
		this.assignBlocks = initBitSetArray(regsCount, bbCount);
		fillBasicBlockInfo();
		processLiveInfo();
	}

	public BitSet getAssignBlocks(int regNum) {
		return assignBlocks[regNum];
	}

	public boolean isLive(int blockId, int regNum) {
		if (blockId >= liveIn.length) {
			LOG.warn("LiveVarAnalysis: out of bounds block: {}, max: {}", blockId, liveIn.length);
			return false;
		}
		return liveIn[blockId].get(regNum);
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
			if (defs[blockId].get(regNum)) {
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
						if (!kill.get(regNum)) {
							gen.set(regNum);
						}
					}
				}
				RegisterArg result = insn.getResult();
				if (result != null) {
					int regNum = result.getRegNum();
					kill.set(regNum);
					assignBlocks[regNum].set(blockId);
				}
			}
		}
	}

	private void processLiveInfo() {
		int bbCount = mth.getBasicBlocks().size();
		int regsCount = mth.getRegsCount();
		BitSet[] liveInBlocks = initBitSetArray(bbCount, regsCount);
		List<BlockNode> blocks = mth.getBasicBlocks();
		int blocksCount = blocks.size();
		int iterationsLimit = blocksCount * 10;
		BitSet newIn = new BitSet(regsCount);
		boolean changed;
		int k = 0;
		do {
			changed = false;
			for (int blockIndex = 0; blockIndex < blocksCount; blockIndex++) {
				BlockNode block = blocks.get(blockIndex);
				int blockId = block.getId();
				BitSet prevIn = liveInBlocks[blockId];
				newIn.clear();
				List<BlockNode> successors = block.getSuccessors();
				int successorsCount = successors.size();
				for (int successorIndex = 0; successorIndex < successorsCount; successorIndex++) {
					BlockNode successor = successors.get(successorIndex);
					newIn.or(liveInBlocks[successor.getId()]);
				}
				newIn.andNot(defs[blockId]);
				newIn.or(uses[blockId]);
				if (!prevIn.equals(newIn)) {
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

	private static BitSet[] initBitSetArray(int length, int bitsCount) {
		BitSet[] array = new BitSet[length];
		for (int i = 0; i < length; i++) {
			array[i] = new BitSet(bitsCount);
		}
		return array;
	}
}
