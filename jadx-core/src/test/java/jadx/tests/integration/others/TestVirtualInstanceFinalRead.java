package jadx.tests.integration.others;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestVirtualInstanceFinalRead extends SmaliTest {
	@Test
	public void test() {
		List<ClassNode> classes = loadFromSmaliFiles();
		assertThat(searchCls(classes, "others.TestVirtualInstanceFinalRead"))
				.runDecompiledAutoCheck(this);
		assertThat(searchCls(classes, "others.VirtualReadBase"))
				.code()
				.containsOne("JADX WARN: Final modifier removed: instance field is read before assignment in constructor")
				.doesNotContain("final int INITIAL");
	}
}
