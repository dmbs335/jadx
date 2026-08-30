package jadx.tests.integration.inner;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTopLevelSyntheticAnonymousCapture extends SmaliTest {

	@Test
	public void test() {
		ClassNode cls = getClassNodeFromSmaliFiles("TopLevelCaptureCaller");

		String first = cls.getCode().getCodeStr();
		String second = cls.reloadCode().getCodeStr();

		assertThat(first).contains("capture(final TopLevelCaptureCaller caller)");
		assertThat(first).contains("TopLevelCaptureCaller.this.print();");
		assertThat(second).isEqualTo(first);
	}
}
