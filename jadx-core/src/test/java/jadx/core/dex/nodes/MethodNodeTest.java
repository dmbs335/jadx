package jadx.core.dex.nodes;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;

class MethodNodeTest {
	@Test
	void registerFrameMustCoverInstanceAndWideArguments() {
		List<ArgType> args = List.of(ArgType.OBJECT, ArgType.LONG);

		assertThat(MethodNode.getRequiredRegistersCount(2, args, false)).isEqualTo(6);
		assertThat(MethodNode.getRequiredRegistersCount(2, args, true)).isEqualTo(5);
	}
}
