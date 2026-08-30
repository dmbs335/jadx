package jadx.api;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.utils.exceptions.JadxArgsValidateException;

public class JadxArgsValidator {

	private static final Logger LOG = LoggerFactory.getLogger(JadxArgsValidator.class);

	public static void validate(JadxDecompiler jadx) {
		JadxArgs args = jadx.getArgs();
		checkInputFiles(jadx, args);
		validateOutDirs(args);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Effective jadx args: {}", args);
		}
	}

	private static void checkInputFiles(JadxDecompiler jadx, JadxArgs args) {
		List<File> inputFiles = args.getInputFiles();
		if (inputFiles.isEmpty() && jadx.getCustomCodeLoaders().isEmpty()) {
			throw new JadxArgsValidateException("Please specify input file");
		}
		for (File file : args.getAllInputFiles()) {
			checkFile(file);
		}
		Set<String> primaryInputs = inputFiles.stream()
				.map(JadxArgsValidator::normalizedPath)
				.collect(Collectors.toSet());
		Set<String> dependencyInputs = new HashSet<>();
		for (File dependencyInput : args.getDependencyInputFiles()) {
			if (!dependencyInput.getName().toLowerCase(Locale.ROOT).endsWith(".dex")) {
				throw new JadxArgsValidateException(
						"Dependency input currently supports standalone .dex files only: "
								+ dependencyInput.getAbsolutePath());
			}
			String dependencyPath = normalizedPath(dependencyInput);
			if (!dependencyInputs.add(dependencyPath)) {
				throw new JadxArgsValidateException("Duplicate dependency input: " + dependencyInput.getAbsolutePath());
			}
			if (primaryInputs.contains(dependencyPath)) {
				throw new JadxArgsValidateException(
						"Input file can't also be a dependency: " + dependencyInput.getAbsolutePath());
			}
		}
	}

	private static String normalizedPath(File file) {
		return file.toPath().toAbsolutePath().normalize().toString();
	}

	private static void validateOutDirs(JadxArgs args) {
		File outDir = args.getOutDir();
		File srcDir = args.getOutDirSrc();
		File resDir = args.getOutDirRes();
		if (outDir == null) {
			if (srcDir != null) {
				outDir = srcDir;
			} else if (resDir != null) {
				outDir = resDir;
			} else {
				outDir = makeDirFromInput(args);
			}
			args.setOutDir(outDir);
		}
		if (srcDir == null) {
			args.setOutDirSrc(new File(args.getOutDir(), JadxArgs.DEFAULT_SRC_DIR));
		}
		if (resDir == null) {
			args.setOutDirRes(new File(args.getOutDir(), JadxArgs.DEFAULT_RES_DIR));
		}

		checkDir(args.getOutDir(), "Output");
		checkDir(args.getOutDirSrc(), "Source output");
		checkDir(args.getOutDirRes(), "Resources output");
	}

	@NotNull
	private static File makeDirFromInput(JadxArgs args) {
		String outDirName;
		List<File> inputFiles = args.getInputFiles();
		if (inputFiles.isEmpty()) {
			outDirName = JadxArgs.DEFAULT_OUT_DIR;
		} else {
			File file = inputFiles.get(0);
			String name = file.getName();
			int pos = name.lastIndexOf('.');
			if (pos != -1) {
				outDirName = name.substring(0, pos);
			} else {
				outDirName = name + '-' + JadxArgs.DEFAULT_OUT_DIR;
			}
		}
		LOG.info("output directory: {}", outDirName);
		return new File(outDirName);
	}

	private static void checkFile(File file) {
		if (!file.exists()) {
			throw new JadxArgsValidateException("File not found " + file.getAbsolutePath());
		}
	}

	private static void checkDir(File dir, String desc) {
		if (dir != null && dir.exists() && !dir.isDirectory()) {
			throw new JadxArgsValidateException(desc + " directory exists as file " + dir);
		}
	}

	private JadxArgsValidator() {
	}
}
