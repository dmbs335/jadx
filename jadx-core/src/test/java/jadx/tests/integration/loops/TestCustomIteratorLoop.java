package jadx.tests.integration.loops;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCustomIteratorLoop extends IntegrationTest {

	public static class TestCls {
		public String test(Provider<String> provider) {
			StringBuilder result = new StringBuilder();
			Iterator<String> iterator = provider.iterator();
			while (iterator.hasNext()) {
				result.append(iterator.next());
			}
			return result.toString();
		}

		public static class Provider<T> {
			private final List<T> values;

			public Provider(List<T> values) {
				this.values = values;
			}

			public Iterator<T> iterator() {
				return values.iterator();
			}
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("Iterator<String> iterator = provider.iterator();")
				.containsOne("while (iterator.hasNext()) {")
				.doesNotContain("for (String");
	}
}
