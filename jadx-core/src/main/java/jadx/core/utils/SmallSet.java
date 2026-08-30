package jadx.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Mutable set optimized for a small number of elements.
 * Stores up to nine values directly and switches to a hash set when it grows.
 */
public class SmallSet<E> extends AbstractSet<E> {
	private static final int INLINE_CAPACITY = 9;

	private E first;
	private E second;
	private E third;
	private E fourth;
	private E fifth;
	private E sixth;
	private E seventh;
	private E eighth;
	private E ninth;
	private int size;
	private Set<E> delegate;

	public SmallSet() {
	}

	public SmallSet(Collection<? extends E> values) {
		addAll(values);
	}

	@Override
	public int size() {
		return delegate == null ? size : delegate.size();
	}

	@Override
	public boolean contains(Object value) {
		if (delegate != null) {
			return delegate.contains(value);
		}
		for (int i = 0; i < size; i++) {
			if (Objects.equals(getInline(i), value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean add(E value) {
		if (delegate != null) {
			return delegate.add(value);
		}
		if (contains(value)) {
			return false;
		}
		if (size < INLINE_CAPACITY) {
			setInline(size++, value);
			return true;
		}
		promote();
		return delegate.add(value);
	}

	@Override
	public boolean remove(Object value) {
		if (delegate != null) {
			return delegate.remove(value);
		}
		for (int i = 0; i < size; i++) {
			if (Objects.equals(getInline(i), value)) {
				removeInline(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public void clear() {
		if (delegate != null) {
			delegate = null;
		}
		first = null;
		second = null;
		third = null;
		fourth = null;
		fifth = null;
		sixth = null;
		seventh = null;
		eighth = null;
		ninth = null;
		size = 0;
	}

	@Override
	public Iterator<E> iterator() {
		if (delegate != null) {
			return delegate.iterator();
		}
		return new Iterator<>() {
			private int next;
			private int current = -1;

			@Override
			public boolean hasNext() {
				return next < size;
			}

			@Override
			public E next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				current = next++;
				return getInline(current);
			}

			@Override
			public void remove() {
				if (current == -1) {
					throw new IllegalStateException();
				}
				removeInline(current);
				next = current;
				current = -1;
			}
		};
	}

	private void promote() {
		delegate = new HashSet<>(INLINE_CAPACITY + 1);
		for (int i = 0; i < size; i++) {
			delegate.add(getInline(i));
		}
		first = null;
		second = null;
		third = null;
		fourth = null;
		fifth = null;
		sixth = null;
		seventh = null;
		eighth = null;
		ninth = null;
		size = 0;
	}

	private void removeInline(int index) {
		int last = --size;
		if (index != last) {
			setInline(index, getInline(last));
		}
		setInline(last, null);
	}

	private E getInline(int index) {
		switch (index) {
			case 0:
				return first;
			case 1:
				return second;
			case 2:
				return third;
			case 3:
				return fourth;
			case 4:
				return fifth;
			case 5:
				return sixth;
			case 6:
				return seventh;
			case 7:
				return eighth;
			case 8:
				return ninth;
			default:
				throw new IndexOutOfBoundsException("index: " + index);
		}
	}

	private void setInline(int index, E value) {
		switch (index) {
			case 0:
				first = value;
				break;
			case 1:
				second = value;
				break;
			case 2:
				third = value;
				break;
			case 3:
				fourth = value;
				break;
			case 4:
				fifth = value;
				break;
			case 5:
				sixth = value;
				break;
			case 6:
				seventh = value;
				break;
			case 7:
				eighth = value;
				break;
			case 8:
				ninth = value;
				break;
			default:
				throw new IndexOutOfBoundsException("index: " + index);
		}
	}
}
