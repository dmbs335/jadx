package jadx.core.utils;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/** Compact immutable sorted set backed by one exact-size array. */
public final class ImmutableSortedSet<E> extends AbstractSet<E> implements SortedSet<E> {
	private final Object[] elements;
	private final Comparator<? super E> comparator;

	private ImmutableSortedSet(Object[] elements, Comparator<? super E> comparator) {
		this.elements = elements;
		this.comparator = comparator;
	}

	public static <E> ImmutableSortedSet<E> copyOf(SortedSet<E> source) {
		return new ImmutableSortedSet<>(source.toArray(), source.comparator());
	}

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		if (compare(fromElement, toElement) > 0) {
			throw new IllegalArgumentException("fromElement is greater than toElement");
		}
		return slice(lowerBound(fromElement), lowerBound(toElement));
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return slice(0, lowerBound(toElement));
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return slice(lowerBound(fromElement), elements.length);
	}

	@Override
	@SuppressWarnings("unchecked")
	public E first() {
		if (elements.length == 0) {
			throw new NoSuchElementException();
		}
		return (E) elements[0];
	}

	@Override
	@SuppressWarnings("unchecked")
	public E last() {
		if (elements.length == 0) {
			throw new NoSuchElementException();
		}
		return (E) elements[elements.length - 1];
	}

	@Override
	public int size() {
		return elements.length;
	}

	@Override
	public boolean contains(Object value) {
		try {
			int index = lowerBound(value);
			return index < elements.length && compare(elements[index], value) == 0;
		} catch (ClassCastException | NullPointerException e) {
			return false;
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<>() {
			private int index;

			@Override
			public boolean hasNext() {
				return index < elements.length;
			}

			@Override
			@SuppressWarnings("unchecked")
			public E next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return (E) elements[index++];
			}
		};
	}

	@Override
	public Object[] toArray() {
		return elements.clone();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] array) {
		int size = elements.length;
		T[] result = array.length >= size
				? array
				: (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		System.arraycopy(elements, 0, result, 0, size);
		if (result.length > size) {
			result[size] = null;
		}
		return result;
	}

	private SortedSet<E> slice(int from, int to) {
		return new ImmutableSortedSet<>(Arrays.copyOfRange(elements, from, to), comparator);
	}

	private int lowerBound(Object value) {
		int low = 0;
		int high = elements.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			if (compare(elements[mid], value) < 0) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		return low;
	}

	@SuppressWarnings("unchecked")
	private int compare(Object first, Object second) {
		if (comparator != null) {
			return comparator.compare((E) first, (E) second);
		}
		return ((Comparable<Object>) first).compareTo(second);
	}
}
