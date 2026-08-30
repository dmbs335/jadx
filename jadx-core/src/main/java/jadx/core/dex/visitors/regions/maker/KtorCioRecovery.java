package jadx.core.dex.visitors.regions.maker;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;

/**
 * Centralizes structural Ktor CIO state-machine recognition used by narrowly-scoped decompiler
 * recovery.
 *
 * Generated continuation class names are deliberately excluded: Kotlin can renumber or rename
 * them between otherwise compatible library builds. Captured protocol fields and their stable API
 * types form the boundary, while each consumer proves the exact CFG it intends to rewrite.
 */
final class KtorCioRecovery {
	private static final Set<String> READ_FIELDS =
			Set.of("label", "$channel", "$nioChannel", "$selectable", "$selector");
	private static final String BYTE_CHANNEL = "io.ktor.utils.io.ByteChannel";
	private static final String TIMEOUT = "io.ktor.network.util.Timeout";

	private KtorCioRecovery() {
	}

	static boolean isReadStateMachine(MethodNode mth) {
		return matchesReadShape(
				CoroutineMethodUtils.isStateMachine(mth),
				mth.getParentClass().getFields().stream()
						.map(field -> field.getName())
						.collect(Collectors.toSet()));
	}

	static boolean isDirectReadStateMachine(MethodNode mth) {
		return matchesDirectReadShape(
				CoroutineMethodUtils.isStateMachine(mth),
				mth.getParentClass().getFields().stream()
						.collect(Collectors.toMap(
								field -> field.getName(),
								field -> field.getType().toString(),
								(first, ignored) -> first)));
	}

	static boolean matchesReadShape(boolean stateMachine, Set<String> fieldNames) {
		return stateMachine && fieldNames.containsAll(READ_FIELDS);
	}

	static boolean matchesDirectReadShape(boolean stateMachine, Map<String, String> fieldTypes) {
		return stateMachine
				&& "int".equals(fieldTypes.get("label"))
				&& BYTE_CHANNEL.equals(fieldTypes.get("$channel"))
				&& TIMEOUT.equals(fieldTypes.get("$timeout"));
	}
}
