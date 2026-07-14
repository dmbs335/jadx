package jadx.tests.integration.conditions;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInlineAssignInCopiedCondition extends IntegrationTest {

	public static class TestCls {
		public String test(Data data) {
			Value value;
			if (data == null || (value = data.getValue()) == null) {
				return "empty";
			}
			return value.getText();
		}

		private static class Data {
			Value getValue() {
				return null;
			}
		}

		private static class Value {
			String getText() {
				return null;
			}
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);

		assertThat(cls)
				.code()
				.containsOne("data == null || (value = data.getValue()) == null");
	}
}
