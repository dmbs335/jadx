package jadx.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.analysis.callgraph.JadxCallGraph;
import jadx.analysis.callgraph.api.ICallGraph;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.impl.AnnotatedCodeWriter;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.cli.LogHelper.LogLevelEnum;
import jadx.cli.config.JadxConfigAdapter;
import jadx.cli.plugins.JadxFilesGetter;
import jadx.core.utils.exceptions.JadxArgsValidateException;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.plugins.tools.JadxExternalPluginsLoader;
import jadx.storage.api.CompactionStats;
import jadx.storage.api.ContentIndexMode;
import jadx.storage.api.IngestRequest;
import jadx.storage.api.IngestStats;
import jadx.storage.api.MaterializationMode;
import jadx.storage.api.SearchResult;
import jadx.storage.api.SecurityFactResult;
import jadx.storage.api.StoreStats;
import jadx.storage.impl.SqliteContentStore;

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
			if (cliArgs.getContentStoreSearch() != null) {
				for (SearchResult result : store.search(cliArgs.getContentStoreSearch(), cliArgs.getContentStoreLimit())) {
					System.out.printf("%d\t%s\t%s\t%s\t%s%n",
							result.getApplicationId(), cleanCell(result.getApplicationName()), cleanCell(result.getPath()),
							result.getObjectHash(), cleanCell(result.getSnippet()));
				}
				return 0;
			}
			if (cliArgs.getContentStoreFacts() != null) {
				for (SecurityFactResult result : store.findSecurityFacts(
						cliArgs.getContentStoreAppId(), cliArgs.getContentStoreFacts(), cliArgs.getContentStoreLimit())) {
					System.out.printf("%d\t%s\t%s\t%s\t%d\t%s%n",
							result.getApplicationId(), cleanCell(result.getPath()), result.getObjectHash(),
							result.getFact().getKind(), result.getFact().getLine(), cleanCell(result.getFact().getValue()));
				}
				return 0;
			}
			if (cliArgs.isContentStoreCompact()) {
				long maxPackBytes = Math.multiplyExact(cliArgs.getContentStorePackSizeMiB(), 1024L * 1024L);
				CompactionStats stats = store.compact(maxPackBytes);
				System.out.printf("objects=%d packedBytes=%d looseBytesDeleted=%d packs=%d elapsedMs=%d%n",
						stats.getObjectCount(), stats.getPackedBytes(), stats.getLooseBytesDeleted(),
						stats.getPackCount(), stats.getElapsedMillis());
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
		try (JadxDecompiler jadx = new JadxDecompiler(jadxArgs)) {
			jadx.load();
			if (checkForErrors(jadx)) {
				return 2;
			}
			writeCallGraph(jadx, cliArgs);
			if (!SingleClassMode.process(jadx, cliArgs)) {
				save(jadx);
			}
			ingestContentStore(jadx, cliArgs);
			int errorsCount = jadx.getErrorsCount();
			if (errorsCount != 0) {
				jadx.printErrorsReport();
				LOG.error("finished with errors, count: {}", errorsCount);
				return 3;
			}
			LOG.info("done");
			return 0;
		}
	}

	private static void ingestContentStore(JadxDecompiler jadx, JadxCLIArgs cliArgs) {
		String storeDir = cliArgs.getContentStoreDir();
		if (storeDir == null || storeDir.isEmpty()) {
			return;
		}
		JadxArgs args = jadx.getArgs();
		List<Path> inputPaths = args.getAllInputFiles().stream()
				.map(File::toPath)
				.collect(Collectors.toList());
		String applicationName = args.getInputFiles().stream()
				.map(File::getName)
				.collect(Collectors.joining("+"));
		String analysisKey = String.join("|",
				JadxDecompiler.getVersion(),
				args.getOutputFormat().name(),
				args.getDecompilationMode().name(),
				args.getCommentsLevel().name(),
				args.getRenameFlags().toString());
		MaterializationMode mode = cliArgs.isContentStoreHardLink()
				? MaterializationMode.HARD_LINK
				: MaterializationMode.KEEP;
		ContentIndexMode indexMode;
		switch (cliArgs.getContentStoreIndex()) {
			case "none":
				indexMode = ContentIndexMode.NONE;
				break;
			case "security":
				indexMode = ContentIndexMode.SECURITY;
				break;
			case "full-text":
				indexMode = ContentIndexMode.FULL_TEXT;
				break;
			default:
				throw new JadxRuntimeException("Unknown content-store index mode: " + cliArgs.getContentStoreIndex());
		}
		IngestRequest request = new IngestRequest(
				applicationName, inputPaths, args.getOutDir().toPath(), analysisKey, mode, indexMode);
		try (SqliteContentStore store = SqliteContentStore.open(Path.of(storeDir))) {
			IngestStats stats = store.ingest(request);
			LOG.info("content store: run={}, files={}, new objects={}, reused={}, reused bytes={}, indexed={}, facts={}, time={} ms",
					stats.getRunId(), stats.getArtifactCount(), stats.getUniqueObjectCount(), stats.getReusedObjectCount(),
					stats.getDeduplicatedBytes(), stats.getIndexedObjectCount(), stats.getSecurityFactCount(),
					stats.getElapsedMillis());
			if (stats.getHardLinkFallbackCount() != 0) {
				LOG.warn("content store: {} files could not be hard-linked and were retained normally",
						stats.getHardLinkFallbackCount());
			}
		} catch (Exception e) {
			throw new JadxRuntimeException("Content-store ingest failed", e);
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
		LOG.info("Call graph saved: {}", cgPath.toAbsolutePath());
	}
}
