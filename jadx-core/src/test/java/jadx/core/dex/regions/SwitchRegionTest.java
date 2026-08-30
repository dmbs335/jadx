package jadx.core.dex.regions;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class SwitchRegionTest {

	@Test
	void testKeepSwitchInsnAfterHeaderRewrite() {
		BlockNode header = new BlockNode(0, 0, 0);
		SwitchInsn switchInsn = new SwitchInsn(InsnArg.lit(0, ArgType.INT), 0, true);
		header.getInstructions().add(switchInsn);

		SwitchRegion region = new SwitchRegion(null, header, switchInsn);
		header.getInstructions().clear();

		assertThat(region.getInsn()).isSameAs(switchInsn);
	}

	@Test
	void testReplaceCaseContainer() {
		BlockNode header = new BlockNode(0, 0, 0);
		SwitchInsn switchInsn = new SwitchInsn(InsnArg.lit(0, ArgType.INT), 0, true);
		SwitchRegion region = new SwitchRegion(null, header, switchInsn);
		Region oldCase = new Region(region);
		Region newCase = new Region(null);
		List<Object> keys = new ArrayList<>(List.of(1, 2));
		region.addCase(keys, oldCase);

		assertThat(region.replaceSubBlock(oldCase, newCase)).isTrue();
		assertThat(region.getCases().get(0).getContainer()).isSameAs(newCase);
		assertThat(region.getCases().get(0).getKeys()).isSameAs(keys);
		assertThat(newCase.getParent()).isSameAs(region);
	}
}
