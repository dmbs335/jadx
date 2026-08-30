package jadx.tests.integration.others;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;
import jadx.tests.integration.others.cfg.CfgDependency;
import jadx.tests.integration.others.cfg.CfgTarget;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCfgOutputFilter extends IntegrationTest {

	@Test
	public void testDependencyGraphsAreExcluded() throws IOException {
		disableCompilation();
		args.setCfgOutput(true);
		args.setRawCFGOutput(true);
		args.setCfgOutputFilter(CfgTarget.class.getName()::equals);

		getClassNodes(CfgTarget.class, CfgDependency.class);

		List<String> graphPaths;
		try (Stream<Path> stream = Files.walk(args.getOutDir().toPath())) {
			graphPaths = stream.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".dot"))
					.map(Path::toString)
					.toList();
		}
		assertThat(graphPaths)
				.isNotEmpty()
				.allMatch(path -> path.contains("CfgTarget_graphs"))
				.noneMatch(path -> path.contains("CfgDependency_graphs"));
	}
}
