package jadx.api.impl;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

/**
 * Compact sorted map for code positions. Code generation writes positions in ascending order in
 * almost all cases, so the common path is an append to primitive keys instead of a tree node
 * allocation. Rare out-of-order line annotations are inserted at their sorted position.
 */
final class CodePositionMap<V> extends AbstractMap<Integer, V> {
	private static final int INITIAL_CAPACITY = 4;

	private int[] keys = new int[INITIAL_CAPACITY];
	private Object[] values = new Object[INITIAL_CAPACITY];
	private final int growthShift;
	private int size;

	CodePositionMap() {
		this(1);
	}

	CodePositionMap(int growthShift) {
		this.growthShift = growthShift;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean containsKey(Object key) {
		return key instanceof Integer && find((Integer) key) >= 0;
	}

	@Override
	public @Nullable V get(Object key) {
		if (!(key instanceof Integer)) {
			return null;
		}
		int index = find((Integer) key);
		return index < 0 ? null : valueAt(index);
	}

	@Override
	public @Nullable V put(Integer key, V value) {
		return putValue(key.intValue(), value);
	}

	public @Nullable V putValue(int key, V value) {
		if (size != 0) {
			int last = size - 1;
			int lastKey = keys[last];
			if (key > lastKey) {
				ensureCapacity(size + 1);
				keys[size] = key;
				values[size] = value;
				size++;
				return null;
			}
			if (key == lastKey) {
				V previous = valueAt(last);
				values[last] = value;
				return previous;
			}
		}
		int index = find(key);
		if (index >= 0) {
			V previous = valueAt(index);
			values[index] = value;
			return previous;
		}
		int insertAt = -index - 1;
		ensureCapacity(size + 1);
		int moveCount = size - insertAt;
		if (moveCount != 0) {
			System.arraycopy(keys, insertAt, keys, insertAt + 1, moveCount);
			System.arraycopy(values, insertAt, values, insertAt + 1, moveCount);
		}
		keys[insertAt] = key;
		values[insertAt] = value;
		size++;
		return null;
	}

	public void putAllShifted(CodePositionMap<? extends V> source, int shift) {
		int sourceSize = source.size;
		ensureCapacity(size + sourceSize);
		for (int i = 0; i < sourceSize; i++) {
			putValue(source.keys[i] + shift, source.valueAt(i));
		}
	}

	@Override
	public void forEach(BiConsumer<? super Integer, ? super V> action) {
		for (int i = 0; i < size; i++) {
			action.accept(keys[i], valueAt(i));
		}
	}

	@Override
	public Set<Map.Entry<Integer, V>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public Iterator<Map.Entry<Integer, V>> iterator() {
				return new Iterator<>() {
					private int index;

					@Override
					public boolean hasNext() {
						return index < size;
					}

					@Override
					public Map.Entry<Integer, V> next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						int current = index++;
						return new SimpleImmutableEntry<>(keys[current], valueAt(current));
					}
				};
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	private int find(int key) {
		return Arrays.binarySearch(keys, 0, size, key);
	}

	private void ensureCapacity(int required) {
		if (required <= keys.length) {
			return;
		}
		int newCapacity = Math.max(required, keys.length << growthShift);
		keys = Arrays.copyOf(keys, newCapacity);
		values = Arrays.copyOf(values, newCapacity);
	}

	@SuppressWarnings("unchecked")
	private V valueAt(int index) {
		return (V) values[index];
	}
}
