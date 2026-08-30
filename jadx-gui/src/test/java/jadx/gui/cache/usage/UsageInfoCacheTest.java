package jadx.gui.cache.usage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.usage.UsageInfo;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class UsageInfoCacheTest extends IntegrationTest {

	@TempDir
	Path tempDir;

	@Test
	void persistsPendingDataBeforeCloseReturns() {
		disableCompilation();
		ClassNode cls = getClassNode(UsageInfoCacheTest.class);
		UsageInfoCache cache = new UsageInfoCache(tempDir, Collections.emptyList());
		cache.set(cls.root(), new UsageInfo(cls.root()));
		cache.persistAsync();
		cache.close();

		assertThat(tempDir.resolve("usage")).isRegularFile();
		assertThat(temporaryFiles()).isZero();
	}

	@Test
	void restoresMethodEdgesByClassMethodIndex() {
		disableCompilation();
		ClassNode cls = getClassNode(UsageInfoCacheTest.class);
		MethodNode caller = cls.searchMethodByShortId("caller()V");
		MethodNode target = cls.searchMethodByShortId("target()V");
		assertThat(caller).isNotNull();
		assertThat(target).isNotNull();

		UsageInfo usageInfo = new UsageInfo(cls.root());
		usageInfo.methodUse(caller, target);
		Path usageFile = tempDir.resolve("method-index-usage");
		UsageFileAdapter.save(cls.root(), usageInfo, usageFile, Collections.emptyList());

		RawUsageData rawUsage = UsageFileAdapter.load(cls.root(), usageFile, Collections.emptyList());
		assertThat(rawUsage).isNotNull();
		new UsageData(cls.root(), rawUsage).apply();
		assertThat(target.getUseIn()).contains(caller);
		assertThat(caller.getUsed()).contains(target);
	}

	private long temporaryFiles() {
		try (Stream<Path> files = Files.list(tempDir)) {
			return files.filter(path -> path.getFileName().toString().endsWith(".tmp")).count();
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private void caller() {
	}

	private void target() {
	}
}
