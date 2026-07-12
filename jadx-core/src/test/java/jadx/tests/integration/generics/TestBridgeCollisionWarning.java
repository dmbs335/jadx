package jadx.tests.integration.generics;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestBridgeCollisionWarning extends IntegrationTest {

	public static class TestCls implements Factory<String> {
		@Override
		public String create() {
			return "result";
		}
	}

	public interface Factory<T> {
		T create();
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.doesNotContain("Can't rename method to resolve collision")
				.containsOne("String create()")
				.doesNotContain("Object create()");
	}
}
