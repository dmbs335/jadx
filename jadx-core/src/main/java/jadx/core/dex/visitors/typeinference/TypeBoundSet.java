package jadx.core.dex.visitors.typeinference;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Insertion-ordered set optimized for the small type-bound collections used by {@link TypeInfo}.
 */
final class TypeBoundSet extends AbstractSet<ITypeBound> {
	private static final ITypeBound[] EMPTY = new ITypeBound[0];

	private ITypeBound[] values = EMPTY;
	private int size;
	private int modCount;

	@Override
	public int size() {
		return size;
	}

	ITypeBound get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		}
		return values[index];
	}

	@Override
	public boolean contains(Object value) {
		return indexOf(value) != -1;
	}

	@Override
	public boolean add(ITypeBound value) {
		if (contains(value)) {
			return false;
		}
		ensureCapacity(size + 1);
		values[size++] = value;
		modCount++;
		return true;
	}

	@Override
	public boolean remove(Object value) {
		int index = indexOf(value);
		if (index == -1) {
			return false;
		}
		removeAt(index);
		return true;
	}

	@Override
	public void clear() {
		if (size != 0) {
			values = EMPTY;
			size = 0;
			modCount++;
		}
	}

	@Override
	public Iterator<ITypeBound> iterator() {
		return new Iterator<>() {
			private int cursor;
			private int lastReturned = -1;
			private int expectedModCount = modCount;

			@Override
			public boolean hasNext() {
				return cursor < size;
			}

			@Override
			public ITypeBound next() {
				checkForModification();
				if (cursor >= size) {
					throw new NoSuchElementException();
				}
				lastReturned = cursor;
				return values[cursor++];
			}

			@Override
			public void remove() {
				checkForModification();
				if (lastReturned == -1) {
					throw new IllegalStateException();
				}
				TypeBoundSet.this.removeAt(lastReturned);
				cursor = lastReturned;
				lastReturned = -1;
				expectedModCount = modCount;
			}

			private void checkForModification() {
				if (expectedModCount != modCount) {
					throw new ConcurrentModificationException();
				}
			}
		};
	}

	private int indexOf(Object value) {
		for (int i = 0; i < size; i++) {
			if (Objects.equals(values[i], value)) {
				return i;
			}
		}
		return -1;
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity <= values.length) {
			return;
		}
		int newCapacity = values.length == 0 ? 2 : values.length << 1;
		if (newCapacity < minCapacity) {
			newCapacity = minCapacity;
		}
		ITypeBound[] newValues = new ITypeBound[newCapacity];
		System.arraycopy(values, 0, newValues, 0, size);
		values = newValues;
	}

	private void removeAt(int index) {
		int moved = size - index - 1;
		if (moved > 0) {
			System.arraycopy(values, index + 1, values, index, moved);
		}
		values[--size] = null;
		modCount++;
	}
}
