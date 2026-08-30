package jadx.core.xmlgen;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceOutputRegistryTest {
	@Test
	void separatesCaseEquivalentArchivePathsDeterministically() {
		ResourceOutputRegistry first = new ResourceOutputRegistry(List.of("assets/Foo.png", "assets/foo.png"));
		ResourceOutputRegistry second = new ResourceOutputRegistry(List.of("assets/foo.png", "assets/Foo.png"));

		assertThat(first.resolve("assets/Foo.png")).isEqualTo(second.resolve("assets/Foo.png"));
		assertThat(first.resolve("assets/foo.png")).isEqualTo(second.resolve("assets/foo.png"));
		assertThat(first.resolve("assets/Foo.png").toLowerCase())
				.isNotEqualTo(first.resolve("assets/foo.png").toLowerCase());
	}

	@Test
	void separatesUnicodeNormalizationEquivalentPaths() {
		String composed = "assets/caf\u00e9.json";
		String decomposed = "assets/cafe\u0301.json";
		ResourceOutputRegistry registry = new ResourceOutputRegistry(List.of(composed, decomposed));

		assertThat(registry.resolve(composed)).isNotEqualTo(registry.resolve(decomposed));
	}

	@Test
	void separatesFileFromImplicitDirectory() {
		ResourceOutputRegistry registry = new ResourceOutputRegistry(List.of("assets/api", "assets/api/config.json"));

		assertThat(registry.resolve("assets/api")).startsWith("assets/api~");
		assertThat(registry.resolve("assets/api/config.json")).isEqualTo("assets/api/config.json");
	}

	@Test
	void returnsSameMappingForRepeatedResolution() {
		ResourceOutputRegistry registry = new ResourceOutputRegistry(List.of("assets/Foo", "assets/foo"));

		assertThat(registry.resolve("assets/Foo")).isEqualTo(registry.resolve("assets/Foo"));
	}

	@Test
	void treatsArchiveSeparatorsAsTheSameSourcePath() {
		ResourceOutputRegistry registry = new ResourceOutputRegistry(List.of("assets\\config\\settings.json"));

		assertThat(registry.resolve("assets/config/settings.json"))
				.isEqualTo("assets/config/settings.json");
	}

	@Test
	void allowsSiblingFilesWithoutScanningAllocatedDescendants() {
		ResourceOutputRegistry registry = new ResourceOutputRegistry(List.of(
				"assets/config/first.json",
				"assets/config/second.json",
				"assets/images/icon.png"));

		assertThat(registry.resolve("assets/config/first.json"))
				.isEqualTo("assets/config/first.json");
		assertThat(registry.resolve("assets/config/second.json"))
				.isEqualTo("assets/config/second.json");
	}
}
