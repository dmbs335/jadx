package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestReceiverRestoreAfterMapPut extends SmaliTest {
	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.contains("step()")
				.doesNotContain("this = this")
				.doesNotContain("Code decompiled incorrectly");
	}
}
