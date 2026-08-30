package jadx.tests.integration.others;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.MethodThrowsAttr;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.MethodThrowsVisitor;
import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class TestThrows extends IntegrationTest {

	public static class MissingThrowsTest extends Exception {

		private void throwCustomException() throws MissingThrowsTest {
			throw new MissingThrowsTest();
		}

		private void throwException() throws Exception {
			throw new Exception();
		}

		private void throwRuntimeException1() {
			throw new RuntimeException();
		}

		private void throwRuntimeException2() {
			throw new NullPointerException();
		}

		private void throwError() {
			throw new Error();
		}

		private void throwError2() {
			throw new OutOfMemoryError();
		}

		@SuppressWarnings("checkstyle:illegalThrows")
		private void throwThrowable() throws Throwable {
			throw new Throwable();
		}

		private void exceptionSource() throws FileNotFoundException {
			throw new FileNotFoundException("");
		}

		public void mergeThrownExceptions() throws IOException {
			exceptionSource();
		}

		public void rethrowThrowable() {
			try {
			} catch (Throwable t) {
				throw t;
			}
		}

		public void doSomething1(int i) throws FileNotFoundException {
			if (i == 1) {
				doSomething2(i);
			} else {
				doSomething1(i);
			}
		}

		public void doSomething2(int i) throws FileNotFoundException {
			if (i == 1) {
				exceptionSource();
			} else {
				doSomething1(i);
			}
		}

		public int doSomething3(int i) throws IllegalArgumentException {
			if (i < 0) {
				throw new IllegalArgumentException();
			}
			return 1;
		}

		public void noThrownExceptions1(InputStream i1) {
			try {
				i1.close();
			} catch (IOException ignore) {
			}
		}

		public void noThrownExceptions2() {
			try {
				throw new FileNotFoundException("");
			} catch (IOException ignore) {
			}
		}

		public void noThrownExceptions3() {
			int i = doSomething3(0);
			System.out.print(i);
		}
	}

	public static class WideSharedCallGraph {
		private void leaf() throws FileNotFoundException {
			throw new FileNotFoundException();
		}

		private void m1() throws FileNotFoundException {
			leaf();
			leaf();
		}

		private void m2() throws FileNotFoundException {
			m1();
			m1();
		}

		private void m3() throws FileNotFoundException {
			m2();
			m2();
		}

		private void m4() throws FileNotFoundException {
			m3();
			m3();
		}

		private void m5() throws FileNotFoundException {
			m4();
			m4();
		}

		private void m6() throws FileNotFoundException {
			m5();
			m5();
		}

		private void m7() throws FileNotFoundException {
			m6();
			m6();
		}

		private void m8() throws FileNotFoundException {
			m7();
			m7();
		}

		private void m9() throws FileNotFoundException {
			m8();
			m8();
		}

		private void m10() throws FileNotFoundException {
			m9();
			m9();
		}

		private void m11() throws FileNotFoundException {
			m10();
			m10();
		}

		private void m12() throws FileNotFoundException {
			m11();
			m11();
		}

		private void m13() throws FileNotFoundException {
			m12();
			m12();
		}

		private void m14() throws FileNotFoundException {
			m13();
			m13();
		}

		private void m15() throws FileNotFoundException {
			m14();
			m14();
		}

		private void m16() throws FileNotFoundException {
			m15();
			m15();
		}

		private void m17() throws FileNotFoundException {
			m16();
			m16();
		}

		private void m18() throws FileNotFoundException {
			m17();
			m17();
		}
	}

	@Test
	public void test() {
		assertThat(getClassNode(MissingThrowsTest.class))
				.code()
				.containsOne("throwCustomException() throws TestThrows$MissingThrowsTest {")
				.containsOne("throwException() throws Exception {")
				.containsOne("throwRuntimeException1() {")
				.containsOne("throwRuntimeException2() {")
				.containsOne("throwError() {")
				.containsOne("throwError2() {")
				.containsOne("throwThrowable() throws Throwable {")
				.containsOne("exceptionSource() throws FileNotFoundException {")
				.containsOne("mergeThrownExceptions() throws IOException {")
				.containsOne("rethrowThrowable() {")
				.containsOne("noThrownExceptions1(InputStream i1) {")
				.containsOne("noThrownExceptions2() {")
				.containsOne("noThrownExceptions3() {");
	}

	@Test
	public void testLateExceptionPropagationToProcessedCaller() throws Exception {
		ClassNode cls = getClassNode(MissingThrowsTest.class);
		cls.root().getProcessClasses().forceProcess(cls);
		MethodNode source = getMethod(cls, "exceptionSource");
		MethodNode caller = getMethod(cls, "mergeThrownExceptions");
		source.get(AType.METHOD_THROWS).getList().clear();
		caller.get(AType.METHOD_THROWS).getList().clear();

		MethodThrowsVisitor visitor = cls.root().getProcessClasses().getPasses().stream()
				.filter(MethodThrowsVisitor.class::isInstance)
				.map(MethodThrowsVisitor.class::cast)
				.findFirst()
				.orElseThrow();
		visitor.visit(source);

		MethodThrowsAttr callerThrows = caller.get(AType.METHOD_THROWS);
		org.assertj.core.api.Assertions.assertThat(callerThrows.getList()).contains("java.io.FileNotFoundException");
	}

	@Test
	public void testNoEmptyThrowsAttributeAfterVisit() {
		ClassNode cls = getClassNode(MissingThrowsTest.class);
		cls.root().getProcessClasses().forceProcess(cls);
		MethodNode mth = getMethod(cls, "noThrownExceptions3");

		org.assertj.core.api.Assertions.assertThat(mth.isMethodThrowsVisited()).isTrue();
		org.assertj.core.api.Assertions.assertThat(mth.get(AType.METHOD_THROWS)).isNull();
	}

	@Test
	public void testWideSharedCallGraphPropagationCompletesWithinBudget() {
		ClassNode cls = getClassNode(WideSharedCallGraph.class);
		for (MethodNode mth : cls.getMethods()) {
			mth.remove(AType.METHOD_THROWS);
			mth.setMethodThrowsVisited(true);
		}
		MethodThrowsVisitor visitor = cls.root().getProcessClasses().getPasses().stream()
				.filter(MethodThrowsVisitor.class::isInstance)
				.map(MethodThrowsVisitor.class::cast)
				.findFirst()
				.orElseThrow();

		assertTimeout(Duration.ofSeconds(5), () -> visitor.visit(getMethod(cls, "leaf")));

		MethodThrowsAttr propagated = getMethod(cls, "m18").get(AType.METHOD_THROWS);
		org.assertj.core.api.Assertions.assertThat(propagated).isNotNull();
		org.assertj.core.api.Assertions.assertThat(propagated.getList()).contains("java.io.FileNotFoundException");
	}
}
