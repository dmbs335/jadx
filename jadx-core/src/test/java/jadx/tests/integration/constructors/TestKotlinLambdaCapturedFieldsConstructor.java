package jadx.tests.integration.constructors;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestKotlinLambdaCapturedFieldsConstructor extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmaliFiles())
				.code()
				.doesNotContain("'super' call moved")
				.contains("class Capture extends Lambda implements Function3")
				.contains("this.capture4 = obj4;")
				.contains("return new Capture(obj, obj2, obj3, obj4);")
				.doesNotContain("new Function3()")
				.contains("super(3);");
	}
}
