package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.api.CommentsLevel;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCoroutineNestedInvalidationDrain extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		getArgs().setCommentsLevel(CommentsLevel.DEBUG);

		assertThat(searchCls(
				loadFromSmaliFiles(),
				"androidx.room.RoomTrackingLiveData"))
						.code()
						.containsOne("Merge coroutine invalidation-drain entries through nested-loop pre-header")
						.countString(2, "while (this.o.compareAndSet(true, false))")
						.containsOne("while (true) {")
						.containsLines(2,
								"while (true) {",
								indent() + "if (this.p.compareAndSet(false, true)) {",
								indent(2) + "objCompute = null;",
								indent(2) + "i = 0;",
								indent(2) + "while (this.o.compareAndSet(true, false)) {")
						.contains("postValue(")
						.contains("this.p.set(false)")
						.contains("return coroutine_suspended")
						.doesNotContain("Unsupported multi-entry loop pattern")
						.doesNotContain("Method not decompiled")
						.doesNotContain("JADX WARN")
						.doesNotContain("JADX ERROR");
	}
}
