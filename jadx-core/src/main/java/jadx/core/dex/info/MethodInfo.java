package jadx.core.dex.info;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.IMethodProto;
import jadx.api.plugins.input.data.IMethodRef;
import jadx.core.codegen.TypeGen;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.ImmutableList;
import jadx.core.utils.Utils;

public final class MethodInfo implements Comparable<MethodInfo> {

	private final String name;
	private final ArgType retType;
	private final List<ArgType> argTypes;
	private final ClassInfo declClass;
	private final String shortId;
	private final String rawFullId;
	private final int hash;
	private final int overloadKeyHash;

	private String alias;

	private MethodInfo(ClassInfo declClass, String name, List<ArgType> args, ArgType retType) {
		this.name = name;
		this.alias = name;
		this.declClass = declClass;
		this.argTypes = Utils.lockList(Objects.requireNonNull(args));
		this.retType = retType;
		this.shortId = makeShortId(name, argTypes, retType);
		this.rawFullId = declClass.makeRawFullName() + '.' + shortId;
		this.hash = calcHashCode();
		this.overloadKeyHash = 31 * name.hashCode() + argTypes.size();
	}

	public static MethodInfo fromRef(RootNode root, IMethodRef methodRef) {
		InfoStorage infoStorage = root.getInfoStorage();
		int uniqId = methodRef.getUniqId();
		if (uniqId != 0) {
			MethodInfo prevMth = infoStorage.getByUniqId(uniqId);
			if (prevMth != null) {
				return prevMth;
			}
		}
		methodRef.load();
		ArgType parentClsType = infoStorage.getType(methodRef.getParentClassType());
		ClassInfo parentClass = ClassInfo.fromType(root, parentClsType);
		ArgType returnType = infoStorage.getType(methodRef.getReturnType());
		List<ArgType> args = mapArgTypes(infoStorage, methodRef.getArgTypes());
		MethodInfo newMth = new MethodInfo(parentClass, methodRef.getName(), args, returnType);
		MethodInfo uniqMth = infoStorage.putMethod(newMth);
		if (uniqId != 0) {
			infoStorage.putByUniqId(uniqId, uniqMth);
		}
		return uniqMth;
	}

	public static MethodInfo fromDetails(RootNode root, ClassInfo declClass, String name, List<ArgType> args, ArgType retType) {
		MethodInfo newMth = new MethodInfo(declClass, name, args, retType);
		return root.getInfoStorage().putMethod(newMth);
	}

	public static MethodInfo fromMethodProto(RootNode root, ClassInfo declClass, String name, IMethodProto proto) {
		InfoStorage infoStorage = root.getInfoStorage();
		List<ArgType> args = mapArgTypes(infoStorage, proto.getArgTypes());
		ArgType returnType = infoStorage.getType(proto.getReturnType());
		return fromDetails(root, declClass, name, args, returnType);
	}

	private static List<ArgType> mapArgTypes(InfoStorage infoStorage, List<String> types) {
		int size = types.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		if (size == 1) {
			return Collections.singletonList(infoStorage.getType(types.get(0)));
		}
		ArgType[] args = new ArgType[size];
		for (int i = 0; i < size; i++) {
			args[i] = infoStorage.getType(types.get(i));
		}
		return new ImmutableList<>(args);
	}

	public String makeSignature(boolean includeRetType) {
		return makeSignature(false, includeRetType);
	}

	public String makeSignature(boolean useAlias, boolean includeRetType) {
		return makeShortId(useAlias ? alias : name,
				argTypes,
				includeRetType ? retType : null);
	}

	public static String makeShortId(String name, List<ArgType> argTypes, @Nullable ArgType retType) {
		int argsCount = argTypes.size();
		// Most signatures exceed StringBuilder's default 16-character buffer. Reserve a modest
		// per-type estimate to avoid the first one or two backing-array copies without retaining
		// excessively large temporary buffers for unusually long object names.
		int capacity = name.length() + 3 + (argsCount << 3) + (retType == null ? 0 : 8);
		StringBuilder sb = new StringBuilder(capacity);
		sb.append(name);
		sb.append('(');
		for (int i = 0; i < argsCount; i++) {
			TypeGen.appendSignature(sb, argTypes.get(i));
		}
		sb.append(')');
		if (retType != null) {
			TypeGen.appendSignature(sb, retType);
		}
		return sb.toString();
	}

	public boolean isOverloadedBy(MethodInfo otherMthInfo) {
		return overloadKeyHash == otherMthInfo.overloadKeyHash
				&& argTypes.size() == otherMthInfo.argTypes.size()
				&& name.equals(otherMthInfo.name)
				&& !shortId.equals(otherMthInfo.shortId);
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return declClass.getFullName() + '.' + name;
	}

	public String getAliasFullName() {
		return declClass.getAliasFullName() + '.' + alias;
	}

	public String getFullId() {
		return declClass.getFullName() + '.' + shortId;
	}

	public String getRawFullId() {
		return rawFullId;
	}

	/**
	 * Method name and signature
	 */
	public String getShortId() {
		return shortId;
	}

	public ClassInfo getDeclClass() {
		return declClass;
	}

	public ArgType getReturnType() {
		return retType;
	}

	public List<ArgType> getArgumentsTypes() {
		return argTypes;
	}

	public int getArgsCount() {
		return argTypes.size();
	}

	public boolean isConstructor() {
		return name.equals("<init>");
	}

	public boolean isClassInit() {
		return name.equals("<clinit>");
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void removeAlias() {
		this.alias = name;
	}

	public boolean hasAlias() {
		return !name.equals(alias);
	}

	public int calcHashCode() {
		return shortId.hashCode() + 31 * declClass.hashCode();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MethodInfo)) {
			return false;
		}
		MethodInfo other = (MethodInfo) obj;
		return shortId.equals(other.shortId)
				&& declClass.equals(other.declClass);
	}

	@Override
	public int compareTo(MethodInfo other) {
		if (declClass != other.declClass) {
			int clsCmp = declClass.compareTo(other.declClass);
			if (clsCmp != 0) {
				return clsCmp;
			}
		}
		return shortId.compareTo(other.shortId);
	}

	@Override
	public String toString() {
		return declClass.getFullName() + '.' + name
				+ '(' + Utils.listToString(argTypes) + "):" + retType;
	}
}
