package jadx.core.xmlgen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jadx.core.dex.visitors.SaveCode;
import jadx.core.utils.files.FileUtils;

/**
 * Allocates resource output paths without relying on host file-system case or Unicode rules.
 *
 * <p>
 * Android archives can contain names which are distinct in ZIP but equivalent on Windows or
 * default macOS file systems. All {@link ResourcesSaver} tasks for one output share this registry,
 * so a parallel save cannot silently replace another resource. Known paths are reserved in a
 * stable order; late decoded resource-table entries are still protected under the same lock.
 * </p>
 */
public final class ResourceOutputRegistry {
	private final Map<String, String> outputBySource = new HashMap<>();
	private final Map<String, String> sourceByOutputKey = new HashMap<>();
	private final Set<String> allocatedFileKeys = new HashSet<>();
	private final Set<String> requiredDirectoryKeys = new HashSet<>();

	public ResourceOutputRegistry(Collection<String> knownPaths) {
		List<String> sorted = new ArrayList<>(knownPaths);
		// Reserve descendants first so a later file which shadows their directory is renamed.
		sorted.sort(Comparator.comparingInt(ResourceOutputRegistry::pathDepth)
				.reversed()
				.thenComparing(Comparator.naturalOrder()));
		for (String path : sorted) {
			resolve(path);
		}
	}

	synchronized String resolve(String archivePath) {
		String sourceKey = normalizeArchivePath(archivePath);
		String existing = outputBySource.get(sourceKey);
		if (existing != null) {
			return existing;
		}
		String safePath = FileUtils.toSafeFilePath(sourceKey);
		String candidate = safePath;
		int attempt = 0;
		while (conflicts(candidate, sourceKey)) {
			candidate = addStableSuffix(safePath, sourceKey, attempt++);
		}
		outputBySource.put(sourceKey, candidate);
		String outputKey = collisionKey(candidate);
		sourceByOutputKey.put(outputKey, sourceKey);
		allocatedFileKeys.add(outputKey);
		reserveParentDirectories(outputKey);
		return candidate;
	}

	private boolean conflicts(String candidate, String source) {
		String key = collisionKey(candidate);
		String exactSource = sourceByOutputKey.get(key);
		if (exactSource != null && !exactSource.equals(source)) {
			return true;
		}
		if (requiredDirectoryKeys.contains(key)) {
			return true;
		}
		for (int slash = key.indexOf('/'); slash != -1; slash = key.indexOf('/', slash + 1)) {
			if (allocatedFileKeys.contains(key.substring(0, slash))) {
				return true;
			}
		}
		return false;
	}

	private void reserveParentDirectories(String outputKey) {
		for (int slash = outputKey.indexOf('/'); slash != -1; slash = outputKey.indexOf('/', slash + 1)) {
			requiredDirectoryKeys.add(outputKey.substring(0, slash));
		}
	}

	private static String normalizeArchivePath(String path) {
		return path.replace('\\', '/');
	}

	private static String addStableSuffix(String safePath, String source, int attempt) {
		String suffix = "~" + stableHash(source, attempt);
		int slash = safePath.lastIndexOf('/');
		int dot = safePath.lastIndexOf('.');
		if (dot <= slash) {
			return safePath + suffix;
		}
		return safePath.substring(0, dot) + suffix + safePath.substring(dot);
	}

	private static String stableHash(String source, int attempt) {
		MessageDigest digest = SaveCode.newSha256Digest();
		digest.update(source.getBytes(StandardCharsets.UTF_8));
		if (attempt != 0) {
			digest.update((byte) 0);
			digest.update(Integer.toString(attempt).getBytes(StandardCharsets.US_ASCII));
		}
		return SaveCode.toHex(digest.digest()).substring(0, 12);
	}

	private static String collisionKey(String path) {
		return Normalizer.normalize(path, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
	}

	private static int pathDepth(String path) {
		int depth = 0;
		for (int i = 0; i < path.length(); i++) {
			char ch = path.charAt(i);
			if (ch == '/' || ch == '\\') {
				depth++;
			}
		}
		return depth;
	}
}
