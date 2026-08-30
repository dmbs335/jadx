package jadx.core.dex.visitors.regions.maker;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KtorCioRecoveryTest {
	@Test
	void recognizesPooledReaderByCapturedProtocolInsteadOfGeneratedClassName() {
		assertThat(KtorCioRecovery.matchesReadShape(true,
				Set.of("label", "$channel", "$nioChannel", "$selectable", "$selector")))
				.isTrue();
	}

	@Test
	void rejectsIncompletePooledReaderShape() {
		assertThat(KtorCioRecovery.matchesReadShape(true,
				Set.of("label", "$channel", "$nioChannel", "$selector")))
				.isFalse();
		assertThat(KtorCioRecovery.matchesReadShape(false,
				Set.of("label", "$channel", "$nioChannel", "$selectable", "$selector")))
				.isFalse();
	}

	@Test
	void recognizesDirectReaderByStableCapturedApiTypes() {
		Map<String, String> fields = Map.of(
				"label", "int",
				"$channel", "io.ktor.utils.io.ByteChannel",
				"$timeout", "io.ktor.network.util.Timeout");
		assertThat(KtorCioRecovery.matchesDirectReadShape(true, fields)).isTrue();
		assertThat(KtorCioRecovery.matchesDirectReadShape(false, fields)).isFalse();
		assertThat(KtorCioRecovery.matchesDirectReadShape(true, Map.of(
				"label", "int",
				"$channel", "java.lang.Object",
				"$timeout", "io.ktor.network.util.Timeout")))
				.isFalse();
	}
}
