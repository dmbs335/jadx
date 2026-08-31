package jadx.core.dex.visitors.typeinference;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityIntMapTest {
	@Test
	void shouldKeepIndexesAcrossResizeAndRemoval() {
		IdentityIntMap<Object> map = new IdentityIntMap<>();
		Object[] first = new Object[64];
		for (int i = 0; i < first.length; i++) {
			first[i] = new Object();
			map.put(first[i], i);
		}
		for (int i = 0; i < first.length; i++) {
			assertThat(map.get(first[i])).isEqualTo(i);
		}

		for (int i = 0; i < first.length; i += 2) {
			map.remove(first[i]);
		}
		for (int i = 0; i < first.length; i++) {
			assertThat(map.get(first[i])).isEqualTo(i % 2 == 0 ? -1 : i);
		}

		Object[] second = new Object[64];
		for (int i = 0; i < second.length; i++) {
			second[i] = new Object();
			map.put(second[i], i + 100);
		}
		for (int i = 0; i < second.length; i++) {
			assertThat(map.get(second[i])).isEqualTo(i + 100);
		}

		map.clear();
		for (Object key : second) {
			assertThat(map.get(key)).isEqualTo(-1);
		}
	}

	@Test
	void shouldCompareKeysByIdentity() {
		IdentityIntMap<String> map = new IdentityIntMap<>();
		String first = new String("same");
		String second = new String("same");
		map.put(first, 1);

		assertThat(map.get(first)).isEqualTo(1);
		assertThat(map.get(second)).isEqualTo(-1);
	}
}
