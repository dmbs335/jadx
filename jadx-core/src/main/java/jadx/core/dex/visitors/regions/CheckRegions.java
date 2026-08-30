package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeWriter;
import jadx.api.impl.SimpleCodeWriter;
import jadx.core.Consts;
import jadx.core.codegen.InsnGen;
import jadx.core.codegen.MethodGen;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.exceptions.CodegenException;
import jadx.core.utils.exceptions.JadxException;

public class CheckRegions extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(CheckRegions.class);

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()
				|| mth.getRegion() == null
				|| mth.getBasicBlocks().isEmpty()
				|| mth.contains(AType.JADX_ERROR)) {
			return;
		}

		// check if all blocks included in regions
		Set<BlockNode> blocksInRegions = new HashSet<>();
		DepthRegionTraversal.traverse(mth, new AbstractRegionVisitor() {
			@Override
			public void processBlock(MethodNode mth, IBlock container) {
				if (!(container instanceof BlockNode)) {
					return;
				}
				BlockNode block = (BlockNode) container;
				if (blocksInRegions.add(block)) {
					return;
				}
				if (Consts.DEBUG_RESTRUCTURE
						&& LOG.isDebugEnabled()
						&& !block.contains(AFlag.RETURN)
						&& !block.contains(AFlag.REMOVE)
						&& !block.contains(AFlag.SYNTHETIC)
						&& !block.getInstructions().isEmpty()) {
					LOG.debug("Duplicated block: {} - {}", mth, block);
				}
			}
		});
		if (mth.getBasicBlocks().size() != blocksInRegions.size()) {
			for (BlockNode block : mth.getBasicBlocks()) {
				if (!blocksInRegions.contains(block) && inlineSyntheticSameRegisterMove(mth, block)) {
					continue;
				}
				if (!blocksInRegions.contains(block)
						&& !block.getInstructions().isEmpty()
						&& !block.contains(AFlag.ADDED_TO_REGION)
						&& !block.contains(AFlag.DONT_GENERATE)
						&& !block.contains(AFlag.REMOVE)
						&& !isImplicitVoidReturn(mth, block, blocksInRegions)
						&& !isGeneratedSyntheticDuplicate(block, blocksInRegions)) {
					String blockCode = getBlockInsnStr(mth, block).replace("*/", "*\\/");
					mth.addWarn("Code restructure failed: missing block: " + block + ", code lost:" + blockCode);
				}
			}
		}

		DepthRegionTraversal.traverse(mth, new AbstractRegionVisitor() {
			@Override
			public boolean enterRegion(MethodNode mth, IRegion region) {
				if (region instanceof LoopRegion) {
					// check loop conditions
					BlockNode loopHeader = ((LoopRegion) region).getHeader();
					if (loopHeader != null && !loopHeader.contains(AFlag.ALLOW_MULTIPLE_INSNS_LOOP_COND)
							&& loopHeader.getInstructions().size() != 1) {
						mth.addWarn("Incorrect condition in loop: " + loopHeader);
					}
				}
				return true;
			}
		});
	}

	/**
	 * A shared return-void block may remain outside the region tree after all live predecessors have
	 * been represented. Falling through the Java method brace is exactly equivalent; instructions in
	 * any predecessor still outside the tree keep the normal lost-code warning.
	 */
	static boolean isImplicitVoidReturn(MethodNode mth, BlockNode block, Set<BlockNode> blocksInRegions) {
		if (!mth.getReturnType().equals(jadx.core.dex.instructions.args.ArgType.VOID)
				|| block.getInstructions().size() != 1) {
			return false;
		}
		InsnNode insn = block.getInstructions().get(0);
		if (insn.getType() != InsnType.RETURN || insn.getArgsCount() != 0
				|| block.getPredecessors().isEmpty()) {
			return false;
		}
		return block.getPredecessors().stream().allMatch(predecessor -> blocksInRegions.contains(predecessor)
				|| predecessor.contains(AFlag.DONT_GENERATE)
				|| predecessor.contains(AFlag.REMOVE));
	}

	static boolean isGeneratedSyntheticDuplicate(BlockNode block, Set<BlockNode> blocksInRegions) {
		return block.contains(AFlag.SYNTHETIC)
				&& blocksInRegions.stream().anyMatch(generated -> BlockUtils.isDuplicateBlockPath(block, generated));
	}

	/**
	 * Region simplification can inline every effective instruction from a branch block and leave only
	 * a synthetic SSA bridge behind. A bridge between two versions of the same physical DEX register
	 * with the exact same known type does not represent a runtime write.
	 *
	 * Redirecting its Java consumers to the reaching source avoids both a lost-code warning and an
	 * uninitialized split local. Keep this proof deliberately narrow: one synthetic register MOVE,
	 * the same register number, the same known type, and only attached consumers.
	 */
	static boolean isSyntheticSameRegisterMoveBlock(BlockNode block) {
		if (block.getInstructions().size() != 1) {
			return false;
		}
		InsnNode insn = block.getInstructions().get(0);
		if (insn.getType() != InsnType.MOVE || insn.getArgsCount() != 1 || !insn.contains(AFlag.SYNTHETIC)) {
			return false;
		}
		RegisterArg result = insn.getResult();
		InsnArg sourceArg = insn.getArg(0);
		if (result == null || result.getSVar() == null || !sourceArg.isRegister()) {
			return false;
		}
		RegisterArg source = (RegisterArg) sourceArg;
		return source.getSVar() != null
				&& result.getRegNum() == source.getRegNum()
				&& result.getType().isTypeKnown()
				&& result.getType().equals(source.getType())
				&& result.getSVar().getUseList().stream().allMatch(use -> use.getParentInsn() != null);
	}

	private static boolean inlineSyntheticSameRegisterMove(MethodNode mth, BlockNode block) {
		if (!isSyntheticSameRegisterMoveBlock(block)) {
			return false;
		}
		InsnNode move = block.getInstructions().get(0);
		RegisterArg source = (RegisterArg) move.getArg(0);
		for (RegisterArg use : new ArrayList<>(move.getResult().getSVar().getUseList())) {
			use.getSVar().removeUse(use);
			source.getSVar().use(use);
		}
		InsnRemover.remove(mth, block, move);
		return true;
	}

	private static String getBlockInsnStr(MethodNode mth, IBlock block) {
		ICodeWriter code = new SimpleCodeWriter();
		code.incIndent();
		code.newLine();
		MethodGen mg = MethodGen.getFallbackMethodGen(mth);
		InsnGen ig = new InsnGen(mg, true);
		for (InsnNode insn : block.getInstructions()) {
			try {
				ig.makeInsn(insn, code);
			} catch (CodegenException e) {
				// ignore
			}
		}
		code.newLine();
		return code.getCodeStr();
	}
}
