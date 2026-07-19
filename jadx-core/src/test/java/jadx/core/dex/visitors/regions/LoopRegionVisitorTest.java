package jadx.core.dex.visitors.regions;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.args.ArgType;

import static org.assertj.core.api.Assertions.assertThat;

class LoopRegionVisitorTest {

	@Test
	void matchesGenericAndRawTypesByObjectName() {
		ArgType genericEntry = ArgType.generic("java.util.Map$Entry", ArgType.STRING, ArgType.INT);
		ArgType rawEntry = ArgType.object("java.util.Map$Entry");

		assertThat(LoopRegionVisitor.isSameRawObjectType(genericEntry, rawEntry)).isTrue();
		assertThat(LoopRegionVisitor.isSameRawObjectType(genericEntry, ArgType.object("java.lang.ref.WeakReference"))).isFalse();
	}
}
