package jadx.tests.integration.corpus;

import java.util.function.IntSupplier;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Semantic round-trip cases focused on evaluation timing, dispatch and JVM edge values. */
public class TestSemanticRoundTripCorpus5 extends IntegrationTest {

	public static class FloatingPointEdgeFlow {
		private int classify(double value) {
			int score = 0;
			if (!(value >= 0.0d)) {
				score += 1;
			}
			if (value == 0.0d) {
				score += 10;
			}
			if (1.0d / value == Double.NEGATIVE_INFINITY) {
				score += 100;
			}
			if (value != value) {
				score += 1000;
			}
			return score;
		}

		private boolean notAtMostZero(double value) {
			return !(value <= 0.0d);
		}

		public void check() {
			if (classify(Double.NaN) != 1001
					|| classify(-0.0d) != 110
					|| classify(0.0d) != 10
					|| classify(-1.0d) != 1
					|| !notAtMostZero(Double.NaN)
					|| notAtMostZero(-1.0d)
					|| notAtMostZero(0.0d)
					|| !notAtMostZero(1.0d)) {
				throw new AssertionError("floating-point edge values");
			}
		}
	}

	public static class ArrayCovarianceStoreFlow {
		private int trace;

		private int index() {
			trace = trace * 10 + 1;
			return 0;
		}

		private Object value(boolean incompatible) {
			trace = trace * 10 + 2;
			return incompatible ? Integer.valueOf(7) : "ok";
		}

		private int run(boolean incompatible) {
			String[] values = { "x" };
			Object[] alias = values;
			trace = 0;
			try {
				alias[index()] = value(incompatible);
				return trace * 10 + values[0].length();
			} catch (ArrayStoreException e) {
				return -(trace * 10 + values[0].length());
			}
		}

		public void check() {
			if (run(false) != 122 || run(true) != -121) {
				throw new AssertionError("array covariance store order");
			}
		}
	}

	public static class MethodReferenceReceiverFlow {
		private final class Target {
			private int read() {
				trace = trace * 10 + 3;
				return trace;
			}
		}

		private int trace;

		private Target target(boolean missing) {
			trace = trace * 10 + 1;
			return missing ? null : new Target();
		}

		private int run(boolean missing) {
			trace = 0;
			try {
				IntSupplier supplier = target(missing)::read;
				trace = trace * 10 + 2;
				return supplier.getAsInt();
			} catch (NullPointerException e) {
				return -trace;
			}
		}

		public void check() {
			if (run(false) != 123 || run(true) != -1) {
				throw new AssertionError("method-reference receiver timing");
			}
		}
	}

	public static class GenericBridgeDispatchFlow {
		private abstract static class Holder<T> {
			abstract T get();
		}

		private final class StringHolder extends Holder<String> {
			@Override
			String get() {
				trace = trace * 10 + 1;
				return "x" + trace;
			}
		}

		private int trace;

		private int run() {
			trace = 0;
			Holder<String> typed = new StringHolder();
			String first = typed.get();
			Object second = ((Holder<?>) typed).get();
			return trace * 100 + first.length() * 10 + second.toString().length();
		}

		public void check() {
			if (run() != 1123) {
				throw new AssertionError("generic bridge dispatch");
			}
		}
	}

	public static class ClassInitializationBoundaryFlow {
		private static int events;

		private static final class Probe {
			private static final int COMPILE_TIME = 7;
			private static final int RUNTIME = initialize();

			private static int initialize() {
				events = events * 10 + 3;
				return 9;
			}
		}

		private int run() {
			events = 0;
			int constant = Probe.COMPILE_TIME;
			int beforeInitialization = events;
			int runtime = Probe.RUNTIME;
			return constant * 1000 + beforeInitialization * 100 + events * 10 + runtime;
		}

		public void check() {
			if (run() != 7039) {
				throw new AssertionError("class initialization boundary");
			}
		}
	}

	public static class InterfaceDefaultSuperFlow {
		private interface Primary {
			default int value(InterfaceDefaultSuperFlow owner) {
				owner.trace = owner.trace * 10 + 1;
				return 4;
			}
		}

		private interface Secondary {
			default int value(InterfaceDefaultSuperFlow owner) {
				owner.trace = owner.trace * 10 + 9;
				return 8;
			}
		}

		private final class Combined implements Primary, Secondary {
			@Override
			public int value(InterfaceDefaultSuperFlow owner) {
				owner.trace = owner.trace * 10 + 2;
				return Primary.super.value(owner) + 3;
			}
		}

		private int trace;

		private int run() {
			trace = 0;
			Primary primary = new Combined();
			int result = primary.value(this);
			return trace * 10 + result;
		}

		public void check() {
			if (run() != 217) {
				throw new AssertionError("interface default-super dispatch");
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFloatingPointEdgeFlow() {
		getClassNode(FloatingPointEdgeFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testArrayCovarianceStoreFlow() {
		getClassNode(ArrayCovarianceStoreFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMethodReferenceReceiverFlow() {
		getClassNode(MethodReferenceReceiverFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testGenericBridgeDispatchFlow() {
		getClassNode(GenericBridgeDispatchFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testClassInitializationBoundaryFlow() {
		getClassNode(ClassInitializationBoundaryFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testInterfaceDefaultSuperFlow() {
		getClassNode(InterfaceDefaultSuperFlow.class);
	}
}
