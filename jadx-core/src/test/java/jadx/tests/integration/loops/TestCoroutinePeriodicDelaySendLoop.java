package jadx.tests.integration.loops;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutinePeriodicDelaySendLoop extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getWarnsCount()).isZero();
	}

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls)
				.code()
				.containsOne("Normalize periodic delay/send loop completion through state dispatch")
				.containsOne("this.result = obj;")
				.containsOne("a.getCOROUTINE_SUSPENDED()")
				.contains("do {")
				.contains("} while (obj != coroutine_suspended);")
				// The smali sends immediately after state 3 restores L$0. A prior region tree
				// used producerScope before assignment and repeated delay instead of send.
				// TODO: Immediate completion of the repeated delay reaches :send without
				// throwOnFailure; the current state-dispatch rendering re-enters state 3 first.
				.containsLines(4,
						"if (i != 3) {",
						"    throw new IllegalStateException();",
						"}",
						"producerScope = (ProducerScope) this.L$0;",
						"ResultKt.throwOnFailure(obj);",
						"channel = producerScope.getChannel();")
				.countString(3, "channel.send(Unit.INSTANCE, this)")
				.doesNotContain("Unsupported multi-entry loop")
				.doesNotContain("Region traversal cycle")
				.doesNotContain("Recursive region processing")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
		String code = cls.getCode().getCodeStr();
		org.assertj.core.api.Assertions.assertThat(code.indexOf("this.result = obj;"))
				.isLessThan(code.indexOf("do {"));
		org.assertj.core.api.Assertions.assertThat(code.indexOf("a.getCOROUTINE_SUSPENDED()"))
				.isLessThan(code.indexOf("do {"));
	}
}
