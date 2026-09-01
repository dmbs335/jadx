package jadx.api.plugins.input.data.annotations;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JadxAnnotationTest {

	@Test
	void preservesMutableOrderedMapContract() {
		Map<String, EncodedValue> source = new LinkedHashMap<>();
		for (int i = 0; i < 8; i++) {
			source.put("k" + i, value(i));
		}
		JadxAnnotation annotation = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LTest;", source);
		Map<String, EncodedValue> values = annotation.getValues();

		assertThat(annotation.getValue("k3").getValue()).isEqualTo(3);
		annotation.replaceValues((key, value) -> key.equals("k3") ? value(33) : value);
		assertThat(values.put("k3", value(34)).getValue()).isEqualTo(33);
		assertThat(values.remove("k1").getValue()).isEqualTo(1);
		values.put("k1", value(1));
		assertThat(values.keySet()).containsExactly("k0", "k2", "k3", "k4", "k5", "k6", "k7", "k1");
		assertThat(values.put("k3", value(33)).getValue()).isEqualTo(34);
		assertThat(values.keySet()).containsExactly("k0", "k2", "k3", "k4", "k5", "k6", "k7", "k1");
		assertThat(values.remove("k1").getValue()).isEqualTo(1);
		values.put("k8", value(8));
		values.put("k9", value(9));
		assertThat(values.keySet()).containsExactly("k0", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9");
	}

	@Test
	void entrySetRemainsLiveAfterPromotion() {
		JadxAnnotation annotation = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LTest;");
		Map<String, EncodedValue> values = annotation.getValues();
		values.put(null, null);
		values.put("key", value(1));

		Iterator<Map.Entry<String, EncodedValue>> iterator = values.entrySet().iterator();
		Map.Entry<String, EncodedValue> first = iterator.next();
		assertThat(first.getKey()).isNull();
		first.setValue(value(2));
		iterator.remove();

		assertThat(values).containsOnly(Map.entry("key", value(1)));
		values.replaceAll((key, value) -> value(3));
		assertThat(values.get("key").getValue()).isEqualTo(3);
	}

	private static EncodedValue value(int value) {
		return new EncodedValue(EncodedType.ENCODED_INT, value);
	}
}
