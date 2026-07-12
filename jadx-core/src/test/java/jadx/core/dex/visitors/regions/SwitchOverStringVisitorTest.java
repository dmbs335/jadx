package jadx.core.dex.visitors.regions;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class SwitchOverStringVisitorTest {

	@Test
	void testFindSwitchAfterEmptyBridge() {
		Region parent = new Region(null);
		SwitchRegion firstSwitch = makeSwitch(parent, 0);
		BlockNode emptyBridge = new BlockNode(1, 1, 1);
		SwitchRegion secondSwitch = makeSwitch(parent, 2);
		parent.add(firstSwitch);
		parent.add(emptyBridge);
		parent.add(secondSwitch);

		assertThat(SwitchOverStringVisitor.getSwitchAfterEmptyBridge(firstSwitch, emptyBridge))
				.isSameAs(secondSwitch);
	}

	@Test
	void testRejectNonEmptyBridge() {
		Region parent = new Region(null);
		SwitchRegion firstSwitch = makeSwitch(parent, 0);
		BlockNode bridge = new BlockNode(1, 1, 1);
		bridge.getInstructions().add(new InsnNode(InsnType.NOP, 0));
		SwitchRegion secondSwitch = makeSwitch(parent, 2);
		parent.add(firstSwitch);
		parent.add(bridge);
		parent.add(secondSwitch);

		assertThat(SwitchOverStringVisitor.getSwitchAfterEmptyBridge(firstSwitch, bridge)).isNull();
	}

	private static SwitchRegion makeSwitch(Region parent, int id) {
		BlockNode header = new BlockNode(id, id, id);
		SwitchInsn switchInsn = new SwitchInsn(InsnArg.lit(0, ArgType.INT), 0, true);
		header.getInstructions().add(switchInsn);
		return new SwitchRegion(parent, header, switchInsn);
	}
}
