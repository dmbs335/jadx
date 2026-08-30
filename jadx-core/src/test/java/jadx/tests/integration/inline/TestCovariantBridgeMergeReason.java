package jadx.tests.integration.inline;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.tests.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCovariantBridgeMergeReason extends IntegrationTest {

	public static class TestCls {
		public static class Base {
			public Base copy() {
				return this;
			}
		}

		public static class Middle extends Base {
			@Override
			public Middle copy() {
				return this;
			}
		}

		public static class Leaf extends Middle {
			@Override
			public Leaf copy() {
				return this;
			}
		}
	}

	@Test
	public void testMergeReasonIsRecordedForEveryCovariantBridge() {
		ClassNode cls = getClassNode(TestCls.class);
		assertMergeReason(searchCls(cls.getInnerClasses(), "Middle"));
		assertMergeReason(searchCls(cls.getInnerClasses(), "Leaf"));
	}

	private static void assertMergeReason(ClassNode cls) {
		MethodNode implementation = cls.getMethods().stream()
				.filter(mth -> mth.getName().equals("copy") && !mth.getAccessFlags().isBridge())
				.findFirst()
				.orElseThrow();
		assertThat(implementation.get(AType.RENAME_REASON))
				.isNotNull()
				.extracting(reason -> reason.getDescription())
				.isEqualTo("merged with bridge method [inline-methods]");
	}
}
