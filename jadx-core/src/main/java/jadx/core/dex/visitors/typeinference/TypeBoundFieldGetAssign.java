package jadx.core.dex.visitors.typeinference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IMethodDetails;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;

/**
 * Dynamic bound for instance field get of generic type.
 * Bound type calculated using instance generic type.
 */
public final class TypeBoundFieldGetAssign implements ITypeBoundDynamic {
	private final RootNode root;
	private final IndexInsnNode getNode;
	private final FieldInfo fieldInfo;
	private final ArgType initType;
	private final ArgType fieldDeclType;
	private final ArgType typeForReplace;

	public TypeBoundFieldGetAssign(RootNode root, IndexInsnNode getNode, ArgType initType) {
		this.root = root;
		this.getNode = getNode;
		this.fieldInfo = (FieldInfo) getNode.getIndex();
		this.initType = initType;
		FieldNode fieldNode = root.resolveField(fieldInfo);
		this.fieldDeclType = fieldNode != null
				? fieldNode.getParentClass().getType()
				: fieldInfo.getDeclClass().getType();
		this.typeForReplace = normalizeFieldTypeVars(root, fieldDeclType, initType);
	}

	@Override
	public BoundEnum getBound() {
		return BoundEnum.ASSIGN;
	}

	@Override
	public ArgType getType(TypeUpdateInfo updateInfo) {
		return getResultType(updateInfo.getType(getInstanceArg()));
	}

	@Override
	public ArgType getType() {
		return getResultType(getInstanceArg().getType());
	}

	private ArgType getResultType(ArgType instanceType) {
		ArgType resultGeneric = root.getTypeUtils().replaceClassGenerics(instanceType, fieldDeclType, initType);
		if (resultGeneric != null
				&& resultGeneric.equals(initType)
				&& !typeForReplace.equals(initType)
				&& canUseNormalizedResult()) {
			ArgType normalizedResult = root.getTypeUtils().replaceClassGenerics(instanceType, fieldDeclType, typeForReplace);
			if (normalizedResult != null) {
				resultGeneric = normalizedResult;
			}
		}
		if (resultGeneric != null && !resultGeneric.isWildcard()) {
			return resultGeneric;
		}
		return initType; // TODO: check if this type is allowed in current scope
	}

	private boolean canUseNormalizedResult() {
		RegisterArg result = getNode.getResult();
		if (result == null || result.getSVar() == null || result.getSVar().isUsedInPhi()) {
			return false;
		}
		for (RegisterArg use : result.getSVar().getUseList()) {
			InsnNode parentInsn = use.getParentInsn();
			if (parentInsn instanceof BaseInvokeNode) {
				IMethodDetails details = root.getMethodUtils().getMethodDetails((BaseInvokeNode) parentInsn);
				if (details != null && details.getReturnType().containsTypeVariable()) {
					return false;
				}
			}
		}
		return true;
	}

	ArgType getTypeForFallback() {
		ArgType resultGeneric = root.getTypeUtils().replaceClassGenerics(
				getInstanceArg().getType(), fieldDeclType, initType);
		if (resultGeneric != null && !resultGeneric.isWildcard()) {
			return resultGeneric;
		}
		return initType;
	}

	private static ArgType normalizeFieldTypeVars(RootNode root, ArgType fieldDeclType, ArgType initType) {
		if (!initType.containsTypeVariable()) {
			return initType;
		}
		List<ArgType> typeVars = root.getTypeUtils().getClassGenerics(fieldDeclType);
		Map<ArgType, ArgType> normalizeMap = new HashMap<>(typeVars.size());
		for (ArgType typeVar : typeVars) {
			if (typeVar.isGenericType() && typeVar.getExtendTypes() != null) {
				normalizeMap.put(typeVar, ArgType.genericType(typeVar.getObject()));
			}
		}
		ArgType normalizedType = root.getTypeUtils().replaceTypeVariablesUsingMap(initType, normalizeMap);
		return normalizedType != null ? normalizedType : initType;
	}

	private InsnArg getInstanceArg() {
		return getNode.getArg(0);
	}

	@Override
	public RegisterArg getArg() {
		return getNode.getResult();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TypeBoundFieldGetAssign that = (TypeBoundFieldGetAssign) o;
		return getNode.equals(that.getNode);
	}

	@Override
	public int hashCode() {
		return getNode.hashCode();
	}

	@Override
	public String toString() {
		return "FieldGetAssign{" + fieldInfo
				+ ", type=" + getType()
				+ ", instanceArg=" + getInstanceArg()
				+ '}';
	}
}
