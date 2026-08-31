package jadx.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/** Compact immutable set for small collections, preserving source iteration order. */
public final class ImmutableArraySet<E> extends AbstractSet<E> {
	private final Object[] elements;

	public ImmutableArraySet(Collection<? extends E> source) {
		this.elements = source.toArray();
	}

	@Override
	public int size() {
		return elements.length;
	}

	@Override
	public boolean contains(Object value) {
		for (Object element : elements) {
			if (Objects.equals(element, value)) {
				return true;
			}
		}
		return false;
	}

	@NotNull
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
	@SuppressWarnings("unchecked")
	public void forEach(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		for (Object element : elements) {
			action.accept((E) element);
		}
	}
}
