package jadx.api.plugins.input.data.annotations;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

public interface IAnnotation {
	String getAnnotationClass();

	AnnotationVisibility getVisibility();

	Map<String, EncodedValue> getValues();

	default int getValuesCount() {
		Map<String, EncodedValue> values = getValues();
		return values == null ? 0 : values.size();
	}

	default boolean isValuesEmpty() {
		return getValuesCount() == 0;
	}

	default @Nullable EncodedValue getValue(String name) {
		Map<String, EncodedValue> values = getValues();
		return values == null ? null : values.get(name);
	}

	default void forEachValue(BiConsumer<? super String, ? super EncodedValue> action) {
		Map<String, EncodedValue> values = getValues();
		if (values != null) {
			values.forEach(action);
		}
	}

	default void replaceValues(BiFunction<? super String, ? super EncodedValue, ? extends EncodedValue> function) {
		Map<String, EncodedValue> values = getValues();
		if (values != null) {
			values.replaceAll(function);
		}
	}

	default @Nullable EncodedValue getDefaultValue() {
		return getValue("value");
	}
}
