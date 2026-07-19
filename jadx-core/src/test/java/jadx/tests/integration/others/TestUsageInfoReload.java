package jadx.tests.integration.others;

import org.junit.jupiter.api.Test;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUsageInfoReload extends IntegrationTest {

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
		MethodNode bTarget = getMethod(clsB, "target");
		MethodNode bCallA = getMethod(clsB, "callA");
		assertThat(bCallA.getUsed()).anyMatch(mth -> mth == oldATarget);
		assertThat(bTarget.getUseIn()).anyMatch(mth -> mth == oldACallB);

		clsA.deepUnload();

		MethodNode newATarget = getMethod(clsA, "target");
		MethodNode newACallB = getMethod(clsA, "callB");
		FieldNode newAField = getField(clsA, "value");
		assertThat(bCallA.getUsed()).anyMatch(mth -> mth == newATarget).noneMatch(mth -> mth == oldATarget);
		assertThat(bTarget.getUseIn()).anyMatch(mth -> mth == newACallB).noneMatch(mth -> mth == oldACallB);
		assertThat(newAField.getUseIn()).anyMatch(mth -> mth == bCallA);
	}
}
