package jadx.gui.settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.gui.settings.data.ProjectData;

import static org.assertj.core.api.Assertions.assertThat;

class JadxProjectTest {
	@TempDir
	Path tempDir;

	@Test
	void projectSaveReplacesFileAndCleansTemporaryFile() throws Exception {
		Path projectFile = tempDir.resolve("sample.jadx");
		Files.writeString(projectFile, "old-content");
		ProjectData data = new ProjectData();
		data.setSearchHistory(List.of("needle"));

		JadxProject.saveProjectData(data, projectFile);

		assertThat(JadxProject.loadProjectData(projectFile).getSearchHistory()).containsExactly("needle");
		try (var files = Files.list(tempDir)) {
			assertThat(files).containsExactly(projectFile);
		}
	}
}
