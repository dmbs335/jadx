package jadx.core.dex.visitors.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.TmpEdgeAttr;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.utils.BlockUtils;

/**
 * Normalize two protected entry arms before exception splitters and loop duplication are built.
 * Both arms must overwrite the same narrow register before using it. Edge-local selector writes
 * can therefore reuse that dead-on-entry register without allocating a new register or changing
 * any original value observed by the body or handler. No throwing instruction is moved or copied.
 */
final class SplitEntryRetryNormalizer {
	private SplitEntryRetryNormalizer() {
	}

	static boolean process(MethodNode mth) {
		if (mth.getExceptionHandlersCount() != 1) {
			return false;
		}
		Plan plan = findPlan(mth.getBasicBlocks());
		if (plan == null) {
			return false;
		}
		apply(mth, plan.body, plan.alternate, plan.register);
		return true;
	}

	static @Nullable Plan findPlan(List<BlockNode> blocks) {
		for (BlockNode body : blocks) {
			InsnNode first = BlockUtils.getFirstInsn(body);
			if (!isEntryAssignment(first)
					|| body.getPredecessors().size() < 2 || findCatch(body) == null) {
				continue;
			}
			BlockNode join = singleNormalSuccessor(body);
			if (join == null || join.getPredecessors().size() != 2) {
				continue;
			}
			Set<BlockNode> forward = reachable(body, false);
			Set<BlockNode> cycle = reachable(body, true);
			cycle.retainAll(forward);
			if (!cycle.contains(join)) {
				continue;
			}
			for (BlockNode alternate : join.getPredecessors()) {
				if (alternate == body || cycle.contains(alternate) || alternate.getPredecessors().size() != 1
						|| singleNormalSuccessor(alternate) != join) {
					continue;
				}
				InsnNode altFirst = BlockUtils.getFirstInsn(alternate);
				if (!isEntryAssignment(altFirst) || first.getResult().getRegNum() != altFirst.getResult().getRegNum()) {
					continue;
				}
				CatchAttr catchAttr = findCatch(body);
				if (catchAttr == null || catchAttr.getHandlers().size() != 1 || !catchAttr.equals(findCatch(alternate))) {
					continue;
				}
				Set<BlockNode> scope = new HashSet<>(cycle);
				scope.add(alternate);
				if (!hasOnlyTwoEntries(cycle, body, alternate)
						|| !validScope(blocks, scope, catchAttr) || !hasRetryEntry(blocks, body, alternate)) {
					continue;
				}
				return new Plan(body, alternate, first.getResult().getRegNum());
			}
		}
		return null;
	}

	static final class Plan {
		final BlockNode body;
		final BlockNode alternate;
		final int register;

		Plan(BlockNode body, BlockNode alternate, int register) {
			this.body = body;
			this.alternate = alternate;
			this.register = register;
		}
	}

	private static boolean hasOnlyTwoEntries(Set<BlockNode> cycle, BlockNode body, BlockNode alternate) {
		for (BlockNode block : cycle) {
			for (BlockNode predecessor : block.getPredecessors()) {
				if (!cycle.contains(predecessor) && block != body && predecessor != alternate) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isEntryAssignment(InsnNode insn) {
		return insn != null && insn.getType() == InsnType.CONST && insn.getResult() != null
				&& insn.getResult().getInitType().canBePrimitive(PrimitiveType.INT)
				&& !insn.getResult().getInitType().canBePrimitive(PrimitiveType.LONG)
				&& !insn.getResult().getInitType().canBePrimitive(PrimitiveType.DOUBLE);
	}

	private static CatchAttr findCatch(BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn.canThrowException()) {
				return insn.get(AType.EXC_CATCH);
			}
		}
		return null;
	}

	private static boolean validScope(List<BlockNode> blocks, Set<BlockNode> scope, CatchAttr catchAttr) {
		for (BlockNode block : blocks) {
			for (InsnNode insn : block.getInstructions()) {
				if (!insn.canThrowException()) {
					continue;
				}
				boolean protectedInsn = catchAttr.equals(insn.get(AType.EXC_CATCH));
				if (scope.contains(block) != protectedInsn) {
					// Require the entire throwing protected range, and no unprotected throwing code.
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasRetryEntry(List<BlockNode> blocks, BlockNode body, BlockNode alternate) {
		for (BlockNode block : blocks) {
			InsnNode first = BlockUtils.getFirstInsn(block);
			if (first != null && first.getType() == InsnType.MOVE_EXCEPTION) {
				Set<BlockNode> reached = reachable(block, false);
				return reached.contains(body) && !reached.contains(alternate);
			}
		}
		return false;
	}

	private static Set<BlockNode> reachable(BlockNode start, boolean reverse) {
		Set<BlockNode> seen = new HashSet<>();
		ArrayDeque<BlockNode> pending = new ArrayDeque<>();
		pending.add(start);
		while (!pending.isEmpty()) {
			BlockNode block = pending.removeLast();
			if (!seen.add(block)) {
				continue;
			}
			for (BlockNode next : reverse ? block.getPredecessors() : block.getSuccessors()) {
				if (!(reverse ? isTemporaryEdge(next, block) : isTemporaryEdge(block, next))) {
					pending.add(next);
				}
			}
		}
		return seen;
	}

	private static @Nullable BlockNode singleNormalSuccessor(BlockNode block) {
		BlockNode result = null;
		for (BlockNode next : block.getSuccessors()) {
			if (!isTemporaryEdge(block, next)) {
				if (result != null) {
					return null;
				}
				result = next;
			}
		}
		return result;
	}

	private static boolean isTemporaryEdge(BlockNode from, BlockNode to) {
		// The handler's temporary reachability edge can originate on either entry arm.
		TmpEdgeAttr edge = to.get(AType.TMP_EDGE);
		return edge != null && edge.getBlock() == from;
	}

	private static void apply(MethodNode mth, BlockNode body, BlockNode alternate, int register) {
		List<BlockNode> bodyPreds = new ArrayList<>(body.getPredecessors());
		List<BlockNode> altPreds = new ArrayList<>(alternate.getPredecessors());
		BlockNode dispatch = BlockSplitter.startNewBlock(mth, -1);
		dispatch.add(AFlag.SYNTHETIC);
		IfNode branch = new IfNode(IfOp.EQ, body.getStartOffset(),
				InsnArg.reg(register, ArgType.INT), InsnArg.lit(0, ArgType.INT));
		dispatch.getInstructions().add(branch);
		BlockSplitter.connect(dispatch, body);
		BlockSplitter.connect(dispatch, alternate);
		branch.initBlocks(dispatch);
		redirect(mth, bodyPreds, body, dispatch, register, 0);
		redirect(mth, altPreds, alternate, dispatch, register, 1);
		mth.addDebugComment("Normalize protected retry entries through a common dispatch");
	}

	private static void redirect(MethodNode mth, List<BlockNode> predecessors, BlockNode target,
			BlockNode dispatch, int register, int value) {
		for (BlockNode predecessor : predecessors) {
			BlockNode setter = BlockSplitter.startNewBlock(mth, -1);
			setter.add(AFlag.SYNTHETIC);
			InsnNode constant = new InsnNode(InsnType.CONST, 1);
			constant.setResult(InsnArg.reg(register, ArgType.INT));
			constant.addArg(InsnArg.lit(value, ArgType.INT));
			setter.getInstructions().add(constant);
			BlockSplitter.replaceConnection(predecessor, target, setter);
			BlockSplitter.connect(setter, dispatch);
		}
	}
}
