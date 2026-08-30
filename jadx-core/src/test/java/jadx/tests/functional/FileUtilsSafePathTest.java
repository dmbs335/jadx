package jadx.tests.functional;

import org.junit.jupiter.api.Test;

import jadx.core.utils.files.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsSafePathTest {
	@Test
	void escapesWindowsInvalidCharactersWithoutFlatteningArchivePath() {
		assertThat(FileUtils.toSafeFilePath("assets/auth/app/v1/my-pages/config:default/{id}/put"))
				.isEqualTo("assets/auth/app/v1/my-pages/config%3adefault/{id}/put");
		assertThat(FileUtils.toSafeFilePath("assets/notification/devices:register/post"))
				.isEqualTo("assets/notification/devices%3aregister/post");
	}

	@Test
	void keepsEscapingCollisionFree() {
		assertThat(FileUtils.toSafeFilePath("assets/config%3Adefault"))
				.isEqualTo("assets/config%253Adefault");
	}

	@Test
	void escapesWindowsReservedNamesAndTrailingCharacters() {
		assertThat(FileUtils.toSafeFilePath("assets/CON/file. "))
				.isEqualTo("assets/%43ON/file.%20");
		assertThat(FileUtils.toSafeFilePath("assets/CON./value"))
				.isEqualTo("assets/%43ON%2e/value");
		assertThat(FileUtils.toSafeFilePath("assets/CON .txt/value"))
				.isEqualTo("assets/%43ON .txt/value");
		assertThat(FileUtils.toSafeFilePath("assets/com1.txt/value"))
				.isEqualTo("assets/%63om1.txt/value");
	}

	@Test
	void normalizesArchiveSeparators() {
		assertThat(FileUtils.toSafeFilePath("assets\\api:variant\\get"))
				.isEqualTo("assets/api%3avariant/get");
	}
}
