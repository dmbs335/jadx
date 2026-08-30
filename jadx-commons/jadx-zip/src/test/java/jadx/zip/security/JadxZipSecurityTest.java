package jadx.zip.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JadxZipSecurityTest {
	private final JadxZipSecurity security = new JadxZipSecurity();

	@Test
	void acceptsHostIndependentZipNames() {
		assertThat(security.isValidEntryName("assets/auth/app/v1/my-pages/config:default/get")).isTrue();
		assertThat(security.isValidEntryName("assets/notification/app/v1/devices:register/post")).isTrue();
		assertThat(security.isValidEntryName("assets/portal/app/v1/home-screen:tab/get")).isTrue();
	}

	@Test
	void rejectsTraversalWithEitherSeparator() {
		assertThat(security.isValidEntryName("../classes.dex")).isFalse();
		assertThat(security.isValidEntryName("assets/../classes.dex")).isFalse();
		assertThat(security.isValidEntryName("assets\\..\\classes.dex")).isFalse();
		assertThat(security.isValidEntryName("assets/..")).isFalse();
	}

	@Test
	void rejectsAbsoluteAndWindowsDrivePaths() {
		assertThat(security.isValidEntryName("/classes.dex")).isFalse();
		assertThat(security.isValidEntryName("\\\\server\\share\\classes.dex")).isFalse();
		assertThat(security.isValidEntryName("C:\\classes.dex")).isFalse();
		assertThat(security.isValidEntryName("C:classes.dex")).isFalse();
	}

	@Test
	void rejectsNullCharacter() {
		assertThat(security.isValidEntryName("assets/a\0b")).isFalse();
	}
}
