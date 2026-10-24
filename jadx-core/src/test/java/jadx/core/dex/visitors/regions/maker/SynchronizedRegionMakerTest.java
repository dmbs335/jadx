package jadx.core.dex.visitors.regions.maker;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class SynchronizedRegionMakerTest {
	@Test
	void testKeepPostReleaseWorkInSameBlock() {
		BlockNode block = new BlockNode(0, 0, 0);
		assertThat(SynchronizedRegionMaker.canOmitMonitorExitBlock(block)).isFalse();

		InsnNode release = new InsnNode(InsnType.MONITOR_EXIT, 0);
		release.add(AFlag.DONT_GENERATE);
		block.getInstructions().add(release);
		assertThat(SynchronizedRegionMaker.canOmitMonitorExitBlock(block)).isTrue();

		block.getInstructions().add(new InsnNode(InsnType.SPUT, 0));
		assertThat(SynchronizedRegionMaker.canOmitMonitorExitBlock(block)).isFalse();
	}
}
