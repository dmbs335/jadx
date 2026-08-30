package jadx.core.dex.visitors.typeinference;

import java.util.List;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.RootNode;

/**
 * Dynamic bound for an instance field write of a generic type.
 * The value type is resolved from the instance generic type, or erased for a raw instance.
 */
public final class TypeBoundFieldPutUse implements ITypeBoundDynamic {
	private final RootNode root;
	private final IndexInsnNode putNode;
	private final RegisterArg valueArg;
	private final FieldInfo fieldInfo;
	private final ArgType fieldType;
	private final ArgType fieldDeclType;

	public TypeBoundFieldPutUse(RootNode root, IndexInsnNode putNode, RegisterArg valueArg) {
		this.root = root;
		this.putNode = putNode;
		this.valueArg = valueArg;
		this.fieldInfo = (FieldInfo) putNode.getIndex();
		this.fieldType = valueArg.getInitType();
		FieldNode fieldNode = root.resolveField(fieldInfo);
		this.fieldDeclType = fieldNode != null
				? fieldNode.getParentClass().getType()
				: fieldInfo.getDeclClass().getType();
	}

	@Override
	public BoundEnum getBound() {
		return BoundEnum.USE;
	}

	@Override
	public ArgType getType(TypeUpdateInfo updateInfo) {
		return getValueType(updateInfo.getType(getInstanceArg()));
	}

	@Override
	public ArgType getType() {
		return getValueType(getInstanceArg().getType());
	}

	private ArgType getValueType(ArgType instanceType) {
		ArgType resolvedType = root.getTypeUtils().replaceClassGenerics(instanceType, fieldDeclType, fieldType);
		if (resolvedType != null && !resolvedType.equals(fieldType) && !resolvedType.isWildcard()) {
			return resolvedType;
		}
		if (instanceType.isObject() && !instanceType.containsGeneric()) {
			return eraseTypeVariables(fieldType);
		}
		return fieldType;
	}

	private static ArgType eraseTypeVariables(ArgType type) {
		if (type.isGenericType()) {
			List<ArgType> extendTypes = type.getExtendTypes();
			return extendTypes.isEmpty() ? ArgType.OBJECT : extendTypes.get(0);
		}
		if (type.isArray()) {
			return ArgType.array(eraseTypeVariables(type.getArrayElement()));
		}
		if (type.isObject() && type.containsTypeVariable()) {
			return ArgType.object(type.getObject());
		}
		return type;
	}

	private InsnArg getInstanceArg() {
		return putNode.getArg(1);
	}

	@Override
	public RegisterArg getArg() {
		return valueArg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TypeBoundFieldPutUse that = (TypeBoundFieldPutUse) o;
		return putNode.equals(that.putNode);
	}

	@Override
	public int hashCode() {
		return putNode.hashCode();
	}

	@Override
	public String toString() {
		return "FieldPutUse{" + fieldInfo
				+ ", type=" + getType()
				+ ", instanceArg=" + getInstanceArg()
				+ '}';
	}
}
