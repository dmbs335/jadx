package jadx.core.dex.visitors.kotlin;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;

class CoroutineMethodUtilsTest {
	@Test
	void acceptsCanonicalStateMachineSignature() {
		assertThat(CoroutineMethodUtils.isStateMachineSignature(
				"invokeSuspend", false, List.of(ArgType.OBJECT), ArgType.OBJECT))
						.isTrue();
	}

	@Test
	void acceptsKnownKotlinCoroutineBases() {
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.BaseContinuationImpl")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImpl")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.RestrictedContinuationImpl")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.SuspendLambda")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.RestrictedSuspendLambda")))
						.isTrue();
	}

	@Test
	void rejectsCoroutineBaseLookalikes() {
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImplLike")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.SuspendLambdaImpl")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("kotlin.coroutines.jvm.internal.sub.SuspendLambda")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("example.kotlin.coroutines.jvm.internal.ContinuationImpl")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(ArgType.OBJECT))
				.isFalse();
	}

	@Test
	void distinguishesSuspendLambdaBasesFromOtherStateMachines() {
		assertThat(CoroutineMethodUtils.isSuspendLambdaBase(
				ArgType.object("kotlin.coroutines.jvm.internal.SuspendLambda")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isSuspendLambdaBase(
				ArgType.object("kotlin.coroutines.jvm.internal.RestrictedSuspendLambda")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isSuspendLambdaBase(
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImpl")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isSuspendLambdaBase(
				ArgType.object("example.SuspendLambda")))
						.isFalse();
	}

	@Test
	void preservesCanonicalContinuationBoundaryForGenericPasses() {
		assertThat(CoroutineMethodUtils.isContinuationType(
				ArgType.object("kotlin.coroutines.Continuation")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isContinuationType(
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImpl")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isContinuationType(
				ArgType.object("kotlin.coroutines.jvm.internal.RestrictedContinuationImpl")))
						.isFalse();
	}

	@Test
	void recognizesConcreteRuntimeBasesOnlyAtExplicitKnownTypeBoundary() {
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("kotlin.coroutines.Continuation")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("kotlin.coroutines.jvm.internal.ContinuationImpl")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("kotlin.coroutines.jvm.internal.RestrictedContinuationImpl")))
						.isTrue();
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("kotlin.coroutines.ContinuationImpl")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("kotlin.coroutines.ContinuationLike")))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownContinuationType(
				ArgType.object("java.lang.Object")))
						.isFalse();
	}

	@Test
	void rejectsStateMachineNameCollisions() {
		assertThat(CoroutineMethodUtils.isStateMachineSignature(
				"invokeSuspend", false, List.of(), ArgType.OBJECT))
						.isFalse();
		assertThat(CoroutineMethodUtils.isStateMachineSignature(
				"invokeSuspend", true, List.of(ArgType.OBJECT), ArgType.OBJECT))
						.isFalse();
		assertThat(CoroutineMethodUtils.isStateMachineSignature(
				"invokeSuspend", false, List.of(ArgType.INT), ArgType.OBJECT))
						.isFalse();
		assertThat(CoroutineMethodUtils.isStateMachineSignature(
				"invokeSuspend", false, List.of(ArgType.OBJECT), ArgType.VOID))
						.isFalse();
		assertThat(CoroutineMethodUtils.isKnownCoroutineBase(
				ArgType.object("example.InvokeSuspendBase")))
						.isFalse();
	}
}
