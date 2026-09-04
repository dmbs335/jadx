package jadx.tests.integration.corpus;

import jadx.tests.api.IntegrationTest;
import jadx.tests.api.extensions.profiles.TestProfile;
import jadx.tests.api.extensions.profiles.TestWithProfiles;

/** Additional semantic round-trip cases focused on expression evaluation and nested exits. */
public class TestSemanticRoundTripCorpus2 extends IntegrationTest {

	public static class FieldReceiverOrderFlow {
		private static class Box {
			private int value;

			Box(int value) {
				this.value = value;
			}
		}

		private int trace;
		private Box active;

		private Box receiver() {
			trace = trace * 10 + 1;
			return active;
		}

		private int replacement(Box next, boolean fail) {
			trace = trace * 10 + 2;
			active = next;
			if (fail) {
				throw new IllegalStateException("rhs");
			}
			return 3;
		}

		private String run(boolean fail) {
			Box first = new Box(5);
			Box second = new Box(40);
			active = first;
			trace = 0;
			try {
				receiver().value += replacement(second, fail);
				return first.value + ":" + active.value + ":" + trace;
			} catch (IllegalStateException e) {
				return first.value + ":" + active.value + ":" + trace;
			} finally {
				trace = trace * 10 + 3;
			}
		}

		public void check() {
			String success = run(false);
			if (!success.equals("8:40:12") || trace != 123) {
				throw new AssertionError("field receiver success: " + success + '/' + trace);
			}
			String failure = run(true);
			if (!failure.equals("5:40:12") || trace != 123) {
				throw new AssertionError("field receiver failure: " + failure + '/' + trace);
			}
		}
	}

	public static class NestedBooleanLoopExitFlow {
		private long trace;

		private boolean step(int marker, boolean value) {
			trace = trace * 10 + marker;
			return value;
		}

		private int run(int seed) {
			int result = 0;
			trace = 0;
			outer: for (int i = 0; i < 4; i++) {
				try {
					boolean selected = (step(1, i > seed) && step(2, i != 2))
							|| (step(3, i == seed) && step(4, seed > 0));
					if (selected) {
						result += 10 + i;
						continue;
					}
					if (i + seed > 4) {
						break outer;
					}
					result -= i;
				} finally {
					result += 1;
					trace = trace * 10 + 5;
				}
			}
			return result;
		}

		public void check() {
			int first = run(1);
			if (first != 26 || trace != 13513451235125L) {
				throw new AssertionError("nested boolean loop first: " + first + '/' + trace);
			}
			int second = run(3);
			if (second != 2 || trace != 135135135L) {
				throw new AssertionError("nested boolean loop second: " + second + '/' + trace);
			}
		}
	}

	public static class TernaryUnboxingFlow {
		private int trace;

		private Integer value(int marker, Integer result) {
			trace = trace * 10 + marker;
			return result;
		}

		private int run(int mode) {
			trace = 0;
			try {
				int result = mode == 0
						? value(1, 7)
						: mode == 1 ? value(2, null) : value(3, 9);
				return result + trace;
			} catch (NullPointerException e) {
				trace = trace * 10 + 4;
				return -trace;
			} finally {
				trace = trace * 10 + 5;
			}
		}

		public void check() {
			int first = run(0);
			if (first != 8 || trace != 15) {
				throw new AssertionError("ternary first: " + first + '/' + trace);
			}
			int second = run(1);
			if (second != -24 || trace != 245) {
				throw new AssertionError("ternary second: " + second + '/' + trace);
			}
			int third = run(2);
			if (third != 12 || trace != 35) {
				throw new AssertionError("ternary third: " + third + '/' + trace);
			}
		}
	}

	public static class CollisionSwitchLoopFlow {
		private int run(String[] values) {
			int result = 0;
			outer: for (int i = 0; i < values.length; i++) {
				switch (values[i]) {
					case "FB":
						result = result * 10 + 1;
						continue;
					case "Ea":
						result = result * 10 + 2;
						break;
					case "Aa":
						result = result * 10 + 3;
						continue outer;
					case "BB":
						result = result * 10 + 4;
						break outer;
					default:
						result = result * 10 + 5;
				}
				result++;
			}
			return result;
		}

		public void check() {
			int value = run(new String[] { "FB", "Ea", "Aa", "x", "BB", "FB" });
			if (value != 13364) {
				throw new AssertionError("collision switch loop: " + value);
			}
		}
	}

	public static class MultiArrayOrderFlow {
		private int trace;
		private int row;
		private int column;
		private int[][] active;

		private int nextRow() {
			trace = trace * 10 + 1;
			return row++;
		}

		private int nextColumn() {
			trace = trace * 10 + 2;
			return column++;
		}

		private int replace(int[][] next) {
			trace = trace * 10 + 3;
			active = next;
			return next[0][0];
		}

		private String run() {
			int[][] first = { { 10, 20 }, { 30, 40 } };
			int[][] second = { { 7, 8 } };
			active = first;
			trace = 0;
			row = 0;
			column = 1;
			active[nextRow()][nextColumn()] += replace(second);
			return first[0][1] + ":" + active[0][0] + ":" + row + ":" + column + ":" + trace;
		}

		public void check() {
			String actual = run();
			if (!actual.equals("27:7:1:2:123")) {
				throw new AssertionError("multi array order: " + actual);
			}
		}
	}

	public static class CovariantBridgeFlow {
		private interface Source<T> {
			T get(boolean fail);
		}

		private static class TextSource implements Source<String> {
			private int calls;

			@Override
			public String get(boolean fail) {
				calls++;
				if (fail) {
					throw new IllegalArgumentException("fail");
				}
				return "v" + calls;
			}
		}

		private String run(boolean fail) {
			TextSource source = new TextSource();
			Source<?> generic = source;
			try {
				return generic.get(fail) + ":" + source.calls;
			} catch (IllegalArgumentException e) {
				return e.getMessage() + ":" + source.calls;
			}
		}

		public void check() {
			String success = run(false);
			String failure = run(true);
			if (!success.equals("v1:1") || !failure.equals("fail:1")) {
				throw new AssertionError("covariant bridge: " + success + '/' + failure);
			}
		}
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testFieldReceiverOrderFlow() {
		getClassNode(FieldReceiverOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testNestedBooleanLoopExitFlow() {
		getClassNode(NestedBooleanLoopExitFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testTernaryUnboxingFlow() {
		getClassNode(TernaryUnboxingFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCollisionSwitchLoopFlow() {
		getClassNode(CollisionSwitchLoopFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testMultiArrayOrderFlow() {
		getClassNode(MultiArrayOrderFlow.class);
	}

	@TestWithProfiles({ TestProfile.DX_J8, TestProfile.D8_J8, TestProfile.JAVA8 })
	public void testCovariantBridgeFlow() {
		getClassNode(CovariantBridgeFlow.class);
	}
}
