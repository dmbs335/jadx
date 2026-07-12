package jadx.core.dex.visitors.typeinference;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;

@JadxVisitor(
		name = "Finish Type Inference",
		desc = "Check used types",
		runAfter = {
				TypeInferenceVisitor.class
		}
)
public final class FinishTypeInference extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode() || mth.getSVars().isEmpty()) {
			return;
		}
		mth.getSVars().forEach(var -> {
			ArgType type = var.getTypeInfo().getType();
			if (!type.isTypeKnown() && hasGeneratedUse(var)) {
				mth.addWarnComment("Type inference failed for: " + var.getDetailedVarInfo(mth));
			}
			ArgType codeVarType = var.getCodeVar().getType();
			if (codeVarType == null) {
				var.getCodeVar().setType(ArgType.UNKNOWN);
			}
		});
	}

	static boolean hasGeneratedUse(SSAVar var) {
		return var.getUseList().stream()
				.map(arg -> arg.getParentInsn())
				.anyMatch(insn -> insn != null && !insn.contains(AFlag.DONT_GENERATE));
	}

	@Override
	public String getName() {
		return "FinishTypeInference";
	}
}
