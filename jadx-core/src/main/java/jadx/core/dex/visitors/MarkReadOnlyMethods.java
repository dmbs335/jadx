package jadx.core.dex.visitors;

import java.util.List;

import jadx.core.dex.attributes.nodes.ConstantReturnMethodAttr;
import jadx.core.dex.attributes.nodes.ReadOnlyMethodAttr;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.regions.RegionMakerVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.exceptions.JadxException;

@JadxVisitor(
		name = "MarkReadOnlyMethods",
		desc = "Mark final trivial instance field getters as read-only",
		runAfter = ReplaceNewArray.class,
		runBefore = RegionMakerVisitor.class
)
public class MarkReadOnlyMethods extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (isTrivialInstanceFieldGetter(mth)) {
			mth.addAttr(ReadOnlyMethodAttr.INSTANCE);
		}
		if (isTrivialConstantReturn(mth)) {
			mth.addAttr(ReadOnlyMethodAttr.INSTANCE);
			mth.addAttr(ConstantReturnMethodAttr.INSTANCE);
		}
	}

	static boolean isTrivialInstanceFieldGetter(MethodNode mth) {
		if (mth.isNoCode()
				|| mth.isConstructor()
				|| mth.getAccessFlags().isStatic()
				|| mth.getAccessFlags().isSynchronized()
				|| !mth.isNoExceptionHandlers()
				|| !mth.getArgRegs().isEmpty()
				|| !mth.getAccessFlags().isFinal()
						&& !mth.getAccessFlags().isPrivate()
						&& !mth.getParentClass().getAccessFlags().isFinal()) {
			return false;
		}
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg == null) {
			return false;
		}
		List<InsnNode> insns = BlockUtils.collectInsnsWithLimit(mth.getBasicBlocks(), 2);
		if (insns.size() == 1) {
			InsnNode returnInsn = insns.get(0);
			if (returnInsn.getType() != InsnType.RETURN || returnInsn.getArgsCount() != 1) {
				return false;
			}
			InsnArg returnArg = returnInsn.getArg(0);
			if (!(returnArg instanceof InsnWrapArg)) {
				return false;
			}
			return isThisFieldRead(mth, ((InsnWrapArg) returnArg).getWrapInsn(), thisArg);
		}
		if (insns.size() == 2) {
			InsnNode fieldRead = insns.get(0);
			InsnNode returnInsn = insns.get(1);
			RegisterArg result = fieldRead.getResult();
			return result != null
					&& isThisFieldRead(mth, fieldRead, thisArg)
					&& returnInsn.getType() == InsnType.RETURN
					&& returnInsn.getArgsCount() == 1
					&& returnInsn.getArg(0).isSameVar(result);
		}
		return false;
	}

	private static boolean isThisFieldRead(MethodNode mth, InsnNode insn, RegisterArg thisArg) {
		if (insn.getType() != InsnType.IGET
				|| !(insn instanceof IndexInsnNode)
				|| !(((IndexInsnNode) insn).getIndex() instanceof FieldInfo)
				|| insn.getArgsCount() != 1
				|| !insn.getArg(0).isSameVar(thisArg)) {
			return false;
		}
		FieldNode field = mth.root().resolveField((FieldInfo) ((IndexInsnNode) insn).getIndex());
		return field != null
				&& !field.getAccessFlags().isVolatile();
	}

	static boolean isTrivialConstantReturn(MethodNode mth) {
		if (mth.isNoCode()
				|| mth.isConstructor()
				|| mth.getAccessFlags().isStatic()
				|| mth.getAccessFlags().isSynchronized()
				|| !mth.isNoExceptionHandlers()
				|| !mth.getArgRegs().isEmpty()
				|| !mth.getAccessFlags().isFinal()
						&& !mth.getAccessFlags().isPrivate()
						&& !mth.getParentClass().getAccessFlags().isFinal()) {
			return false;
		}
		List<InsnNode> insns = BlockUtils.collectInsnsWithLimit(mth.getBasicBlocks(), 2);
		if (insns.size() == 1) {
			InsnNode returnInsn = insns.get(0);
			if (returnInsn.getType() != InsnType.RETURN || returnInsn.getArgsCount() != 1) {
				return false;
			}
			InsnArg returnArg = returnInsn.getArg(0);
			return returnArg.isLiteral()
					|| returnArg instanceof InsnWrapArg
							&& isConstantInsn(((InsnWrapArg) returnArg).getWrapInsn());
		}
		if (insns.size() == 2) {
			InsnNode valueInsn = insns.get(0);
			InsnNode returnInsn = insns.get(1);
			RegisterArg result = valueInsn.getResult();
			return result != null
					&& isConstantInsn(valueInsn)
					&& returnInsn.getType() == InsnType.RETURN
					&& returnInsn.getArgsCount() == 1
					&& returnInsn.getArg(0).isSameVar(result);
		}
		return false;
	}

	private static boolean isConstantInsn(InsnNode insn) {
		InsnType type = insn.getType();
		return type == InsnType.CONST || type == InsnType.CONST_STR || type == InsnType.CONST_CLASS;
	}
}
