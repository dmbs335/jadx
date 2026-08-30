package jadx.core.dex.visitors.typeinference;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;

class TypeBoundConstTest {

	@Test
	void hashCodeMatchesObjectsHash() {
		ArgType[] types = { null, ArgType.INT, ArgType.STRING, ArgType.object("test.Type") };
		for (BoundEnum bound : BoundEnum.values()) {
			for (ArgType type : types) {
				assertThat(new TypeBoundConst(bound, type).hashCode())
						.isEqualTo(Objects.hash(bound, type));
			}
		}
	}
}
