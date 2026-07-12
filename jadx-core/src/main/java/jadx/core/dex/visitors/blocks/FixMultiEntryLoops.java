package jadx.core.dex.visitors.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr;
import jadx.core.dex.attributes.nodes.SpecialEdgeAttr.SpecialEdgeType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.ListUtils;

public class FixMultiEntryLoops {
	static boolean hasMultiEntryLoops(MethodNode mth) {
		detectSpecialEdges(mth);
		return mth.getAll(AType.SPECIAL_EDGE).stream()
				.anyMatch(FixMultiEntryLoops::isMultiEntryLoop);
	}

	public static boolean process(MethodNode mth) {
		try {
			detectSpecialEdges(mth);
		} catch (Exception e) {
			mth.addWarnComment("Failed to detect multi-entry loops", e);
			return false;
		}
		List<SpecialEdgeAttr> specialEdges = mth.getAll(AType.SPECIAL_EDGE);
		List<SpecialEdgeAttr> multiEntryLoops = specialEdges.stream()
				.filter(FixMultiEntryLoops::isMultiEntryLoop)
				.collect(Collectors.toList());
		if (multiEntryLoops.isEmpty()) {
			return false;
		}
		try {
			List<SpecialEdgeAttr> crossEdges = ListUtils.filter(specialEdges, e -> e.getType() == SpecialEdgeType.CROSS_EDGE);
			boolean changed = false;
			for (SpecialEdgeAttr backEdge : multiEntryLoops) {
				changed |= fixLoop(mth, backEdge, crossEdges);
			}
			return changed;
		} catch (Exception e) {
			mth.addWarnComment("Failed to fix multi-entry loops", e);
			return false;
		}
	}

	private static boolean fixLoop(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		if (splitDirectHeaderEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		if (isHeaderSuccessorEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		if (isEndBlockEntry(mth, backEdge, crossEdges)) {
			return true;
		}
		mth.addWarnComment("Unsupported multi-entry loop pattern (" + backEdge + "). Please report as a decompilation issue!!!");
		return false;
	}

	private static boolean splitDirectHeaderEntry(
			MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		BlockNode header = backEdge.getEnd();
		BlockNode loopEnd = backEdge.getStart();
		BlockNode externalEntry = ListUtils.filterOnlyOne(header.getPredecessors(), pred -> pred != loopEnd);
		if (externalEntry == null || header.getPredecessors().size() != 2) {
			return false;
		}
		BlockNode newHeader = findAlternateHeader(backEdge.getStart(), header);
		if (newHeader == null || !BlockUtils.isPathExists(header, newHeader)) {
			return false;
		}
		Set<BlockNode> toDuplicate = collectUntilBoundary(header, newHeader);
		if (toDuplicate.size() < 2 || !toDuplicate.contains(header)) {
			return false;
		}
		Map<BlockNode, BlockNode> copies = new HashMap<>();
		for (BlockNode block : toDuplicate) {
			BlockNode copy = BlockSplitter.startNewBlock(mth, block.getStartOffset());
			copy.add(AFlag.SYNTHETIC);
			BlockSplitter.copyBlockData(block, copy);
			copies.put(block, copy);
		}
		for (BlockNode block : toDuplicate) {
			BlockNode copy = copies.get(block);
			for (BlockNode successor : block.getSuccessors()) {
				BlockSplitter.connect(copy, copies.getOrDefault(successor, successor));
			}
			copy.updateCleanSuccessors();
		}
		BlockSplitter.replaceConnection(externalEntry, header, copies.get(header));
		mth.addDebugComment("Duplicate loop entry subgraph (" + toDuplicate.size()
				+ " blocks) to fix direct multi-entry loop: " + backEdge);
		return true;
	}

	private static @Nullable BlockNode findAlternateHeader(BlockNode loopEnd, BlockNode originalHeader) {
		BlockNode block = loopEnd;
		Set<BlockNode> visited = new HashSet<>();
		while (block != originalHeader && visited.add(block)) {
			List<BlockNode> predecessors = block.getPredecessors();
			if (predecessors.size() != 1) {
				return block;
			}
			block = predecessors.get(0);
		}
		return null;
	}

	private static Set<BlockNode> collectUntilBoundary(BlockNode start, BlockNode boundary) {
		Set<BlockNode> blocks = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (block == boundary || block.contains(AFlag.MTH_EXIT_BLOCK) || !blocks.add(block)) {
				continue;
			}
			queue.addAll(new ArrayList<>(block.getSuccessors()));
		}
		return blocks;
	}

	private static boolean isHeaderSuccessorEntry(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		BlockNode header = backEdge.getEnd();
		BlockNode headerIDom = header.getIDom();
		SpecialEdgeAttr subEntry = ListUtils.filterOnlyOne(crossEdges, e -> e.getStart() == headerIDom);
		if (subEntry == null || !ListUtils.isSingleElement(header.getSuccessors(), subEntry.getEnd())) {
			subEntry = ListUtils.filterOnlyOne(crossEdges,
					e -> header.getSuccessors().contains(e.getEnd())
							&& isPathExists(e.getEnd(), backEdge.getStart()));
		}
		if (subEntry == null || !header.getSuccessors().contains(subEntry.getEnd())) {
			return false;
		}
		BlockNode loopEnd = backEdge.getStart();
		BlockNode subEntryBlock = subEntry.getEnd();
		BlockNode copyHeader = BlockSplitter.insertBlockBetween(mth, loopEnd, header);
		BlockSplitter.copyBlockData(header, copyHeader);
		BlockSplitter.replaceConnection(copyHeader, header, subEntryBlock);
		for (BlockNode successor : header.getSuccessors()) {
			if (successor != subEntryBlock) {
				BlockSplitter.connect(copyHeader, successor);
			}
		}
		mth.addDebugComment("Duplicate block (" + header + ") to fix multi-entry loop: " + backEdge);
		return true;
	}

	private static boolean isPathExists(BlockNode start, BlockNode target) {
		Set<BlockNode> visited = new HashSet<>();
		ArrayDeque<BlockNode> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockNode block = queue.removeFirst();
			if (block == target) {
				return true;
			}
			if (visited.add(block)) {
				queue.addAll(block.getSuccessors());
			}
		}
		return false;
	}

	private static boolean isEndBlockEntry(MethodNode mth, SpecialEdgeAttr backEdge, List<SpecialEdgeAttr> crossEdges) {
		BlockNode loopEnd = backEdge.getStart();
		SpecialEdgeAttr subEntry = ListUtils.filterOnlyOne(crossEdges, e -> e.getEnd() == loopEnd);
		if (subEntry == null) {
			return false;
		}
		dupPath(mth, subEntry.getStart(), loopEnd, backEdge.getEnd());
		mth.addDebugComment("Duplicate block (" + loopEnd + ") to fix multi-entry loop: " + backEdge);
		return true;
	}

	/**
	 * Duplicate 'center' block on path from 'start' to 'end'
	 */
	private static void dupPath(MethodNode mth, BlockNode start, BlockNode center, BlockNode end) {
		BlockNode copyCenter = BlockSplitter.insertBlockBetween(mth, start, center);
		BlockSplitter.copyBlockData(center, copyCenter);
		BlockSplitter.replaceConnection(copyCenter, center, end);
	}

	private static boolean isSingleEntryLoop(SpecialEdgeAttr e) {
		BlockNode header = e.getEnd();
		BlockNode loopEnd = e.getStart();
		return header == loopEnd
				|| loopEnd.getDoms().get(header.getId()); // header dominates loop end
	}

	static boolean isMultiEntryLoop(SpecialEdgeAttr edge) {
		return edge.getType() == SpecialEdgeType.BACK_EDGE
				&& !BlockUtils.isExceptionHandlerPath(edge.getStart())
				&& !BlockUtils.isExceptionHandlerPath(edge.getEnd())
				&& !isSingleEntryLoop(edge);
	}

	private enum BlockColor {
		WHITE, GRAY, BLACK
	}

	private static void detectSpecialEdges(MethodNode mth) {
		mth.remove(AType.SPECIAL_EDGE);
		BlockColor[] colors = new BlockColor[mth.getBasicBlocks().size()];
		Arrays.fill(colors, BlockColor.WHITE);
		colorDFS(mth, colors, mth.getEnterBlock());
	}

	// TODO: transform to non-recursive form
	private static void colorDFS(MethodNode mth, BlockColor[] colors, BlockNode block) {
		colors[block.getId()] = BlockColor.GRAY;
		for (BlockNode v : block.getSuccessors()) {
			switch (colors[v.getId()]) {
				case WHITE:
					colorDFS(mth, colors, v);
					break;
				case GRAY:
					mth.addAttr(AType.SPECIAL_EDGE, new SpecialEdgeAttr(SpecialEdgeType.BACK_EDGE, block, v));
					break;
				case BLACK:
					mth.addAttr(AType.SPECIAL_EDGE, new SpecialEdgeAttr(SpecialEdgeType.CROSS_EDGE, block, v));
					break;
			}
		}
		colors[block.getId()] = BlockColor.BLACK;
	}
}
