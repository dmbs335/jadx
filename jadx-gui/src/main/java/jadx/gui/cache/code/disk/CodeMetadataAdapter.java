package jadx.gui.cache.code.disk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jadx.api.ICodeInfo;
import jadx.api.impl.AnnotatedCodeInfo;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeMetadata;
import jadx.core.dex.nodes.RootNode;
import jadx.gui.cache.code.disk.adapters.CodeAnnotationAdapter;
import jadx.gui.cache.code.disk.adapters.DataAdapterHelper;

public class CodeMetadataAdapter {
	private static final byte[] JADX_BUNDLE_HEADER = "jadxcb1".getBytes(StandardCharsets.US_ASCII);
	private static final int MAX_METADATA_ENTRIES = 1_000_000;
	private static final int INITIAL_MAP_CAPACITY = 1_024;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private static final int MAX_PREALLOCATED_METADATA = 1 << 20;

	private final CodeAnnotationAdapter codeAnnotationAdapter;

	public CodeMetadataAdapter(RootNode root) {
		codeAnnotationAdapter = new CodeAnnotationAdapter(root);
	}

	public byte[] writeBundle(ICodeInfo codeInfo) {
		byte[] code = codeInfo.getCodeStr().getBytes(StandardCharsets.UTF_8);
		ICodeMetadata metadata = codeInfo.getCodeMetadata();
		int initialCapacity = estimateBundleSize(code.length, metadata);
		try (ByteArrayOutputStream bytes = new FastByteArrayOutputStream(initialCapacity);
				DataOutputStream out = new DataOutputStream(bytes)) {
			writeBundle(out, code, metadata);
			out.flush();
			return bytes.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to write code cache bundle", e);
		}
	}

	private void writeBundle(DataOutputStream out, byte[] code, ICodeMetadata metadata) throws IOException {
		out.write(JADX_BUNDLE_HEADER);
		out.writeInt(code.length);
		out.write(code);
		writeLines(out, metadata.getLineMapping());
		writeAnnotations(out, metadata.getAsMap());
	}

	public String readCode(byte[] bundle) {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bundle))) {
			int codeSize = readCodeSize(in, bundle.length, "SQLite bundle");
			return new String(readBytes(in, codeSize), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("Failed to read code cache bundle", e);
		}
	}

	public ICodeInfo readAndBuild(byte[] bundle, String knownCode) {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bundle))) {
			int codeSize = readCodeSize(in, bundle.length, "SQLite bundle");
			String code;
			if (knownCode == null) {
				code = new String(readBytes(in, codeSize), StandardCharsets.UTF_8);
			} else {
				in.skipNBytes(codeSize);
				code = knownCode;
			}
			int entriesLimit = Math.min(MAX_METADATA_ENTRIES, Math.min(code.length() + 1, bundle.length));
			Map<Integer, Integer> lines = readLines(in, entriesLimit, code.length());
			Map<Integer, ICodeAnnotation> annotations = readAnnotations(in, entriesLimit, code.length());
			return new AnnotatedCodeInfo(code, lines, annotations);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse code cache bundle", e);
		}
	}

	private static int readCodeSize(DataInputStream in, long bundleSize, String source) throws IOException {
		byte[] header = new byte[JADX_BUNDLE_HEADER.length];
		in.readFully(header);
		if (!Arrays.equals(header, JADX_BUNDLE_HEADER)) {
			throw new IOException("Invalid code cache bundle header: " + source);
		}
		int codeSize = in.readInt();
		long maxCodeSize = bundleSize - JADX_BUNDLE_HEADER.length - Integer.BYTES;
		if (codeSize < 0 || codeSize > maxCodeSize) {
			throw new IOException("Invalid code size: " + codeSize + ", limit: " + maxCodeSize);
		}
		return codeSize;
	}

	private static byte[] readBytes(DataInputStream in, int size) throws IOException {
		byte[] bytes = new byte[size];
		in.readFully(bytes);
		return bytes;
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

	private void writeAnnotations(DataOutput out, Map<Integer, ICodeAnnotation> annotations) throws IOException {
		out.writeInt(annotations.size());
		for (Map.Entry<Integer, ICodeAnnotation> entry : annotations.entrySet()) {
			DataAdapterHelper.writeUVInt(out, entry.getKey());
			codeAnnotationAdapter.write(out, entry.getValue());
		}
	}

	private Map<Integer, ICodeAnnotation> readAnnotations(DataInput in, int entriesLimit, int codeLength) throws IOException {
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

	private static int estimateBundleSize(int codeSize, ICodeMetadata metadata) {
		long metadataSize = 32L
				+ metadata.getLineMapping().size() * 4L
				+ metadata.getAsMap().size() * 16L;
		long estimate = codeSize + Math.min(metadataSize, MAX_PREALLOCATED_METADATA);
		return (int) Math.min(estimate, MAX_ARRAY_SIZE);
	}

	/**
	 * {@link ByteArrayOutputStream} synchronizes every write. Cache bundles are thread-confined,
	 * so those locks only add CPU and scheduler traffic while writing varints byte by byte.
	 */
	private static final class FastByteArrayOutputStream extends ByteArrayOutputStream {
		private FastByteArrayOutputStream(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public void write(int value) {
			ensureCapacity(count + 1L);
			buf[count++] = (byte) value;
		}

		@Override
		public void write(byte[] bytes, int offset, int length) {
			Objects.checkFromIndexSize(offset, length, bytes.length);
			if (length == 0) {
				return;
			}
			ensureCapacity((long) count + length);
			System.arraycopy(bytes, offset, buf, count, length);
			count += length;
		}

		private void ensureCapacity(long minCapacity) {
			if (minCapacity <= buf.length) {
				return;
			}
			if (minCapacity > MAX_ARRAY_SIZE) {
				throw new OutOfMemoryError("Required array size too large");
			}
			int oldCapacity = buf.length;
			int grownCapacity = oldCapacity <= MAX_ARRAY_SIZE / 2 ? oldCapacity * 2 : MAX_ARRAY_SIZE;
			int newCapacity = Math.max((int) minCapacity, grownCapacity);
			buf = Arrays.copyOf(buf, newCapacity);
		}
	}
}
