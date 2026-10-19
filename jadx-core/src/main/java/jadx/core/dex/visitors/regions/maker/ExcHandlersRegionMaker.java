package jadx.core.dex.visitors.regions.maker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.NamedArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
import jadx.core.dex.visitors.kotlin.CoroutineMethodUtils;
import jadx.core.dex.visitors.regions.ProcessTryCatchRegions;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.RegionUtils;

public class ExcHandlersRegionMaker {
	private final MethodNode mth;
	private final RegionMaker regionMaker;

	public ExcHandlersRegionMaker(MethodNode mth, RegionMaker regionMaker) {
		this.mth = mth;
		this.regionMaker = regionMaker;
	}

	public void process() {
		if (mth.isNoExceptionHandlers()) {
			return;
		}
		IRegion excOutBlock = collectHandlerRegions();
		if (excOutBlock != null) {
			mth.getRegion().add(excOutBlock);
		}
	}

	private @Nullable IRegion collectHandlerRegions() {
		List<TryCatchBlockAttr> tcs = mth.getAll(AType.TRY_BLOCKS_LIST);
		restoreSharedRetryTails(mth.getRegion());
		for (TryCatchBlockAttr tc : tcs) {
			List<BlockNode> blocks = new ArrayList<>(tc.getHandlersCount());
			Set<BlockNode> splitters = new HashSet<>();
			for (ExceptionHandler handler : tc.getHandlers()) {
				BlockNode handlerBlock = handler.getHandlerBlock();
				if (handlerBlock != null) {
					blocks.add(handlerBlock);
					BlockNode splitter = BlockUtils.findTopSplitterForHandler(handlerBlock);
					if (splitter != null) {
						splitters.add(splitter);
					} else {
						mth.addDebugComment("No top splitter for exception handler: " + handler);
					}
				} else {
					mth.addDebugComment("No exception handler block: " + handler);
				}
			}
			Set<BlockNode> exits = new HashSet<>();
			for (BlockNode splitter : splitters) {
				for (BlockNode handler : blocks) {
					if (handler.contains(AFlag.REMOVE)) {
						continue;
					}
					List<BlockNode> s = splitter.getSuccessors();
					if (s.isEmpty()) {
						mth.addDebugComment("No successors for splitter: " + splitter);
						continue;
					}
					BlockNode ss = s.get(0);
					BlockNode cross = BlockUtils.getPathCross(mth, ss, handler);
					if (cross != null && cross != ss && cross != handler) {
						exits.add(cross);
					}
				}
			}
			for (ExceptionHandler handler : tc.getHandlers()) {
				processExcHandler(handler, exits);
			}
		}
		return processHandlersOutBlocks(tcs);
	}

	private void restoreSharedRetryTails(IRegion region) {
		for (IContainer child : new ArrayList<>(region.getSubBlocks())) {
			if (child instanceof IRegion) {
				restoreSharedRetryTails((IRegion) child);
			}
		}
		if (!(region instanceof LoopRegion) || !((LoopRegion) region).isEndless()) {
			return;
		}
		LoopInfo loop = ((LoopRegion) region).getInfo();
		IContainer bodyContainer = ((LoopRegion) region).getBody();
		if (!(bodyContainer instanceof Region)) {
			return;
		}
		Region body = (Region) bodyContainer;
		BlockNode end = loop.getEnd();
		List<IContainer> children = body.getSubBlocks();
		if (children.isEmpty() || children.get(children.size() - 1) != end) {
			return;
		}
		BlockNode join = end;
		Set<BlockNode> visited = new HashSet<>();
		while (join.getPredecessors().size() == 1 && visited.add(join)) {
			join = join.getPredecessors().get(0);
		}
		if (join == end || join.getPredecessors().size() < 2) {
			return;
		}
		Set<BlockNode> handlerStarts = new HashSet<>();
		for (BlockNode pred : join.getPredecessors()) {
			BlockNode start = findMoveOnlyHandlerStart(pred, join);
			if (start == null || !loop.getLoopBlocks().contains(start)) {
				return;
			}
			handlerStarts.add(start);
		}
		if (handlerStarts.size() < 2) {
			return;
		}
		if (BlockUtils.isPathExists(loop.getStart(), end) || BlockUtils.isPathExists(loop.getStart(), join)) {
			return;
		}
		Set<IBlock> mainBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(mth.getRegion(), mainBlocks);
		if (!isSharedRetryTail(join, end, mth.getExitBlock(), mainBlocks)) {
			return;
		}
		// The normal loop body terminates; only catch entry moves reach this common tail.
		// Emit it once, outside the protected regions, before the already appended back edge.
		RegionStack stack = regionMaker.getStack().clear();
		stack.push(body);
		stack.addExit(end);
		stack.addExit(mth.getExitBlock());
		Region sharedTail = regionMaker.makeRegion(join);
		children.add(children.size() - 1, sharedTail);
		stack.clear();
	}

	static boolean isSharedRetryTail(BlockNode start, BlockNode end, BlockNode methodExit, Set<IBlock> mainBlocks) {
		Set<BlockNode> tail = new HashSet<>();
		ArrayDeque<BlockNode> pending = new ArrayDeque<>();
		pending.add(start);
		boolean reachesEnd = false;
		while (!pending.isEmpty()) {
			BlockNode block = pending.removeFirst();
			if (block == end) {
				reachesEnd = true;
				continue;
			}
			if (!tail.add(block)) {
				continue;
			}
			if (block == methodExit || mainBlocks.contains(block)
					|| block.contains(AType.EXC_CATCH) || block.contains(AType.EXC_HANDLER)
					|| block.contains(AFlag.LOOP_START) || block.contains(AFlag.LOOP_END)
					|| block.contains(AFlag.DONT_GENERATE) || block.contains(AFlag.REMOVE)) {
				return false;
			}
			InsnNode last = BlockUtils.getLastInsn(block);
			if (last != null && (last.getType() == InsnType.THROW || last.getType() == InsnType.RETURN)) {
				if (last.contains(AFlag.DONT_GENERATE)) {
					return false;
				}
				continue;
			}
			if (block.getCleanSuccessors().isEmpty()) {
				return false;
			}
			pending.addAll(block.getCleanSuccessors());
		}
		if (!reachesEnd || !tail.containsAll(end.getPredecessors())) {
			return false;
		}
		// No second entry or hidden cycle may be introduced into the emitted statement sequence.
		for (BlockNode block : tail) {
			if (block != start && !tail.containsAll(block.getPredecessors())) {
				return false;
			}
		}
		Set<BlockNode> emitted = new HashSet<>();
		pending.add(start);
		while (!pending.isEmpty()) {
			BlockNode block = pending.removeFirst();
			if (!emitted.add(block)) {
				continue;
			}
			for (BlockNode next : block.getCleanSuccessors()) {
				if (tail.contains(next) && next != start && emitted.containsAll(next.getPredecessors())) {
					pending.add(next);
				}
			}
		}
		return emitted.size() == tail.size()
				&& start.getPredecessors().stream().noneMatch(tail::contains);
	}

	static @Nullable BlockNode findMoveOnlyHandlerStart(BlockNode block, BlockNode next) {
		Set<BlockNode> visited = new HashSet<>();
		while (visited.add(block)) {
			if (block.getCleanSuccessors().size() != 1 || block.getCleanSuccessors().get(0) != next
					|| block.getInstructions().stream().anyMatch(insn -> insn.getType() != InsnType.MOVE)) {
				return null;
			}
			if (block.contains(AType.EXC_HANDLER)) {
				return block.get(AType.EXC_HANDLER).getHandler().isFinally() ? null : block;
			}
			if (block.getPredecessors().size() != 1) {
				return null;
			}
			next = block;
			block = block.getPredecessors().get(0);
		}
		return null;
	}

	/**
	 * Search handlers successor blocks aren't included in any region.
	 */
	private @Nullable IRegion processHandlersOutBlocks(List<TryCatchBlockAttr> tcs) {
		Set<IBlock> mainRegionBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(mth.getRegion(), mainRegionBlocks);
		Set<IBlock> allRegionBlocks = new HashSet<>(mainRegionBlocks);

		Set<IBlock> successorBlocks = new HashSet<>();
		Set<IBlock> resourceSuccessorBlocks = new HashSet<>();
		Set<IBlock> directTerminalBlocks = new HashSet<>();
		for (TryCatchBlockAttr tc : tcs) {
			Set<IBlock> trySuccessorBlocks = new HashSet<>();
			for (ExceptionHandler handler : tc.getHandlers()) {
				IContainer region = handler.getHandlerRegion();
				if (region != null) {
					IBlock lastBlock = RegionUtils.getLastBlock(region);
					if (lastBlock instanceof BlockNode) {
						trySuccessorBlocks.addAll(((BlockNode) lastBlock).getSuccessors());
					} else {
						collectHandlerBoundarySuccessors(region, trySuccessorBlocks);
					}
					RegionUtils.getAllRegionBlocks(region, allRegionBlocks);
				}
			}
			successorBlocks.addAll(trySuccessorBlocks);
			if (ProcessTryCatchRegions.containsResourceSuppressionTry(tc)) {
				resourceSuccessorBlocks.addAll(trySuccessorBlocks);
			}
		}
		if (!resourceSuccessorBlocks.isEmpty()) {
			Set<IBlock> handlerOutBlocks = new HashSet<>();
			Set<IBlock> regularSuccessorBlocks = new HashSet<>(successorBlocks);
			regularSuccessorBlocks.removeAll(resourceSuccessorBlocks);
			regularSuccessorBlocks.removeAll(allRegionBlocks);
			handlerOutBlocks.addAll(regularSuccessorBlocks);
			for (IBlock successor : resourceSuccessorBlocks) {
				if (successor instanceof BlockNode
						&& mainRegionBlocks.contains(successor)
						&& isSharedLoopTailJoin((BlockNode) successor, mainRegionBlocks)) {
					// Both the normal try path and its handler reach this in-loop join.
					// Detaching it to the method-level handler tail would move it past the loop return.
					continue;
				}
				if (successor instanceof BlockNode) {
					BlockNode successorBlock = (BlockNode) successor;
					BlockNode terminalReturn = findTerminalReturnTail(successorBlock);
					if (terminalReturn != null) {
						if (!allRegionBlocks.contains(terminalReturn)
								|| mainRegionBlocks.contains(terminalReturn)
										&& detachFromPlainRegion(mth.getRegion(), terminalReturn)) {
							// Empty connector blocks can make several handlers reach the same return. Use the
							// terminal block as their canonical shared continuation and emit it exactly once.
							handlerOutBlocks.add(terminalReturn);
						}
						continue;
					}
				}
				if (!allRegionBlocks.contains(successor)
						|| mainRegionBlocks.contains(successor)
								&& detachFromPlainRegion(mth.getRegion(), successor)) {
					// A non-terminal handler successor can already be present in the main region when it is
					// the shared finally/continuation path. Move it after the try/catch instead of either
					// duplicating it or leaving the handlers without their common tail.
					handlerOutBlocks.add(successor);
				}
			}
			successorBlocks = handlerOutBlocks;
		} else {
			Set<IBlock> handlerOutBlocks = new HashSet<>();
			for (IBlock successor : successorBlocks) {
				if (successor instanceof BlockNode) {
					BlockNode terminalReturn = findTerminalReturnTail((BlockNode) successor);
					if (terminalReturn != null) {
						if (!allRegionBlocks.contains(terminalReturn)
								|| mainRegionBlocks.contains(terminalReturn)
										&& detachFromPlainRegion(mth.getRegion(), terminalReturn)) {
							handlerOutBlocks.add(terminalReturn);
							directTerminalBlocks.add(terminalReturn);
						}
						continue;
					}
				}
				if (!allRegionBlocks.contains(successor)) {
					handlerOutBlocks.add(successor);
				}
			}
			successorBlocks = handlerOutBlocks;
		}
		if (successorBlocks.isEmpty()) {
			return null;
		}
		RegionStack stack = regionMaker.getStack();
		Region excOutRegion = new Region(mth.getRegion());
		for (IBlock block : successorBlocks) {
			if (block instanceof BlockNode) {
				if (directTerminalBlocks.contains(block)) {
					excOutRegion.add(block);
				} else {
					stack.clear();
					stack.push(excOutRegion);
					excOutRegion.add(regionMaker.makeRegion((BlockNode) block));
				}
			}
		}
		return excOutRegion;
	}

	private boolean isSharedLoopTailJoin(BlockNode successor, Set<IBlock> mainRegionBlocks) {
		LoopInfo loop = mth.getLoopForBlock(successor);
		if (loop == null || successor.getPredecessors().size() < 2
				|| successor.getPredecessors().stream().noneMatch(mainRegionBlocks::contains)
				|| successor.getPredecessors().stream().allMatch(mainRegionBlocks::contains)) {
			return false;
		}
		Set<BlockNode> visited = new HashSet<>();
		BlockNode current = successor;
		while (current != loop.getEnd() && visited.add(current)) {
			if (current != successor && !current.getInstructions().isEmpty()) {
				return false;
			}
			List<BlockNode> next = current.getCleanSuccessors();
			if (next.size() != 1) {
				return false;
			}
			current = next.get(0);
		}
		return current == loop.getEnd();
	}

	static @Nullable BlockNode findTerminalReturnTail(BlockNode start) {
		Set<BlockNode> visited = new HashSet<>();
		BlockNode block = start;
		while (visited.add(block)) {
			List<InsnNode> generatedInsns = block.getInstructions().stream()
					.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
					.toList();
			if (!generatedInsns.isEmpty()) {
				return generatedInsns.size() == 1 && generatedInsns.get(0).getType() == InsnType.RETURN
						? block
						: null;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return null;
			}
			block = successors.get(0);
		}
		return null;
	}

	private static boolean detachFromPlainRegion(IRegion region, IBlock target) {
		if (region instanceof Region && region.getSubBlocks().remove(target)) {
			return true;
		}
		for (IContainer container : region.getSubBlocks()) {
			if (container instanceof IRegion && detachFromPlainRegion((IRegion) container, target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A handler ending in an if/switch has no single last block. Collect only clean edges leaving
	 * that handler so its shared continuation is still emitted after the try/catch region.
	 */
	private static void collectHandlerBoundarySuccessors(IContainer region, Set<IBlock> successorBlocks) {
		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(region, handlerBlocks);
		for (IBlock handlerBlock : handlerBlocks) {
			if (!(handlerBlock instanceof BlockNode)) {
				continue;
			}
			for (BlockNode successor : ((BlockNode) handlerBlock).getCleanSuccessors()) {
				if (!handlerBlocks.contains(successor) && isEmptyFinallyVoidReturnTail(successor)) {
					successorBlocks.add(successor);
				}
			}
		}
	}

	private static boolean isEmptyFinallyVoidReturnTail(BlockNode start) {
		Set<BlockNode> visited = new HashSet<>();
		BlockNode block = start;
		while (visited.size() < 8 && visited.add(block)) {
			List<InsnNode> generatedInsns = block.contains(AFlag.DONT_GENERATE)
					? List.of()
					: block.getInstructions().stream()
							.filter(insn -> !insn.contains(AFlag.DONT_GENERATE))
							.toList();
			if (!generatedInsns.isEmpty()) {
				return generatedInsns.size() == 1
						&& generatedInsns.get(0).getType() == InsnType.RETURN
						&& generatedInsns.get(0).getArgsCount() == 0;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	private void processExcHandler(ExceptionHandler handler, Set<BlockNode> exits) {
		BlockNode start = handler.getHandlerBlock();
		if (start == null) {
			return;
		}
		RegionStack stack = regionMaker.getStack().clear();
		BlockNode dom;
		if (handler.isFinally()) {
			dom = BlockUtils.getTopSplitterForHandler(start);
		} else {
			dom = start;
			for (BlockNode exit : exits) {
				if (!isSharedConditionalCleanupRethrow(exit)) {
					stack.addExit(exit);
				}
			}
		}
		if (dom.contains(AFlag.REMOVE)) {
			return;
		}
		List<BlockNode> handlerExits = new ArrayList<>();

		BlockNode sharedRethrowExit = findSharedSynchronizedRethrow(start);
		boolean declaredSynchronized = mth.getAccessFlags().isSynchronized();
		BlockNode handlerOutBlock = BlockUtils.getTryAndHandlerCrossBlock(mth, handler);
		boolean appendSharedCleanupRethrow = handlerOutBlock != null
				&& isSharedTerminalRethrow(handlerOutBlock)
				&& !isSharedSynchronizedRethrow(handlerOutBlock);
		BlockNode rethrowExit = appendSharedCleanupRethrow ? handlerOutBlock : sharedRethrowExit;
		List<InsnNode> rethrowMoves = findHandlerRethrowMoves(
				start, rethrowExit, handler, declaredSynchronized || appendSharedCleanupRethrow);
		boolean namedRethrowChain = !rethrowMoves.isEmpty()
				&& rethrowMoves.get(0).getArgsCount() == 1
				&& rethrowMoves.get(0).getArg(0).isNamed();
		boolean appendDirectRethrow = namedRethrowChain && declaredSynchronized;
		if (handlerOutBlock != null) {
			// ensure frontier's other predecessors comes from try end
			if (isSharedSynchronizedRethrow(handlerOutBlock)) {
				handlerExits.add(handlerOutBlock);
				appendDirectRethrow |= namedRethrowChain && handlerOutBlock == sharedRethrowExit;
			} else {
				handlerExits.add(handlerOutBlock);
			}
		} else {
			// fallback to simple frontier
			BitSet domFrontier = dom.getDomFrontier();
			handlerExits.addAll(BlockUtils.bitSetToBlocks(mth, domFrontier));
		}

		boolean inLoop = mth.getLoopForBlock(start) != null;
		Set<BlockNode> inlineSwitchHandlerExits = new HashSet<>();
		for (BlockNode exit : handlerExits) {
			if (isFallThroughSwitchHandlerContinuation(
					mth.getRegion(), handler.getTryBlock().getTopSplitter(), exit)) {
				inlineSwitchHandlerExits.add(exit);
			}
		}
		for (BlockNode exit : handlerExits) {
			if (inlineSwitchHandlerExits.contains(exit)) {
				continue;
			}
			if (isSharedConditionalCleanupRethrow(exit)) {
				// Every branch ends at the same throw. Stopping at this shared block
				// would leave the later catch body without its cleanup and rethrow.
				continue;
			}
			if ((!inLoop || BlockUtils.isPathExists(start, exit))
					&& RegionUtils.isRegionContainsBlock(mth.getRegion(), exit)) {
				stack.addExit(exit);
			}
		}
		if (!inlineSwitchHandlerExits.isEmpty()) {
			var loop = mth.getLoopForBlock(start);
			if (loop != null) {
				stack.addExit(loop.getStart());
			}
		}
		if (appendDirectRethrow && declaredSynchronized) {
			hideSharedRethrow(sharedRethrowExit);
		}
		handler.setHandlerRegion(regionMaker.makeRegion(start));
		appendSharedCoroutineRethrow(handler, handlerOutBlock);
		if (appendSharedCleanupRethrow
				&& containsSharedCleanupRethrow(handler.getHandlerRegion(), handlerOutBlock)) {
			// Region construction already included this exact shared cleanup and throw.
			// Appending it again emits an unreachable second cleanup and a stale rethrow.
			appendSharedCleanupRethrow = false;
		}
		if (handlerOutBlock != null
				&& handlerOutBlock.contains(AType.EXC_CATCH)
				&& hasOnlyEmptyPath(start, handlerOutBlock)) {
			appendSharedHandlerPrefix(handler, handlerOutBlock);
		}
		appendSharedTerminalReturn(handler);
		appendSharedSwitchContinuation(handler);
		if (appendDirectRethrow || appendSharedCleanupRethrow && namedRethrowChain) {
			for (InsnNode rethrowMove : rethrowMoves) {
				rethrowMove.add(AFlag.DONT_GENERATE);
			}
		}
		if (appendDirectRethrow) {
			InsnNode rethrow = new InsnNode(InsnType.THROW, 1);
			// Keep the handler's NamedArg instance: codegen assigns its collision-free catch name.
			rethrow.addArg(rethrowMoves.get(0).getArg(0));
			handler.getHandlerRegion().getSubBlocks().add(new InsnContainer(rethrow));
		}
		if (appendSharedCleanupRethrow && namedRethrowChain) {
			appendSharedCleanupRethrow(handler, handlerOutBlock);
		}

		ExcHandlerAttr excHandlerAttr = start.get(AType.EXC_HANDLER);
		if (excHandlerAttr == null) {
			mth.addWarn("Missing exception handler attribute for start block: " + start);
		} else {
			handler.getHandlerRegion().addAttr(excHandlerAttr);
		}
	}

	private static boolean isFallThroughSwitchHandlerContinuation(
			IRegion region, BlockNode topSplitter, BlockNode handlerOut) {
		if (region instanceof SwitchRegion) {
			SwitchRegion switchRegion = (SwitchRegion) region;
			IContainer owner = null;
			boolean siblingOwnsOut = false;
			for (SwitchRegion.CaseInfo caseInfo : switchRegion.getCases()) {
				IContainer caseContainer = caseInfo.getContainer();
				if (RegionUtils.isRegionContainsBlock(caseContainer, topSplitter)) {
					owner = caseContainer;
				} else if (RegionUtils.isRegionContainsBlock(caseContainer, handlerOut)) {
					siblingOwnsOut = true;
				}
			}
			if (owner != null
					&& owner.contains(AFlag.FALL_THROUGH)
					&& !RegionUtils.isRegionContainsBlock(owner, handlerOut)
					&& siblingOwnsOut) {
				return true;
			}
		}
		for (IContainer container : region.getSubBlocks()) {
			if (container instanceof IRegion
					&& isFallThroughSwitchHandlerContinuation(
							(IRegion) container, topSplitter, handlerOut)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A suspend catch may return the suspension marker or reach a throw tail also used by its
	 * resume state. The latter must be emitted in the catch without moving the shared tail away
	 * from the resume case. Only a single forwarding move and a side-effect-free throw suffix
	 * establish that throwing the caught value here is equivalent to following that edge.
	 */
	private void appendSharedCoroutineRethrow(ExceptionHandler handler, @Nullable BlockNode sharedTail) {
		if (!CoroutineMethodUtils.isStateMachineBody(mth) || handler.isFinally()
				|| handler.getArg() == null || sharedTail == null || handler.getHandlerRegion() == null
				|| !isSharedTerminalRethrow(sharedTail)
				|| RegionUtils.hasExitBlock(handler.getHandlerRegion())) {
			return;
		}
		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(handler.getHandlerRegion(), handlerBlocks);
		BlockNode forwardingExit = null;
		for (IBlock member : handlerBlocks) {
			if (!(member instanceof BlockNode)) {
				continue;
			}
			BlockNode block = (BlockNode) member;
			InsnNode last = BlockUtils.getLastInsn(block);
			if (last != null && (last.getType() == InsnType.RETURN || last.getType() == InsnType.THROW)) {
				continue;
			}
			for (BlockNode successor : block.getCleanSuccessors()) {
				if (handlerBlocks.contains(successor)) {
					continue;
				}
				if (successor != sharedTail || forwardingExit != null || last == null
						|| last.getType() != InsnType.MOVE || last.getArgsCount() != 1
						|| !isSameForwardedArg(last.getArg(0), handler.getArg())) {
					return;
				}
				forwardingExit = block;
			}
		}
		if (forwardingExit == null) {
			return;
		}
		RegisterArg forwardedResult = BlockUtils.getLastInsn(forwardingExit).getResult();
		if (forwardedResult == null) {
			return;
		}
		BlockNode tail = sharedTail;
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8 && visited.add(tail)) {
			List<InsnNode> insns = tail.getInstructions().stream()
					.filter(insn -> !insn.contains(AFlag.DONT_GENERATE)).toList();
			if (!insns.isEmpty()) {
				if (insns.size() != 1 || insns.get(0).getType() != InsnType.THROW
						|| insns.get(0).getArgsCount() != 1
						|| !isSameForwardedArg(insns.get(0).getArg(0), forwardedResult)) {
					return;
				}
				// Reuse the original terminal instruction. Its register stays attached to the
				// SSA block tree, while the catch's forwarding move supplies its value.
				InsnContainer rethrow = new InsnContainer(insns.get(0));
				rethrow.add(AFlag.SYNTHETIC);
				handler.getHandlerRegion().getSubBlocks().add(rethrow);
				return;
			}
			if (tail.getCleanSuccessors().size() != 1) {
				return;
			}
			tail = tail.getCleanSuccessors().get(0);
		}
	}

	/**
	 * A handler can branch around a normal continuation and join only at the method's shared return.
	 * Region construction omits that boundary edge, which would otherwise make generated catch code
	 * fall through into the normal continuation. Duplicate the terminal return inside the handler;
	 * the original block remains available to the normal path.
	 */
	private static void appendSharedTerminalReturn(ExceptionHandler handler) {
		IRegion handlerRegion = handler.getHandlerRegion();
		if (handlerRegion == null || RegionUtils.hasExitBlock(handlerRegion)) {
			return;
		}
		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(handlerRegion, handlerBlocks);
		boolean hasTerminalThrow = false;
		for (IBlock handlerBlock : handlerBlocks) {
			if (handlerBlock instanceof BlockNode) {
				InsnNode lastInsn = BlockUtils.getLastInsn((BlockNode) handlerBlock);
				if (lastInsn != null && lastInsn.getType() == InsnType.THROW) {
					hasTerminalThrow = true;
					break;
				}
			}
		}
		if (!hasTerminalThrow) {
			return;
		}
		BlockNode terminalReturn = null;
		for (IBlock handlerBlock : handlerBlocks) {
			if (!(handlerBlock instanceof BlockNode)) {
				continue;
			}
			BlockNode block = (BlockNode) handlerBlock;
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null
					&& (lastInsn.getType() == InsnType.RETURN || lastInsn.getType() == InsnType.THROW)) {
				continue;
			}
			for (BlockNode successor : block.getCleanSuccessors()) {
				if (handlerBlocks.contains(successor)) {
					continue;
				}
				BlockNode candidate = findTerminalReturnTail(successor);
				if (candidate == null || terminalReturn != null && terminalReturn != candidate) {
					return;
				}
				terminalReturn = candidate;
			}
		}
		if (terminalReturn == null) {
			return;
		}
		InsnNode returnInsn = BlockUtils.getLastInsn(terminalReturn);
		if (returnInsn != null && returnInsn.getType() == InsnType.RETURN) {
			// Keep the original instruction so its register argument remains attached to the
			// SSA definition in the method block tree. A copied instruction creates a second
			// parent for the same SSA variable and fails the post-region debug consistency check.
			handlerRegion.getSubBlocks().add(new InsnContainer(returnInsn));
		}
	}

	/**
	 * A catch can continue through a linear tail which is also the body of a different
	 * switch case. That case must keep its body, but omitting the catch edge silently
	 * drops its side effects. Emit the same terminal sequence in the catch as well.
	 * Keep this restricted to result-free calls and a literal return: duplicating
	 * definitions or a branch would require remapping SSA and exception boundaries.
	 */
	private void appendSharedSwitchContinuation(ExceptionHandler handler) {
		IRegion handlerRegion = handler.getHandlerRegion();
		if (handler.isFinally() || handlerRegion == null) {
			return;
		}
		Set<IBlock> handlerBlocks = new HashSet<>();
		RegionUtils.getAllRegionBlocks(handlerRegion, handlerBlocks);
		BlockNode boundary = null;
		for (IBlock member : handlerBlocks) {
			if (!(member instanceof BlockNode)) {
				continue;
			}
			BlockNode block = (BlockNode) member;
			InsnNode last = BlockUtils.getLastInsn(block);
			if (last != null && (last.getType() == InsnType.RETURN || last.getType() == InsnType.THROW)) {
				continue;
			}
			for (BlockNode successor : block.getCleanSuccessors()) {
				if (handlerBlocks.contains(successor)) {
					continue;
				}
				if (boundary != null && boundary != successor) {
					return;
				}
				boundary = successor;
			}
		}
		if (boundary == null || !isSiblingSwitchCaseContinuation(
				mth.getRegion(), handler.getTryBlock().getBlocks(), boundary)) {
			return;
		}
		List<IContainer> tail = new ArrayList<>();
		Set<BlockNode> visited = new HashSet<>();
		BlockNode block = boundary;
		while (visited.size() < 8 && visited.add(block)) {
			if (block.contains(AType.PHI_LIST) || block.contains(AFlag.LOOP_START)
					|| block.contains(AFlag.LOOP_END) || block.contains(AFlag.REMOVE)) {
				return;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (insn.getType() == InsnType.RETURN) {
					if (insn.getArgsCount() != 1 || !insn.getArg(0).isLiteral()) {
						return;
					}
					tail.add(new InsnContainer(insn));
					handlerRegion.getSubBlocks().addAll(tail);
					return;
				}
				if (insn.getType() != InsnType.INVOKE || insn.getResult() != null) {
					return;
				}
			}
			// Keep the original block (and its protected-range attributes) so a later
			// try/catch pass can wrap this occurrence independently of the switch case.
			tail.add(block);
			if (block.getCleanSuccessors().size() != 1) {
				return;
			}
			block = block.getCleanSuccessors().get(0);
		}
	}

	private static boolean isSiblingSwitchCaseContinuation(
			IRegion region, List<BlockNode> protectedBlocks, BlockNode continuation) {
		if (region instanceof SwitchRegion) {
			IContainer continuationCase = null;
			for (SwitchRegion.CaseInfo caseInfo : ((SwitchRegion) region).getCases()) {
				IContainer caseContainer = caseInfo.getContainer();
				if (RegionUtils.isRegionContainsBlock(caseContainer, continuation)) {
					continuationCase = caseContainer;
					break;
				}
			}
			if (continuationCase != null) {
				for (SwitchRegion.CaseInfo caseInfo : ((SwitchRegion) region).getCases()) {
					IContainer caseContainer = caseInfo.getContainer();
					if (caseContainer != continuationCase && protectedBlocks.stream()
							.anyMatch(block -> RegionUtils.isRegionContainsBlock(caseContainer, block))) {
						return true;
					}
				}
			}
		}
		for (IContainer child : region.getSubBlocks()) {
			if (child instanceof IRegion
					&& isSiblingSwitchCaseContinuation((IRegion) child, protectedBlocks, continuation)) {
				return true;
			}
		}
		return false;
	}

	private static void hideSharedRethrow(@Nullable BlockNode sharedRethrowExit) {
		if (sharedRethrowExit == null || sharedRethrowExit.getCleanSuccessors().size() != 1) {
			return;
		}
		InsnNode throwInsn = BlockUtils.getLastInsn(sharedRethrowExit.getCleanSuccessors().get(0));
		if (throwInsn != null && throwInsn.getType() == InsnType.THROW) {
			throwInsn.add(AFlag.DONT_GENERATE);
		}
	}

	/**
	 * A synchronized catch-all can share its {@code monitor-exit; throw} tail with another handler.
	 * Stopping the second handler at that cross block drops its rethrow from generated code. Let
	 * region construction visit the terminal tail again; terminal throws are safe to duplicate.
	 */
	private static boolean isSharedSynchronizedRethrow(BlockNode block) {
		if (block.getPredecessors().size() < 2
				|| block.getCleanSuccessors().size() != 1
				|| block.getInstructions().stream().noneMatch(insn -> insn.getType() == InsnType.MONITOR_EXIT)) {
			return false;
		}
		BlockNode successor = block.getCleanSuccessors().get(0);
		return successor.getInstructions().size() == 1
				&& BlockUtils.getLastInsn(successor).getType() == InsnType.THROW;
	}

	private static boolean isSharedTerminalRethrow(BlockNode start) {
		if (start.getPredecessors().size() < 2) {
			return false;
		}
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8 && visited.add(block)) {
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null && lastInsn.getType() == InsnType.THROW) {
				return true;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return false;
	}

	private boolean isSharedConditionalCleanupRethrow(BlockNode start) {
		return isSharedConditionalCleanupRethrow(start, mth.getExitBlock());
	}

	static boolean isSharedConditionalCleanupRethrow(BlockNode start, BlockNode methodExit) {
		if (start.getPredecessors().size() < 2 || start.getCleanSuccessors().size() != 1) {
			return false;
		}
		BlockNode condition = start.getCleanSuccessors().get(0);
		InsnNode conditionInsn = BlockUtils.getLastInsn(condition);
		if (conditionInsn == null || conditionInsn.getType() != InsnType.IF
				|| condition.getCleanSuccessors().size() != 2) {
			return false;
		}
		List<BlockNode> branches = condition.getCleanSuccessors();
		BlockNode direct = branches.get(0);
		BlockNode optional = branches.get(1);
		if (optional.getCleanSuccessors().size() != 1 || optional.getCleanSuccessors().get(0) != direct) {
			optional = branches.get(0);
			direct = branches.get(1);
		}
		if (optional == start || optional == condition || optional == direct
				|| optional.getCleanSuccessors().size() != 1
				|| optional.getCleanSuccessors().get(0) != direct
				|| direct.getCleanSuccessors().size() != 1) {
			return false;
		}
		BlockNode throwBlock = direct;
		if (direct.getCleanSuccessors().get(0) != methodExit) {
			// The conditional cleanup may invoke its callback in the join block and
			// rethrow in a dedicated successor. Keep that entire suffix in each catch.
			throwBlock = direct.getCleanSuccessors().get(0);
			if (throwBlock == start || throwBlock == condition || throwBlock == optional
					|| throwBlock.getPredecessors().size() != 1
					|| throwBlock.getPredecessors().get(0) != direct
					|| throwBlock.getCleanSuccessors().size() != 1
					|| throwBlock.getCleanSuccessors().get(0) != methodExit) {
				return false;
			}
		}
		InsnNode terminal = BlockUtils.getLastInsn(throwBlock);
		return terminal != null && terminal.getType() == InsnType.THROW;
	}

	private static boolean hasOnlyEmptyPath(BlockNode start, BlockNode target) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != target && visited.size() < 8 && visited.add(block)) {
			if (block.getInstructions().stream().anyMatch(insn -> !insn.contains(AFlag.DONT_GENERATE))) {
				return false;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			block = successors.get(0);
		}
		return block == target;
	}

	private static void appendSharedHandlerPrefix(ExceptionHandler handler, BlockNode start) {
		List<BlockNode> prefixBlocks = collectSharedHandlerPrefixBlocks(start);
		if (prefixBlocks.isEmpty()) {
			return;
		}
		List<InsnNode> sharedInsns = new ArrayList<>();
		for (BlockNode block : prefixBlocks) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				sharedInsns.add(insn);
			}
		}
		if (!sharedInsns.isEmpty()) {
			handler.getHandlerRegion().getSubBlocks().add(new InsnContainer(sharedInsns));
			// InsnContainer represents these original blocks in the region tree. Record that fact so
			// CheckRegions does not report their already-emitted instructions as lost CFG blocks.
			prefixBlocks.forEach(block -> block.add(AFlag.ADDED_TO_REGION));
		}
	}

	/**
	 * Collect a short, branch-free handler suffix ending in an abrupt exit. Returning the original
	 * instructions in an InsnContainer is safe only when every traversed block has one unambiguous
	 * continuation and no condition needs structural reconstruction.
	 */
	static List<BlockNode> collectSharedHandlerPrefixBlocks(BlockNode start) {
		List<BlockNode> blocks = new ArrayList<>();
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8 && visited.add(block)) {
			for (InsnNode insn : block.getInstructions()) {
				if (!insn.contains(AFlag.DONT_GENERATE)
						&& (insn.getType() == InsnType.IF || insn.getType() == InsnType.SWITCH)) {
					return List.of();
				}
			}
			blocks.add(block);
			InsnNode lastInsn = BlockUtils.getLastInsn(block);
			if (lastInsn != null
					&& (lastInsn.getType() == InsnType.RETURN || lastInsn.getType() == InsnType.THROW)) {
				return blocks;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return List.of();
			}
			block = successors.get(0);
		}
		return List.of();
	}

	private static boolean containsSharedCleanupRethrow(IRegion handlerRegion, BlockNode start) {
		Set<BlockNode> visited = new HashSet<>();
		BlockNode block = start;
		while (visited.size() < 8 && visited.add(block)) {
			if (!RegionUtils.isRegionContainsBlock(handlerRegion, block)) {
				return false;
			}
			InsnNode last = BlockUtils.getLastInsn(block);
			if (last != null && last.getType() == InsnType.THROW) {
				return true;
			}
			if (block.getCleanSuccessors().size() != 1) {
				return false;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return false;
	}

	private static void appendSharedCleanupRethrow(ExceptionHandler handler, BlockNode start) {
		List<InsnNode> cleanupInsns = new ArrayList<>();
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (visited.size() < 8 && visited.add(block)) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (insn.getType() == InsnType.THROW) {
					if (!cleanupInsns.isEmpty()) {
						handler.getHandlerRegion().getSubBlocks().add(new InsnContainer(cleanupInsns));
					}
					InsnNode rethrow = new InsnNode(InsnType.THROW, 1);
					rethrow.addArg(handler.getArg());
					handler.getHandlerRegion().getSubBlocks().add(new InsnContainer(rethrow));
					return;
				}
				cleanupInsns.add(insn);
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return;
			}
			block = successors.get(0);
		}
	}

	/**
	 * Find a shared synchronized rethrow along a short, straight handler path.
	 */
	private static @Nullable BlockNode findSharedSynchronizedRethrow(BlockNode start) {
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		while (block != null && visited.size() < 8 && visited.add(block)) {
			if (isSharedSynchronizedRethrow(block)) {
				return block;
			}
			if (block.getCleanSuccessors().size() != 1) {
				return null;
			}
			block = block.getCleanSuccessors().get(0);
		}
		return null;
	}

	/**
	 * Return the throwable forwarding move from the catch parameter into the shared rethrow phi.
	 * Other moves on the path can carry cleanup state and must be preserved.
	 */
	private static List<InsnNode> findHandlerRethrowMoves(
			BlockNode start, @Nullable BlockNode exit, ExceptionHandler handler, boolean followAliases) {
		if (exit == null || handler.getArg() == null) {
			return List.of();
		}
		BlockNode block = start;
		Set<BlockNode> visited = new HashSet<>();
		List<InsnNode> moves = new ArrayList<>();
		InsnArg forwardedArg = handler.getArg();
		while (block != null && block != exit && visited.add(block)) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (insn.getType() == InsnType.MOVE
						&& insn.getResult() != null
						&& insn.getArgsCount() == 1
						&& (isSameForwardedArg(insn.getArg(0), followAliases ? forwardedArg : handler.getArg())
								|| forwardedArg instanceof NamedArg
										&& insn.getArg(0) instanceof NamedArg
										&& insn.getResult().getName() != null
										&& insn.getResult().getName().equals(((NamedArg) insn.getArg(0)).getName()))) {
					if (!followAliases && !moves.isEmpty()) {
						return List.of();
					}
					moves.add(insn);
					if (followAliases) {
						forwardedArg = insn.getResult();
					}
				}
			}
			if (block.getCleanSuccessors().size() != 1) {
				return List.of();
			}
			block = block.getCleanSuccessors().get(0);
		}
		if (block != exit || moves.isEmpty()) {
			return List.of();
		}
		return moves;
	}

	private static boolean isSameForwardedArg(InsnArg arg, InsnArg forwardedArg) {
		if (arg.equals(forwardedArg)) {
			return true;
		}
		if (!arg.isRegister() || !forwardedArg.isRegister()) {
			return false;
		}
		RegisterArg registerArg = (RegisterArg) arg;
		RegisterArg forwardedRegister = (RegisterArg) forwardedArg;
		if (registerArg.getSVar() == null || forwardedRegister.getSVar() == null) {
			return false;
		}
		var forwardedVar = forwardedRegister.getSVar();
		List<RegisterArg> pending = new ArrayList<>();
		pending.add(registerArg);
		Set<SSAVar> visited = new HashSet<>();
		for (int i = 0; i < pending.size(); i++) {
			var ssaVar = pending.get(i).getSVar();
			if (ssaVar == null || !visited.add(ssaVar)) {
				continue;
			}
			if (ssaVar == forwardedVar) {
				return true;
			}
			InsnNode assignInsn = ssaVar.getAssignInsn();
			if (assignInsn == null
					|| assignInsn.getType() != InsnType.MOVE && assignInsn.getType() != InsnType.PHI) {
				continue;
			}
			for (InsnArg sourceArg : assignInsn.getArguments()) {
				if (sourceArg.isRegister()) {
					pending.add((RegisterArg) sourceArg);
				}
			}
		}
		return false;
	}
}
