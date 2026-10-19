package jadx.core.dex.instructions.invokedynamic;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import jadx.api.plugins.input.data.IMethodHandle;
import jadx.api.plugins.input.data.IMethodProto;
import jadx.api.plugins.input.data.IMethodRef;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.insns.InsnData;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.InvokeCustomNode;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.NamedArg;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class CustomLambdaCall {
	private static final int FLAG_SERIALIZABLE = 1;
	private static final int FLAG_MARKERS = 2;
	private static final int FLAG_BRIDGES = 4;

	/**
	 * Expect LambdaMetafactory.metafactory method
	 */
	public static boolean isLambdaInvoke(List<EncodedValue> values) {
		if (values.size() < 6) {
			return false;
		}
		EncodedValue mthRef = values.get(0);
		if (mthRef.getType() != EncodedType.ENCODED_METHOD_HANDLE) {
			return false;
		}
		IMethodHandle methodHandle = (IMethodHandle) mthRef.getValue();
		if (methodHandle.getType() != MethodHandleType.INVOKE_STATIC) {
			return false;
		}
		IMethodRef methodRef = methodHandle.getMethodRef();
		if (!methodRef.getParentClassType().equals("Ljava/lang/invoke/LambdaMetafactory;")) {
			return false;
		}
		String mthName = methodRef.getName();
		return mthName.equals("metafactory") || mthName.equals("altMetafactory");
	}

	public static InvokeCustomNode buildLambdaMethodCall(MethodNode mth, InsnData insn, boolean isRange, List<EncodedValue> values) {
		IMethodHandle callMthHandle = (IMethodHandle) values.get(4).getValue();
		if (callMthHandle.getType().isField()) {
			throw new JadxRuntimeException("Not yet supported");
		}
		InvokeCustomNode resNode = buildMethodCall(mth, insn, isRange, values, callMthHandle);
		resNode.setLambdaInterfaces(readLambdaInterfaces(values, resNode.getCallMth().getReturnType()));
		if (resNode.getLambdaInterfaces().contains(ArgType.object("java.io.Serializable"))) {
			hideGeneratedLambdaDeserializer(mth);
		}
		int resReg = insn.getResultReg();
		if (resReg != -1) {
			resNode.setResult(InsnArg.reg(resReg, mth.getReturnType()));
		}
		return resNode;
	}

	private static void hideGeneratedLambdaDeserializer(MethodNode mth) {
		ArgType serializedLambda = ArgType.object("java.lang.invoke.SerializedLambda");
		for (MethodNode method : mth.getParentClass().getMethods()) {
			if (method.getName().equals("$deserializeLambda$")
					&& method.getAccessFlags().isSynthetic()
					&& method.getAccessFlags().isStatic()
					&& method.getReturnType().equals(ArgType.OBJECT)
					&& method.getArgTypes().equals(List.of(serializedLambda))) {
				method.add(AFlag.DONT_GENERATE);
			}
		}
	}

	private static List<ArgType> readLambdaInterfaces(List<EncodedValue> values, ArgType primary) {
		IMethodHandle bootstrap = (IMethodHandle) values.get(0).getValue();
		if (!bootstrap.getMethodRef().getName().equals("altMetafactory")) {
			return List.of();
		}
		if (values.size() <= 6 || values.get(6).getType() != EncodedType.ENCODED_INT) {
			throw new JadxRuntimeException("Invalid altMetafactory flags");
		}
		int flags = (int) values.get(6).getValue();
		if ((flags & ~(FLAG_SERIALIZABLE | FLAG_MARKERS | FLAG_BRIDGES)) != 0) {
			throw new JadxRuntimeException("Unsupported altMetafactory flags: " + flags);
		}
		if ((flags & (FLAG_SERIALIZABLE | FLAG_MARKERS)) == 0) {
			return List.of();
		}
		List<ArgType> interfaces = new ArrayList<>();
		interfaces.add(primary);
		if ((flags & FLAG_MARKERS) != 0) {
			if (values.size() <= 7 || values.get(7).getType() != EncodedType.ENCODED_INT) {
				throw new JadxRuntimeException("Invalid altMetafactory marker count");
			}
			int count = (int) values.get(7).getValue();
			if (count < 0 || count > values.size() - 8) {
				throw new JadxRuntimeException("Invalid altMetafactory marker count: " + count);
			}
			for (int i = 0; i < count; i++) {
				EncodedValue marker = values.get(8 + i);
				if (marker.getType() != EncodedType.ENCODED_TYPE || !(marker.getValue() instanceof String)) {
					throw new JadxRuntimeException("Invalid altMetafactory marker type");
				}
				ArgType type = ArgType.parse((String) marker.getValue());
				if (!type.isObject()) {
					throw new JadxRuntimeException("Invalid altMetafactory marker: " + type);
				}
				if (!interfaces.contains(type)) {
					interfaces.add(type);
				}
			}
		}
		if ((flags & FLAG_SERIALIZABLE) != 0) {
			ArgType serializable = ArgType.object("java.io.Serializable");
			if (!interfaces.contains(serializable)) {
				interfaces.add(serializable);
			}
		}
		return interfaces.size() > 1 ? interfaces : List.of();
	}

	@NotNull
	private static InvokeCustomNode buildMethodCall(MethodNode mth, InsnData insn, boolean isRange,
			List<EncodedValue> values, IMethodHandle callMthHandle) {
		RootNode root = mth.root();
		IMethodProto lambdaProto = (IMethodProto) values.get(2).getValue();
		MethodInfo lambdaInfo = MethodInfo.fromMethodProto(root, mth.getParentClass().getClassInfo(), "", lambdaProto);

		MethodHandleType methodHandleType = callMthHandle.getType();
		InvokeCustomNode invokeCustomNode = new InvokeCustomNode(lambdaInfo, insn, false, isRange);
		invokeCustomNode.setHandleType(methodHandleType);

		ClassInfo implCls = ClassInfo.fromType(root, lambdaInfo.getReturnType());
		String implName = (String) values.get(1).getValue();
		IMethodProto implProto = (IMethodProto) values.get(3).getValue();
		MethodInfo implMthInfo = MethodInfo.fromMethodProto(root, implCls, implName, implProto);
		invokeCustomNode.setImplMthInfo(implMthInfo);
		IMethodProto instantiatedProto = (IMethodProto) values.get(5).getValue();
		invokeCustomNode.setInstantiatedMthInfo(MethodInfo.fromMethodProto(root, implCls, implName, instantiatedProto));

		MethodInfo callMthInfo = MethodInfo.fromRef(root, callMthHandle.getMethodRef());
		InvokeNode invokeNode = buildInvokeNode(methodHandleType, invokeCustomNode, callMthInfo);

		if (methodHandleType == MethodHandleType.INVOKE_CONSTRUCTOR) {
			ConstructorInsn ctrInsn = new ConstructorInsn(mth, invokeNode);
			invokeCustomNode.setCallInsn(ctrInsn);
		} else {
			invokeCustomNode.setCallInsn(invokeNode);
		}

		MethodNode callMth = root.resolveMethod(callMthInfo);
		if (callMth != null) {
			invokeCustomNode.getCallInsn().addAttr(callMth);
			if (callMth.getAccessFlags().isSynthetic()
					&& callMth.getParentClass().equals(mth.getParentClass())) {
				// inline only synthetic methods from same class
				callMth.add(AFlag.DONT_GENERATE);
				invokeCustomNode.setInlineInsn(true);
			}
		}
		if (!invokeCustomNode.isInlineInsn()) {
			List<ArgType> args = invokeCustomNode.getInstantiatedMthInfo().getArgumentsTypes();
			boolean sameArgs = args.equals(callMthInfo.getArgumentsTypes());
			invokeCustomNode.setUseRef(sameArgs);
		}

		// prevent args inlining into not generated invoke custom node
		for (InsnArg arg : invokeCustomNode.getArguments()) {
			arg.add(AFlag.DONT_INLINE);
		}
		return invokeCustomNode;
	}

	@NotNull
	private static InvokeNode buildInvokeNode(MethodHandleType methodHandleType, InvokeCustomNode invokeCustomNode,
			MethodInfo callMthInfo) {
		InvokeType invokeType = InvokeCustomUtils.convertInvokeType(methodHandleType);
		int callArgsCount = callMthInfo.getArgsCount();
		boolean instanceCall = invokeType != InvokeType.STATIC;
		if (instanceCall) {
			callArgsCount++;
		}
		InvokeNode invokeNode = new InvokeNode(callMthInfo, invokeType, callArgsCount);

		// copy insn args
		int argsCount = invokeCustomNode.getArgsCount();
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = invokeCustomNode.getArg(i);
			invokeNode.addArg(arg.duplicate());
		}
		if (callArgsCount > argsCount) {
			// fill remaining args with NamedArg
			int callArgNum = argsCount;
			if (instanceCall) {
				callArgNum--; // start from instance type
			}
			List<ArgType> callArgTypes = callMthInfo.getArgumentsTypes();
			for (int i = argsCount; i < callArgsCount; i++) {
				ArgType argType;
				if (callArgNum < 0) {
					// instance arg type
					argType = callMthInfo.getDeclClass().getType();
				} else {
					argType = callArgTypes.get(callArgNum++);
				}
				invokeNode.addArg(new NamedArg("v" + i, argType));
			}
		}
		return invokeNode;
	}
}
