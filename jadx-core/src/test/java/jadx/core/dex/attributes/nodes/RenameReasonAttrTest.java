package jadx.core.dex.attributes.nodes;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AttrNode;

import static org.assertj.core.api.Assertions.assertThat;

class RenameReasonAttrTest {

	@Test
	void testAppendIsIdempotent() {
		RenameReasonAttr attr = new RenameReasonAttr();

		attr.append("").append("first").append("second").append("third").append("first").append("second");

		assertThat(attr.getDescription()).isEqualTo("first and second and third");
	}

	@Test
	void testEmptyDescription() {
		assertThat(new RenameReasonAttr().getDescription()).isEmpty();
	}

	@Test
	void testCopyHasIndependentAdditionalReasons() {
		AttrNode node = new AttrNode() {
		};
		RenameReasonAttr original = new RenameReasonAttr("first").append("second");
		node.addAttr(original);

		RenameReasonAttr copy = new RenameReasonAttr(node).append("third");

		assertThat(original.getDescription()).isEqualTo("first and second");
		assertThat(copy.getDescription()).isEqualTo("first and second and third");
	}
}
