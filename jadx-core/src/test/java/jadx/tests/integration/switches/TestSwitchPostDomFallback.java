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

		public static int nestedSwitchesShareOuterExit(int outer, int first, int second) {
			int result = 0;
			switch (outer) {
				case 0:
					switch (first) {
						case 1:
							result = 11;
							break;
						case 2:
							result = 12;
							break;
						default:
							result = 10;
							break;
					}
					break;

				case 1:
					switch (second) {
						case 1:
							result = 21;
							break;
						case 2:
							result = 22;
							break;
						default:
							result = 20;
							break;
					}
					break;
			}
			return result;
		}
	}

	@Test
	public void test() {
		allowWarnInCode(); // unrelated switch-order warning from the string-switch lowering
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("switch (str)")
				.countString(2, "switch (ch)")
				.countString(1, "switch (outer)")
				.countString(1, "switch (first)")
				.countString(1, "switch (second)")
				.containsOne("return sb.toString();")
				.doesNotContain("Failed to find 'out' block for switch")
				.doesNotContain("already processed");
	}
}
