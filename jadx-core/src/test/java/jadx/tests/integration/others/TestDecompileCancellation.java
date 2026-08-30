package jadx.tests.integration.others;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import jadx.api.ICodeWriter;
import jadx.api.JadxArgs;
import jadx.api.impl.SimpleCodeWriter;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.ProcessState;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.IDexTreeVisitor;
import jadx.core.utils.exceptions.JadxTaskCancelledException;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestDecompileCancellation extends IntegrationTest {
	public static class TestCls {
		public int twice(int value) {
			return value * 2;
		}
	}

	@Test
	public void testCancellationIsNotCachedAndClassCanBeRetried() {
		ClassNode cls = getClassNode(TestCls.class);
		IDexTreeVisitor cancellingPass = new AbstractVisitor() {
			@Override
			public void visit(MethodNode mth) {
				Thread.currentThread().interrupt();
			}
		};
		List<IDexTreeVisitor> passes = cls.root().getProcessClasses().getPasses();
		passes.add(0, cancellingPass);
		cls.unloadCode();
		try {
			assertThatThrownBy(cls::decompile).isInstanceOf(JadxTaskCancelledException.class);
			assertCleanCancellation(cls);
		} finally {
			Thread.interrupted();
			passes.remove(cancellingPass);
		}

		assertRetrySucceeds(cls);
	}

	@Test
	public void testCancellationDuringCodegenIsNotCachedAndCanBeRetried() {
		ClassNode cls = getClassNode(TestCls.class);
		Function<JadxArgs, ICodeWriter> originalProvider = getArgs().getCodeWriterProvider();
		getArgs().setCodeWriterProvider(InterruptingCodeWriter::new);
		cls.unloadCode();
		try {
			assertThatThrownBy(cls::decompile).isInstanceOf(JadxTaskCancelledException.class);
			assertCleanCancellation(cls);
		} finally {
			Thread.interrupted();
			getArgs().setCodeWriterProvider(originalProvider);
		}

		assertRetrySucceeds(cls);
	}

	@Test
	public void testCancellationDuringPartialMethodProcessingCanBeRetried() {
		ClassNode cls = getClassNode(TestCls.class);
		MethodNode mth = cls.getMethods().stream()
				.filter(method -> method.getName().equals("twice"))
				.findFirst()
				.orElseThrow();
		IDexTreeVisitor cancellingPass = new AbstractVisitor() {
			@Override
			public void visit(MethodNode method) {
				Thread.currentThread().interrupt();
			}
		};
		List<IDexTreeVisitor> passes = cls.root().getProcessClasses().getPasses();
		passes.add(0, cancellingPass);
		try {
			assertThatThrownBy(() -> cls.root().getProcessClasses().processMethodToVisitor(mth, cancellingPass))
					.isInstanceOf(JadxTaskCancelledException.class);
			assertCleanCancellation(cls);
		} finally {
			Thread.interrupted();
			passes.remove(cancellingPass);
		}

		assertRetrySucceeds(cls);
	}

	private static void assertCleanCancellation(ClassNode cls) {
		assertThat(cls.getState()).isEqualTo(ProcessState.NOT_LOADED);
		assertThat(cls.getCodeFromCache()).isNull();
		assertThat(cls.contains(AType.JADX_ERROR)).isFalse();
	}

	private static void assertRetrySucceeds(ClassNode cls) {
		String code = cls.decompile().getCodeStr();
		assertThat(code).contains("return value * 2;");
		assertThat(code).doesNotContain("JADX task cancelled");
	}

	private static final class InterruptingCodeWriter extends SimpleCodeWriter {
		private boolean interruptPending = true;

		private InterruptingCodeWriter(JadxArgs args) {
			super(args);
		}

		@Override
		public SimpleCodeWriter startLine() {
			SimpleCodeWriter writer = super.startLine();
			if (interruptPending) {
				interruptPending = false;
				Thread.currentThread().interrupt();
			}
			return writer;
		}
	}
}
