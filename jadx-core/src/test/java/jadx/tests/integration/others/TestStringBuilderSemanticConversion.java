package jadx.tests.integration.others;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;
import jadx.tests.api.utils.assertj.JadxAssertions;

@SuppressWarnings("StringBufferReplaceableByString")
public class TestStringBuilderSemanticConversion extends IntegrationTest {

	public static class TestCls {
		public static String singleString(String value) {
			return new StringBuilder().append(value).toString();
		}

		public static String nullableInitial(String value) {
			return new StringBuilder(value).append("tail").toString();
		}

		public static String appendChars(char[] value) {
			return new StringBuilder().append("head").append(value).toString();
		}

		public static String appendSequence(CharSequence value) {
			return new StringBuilder().append("head").append(value).toString();
		}

		public void check() {
			if (!"null".equals(singleString(null))) {
				throw new AssertionError("single string append must convert null");
			}
			try {
				nullableInitial(null);
				throw new AssertionError("nullable constructor must throw");
			} catch (NullPointerException expected) {
				// expected
			}
			if (!"headab".equals(appendChars(new char[] { 'a', 'b' }))) {
				throw new AssertionError("char array append must use array contents");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesStringBuilderOverloadSemantics() {
		JadxAssertions.assertThat(getClassNode(TestCls.class))
				.code()
				.contains("return \"\" + value;")
				.contains("new StringBuilder(value).append(\"tail\").toString()")
				.countString(2, "new StringBuilder().append(\"head\").append(value).toString()");
	}
}
