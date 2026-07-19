package jadx.core.dex.visitors.typeinference;

import java.util.Map;
import java.util.Set;

import jadx.api.CommentsLevel;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.PrepareForCodeGen;

/**
 * Recheck only methods which received an early type-inference warning. Structural transforms can
 * remove the last generated use of an unresolved variable, making its original warning stale.
 * Avoid walking SSA variables in every method here: the early warning is also the cheap candidate
 * index for this final validation.
 */
@JadxVisitor(
		name = "Report Type Inference Warnings",
		desc = "Report unresolved types remaining in final code",
		runAfter = FinishTypeInference.class,
		runBefore = PrepareForCodeGen.class
)
public final class ReportTypeInferenceWarnings extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode()) {
			return;
		}
		JadxCommentsAttr commentsAttr = mth.get(AType.JADX_COMMENTS);
		if (commentsAttr == null || !removeTypeInferenceWarnings(commentsAttr)) {
			return;
		}
		if (commentsAttr.getComments().isEmpty()) {
			mth.remove(AType.JADX_COMMENTS);
		}
		if (mth.getSVars().isEmpty()) {
			return;
		}
		FinishTypeInference.repairLateCoroutineStateCleanupPathCasts(mth);
		FinishTypeInference.repairLateExceptionPrimitiveToReferenceMoves(mth);
		FinishTypeInference.repairLateStaleConstStringHandlerIntFlows(mth);
		FinishTypeInference.repairLateStaleConstStringHandlerPhis(mth);
		FinishTypeInference.repairLateTerminalCoroutineBooleanFlows(mth);
		FinishTypeInference.repairLateBooleanBitFlows(mth.getSVars());
		FinishTypeInference.repairLateCoroutineObjectResultFlows(mth.getSVars());
		FinishTypeInference.repairLateCoroutineBooleanIntSpillFlows(mth);
		FinishTypeInference.repairLateCoroutineFloatIntCarrierFlows(mth);
		FinishTypeInference.repairLateBooleanIntLoopFlows(mth);
		FinishTypeInference.repairLateCoroutineIntSpillFlows(mth.getSVars());
		FinishTypeInference.repairLateMixedPrimitiveReferencePhiRelays(mth);
		FinishTypeInference.repairLateNullableCoroutineObjectCarriers(mth);
		FinishTypeInference.repairLateZeroAndNullablePhiRelays(mth.getSVars());
		FinishTypeInference.repairLateExactArrayFlows(mth.getSVars());
		FinishTypeInference.repairLateMixedReferenceObjectFlows(mth.getSVars());
		FinishTypeInference.repairLateReferenceNullFlows(mth.getSVars());
		FinishTypeInference.repairLateReferenceOnlyPhiFlows(mth, mth.getSVars());
		for (SSAVar var : FinishTypeInference.collectWarnVars(mth.getSVars())) {
			mth.addWarnComment("Type inference failed for: " + var.getDetailedVarInfo(mth));
		}
	}

	static boolean removeTypeInferenceWarnings(JadxCommentsAttr commentsAttr) {
		Map<CommentsLevel, Set<String>> comments = commentsAttr.getComments();
		Set<String> warnings = comments.get(CommentsLevel.WARN);
		if (warnings == null) {
			return false;
		}
		boolean removed = warnings.removeIf(warning -> warning.startsWith("Type inference failed for:"));
		if (warnings.isEmpty()) {
			comments.remove(CommentsLevel.WARN);
		}
		return removed;
	}
}
