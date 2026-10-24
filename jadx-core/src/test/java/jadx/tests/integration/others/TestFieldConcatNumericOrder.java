package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestFieldConcatNumericOrder extends IntegrationTest {
	public static class TestCls {
		private static String text;
		private String instanceText;

		public static void appendNumbers(int first, int second) {
			text = text + first + second + "!";
		}

		public void appendInstanceNumbers(int first, int second) {
			instanceText = instanceText + first + second + "!";
		}

		public void check() {
			text = "P";
			appendNumbers(1, 2);
			if (!"P12!".equals(text)) {
				throw new AssertionError("string concat must precede numeric addition: " + text);
			}
			instanceText = "Q";
			appendInstanceNumbers(3, 4);
			if (!"Q34!".equals(instanceText)) {
				throw new AssertionError("instance field concat must precede numeric addition: " + instanceText);
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesFieldConcatEvaluationOrder() {
		getClassNode(TestCls.class);
	}
}
