package jadx.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.analysis.callgraph.JadxCallGraph;
import jadx.analysis.callgraph.api.ICallGraph;
import jadx.api.IOutputFileListener;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.impl.AnnotatedCodeWriter;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.cli.LogHelper.LogLevelEnum;
import jadx.cli.config.JadxConfigAdapter;
import jadx.cli.plugins.JadxFilesGetter;
import jadx.core.dex.visitors.SaveCode;
import jadx.core.utils.exceptions.JadxArgsValidateException;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.plugins.tools.JadxExternalPluginsLoader;
import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;
import jadx.storage.api.PruneStats;
import jadx.storage.api.SearchResult;
import jadx.storage.api.StoreStats;
import jadx.storage.impl.ParallelContentImporter;
import jadx.storage.impl.SqliteContentStore;
import jadx.storage.impl.WaveContentIngestSession;

public class JadxCLI {
	private static final Logger LOG = LoggerFactory.getLogger(JadxCLI.class);

	public static void main(String[] args) {
		int result = 1;
		try {
			result = execute(args);
		} finally {
			System.exit(result);
		}
	}

	public static int execute(String[] args) {
		return execute(args, null);
	}

	public static int execute(String[] args, @Nullable Consumer<JadxArgs> argsMod) {
		try {
			JadxCLIArgs cliArgs = JadxCLIArgs.processArgs(args,
					new JadxCLIArgs(),
					new JadxConfigAdapter<>(JadxCLIArgs.class, "cli"));
			if (cliArgs == null) {
				return 0;
			}
			if (cliArgs.hasContentStoreCommand()) {
				return runContentStoreCommand(cliArgs);
			}
			JadxArgs jadxArgs = buildArgs(cliArgs);
			if (argsMod != null) {
				argsMod.accept(jadxArgs);
			}
			return runSave(jadxArgs, cliArgs);
		} catch (JadxArgsValidateException e) {
			LOG.error("Incorrect arguments: {}", e.getMessage());
			return 1;
		} catch (Throwable e) {
			LOG.error("Process error:", e);
			return 1;
		}
	}

	private static int runContentStoreCommand(JadxCLIArgs cliArgs) {
		try (SqliteContentStore store = SqliteContentStore.open(Path.of(cliArgs.getContentStoreDir()))) {
			if (cliArgs.getContentStoreImportDir() != null) {
				int hashThreads = Math.max(1, Math.min(cliArgs.getThreadsCount(), 4));
				IngestStats stats = ParallelContentImporter.ingest(store, buildImportRequest(cliArgs), hashThreads);
				System.out.printf("run=%d files=%d newObjects=%d reusedObjects=%d reusedBytes=%d indexed=%d "
						+ "hardLinkFallbacks=%d hashThreads=%d elapsedMs=%d%n",
						stats.getRunId(), stats.getArtifactCount(), stats.getUniqueObjectCount(),
						stats.getReusedObjectCount(), stats.getDeduplicatedBytes(), stats.getIndexedObjectCount(),
						stats.getHardLinkFallbackCount(), hashThreads, stats.getElapsedMillis());
				return 0;
			}
			if (cliArgs.getContentStoreSearch() != null) {
				for (SearchResult result : store.search(cliArgs.getContentStoreSearch(), cliArgs.getContentStoreLimit())) {
					System.out.printf("%d\t%s\t%s\t%s\t%s%n",
							result.getApplicationId(), cleanCell(result.getApplicationName()), cleanCell(result.getPath()),
							result.getObjectHash(), cleanCell(result.getSnippet()));
				}
				return 0;
			}
			if (cliArgs.isContentStoreCompact()) {
				long maxPackBytes = Math.multiplyExact(cliArgs.getContentStorePackSizeMiB(), 1024L * 1024L);
				CompactionStats stats = store.compact(maxPackBytes);
				System.out.printf("objects=%d packedBytes=%d looseBytesDeleted=%d packGarbageReclaimed=%d "
						+ "packs=%d elapsedMs=%d%n",
						stats.getObjectCount(), stats.getPackedBytes(), stats.getLooseBytesDeleted(),
						stats.getPackGarbageBytesReclaimed(), stats.getPackCount(), stats.getElapsedMillis());
				return 0;
			}
			if (cliArgs.getContentStoreRetainRuns() > 0) {
				PruneStats stats = store.pruneRuns(cliArgs.getContentStoreRetainRuns());
				System.out.printf("runs=%d objects=%d looseBytesUnlinked=%d packedBytesPendingRepack=%d elapsedMs=%d%n",
						stats.getRunCount(), stats.getObjectCount(), stats.getLooseBytesUnlinked(),
						stats.getPackedBytesPendingRepack(), stats.getElapsedMillis());
				return 0;
			}
			if (cliArgs.getContentStoreMaterializeRun() > 0) {
				long count = store.materializeRun(
						cliArgs.getContentStoreMaterializeRun(), Path.of(cliArgs.getContentStoreMaterializeDir()));
				System.out.printf("run=%d materialized=%d target=%s%n",
						cliArgs.getContentStoreMaterializeRun(), count, cliArgs.getContentStoreMaterializeDir());
				return 0;
			}
			StoreStats stats = store.getStats();
			System.out.printf("applications=%d runs=%d objects=%d logicalBytes=%d packedObjects=%d packedBytes=%d%n",
					stats.getApplicationCount(), stats.getRunCount(), stats.getObjectCount(), stats.getLogicalBytes(),
					stats.getPackedObjectCount(), stats.getPackedBytes());
			return 0;
		} catch (Exception e) {
			throw new JadxRuntimeException("Content-store command failed", e);
		}
	}

	private static IngestRequest buildImportRequest(JadxCLIArgs cliArgs) {
		List<Path> inputPaths = cliArgs.getFiles().stream()
				.map(Path::of)
				.collect(Collectors.toList());
		String applicationName = inputPaths.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining("+"));
		MaterializationMode mode = cliArgs.isContentStoreHardLink()
				? MaterializationMode.HARD_LINK
				: MaterializationMode.KEEP;
		return new IngestRequest(
				applicationName,
				inputPaths,
				Path.of(cliArgs.getContentStoreImportDir()),
				"legacy-output-import-v1",
				mode,
				parseContentIndexMode(cliArgs.getContentStoreIndex()));
	}

	private static String cleanCell(String value) {
		return value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
	}

	private static JadxArgs buildArgs(JadxCLIArgs cliArgs) {
		JadxArgs jadxArgs = cliArgs.toJadxArgs();
		jadxArgs.setCodeCache(new NoOpCodeCache());
		jadxArgs.setUsageInfoCache(new EmptyUsageInfoCache());
		jadxArgs.setPluginLoader(new JadxExternalPluginsLoader());
		jadxArgs.setFilesGetter(JadxFilesGetter.INSTANCE);
		initCodeWriterProvider(jadxArgs);
		JadxAppCommon.applyEnvVars(jadxArgs);
		return jadxArgs;
	}

	private static int runSave(JadxArgs jadxArgs, JadxCLIArgs cliArgs) {
		DeferredOutputFileListener deferredListener = null;
		if (useStreamingContentStore(cliArgs)) {
			deferredListener = new DeferredOutputFileListener();
			jadxArgs.setOutputFileListener(deferredListener);
		}
		try (JadxDecompiler jadx = new JadxDecompiler(jadxArgs)) {
			jadx.load();
			if (checkForErrors(jadx)) {
				return 2;
			}
			try {
				try (StreamingContentStore streamingStore = startStreamingContentStore(jadx, cliArgs, deferredListener)) {
					writeCallGraph(jadx, cliArgs);
					if (!SingleClassMode.process(jadx, cliArgs)) {
						save(jadx);
					}
					if (streamingStore == null) {
						ingestContentStore(jadx, cliArgs);
					} else {
						logIngestStats(streamingStore.complete());
					}
				}
			} catch (IOException e) {
				throw new JadxRuntimeException("Streaming content-store ingest failed", e);
			}
			int errorsCount = jadx.getErrorsCount();
			if (errorsCount != 0) {
				jadx.printErrorsReport();
				LOG.error("finished with errors, count: {}", errorsCount);
				return 3;
			}
			LOG.info("done");
			return 0;
		} finally {
			jadxArgs.setOutputFileListener(IOutputFileListener.NONE);
		}
	}

	private static boolean useStreamingContentStore(JadxCLIArgs cliArgs) {
		String storeDir = cliArgs.getContentStoreDir();
		return storeDir != null && !storeDir.isEmpty() && cliArgs.isContentStoreHardLink();
	}

	private static @Nullable StreamingContentStore startStreamingContentStore(
			JadxDecompiler jadx, JadxCLIArgs cliArgs, @Nullable DeferredOutputFileListener deferredListener) {
		if (!useStreamingContentStore(cliArgs)) {
			return null;
		}
		try {
			SqliteContentStore store = SqliteContentStore.open(Path.of(cliArgs.getContentStoreDir()));
			IngestRequest request = buildIngestRequest(jadx, cliArgs, MaterializationMode.HARD_LINK);
			StreamingContentStore streamingStore;
			try {
				streamingStore = new StreamingContentStore(store, request);
			} catch (Exception e) {
				store.close();
				throw e;
			}
			try {
				if (deferredListener != null) {
					deferredListener.attach(streamingStore);
				}
			} catch (Exception e) {
				streamingStore.close();
				throw e;
			}
			return streamingStore;
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to start streaming content-store ingest", e);
		}
	}

	private static void ingestContentStore(JadxDecompiler jadx, JadxCLIArgs cliArgs) {
		String storeDir = cliArgs.getContentStoreDir();
		if (storeDir == null || storeDir.isEmpty()) {
			return;
		}
		MaterializationMode mode = cliArgs.isContentStoreHardLink()
				? MaterializationMode.HARD_LINK
				: MaterializationMode.KEEP;
		IngestRequest request = buildIngestRequest(jadx, cliArgs, mode);
		try (SqliteContentStore store = SqliteContentStore.open(Path.of(storeDir))) {
			logIngestStats(store.ingest(request));
		} catch (Exception e) {
			throw new JadxRuntimeException("Content-store ingest failed", e);
		}
	}

	private static IngestRequest buildIngestRequest(
			JadxDecompiler jadx, JadxCLIArgs cliArgs, MaterializationMode mode) {
		JadxArgs args = jadx.getArgs();
		List<Path> inputPaths = args.getAllInputFiles().stream()
				.map(File::toPath)
				.collect(Collectors.toList());
		String applicationName = args.getInputFiles().stream()
				.map(File::getName)
				.collect(Collectors.joining("+"));
		String analysisKey = jadx.getAnalysisFingerprint();
		ContentIndexMode indexMode = parseContentIndexMode(cliArgs.getContentStoreIndex());
		return new IngestRequest(
				applicationName, inputPaths, args.getOutDir().toPath(), analysisKey, mode, indexMode);
	}

	private static ContentIndexMode parseContentIndexMode(String value) {
		switch (value) {
			case "none":
				return ContentIndexMode.NONE;
			case "full-text":
				return ContentIndexMode.FULL_TEXT;
			default:
				throw new JadxRuntimeException("Unknown content-store index mode: " + value);
		}
	}

	private static void logIngestStats(IngestStats stats) {
		LOG.info("content store: run={}, files={}, new objects={}, reused={}, reused bytes={}, indexed={}, time={} ms",
					stats.getRunId(), stats.getArtifactCount(), stats.getUniqueObjectCount(), stats.getReusedObjectCount(),
					stats.getDeduplicatedBytes(), stats.getIndexedObjectCount(),
					stats.getElapsedMillis());
		if (stats.getHardLinkFallbackCount() != 0) {
			LOG.warn("content store: {} files could not be hard-linked and were retained normally",
						stats.getHardLinkFallbackCount());
		}
	}

	private static void initCodeWriterProvider(JadxArgs jadxArgs) {
		switch (jadxArgs.getOutputFormat()) {
			case JAVA:
				jadxArgs.setCodeWriterProvider(SimpleCodeWriter::new);
				break;
			case JSON:
				// needed for code offsets and source lines
				jadxArgs.setCodeWriterProvider(AnnotatedCodeWriter::new);
				break;
		}
	}

	private static boolean checkForErrors(JadxDecompiler jadx) {
		if (jadx.getRoot().getClasses().isEmpty()) {
			if (jadx.getArgs().isSkipResources()) {
				LOG.error("Load failed! No classes for decompile!");
				return true;
			}
			if (!jadx.getArgs().isSkipSources()) {
				LOG.warn("No classes to decompile; decoding resources only");
				jadx.getArgs().setSkipSources(true);
			}
		}
		int errorsCount = jadx.getErrorsCount();
		if (errorsCount > 0) {
			LOG.error("Loading finished with errors! Count: {}", errorsCount);
			// continue processing
		}
		return false;
	}

	private static void save(JadxDecompiler jadx) {
		if (LogHelper.getLogLevel() == LogLevelEnum.QUIET) {
			jadx.save();
		} else {
			LOG.info("processing ...");
			jadx.save(500, (done, total) -> {
				int progress = (int) (done * 100.0 / total);
				System.out.printf("INFO  - progress: %d of %d (%d%%)\r", done, total, progress);
			});
			// dumb line clear :)
			System.out.print("                                                             \r");
		}
	}

	private static void writeCallGraph(JadxDecompiler jadx, JadxCLIArgs cliArgs) {
		JadxCLIArgs.CallGraphSaveMode mode = cliArgs.callGraphSaveMode;
		if (mode == null || mode == JadxCLIArgs.CallGraphSaveMode.NONE) {
			return;
		}
		Path outPath = jadx.getArgs().getOutDir().toPath();
		ICallGraph callGraph = JadxCallGraph.builder(jadx)
				.resolvedOnly(true)
				.build();
		Path cgPath;
		switch (mode) {
			case JSON:
				cgPath = outPath.resolve("callgraph.json");
				callGraph.writeJson(cgPath);
				break;
			case DOT:
				cgPath = outPath.resolve("callgraph.dot");
				callGraph.writeDot(cgPath);
				break;
			default:
				throw new JadxRuntimeException("Unexpected call graph save mode: " + mode);
		}
		SaveCode.notifyFileSaved(jadx.getArgs(), cgPath.toFile());
		LOG.info("Call graph saved: {}", cgPath.toAbsolutePath());
	}

	private static final class DeferredOutputFileListener implements IOutputFileListener {
		private final Deque<OutputFile> pending = new ArrayDeque<>();
		private @Nullable IOutputFileListener delegate;

		@Override
		public synchronized void onFileSaved(Path path) throws Exception {
			onFileSaved(path, null, -1);
		}

		@Override
		public synchronized void onFileSaved(Path path, String contentHash, long size) throws Exception {
			if (delegate == null) {
				pending.addLast(new OutputFile(path, contentHash, size));
			} else if (contentHash == null) {
				delegate.onFileSaved(path);
			} else {
				delegate.onFileSaved(path, contentHash, size);
			}
		}

		@Override
		public boolean useContentMetadata() {
			return true;
		}

		@Override
		public boolean useWaveCheckpoints() {
			return true;
		}

		@Override
		public synchronized void onOutputCheckpoint() throws Exception {
			if (delegate != null) {
				delegate.onOutputCheckpoint();
			}
		}

		public synchronized void attach(IOutputFileListener listener) throws Exception {
			delegate = listener;
			while (!pending.isEmpty()) {
				OutputFile file = pending.removeFirst();
				if (file.contentHash == null) {
					listener.onFileSaved(file.path);
				} else {
					listener.onFileSaved(file.path, file.contentHash, file.size);
				}
			}
		}

		private static final class OutputFile {
			private final Path path;
			private final String contentHash;
			private final long size;

			private OutputFile(Path path, String contentHash, long size) {
				this.path = path;
				this.contentHash = contentHash;
				this.size = size;
			}
		}
	}

	private static final class StreamingContentStore implements IOutputFileListener, AutoCloseable {
		private final SqliteContentStore store;
		private final WaveContentIngestSession ingestSession;
		private final Path outputRoot;

		private StreamingContentStore(SqliteContentStore store, IngestRequest request) throws IOException {
			this.store = store;
			this.ingestSession = new WaveContentIngestSession(store.beginIngest(request));
			this.outputRoot = request.getOutputDirectory().toAbsolutePath().normalize();
		}

		@Override
		public void onFileSaved(Path path) throws IOException {
			onFileSaved(path, null, -1);
		}

		@Override
		public void onFileSaved(Path path, String contentHash, long size) throws IOException {
			Path normalized = path.toAbsolutePath().normalize();
			if (normalized.startsWith(outputRoot)) {
				if (contentHash == null) {
					ingestSession.submit(normalized);
				} else {
					ingestSession.submit(normalized, contentHash, size);
				}
			}
		}

		@Override
		public boolean useContentMetadata() {
			return true;
		}

		@Override
		public void onOutputCheckpoint() throws IOException {
			ingestSession.checkpoint();
		}

		private IngestStats complete() throws IOException {
			return ingestSession.complete();
		}

		@Override
		public void close() throws IOException {
			IOException failure = null;
			try {
				ingestSession.close();
			} catch (IOException e) {
				failure = e;
			}
			try {
				store.close();
			} catch (IOException e) {
				if (failure == null) {
					failure = e;
				} else {
					failure.addSuppressed(e);
				}
			}
			if (failure != null) {
				throw failure;
			}
		}
	}
}
