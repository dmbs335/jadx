package jadx.core.dex.visitors.kotlin;

import java.util.List;
import java.util.function.Predicate;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.MethodNode;

/**
 * Centralizes Kotlin coroutine compiler/runtime recognition.
 *
 * Generic SSA, type and region passes should consume the semantic state-machine marker or these
 * predicates instead of matching compiler-generated names and runtime types themselves.
 */
public final class CoroutineMethodUtils {
	private static final String KOTLIN_COROUTINE_RUNTIME = "kotlin.coroutines.jvm.internal.";
	private static final String KOTLIN_CONTINUATION = "kotlin.coroutines.Continuation";

	private CoroutineMethodUtils() {
	}

	public static boolean isStateMachine(MethodNode mth) {
		return mth.contains(AType.COROUTINE_STATE_MACHINE);
	}

	/**
	 * Structural recognition used before the semantic marker pass has executed.
	 */
	public static boolean isStateMachineBody(MethodNode mth) {
		if (!isKnownCoroutineBase(mth.getParentClass().getSuperClass())) {
			return false;
		}
		return isStateMachineSignature(
				mth.getName(),
				mth.getAccessFlags().isStatic(),
				mth.getArgTypes(),
				mth.getReturnType())
				|| isObfuscatedStateMachineSignature(
						mth.getAccessFlags().isStatic(),
						mth.getArgTypes(),
						mth.getReturnType());
	}

	/**
	 * Broader predicate for transformations that apply both to generated state-machine bodies and
	 * ordinary Kotlin suspend methods whose lowered JVM signature carries a Continuation parameter.
	 *
	 * This method is deliberately stage-independent: block passes execute before the marker visitor,
	 * so structural state-machine recognition remains as a fallback until the attribute is available.
	 */
	public static boolean isCoroutineMethod(MethodNode mth) {
		return isStateMachine(mth) || isStateMachineBody(mth) || hasContinuationParameter(mth);
	}

	/**
	 * Preserve legacy checks which intentionally target SuspendLambda bodies rather than every
	 * continuation implementation.
	 */
	public static boolean isSuspendLambdaBody(MethodNode mth) {
		if (!isSuspendLambdaBase(mth.getParentClass().getSuperClass())) {
			return false;
		}
		return isStateMachineSignature(
				mth.getName(),
				mth.getAccessFlags().isStatic(),
				mth.getArgTypes(),
				mth.getReturnType())
				|| isObfuscatedStateMachineSignature(
						mth.getAccessFlags().isStatic(),
						mth.getArgTypes(),
						mth.getReturnType());
	}

	/**
	 * Atomic predicate for lowered suspend functions. Consumers with narrower legacy semantics can
	 * combine this with {@link #isSuspendLambdaBody(MethodNode)} or
	 * {@link #isStateMachineBody(MethodNode)} without reintroducing runtime string matching.
	 */
	public static boolean hasContinuationParameter(MethodNode mth) {
		return hasParameterMatching(mth, CoroutineMethodUtils::isCanonicalContinuationType);
	}

	/**
	 * Recognize lowered signatures which were widened or rewritten to a concrete Kotlin runtime
	 * continuation base. Keep this separate from {@link #hasContinuationParameter(MethodNode)} so
	 * existing shape-specific coroutine passes are not enabled for every runtime implementation.
	 */
	public static boolean hasKnownContinuationParameter(MethodNode mth) {
		return hasParameterMatching(mth, CoroutineMethodUtils::isKnownContinuationType);
	}

	private static boolean hasParameterMatching(
			MethodNode mth, Predicate<ArgType> predicate) {
		List<ArgType> argTypes = mth.getMethodInfo().getArgumentsTypes();
		int count = argTypes.size();
		for (int i = 0; i < count; i++) {
			if (predicate.test(argTypes.get(i))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCanonicalContinuationType(ArgType type) {
		return type != null
				&& type.isObject()
				&& KOTLIN_CONTINUATION.equals(type.getObject());
	}

	static boolean isStateMachineSignature(
			String name, boolean isStatic, List<ArgType> argTypes, ArgType returnType) {
		return !isStatic
				&& "invokeSuspend".equals(name)
				&& argTypes.size() == 1
				&& ArgType.OBJECT.equals(argTypes.get(0))
				&& ArgType.OBJECT.equals(returnType);
	}

	/**
	 * R8 can rename both the generated state-machine method and its bundled Kotlin runtime base.
	 * Once the declaring class is already proven to extend a known continuation implementation,
	 * the non-static {@code Object -> Object} shape uniquely identifies the state-machine body among
	 * the generated lambda bridge/create methods, without relying on the {@code invokeSuspend} name.
	 */
	static boolean isObfuscatedStateMachineSignature(
			boolean isStatic, List<ArgType> argTypes, ArgType returnType) {
		return !isStatic
				&& argTypes.size() == 1
				&& ArgType.OBJECT.equals(argTypes.get(0))
				&& ArgType.OBJECT.equals(returnType);
	}

	public static boolean isKnownCoroutineBase(ArgType superType) {
		if (superType == null || !superType.isObject()) {
			return false;
		}
		String name = superType.getObject();
		if (!name.startsWith(KOTLIN_COROUTINE_RUNTIME)) {
			return false;
		}
		String simpleName = name.substring(KOTLIN_COROUTINE_RUNTIME.length());
		return simpleName.equals("BaseContinuationImpl")
				|| simpleName.equals("ContinuationImpl")
				|| simpleName.equals("RestrictedContinuationImpl")
				|| simpleName.equals("SuspendLambda")
				|| simpleName.equals("RestrictedSuspendLambda");
	}

	static boolean isSuspendLambdaBase(ArgType superType) {
		if (superType == null || !superType.isObject()) {
			return false;
		}
		String name = superType.getObject();
		return name.equals(KOTLIN_COROUTINE_RUNTIME + "SuspendLambda")
				|| name.equals(KOTLIN_COROUTINE_RUNTIME + "RestrictedSuspendLambda");
	}

	public static boolean isContinuationType(ArgType type) {
		return isCanonicalContinuationType(type);
	}

	/**
	 * Recognize both the public Continuation contract and concrete compiler runtime bases. Generic
	 * type and region passes must keep using {@link #isContinuationType(ArgType)} unless they have a
	 * shape proof which is valid for concrete continuation implementations.
	 */
	public static boolean isKnownContinuationType(ArgType type) {
		return type != null
				&& type.isObject()
				&& (KOTLIN_CONTINUATION.equals(type.getObject())
						|| isKnownCoroutineBase(type));
	}
}
