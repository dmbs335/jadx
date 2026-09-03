package jadx.core.dex.visitors.regions;

import java.util.List;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EdgeInsnAttr;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.regions.maker.SwitchRegionMaker;
import jadx.core.utils.RegionUtils;

public final class PostProcessRegions extends AbstractRegionVisitor {
	private static final IRegionVisitor INSTANCE = new PostProcessRegions();

	static void process(MethodNode mth) {
		DepthRegionTraversal.traverse(mth, INSTANCE);
	}

	@Override
	public void leaveRegion(MethodNode mth, IRegion region) {
		if (region instanceof LoopRegion) {
			// merge conditions in loops
			LoopRegion loop = (LoopRegion) region;
			loop.mergePreCondition();
		} else if (region instanceof SwitchRegion) {
			SwitchRegionMaker.insertBreaks(mth, (SwitchRegion) region);
		} else if (region instanceof Region) {
			insertEdgeInsn((Region) region);
		}
	}

	/**
	 * Insert insn block from edge insn attribute.
	 */
	private static void insertEdgeInsn(Region region) {
		List<IContainer> subBlocks = region.getSubBlocks();
		if (subBlocks.isEmpty()) {
			return;
		}
		IContainer last = subBlocks.get(subBlocks.size() - 1);
		if (RegionUtils.hasExitEdge(last)) {
			return;
		}
		List<EdgeInsnAttr> edgeInsnAttrs = last.getAll(AType.EDGE_INSN);
		if (edgeInsnAttrs.isEmpty()) {
			return;
		}
		EdgeInsnAttr insnAttr = findRegionExitInsn(region, last, edgeInsnAttrs);
		if (insnAttr == null) {
			return;
		}
		if (last instanceof BlockNode) {
			BlockNode block = (BlockNode) last;
			if (block.getInstructions().isEmpty()) {
				block.getInstructions().add(insnAttr.getInsn());
				return;
			}
		}
		region.add(new InsnContainer(insnAttr.getInsn()));
	}

	private static EdgeInsnAttr findRegionExitInsn(
			Region region, IContainer last, List<EdgeInsnAttr> edgeInsnAttrs) {
		for (EdgeInsnAttr edgeInsnAttr : edgeInsnAttrs) {
			if (edgeInsnAttr.getStart().equals(last)) {
				return edgeInsnAttr;
			}
		}
		if (!(last instanceof BlockNode)) {
			return null;
		}
		BlockNode lastBlock = (BlockNode) last;
		EdgeInsnAttr selected = null;
		for (BlockNode predecessor : lastBlock.getPredecessors()) {
			if (!RegionUtils.isRegionContainsBlock(region, predecessor)) {
				continue;
			}
			EdgeInsnAttr predecessorInsn = null;
			for (EdgeInsnAttr edgeInsnAttr : edgeInsnAttrs) {
				if (edgeInsnAttr.getStart() == predecessor && edgeInsnAttr.getEnd() == lastBlock) {
					predecessorInsn = edgeInsnAttr;
					break;
				}
			}
			if (predecessorInsn == null) {
				return null;
			}
			if (selected != null
					&& selected.getInsn().getType() != predecessorInsn.getInsn().getType()) {
				return null;
			}
			selected = predecessorInsn;
		}
		if (selected != null
				&& (selected.getInsn().getType() == InsnType.BREAK
						|| selected.getInsn().getType() == InsnType.CONTINUE)) {
			if (!targetsEnclosingLoop(region, selected)) {
				return null;
			}
		}
		return selected;
	}

	private static boolean targetsEnclosingLoop(Region region, EdgeInsnAttr edgeInsnAttr) {
		List<LoopInfo> targetLoops = edgeInsnAttr.getInsn().getAll(AType.LOOP);
		IRegion current = region;
		while (current != null) {
			if (current instanceof LoopRegion
					&& targetLoops.contains(((LoopRegion) current).getInfo())) {
				return true;
			}
			current = current.getParent();
		}
		for (LoopInfo targetLoop : targetLoops) {
			if (!RegionUtils.isRegionContainsBlock(region, targetLoop.getStart())) {
				return true;
			}
		}
		return false;
	}

	private PostProcessRegions() {
		// singleton
	}
}
