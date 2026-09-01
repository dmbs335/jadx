package jadx.api.plugins.input.data.annotations;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class JadxAnnotation implements IAnnotation {
	private final AnnotationVisibility visibility;
	private final String type;
	private Map<String, EncodedValue> values;

	public JadxAnnotation(AnnotationVisibility visibility, String type) {
		this.visibility = visibility;
		this.type = type;
	}

	public JadxAnnotation(AnnotationVisibility visibility, String type, Map<String, EncodedValue> values) {
		this.visibility = visibility;
		this.type = type;
		this.values = CompactLinkedHashMap.pack(values);
	}

	@Override
	public String getAnnotationClass() {
		return type;
	}

	@Override
	public AnnotationVisibility getVisibility() {
		return visibility;
	}

	@Override
	public Map<String, EncodedValue> getValues() {
		if (values == null) {
			values = new CompactLinkedHashMap<>();
		}
		return values;
	}

	@Override
	public int getValuesCount() {
		return values == null ? 0 : values.size();
	}

	@Override
	public boolean isValuesEmpty() {
		return values == null || values.isEmpty();
	}

	@Override
	public EncodedValue getValue(String name) {
		return values == null ? null : values.get(name);
	}

	@Override
	public void forEachValue(BiConsumer<? super String, ? super EncodedValue> action) {
		if (values != null) {
			values.forEach(action);
		}
	}

	@Override
	public void replaceValues(BiFunction<? super String, ? super EncodedValue, ? extends EncodedValue> function) {
		if (values != null) {
			values.replaceAll(function);
		}
	}

	@Override
	public String toString() {
		return "Annotation{" + visibility + ", type=" + type + ", values="
				+ (values == null ? "{}" : values) + '}';
	}

	/**
	 * Preserve insertion order and the mutable {@link Map} contract for external callers while
	 * keeping the common small annotation maps in one flat array. Operations requiring a live
	 * entry-set view, or maps growing past the compact limit, transparently promote the storage
	 * to a {@link LinkedHashMap}.
	 */
	private static final class CompactLinkedHashMap<K, V> extends AbstractMap<K, V> {
		private static final int MAX_COMPACT_SIZE = 8;

		private Object[] entries;
		private int size;
		private LinkedHashMap<K, V> delegate;

		private static <K, V> Map<K, V> pack(Map<K, V> source) {
			if (source == null || source.isEmpty()) {
				return null;
			}
			if (source.size() > MAX_COMPACT_SIZE) {
				return source;
			}
			CompactLinkedHashMap<K, V> map = new CompactLinkedHashMap<>();
			map.entries = new Object[source.size() * 2];
			source.forEach((key, value) -> {
				int index = map.size * 2;
				map.entries[index] = key;
				map.entries[index + 1] = value;
				map.size++;
			});
			return map;
		}

		private LinkedHashMap<K, V> ensureDelegate() {
			if (delegate == null) {
				delegate = new LinkedHashMap<>(Math.max(size + 1, 4));
				for (int i = 0; i < size; i++) {
					delegate.put(keyAt(i), valueAt(i));
				}
				entries = null;
				size = 0;
			}
			return delegate;
		}

		@SuppressWarnings("unchecked")
		private K keyAt(int index) {
			return (K) entries[index * 2];
		}

		@SuppressWarnings("unchecked")
		private V valueAt(int index) {
			return (V) entries[index * 2 + 1];
		}

		private int findKey(Object key) {
			for (int i = 0; i < size; i++) {
				if (Objects.equals(entries[i * 2], key)) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int size() {
			return delegate == null ? size : delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate == null ? size == 0 : delegate.isEmpty();
		}

		@Override
		public V get(Object key) {
			if (delegate != null) {
				return delegate.get(key);
			}
			int index = findKey(key);
			return index == -1 ? null : valueAt(index);
		}

		@Override
		public boolean containsKey(Object key) {
			return delegate == null ? findKey(key) != -1 : delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			if (delegate != null) {
				return delegate.containsValue(value);
			}
			for (int i = 0; i < size; i++) {
				if (Objects.equals(entries[i * 2 + 1], value)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public V put(K key, V value) {
			if (delegate != null) {
				return delegate.put(key, value);
			}
			int index = findKey(key);
			if (index != -1) {
				int valueIndex = index * 2 + 1;
				V previous = valueAt(index);
				entries[valueIndex] = value;
				return previous;
			}
			if (size == MAX_COMPACT_SIZE) {
				return ensureDelegate().put(key, value);
			}
			entries = entries == null ? new Object[2] : Arrays.copyOf(entries, (size + 1) * 2);
			entries[size * 2] = key;
			entries[size * 2 + 1] = value;
			size++;
			return null;
		}

		@Override
		public V remove(Object key) {
			if (delegate != null) {
				return delegate.remove(key);
			}
			int index = findKey(key);
			if (index == -1) {
				return null;
			}
			V previous = valueAt(index);
			int offset = index * 2;
			int move = (size - index - 1) * 2;
			if (move != 0) {
				System.arraycopy(entries, offset + 2, entries, offset, move);
			}
			entries[(size - 1) * 2] = null;
			entries[(size - 1) * 2 + 1] = null;
			size--;
			return previous;
		}

		@Override
		public void clear() {
			if (delegate == null) {
				entries = null;
				size = 0;
			} else {
				delegate.clear();
			}
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {
			Objects.requireNonNull(action);
			if (delegate == null) {
				for (int i = 0; i < size; i++) {
					action.accept(keyAt(i), valueAt(i));
				}
			} else {
				delegate.forEach(action);
			}
		}

		@Override
		public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
			Objects.requireNonNull(function);
			if (delegate == null) {
				for (int i = 0; i < size; i++) {
					entries[i * 2 + 1] = function.apply(keyAt(i), valueAt(i));
				}
			} else {
				delegate.replaceAll(function);
			}
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return ensureDelegate().entrySet();
		}
	}
}
