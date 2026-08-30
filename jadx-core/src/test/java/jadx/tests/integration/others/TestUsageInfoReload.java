package jadx.tests.integration.others;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.RenameReasonAttr;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUsageInfoReload extends IntegrationTest {
	public interface Callback {
		int call();
	}

	public static class AnonymousReloadCls {
		public static Callback select(boolean first) {
			Callback callback;
			if (first) {
				callback = new Callback() {
					@Override
					public int call() {
						return 1;
					}
				};
			} else {
				callback = new Callback() {
					@Override
					public int call() {
						return 2;
					}
				};
			}
			return callback;
		}
	}

	public static class AnonymousCaptureReloadCls {
		public static Callback capture(String value) {
			return new Callback() {
				@Override
				public int call() {
					return value.length();
				}
			};
		}
	}

	public static class ConstantReloadCls {
		private static final String TAG = "reload-constant-tag";

		public static int first() {
			return "reload-constant-tag".length();
		}

		public static String second() {
			return "reload-constant-tag";
		}
	}

	public static class ThrowsReloadCls {
		public static void source() throws IOException {
			throw new IOException("test");
		}

		public static void caller() throws IOException {
			source();
		}
	}

	public static class TestCls {
		private int value;

		public int caller(int arg) {
			return callee(arg) + value;
		}

		public int callee(int arg) {
			return arg + 1;
		}

		public int recursive(int arg) {
			return arg == 0 ? 0 : recursive(arg - 1);
		}

		public static class A {
			public static int value;

			public static int target(int arg) {
				return arg + 1;
			}

			public static int callB(int arg) {
				return B.target(arg);
			}

			public static int callBAgain(int arg) {
				return B.target(arg + 1);
			}
		}

		public static class B {
			public static int target(int arg) {
				return arg + 2;
			}

			public static int callA(int arg) {
				return A.target(arg) + A.value;
			}
		}
	}

	@Test
	public void testUsageRelationsSurviveDeepReload() {
		ClassNode cls = getClassNode(TestCls.class);
		MethodNode oldCaller = getMethod(cls, "caller");
		MethodNode oldCallee = getMethod(cls, "callee");
		MethodNode oldRecursive = getMethod(cls, "recursive");
		FieldNode oldField = getField(cls, "value");

		assertThat(oldCallee.getUseIn()).contains(oldCaller);
		assertThat(oldCaller.getUsed()).contains(oldCallee);
		assertThat(oldField.getUseIn()).contains(oldCaller);
		assertThat(oldRecursive.callsSelf()).isTrue();

		cls.deepUnload();

		MethodNode newCaller = getMethod(cls, "caller");
		MethodNode newCallee = getMethod(cls, "callee");
		MethodNode newRecursive = getMethod(cls, "recursive");
		FieldNode newField = getField(cls, "value");

		assertThat(newCaller).isNotSameAs(oldCaller);
		assertThat(newCallee).isNotSameAs(oldCallee);
		assertThat(newField).isNotSameAs(oldField);
		assertThat(newCallee.getUseIn()).anyMatch(mth -> mth == newCaller).noneMatch(mth -> mth == oldCaller);
		assertThat(newCaller.getUsed()).anyMatch(mth -> mth == newCallee).noneMatch(mth -> mth == oldCallee);
		assertThat(newField.getUseIn()).anyMatch(mth -> mth == newCaller).noneMatch(mth -> mth == oldCaller);
		assertThat(newRecursive.callsSelf()).isTrue();
	}

	@Test
	public void testExternalUsageRelationsPointToReloadedMethods() {
		ClassNode cls = getClassNode(TestCls.class);
		ClassNode clsA = cls.getInnerClasses().stream()
				.filter(inner -> inner.getName().equals("A"))
				.findFirst()
				.orElseThrow();
		ClassNode clsB = cls.getInnerClasses().stream()
				.filter(inner -> inner.getName().equals("B"))
				.findFirst()
				.orElseThrow();

		MethodNode oldATarget = getMethod(clsA, "target");
		MethodNode oldACallB = getMethod(clsA, "callB");
		MethodNode oldACallBAgain = getMethod(clsA, "callBAgain");
		MethodNode bTarget = getMethod(clsB, "target");
		MethodNode bCallA = getMethod(clsB, "callA");
		assertThat(bCallA.getUsed()).anyMatch(mth -> mth == oldATarget);
		assertThat(bTarget.getUseIn()).anyMatch(mth -> mth == oldACallB);

		clsA.deepUnload();

		MethodNode newATarget = getMethod(clsA, "target");
		MethodNode newACallB = getMethod(clsA, "callB");
		MethodNode newACallBAgain = getMethod(clsA, "callBAgain");
		FieldNode newAField = getField(clsA, "value");
		assertThat(bCallA.getUsed()).anyMatch(mth -> mth == newATarget).noneMatch(mth -> mth == oldATarget);
		assertThat(bTarget.getUseIn()).anyMatch(mth -> mth == newACallB).noneMatch(mth -> mth == oldACallB);
		assertThat(bTarget.getUseIn())
				.anyMatch(mth -> mth == newACallBAgain)
				.noneMatch(mth -> mth == oldACallBAgain);
		assertThat(newAField.getUseIn()).anyMatch(mth -> mth == bCallA);
	}

	@Test
	public void testMethodNamePrefilterIgnoresIncompleteUsageEntry() {
		ClassNode cls = getClassNode(TestCls.class);
		MethodNode caller = getMethod(cls, "caller");
		MethodNode callee = getMethod(cls, "callee");

		caller.setUsed(Arrays.asList(null, callee));

		assertThat(caller.referencesMethodNamed("callee")).isTrue();
		assertThat(caller.referencesMethodNamed("missing")).isFalse();
	}

	@Test
	public void testRenameReasonsSurviveDeepReload() {
		ClassNode cls = getClassNode(TestCls.class);
		FieldNode field = getField(cls, "value");
		MethodNode method = getMethod(cls, "caller");
		RenameReasonAttr.forNode(cls).append("class reason");
		RenameReasonAttr.forNode(field).append("field reason");
		RenameReasonAttr.forNode(method).append("method reason");

		cls.deepUnload();

		assertThat(cls.get(AType.RENAME_REASON).getDescription()).isEqualTo("class reason");
		assertThat(getField(cls, "value").get(AType.RENAME_REASON).getDescription()).isEqualTo("field reason");
		assertThat(getMethod(cls, "caller").get(AType.RENAME_REASON).getDescription()).isEqualTo("method reason");
	}

	@Test
	public void testAnonymousVariableNameStableOnReload() {
		ClassNode cls = getClassNode(AnonymousReloadCls.class);

		String first = cls.getCode().getCodeStr();
		String second = cls.reloadCode().getCodeStr();

		assertThat(second).isEqualTo(first);
	}

	@Test
	public void testAnonymousCapturedArgumentFinalStableOnReload() {
		ClassNode cls = getClassNode(AnonymousCaptureReloadCls.class);

		String first = cls.getCode().getCodeStr();
		String second = cls.reloadCode().getCodeStr();

		assertThat(first).contains("capture(final String value)");
		assertThat(second).isEqualTo(first);
	}

	@Test
	public void testRestoredConstantsStableOnReload() {
		ClassNode cls = getClassNode(ConstantReloadCls.class);

		String first = cls.getCode().getCodeStr();
		String second = cls.reloadCode().getCodeStr();

		assertThat(first).contains("return TAG");
		assertThat(second).isEqualTo(first);
	}

	@Test
	public void testInferredThrowsSurviveDeepReload() {
		ClassNode cls = getClassNode(ThrowsReloadCls.class);
		MethodNode oldCaller = getMethod(cls, "caller");
		ArgType ioException = ArgType.object(IOException.class.getName());
		assertThat(oldCaller.getThrows()).containsExactlyInAnyOrder(ioException);

		cls.deepUnload();

		MethodNode newCaller = getMethod(cls, "caller");
		assertThat(newCaller).isNotSameAs(oldCaller);
		assertThat(newCaller.getThrows()).containsExactlyInAnyOrder(ioException);
	}
}
