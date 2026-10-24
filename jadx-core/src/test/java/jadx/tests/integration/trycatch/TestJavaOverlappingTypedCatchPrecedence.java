package jadx.tests.integration.trycatch;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.RaungTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestJavaOverlappingTypedCatchPrecedence extends RaungTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
	}

	@Test
	public void test() {
		forceDecompiledCheck();
		assertThat(getClassNodeFromRaung()).code()
				.contains("catch (RuntimeException")
				.doesNotContain("catch (IllegalArgumentException")
				.doesNotContain("JADX ERROR");
	}
}
