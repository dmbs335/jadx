package jadx.plugins.input.dex.sections.debuginfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.data.IDebugInfo;

import static org.assertj.core.api.Assertions.assertThat;

class DexDebugInfoTest {

	@Test
	void keepPrimitiveLinesAndLastValueForSameOffset() {
		DexDebugInfo.Builder builder = new DexDebugInfo.Builder();
		builder.put(2, 10);
		builder.put(2, 11);
		builder.put(4, 12);
		IDebugInfo debugInfo = builder.build(Collections.emptyList());

		List<String> lines = new ArrayList<>();
		debugInfo.forEachSourceLine((offset, line) -> lines.add(offset + "=" + line));

		assertThat(debugInfo.getSourceLineMappingSize()).isEqualTo(2);
		assertThat(lines).containsExactly("2=11", "4=12");
		assertThat(debugInfo.getSourceLineMapping()).containsEntry(2, 11).containsEntry(4, 12);
	}

	@Test
	void reflectLegacyMapChangesAfterMaterialization() {
		DexDebugInfo.Builder builder = new DexDebugInfo.Builder();
		builder.put(1, 20);
		IDebugInfo debugInfo = builder.build(Collections.emptyList());
		debugInfo.getSourceLineMapping().put(3, 30);

		List<String> lines = new ArrayList<>();
		debugInfo.forEachSourceLine((offset, line) -> lines.add(offset + "=" + line));

		assertThat(debugInfo.getSourceLineMappingSize()).isEqualTo(2);
		assertThat(lines).containsExactlyInAnyOrder("1=20", "3=30");
	}
}
