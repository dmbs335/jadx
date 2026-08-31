package jadx.gui.utils.cache.code;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeInfo;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.gui.cache.code.CodeStringCache;
import jadx.gui.cache.code.disk.BufferCodeCache;
import jadx.gui.cache.code.disk.DiskCodeCache;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiskCodeCacheTest extends IntegrationTest {
	private static final Logger LOG = LoggerFactory.getLogger(DiskCodeCacheTest.class);

	@TempDir
	public Path tempDir;

	@Test
	public void test() throws IOException {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		ICodeInfo codeInfo = clsNode.getCode();

		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);
		String codeVersion = Files.readString(tempDir.resolve("code").resolve("code-version"));
		assertThat(codeVersion).startsWith("18:af2:");

		String clsKey = clsNode.getFullName();
		cache.add(clsKey, codeInfo);

		ICodeInfo readCodeInfo = cache.get(clsKey);

		assertThat(readCodeInfo).isNotNull();
		assertThat(readCodeInfo.getCodeStr()).isEqualTo(codeInfo.getCodeStr());
		assertThat(readCodeInfo.getCodeMetadata().getLineMapping()).isEqualTo(codeInfo.getCodeMetadata().getLineMapping());
		LOG.info("Disk code annotations: {}", readCodeInfo.getCodeMetadata().getAsMap());
		assertThat(readCodeInfo.getCodeMetadata().getAsMap()).hasSameSizeAs(codeInfo.getCodeMetadata().getAsMap());

		cache.close();
	}

	@Test
	public void testCorruptedEntryIsRegenerated() throws IOException {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		ICodeInfo expected = clsNode.getCode();
		String clsKey = clsNode.getFullName();

		DiskCodeCache initialCache = new DiskCodeCache(clsNode.root(), tempDir);
		initialCache.add(clsKey, expected);
		initialCache.close();

		Path entryFile = findCacheFile(tempDir.resolve("code/entries"), ".jadxbc");
		Files.delete(entryFile);
		DiskCodeCache missingSourceCache = new DiskCodeCache(clsNode.root(), tempDir);
		getArgs().setCodeCache(missingSourceCache);
		assertThat(clsNode.getCode().getCodeStr()).isEqualTo(expected.getCodeStr());
		missingSourceCache.close();
		Path regeneratedEntry = findCacheFile(tempDir.resolve("code/entries"), ".jadxbc");
		DiskCodeCache regeneratedCache = new DiskCodeCache(clsNode.root(), tempDir);
		assertThat(regeneratedCache.getCode(clsKey)).isEqualTo(expected.getCodeStr());
		regeneratedCache.close();

		Files.write(regeneratedEntry, new byte[] { 'b', 'a', 'd' });
		DiskCodeCache corruptMetadataCache = new DiskCodeCache(clsNode.root(), tempDir);
		getArgs().setCodeCache(corruptMetadataCache);
		assertThat(clsNode.getCode().getCodeStr()).isEqualTo(expected.getCodeStr());
		corruptMetadataCache.close();

		DiskCodeCache verifiedCache = new DiskCodeCache(clsNode.root(), tempDir);
		assertThat(verifiedCache.get(clsKey).getCodeStr()).isEqualTo(expected.getCodeStr());
		verifiedCache.close();
	}

	@Test
	public void testMetadataLoadReusesKnownCodeString() throws IOException {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		ICodeInfo expected = clsNode.getCode();
		String clsKey = clsNode.getFullName();

		DiskCodeCache initialCache = new DiskCodeCache(clsNode.root(), tempDir);
		initialCache.add(clsKey, expected);
		initialCache.close();

		CodeStringCache cache = new CodeStringCache(
				new BufferCodeCache(new DiskCodeCache(clsNode.root(), tempDir)));
		String knownCode = cache.getCode(clsKey);
		assertThat(knownCode).isEqualTo(expected.getCodeStr());

		ICodeInfo readCodeInfo = cache.get(clsKey);
		assertThat(readCodeInfo.getCodeStr()).isSameAs(knownCode);
		assertThat(readCodeInfo.getCodeMetadata().getLineMapping())
				.isEqualTo(expected.getCodeMetadata().getLineMapping());
		assertThat(readCodeInfo.getCodeMetadata().getAsMap())
				.hasSameSizeAs(expected.getCodeMetadata().getAsMap());
		cache.close();
	}

	@Test
	public void interruptedCloseStillWaitsForWriters() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);
		ExecutorService writePool = getWritePool(cache);
		CountDownLatch writerStarted = new CountDownLatch(1);
		CountDownLatch releaseWriter = new CountDownLatch(1);
		writePool.execute(() -> {
			writerStarted.countDown();
			try {
				releaseWriter.await(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		assertThat(writerStarted.await(1, TimeUnit.SECONDS)).isTrue();
		Thread releaser = new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			releaseWriter.countDown();
		});
		releaser.start();

		try {
			Thread.currentThread().interrupt();
			cache.close();
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
			assertThat(writePool.isTerminated()).isTrue();
		} finally {
			Thread.interrupted();
			releaseWriter.countDown();
			releaser.join(1_000);
		}
	}

	@Test
	public void latestSameClassWriteWins() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		getArgs().setThreadsCount(1);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		String clsKey = clsNode.getFullName();
		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);

		CountDownLatch firstWriteStarted = new CountDownLatch(1);
		CountDownLatch releaseFirstWrite = new CountDownLatch(1);
		cache.add(clsKey, new BlockingCodeInfo("old", firstWriteStarted, releaseFirstWrite));
		assertThat(firstWriteStarted.await(1, TimeUnit.SECONDS)).isTrue();
		cache.add(clsKey, new SimpleCodeInfo("new"));
		releaseFirstWrite.countDown();
		cache.close();

		DiskCodeCache reopened = new DiskCodeCache(clsNode.root(), tempDir);
		assertThat(reopened.getCode(clsKey)).isEqualTo("new");
		reopened.close();
		try (Stream<Path> stagingFiles = Files.list(tempDir.resolve("code-staging"))) {
			assertThat(stagingFiles.filter(Files::isRegularFile)).isEmpty();
		}
	}

	@Test
	public void removeCannotDeleteANewerGeneration() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		getArgs().setThreadsCount(1);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		String clsKey = clsNode.getFullName();
		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);

		CountDownLatch oldWriteStarted = new CountDownLatch(1);
		CountDownLatch releaseOldWrite = new CountDownLatch(1);
		cache.add(clsKey, new BlockingCodeInfo("removed", oldWriteStarted, releaseOldWrite));
		assertThat(oldWriteStarted.await(1, TimeUnit.SECONDS)).isTrue();
		cache.remove(clsKey);
		cache.add(clsKey, new SimpleCodeInfo("replacement"));
		releaseOldWrite.countDown();
		cache.close();

		DiskCodeCache reopened = new DiskCodeCache(clsNode.root(), tempDir);
		assertThat(reopened.getCode(clsKey)).isEqualTo("replacement");
		reopened.close();
	}

	@Test
	public void rejectedWriteRestoresThePreviousCacheState() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		String clsKey = clsNode.getFullName();
		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);
		cache.close();

		assertThatThrownBy(() -> cache.add(clsKey, new SimpleCodeInfo("rejected")))
				.isInstanceOf(RejectedExecutionException.class);
		assertThat(cache.contains(clsKey)).isFalse();
		assertThat(cache.getCode(clsKey)).isNull();
	}

	@Test
	public void publishesCodeAndMetadataThroughOneAtomicEntry() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		String clsKey = clsNode.getFullName();
		DiskCodeCache cache = new DiskCodeCache(clsNode.root(), tempDir);
		cache.add(clsKey, clsNode.getCode());
		cache.close();

		Path entry = findCacheFile(tempDir.resolve("code/entries"), ".jadxbc");
		assertThat(entry).isRegularFile();
		try (Stream<Path> files = Files.walk(tempDir.resolve("code/entries"))) {
			assertThat(files.filter(Files::isRegularFile)).hasSize(1);
		}
	}

	@Test
	public void concurrentCacheInstancesObserveThePublishedGeneration() throws Exception {
		disableCompilation();
		getArgs().setCodeCache(NoOpCodeCache.INSTANCE);
		ClassNode clsNode = getClassNode(DiskCodeCacheTest.class);
		String clsKey = clsNode.getFullName();
		DiskCodeCache first = new DiskCodeCache(clsNode.root(), tempDir);
		DiskCodeCache second = new DiskCodeCache(clsNode.root(), tempDir);

		first.add(clsKey, new SimpleCodeInfo("first"));
		first.close();
		assertThat(second.getCode(clsKey)).isEqualTo("first");

		second.add(clsKey, new SimpleCodeInfo("second"));
		second.close();
		assertThat(first.getCode(clsKey)).isEqualTo("second");
		Path entry = findCacheFile(tempDir.resolve("code/entries"), ".jadxbc");
		try (Stream<Path> entries = Files.list(entry.getParent())) {
			assertThat(entries.filter(Files::isRegularFile)).hasSize(1);
		}
	}

	private static ExecutorService getWritePool(DiskCodeCache cache) throws Exception {
		Field field = DiskCodeCache.class.getDeclaredField("writePool");
		field.setAccessible(true);
		return (ExecutorService) field.get(cache);
	}

	private static final class BlockingCodeInfo extends SimpleCodeInfo {
		private final CountDownLatch started;
		private final CountDownLatch release;

		private BlockingCodeInfo(String code, CountDownLatch started, CountDownLatch release) {
			super(code);
			this.started = started;
			this.release = release;
		}

		@Override
		public String getCodeStr() {
			started.countDown();
			try {
				if (!release.await(2, TimeUnit.SECONDS)) {
					throw new IllegalStateException("Timed out waiting to release cache writer");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting to release cache writer", e);
			}
			return super.getCodeStr();
		}
	}

	private static Path findCacheFile(Path dir, String suffix) throws IOException {
		try (Stream<Path> files = Files.walk(dir)) {
			return files.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(suffix))
					.findFirst()
					.orElseThrow();
		}
	}
}
