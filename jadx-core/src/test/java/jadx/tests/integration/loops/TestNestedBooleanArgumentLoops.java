package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNestedBooleanArgumentLoops extends IntegrationTest {
	public static class TestCls {
		private int count;
		private int checksum;

		private void record(boolean stop, int failures, int retries, boolean outside, boolean handler, boolean returned) {
			count++;
			checksum += (stop ? 100000 : 0) + failures * 10000 + retries * 1000
					+ (outside ? 100 : 0) + (handler ? 10 : 0) + (returned ? 1 : 0);
		}

		public void run() {
			for (int stop = 0; stop < 2; stop++) {
				for (int failures = 0; failures < 4; failures++) {
					for (int retries = 0; retries < 4; retries++) {
						for (int outside = 0; outside < 2; outside++) {
							for (int handler = 0; handler < 2; handler++) {
								record(stop != 0, failures, retries, outside != 0, handler != 0, false);
								record(stop != 0, failures, retries, outside != 0, handler != 0, true);
							}
						}
					}
				}
			}
		}

		public void check() {
			run();
			assertThat(count).isEqualTo(256);
			assertThat(checksum).isEqualTo(17038208);
		}
	}

	@Test
	public void test() {
		getClassNode(TestCls.class);
	}

	@Test
	public void testWithoutDebugInfo() {
		noDebugInfo();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInput() {
		useJavaInput();
		getClassNode(TestCls.class);
	}

	@Test
	public void testJavaInputWithoutDebugInfo() {
		useJavaInput();
		noDebugInfo();
		getClassNode(TestCls.class);
	}
}
