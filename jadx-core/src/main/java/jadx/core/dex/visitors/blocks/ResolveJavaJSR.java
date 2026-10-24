package jadx.core.dex.visitors.blocks;

import java.util.ArrayList;
import java.util.List;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;

/**
 * Duplicate code to resolve java jsr/ret.
 * JSR (jump subroutine) allows executing the same code from different places.
 * Used mostly for 'finally' blocks, deprecated in Java 7.
 */
public class ResolveJavaJSR {

	public static void process(MethodNode mth) {
		int blocksCount = mth.getBasicBlocks().size();
		int k = 0;
		while (true) {
			boolean changed = resolve(mth);
			if (!changed) {
				break;
			}
			if (k++ > blocksCount) {
				throw new JadxRuntimeException("Fail to resolve jsr instructions");
			}
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.JAVA_JSR || insn.getType() == InsnType.JAVA_RET) {
					throw new JadxRuntimeException("Unresolved JVM jsr/ret subroutine");
				}
			}
		}
	}

	private static boolean resolve(MethodNode mth) {
		List<BlockNode> blocks = mth.getBasicBlocks();
		for (BlockNode block : blocks) {
			if (BlockUtils.checkLastInsnType(block, InsnType.JAVA_RET)
					&& resolveForRetBlock(mth, block)) {
				return true;
			}
		}
		return false;
	}

	private static boolean resolveForRetBlock(MethodNode mth, BlockNode retBlock) {
		boolean[] resolved = { false };
		BlockUtils.visitPredecessorsUntil(mth, retBlock, startBlock -> {
			List<BlockNode> preds = startBlock.getPredecessors();
			if (!preds.isEmpty()
					&& preds.stream().allMatch(p -> BlockUtils.checkLastInsnType(p, InsnType.JAVA_JSR))) {
				List<BlockNode> jsrBlocks = new ArrayList<>(preds);
				List<BlockNode> dupBlocks = BlockUtils.collectAllSuccessors(mth, startBlock, false);
				List<BlockNode> retBlocks = new ArrayList<>();
				int retReg = ((RegisterArg) BlockUtils.getLastInsn(retBlock).getArg(0)).getRegNum();
				for (BlockNode subBlock : dupBlocks) {
					if (BlockUtils.checkLastInsnType(subBlock, InsnType.JAVA_RET)) {
						InsnNode subRet = BlockUtils.getLastInsn(subBlock);
						if (((RegisterArg) subRet.getArg(0)).getRegNum() != retReg) {
							return false;
						}
						retBlocks.add(subBlock);
					}
				}
				removeInsns(retBlocks, startBlock, jsrBlocks);
				processBlocks(mth, retBlocks, startBlock, jsrBlocks, dupBlocks);
				resolved[0] = true;
				return true;
			}
			return false;
		});
		return resolved[0];
	}

	private static void removeInsns(List<BlockNode> retBlocks, BlockNode startBlock, List<BlockNode> jsrBlocks) {
		InsnNode retInsn = ListUtils.removeLast(retBlocks.get(0).getInstructions());
		for (int i = 1; i < retBlocks.size(); i++) {
			ListUtils.removeLast(retBlocks.get(i).getInstructions());
		}
		if (retInsn != null && retInsn.getType() == InsnType.JAVA_RET) {
			InsnArg retArg = retInsn.getArg(0);
			if (retArg.isRegister()) {
				int regNum = ((RegisterArg) retArg).getRegNum();
				InsnNode startInsn = BlockUtils.getFirstInsn(startBlock);
				if (startInsn != null
						&& startInsn.getType() == InsnType.MOVE
						&& startInsn.getResult().getRegNum() == regNum) {
					startBlock.getInstructions().remove(0);
				}
			}
		}
		jsrBlocks.forEach(p -> ListUtils.removeLast(p.getInstructions()));
	}

	private static void processBlocks(MethodNode mth, List<BlockNode> retBlocks, BlockNode startBlock,
			List<BlockNode> jsrBlocks, List<BlockNode> dupBlocks) {
		BlockNode first = null;
		for (BlockNode jsrBlock : jsrBlocks) {
			if (first == null) {
				first = jsrBlock;
			} else {
				BlockNode pathBlock = BlockUtils.selectOther(startBlock, jsrBlock.getSuccessors());
				BlockSplitter.removeConnection(jsrBlock, startBlock);
				BlockSplitter.removeConnection(jsrBlock, pathBlock);
				List<BlockNode> newBlocks = BlockSplitter.copyBlocksTree(mth, dupBlocks);
				BlockNode newStart = newBlocks.get(dupBlocks.indexOf(startBlock));
				BlockSplitter.connect(jsrBlock, newStart);
				for (BlockNode retBlock : retBlocks) {
					BlockNode newRetBlock = newBlocks.get(dupBlocks.indexOf(retBlock));
					BlockSplitter.connect(newRetBlock, pathBlock);
				}
			}
		}
		if (first != null) {
			BlockNode pathBlock = BlockUtils.selectOther(startBlock, first.getSuccessors());
			BlockSplitter.removeConnection(first, pathBlock);
			for (BlockNode retBlock : retBlocks) {
				BlockSplitter.connect(retBlock, pathBlock);
			}
		}
	}
}
