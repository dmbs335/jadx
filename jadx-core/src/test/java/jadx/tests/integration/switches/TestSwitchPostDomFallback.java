package jadx.tests.integration.switches;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestSwitchPostDomFallback extends IntegrationTest {

	public static class TestCls {
		public static boolean emptySwitchAfterStringSwitch(String str) {
			byte type = 0;
			switch (str) {
				case "SM-A520":
					break;
				case "SM-G930":
					type = 1;
					break;
				default:
					type = -1;
					break;
			}
			switch (type) {
			}
			return true;
		}

		public static String nestedSwitchInLoop(String str) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i);
				switch (ch) {
					case '(':
					case ')':
						sb.append('\\').append(ch);
						break;
					case '*':
						sb.append(".*");
						continue;
					default:
						switch (ch) {
							case '[':
							case '\\':
							case ']':
							case '^':
								sb.append('\\').append(ch);
								break;
							default:
								sb.append(ch);
								continue;
						}
						break;
				}
			}
			return sb.toString();
		}
	}

	@Test
	public void test() {
		allowWarnInCode(); // unrelated switch-order warning from the string-switch lowering
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("switch (str)")
				.countString(2, "switch (ch)")
				.containsOne("return sb.toString();")
				.doesNotContain("Failed to find 'out' block for switch")
				.doesNotContain("already processed");
	}
}
