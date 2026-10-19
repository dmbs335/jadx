package jadx.tests.integration.fields;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestFieldCastRuntimeSemantics extends IntegrationTest {

	public static class TestCls {
		private Object value = "ok";
		private static Object staticValue = "ok";

		public String read() {
			return ((String) value).trim();
		}

		public String readStatic() {
			return ((String) staticValue).trim();
		}

		public String readLocal() {
			Object local = value;
			if (local == null) {
				return null;
			}
			return ((String) local).trim();
		}

		public void check() {
			if (!"ok".equals(read()) || !"ok".equals(readStatic()) || !"ok".equals(readLocal())) {
				throw new AssertionError("field cast success");
			}
			value = new Object();
			staticValue = new Object();
			boolean instanceRejected = false;
			boolean staticRejected = false;
			boolean localRejected = false;
			try {
				read();
			} catch (ClassCastException expected) {
				instanceRejected = true;
			}
			try {
				readStatic();
			} catch (ClassCastException expected) {
				staticRejected = true;
			}
			try {
				readLocal();
			} catch (ClassCastException expected) {
				localRejected = true;
			}
			if (!instanceRejected || !staticRejected || !localRejected) {
				throw new AssertionError("field cast failure lost");
			}
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class)).code().contains("(String)");
	}
}
