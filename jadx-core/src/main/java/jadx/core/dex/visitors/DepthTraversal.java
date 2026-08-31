package jadx.core.dex.visitors;

import java.util.List;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxTaskCancelledException;

public class DepthTraversal {
	public static void visit(IDexTreeVisitor visitor, ClassNode cls) {
		try {
			Utils.checkThreadInterrupt();
			if (visitor.visit(cls)) {
				List<ClassNode> innerClasses = cls.getInnerClasses();
				int innerClassesCount = innerClasses.size();
				for (int innerClassIndex = 0; innerClassIndex < innerClassesCount; innerClassIndex++) {
					visit(visitor, innerClasses.get(innerClassIndex));
				}
				List<MethodNode> methods = cls.getMethods();
				int methodsCount = methods.size();
				for (int methodIndex = 0; methodIndex < methodsCount; methodIndex++) {
					visit(visitor, methods.get(methodIndex));
				}
			}
			Utils.checkThreadInterrupt();
		} catch (JadxTaskCancelledException e) {
			throw e;
		} catch (StackOverflowError | BootstrapMethodError | Exception e) {
			cls.addError(e.getClass().getSimpleName() + " in pass: " + visitor.getClass().getSimpleName(), e);
		}
	}

	public static void visit(IDexTreeVisitor visitor, MethodNode mth) {
		try {
			Utils.checkThreadInterrupt();
			if (mth.contains(AFlag.JADX_ERROR)) {
				return;
			}
			visitor.visit(mth);
			Utils.checkThreadInterrupt();
		} catch (JadxTaskCancelledException e) {
			throw e;
		} catch (StackOverflowError | BootstrapMethodError | Exception e) {
			mth.addError(e.getClass().getSimpleName() + " in pass: " + visitor.getClass().getSimpleName(), e);
		}
	}

	private DepthTraversal() {
	}
}
