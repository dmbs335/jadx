package jadx.api.plugins.input.data.annotations;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
		this.values = values;
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
			values = new LazyLinkedHashMap<>();
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
	 * Preserve the mutable {@link Map} contract for external callers without
	 * allocating the 64-byte {@link LinkedHashMap} body until a write or live
	 * entry-set view actually needs it.
	 */
	private static final class LazyLinkedHashMap<K, V> extends AbstractMap<K, V> {
		private LinkedHashMap<K, V> delegate;

		private LinkedHashMap<K, V> ensureDelegate() {
			if (delegate == null) {
				delegate = new LinkedHashMap<>();
			}
			return delegate;
		}

		@Override
		public int size() {
			return delegate == null ? 0 : delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate == null || delegate.isEmpty();
		}

		@Override
		public V get(Object key) {
			return delegate == null ? null : delegate.get(key);
		}

		@Override
		public V put(K key, V value) {
			return ensureDelegate().put(key, value);
		}

		@Override
		public V remove(Object key) {
			return delegate == null ? null : delegate.remove(key);
		}

		@Override
		public void clear() {
			if (delegate != null) {
				delegate.clear();
			}
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {
			if (delegate != null) {
				delegate.forEach(action);
			}
		}

		@Override
		public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
			if (delegate != null) {
				delegate.replaceAll(function);
			}
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return ensureDelegate().entrySet();
		}
	}
}
