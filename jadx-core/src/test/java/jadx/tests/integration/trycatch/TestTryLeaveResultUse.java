package jadx.tests.integration.trycatch;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTryLeaveResultUse extends SmaliTest {
	public static Iterable<Integer> provide(int mode) {
		if (mode == 2) {
			throw new IllegalArgumentException("protected producer");
		}
		if (mode == 1) {
			return new Iterable<>() {
				@Override
				public Iterator<Integer> iterator() {
					throw new IllegalArgumentException("unprotected iterator");
				}
			};
		}
		return List.of(1);
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		ClassNode cls = getClassNodeFromSmali();
		assertThat(cls).code()
				.doesNotContain("Incorrect try/catch scope")
				.doesNotContain("Code decompiled incorrectly");
	}
}
