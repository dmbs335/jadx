package jadx.gui.cache.code.disk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jadx.api.ICodeInfo;
import jadx.api.impl.AnnotatedCodeInfo;
import jadx.api.impl.SimpleCodeInfo;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeMetadata;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.files.FileUtils;
import jadx.gui.cache.code.disk.adapters.CodeAnnotationAdapter;
import jadx.gui.cache.code.disk.adapters.DataAdapterHelper;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class CodeMetadataAdapter {
	private static final byte[] JADX_METADATA_HEADER = "jadxmd".getBytes(StandardCharsets.US_ASCII);
	private static final int MAX_METADATA_ENTRIES = 1_000_000;
	private static final int INITIAL_MAP_CAPACITY = 1_024;

	private final CodeAnnotationAdapter codeAnnotationAdapter;

	public CodeMetadataAdapter(RootNode root) {
		codeAnnotationAdapter = new CodeAnnotationAdapter(root);
	}

	public void write(Path metadataFile, ICodeMetadata metadata) {
		FileUtils.makeDirsForFile(metadataFile);
		try (OutputStream fileOutput = Files.newOutputStream(metadataFile, WRITE, CREATE, TRUNCATE_EXISTING);
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fileOutput))) {
			out.write(JADX_METADATA_HEADER);
			writeLines(out, metadata.getLineMapping());
			writeAnnotations(out, metadata.getAsMap());
		} catch (Exception e) {
			throw new RuntimeException("Failed to write metadata file", e);
		}
	}

	public ICodeInfo readAndBuild(Path metadataFile, String code) {
		if (!Files.exists(metadataFile)) {
			return new SimpleCodeInfo(code);
		}
		try (InputStream fileInput = Files.newInputStream(metadataFile);
				DataInputStream in = new DataInputStream(new BufferedInputStream(fileInput))) {
			byte[] header = new byte[JADX_METADATA_HEADER.length];
			in.readFully(header);
			if (!Arrays.equals(header, JADX_METADATA_HEADER)) {
				throw new IOException("Invalid metadata header");
			}
			long fileSize = Files.size(metadataFile);
			int entriesLimit = (int) Math.min(MAX_METADATA_ENTRIES,
					Math.min((long) code.length() + 1, fileSize));
			Map<Integer, Integer> lines = readLines(in, entriesLimit, code.length());
			Map<Integer, ICodeAnnotation> annotations = readAnnotations(in, entriesLimit, code.length());
			return new AnnotatedCodeInfo(code, lines, annotations);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse code annotations", e);
		}
	}

	private void writeLines(DataOutput out, Map<Integer, Integer> lines) throws IOException {
		out.writeInt(lines.size());
		for (Map.Entry<Integer, Integer> entry : lines.entrySet()) {
			DataAdapterHelper.writeUVInt(out, entry.getKey());
			DataAdapterHelper.writeUVInt(out, entry.getValue());
		}
	}

	private Map<Integer, Integer> readLines(DataInput in, int entriesLimit, int codeLength) throws IOException {
		int size = readSize(in, "line mappings", entriesLimit);
		if (size == 0) {
			return Collections.emptyMap();
		}
		Map<Integer, Integer> lines = new HashMap<>(Math.min(size, INITIAL_MAP_CAPACITY));
		for (int i = 0; i < size; i++) {
			int key = DataAdapterHelper.readUVInt(in);
			int value = DataAdapterHelper.readUVInt(in);
			checkCodePosition(key, codeLength, "line mapping key");
			lines.put(key, value);
		}
		return lines;
	}

	private void writeAnnotations(DataOutputStream out, Map<Integer, ICodeAnnotation> annotations) throws IOException {
		out.writeInt(annotations.size());
		for (Map.Entry<Integer, ICodeAnnotation> entry : annotations.entrySet()) {
			DataAdapterHelper.writeUVInt(out, entry.getKey());
			codeAnnotationAdapter.write(out, entry.getValue());
		}
	}

	private Map<Integer, ICodeAnnotation> readAnnotations(DataInputStream in, int entriesLimit, int codeLength) throws IOException {
		int size = readSize(in, "annotations", entriesLimit);
		if (size == 0) {
			return Collections.emptyMap();
		}
		Map<Integer, ICodeAnnotation> map = new HashMap<>(Math.min(size, INITIAL_MAP_CAPACITY));
		for (int i = 0; i < size; i++) {
			int pos = DataAdapterHelper.readUVInt(in);
			checkCodePosition(pos, codeLength, "annotation position");
			ICodeAnnotation ann = codeAnnotationAdapter.read(in);
			if (ann != null) {
				map.put(pos, ann);
			}
		}
		return map;
	}

	private static int readSize(DataInput in, String section, int entriesLimit) throws IOException {
		int size = in.readInt();
		if (size < 0 || size > entriesLimit) {
			throw new IOException("Invalid " + section + " count: " + size + ", limit: " + entriesLimit);
		}
		return size;
	}

	private static void checkCodePosition(int pos, int codeLength, String name) throws IOException {
		if (pos < 0 || pos > codeLength) {
			throw new IOException("Invalid " + name + ": " + pos + ", code length: " + codeLength);
		}
	}
}
