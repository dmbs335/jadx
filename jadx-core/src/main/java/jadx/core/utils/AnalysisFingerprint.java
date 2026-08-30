package jadx.core.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.core.Jadx;
import jadx.core.plugins.PluginContext;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

/**
 * Builds the correctness boundary for reusable semantic analysis results.
 *
 * <p>
 * Content identity and result reuse are intentionally separate concepts. Equal generated blobs
 * can share CAS storage, but a semantic result is reusable only when this fingerprint is equal.
 * </p>
 */
public final class AnalysisFingerprint {
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisFingerprint.class);

	private static final int SCHEMA_VERSION = 2;
	private static final String PREFIX = "af" + SCHEMA_VERSION + ':';

	private AnalysisFingerprint() {
	}

	public static String build(JadxArgs args, @Nullable JadxDecompiler decompiler) {
		return build(args, decompiler, buildInputIdentity(args));
	}

	public static InputIdentity buildInputIdentity(JadxArgs args) {
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.openDefault()) {
			InputIdentity identity = new InputIdentity(
					buildInputsHash(args.getInputFiles(), hashIndex),
					buildInputsHash(args.getDependencyInputFiles(), hashIndex));
			LOG.debug("Analysis input hash index: hits={}, misses={}",
					hashIndex.getHitCount(), hashIndex.getMissCount());
			return identity;
		}
	}

	public static String build(
			JadxArgs args,
			@Nullable JadxDecompiler decompiler,
			InputIdentity inputIdentity) {
		try (AnalysisHashIndex hashIndex = AnalysisHashIndex.openDefault()) {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			add(digest, "schema", Integer.toString(SCHEMA_VERSION));
			add(digest, "jadx-version", Jadx.getVersion());
			add(digest, "code-args", args.makeCodeArgsHash(decompiler));
			add(digest, "primary-inputs", inputIdentity.primaryInputsHash);
			add(digest, "dependency-inputs", inputIdentity.dependencyInputsHash);
			addOptionalFile(digest, "user-mappings", args.getUserRenamesMappingsPath(), hashIndex);
			if (args.getGeneratedRenamesMappingFileMode().shouldRead()) {
				File mappingsFile = args.getGeneratedRenamesMappingFile();
				addOptionalFile(digest, "generated-mappings",
						mappingsFile == null ? null : mappingsFile.toPath(), hashIndex);
			}
			add(digest, "runtime", buildRuntimeHash(decompiler, hashIndex));
			String fingerprint = PREFIX + FileUtils.bytesToHex(digest.digest());
			LOG.debug("Analysis content hash index: hits={}, misses={}",
					hashIndex.getHitCount(), hashIndex.getMissCount());
			return fingerprint;
		} catch (NoSuchAlgorithmException e) {
			throw new JadxRuntimeException("SHA-256 is unavailable", e);
		}
	}

	public static final class InputIdentity {
		private final String primaryInputsHash;
		private final String dependencyInputsHash;

		private InputIdentity(String primaryInputsHash, String dependencyInputsHash) {
			this.primaryInputsHash = primaryInputsHash;
			this.dependencyInputsHash = dependencyInputsHash;
		}
	}

	private static String buildRuntimeHash(
			@Nullable JadxDecompiler decompiler, AnalysisHashIndex hashIndex) {
		Set<Path> runtimePaths = new LinkedHashSet<>();
		addCodeSource(runtimePaths, Jadx.class);
		addCodeSource(runtimePaths, JadxDecompiler.class);
		if (decompiler != null) {
			for (PluginContext plugin : decompiler.getPluginManager().getResolvedPluginContexts()) {
				addCodeSource(runtimePaths, plugin.getPluginInstance().getClass());
			}
		}
		if (runtimePaths.isEmpty()) {
			return "unknown";
		}
		return FileUtils.buildInputsContentHash(new ArrayList<>(runtimePaths), hashIndex::hash);
	}

	static String buildRuntimeHash(List<Path> runtimePaths) {
		return FileUtils.buildInputsContentHash(runtimePaths);
	}

	private static void addCodeSource(Set<Path> runtimePaths, Class<?> cls) {
		try {
			if (cls.getProtectionDomain() != null
					&& cls.getProtectionDomain().getCodeSource() != null
					&& cls.getProtectionDomain().getCodeSource().getLocation() != null) {
				runtimePaths.add(Paths.get(cls.getProtectionDomain().getCodeSource().getLocation().toURI()));
			}
		} catch (SecurityException | URISyntaxException e) {
			LOG.debug("Failed to resolve code source for analysis fingerprint: {}", cls.getName(), e);
		}
	}

	private static String buildInputsHash(List<File> files, AnalysisHashIndex hashIndex) {
		return FileUtils.buildInputsContentHash(toPaths(files), hashIndex::hash);
	}

	private static void addOptionalFile(
			MessageDigest digest, String name, @Nullable Path file, AnalysisHashIndex hashIndex) {
		if (file == null) {
			add(digest, name, "none");
			return;
		}
		Path normalized = file.toAbsolutePath().normalize();
		if (!Files.isRegularFile(normalized)) {
			add(digest, name, "missing:" + normalized);
			return;
		}
		add(digest, name, FileUtils.buildInputsContentHash(List.of(normalized), hashIndex::hash));
	}

	private static List<Path> toPaths(List<File> files) {
		List<Path> paths = new ArrayList<>(files.size());
		for (File file : files) {
			paths.add(file.toPath());
		}
		return paths;
	}

	private static void add(MessageDigest digest, String name, String value) {
		update(digest, name.getBytes(StandardCharsets.UTF_8));
		update(digest, value.getBytes(StandardCharsets.UTF_8));
	}

	private static void update(MessageDigest digest, byte[] bytes) {
		int length = bytes.length;
		digest.update((byte) (length >>> 24));
		digest.update((byte) (length >>> 16));
		digest.update((byte) (length >>> 8));
		digest.update((byte) length);
		digest.update(bytes);
	}
}
