package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;


import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestConstructorMoveWarning extends ExpectedConstructorErrorTest {

	@Test
	public void test() {
		disableCompilation();
		// The field write in the bytecode precedes the super call. Moving the call
		// ahead of the write can change what the parent constructor observes.
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Cannot move 'super' call to the top without changing code semantics")
				.contains("JADX ERROR")
				.contains("Method not decompiled:");
	}
}
