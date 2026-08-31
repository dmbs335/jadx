package jadx.core.dex.visitors.regions;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.regions.maker.ExcHandlersRegionMaker;
import jadx.core.dex.visitors.regions.maker.RegionMaker;
import jadx.core.dex.visitors.regions.maker.SynchronizedRegionMaker;
import jadx.core.dex.visitors.shrink.CodeShrinkVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.exceptions.JadxException;

@JadxVisitor(
		name = "RegionMakerVisitor",
		desc = "Pack blocks into regions for code generation"
)
public class RegionMakerVisitor extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode() || mth.getBasicBlocks().isEmpty()) {
			return;
		}
		try (BlockUtils.PathCacheScope ignored = BlockUtils.enterPathCache()) {
			RegionMaker rm = new RegionMaker(mth);
			mth.setRegion(rm.makeMthRegion());
			if (!mth.isNoExceptionHandlers()) {
				new ExcHandlersRegionMaker(mth, rm).process();
			}
		}
		processForceInlineInsns(mth);
		ProcessTryCatchRegions.process(mth);
		PostProcessRegions.process(mth);
		CleanRegions.process(mth);
		if (mth.getAccessFlags().isSynchronized()) {
			SynchronizedRegionMaker.removeSynchronized(mth);
		}
	}

	private static void processForceInlineInsns(MethodNode mth) {
		var blocks = mth.getBasicBlocks();
		for (int blockIndex = 0, blocksCount = blocks.size(); blockIndex < blocksCount; blockIndex++) {
			BlockNode block = blocks.get(blockIndex);
			var insns = block.getInstructions();
			for (int insnIndex = 0, insnsCount = insns.size(); insnIndex < insnsCount; insnIndex++) {
				InsnNode insn = insns.get(insnIndex);
				if (insn.contains(AFlag.FORCE_ASSIGN_INLINE)) {
					CodeShrinkVisitor.shrinkMethod(mth);
					return;
				}
			}
		}
	}

	@Override
	public String getName() {
		return "RegionMakerVisitor";
	}
}
