package jadx.plugins.input.dex.sections.annotations;

import java.util.Arrays;

import jadx.plugins.input.dex.sections.SectionReader;

/** Compact immutable int-to-int index used by DEX annotation directories. */
public final class AnnotationsOffsets {
	private static final AnnotationsOffsets EMPTY = new AnnotationsOffsets(new long[0]);

	private final long[] entries;

	private AnnotationsOffsets(long[] entries) {
		this.entries = entries;
	}

	public static AnnotationsOffsets empty() {
		return EMPTY;
	}

	public static AnnotationsOffsets read(SectionReader reader, int count) {
		if (count == 0) {
			return EMPTY;
		}
		long[] entries = new long[count];
		boolean sorted = true;
		int previousKey = -1;
		for (int i = 0; i < count; i++) {
			int key = reader.readInt();
			int value = reader.readInt();
			entries[i] = ((long) key << 32) | (value & 0xFFFF_FFFFL);
			if (key < previousKey) {
				sorted = false;
			}
			previousKey = key;
		}
		if (!sorted) {
			Arrays.sort(entries);
		}
		return new AnnotationsOffsets(entries);
	}

	public int get(int key) {
		int low = 0;
		int high = entries.length - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			long entry = entries[mid];
			int entryKey = (int) (entry >>> 32);
			if (entryKey < key) {
				low = mid + 1;
			} else if (entryKey > key) {
				high = mid - 1;
			} else {
				return (int) entry;
			}
		}
		return 0;
	}
}
