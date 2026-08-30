package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMarkReadOnlyMethods extends IntegrationTest {

	public static class TestCls {
		private Object value;
		private volatile Object volatileValue;
		private int reads;

		public final Object getFinalValue() {
			return value;
		}

		public Object getOpenValue() {
			return value;
		}

		public final Object getVolatileValue() {
			return volatileValue;
		}

		public final synchronized Object getSynchronizedValue() {
			return value;
		}

		public final Object getValueWithSideEffect() {
			reads++;
			return value;
		}

		public final String getConstantString() {
			return "{}";
		}

		public String getOpenConstantString() {
			return "{}";
		}

	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);

		assertThat(getMethod(cls, "getFinalValue").contains(AType.READ_ONLY_METHOD)).isTrue();
		assertThat(getMethod(cls, "getOpenValue").contains(AType.READ_ONLY_METHOD)).isFalse();
		assertThat(getMethod(cls, "getVolatileValue").contains(AType.READ_ONLY_METHOD)).isFalse();
		assertThat(getMethod(cls, "getSynchronizedValue").contains(AType.READ_ONLY_METHOD)).isFalse();
		assertThat(getMethod(cls, "getValueWithSideEffect").contains(AType.READ_ONLY_METHOD)).isFalse();
		assertThat(getMethod(cls, "getConstantString").contains(AType.READ_ONLY_METHOD)).isTrue();
		assertThat(getMethod(cls, "getConstantString").contains(AType.CONSTANT_RETURN_METHOD)).isTrue();
		assertThat(getMethod(cls, "getOpenConstantString").contains(AType.READ_ONLY_METHOD)).isFalse();
		assertThat(getMethod(cls, "getOpenConstantString").contains(AType.CONSTANT_RETURN_METHOD)).isFalse();
	}
}
