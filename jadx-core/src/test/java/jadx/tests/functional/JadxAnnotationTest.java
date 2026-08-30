package jadx.tests.functional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.data.annotations.AnnotationVisibility;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.annotations.JadxAnnotation;
import jadx.api.plugins.input.data.attributes.types.AnnotationsAttr;

import static org.assertj.core.api.Assertions.assertThat;

public class JadxAnnotationTest {
	@Test
	public void testLazyEmptyValuesPreserveMutableMapContract() {
		JadxAnnotation annotation = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LTest;");
		Map<String, EncodedValue> values = annotation.getValues();

		assertThat(values).isEmpty();
		Set<Map.Entry<String, EncodedValue>> entries = values.entrySet();
		EncodedValue first = new EncodedValue(EncodedType.ENCODED_STRING, "first");
		values.put("value", first);

		assertThat(entries).hasSize(1);
		assertThat(values.get("value")).isSameAs(first);

		values.replaceAll((name, value) -> new EncodedValue(EncodedType.ENCODED_STRING, "second"));
		assertThat(values.get("value").getValue()).isEqualTo("second");

		values.remove("value");
		assertThat(values).isEmpty();
	}

	@Test
	public void testAnnotationsAttrForEachAndList() {
		JadxAnnotation first = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LFirst;");
		JadxAnnotation second = new JadxAnnotation(AnnotationVisibility.BUILD, "LSecond;");
		Map<String, IAnnotation> map = new HashMap<>();
		map.put(first.getAnnotationClass(), first);
		map.put(second.getAnnotationClass(), second);
		AnnotationsAttr attr = new AnnotationsAttr(map);

		List<String> types = new ArrayList<>();
		attr.forEach((type, annotation) -> types.add(annotation.getAnnotationClass()));

		assertThat(types).containsExactlyInAnyOrder("LFirst;", "LSecond;");
		assertThat(attr.getList()).containsExactlyInAnyOrder(first, second);
		assertThat(attr.getAll()).containsExactlyInAnyOrder(first, second);
	}

	@Test
	public void testAnnotationsAttrPack() {
		JadxAnnotation system = new JadxAnnotation(AnnotationVisibility.SYSTEM, "LSystem;");
		JadxAnnotation first = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LFirst;");
		JadxAnnotation replacement = new JadxAnnotation(AnnotationVisibility.BUILD, "LFirst;");
		JadxAnnotation second = new JadxAnnotation(AnnotationVisibility.RUNTIME, "LSecond;");

		assertThat(AnnotationsAttr.pack(List.of())).isNull();
		assertThat(AnnotationsAttr.pack(List.of(system))).isNull();

		AnnotationsAttr singleton = AnnotationsAttr.pack(List.of(system, first));
		assertThat(singleton).isNotNull();
		assertThat(singleton.size()).isEqualTo(1);
		assertThat(singleton.get("LFirst;")).isSameAs(first);

		AnnotationsAttr duplicate = AnnotationsAttr.pack(List.of(first, replacement));
		assertThat(duplicate).isNotNull();
		assertThat(duplicate.size()).isEqualTo(1);
		assertThat(duplicate.get("LFirst;")).isSameAs(replacement);

		AnnotationsAttr multiple = AnnotationsAttr.pack(List.of(system, first, second));
		assertThat(multiple).isNotNull();
		assertThat(multiple.getList()).containsExactlyInAnyOrder(first, second);
	}
}
