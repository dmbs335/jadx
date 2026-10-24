package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestMultipleInstanceFinalForwardReads extends SmaliTest {
	@Test
	public void test() {
		assertThat(getClassNodeFromSmali())
				.runDecompiledAutoCheck(this)
				.code()
				.contains("JADX WARN: Final modifier removed: instance field is read before assignment in constructor")
				.doesNotContain("final int")
				.contains("observedFirst = this.first")
				.contains("observedSecond = this.second");
	}
}
