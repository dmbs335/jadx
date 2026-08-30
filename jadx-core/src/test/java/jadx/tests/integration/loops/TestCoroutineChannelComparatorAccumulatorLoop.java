package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelComparatorAccumulatorLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(loadFromSmaliFiles(),
				"kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt"))
						.code()
						.containsOne("Split coroutine comparator accumulator resume iteration before loop")
						.contains("while (true)")
						.contains("comparator.compare")
						.countString(2, "if (comparator.compare(obj, next) >= 0)")
						.doesNotContain("if (comparator.compare(obj, next) < 0)")
						.doesNotContain("continue;")
						.doesNotContain("Unsupported multi-entry loop")
						.doesNotContain("Region traversal cycle")
						.doesNotContain("Recursive region processing")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
