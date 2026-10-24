package jadx.tests.integration.others;

import java.nio.charset.StandardCharsets;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

public class TestStringConstructorLiteralArrays extends IntegrationTest {
	public static class TestCls {
		public static String fromChars() {
			return new String(new char[] { '\u0100', 'A' });
		}

		public static String fromCharSlice() {
			return new String(new char[] { 'A', 'B' }, 1, 1);
		}

		public static String fromBytesWithCharset() {
			return new String(new byte[] { (byte) 0xff }, StandardCharsets.ISO_8859_1);
		}

		public void check() {
			if (!"\u0100A".equals(fromChars())) {
				throw new AssertionError("char values must not be truncated to bytes");
			}
			if (!"B".equals(fromCharSlice())) {
				throw new AssertionError("char slice must keep the char-array constructor");
			}
			if (!"\u00ff".equals(fromBytesWithCharset())) {
				throw new AssertionError("explicit charset must consume the original bytes");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void preservesStringArrayConstructorSemantics() {
		getClassNode(TestCls.class);
	}
}
