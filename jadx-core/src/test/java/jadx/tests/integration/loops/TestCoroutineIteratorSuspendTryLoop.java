package jadx.tests.integration.loops;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlinx.coroutines.Deferred;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;
import jadx.tests.api.compiler.TestCompiler;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineIteratorSuspendTryLoop extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		super.runChecks(classes);
		try (TestCompiler compiler = new TestCompiler(getCompilerOptions())) {
			compiler.compileNodes(classes);
			Class<?> generated = compiler.getClass("loops.TestCoroutineIteratorSuspendTryLoop");
			Method clear = generated.getMethod("clear", Map.class, long.class, Continuation.class);
			checkImmediate(clear);
			checkSuspended(clear);
		} catch (Exception e) {
			throw new AssertionError("Iterator coroutine behavior differs from smali", e);
		}
	}

	private static void checkImmediate(Method clear) throws Exception {
		assertImmediate(clear, true, false, null, true);
		assertImmediate(clear, false, false, null, false);
		assertImmediate(clear, false, true, 4, true);
		assertImmediate(clear, false, true, 6, false);
		assertImmediate(clear, false, true, new IllegalStateException("await"), true);
		assertImmediate(clear, false, true, new ThrowingHash(), true);
	}

	private static void assertImmediate(Method clear, boolean cancelled, boolean completed,
			Object awaited, boolean removed) throws Exception {
		TestDeferred deferred = new TestDeferred(cancelled, completed, awaited);
		Map<String, Deferred> map = singleEntry(deferred);
		RecordingContinuation completion = new RecordingContinuation();
		org.assertj.core.api.Assertions.assertThat(invoke(clear, map, 5L, completion)).isSameAs(Unit.INSTANCE);
		org.assertj.core.api.Assertions.assertThat(map.containsKey("key")).isEqualTo(!removed);
		org.assertj.core.api.Assertions.assertThat(deferred.awaitCalls).isEqualTo(completed && !cancelled ? 1 : 0);
	}

	private static void checkSuspended(Method clear) throws Exception {
		for (int hash : new int[] { 4, 6 }) {
			TestDeferred deferred = new TestDeferred(false, true, IntrinsicsKt.getCOROUTINE_SUSPENDED());
			Map<String, Deferred> map = singleEntry(deferred);
			RecordingContinuation completion = new RecordingContinuation();
			org.assertj.core.api.Assertions.assertThat(invoke(clear, map, 5L, completion))
					.isSameAs(IntrinsicsKt.getCOROUTINE_SUSPENDED());
			org.assertj.core.api.Assertions.assertThat(map).containsKey("key");
			org.assertj.core.api.Assertions.assertThat(deferred.captured).isNotNull();
			deferred.captured.resumeWith(hash);
			org.assertj.core.api.Assertions.assertThat(completion.result).isSameAs(Unit.INSTANCE);
			org.assertj.core.api.Assertions.assertThat(map.containsKey("key"))
					.as("resume hash=%s, map=%s", hash, map).isEqualTo(hash > 5);
		}

		TestDeferred deferred = new TestDeferred(false, true, IntrinsicsKt.getCOROUTINE_SUSPENDED());
		Map<String, Deferred> map = singleEntry(deferred);
		RecordingContinuation completion = new RecordingContinuation();
		invoke(clear, map, 5L, completion);
		IllegalStateException failure = new IllegalStateException("resume");
		deferred.captured.resumeWith(ResultKt.createFailure(failure));
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> ResultKt.throwOnFailure(completion.result))
				.isSameAs(failure);
		org.assertj.core.api.Assertions.assertThat(map).containsKey("key");
	}

	private static Map<String, Deferred> singleEntry(TestDeferred deferred) {
		Map<String, Deferred> map = new LinkedHashMap<>();
		map.put("key", deferred);
		return map;
	}

	private static Object invoke(Method clear, Map<String, Deferred> map, long targetTime,
			RecordingContinuation completion) throws Exception {
		try {
			return clear.invoke(null, map, targetTime, completion);
		} catch (InvocationTargetException e) {
			throw new AssertionError("Unexpected coroutine failure", e.getCause());
		}
	}

	private static final class TestDeferred implements Deferred {
		private final boolean cancelled;
		private final boolean completed;
		private final Object awaited;
		private Continuation<?> captured;
		private int awaitCalls;

		private TestDeferred(boolean cancelled, boolean completed, Object awaited) {
			this.cancelled = cancelled;
			this.completed = completed;
			this.awaited = awaited;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public boolean isCompleted() {
			return completed;
		}

		@Override
		public Object await(Continuation<?> continuation) {
			awaitCalls++;
			captured = continuation;
			if (awaited instanceof RuntimeException) {
				throw (RuntimeException) awaited;
			}
			return awaited;
		}
	}

	private static final class RecordingContinuation implements Continuation<Object> {
		private Object result;

		@Override
		public CoroutineContext getContext() {
			return EmptyCoroutineContext.INSTANCE;
		}

		@Override
		public void resumeWith(Object result) {
			this.result = result;
		}
	}

	@SuppressWarnings("checkstyle:EqualsHashCode")
	private static final class ThrowingHash {
		@Override
		public int hashCode() {
			throw new IllegalStateException("hashCode");
		}
	}

	@Test
	public void reconstructsIteratorAwaitResume() {
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.contains("while (it.hasNext())")
				.contains("hasNext()")
				.contains("await(")
				.contains("if (j < objAwait.hashCode())")
				.doesNotContain("Method not decompiled")
				.doesNotContain("Code decompiled incorrectly");
	}
}
