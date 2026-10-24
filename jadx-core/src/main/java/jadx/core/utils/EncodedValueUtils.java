package jadx.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.IFieldRef;
import jadx.api.plugins.input.data.IMethodHandle;
import jadx.api.plugins.input.data.IMethodProto;
import jadx.api.plugins.input.data.IMethodRef;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.ConstClassNode;
import jadx.core.dex.instructions.ConstStringNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class EncodedValueUtils {

	/**
	 * Return constant literal from {@code jadx.api.plugins.input.data.annotations.EncodedValue}
	 *
	 * @return LiteralArg, String, ArgType or null
	 */
	@Nullable
	public static Object convertToConstValue(EncodedValue encodedValue) {
		if (encodedValue == null) {
			return null;
		}
		Object value = encodedValue.getValue();
		switch (encodedValue.getType()) {
			case ENCODED_NULL:
				return InsnArg.lit(0, ArgType.OBJECT);
			case ENCODED_BOOLEAN:
				return Boolean.TRUE.equals(value) ? LiteralArg.litTrue() : LiteralArg.litFalse();
			case ENCODED_BYTE:
				return InsnArg.lit((Byte) value, ArgType.BYTE);
			case ENCODED_SHORT:
				return InsnArg.lit((Short) value, ArgType.SHORT);
			case ENCODED_CHAR:
				return InsnArg.lit((Character) value, ArgType.CHAR);
			case ENCODED_INT:
				return InsnArg.lit((Integer) value, ArgType.INT);
			case ENCODED_LONG:
				return InsnArg.lit((Long) value, ArgType.LONG);
			case ENCODED_FLOAT:
				return InsnArg.lit(Float.floatToIntBits((Float) value), ArgType.FLOAT);
			case ENCODED_DOUBLE:
				return InsnArg.lit(Double.doubleToLongBits((Double) value), ArgType.DOUBLE);
			case ENCODED_STRING:
				// noinspection RedundantCast
				return (String) value;

			case ENCODED_TYPE:
				return ArgType.parse((String) value);

			default:
				return null;
		}
	}

	public static InsnArg convertToInsnArg(RootNode root, EncodedValue value) {
		return convertToInsnArg(root, value, null);
	}

	public static InsnArg convertToInsnArg(RootNode root, EncodedValue value, @Nullable ArgType callerType) {
		Object obj = value.getValue();
		switch (value.getType()) {
			case ENCODED_NULL:
			case ENCODED_BYTE:
			case ENCODED_SHORT:
			case ENCODED_CHAR:
			case ENCODED_INT:
			case ENCODED_LONG:
			case ENCODED_FLOAT:
			case ENCODED_DOUBLE:
				return (InsnArg) convertToConstValue(value);

			case ENCODED_BOOLEAN:
				return InsnArg.lit(((Boolean) obj) ? 1 : 0, ArgType.BOOLEAN);
			case ENCODED_STRING:
				return InsnArg.wrapArg(new ConstStringNode((String) obj));
			case ENCODED_TYPE:
				return InsnArg.wrapArg(new ConstClassNode(ArgType.parse((String) obj)));
			case ENCODED_METHOD_TYPE:
				return InsnArg.wrapArg(buildMethodType(root, (IMethodProto) obj));
			case ENCODED_METHOD_HANDLE:
				return InsnArg.wrapArg(buildMethodHandle(root, (IMethodHandle) obj, callerType));

		}
		throw new JadxRuntimeException("Unsupported type for raw invoke-custom: " + value.getType());
	}

	private static InvokeNode buildMethodType(RootNode root, IMethodProto methodProto) {
		ArgType retType = ArgType.parse(methodProto.getReturnType());
		List<ArgType> argTypes = Utils.collectionMap(methodProto.getArgTypes(), ArgType::parse);
		List<ArgType> callTypes = new ArrayList<>(1 + argTypes.size());
		callTypes.add(retType);
		callTypes.addAll(argTypes);
		List<ArgType> classArgTypes = new ArrayList<>(callTypes.size());
		for (int i = 0; i < callTypes.size(); i++) {
			classArgTypes.add(ArgType.CLASS);
		}
		ArgType mthType = ArgType.object("java.lang.invoke.MethodType");
		ClassInfo cls = ClassInfo.fromType(root, mthType);
		MethodInfo mth = MethodInfo.fromDetails(root, cls, "methodType", classArgTypes, mthType);
		InvokeNode invoke = new InvokeNode(mth, InvokeType.STATIC, callTypes.size());
		for (ArgType type : callTypes) {
			InsnNode argInsn;
			if (type.isPrimitive()) {
				argInsn = new IndexInsnNode(InsnType.SGET, getTypeField(root, type.getPrimitiveType()), 0);
			} else {
				argInsn = new ConstClassNode(type);
			}
			invoke.addArg(InsnArg.wrapArg(argInsn));
		}
		return invoke;
	}

	public static FieldInfo getTypeField(RootNode root, PrimitiveType type) {
		ArgType boxType = type.getBoxType();
		ClassInfo boxCls = ClassInfo.fromType(root, boxType);
		return FieldInfo.from(root, boxCls, "TYPE", ArgType.CLASS);
	}

	/**
	 * Build `MethodHandles.lookup().find{type}(methodCls, methodName, methodType)`
	 */
	private static InsnNode buildMethodHandle(RootNode root, IMethodHandle methodHandle, @Nullable ArgType callerType) {
		if (methodHandle.getType().isField()) {
			return buildFieldMethodHandle(root, methodHandle);
		}
		IMethodRef methodRef = methodHandle.getMethodRef();
		if (methodRef == null) {
			throw new JadxRuntimeException("Missing method reference for method handle: " + methodHandle.getType());
		}
		methodRef.load();
		if (methodHandle.getType() == MethodHandleType.INVOKE_CONSTRUCTOR) {
			if (!"<init>".equals(methodRef.getName()) || !"V".equals(methodRef.getReturnType())) {
				throw new JadxRuntimeException("Invalid constructor method handle reference: " + methodRef);
			}
			ClassInfo lookupCls = ClassInfo.fromName(root, "java.lang.invoke.MethodHandles.Lookup");
			MethodInfo findConstructor = MethodInfo.fromDetails(root, lookupCls, "findConstructor",
					Arrays.asList(ArgType.CLASS, ArgType.object("java.lang.invoke.MethodType")),
					ArgType.object("java.lang.invoke.MethodHandle"));
			InvokeNode invoke = new InvokeNode(findConstructor, InvokeType.DIRECT, 3);
			invoke.addArg(buildLookupArg(root));
			invoke.addArg(InsnArg.wrapArg(new ConstClassNode(ArgType.object(methodRef.getParentClassType()))));
			invoke.addArg(InsnArg.wrapArg(buildMethodType(root, methodRef)));
			return invoke;
		}
		if (methodHandle.getType() == MethodHandleType.INVOKE_DIRECT) {
			if (callerType == null) {
				throw new JadxRuntimeException("Missing caller class for direct method handle: " + methodRef);
			}
			ClassInfo lookupCls = ClassInfo.fromName(root, "java.lang.invoke.MethodHandles.Lookup");
			MethodInfo findSpecial = MethodInfo.fromDetails(root, lookupCls, "findSpecial",
					Arrays.asList(ArgType.CLASS, ArgType.STRING, ArgType.object("java.lang.invoke.MethodType"), ArgType.CLASS),
					ArgType.object("java.lang.invoke.MethodHandle"));
			InvokeNode invoke = new InvokeNode(findSpecial, InvokeType.DIRECT, 5);
			invoke.addArg(buildLookupArg(root));
			invoke.addArg(InsnArg.wrapArg(new ConstClassNode(ArgType.object(methodRef.getParentClassType()))));
			invoke.addArg(InsnArg.wrapArg(new ConstStringNode(methodRef.getName())));
			invoke.addArg(InsnArg.wrapArg(buildMethodType(root, methodRef)));
			invoke.addArg(InsnArg.wrapArg(new ConstClassNode(callerType)));
			return invoke;
		}

		ClassInfo lookupCls = ClassInfo.fromName(root, "java.lang.invoke.MethodHandles.Lookup");
		MethodInfo findMethod = MethodInfo.fromDetails(root, lookupCls,
				getFindMethodName(methodHandle.getType()),
				Arrays.asList(ArgType.CLASS, ArgType.STRING, ArgType.object("java.lang.invoke.MethodType")),
				ArgType.object("java.lang.invoke.MethodHandle"));

		InvokeNode invoke = new InvokeNode(findMethod, InvokeType.DIRECT, 4);
		invoke.addArg(buildLookupArg(root));
		invoke.addArg(InsnArg.wrapArg(new ConstClassNode(ArgType.object(methodRef.getParentClassType()))));
		invoke.addArg(InsnArg.wrapArg(new ConstStringNode(methodRef.getName())));
		invoke.addArg(InsnArg.wrapArg(buildMethodType(root, methodRef)));
		return invoke;
	}

	private static InvokeNode buildFieldMethodHandle(RootNode root, IMethodHandle methodHandle) {
		IFieldRef fieldRef = methodHandle.getFieldRef();
		if (fieldRef == null) {
			throw new JadxRuntimeException("Missing field reference for method handle: " + methodHandle.getType());
		}
		String findMethodName;
		switch (methodHandle.getType()) {
			case INSTANCE_GET:
				findMethodName = "findGetter";
				break;
			case STATIC_GET:
				findMethodName = "findStaticGetter";
				break;
			case INSTANCE_PUT:
				findMethodName = "findSetter";
				break;
			case STATIC_PUT:
				findMethodName = "findStaticSetter";
				break;
			default:
				throw new JadxRuntimeException("Unexpected field method handle type: " + methodHandle.getType());
		}

		ClassInfo lookupCls = ClassInfo.fromName(root, "java.lang.invoke.MethodHandles.Lookup");
		MethodInfo findMethod = MethodInfo.fromDetails(root, lookupCls, findMethodName,
				Arrays.asList(ArgType.CLASS, ArgType.STRING, ArgType.CLASS),
				ArgType.object("java.lang.invoke.MethodHandle"));
		InvokeNode invoke = new InvokeNode(findMethod, InvokeType.DIRECT, 4);
		invoke.addArg(buildLookupArg(root));
		invoke.addArg(InsnArg.wrapArg(new ConstClassNode(ArgType.object(fieldRef.getParentClassType()))));
		invoke.addArg(InsnArg.wrapArg(new ConstStringNode(fieldRef.getName())));
		ArgType fieldType = ArgType.parse(fieldRef.getType());
		InsnNode fieldTypeClass = fieldType.isPrimitive()
				? new IndexInsnNode(InsnType.SGET, getTypeField(root, fieldType.getPrimitiveType()), 0)
				: new ConstClassNode(fieldType);
		invoke.addArg(InsnArg.wrapArg(fieldTypeClass));
		return invoke;
	}

	public static InsnArg buildLookupArg(RootNode root) {
		ArgType lookupType = ArgType.object("java.lang.invoke.MethodHandles.Lookup");
		ClassInfo cls = ClassInfo.fromName(root, "java.lang.invoke.MethodHandles");
		MethodInfo mth = MethodInfo.fromDetails(root, cls, "lookup", Collections.emptyList(), lookupType);
		return InsnArg.wrapArg(new InvokeNode(mth, InvokeType.STATIC, 0));
	}

	private static String getFindMethodName(MethodHandleType type) {
		switch (type) {
			case INVOKE_STATIC:
				return "findStatic";
			case INVOKE_INSTANCE:
			case INVOKE_INTERFACE:
				return "findVirtual";

			default:
				throw new JadxRuntimeException("Method handle lookup needs caller context: " + type);
		}
	}
}
