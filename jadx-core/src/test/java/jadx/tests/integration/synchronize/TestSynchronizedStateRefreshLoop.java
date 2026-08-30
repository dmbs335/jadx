package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSynchronizedStateRefreshLoop extends SmaliTest {

	@Test
	public void test() {
		assertThat(searchCls(loadFromSmaliFiles(), "synchronize.TestSynchronizedStateRefreshLoop"))
				.code()
				.containsOne("while (")
				.containsOne("state2 = state;")
				.containsOne("objCreate = create();")
				.contains("drain();")
				.doesNotContain("Method not decompiled")
				.doesNotContain("missing block")
				.doesNotContain("JADX ERROR");
	}
}
