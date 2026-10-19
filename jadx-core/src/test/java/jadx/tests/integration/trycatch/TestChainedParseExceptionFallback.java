package jadx.tests.integration.trycatch;

import java.text.ParseException;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestChainedParseExceptionFallback extends IntegrationTest {

	public static class TestCls {
		private int parse(String value, String accepted, int result) throws ParseException {
			if (accepted.equals(value)) {
				return result;
			}
			throw new ParseException(value, 0);
		}

		private synchronized int parseWithFallback(String value) {
			try {
				return parse(value, "local", 1);
			} catch (ParseException ignored) {
				try {
					return parse(value, "en", 2);
				} catch (ParseException ignoredAgain) {
					try {
						return parse(value, "iso", 3);
					} catch (ParseException failure) {
						throw new IllegalArgumentException(value, failure);
					}
				}
			}
		}

		public void check() {
			if (parseWithFallback("local") != 1 || parseWithFallback("en") != 2 || parseWithFallback("iso") != 3) {
				throw new AssertionError("chained parse fallback");
			}
			try {
				parseWithFallback("invalid");
				throw new AssertionError("missing parse failure");
			} catch (IllegalArgumentException expected) {
				if (!(expected.getCause() instanceof ParseException)) {
					throw new AssertionError("incorrect parse failure cause", expected);
				}
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void test() {
		getClassNode(TestCls.class);
	}
}
