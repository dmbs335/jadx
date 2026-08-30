package jadx.core.dex.visitors;

import jadx.core.dex.attributes.nodes.CoroutineStateMachineAttr;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;
import jadx.core.utils.exceptions.JadxException;

/**
 * Marks compiler-generated coroutine state-machine bodies for downstream generic passes.
 */
@JadxVisitor(
		name = "MarkCoroutineStateMachine",
		desc = "Mark compiler-generated coroutine state-machine methods",
		runBefore = jadx.core.dex.visitors.ssa.SSATransform.class
)
public class MarkCoroutineStateMachine extends AbstractVisitor {
	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (CoroutineMethodUtils.isStateMachineBody(mth)) {
			mth.addAttr(CoroutineStateMachineAttr.INSTANCE);
		}
	}
}
