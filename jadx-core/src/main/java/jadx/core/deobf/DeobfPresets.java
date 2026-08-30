package jadx.core.deobf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxArgs;
import jadx.api.args.GeneratedRenamesMappingFileMode;
import jadx.api.deobf.IAliasProvider;
import jadx.api.deobf.impl.AlwaysRename;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.PackageNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.files.FileUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DeobfPresets {
	private static final Logger LOG = LoggerFactory.getLogger(DeobfPresets.class);

	private static final Charset MAP_FILE_CHARSET = UTF_8;

	private final Path deobfMapFile;

	private final Map<String, String> pkgPresetMap = new HashMap<>();
	private final Map<String, String> clsPresetMap = new HashMap<>();
	private final Map<String, String> fldPresetMap = new HashMap<>();
	private final Map<String, String> mthPresetMap = new HashMap<>();
	private final Map<String, List<String>> pkgDuplicateMap = new HashMap<>();
	private final Map<String, List<String>> clsDuplicateMap = new HashMap<>();
	private final Map<String, List<String>> fldDuplicateMap = new HashMap<>();
	private final Map<String, List<String>> mthDuplicateMap = new HashMap<>();

	public static DeobfPresets build(RootNode root) {
		Path deobfMapPath = getPathDeobfMapPath(root);
		if (root.getArgs().getGeneratedRenamesMappingFileMode() != GeneratedRenamesMappingFileMode.IGNORE) {
			LOG.debug("Deobfuscation map file set to: {}", deobfMapPath);
		}
		return new DeobfPresets(deobfMapPath);
	}

	private static Path getPathDeobfMapPath(RootNode root) {
		JadxArgs jadxArgs = root.getArgs();
		File deobfMapFile = jadxArgs.getGeneratedRenamesMappingFile();
		if (deobfMapFile != null) {
			return deobfMapFile.toPath();
		}
		Path inputFilePath = jadxArgs.getInputFiles().get(0).toPath().toAbsolutePath();
		String baseName = FileUtils.getPathBaseName(inputFilePath);
		return inputFilePath.getParent().resolve(baseName + ".jobf");
	}

	private DeobfPresets(Path deobfMapFile) {
		this.deobfMapFile = deobfMapFile;
	}

	/**
	 * Loads deobfuscator presets
	 */
	public boolean load() {
		if (!Files.exists(deobfMapFile)) {
			return false;
		}
		LOG.info("Loading obfuscation map from: {}", deobfMapFile.toAbsolutePath());
		try {
			try (BufferedReader reader = Files.newBufferedReader(deobfMapFile, MAP_FILE_CHARSET)) {
				String l;
				while ((l = reader.readLine()) != null) {
					l = l.trim();
					if (l.isEmpty() || l.startsWith("#")) {
						continue;
					}
					String[] va = splitAndTrim(l);
					if (va.length != 2) {
						continue;
					}
					String origName = va[0];
					String alias = va[1];
					switch (l.charAt(0)) {
						case 'p':
							addPreset(pkgPresetMap, pkgDuplicateMap, origName, alias);
							break;
						case 'c':
							addPreset(clsPresetMap, clsDuplicateMap, origName, alias);
							break;
						case 'f':
							addPreset(fldPresetMap, fldDuplicateMap, origName, alias);
							break;
						case 'm':
							addPreset(mthPresetMap, mthDuplicateMap, origName, alias);
							break;
						case 'v':
							// deprecated
							break;
					}
				}
			}
			return true;
		} catch (Exception e) {
			LOG.error("Failed to load deobfuscation map file '{}'", deobfMapFile.toAbsolutePath(), e);
			return false;
		}
	}

	private static String[] splitAndTrim(String str) {
		String[] v = str.substring(2).split("=");
		for (int i = 0; i < v.length; i++) {
			v[i] = v[i].trim();
		}
		return v;
	}

	public void save() throws IOException {
		List<String> list = new ArrayList<>();
		appendMappings(list, 'p', pkgPresetMap, pkgDuplicateMap);
		appendMappings(list, 'c', clsPresetMap, clsDuplicateMap);
		appendMappings(list, 'f', fldPresetMap, fldDuplicateMap);
		appendMappings(list, 'm', mthPresetMap, mthDuplicateMap);
		// Stable key-only sorting keeps aliases for duplicate raw ids in occurrence order.
		list.sort((first, second) -> mappingKey(first).compareTo(mappingKey(second)));
		if (list.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Deobfuscation map is empty, not saving it");
			}
			return;
		}
		Files.write(deobfMapFile, list, MAP_FILE_CHARSET,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		LOG.info("Deobfuscation map file saved as: {}", deobfMapFile);
	}

	public void fill(RootNode root) {
		Set<String> aliasedPackages = new HashSet<>();
		Set<String> aliasedClasses = new HashSet<>();
		Set<String> aliasedFields = new HashSet<>();
		Set<String> aliasedMethods = new HashSet<>();
		for (PackageNode pkg : root.getPackages()) {
			if (pkg.hasAlias() || pkg.hasParentAlias()) {
				aliasedPackages.add(pkg.getPkgInfo().getFullName());
			}
		}
		for (ClassNode cls : root.getClasses()) {
			ClassInfo classInfo = cls.getClassInfo();
			if (classInfo.hasAlias()) {
				aliasedClasses.add(classInfo.makeRawFullName());
			}
			for (FieldNode fld : cls.getFields()) {
				FieldInfo fieldInfo = fld.getFieldInfo();
				if (fieldInfo.hasAlias()) {
					aliasedFields.add(fieldInfo.getRawFullId());
				}
			}
			for (MethodNode mth : cls.getMethods()) {
				MethodInfo methodInfo = mth.getMethodInfo();
				if (methodInfo.hasAlias()) {
					aliasedMethods.add(methodInfo.getRawFullId());
				}
			}
		}

		// Preserve occurrence positions for duplicate raw ids. An unaliased node can precede
		// an aliased node with the same id, so omitting it shifts all following aliases on read.
		for (PackageNode pkg : root.getPackages()) {
			String rawName = pkg.getPkgInfo().getFullName();
			if (!aliasedPackages.contains(rawName)) {
				continue;
			}
			if (pkg.hasParentAlias()) {
				addPreset(pkgPresetMap, pkgDuplicateMap,
						rawName, pkg.getAliasPkgInfo().getFullName());
			} else {
				addPreset(pkgPresetMap, pkgDuplicateMap,
						rawName, pkg.getAliasPkgInfo().getName());
			}
		}
		for (ClassNode cls : root.getClasses()) {
			ClassInfo classInfo = cls.getClassInfo();
			String rawClass = classInfo.makeRawFullName();
			if (aliasedClasses.contains(rawClass)) {
				addPreset(clsPresetMap, clsDuplicateMap, rawClass, classInfo.getAliasShortName());
			}
			for (FieldNode fld : cls.getFields()) {
				FieldInfo fieldInfo = fld.getFieldInfo();
				String rawField = fieldInfo.getRawFullId();
				if (aliasedFields.contains(rawField)) {
					addPreset(fldPresetMap, fldDuplicateMap, rawField, fld.getAlias());
				}
			}
			for (MethodNode mth : cls.getMethods()) {
				MethodInfo methodInfo = mth.getMethodInfo();
				String rawMethod = methodInfo.getRawFullId();
				if (aliasedMethods.contains(rawMethod)) {
					addPreset(mthPresetMap, mthDuplicateMap, rawMethod, methodInfo.getAlias());
				}
			}
		}
	}

	public void apply(RootNode root) {
		apply(root, null);
	}

	public void apply(RootNode root, ReapplyData reapplyData) {
		Map<String, Integer> pkgOccurrences = new HashMap<>();
		Map<String, Integer> clsOccurrences = new HashMap<>();
		Map<String, Integer> fldOccurrences = new HashMap<>();
		Map<String, Integer> mthOccurrences = new HashMap<>();
		DeobfuscatorVisitor.process(root,
				AlwaysRename.INSTANCE,
				new IAliasProvider() {
					@Override
					public String forPackage(PackageNode pkg) {
						String alias = getNext(pkgPresetMap, pkgDuplicateMap,
								pkg.getPkgInfo().getFullName(), pkgOccurrences);
						if (alias != null && reapplyData != null) {
							reapplyData.add(pkg, alias);
						}
						return alias;
					}

					@Override
					public String forClass(ClassNode cls) {
						String alias = getNext(clsPresetMap, clsDuplicateMap,
								cls.getClassInfo().makeRawFullName(), clsOccurrences);
						if (alias != null && reapplyData != null) {
							reapplyData.add(cls, alias);
						}
						return alias;
					}

					@Override
					public String forField(FieldNode fld) {
						String alias = getNext(fldPresetMap, fldDuplicateMap,
								fld.getFieldInfo().getRawFullId(), fldOccurrences);
						if (alias != null && reapplyData != null) {
							reapplyData.add(fld, alias);
						}
						return alias;
					}

					@Override
					public String forMethod(MethodNode mth) {
						// Apply method presets below without override-group propagation.
						return null;
					}
				});
		for (ClassNode cls : root.getClasses()) {
			for (MethodNode mth : cls.getMethods()) {
				String alias = getNext(mthPresetMap, mthDuplicateMap,
						mth.getMethodInfo().getRawFullId(), mthOccurrences);
				if (alias != null) {
					mth.getMethodInfo().setAlias(alias);
					if (reapplyData != null) {
						reapplyData.add(mth, alias);
					}
				}
			}
		}
	}

	public void initIndexes(IAliasProvider aliasProvider) {
		aliasProvider.initIndexes(
				countValues(pkgPresetMap, pkgDuplicateMap),
				countValues(clsPresetMap, clsDuplicateMap),
				countValues(fldPresetMap, fldDuplicateMap),
				countValues(mthPresetMap, mthDuplicateMap));
	}

	public String getForCls(ClassInfo cls) {
		if (clsPresetMap.isEmpty()) {
			return null;
		}
		return clsPresetMap.get(cls.makeRawFullName());
	}

	public String getForFld(FieldInfo fld) {
		if (fldPresetMap.isEmpty()) {
			return null;
		}
		return fldPresetMap.get(fld.getRawFullId());
	}

	public String getForMth(MethodInfo mth) {
		if (mthPresetMap.isEmpty()) {
			return null;
		}
		return mthPresetMap.get(mth.getRawFullId());
	}

	private static void addPreset(Map<String, String> values, Map<String, List<String>> duplicates,
			String key, String alias) {
		String previous = values.putIfAbsent(key, alias);
		if (previous != null) {
			duplicates.computeIfAbsent(key, unused -> new ArrayList<>(1)).add(alias);
		}
	}

	private static void appendMappings(List<String> output, char type,
			Map<String, String> values, Map<String, List<String>> duplicates) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			String key = entry.getKey();
			output.add(formatMapping(type, key, entry.getValue()));
			List<String> additional = duplicates.get(key);
			if (additional != null) {
				for (String alias : additional) {
					output.add(formatMapping(type, key, alias));
				}
			}
		}
	}

	private static String formatMapping(char type, String key, String alias) {
		return String.format("%s %s = %s", type, key, alias);
	}

	private static String mappingKey(String line) {
		int separator = line.indexOf(" = ");
		return separator == -1 ? line : line.substring(0, separator);
	}

	private static int countValues(Map<String, String> values, Map<String, List<String>> duplicates) {
		return values.size() + duplicates.values().stream().mapToInt(List::size).sum();
	}

	private static String getNext(Map<String, String> values, Map<String, List<String>> duplicates,
			String key, Map<String, Integer> occurrences) {
		String first = values.get(key);
		if (first == null) {
			return null;
		}
		int occurrence = occurrences.getOrDefault(key, 0);
		occurrences.put(key, occurrence + 1);
		if (occurrence == 0) {
			return first;
		}
		List<String> additional = duplicates.get(key);
		if (additional == null || occurrence > additional.size()) {
			return null;
		}
		return additional.get(occurrence - 1);
	}

	public void clear() {
		pkgPresetMap.clear();
		clsPresetMap.clear();
		fldPresetMap.clear();
		mthPresetMap.clear();
		pkgDuplicateMap.clear();
		clsDuplicateMap.clear();
		fldDuplicateMap.clear();
		mthDuplicateMap.clear();
	}

	public static final class ReapplyData {
		private final List<PackageNode> packages = new ArrayList<>();
		private final List<String> packageAliases = new ArrayList<>();
		private final List<ClassNode> classes = new ArrayList<>();
		private final List<String> classAliases = new ArrayList<>();
		private final List<FieldNode> fields = new ArrayList<>();
		private final List<String> fieldAliases = new ArrayList<>();
		private final List<MethodNode> methods = new ArrayList<>();
		private final List<String> methodAliases = new ArrayList<>();

		private void add(PackageNode pkg, String alias) {
			packages.add(pkg);
			packageAliases.add(alias);
		}

		private void add(ClassNode cls, String alias) {
			classes.add(cls);
			classAliases.add(alias);
		}

		private void add(FieldNode fld, String alias) {
			fields.add(fld);
			fieldAliases.add(alias);
		}

		private void add(MethodNode mth, String alias) {
			methods.add(mth);
			methodAliases.add(alias);
		}

		public void reapply(RootNode root) {
			if (!packages.isEmpty()) {
				for (int i = 0; i < packages.size(); i++) {
					packages.get(i).rename(packageAliases.get(i), false);
				}
				root.runPackagesUpdate();
			}
			for (int i = 0; i < classes.size(); i++) {
				classes.get(i).rename(classAliases.get(i));
			}
			for (int i = 0; i < fields.size(); i++) {
				fields.get(i).getFieldInfo().setAlias(fieldAliases.get(i));
			}
			for (int i = 0; i < methods.size(); i++) {
				methods.get(i).getMethodInfo().setAlias(methodAliases.get(i));
			}
			clear();
		}

		private void clear() {
			packages.clear();
			packageAliases.clear();
			classes.clear();
			classAliases.clear();
			fields.clear();
			fieldAliases.clear();
			methods.clear();
			methodAliases.clear();
		}
	}

	public Path getDeobfMapFile() {
		return deobfMapFile;
	}

	public Map<String, String> getPkgPresetMap() {
		return pkgPresetMap;
	}

	public Map<String, String> getClsPresetMap() {
		return clsPresetMap;
	}

	public Map<String, String> getFldPresetMap() {
		return fldPresetMap;
	}

	public Map<String, String> getMthPresetMap() {
		return mthPresetMap;
	}
}
