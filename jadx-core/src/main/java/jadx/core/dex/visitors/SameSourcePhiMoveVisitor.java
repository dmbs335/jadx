package jadx.core.dex.visitors;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.ssa.SSATransform;
import jadx.core.dex.visitors.typeinference.FinishTypeInference;

@JadxVisitor(
		name = "SameSourcePhiMoveVisitor",
		desc = "Normalize redundant branch moves before region construction",
		runAfter = FinishTypeInference.class,
		runBefore = AdjustForIfMergeVisitor.class
)
public class SameSourcePhiMoveVisitor extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		if (mth.isConstructor()) {
			if (mth.getBasicBlocks().size() <= 64
					&& !mth.getArgRegs().isEmpty()
					&& mth.getArgRegs().get(0).getInitType().equals(ArgType.STRING)) {
				SSATransform.inlineSameSourceMovePhis(mth);
			}
			return;
		}
		if (!mth.getAccessFlags().isStatic()
				|| !mth.getReturnType().equals(ArgType.VOID)
				|| mth.getArgRegs().size() < 3) {
			return;
		}
		if (mth.getArgRegs().get(0).getInitType().equals(ArgType.BYTE)
				&& mth.getArgRegs().get(1).getInitType().equals(ArgType.BYTE)
				&& mth.getArgRegs().get(2).getInitType().equals(ArgType.BYTE)
				&& mth.getBasicBlocks().size() <= 64) {
			SSATransform.inlineBranchProvenConstMovePhis(mth);
		}
	}
}
