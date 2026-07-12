package jadx.tests.integration.synchronize;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestGenericStaticSynchronized extends IntegrationTest {

	public static class TestCls<T> {
		private static synchronized <V> V get(V value) {
			return value;
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("private static synchronized <V> V get(V value) {")
				.doesNotContain("synchronized (TestCls.class)")
				.doesNotContain("JADX WARN");
	}
}
