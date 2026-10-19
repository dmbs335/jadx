package jadx.tests.integration.types;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestTypeResolver25 extends SmaliTest {

	@Test
	public void testSmali() {
		assertThat(getClassNodeFromSmali())
				.code()
				.containsOne("((Closeable) t).close();")
				.oneOf(c -> c.containsOne("t = obj;"),
						c -> c.containsOne("t = (T) obj;"));
	}
}
