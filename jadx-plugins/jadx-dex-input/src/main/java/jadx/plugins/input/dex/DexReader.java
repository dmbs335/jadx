package jadx.plugins.input.dex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

import jadx.api.plugins.input.data.IClassData;
import jadx.plugins.input.dex.sections.DexClassData;
import jadx.plugins.input.dex.sections.DexHeader;
import jadx.plugins.input.dex.sections.SectionReader;

public class DexReader {
	private final int uniqId;
	private final String inputFileName;
	private final ByteBuffer buf;
	private final DexHeader header;
	// Cache only strings requested at least twice, keeping one-off debug and annotation values collectible.
	private final AtomicReferenceArray<String> stringCache;
	private final AtomicLongArray seenStrings;

	public DexReader(int uniqId, String inputFileName, byte[] content, int offset) {
		this.uniqId = uniqId;
		this.inputFileName = inputFileName;
		this.buf = ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN);
		this.header = new DexHeader(new SectionReader(this, offset));
		int stringCount = header.getStringIdsSize();
		this.stringCache = new AtomicReferenceArray<>(stringCount);
		this.seenStrings = new AtomicLongArray((stringCount + Long.SIZE - 1) / Long.SIZE);
	}

	public String getCachedString(int idx) {
		return stringCache.get(idx);
	}

	public boolean shouldCacheString(int idx) {
		int wordIndex = idx / Long.SIZE;
		long mask = 1L << (idx % Long.SIZE);
		long word = seenStrings.get(wordIndex);
		while ((word & mask) == 0) {
			if (seenStrings.compareAndSet(wordIndex, word, word | mask)) {
				return false;
			}
			word = seenStrings.get(wordIndex);
		}
		return true;
	}

	public String cacheString(int idx, String value) {
		if (stringCache.compareAndSet(idx, null, value)) {
			return value;
		}
		return stringCache.get(idx);
	}

	public void visitClasses(Consumer<IClassData> consumer) {
		int count = header.getClassDefsSize();
		if (count == 0) {
			return;
		}
		int classDefsOff = header.getClassDefsOff();
		SectionReader in = new SectionReader(this, classDefsOff);
		DexClassData classData = new DexClassData(in);
		for (int i = 0; i < count; i++) {
			consumer.accept(classData);
			classData.shiftOffset(DexClassData.SIZE);
		}
	}

	public ByteBuffer getBuf() {
		return buf;
	}

	public DexHeader getHeader() {
		return header;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public int getUniqId() {
		return uniqId;
	}

	@Override
	public String toString() {
		return inputFileName;
	}
}
