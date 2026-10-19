package jadx.tests.integration.others;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInsnsBeforeThis extends SmaliTest {
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
			public A(String str) {
				checkNull(str);
				this(str.length());
			}

			public A(int i) {
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
		// The original null check precedes the delegated constructor and may throw.
		assertThat(getClassNodeFromSmali())
				.code()
				.contains("Cannot move 'this' call to the top without changing code semantics")
				.contains("JADX ERROR")
				.contains("Method not decompiled: others.TestInsnsBeforeThis.<init>");
	}
}
