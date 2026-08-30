package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineChannelFilterNotNullCollectionLoop extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);
		assertThat(searchCls(loadFromSmaliFiles(),
				"kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt"))
						.code()
						.containsOne("Split coroutine resume Boolean projection/branch before loop header")
						.containsOne("while (true)")
						.countString(2, "hasNext(")
						.countString(2, ".add(")
						.containsOne("if (next != null) {")
						.containsOne("if (next2 != null) {")
						.doesNotContain("Unsupported multi-entry loop")
						.doesNotContain("Region traversal cycle")
						.doesNotContain("Recursive region processing")
						.doesNotContain("Code duplicated")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX ERROR");
	}
}
