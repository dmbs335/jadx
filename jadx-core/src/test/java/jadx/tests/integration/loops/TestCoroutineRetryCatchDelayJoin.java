package jadx.tests.integration.loops;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

@SuppressWarnings("checkstyle:IllegalThrows")
public class TestCoroutineRetryCatchDelayJoin extends SmaliTest {
	private static final RuntimeException REQUEST_FAILURE = new RuntimeException("request");
	private static final RuntimeException DELAY_FAILURE = new RuntimeException("delay");

	public static void check(Class<?> generated) throws Throwable {
		Object suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
		StringBuilder trace = new StringBuilder();

		Runner direct = new Runner(generated, 0, (value, index, continuation) -> {
			trace.append('C').append(index);
			return "done";
		}, (value, continuation) -> {
			throw new AssertionError("unexpected delay");
		});
		org.assertj.core.api.Assertions.assertThat(direct.resume(Unit.INSTANCE)).isEqualTo("done");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0");

		trace.setLength(0);
		Runner nullResponse = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return null;
		}, (value, continuation) -> {
			throw new AssertionError("null response must not delay");
		});
		org.assertj.core.api.Assertions.assertThat(nullResponse.resume(Unit.INSTANCE)).isNull();
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0");

		trace.setLength(0);
		Runner terminalFailure = new Runner(generated, 0, (value, index, continuation) -> {
			trace.append('C').append(index);
			throw REQUEST_FAILURE;
		}, (value, continuation) -> {
			throw new AssertionError("terminal failure must not delay");
		});
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> terminalFailure.resume(Unit.INSTANCE))
				.isSameAs(REQUEST_FAILURE);
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0");

		trace.setLength(0);
		Runner initialFailure = new Runner(generated, 1, (value, index, continuation) -> {
			throw new AssertionError("initial failure must not call request");
		}, (value, continuation) -> {
			throw new AssertionError("initial failure must not call delay");
		});
		org.assertj.core.api.Assertions.assertThatThrownBy(
				() -> initialFailure.resume(ResultKt.createFailure(REQUEST_FAILURE)))
				.isSameAs(REQUEST_FAILURE);
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEmpty();

		trace.setLength(0);
		Runner retry = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return "done" + index;
		}, (value, continuation) -> {
			trace.append('D');
			return Unit.INSTANCE;
		});
		org.assertj.core.api.Assertions.assertThat(retry.resume(Unit.INSTANCE)).isEqualTo("done1");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0DC1");

		trace.setLength(0);
		Runner requestSuspend = new Runner(generated, 0, (value, index, continuation) -> {
			trace.append('C').append(index);
			return suspended;
		}, (value, continuation) -> {
			throw new AssertionError("unexpected delay");
		});
		org.assertj.core.api.Assertions.assertThat(requestSuspend.resume(Unit.INSTANCE)).isSameAs(suspended);
		org.assertj.core.api.Assertions.assertThat(requestSuspend.resume("resumed")).isEqualTo("resumed");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0");

		trace.setLength(0);
		Runner requestFailure = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			if (((Integer) index) == 0) {
				throw REQUEST_FAILURE;
			}
			return "done1";
		}, (value, continuation) -> {
			trace.append('D');
			return Unit.INSTANCE;
		});
		org.assertj.core.api.Assertions.assertThat(requestFailure.resume(Unit.INSTANCE)).isEqualTo("done1");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0DC1");

		trace.setLength(0);
		Runner resumedFailure = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return ((Integer) index) == 0 ? suspended : "done1";
		}, (value, continuation) -> {
			trace.append('D');
			return Unit.INSTANCE;
		});
		org.assertj.core.api.Assertions.assertThat(resumedFailure.resume(Unit.INSTANCE)).isSameAs(suspended);
		org.assertj.core.api.Assertions.assertThat(resumedFailure.resume(ResultKt.createFailure(REQUEST_FAILURE)))
				.isEqualTo("done1");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0DC1");

		trace.setLength(0);
		Runner delayFailure = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return "done0";
		}, (value, continuation) -> {
			trace.append('D');
			throw DELAY_FAILURE;
		});
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> delayFailure.resume(Unit.INSTANCE))
				.isSameAs(DELAY_FAILURE);
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0D");

		trace.setLength(0);
		Runner delaySuspend = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return "done" + index;
		}, (value, continuation) -> {
			trace.append('D');
			return suspended;
		});
		org.assertj.core.api.Assertions.assertThat(delaySuspend.resume(Unit.INSTANCE)).isSameAs(suspended);
		org.assertj.core.api.Assertions.assertThat(delaySuspend.resume(Unit.INSTANCE)).isEqualTo("done1");
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0DC1");

		trace.setLength(0);
		Runner delayResumeFailure = new Runner(generated, 1, (value, index, continuation) -> {
			trace.append('C').append(index);
			return "done0";
		}, (value, continuation) -> {
			trace.append('D');
			return suspended;
		});
		org.assertj.core.api.Assertions.assertThat(delayResumeFailure.resume(Unit.INSTANCE)).isSameAs(suspended);
		org.assertj.core.api.Assertions.assertThatThrownBy(
				() -> delayResumeFailure.resume(ResultKt.createFailure(DELAY_FAILURE)))
				.isSameAs(DELAY_FAILURE);
		org.assertj.core.api.Assertions.assertThat(trace.toString()).isEqualTo("C0D");
	}

	private static final class Runner {
		private final Object instance;
		private final Method invokeSuspend;

		Runner(Class<?> generated, int retryCount, Function3<Object, Object, Object, Object> call,
				Function2<Object, Object, Object> delay) throws Exception {
			Constructor<?> constructor = generated.getConstructor(Continuation.class);
			instance = constructor.newInstance((Object) null);
			setField(generated, "call", call);
			setField(generated, "delay", delay);
			setField(generated, "retryCount", retryCount);
			invokeSuspend = generated.getMethod("invokeSuspend", Object.class);
		}

		private void setField(Class<?> generated, String name, Object value) throws Exception {
			Field field = generated.getDeclaredField(name);
			field.setAccessible(true);
			field.set(instance, value);
		}

		Object resume(Object value) throws Throwable {
			try {
				return invokeSuspend.invoke(instance, value);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}

	public static final class Reference {
		private Function3<Object, Object, Object, Object> call;
		private Function2<Object, Object, Object> delay;
		private int retryCount;
		private int index;
		private Object result;
		private int label;

		public Reference(Continuation<?> ignored) {
		}

		public Object invokeSuspend(Object value) throws Throwable {
			Object suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
			int current;
			Object previous;
			boolean resumedCall = false;
			switch (label) {
				case 0:
					ResultKt.throwOnFailure(value);
					current = 0;
					previous = null;
					break;
				case 1:
					current = index;
					previous = result;
					resumedCall = true;
					break;
				case 2:
					current = index;
					previous = result;
					ResultKt.throwOnFailure(value);
					break;
				default:
					throw new IllegalStateException("unexpected state");
			}
			while (true) {
				Object response;
				try {
					if (resumedCall) {
						resumedCall = false;
						ResultKt.throwOnFailure(value);
						response = value;
					} else {
						result = previous;
						index = current;
						label = 1;
						response = call.invoke(previous, current, this);
						if (response == suspended) {
							return suspended;
						}
					}
				} catch (Throwable failure) {
					if (current >= retryCount) {
						throw failure;
					}
					previous = failure;
					current++;
					result = previous;
					index = current;
					label = 2;
					if (delay.invoke(1L, this) == suspended) {
						return suspended;
					}
					continue;
				}
				if (current >= retryCount || response == null) {
					return response;
				}
				previous = response;
				current++;
				result = previous;
				index = current;
				label = 2;
				if (delay.invoke(1L, this) == suspended) {
					return suspended;
				}
			}
		}
	}

	@Test
	public void validatesBehaviorOracle() throws Throwable {
		check(Reference.class);
	}

	@Test
	public void sourceReferenceRoundTrip() throws Throwable {
		ClassNode cls = getClassNode(Reference.class);
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(List.of(cls));
			Class<?> generated = compiler.getClass(Reference.class.getName());
			org.assertj.core.api.Assertions.assertThat(generated).isNotSameAs(Reference.class);
			check(generated);
		}
	}

	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isZero();
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isLessThanOrEqualTo(1);
	}

	@Test
	public void preservesProtectedScopeByDefault() {
		getArgs().setShowInconsistentCode(false);
		ClassNode cls = getClassNodeFromSmali();
		org.assertj.core.api.Assertions.assertThat(cls.root().getErrorsCounter().getWarnsCount()).isZero();
		assertThat(cls)
				.code()
				.contains("catch (Throwable")
				.doesNotContain("Method not decompiled:")
				.doesNotContain("Incorrect try/catch scope:");
	}

	/**
	 * Protected request/resume calls share a retry catch, but the suspension delay is outside
	 * its scope. The direct delay completion must re-enter the state dispatcher so that the
	 * handler and all protected calls form one natural loop without catching delay failures.
	 */
	@Test
	public void test() {
		forceDecompiledCheck();
		ClassNode cls = getClassNodeFromSmali();
		org.assertj.core.api.Assertions.assertThat(cls.root().getErrorsCounter().getWarnsCount()).isZero();
		assertThat(cls)
				.code()
				.containsOne("while (true)")
				.contains("catch (Throwable")
				.contains("this.delay.invoke(1L, this)")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
