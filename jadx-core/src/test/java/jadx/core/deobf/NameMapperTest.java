package jadx.core.deobf;

import java.util.Random;

import org.junit.jupiter.api.Test;

import static jadx.core.deobf.NameMapper.isValidIdentifier;
import static jadx.core.deobf.NameMapper.removeInvalidChars;
import static jadx.core.deobf.NameMapper.removeInvalidCharsMiddle;
import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class NameMapperTest {

	@Test
	public void validIdentifiers() {
		assertThat(isValidIdentifier("ACls")).isTrue();
	}

	@Test
	public void notValidIdentifiers() {
		assertThat(isValidIdentifier("1cls")).isFalse();
		assertThat(isValidIdentifier("-cls")).isFalse();
		assertThat(isValidIdentifier("A-cls")).isFalse();
	}

	@Test
	public void matchesJavaIdentifierPatternForUnicodeAndMalformedUtf16() {
		String[] edgeCases = {
				"\uD835\uDC9Cname",
				"name\uD835\uDC9C",
				"\uD800",
				"name\uDC00",
				"\u03BB\u03BF\u03B3\u03B9\u03BA\u03AE",
				"class"
		};
		for (String str : edgeCases) {
			assertMatchesPattern(str);
		}

		Random random = new Random(0x4A_41_44_58L);
		for (int i = 0; i < 20_000; i++) {
			int length = random.nextInt(8);
			char[] chars = new char[length];
			for (int j = 0; j < length; j++) {
				chars[j] = (char) random.nextInt(Character.MAX_VALUE + 1);
			}
			assertMatchesPattern(new String(chars));
		}
	}

	private static void assertMatchesPattern(String str) {
		boolean expected = !NameMapper.isReserved(str)
				&& NameMapper.VALID_JAVA_IDENTIFIER.matcher(str).matches();
		assertThat(isValidIdentifier(str)).isEqualTo(expected);
	}

	@Test
	public void testRemoveInvalidCharsMiddle() {
		assertThat(removeInvalidCharsMiddle("1cls")).isEqualTo("1cls");
		assertThat(removeInvalidCharsMiddle("-cls")).isEqualTo("cls");
		assertThat(removeInvalidCharsMiddle("A-cls")).isEqualTo("Acls");
	}

	@Test
	public void testRemoveInvalidChars() {
		assertThat(removeInvalidChars("1cls", "C")).isEqualTo("C1cls");
		assertThat(removeInvalidChars("-cls", "C")).isEqualTo("cls");
		assertThat(removeInvalidChars("A-cls", "C")).isEqualTo("Acls");
	}
}
