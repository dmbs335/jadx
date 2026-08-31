package jadx.core.dex.visitors.typeinference;

import java.util.Arrays;

/**
 * Small identity-keyed map with primitive int values.
 *
 * <p>
 * Type inference uses the map only after a short linear-list prefix. Avoiding boxed integers
 * here matters for methods with large rollback-heavy update graphs.
 * </p>
 */
final class IdentityIntMap<K> {
	private static final int MIN_CAPACITY = 16;
	private static final Object TOMBSTONE = new Object();

	private Object[] keys = new Object[MIN_CAPACITY];
	private int[] values = new int[MIN_CAPACITY];
	private int size;
	private int occupied;
	private int threshold = threshold(MIN_CAPACITY);

	public int get(K key) {
		Object[] mapKeys = keys;
		int mask = mapKeys.length - 1;
		int pos = hash(key) & mask;
		while (true) {
			Object stored = mapKeys[pos];
			if (stored == null) {
				return -1;
			}
			if (stored == key) {
				return values[pos];
			}
			pos = (pos + 1) & mask;
		}
	}

	public void put(K key, int value) {
		if (occupied + 1 > threshold) {
			int newCapacity = size + 1 <= threshold / 2 ? keys.length : keys.length * 2;
			resize(newCapacity);
		}
		Object[] mapKeys = keys;
		int mask = mapKeys.length - 1;
		int pos = hash(key) & mask;
		int tombstone = -1;
		while (true) {
			Object stored = mapKeys[pos];
			if (stored == null) {
				int insertPos = tombstone == -1 ? pos : tombstone;
				if (tombstone == -1) {
					occupied++;
				}
				mapKeys[insertPos] = key;
				values[insertPos] = value;
				size++;
				return;
			}
			if (stored == key) {
				values[pos] = value;
				return;
			}
			if (stored == TOMBSTONE && tombstone == -1) {
				tombstone = pos;
			}
			pos = (pos + 1) & mask;
		}
	}

	public void remove(K key) {
		Object[] mapKeys = keys;
		int mask = mapKeys.length - 1;
		int pos = hash(key) & mask;
		while (true) {
			Object stored = mapKeys[pos];
			if (stored == null) {
				return;
			}
			if (stored == key) {
				mapKeys[pos] = TOMBSTONE;
				size--;
				return;
			}
			pos = (pos + 1) & mask;
		}
	}

	public void clear() {
		Arrays.fill(keys, null);
		size = 0;
		occupied = 0;
	}

	private void resize(int newCapacity) {
		Object[] oldKeys = keys;
		int[] oldValues = values;
		keys = new Object[newCapacity];
		values = new int[newCapacity];
		size = 0;
		occupied = 0;
		threshold = threshold(newCapacity);
		for (int i = 0; i < oldKeys.length; i++) {
			Object key = oldKeys[i];
			if (key != null && key != TOMBSTONE) {
				@SuppressWarnings("unchecked")
				K typedKey = (K) key;
				put(typedKey, oldValues[i]);
			}
		}
	}

	private static int hash(Object key) {
		int hash = System.identityHashCode(key);
		return hash ^ (hash >>> 16);
	}

	private static int threshold(int capacity) {
		return capacity * 2 / 3;
	}
}
