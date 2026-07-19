package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.IFieldRef;
import jadx.api.plugins.input.data.annotations.AnnotationVisibility;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.api.plugins.input.data.attributes.types.AnnotationsAttr;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.AttrNode;
import jadx.core.dex.attributes.nodes.DeclareVariablesAttr;
import jadx.core.dex.attributes.nodes.LineAttrNode;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.regions.conditions.IfCondition.Mode;
import jadx.core.dex.visitors.regions.variables.ProcessVariables;
import jadx.core.dex.visitors.shrink.CodeShrinkVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.exceptions.JadxException;

/**
 * Prepare instructions for code generation pass,
 * most of this modification breaks register dependencies,
 * so this pass must be just before CodeGen.
 */
@JadxVisitor(
		name = "PrepareForCodeGen",
		desc = "Prepare instructions for code generation pass",
		runAfter = { CodeShrinkVisitor.class, ClassModifier.class, ProcessVariables.class }
)
public class PrepareForCodeGen extends AbstractVisitor {

	@Override
	public String getName() {
		return "PrepareForCodeGen";
	}

	@Override
	public boolean visit(ClassNode cls) throws JadxException {
		if (cls.root().getArgs().isDebugInfo()) {
			setClassSourceLine(cls);
		}
		collectFieldsUsageInAnnotations(cls);
		return true;
	}

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			if (block.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			removeInstructions(block);
			checkInline(block);
			collapseMarkedFloatIdentityConversions(mth, block);
			removeParenthesis(block);
			modifyArith(block);
			checkConstUsage(block);
			addNullCasts(mth, block);
		}
		moveConstructorInConstructor(mth);
		collectFieldsUsageInAnnotations(mth, mth);
	}

	private static void removeInstructions(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		int index = 0;
		while (index < insns.size()) {
			InsnNode insn = insns.get(index);
			boolean remove = false;
			switch (insn.getType()) {
				case NOP:
				case MONITOR_ENTER:
				case MONITOR_EXIT:
				case MOVE_EXCEPTION:
					remove = true;
					break;

				case CONSTRUCTOR:
					ConstructorInsn co = (ConstructorInsn) insn;
					if (co.isSelf()) {
						remove = true;
					}
					break;

				case MOVE:
					// remove redundant moves: unused result and same args names (a = a;)
					RegisterArg result = insn.getResult();
					if (result != null
							&& result.getSVar() != null
							&& result.getSVar().getUseCount() == 0
							&& result.isNameEquals(insn.getArg(0))) {
						remove = true;
					}
					break;

				default:
					break;
			}
			if (remove) {
				insns.remove(index);
			} else {
				index++;
			}
		}
	}

	private static void checkInline(BlockNode block) {
		List<InsnNode> list = block.getInstructions();
		for (int i = 0; i < list.size(); i++) {
			InsnNode insn = list.get(i);
			// replace 'move' with inner wrapped instruction
			if (insn.getType() == InsnType.MOVE
					&& insn.getArg(0).isInsnWrap()) {
				InsnNode wrapInsn = ((InsnWrapArg) insn.getArg(0)).getWrapInsn();
				wrapInsn.setResult(insn.getResult());
				wrapInsn.copyAttributesFrom(insn);
				list.set(i, wrapInsn);
			}
		}
	}

	/**
	 * A late coroutine carrier repair can prove that the source of an earlier boolean-to-float
	 * bridge is already a float. These marked synthetic ternaries are stale identity conversions;
	 * collapse them after inlining so both standalone and wrapped copies are handled.
	 */
	private static void collapseMarkedFloatIdentityConversions(MethodNode mth, BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		for (int i = 0; i < insns.size(); i++) {
			InsnNode insn = insns.get(i);
			RegisterArg source = getMarkedFloatIdentitySource(insn);
			if (source != null && insn.getResult() != null) {
				unbindTernaryCondition(mth, (TernaryInsn) insn);
				InsnNode move = new InsnNode(InsnType.MOVE, 1);
				move.setResult(insn.getResult());
				RegisterArg floatSource = source.duplicate();
				floatSource.forceSetInitType(ArgType.FLOAT);
				move.addArg(floatSource);
				move.add(AFlag.SYNTHETIC);
				BlockUtils.replaceInsn(mth, block, i, move);
				continue;
			}
			collapseWrappedMarkedFloatIdentities(mth, insn);
		}
	}

	private static void collapseWrappedMarkedFloatIdentities(MethodNode mth, InsnNode parent) {
		int argsCount = parent.getArgsCount();
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = parent.getArg(i);
			if (!arg.isInsnWrap()) {
				continue;
			}
			InsnNode inner = ((InsnWrapArg) arg).getWrapInsn();
			RegisterArg source = getMarkedFloatIdentitySource(inner);
			if (source != null) {
				unbindTernaryCondition(mth, (TernaryInsn) inner);
				RegisterArg floatSource = source.duplicate();
				floatSource.forceSetInitType(ArgType.FLOAT);
				parent.replaceArg(arg, floatSource);
			} else {
				collapseWrappedMarkedFloatIdentities(mth, inner);
			}
		}
	}

	static RegisterArg getMarkedFloatIdentitySource(InsnNode insn) {
		if (!(insn instanceof TernaryInsn) || !insn.contains(AFlag.SYNTHETIC)) {
			return null;
		}
		TernaryInsn ternary = (TernaryInsn) insn;
		IfCondition condition = ternary.getCondition();
		if (!condition.isCompare()
				|| !condition.getCompare().getInsn().contains(AType.BOOLEAN_NUMERIC_CONVERSION)
				|| condition.getCompare().getOp() != jadx.core.dex.instructions.IfOp.EQ
				|| !(ternary.getArg(0) instanceof LiteralArg)
				|| !(ternary.getArg(1) instanceof LiteralArg)) {
			return null;
		}
		LiteralArg one = (LiteralArg) ternary.getArg(0);
		LiteralArg zero = (LiteralArg) ternary.getArg(1);
		if (!ArgType.FLOAT.equals(one.getType()) || one.getLiteral() != Float.floatToIntBits(1.0f)
				|| !ArgType.FLOAT.equals(zero.getType()) || zero.getLiteral() != 0) {
			return null;
		}
		InsnArg first = condition.getCompare().getA();
		InsnArg second = condition.getCompare().getB();
		RegisterArg source;
		if (first.isRegister() && second.isTrue()) {
			source = (RegisterArg) first;
		} else if (second.isRegister() && first.isTrue()) {
			source = (RegisterArg) second;
		} else {
			return null;
		}
		return source.getSVar() != null && ArgType.FLOAT.equals(source.getSVar().getCodeVar().getType())
				? source
				: null;
	}

	private static void unbindTernaryCondition(MethodNode mth, TernaryInsn ternary) {
		for (RegisterArg conditionArg : ternary.getCondition().getRegisterArgs()) {
			InsnRemover.unbindArgUsage(mth, conditionArg);
		}
	}

	/**
	 * Add explicit type for non int constants
	 */
	private static void checkConstUsage(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		int insnsCount = insns.size();
		for (int i = 0; i < insnsCount; i++) {
			InsnNode blockInsn = insns.get(i);
			blockInsn.visitInsns(insn -> {
				if (forbidExplicitType(insn.getType())) {
					return;
				}
				int argsCount = insn.getArgsCount();
				for (int argIndex = 0; argIndex < argsCount; argIndex++) {
					InsnArg arg = insn.getArg(argIndex);
					if (arg.isLiteral() && arg.getType() != ArgType.INT) {
						arg.add(AFlag.EXPLICIT_PRIMITIVE_TYPE);
					}
				}
			});
		}
	}

	private static boolean forbidExplicitType(InsnType type) {
		switch (type) {
			case CONST:
			case CAST:
			case IF:
			case FILLED_NEW_ARRAY:
			case APUT:
			case ARITH:
				return true;
			default:
				return false;
		}
	}

	private static void removeParenthesis(BlockNode block) {
		List<InsnNode> insns = block.getInstructions();
		int insnsCount = insns.size();
		for (int i = 0; i < insnsCount; i++) {
			removeParenthesis(insns.get(i));
		}
	}

	/**
	 * Remove parenthesis for wrapped insn in arith '+' or '-'
	 * ('(a + b) +c' => 'a + b + c')
	 */
	private static void removeParenthesis(InsnNode insn) {
		if (insn.getType() == InsnType.ARITH) {
			ArithNode arith = (ArithNode) insn;
			ArithOp op = arith.getOp();
			if (op == ArithOp.ADD || op == ArithOp.MUL || op == ArithOp.AND || op == ArithOp.OR) {
				for (int i = 0; i < 2; i++) {
					InsnArg arg = arith.getArg(i);
					if (arg.isInsnWrap()) {
						InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
						if (wrapInsn.getType() == InsnType.ARITH && ((ArithNode) wrapInsn).getOp() == op) {
							wrapInsn.add(AFlag.DONT_WRAP);
						}
						removeParenthesis(wrapInsn);
					}
				}
			}
		} else {
			if (insn.getType() == InsnType.TERNARY) {
				removeParenthesis(((TernaryInsn) insn).getCondition());
			}
			int argsCount = insn.getArgsCount();
			for (int i = 0; i < argsCount; i++) {
				InsnArg arg = insn.getArg(i);
				if (arg.isInsnWrap()) {
					InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
					removeParenthesis(wrapInsn);
				}
			}
		}
	}

	private static void removeParenthesis(IfCondition cond) {
		Mode mode = cond.getMode();
		for (IfCondition c : cond.getArgs()) {
			if (c.getMode() == mode) {
				c.add(AFlag.DONT_WRAP);
			}
		}
	}

	/**
	 * Replace arithmetic operation with short form
	 * ('a = a + 2' => 'a += 2')
	 */
	private static void modifyArith(BlockNode block) {
		List<InsnNode> list = block.getInstructions();
		for (InsnNode insn : list) {
			if (insn.getType() == InsnType.ARITH
					&& !insn.contains(AFlag.ARITH_ONEARG)
					&& !insn.contains(AFlag.DECLARE_VAR)) {
				RegisterArg res = insn.getResult();
				InsnArg arg = insn.getArg(0);
				boolean replace = false;
				if (res.equals(arg)) {
					replace = true;
				} else if (arg.isRegister()) {
					RegisterArg regArg = (RegisterArg) arg;
					replace = res.sameCodeVar(regArg);
				}
				if (replace) {
					insn.setResult(null);
					insn.add(AFlag.ARITH_ONEARG);
				}
			}
		}
	}

	/**
	 * Check that 'super' or 'this' call in constructor is a first instruction.
	 * Otherwise, move to the top and add a warning.
	 */
	private void moveConstructorInConstructor(MethodNode mth) {
		if (!mth.isConstructor()) {
			return;
		}
		ConstructorInsn ctrInsn = searchConstructorCall(mth);
		if (ctrInsn == null || ctrInsn.contains(AFlag.DONT_GENERATE)) {
			return;
		}
		boolean firstInsn = BlockUtils.isFirstInsn(mth, ctrInsn);
		DeclareVariablesAttr declVarsAttr = mth.getRegion().get(AType.DECLARE_VARIABLES);
		if (firstInsn && declVarsAttr == null) {
			// move not needed
			return;
		}
		String callType = ctrInsn.getCallType().toString().toLowerCase();
		BlockNode blockByInsn = BlockUtils.getBlockByInsn(mth, ctrInsn);
		if (blockByInsn == null) {
			mth.addWarn("Failed to move " + callType + " instruction to top");
			return;
		}

		if (!firstInsn) {
			Set<RegisterArg> regArgs = new HashSet<>();
			ctrInsn.getRegisterArgs(regArgs);
			regArgs.remove(mth.getThisArg());
			mth.getArgRegs().forEach(regArgs::remove);
			if (!regArgs.isEmpty() && inlineConstConstructorArgs(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			while (!regArgs.isEmpty() && inlinePureSingleUseConstructorArgs(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineOrderedSingleUseConstructorArgs(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlinePureMultiUseConstructorAssignment(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
				regArgs.removeIf(reg -> isInlineAssignmentInConstructor(ctrInsn, reg));
			}
			if (!regArgs.isEmpty()) {
				mth.addWarnComment("Illegal instructions before constructor call");
				return;
			}
			boolean firstInsnAfterInlining = BlockUtils.isFirstInsn(mth, ctrInsn);
			if (!firstInsnAfterInlining && !isSafeKotlinContinuationConstructorMove(mth, ctrInsn, blockByInsn)) {
				mth.addWarnComment("'" + callType + "' call moved to the top of the method (can break code semantics)");
			}
		}

		// move confirmed
		InsnList.remove(blockByInsn, ctrInsn);
		mth.getRegion().getSubBlocks().add(0, new InsnContainer(ctrInsn));
	}

	private static boolean inlineConstConstructorArgs(MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		Set<InsnNode> assignInsns = new HashSet<>();
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null) {
				return false;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (assignInsn == null
					|| !isLiteralAssign(assignInsn)
					|| regArg.getSVar().getUseList().stream().anyMatch(use -> !isConstructorArgUse(ctrInsn, use))) {
				return false;
			}
			assignInsns.add(assignInsn);
		}
		for (RegisterArg regArg : regArgs) {
			InsnArg constArg = regArg.getSVar().getAssignInsn().getArg(0);
			for (RegisterArg use : new ArrayList<>(regArg.getSVar().getUseList())) {
				ctrInsn.replaceArg(use, constArg.duplicate());
			}
		}
		for (InsnNode assignInsn : assignInsns) {
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock != null) {
				InsnList.remove(assignBlock, assignInsn);
			}
		}
		return true;
	}

	private static boolean isLiteralAssign(InsnNode assignInsn) {
		InsnType type = assignInsn.getType();
		return (type == InsnType.CONST || type == InsnType.MOVE)
				&& assignInsn.getArgsCount() == 1
				&& assignInsn.getArg(0).isLiteral();
	}

	private static boolean isConstructorArgUse(ConstructorInsn ctrInsn, RegisterArg use) {
		return ctrInsn.visitArgs(arg -> arg == use ? Boolean.TRUE : null) != null;
	}

	private static boolean inlinePureSingleUseConstructorArgs(MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		Map<RegisterArg, BlockNode> inlineBlocks = new java.util.HashMap<>();
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null || regArg.getSVar().getUseCount() != 1) {
				return false;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			RegisterArg use = regArg.getSVar().getUseList().get(0);
			if (assignInsn == null
					|| !isPureConstructorExpression(assignInsn)
					|| !isConstructorArgUse(ctrInsn, use)) {
				return false;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock == null) {
				return false;
			}
			inlineBlocks.put(regArg, assignBlock);
		}
		for (Map.Entry<RegisterArg, BlockNode> entry : inlineBlocks.entrySet()) {
			RegisterArg regArg = entry.getKey();
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			RegisterArg use = regArg.getSVar().getUseList().get(0);
			if (use.wrapInstruction(mth, assignInsn, false) == null) {
				return false;
			}
			InsnRemover.unbindResult(mth, assignInsn);
			InsnRemover.removeWithoutUnbind(mth, entry.getValue(), assignInsn);
		}
		return true;
	}

	private static boolean isPureConstructorExpression(InsnNode assignInsn) {
		InsnType type = assignInsn.getType();
		if (type == InsnType.MOVE) {
			return assignInsn.canReorder();
		}
		if (type != InsnType.TERNARY) {
			return false;
		}
		return assignInsn.visitInsns(insn -> insn != assignInsn && !insn.canReorder() ? Boolean.FALSE : null) == null;
	}

	private static boolean inlineOrderedSingleUseConstructorArgs(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		if (regArgs.isEmpty()) {
			return false;
		}
		List<RegisterArg> orderedRegs = new ArrayList<>(regArgs.size());
		ctrInsn.visitArgs(arg -> {
			if (arg.isRegister() && regArgs.contains(arg)) {
				orderedRegs.add((RegisterArg) arg);
			}
		});
		if (orderedRegs.size() != regArgs.size()) {
			return false;
		}
		Map<RegisterArg, BlockNode> assignBlocks = new java.util.LinkedHashMap<>();
		Set<InsnNode> assignInsns = new HashSet<>();
		for (RegisterArg regArg : orderedRegs) {
			if (regArg.getSVar() == null || regArg.getSVar().getUseCount() != 1) {
				return false;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (assignInsn == null
					|| assignInsn.getType() != InsnType.TERNARY
					|| !isConstructorArgUse(ctrInsn, regArg.getSVar().getUseList().get(0))) {
				return false;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock == null) {
				return false;
			}
			assignBlocks.put(regArg, assignBlock);
			assignInsns.add(assignInsn);
		}
		BlockNode ctrBlock = BlockUtils.getBlockByInsn(mth, ctrInsn);
		if (ctrBlock == null || !isOrderedConstructorPath(orderedRegs, assignBlocks, assignInsns, ctrBlock, ctrInsn)) {
			return false;
		}
		for (RegisterArg regArg : orderedRegs) {
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			RegisterArg use = regArg.getSVar().getUseList().get(0);
			if (use.wrapInstruction(mth, assignInsn, false) == null) {
				return false;
			}
			InsnRemover.unbindResult(mth, assignInsn);
			InsnRemover.removeWithoutUnbind(mth, assignBlocks.get(regArg), assignInsn);
		}
		return true;
	}

	private static boolean isOrderedConstructorPath(List<RegisterArg> orderedRegs, Map<RegisterArg, BlockNode> assignBlocks,
			Set<InsnNode> assignInsns, BlockNode ctrBlock, ConstructorInsn ctrInsn) {
		for (int i = 0; i < orderedRegs.size(); i++) {
			RegisterArg reg = orderedRegs.get(i);
			InsnNode fromInsn = reg.getSVar().getAssignInsn();
			BlockNode fromBlock = assignBlocks.get(reg);
			InsnNode toInsn;
			BlockNode toBlock;
			if (i + 1 < orderedRegs.size()) {
				RegisterArg nextReg = orderedRegs.get(i + 1);
				toInsn = nextReg.getSVar().getAssignInsn();
				toBlock = assignBlocks.get(nextReg);
			} else {
				toInsn = ctrInsn;
				toBlock = ctrBlock;
			}
			if (!BlockUtils.isPathExists(fromBlock, toBlock)
					|| !canMoveAcrossSegment(fromBlock, fromInsn, toBlock, toInsn, assignInsns)) {
				return false;
			}
		}
		return true;
	}

	private static boolean canMoveAcrossSegment(BlockNode fromBlock, InsnNode fromInsn, BlockNode toBlock, InsnNode toInsn,
			Set<InsnNode> movableInsns) {
		Set<BlockNode> pathBlocks = BlockUtils.getAllPathsBlocks(fromBlock, toBlock);
		for (BlockNode block : pathBlocks) {
			List<InsnNode> insns = block.getInstructions();
			int start = block == fromBlock ? insns.indexOf(fromInsn) + 1 : 0;
			int end = block == toBlock ? insns.indexOf(toInsn) : insns.size();
			if (start < 0 || end < 0) {
				return false;
			}
			for (int i = start; i < end; i++) {
				InsnNode insn = insns.get(i);
				if (!movableInsns.contains(insn) && !insn.canReorder()) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean inlinePureMultiUseConstructorAssignment(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		if (regArgs.size() != 1) {
			return false;
		}
		RegisterArg regArg = regArgs.iterator().next();
		if (regArg.getSVar() == null || regArg.getSVar().getUseCount() < 2) {
			return false;
		}
		InsnNode assignInsn = regArg.getSVar().getAssignInsn();
		if (assignInsn == null
				|| assignInsn.getType() != InsnType.TERNARY
				|| !isPureConstructorExpression(assignInsn)
				|| regArg.getSVar().getUseList().stream().anyMatch(use -> !isConstructorArgUse(ctrInsn, use))) {
			return false;
		}
		RegisterArg result = assignInsn.getResult();
		if (result == null || mth.getArgRegs().stream().noneMatch(result::sameCodeVar)) {
			return false;
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		if (assignBlock == null) {
			return false;
		}
		List<RegisterArg> uses = regArg.getSVar().getUseList();
		RegisterArg firstUse = ctrInsn.visitArgs(arg -> uses.contains(arg) ? (RegisterArg) arg : null);
		if (firstUse == null) {
			return false;
		}
		assignInsn.add(AFlag.FORCE_ASSIGN_INLINE);
		if (firstUse.wrapInstruction(mth, assignInsn) == null) {
			assignInsn.remove(AFlag.FORCE_ASSIGN_INLINE);
			return false;
		}
		InsnRemover.removeWithoutUnbind(mth, assignBlock, assignInsn);
		return true;
	}

	private static boolean isInlineAssignmentInConstructor(ConstructorInsn ctrInsn, RegisterArg reg) {
		if (reg.getSVar() == null) {
			return false;
		}
		InsnNode assignInsn = reg.getSVar().getAssignInsn();
		return assignInsn != null
				&& assignInsn.contains(AFlag.FORCE_ASSIGN_INLINE)
				&& ctrInsn.visitInsns(insn -> insn == assignInsn ? Boolean.TRUE : null) != null;
	}

	private static boolean isSafeKotlinContinuationConstructorMove(MethodNode mth, ConstructorInsn ctrInsn, BlockNode block) {
		if (!ctrInsn.isSuper() || !isKotlinContinuationClass(mth.getParentClass().getSuperClass())) {
			return false;
		}
		if (BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		RegisterArg thisArg = mth.getThisArg();
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				return true;
			}
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (insn.getType() != InsnType.IPUT
					|| !isSameRegister(insn.getArg(1), thisArg)
					|| !isMethodArgument(mth, insn.getArg(0))) {
				return false;
			}
		}
		return false;
	}

	private static boolean isKotlinContinuationClass(@Nullable ArgType superClass) {
		if (superClass == null || !superClass.isObject()) {
			return false;
		}
		switch (superClass.getObject()) {
			case "kotlin.coroutines.jvm.internal.BaseContinuationImpl":
			case "kotlin.coroutines.jvm.internal.ContinuationImpl":
			case "kotlin.coroutines.jvm.internal.RestrictedContinuationImpl":
			case "kotlin.coroutines.jvm.internal.SuspendLambda":
			case "kotlin.coroutines.jvm.internal.RestrictedSuspendLambda":
				return true;
			default:
				return false;
		}
	}

	private static boolean isSameRegister(InsnArg arg, @Nullable RegisterArg expected) {
		return expected != null && arg.isRegister() && ((RegisterArg) arg).sameCodeVar(expected);
	}

	private static boolean isMethodArgument(MethodNode mth, InsnArg arg) {
		if (!arg.isRegister()) {
			return false;
		}
		RegisterArg registerArg = (RegisterArg) arg;
		return mth.getArgRegs().stream().anyMatch(methodArg -> registerArg.sameCodeVar(methodArg));
	}

	private @Nullable ConstructorInsn searchConstructorCall(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.CONSTRUCTOR) {
					ConstructorInsn ctrInsn = (ConstructorInsn) insn;
					if (ctrInsn.isSuper() || ctrInsn.isThis()) {
						return ctrInsn;
					}
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Use source line from top method
	 */
	private void setClassSourceLine(ClassNode cls) {
		for (ClassNode innerClass : cls.getInnerClasses()) {
			setClassSourceLine(innerClass);
		}
		int minLine = Stream.of(cls.getMethods(), cls.getInnerClasses(), cls.getFields())
				.flatMap(Collection::stream)
				.filter(mth -> !mth.contains(AFlag.DONT_GENERATE))
				.filter(mth -> mth.getSourceLine() != 0)
				.mapToInt(LineAttrNode::getSourceLine)
				.min()
				.orElse(0);
		if (minLine != 0) {
			cls.setSourceLine(minLine - 1);
		}
	}

	private void collectFieldsUsageInAnnotations(ClassNode cls) {
		MethodNode useMth = cls.getDefaultConstructor();
		if (useMth == null && !cls.getMethods().isEmpty()) {
			useMth = cls.getMethods().get(0);
		}
		if (useMth == null) {
			return;
		}
		collectFieldsUsageInAnnotations(useMth, cls);
		MethodNode finalUseMth = useMth;
		cls.getFields().forEach(f -> collectFieldsUsageInAnnotations(finalUseMth, f));
	}

	private void collectFieldsUsageInAnnotations(MethodNode mth, AttrNode attrNode) {
		AnnotationsAttr annotationsList = attrNode.get(JadxAttrType.ANNOTATION_LIST);
		if (annotationsList == null) {
			return;
		}
		for (IAnnotation annotation : annotationsList.getAll()) {
			if (annotation.getVisibility() == AnnotationVisibility.SYSTEM) {
				continue;
			}
			for (Map.Entry<String, EncodedValue> entry : annotation.getValues().entrySet()) {
				checkEncodedValue(mth, entry.getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void checkEncodedValue(MethodNode mth, EncodedValue encodedValue) {
		switch (encodedValue.getType()) {
			case ENCODED_FIELD:
				Object fieldData = encodedValue.getValue();
				FieldInfo fieldInfo;
				if (fieldData instanceof IFieldRef) {
					fieldInfo = FieldInfo.fromRef(mth.root(), (IFieldRef) fieldData);
				} else {
					fieldInfo = (FieldInfo) fieldData;
				}
				FieldNode fieldNode = mth.root().resolveField(fieldInfo);
				if (fieldNode != null) {
					fieldNode.addUseIn(mth);
				}
				break;

			case ENCODED_ANNOTATION:
				IAnnotation annotation = (IAnnotation) encodedValue.getValue();
				annotation.getValues().forEach((k, v) -> checkEncodedValue(mth, v));
				break;

			case ENCODED_ARRAY:
				List<EncodedValue> valueList = (List<EncodedValue>) encodedValue.getValue();
				valueList.forEach(v -> checkEncodedValue(mth, v));
				break;
		}
	}

	private void addNullCasts(MethodNode mth, BlockNode block) {
		for (InsnNode insn : block.getInstructions()) {
			switch (insn.getType()) {
				case INVOKE:
					verifyNullCast(mth, ((InvokeNode) insn).getInstanceArg());
					break;

				case ARRAY_LENGTH:
					verifyNullCast(mth, insn.getArg(0));
					break;
			}
		}
	}

	private void verifyNullCast(MethodNode mth, InsnArg arg) {
		if (arg != null && arg.isZeroConst()) {
			ArgType castType = arg.getType();
			IndexInsnNode castInsn = new IndexInsnNode(InsnType.CAST, castType, 1);
			castInsn.addArg(InsnArg.lit(0, castType));
			arg.wrapInstruction(mth, castInsn);
		}
	}
}
