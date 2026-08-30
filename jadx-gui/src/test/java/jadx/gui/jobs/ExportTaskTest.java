package jadx.gui.jobs;

import java.io.File;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.core.export.ExportGradleType;

import static org.assertj.core.api.Assertions.assertThat;

class ExportTaskTest {
	@Test
	void exportSettingsCanBeRestoredAfterCancelOrFailure() {
		JadxArgs args = new JadxArgs();
		args.setOutDir(new File("original-out"));
		args.setOutDirSrc(new File("original-src"));
		args.setOutDirRes(new File("original-res"));
		args.setSkipSources(true);
		args.setSkipResources(false);
		args.setExportGradleType(ExportGradleType.AUTO);
		ExportTask.ExportArgsState state = ExportTask.ExportArgsState.capture(args);

		args.setRootDir(new File("export"));
		args.setSkipSources(false);
		args.setSkipResources(true);
		args.setExportGradleType(null);
		state.restore(args);

		assertThat(args.getOutDir()).isEqualTo(new File("original-out"));
		assertThat(args.getOutDirSrc()).isEqualTo(new File("original-src"));
		assertThat(args.getOutDirRes()).isEqualTo(new File("original-res"));
		assertThat(args.isSkipSources()).isTrue();
		assertThat(args.isSkipResources()).isFalse();
		assertThat(args.getExportGradleType()).isEqualTo(ExportGradleType.AUTO);
	}
}
