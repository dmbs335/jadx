package jadx.core.dex.visitors.usage;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import jadx.core.utils.SmallSet;

public class UseSet<K, V> {
	private static final Object NULL_VALUE = new Object();

	private final Map<K, Object> useMap = new HashMap<>();

	public void add(K obj, V use) {
		if (obj == use) {
			// self excluded
			return;
		}
		Object stored = useMap.putIfAbsent(obj, maskNull(use));
		if (stored == null) {
			return;
		}
		if (stored instanceof MultiValueSet) {
			asMultiValueSet(stored).add(use);
			return;
		}
		if (stored instanceof SortedValues) {
			SortedValues<V> sortedValues = asSortedValues(stored);
			if (sortedValues.contains(use)) {
				return;
			}
			Set<V> values = new MultiValueSet<>();
			values.addAll(sortedValues.values);
			values.add(use);
			useMap.put(obj, values);
			return;
		}
		V singleValue = unmaskNull(stored);
		if (Objects.equals(singleValue, use)) {
			return;
		}
		Set<V> values = new MultiValueSet<>();
		values.add(singleValue);
		values.add(use);
		useMap.put(obj, values);
	}

	public Set<V> get(K obj) {
		Object stored = useMap.get(obj);
		if (stored == null) {
			return null;
		}
		return asSet(stored);
	}

	public Set<V> getOrDefault(K obj, Set<V> defaultValue) {
		Set<V> values = get(obj);
		return values == null ? defaultValue : values;
	}

	public void visit(BiConsumer<K, Set<V>> consumer) {
		for (Map.Entry<K, Object> entry : useMap.entrySet()) {
			consumer.accept(entry.getKey(), asSet(entry.getValue()));
		}
	}

	public List<V> getSortedList(K obj) {
		Object stored = useMap.get(obj);
		if (stored == null) {
			return Collections.emptyList();
		}
		return asSortedList(obj, stored);
	}

	public void visitSorted(BiConsumer<K, List<V>> consumer) {
		for (Map.Entry<K, Object> entry : useMap.entrySet()) {
			consumer.accept(entry.getKey(), asSortedList(entry.getKey(), entry.getValue()));
		}
	}

	private List<V> asSortedList(K key, Object stored) {
		if (stored instanceof SortedValues) {
			return asSortedValues(stored).values;
		}
		if (!(stored instanceof MultiValueSet)) {
			return Collections.singletonList(unmaskNull(stored));
		}
		List<V> values = new ArrayList<>(asMultiValueSet(stored));
		values.sort(UseSet::compareValues);
		values = Collections.unmodifiableList(values);
		useMap.put(key, new SortedValues<>(values));
		return values;
	}

	@SuppressWarnings("unchecked")
	private static <T> int compareValues(T first, T second) {
		return ((Comparable<? super T>) first).compareTo(second);
	}

	@SuppressWarnings("unchecked")
	private Set<V> asSet(Object stored) {
		if (stored instanceof MultiValueSet || stored instanceof SortedValues) {
			return (Set<V>) stored;
		}
		return Collections.singleton(unmaskNull(stored));
	}

	private Object maskNull(V value) {
		return value == null ? NULL_VALUE : value;
	}

	@SuppressWarnings("unchecked")
	private V unmaskNull(Object value) {
		return value == NULL_VALUE ? null : (V) value;
	}

	@SuppressWarnings("unchecked")
	private MultiValueSet<V> asMultiValueSet(Object stored) {
		return (MultiValueSet<V>) stored;
	}

	@SuppressWarnings("unchecked")
	private SortedValues<V> asSortedValues(Object stored) {
		return (SortedValues<V>) stored;
	}

	private static final class MultiValueSet<E> extends SmallSet<E> {
	}

	private static final class SortedValues<E> extends AbstractSet<E> {
		private final List<E> values;

		private SortedValues(List<E> values) {
			this.values = values;
		}

		@Override
		public Iterator<E> iterator() {
			return values.iterator();
		}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		public boolean contains(Object value) {
			return values.contains(value);
		}
	}
}
