package jadx.tests.integration.enums;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestEnumStaticFieldOrder extends IntegrationTest {

	public enum TestCls {
		ZERO,
		ONE,
		TWO,
		FEW,
		MANY,
		OTHER;

		public static final Companion companion = new Companion();

		public static final class Companion {
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("public static final Companion companion = new Companion();")
				.doesNotContain("static {");
	}
}
