package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.core.dex.visitors.blocks.BlockSplitter.connect;
import static org.assertj.core.api.Assertions.assertThat;

class SwitchRegionMakerTest {

	@Test
	void useExclusivelyOwnedTerminalPastSyntheticCaseBridge() {
		BlockNode caseBridge = new BlockNode(1, 0, 0);
		caseBridge.add(AFlag.SYNTHETIC);
		BlockNode terminal = new BlockNode(2, 0, 0);
		terminal.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		connect(caseBridge, terminal);
		caseBridge.updateCleanSuccessors();
		terminal.updateCleanSuccessors();

		assertThat(SwitchRegionMaker.resolveTerminalCaseEntry(caseBridge, null, null)).isSameAs(terminal);
	}

	@Test
	void retainCaseEntryForSharedTerminal() {
		BlockNode caseBridge = new BlockNode(1, 0, 0);
		caseBridge.add(AFlag.SYNTHETIC);
		BlockNode anotherPredecessor = new BlockNode(2, 0, 0);
		BlockNode terminal = new BlockNode(3, 0, 0);
		terminal.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		connect(caseBridge, terminal);
		connect(anotherPredecessor, terminal);
		caseBridge.updateCleanSuccessors();
		anotherPredecessor.updateCleanSuccessors();
		terminal.updateCleanSuccessors();

		assertThat(SwitchRegionMaker.resolveTerminalCaseEntry(caseBridge, null, null)).isSameAs(caseBridge);
	}

	@Test
	void retainFallThroughCaseEntry() {
		BlockNode caseBridge = new BlockNode(1, 0, 0);
		caseBridge.add(AFlag.SYNTHETIC);
		BlockNode terminal = new BlockNode(2, 0, 0);
		terminal.getInstructions().add(new InsnNode(InsnType.RETURN, 0));
		connect(caseBridge, terminal);
		caseBridge.updateCleanSuccessors();
		terminal.updateCleanSuccessors();

		assertThat(SwitchRegionMaker.resolveTerminalCaseEntry(caseBridge, null, terminal)).isSameAs(caseBridge);
	}
}
