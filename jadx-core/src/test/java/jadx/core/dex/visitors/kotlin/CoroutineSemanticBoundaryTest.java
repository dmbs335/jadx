package jadx.core.dex.visitors.kotlin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoroutineSemanticBoundaryTest {
	private static final Set<String> EXISTING_DIRECT_INVOKE_SUSPEND_USERS = Set.of(
			"jadx/core/dex/visitors/blocks/FixMultiEntryLoops.java",
			"jadx/core/dex/visitors/kotlin/CoroutineMethodUtils.java");

	private static final Set<String> EXISTING_DIRECT_CONTINUATION_USERS = Set.of(
			"jadx/core/dex/visitors/blocks/FixMultiEntryLoops.java",
			"jadx/core/dex/visitors/kotlin/CoroutineMethodUtils.java");

	private static final Set<String> EXISTING_DIRECT_SUSPEND_LAMBDA_USERS = Set.of(
			"jadx/core/dex/visitors/kotlin/CoroutineMethodUtils.java");

	@Test
	void directInvokeSuspendKnowledgeDoesNotSpread() throws IOException {
		assertDirectKnowledgeDoesNotSpread(
				"invokeSuspend",
				EXISTING_DIRECT_INVOKE_SUSPEND_USERS,
				"direct invokeSuspend recognition is technical debt; migrate existing users instead of adding new ones");
	}

	@Test
	void directContinuationKnowledgeDoesNotSpread() throws IOException {
		assertDirectKnowledgeDoesNotSpread(
				"kotlin.coroutines.Continuation",
				EXISTING_DIRECT_CONTINUATION_USERS,
				"direct Continuation recognition belongs behind CoroutineMethodUtils");
	}

	@Test
	void directSuspendLambdaKnowledgeDoesNotSpread() throws IOException {
		assertDirectKnowledgeDoesNotSpread(
				"\"SuspendLambda\"",
				EXISTING_DIRECT_SUSPEND_LAMBDA_USERS,
				"direct SuspendLambda recognition belongs behind CoroutineMethodUtils");
	}

	private static void assertDirectKnowledgeDoesNotSpread(
			String marker, Set<String> expectedUsers, String description) throws IOException {
		Path sourceRoot = Path.of("src/main/java");
		Set<String> actual = new TreeSet<>();
		try (Stream<Path> paths = Files.walk(sourceRoot)) {
			paths.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> contains(path, marker))
					.map(sourceRoot::relativize)
					.map(Path::toString)
					.map(path -> path.replace('\\', '/'))
					.forEach(actual::add);
		}
		assertThat(actual)
				.as(description)
				.containsExactlyInAnyOrderElementsOf(expectedUsers);
	}

	private static boolean contains(Path path, String marker) {
		try {
			return Files.readString(path).contains(marker);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to inspect " + path, e);
		}
	}
}
