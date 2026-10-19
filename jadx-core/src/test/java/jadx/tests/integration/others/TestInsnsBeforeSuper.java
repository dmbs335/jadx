package jadx.tests.integration.others;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInsnsBeforeSuper extends SmaliTest {
	@Override
	protected void runChecks(List<ClassNode> classes) {
		assertThat(classes.stream().flatMap(cls -> cls.getMethods().stream())
				.filter(mth -> mth.contains(AType.JADX_ERROR))
				.toList())
				.hasSize(1)
				.allMatch(mth -> mth.isConstructor());
	}
	// @formatter:off
	/*
		public class A {
			public A(String s) {
			}
		}

		public class B extends A {
			public B(String str) {
				checkNull(str);
				super(str);
			}

			public void checkNull(Object o) {
				if (o == null) {
					throw new NullPointerException();
				}
			}
		}
	*/
	// @formatter:on

	@Test
	public void test() {
		disableCompilation();
		// The null check can throw before the parent constructor in dex.
		// Reordering the two operations is not a semantics-preserving Java translation.
		assertThat(getClassNodeFromSmaliFiles("B"))
				.code()
				.contains("Cannot move 'super' call to the top without changing code semantics")
				.contains("JADX ERROR")
				.contains("Method not decompiled: others.B.<init>");
	}
}
