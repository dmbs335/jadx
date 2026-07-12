package jadx.core.dex.visitors.regions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class CheckRegionsTest {

	@Test
	void testSkipGeneratedSyntheticDuplicate() {
		BlockNode exit = block(2);
		BlockNode generated = block(0);
		BlockNode duplicate = block(1);
		generated.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		duplicate.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		generated.getSuccessors().add(exit);
		duplicate.getSuccessors().add(exit);
		duplicate.add(AFlag.SYNTHETIC);

		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isTrue();
	}

	@Test
	void testKeepNonSyntheticOrDifferentPath() {
		BlockNode generated = block(0);
		BlockNode duplicate = block(1);
		generated.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		duplicate.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		generated.getSuccessors().add(block(2));
		duplicate.getSuccessors().add(block(3));

		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isFalse();
		duplicate.add(AFlag.SYNTHETIC);
		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isFalse();
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
