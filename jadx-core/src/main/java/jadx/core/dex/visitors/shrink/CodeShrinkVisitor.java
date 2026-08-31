package jadx.core.dex.visitors.shrink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeCustomNode;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.Named;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.ModVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;

@JadxVisitor(
		name = "CodeShrinkVisitor",
		desc = "Inline variables to make code smaller",
		runAfter = { ModVisitor.class }
)
public class CodeShrinkVisitor extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) {
		shrinkMethod(mth);
	}

	public static void shrinkMethod(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		mth.remove(AFlag.REQUEST_CODE_SHRINK);
		List<BlockNode> blocks = mth.getBasicBlocks();
		int blocksCount = blocks.size();
		int maxInsnsCount = 0;
		for (int i = 0; i < blocksCount; i++) {
			maxInsnsCount = Math.max(maxInsnsCount, blocks.get(i).getInstructions().size());
		}
		ArgsInfo[] argsList = new ArgsInfo[maxInsnsCount];
		for (int i = 0; i < blocksCount; i++) {
			BlockNode block = blocks.get(i);
			shrinkBlock(mth, block, argsList);
			simplifyMoveInsns(mth, block);
		}
	}

	private static void shrinkBlock(MethodNode mth, BlockNode block, ArgsInfo[] argsList) {
		if (block.getInstructions().isEmpty()) {
			return;
		}
		List<InsnNode> insnList = block.getInstructions();
		int insnCount = insnList.size();
		for (int i = 0; i < insnCount; i++) {
			argsList[i] = new ArgsInfo(insnList.get(i), argsList, i);
		}
		try {
			List<WrapInfo> wrapList = null;
			for (int argsInfoIndex = 0; argsInfoIndex < insnCount; argsInfoIndex++) {
				ArgsInfo argsInfo = argsList[argsInfoIndex];
				for (int i = argsInfo.getArgsCount() - 1; i >= 0; i--) {
					WrapInfo wrapInfo = checkInline(mth, block, insnList, argsInfo, argsInfo.getArg(i));
					if (wrapInfo != null) {
						if (wrapList == null) {
							wrapList = new ArrayList<>(1);
						}
						wrapList.add(wrapInfo);
					}
				}
			}
			if (wrapList != null) {
				int wrapCount = wrapList.size();
				for (int i = 0; i < wrapCount; i++) {
					WrapInfo wrapInfo = wrapList.get(i);
					inline(mth, wrapInfo.getArg(), wrapInfo.getInsn(), block);
				}
			}
		} finally {
			Arrays.fill(argsList, 0, insnCount, null);
		}
	}

	private static WrapInfo checkInline(MethodNode mth, BlockNode block, List<InsnNode> insnList,
			ArgsInfo argsInfo, RegisterArg arg) {
		if (arg.contains(AFlag.DONT_INLINE)
				|| arg.getParentInsn() == null
				|| arg.getParentInsn().contains(AFlag.DONT_GENERATE)) {
			return null;
		}
		SSAVar sVar = arg.getSVar();
		if (sVar == null || sVar.getAssign().contains(AFlag.DONT_INLINE)) {
			return null;
		}
		InsnNode assignInsn = sVar.getAssign().getParentInsn();
		if (assignInsn == null
				|| assignInsn.contains(AFlag.DONT_INLINE)
				|| assignInsn.contains(AFlag.WRAPPED)) {
			return null;
		}
		boolean assignInline = assignInsn.contains(AFlag.FORCE_ASSIGN_INLINE);
		if (!assignInline && sVar.isUsedInPhi()) {
			return null;
		}
		// allow inline only one use arg
		int useCount = 0;
		List<RegisterArg> useList = sVar.getUseList();
		int usesCount = useList.size();
		for (int i = 0; i < usesCount; i++) {
			RegisterArg useArg = useList.get(i);
			InsnNode parentInsn = useArg.getParentInsn();
			if (parentInsn != null && parentInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (!assignInline && useArg.contains(AFlag.DONT_INLINE_CONST)) {
				return null;
			}
			useCount++;
		}
		if (!assignInline && useCount != 1) {
			return null;
		}
		if (!assignInline && sVar.getName() != null) {
			if (searchArgWithName(assignInsn, sVar.getName())) {
				// allow inline if name is reused in result
			} else if (varWithSameNameExists(mth, sVar)) {
				// allow inline if var name is duplicated
			} else {
				// reject inline of named variable
				return null;
			}
		}
		if (!checkLambdaInline(arg, assignInsn)) {
			return null;
		}

		int assignPos = InsnList.getIndex(insnList, assignInsn);
		if (assignPos != -1) {
			return argsInfo.checkInline(assignPos, arg);
		}
		// another block
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		if (assignBlock != null
				&& assignInsn != arg.getParentInsn()
				&& canMoveBetweenBlocks(mth, assignInsn, assignBlock, block, argsInfo.getInsn())) {
			if (assignInline) {
				assignInline(mth, arg, assignInsn, assignBlock);
			} else {
				inline(mth, arg, assignInsn, assignBlock);
			}
		}
		return null;
	}

	/**
	 * Forbid inline lambda into invoke as an instance arg, i.e. this will not compile:
	 * {@code () -> { ... }.apply(); }
	 */
	private static boolean checkLambdaInline(RegisterArg arg, InsnNode assignInsn) {
		if (assignInsn.getType() == InsnType.INVOKE && assignInsn instanceof InvokeCustomNode) {
			List<RegisterArg> useList = arg.getSVar().getUseList();
			int usesCount = useList.size();
			for (int i = 0; i < usesCount; i++) {
				RegisterArg useArg = useList.get(i);
				InsnNode parentInsn = useArg.getParentInsn();
				if (parentInsn != null && parentInsn.getType() == InsnType.INVOKE) {
					InvokeNode invokeNode = (InvokeNode) parentInsn;
					InsnArg instArg = invokeNode.getInstanceArg();
					if (instArg != null && instArg == useArg) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static boolean varWithSameNameExists(MethodNode mth, SSAVar inlineVar) {
		List<SSAVar> ssaVars = mth.getSVars();
		int varsCount = ssaVars.size();
		for (int i = 0; i < varsCount; i++) {
			SSAVar ssaVar = ssaVars.get(i);
			if (ssaVar == inlineVar || ssaVar.getCodeVar() == inlineVar.getCodeVar()) {
				continue;
			}
			if (Objects.equals(ssaVar.getName(), inlineVar.getName())) {
				return ssaVar.getUseCount() > inlineVar.getUseCount();
			}
		}
		return false;
	}

	private static boolean searchArgWithName(InsnNode assignInsn, String varName) {
		InsnArg result = assignInsn.visitArgs(insnArg -> {
			if (insnArg instanceof Named) {
				String argName = ((Named) insnArg).getName();
				if (Objects.equals(argName, varName)) {
					return insnArg;
				}
			}
			return null;
		});
		return result != null;
	}

	private static boolean assignInline(MethodNode mth, RegisterArg arg, InsnNode assignInsn, BlockNode assignBlock) {
		RegisterArg useArg = arg.getSVar().getUseList().get(0);
		InsnNode useInsn = useArg.getParentInsn();
		if (useInsn == null || useInsn.contains(AFlag.DONT_GENERATE)) {
			return false;
		}
		if (!InsnRemover.removeWithoutUnbind(mth, assignBlock, assignInsn)) {
			return false;
		}
		useArg.wrapInstruction(mth, assignInsn);
		return true;
	}

	private static boolean inline(MethodNode mth, RegisterArg arg, InsnNode insn, BlockNode block) {
		if (insn.contains(AFlag.FORCE_ASSIGN_INLINE)) {
			return assignInline(mth, arg, insn, block);
		}
		// just move instruction into arg, don't unbind/copy/duplicate
		InsnArg wrappedArg = arg.wrapInstruction(mth, insn, false);
		boolean replaced = wrappedArg != null;
		if (replaced) {
			InsnNode parentInsn = arg.getParentInsn();
			if (parentInsn != null) {
				parentInsn.inheritMetadata(insn);
			}
			InsnRemover.unbindResult(mth, insn);
			InsnRemover.removeWithoutUnbind(mth, block, insn);
		}
		return replaced;
	}

	private static boolean canMoveBetweenBlocks(MethodNode mth, InsnNode assignInsn, BlockNode assignBlock,
			BlockNode useBlock, InsnNode useInsn) {
		if (!BlockUtils.isPathExists(assignBlock, useBlock)) {
			return false;
		}

		BitSet args = new BitSet();
		ArgsInfo.fillArgsSet(assignInsn, args);
		boolean startCheck = false;
		List<InsnNode> assignInsns = assignBlock.getInstructions();
		int assignInsnsCount = assignInsns.size();
		for (int insnIndex = 0; insnIndex < assignInsnsCount; insnIndex++) {
			InsnNode insn = assignInsns.get(insnIndex);
			if (startCheck && (!insn.canReorder() || ArgsInfo.usedArgAssign(insn, args))) {
				return false;
			}
			if (insn == assignInsn) {
				startCheck = true;
			}
		}
		Set<BlockNode> pathsBlocks = BlockUtils.getAllPathsBlocks(assignBlock, useBlock);
		pathsBlocks.remove(assignBlock);
		pathsBlocks.remove(useBlock);
		for (BlockNode block : pathsBlocks) {
			if (block.contains(AFlag.DONT_GENERATE)) {
				if (BlockUtils.checkLastInsnType(block, InsnType.MONITOR_EXIT)) {
					if (RegionUtils.isBlocksInSameRegion(mth, assignBlock, useBlock)) {
						// allow move inside same synchronized region
					} else {
						// don't move from synchronized block
						return false;
					}
				}
				// skip checks for not generated blocks
				continue;
			}
			List<InsnNode> blockInsns = block.getInstructions();
			int blockInsnsCount = blockInsns.size();
			for (int insnIndex = 0; insnIndex < blockInsnsCount; insnIndex++) {
				InsnNode insn = blockInsns.get(insnIndex);
				if (!insn.canReorder() || ArgsInfo.usedArgAssign(insn, args)) {
					return false;
				}
			}
		}
		List<InsnNode> useInsns = useBlock.getInstructions();
		int useInsnsCount = useInsns.size();
		for (int insnIndex = 0; insnIndex < useInsnsCount; insnIndex++) {
			InsnNode insn = useInsns.get(insnIndex);
			if (insn == useInsn) {
				return true;
			}
			if (!insn.canReorder() || ArgsInfo.usedArgAssign(insn, args)) {
				return false;
			}
		}
		throw new JadxRuntimeException("Can't process instruction move : " + assignBlock);
	}

	private static void simplifyMoveInsns(MethodNode mth, BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		int size = insns.size();
		for (int i = 0; i < size; i++) {
			InsnNode insn = insns.get(i);
			if (insn.getType() == InsnType.MOVE) {
				// replace 'move' with wrapped insn
				InsnArg arg = insn.getArg(0);
				if (arg.isInsnWrap()) {
					InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
					InsnRemover.unbindResult(mth, wrapInsn);
					wrapInsn.setResult(insn.getResult().duplicate());
					wrapInsn.inheritMetadata(insn);
					wrapInsn.setOffset(insn.getOffset());
					wrapInsn.remove(AFlag.WRAPPED);
					block.getInstructions().set(i, wrapInsn);
				}
			}
		}
	}
}
