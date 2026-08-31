package jadx.core.dex.visitors.fixaccessmodifiers;

import jadx.api.plugins.input.data.AccessFlags;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.ICodeNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

class VisibilityUtils {

	private final RootNode root;

	VisibilityUtils(RootNode rootNode) {
		this.root = rootNode;
	}

	void checkVisibility(ICodeNode targetNode, ICodeNode callerNode, OnBadVisibilityCallback callback) {
		ClassNode targetCls = targetNode instanceof ClassNode
				? (ClassNode) targetNode
				: targetNode.getDeclaringClass();

		ClassNode callerCls = callerNode instanceof ClassNode
				? (ClassNode) callerNode
				: callerNode.getDeclaringClass();

		if (targetCls == callerCls || inSameTopClass(targetCls, callerCls)) {
			return;
		}

		if (inSamePkg(targetCls, callerCls)) {
			ICodeNode node = targetNode;
			do {
				if (node.getAccessFlags().isPrivate()) {
					callback.onBadVisibility(node, 0); // PACKAGE_PRIVATE
				}
				node = node.getDeclaringClass();
			} while (node != null);
		} else {
			ICodeNode node = targetNode;
			do {
				AccessInfo nodeVisFlags = node.getAccessFlags();
				if (nodeVisFlags.isPrivate() || nodeVisFlags.isPackagePrivate()) {
					ClassNode nodeDeclaringCls = node.getDeclaringClass();
					int expectedVisFlag = nodeDeclaringCls != null && isSuperType(callerCls, nodeDeclaringCls)
							? AccessFlags.PROTECTED
							: AccessFlags.PUBLIC;

					callback.onBadVisibility(node, expectedVisFlag);
				} else if (nodeVisFlags.isProtected()) {
					ClassNode nodeDeclaringCls = node.getDeclaringClass();
					if (nodeDeclaringCls == null || !isSuperType(callerCls, nodeDeclaringCls)) {
						callback.onBadVisibility(node, AccessFlags.PUBLIC);
					}
				} else if (!nodeVisFlags.isPublic()) {
					throw new JadxRuntimeException(nodeVisFlags + " is not supported");
				}
				node = node.getDeclaringClass();
			} while (node != null);
		}
	}

	private static boolean inSamePkg(ClassNode cls1, ClassNode cls2) {
		return cls1.getPackageNode() == cls2.getPackageNode();
	}

	private static boolean inSameTopClass(ClassNode cls1, ClassNode cls2) {
		return cls1.getTopParentClass() == cls2.getTopParentClass();
	}

	private boolean isSuperType(ClassNode cls, ClassNode superCls) {
		return root.getClsp().isImplements(cls.getRawName(), superCls.getRawName());
	}

	interface OnBadVisibilityCallback {
		void onBadVisibility(ICodeNode node, int expectedVisFlag);
	}
}
