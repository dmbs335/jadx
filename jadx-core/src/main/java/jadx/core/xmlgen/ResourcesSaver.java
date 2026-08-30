package jadx.core.xmlgen;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxDecompiler;
import jadx.api.JadxArgs;
import jadx.api.ResourceFile;
import jadx.api.ResourcesLoader;
import jadx.api.security.IJadxSecurity;
import jadx.core.dex.visitors.SaveCode;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class ResourcesSaver implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(ResourcesSaver.class);

	private final ResourceFile resourceFile;
	private final File outDir;
	private final IJadxSecurity security;
	private final JadxArgs args;

	public ResourcesSaver(JadxDecompiler decompiler, File outDir, ResourceFile resourceFile) {
		this.resourceFile = resourceFile;
		this.outDir = outDir;
		this.args = decompiler.getArgs();
		this.security = args.getSecurity();
	}

	@Override
	public void run() {
		try {
			saveResources(resourceFile.loadContent());
		} catch (StackOverflowError | Exception e) {
			LOG.warn("Failed to save resource: {}", resourceFile.getOriginalName(), e);
		}
	}

	private void saveResources(ResContainer rc) {
		if (rc == null) {
			return;
		}
		if (rc.getDataType() == ResContainer.DataType.RES_TABLE) {
			saveToFile(rc, new File(outDir, "res/values/public.xml"));
			for (ResContainer subFile : rc.getSubFiles()) {
				saveResources(subFile);
			}
		} else {
			save(rc, outDir);
		}
	}

	private void save(ResContainer rc, File outDir) {
		String safeFileName = FileUtils.toSafeFilePath(rc.getFileName());
		File outFile = new File(outDir, safeFileName);
		if (!security.isInSubDirectory(outDir, outFile)) {
			LOG.error("Invalid resource name or path traversal attack detected: {}", outFile.getPath());
			return;
		}
		saveToFile(rc, outFile);
	}

	private void saveToFile(ResContainer rc, File outFile) {
		switch (rc.getDataType()) {
			case TEXT:
			case RES_TABLE:
				SaveCode.save(rc.getText(), outFile, args);
				return;

			case DECODED_DATA:
				byte[] data = rc.getDecodedData();
				FileUtils.makeDirsForFile(outFile);
				try {
					Files.write(outFile.toPath(), data);
					if (args.getOutputFileListener().useContentMetadata()) {
						MessageDigest digest = SaveCode.newSha256Digest();
						SaveCode.notifyFileSaved(args, outFile, SaveCode.toHex(digest.digest(data)), data.length);
					} else {
						SaveCode.notifyFileSaved(args, outFile);
					}
				} catch (Exception e) {
					LOG.warn("Resource '{}' not saved, got exception", rc.getName(), e);
				}
				return;

			case RES_LINK:
				ResourceFile resFile = rc.getResLink();
				FileUtils.makeDirsForFile(outFile);
				try {
					saveResourceFile(resFile, outFile);
				} catch (Exception e) {
					LOG.warn("Resource '{}' not saved, got exception", rc.getName(), e);
				}
				return;

			default:
				LOG.warn("Resource '{}' not saved, unknown type", rc.getName());
				break;
		}
	}

	private void saveResourceFile(ResourceFile resFile, File outFile) throws JadxException {
		ResourcesLoader.decodeStream(resFile, (size, is) -> {
			Path target = outFile.toPath();
			try {
				if (args.getOutputFileListener().useContentMetadata()) {
					MessageDigest digest = SaveCode.newSha256Digest();
					long written;
					try (DigestInputStream digestInput = new DigestInputStream(is, digest)) {
						written = Files.copy(digestInput, target, StandardCopyOption.REPLACE_EXISTING);
					}
					SaveCode.notifyFileSaved(args, outFile, SaveCode.toHex(digest.digest()), written);
				} else {
					Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
					SaveCode.notifyFileSaved(args, outFile);
				}
			} catch (Exception e) {
				Files.deleteIfExists(target); // delete partially written file
				throw new JadxRuntimeException("Resource file save error", e);
			}
			return null;
		});
	}
}
