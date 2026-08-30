package jadx.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorsCounterTest {
	@Test
	void countsGlobalPrepareErrorsWithoutDexNode() {
		ErrorsCounter counter = new ErrorsCounter();

		counter.addGlobalError("Error in prepare pass init: KotlinMetadataPrepare",
				new IllegalArgumentException("malformed metadata"));

		assertThat(counter.getErrorCount()).isEqualTo(1);
		assertThat(counter.getGlobalErrors()).containsExactly(
				"Error in prepare pass init: KotlinMetadataPrepare: malformed metadata");
	}

	@Test
	void aggregatesAnalysisLossByCategory() {
		ErrorsCounter counter = new ErrorsCounter();
		counter.addAnalysisLoss("kotlin-metadata", "sample.First",
				new IllegalArgumentException("missing version"));
		counter.addAnalysisLoss("kotlin-metadata", "sample.Second",
				new IllegalArgumentException("missing version"));
		counter.addAnalysisLoss("kotlin-metadata", "sample.Second",
				new IllegalArgumentException("reported twice"));

		assertThat(counter.getErrorCount()).isEqualTo(1);
		assertThat(counter.getAnalysisLossCounts()).containsEntry("kotlin-metadata", 2);
		assertThat(counter.getAnalysisLossSamples().get("kotlin-metadata"))
				.containsExactly(
						"sample.First: missing version",
						"sample.Second: missing version");
	}

	@Test
	void auditedExclusionIsVisibleWithoutBecomingAnError() {
		ErrorsCounter counter = new ErrorsCounter();
		counter.addAnalysisExclusion("input-load.embedded-dex", "sha256=abc, entry=assets/bad.dex",
				new IllegalArgumentException("bad checksum"));
		counter.addAnalysisExclusion("input-load.embedded-dex", "sha256=abc, entry=assets/bad.dex",
				new IllegalArgumentException("duplicate"));

		assertThat(counter.getErrorCount()).isZero();
		assertThat(counter.getAnalysisExclusionCounts())
				.containsEntry("input-load.embedded-dex", 1);
		assertThat(counter.getAnalysisExclusionSamples().get("input-load.embedded-dex"))
				.containsExactly("sha256=abc, entry=assets/bad.dex: bad checksum");
	}
}
