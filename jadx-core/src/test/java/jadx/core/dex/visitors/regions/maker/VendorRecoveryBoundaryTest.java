package jadx.core.dex.visitors.regions.maker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architecture ratchet for library-specific recovery rules living in generic region passes.
 *
 * Generated Ktor continuation identities have been removed. Stable API types may remain in the
 * narrowly-scoped structural proofs below, and this allow-list prevents that vendor knowledge from
 * spreading into unrelated passes.
 */
class VendorRecoveryBoundaryTest {
	private static final Set<String> EXISTING_DIRECT_KTOR_USERS = Set.of(
			"jadx/core/dex/visitors/blocks/FixMultiEntryLoops.java",
			"jadx/core/dex/visitors/regions/maker/IfRegionMaker.java",
			"jadx/core/dex/visitors/regions/maker/KtorCioRecovery.java",
			"jadx/core/dex/visitors/regions/maker/RegionMaker.java",
			"jadx/core/dex/visitors/regions/maker/SwitchRegionMaker.java");

	@Test
	void directKtorKnowledgeDoesNotSpread() throws IOException {
		Path sourceRoot = Path.of("src/main/java");
		Set<String> actual = new TreeSet<>();
		try (Stream<Path> paths = Files.walk(sourceRoot)) {
			paths.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> contains(path, "io.ktor."))
					.map(sourceRoot::relativize)
					.map(Path::toString)
					.map(path -> path.replace('\\', '/'))
					.forEach(actual::add);
		}
		assertThat(actual)
				.as("vendor-specific Ktor recovery belongs behind a dedicated boundary")
				.containsExactlyInAnyOrderElementsOf(EXISTING_DIRECT_KTOR_USERS);
	}

	@Test
	void applicationIdentityDoesNotControlGenericRegionRecovery() throws IOException {
		Path sourceRoot = Path.of("src/main/java/jadx/core/dex/visitors/regions");
		try (Stream<Path> paths = Files.walk(sourceRoot)) {
			assertThat(paths.filter(path -> path.toString().endsWith(".java")))
					.as("generic decompilation must not depend on an application class identity")
					.noneMatch(path -> contains(path, "getRawFullName().equals(\""));
		}
	}

	private static boolean contains(Path path, String marker) {
		try {
			return Files.readString(path).contains(marker);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to inspect " + path, e);
		}
	}
}
