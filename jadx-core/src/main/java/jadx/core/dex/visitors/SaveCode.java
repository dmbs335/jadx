package jadx.core.dex.visitors;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class SaveCode {
	private static final Logger LOG = LoggerFactory.getLogger(SaveCode.class);

	private SaveCode() {
	}

	public static void save(File dir, ClassNode cls, ICodeInfo code) {
		if (cls.contains(AFlag.DONT_GENERATE)) {
			return;
		}
		if (code == null) {
			throw new JadxRuntimeException("Code not generated for class " + cls.getFullName());
		}
		if (code == ICodeInfo.EMPTY) {
			return;
		}
		String codeStr = code.getCodeStr();
		if (codeStr.isEmpty()) {
			return;
		}
		JadxArgs args = cls.root().getArgs();
		if (args.isSkipFilesSave()) {
			return;
		}
		String fileName = cls.getClassInfo().getAliasFullPath() + getFileExtension(cls.root());
		if (!args.getSecurity().isValidEntryName(fileName)) {
			return;
		}
		save(codeStr, new File(dir, fileName), args);
	}

	public static void save(ICodeInfo codeInfo, File file) {
		save(codeInfo.getCodeStr(), file);
	}

	public static void save(ICodeInfo codeInfo, File file, JadxArgs args) {
		save(codeInfo.getCodeStr(), file, args);
	}

	public static void save(String code, File file) {
		save(code, file, null);
	}

	public static void save(String code, File file, JadxArgs args) {
		File outFile = FileUtils.prepareFile(file);
		boolean useContentMetadata = args != null && args.getOutputFileListener().useContentMetadata();
		MessageDigest digest = useContentMetadata ? newSha256Digest() : null;
		try (OutputStream fileOut = Files.newOutputStream(outFile.toPath());
				PrintWriter out = new PrintWriter(
						digest == null ? fileOut : new DigestOutputStream(fileOut, digest),
						false, StandardCharsets.UTF_8)) {
			out.println(code);
			if (out.checkError()) {
				throw new JadxRuntimeException("Failed to write output file: " + outFile);
			}
		} catch (Exception e) {
			LOG.error("Save file error", e);
			return;
		}
		if (args != null) {
			if (digest == null) {
				notifyFileSaved(args, outFile);
			} else {
				notifyFileSaved(args, outFile, toHex(digest.digest()), outFile.length());
			}
		}
	}

	public static void notifyFileSaved(JadxArgs args, File file) {
		try {
			args.getOutputFileListener().onFileSaved(file.toPath());
		} catch (Exception e) {
			throw new JadxRuntimeException("Output file listener failed for: " + file, e);
		}
	}

	public static void notifyFileSaved(JadxArgs args, File file, String contentHash, long size) {
		try {
			args.getOutputFileListener().onFileSaved(file.toPath(), contentHash, size);
		} catch (Exception e) {
			throw new JadxRuntimeException("Output file listener failed for: " + file, e);
		}
	}

	public static MessageDigest newSha256Digest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	public static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			result.append(Character.forDigit((value >>> 4) & 0xF, 16));
			result.append(Character.forDigit(value & 0xF, 16));
		}
		return result.toString();
	}

	public static void notifyOutputCheckpoint(JadxArgs args) {
		try {
			args.getOutputFileListener().onOutputCheckpoint();
		} catch (Exception e) {
			throw new JadxRuntimeException("Output file checkpoint failed", e);
		}
	}

	public static String getFileExtension(RootNode root) {
		JadxArgs.OutputFormatEnum outputFormat = root.getArgs().getOutputFormat();
		switch (outputFormat) {
			case JAVA:
				return ".java";

			case JSON:
				return ".json";

			default:
				throw new JadxRuntimeException("Unknown output format: " + outputFormat);
		}
	}
}
