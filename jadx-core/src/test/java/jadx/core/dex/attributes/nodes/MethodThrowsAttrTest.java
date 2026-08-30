package jadx.core.dex.attributes.nodes;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodThrowsAttrTest {
	@Test
	void keepsEmptySetUnallocatedUntilFirstThrowType() throws Exception {
		MethodThrowsAttr attr = new MethodThrowsAttr();
		Field listField = MethodThrowsAttr.class.getDeclaredField("list");
		listField.setAccessible(true);

		assertThat(attr.isEmpty()).isTrue();
		assertThat(attr.size()).isZero();
		assertThat(attr.getList()).isEmpty();
		assertThat(listField.get(attr)).isNull();

		assertThat(attr.add("java.io.IOException")).isTrue();
		assertThat(attr.add("java.io.IOException")).isFalse();
		assertThat(attr.getList()).containsExactly("java.io.IOException");
		assertThat(listField.get(attr)).isNotNull();
	}
}
