package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import jadx.api.CommentsLevel;
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
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
import jadx.core.dex.attributes.nodes.LineAttrNode;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
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
		runAfter = {
				CodeShrinkVisitor.class,
				ClassModifier.class,
				ProcessVariables.class
		}
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
		demoteExpectedCoroutineBufferDrainWarning(mth);
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
		eraseUnboundGenericLocals(mth);
	}

	private static void eraseUnboundGenericLocals(MethodNode mth) {
		Set<ArgType> knownTypeVars = mth.root().getTypeUtils().getKnownTypeVarsAtMethod(mth);
		Set<CodeVar> visited = new HashSet<>();
		for (var ssaVar : mth.getSVars()) {
			CodeVar codeVar = ssaVar.getCodeVar();
			if (codeVar == null || !visited.add(codeVar)) {
				continue;
			}
			ArgType type = codeVar.getType();
			if (type == null) {
				continue;
			}
			if (type.isGenericType() && !knownTypeVars.contains(type)) {
				codeVar.setType(ArgType.OBJECT);
			} else if (type.isObject() && !type.isGenericType() && type.containsGeneric()
					&& mth.root().getTypeUtils().containsUnknownTypeVar(mth, type)) {
				codeVar.setType(ArgType.object(type.getObject()));
			}
		}
	}

	private static void demoteExpectedCoroutineBufferDrainWarning(MethodNode mth) {
		JadxCommentsAttr commentsAttr = mth.get(AType.JADX_COMMENTS);
		if (commentsAttr == null) {
			return;
		}
		Map<CommentsLevel, Set<String>> comments = commentsAttr.getComments();
		Set<String> infoComments = comments.get(CommentsLevel.INFO);
		Set<String> warnings = comments.get(CommentsLevel.WARN);
		if (infoComments == null || warnings == null) {
			return;
		}
		String prefix = "Preserved coroutine buffer-drain resume entry (";
		for (String info : infoComments) {
			if (!info.startsWith(prefix) || !info.endsWith(")")) {
				continue;
			}
			String edge = info.substring(prefix.length(), info.length() - 1);
			warnings.remove("Unsupported multi-entry loop pattern (" + edge
					+ "). Please report as a decompilation issue!!!");
		}
		if (warnings.isEmpty()) {
			comments.remove(CommentsLevel.WARN);
		}
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
			if (!regArgs.isEmpty() && inlineConstConstructorArgs(mth, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineMethodArgAliases(mth, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineOrderedInvokeAndFinalStaticPrefix(mth, ctrInsn, regArgs, blockByInsn)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineOrderedIdentityCheckedConstructorPrefix(mth, ctrInsn, regArgs, blockByInsn)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineFinalStaticRequireNonNullPrefix(mth, ctrInsn, regArgs, blockByInsn)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineFinalStaticConstructorArgs(mth, ctrInsn, regArgs)) {
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
			if (!regArgs.isEmpty() && inlineMethodArgAliases(mth, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			if (!regArgs.isEmpty() && inlineStringValueOfMethodArg(mth, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
				regArgs.removeIf(reg -> mth.getArgRegs().stream().anyMatch(reg::sameCodeVar));
				regArgs.removeIf(reg -> isInlineAssignmentInConstructor(ctrInsn, reg));
			}
			if (!regArgs.isEmpty() && inlineFluentStringBuilderConstructorArg(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
			}
			while (!regArgs.isEmpty() && inlinePureMultiUseConstructorAssignment(mth, ctrInsn, regArgs)) {
				regArgs.clear();
				ctrInsn.getRegisterArgs(regArgs);
				regArgs.remove(mth.getThisArg());
				mth.getArgRegs().forEach(regArgs::remove);
				regArgs.removeIf(reg -> isInlineAssignmentInConstructor(ctrInsn, reg));
			}
			if (!regArgs.isEmpty() && inlinePureSingleUseConstructorArgs(mth, ctrInsn, regArgs)) {
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
			if (!firstInsnAfterInlining && inlineSafeRequireNonNullBeforeConstructor(mth, ctrInsn, blockByInsn)) {
				firstInsnAfterInlining = BlockUtils.isFirstInsn(mth, ctrInsn);
			}
			if (!firstInsnAfterInlining
					&& !isSafeLocalConstructorPrefixMove(mth, ctrInsn, blockByInsn)
					&& !isSafeKotlinLambdaConstructorMove(mth, ctrInsn, blockByInsn)
					&& !isSafeKotlinContinuationConstructorMove(mth, ctrInsn, blockByInsn)) {
				mth.addWarnComment("'" + callType + "' call moved to the top of the method (can break code semantics)");
			}
		}

		// move confirmed
		InsnList.remove(blockByInsn, ctrInsn);
		mth.getRegion().getSubBlocks().add(0, new InsnContainer(ctrInsn));
	}

	private static boolean inlineSafeRequireNonNullBeforeConstructor(MethodNode mth, ConstructorInsn ctrInsn, BlockNode block) {
		if (BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		InvokeNode nullCheck = null;
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				break;
			}
			if (nullCheck != null || !isIgnoredObjectsRequireNonNull(insn)) {
				return false;
			}
			nullCheck = (InvokeNode) insn;
		}
		if (nullCheck == null || !nullCheck.getArg(0).isRegister()) {
			return false;
		}
		RegisterArg checkedReg = (RegisterArg) nullCheck.getArg(0);
		RegisterArg ctorUse = ctrInsn.visitArgs(arg -> isSameRegister(arg, checkedReg) ? (RegisterArg) arg : null);
		if (ctorUse == null || !isSafeConstructorEvaluationBefore(ctrInsn, ctorUse)) {
			return false;
		}
		if (ctorUse.wrapInstruction(mth, nullCheck, false) == null) {
			return false;
		}
		InsnRemover.removeWithoutUnbind(mth, block, nullCheck);
		return true;
	}

	private static boolean isIgnoredObjectsRequireNonNull(InsnNode insn) {
		if (!(insn instanceof InvokeNode) || insn.getResult() != null) {
			return false;
		}
		String callId = ((InvokeNode) insn).getCallMth().getRawFullId();
		return callId.equals("java.util.Objects.requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;")
				|| callId.equals("java.util.Objects.requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
	}

	private static boolean inlineOrderedInvokeAndFinalStaticPrefix(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs, BlockNode block) {
		if (regArgs.size() != 2 || BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		List<InsnNode> prefix = new ArrayList<>(2);
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				break;
			}
			if (!insn.contains(AFlag.DONT_GENERATE)) {
				prefix.add(insn);
			}
		}
		if (prefix.size() != 2) {
			return false;
		}
		InsnNode invokeAssign = prefix.stream().filter(InvokeNode.class::isInstance).findFirst().orElse(null);
		InsnNode staticAssign = prefix.stream()
				.filter(insn -> isSafeFinalStaticConstructorRead(mth, insn))
				.findFirst()
				.orElse(null);
		if (invokeAssign == null || staticAssign == null || invokeAssign == staticAssign) {
			return false;
		}
		RegisterArg invokeResult = invokeAssign.getResult();
		RegisterArg staticResult = staticAssign.getResult();
		if (invokeResult == null || invokeResult.getSVar() == null
				|| staticResult == null || staticResult.getSVar() == null
				|| regArgs.stream().noneMatch(reg -> reg.getSVar() == invokeResult.getSVar())
				|| regArgs.stream().noneMatch(reg -> reg.getSVar() == staticResult.getSVar())) {
			return false;
		}
		List<RegisterArg> invokeUses = invokeResult.getSVar().getUseList();
		List<RegisterArg> staticUses = new ArrayList<>(staticResult.getSVar().getUseList());
		RegisterArg invokeCtorUse = findConstructorArgUse(ctrInsn, invokeResult);
		RegisterArg staticCtorUse = findConstructorArgUse(ctrInsn, staticResult);
		if (invokeUses.size() != 1 || invokeCtorUse == null || invokeUses.get(0) != invokeCtorUse
				|| staticUses.size() < 2 || staticCtorUse == null
				|| staticUses.stream().anyMatch(use -> !isConstructorArgUse(ctrInsn, use))) {
			return false;
		}
		List<RegisterArg> evaluationOrder = prefix.get(0) == invokeAssign
				? List.of(invokeCtorUse, staticCtorUse)
				: List.of(staticCtorUse, invokeCtorUse);
		if (!hasSafeConstructorArgOrder(ctrInsn, evaluationOrder)) {
			return false;
		}
		invokeCtorUse.getParentInsn().replaceArg(invokeCtorUse, InsnArg.wrapArg(invokeAssign.copyWithoutResult()));
		for (RegisterArg use : staticUses) {
			use.getParentInsn().replaceArg(use, InsnArg.wrapArg(staticAssign.copyWithoutResult()));
		}
		InsnList.remove(block, invokeAssign);
		InsnList.remove(block, staticAssign);
		return true;
	}

	private static boolean inlineOrderedIdentityCheckedConstructorPrefix(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs, BlockNode block) {
		if (BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		List<InsnNode> meaningfulPrefix = new ArrayList<>();
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				break;
			}
			if (!insn.contains(AFlag.DONT_GENERATE)) {
				meaningfulPrefix.add(insn);
			}
		}
		List<InsnNode> prefixInsns = new ArrayList<>();
		List<InvokeNode> compoundChecks = new ArrayList<>();
		List<RegisterArg> targets = new ArrayList<>();
		Set<RegisterArg> coveredRegs = new HashSet<>();
		boolean identityCheckFound = false;
		for (int i = 0; i < meaningfulPrefix.size(); i++) {
			InsnNode insn = meaningfulPrefix.get(i);
			RegisterArg result = insn.getResult();
			if (isSupportedOrderedPrefixAssignment(insn) && result != null && result.getSVar() != null) {
				if (regArgs.stream().noneMatch(reg -> reg.getSVar() == result.getSVar())) {
					return false;
				}
				RegisterArg ctorUse = findConstructorArgUse(ctrInsn, result);
				if (ctorUse == null) {
					return false;
				}
				List<RegisterArg> uses = result.getSVar().getUseList();
				InvokeNode compoundCheck = null;
				if (uses.size() == 2 && i + 1 < meaningfulPrefix.size()) {
					InsnNode nextInsn = meaningfulPrefix.get(i + 1);
					if (isIgnoredIdentityNullCheck(nextInsn) && nextInsn.getArg(0).isRegister()) {
						RegisterArg checkedReg = (RegisterArg) nextInsn.getArg(0);
						if (checkedReg.getSVar() == result.getSVar()
								&& uses.contains(checkedReg) && uses.contains(ctorUse)) {
							compoundCheck = (InvokeNode) nextInsn;
							identityCheckFound = true;
							i++;
						}
					}
				}
				if (compoundCheck == null
						&& (uses.size() != 1 || !isConstructorArgUse(ctrInsn, uses.get(0)))) {
					return false;
				}
				prefixInsns.add(insn);
				compoundChecks.add(compoundCheck);
				targets.add(ctorUse);
				coveredRegs.add(result);
				continue;
			}
			if (isIgnoredIdentityNullCheck(insn) && insn.getArg(0).isRegister()) {
				RegisterArg checkedReg = (RegisterArg) insn.getArg(0);
				if (!isMethodArgument(mth, checkedReg)) {
					return false;
				}
				RegisterArg ctorUse = findConstructorArgUse(ctrInsn, checkedReg);
				if (ctorUse == null) {
					return false;
				}
				prefixInsns.add(insn);
				compoundChecks.add(null);
				targets.add(ctorUse);
				identityCheckFound = true;
				continue;
			}
			return false;
		}
		if (!identityCheckFound || prefixInsns.isEmpty()
				|| regArgs.stream().anyMatch(reg -> coveredRegs.stream().noneMatch(reg::sameCodeVar))
				|| new HashSet<>(targets).size() != targets.size()
				|| !hasSafeConstructorArgOrder(ctrInsn, targets)) {
			return false;
		}
		for (int i = 0; i < prefixInsns.size(); i++) {
			InsnNode insn = prefixInsns.get(i);
			InvokeNode compoundCheck = compoundChecks.get(i);
			RegisterArg target = targets.get(i);
			if (compoundCheck != null) {
				if (target.wrapInstruction(mth, compoundCheck, false) == null) {
					return false;
				}
				RegisterArg checkedReg = (RegisterArg) compoundCheck.getArg(0);
				compoundCheck.replaceArg(checkedReg, InsnArg.wrapArg(insn.copyWithoutResult()));
				InsnRemover.removeWithoutUnbind(mth, block, compoundCheck);
				InsnList.remove(block, insn);
			} else if (insn.getResult() != null) {
				target.getParentInsn().replaceArg(target, InsnArg.wrapArg(insn.copyWithoutResult()));
				InsnList.remove(block, insn);
			} else {
				if (target.wrapInstruction(mth, insn, false) == null) {
					return false;
				}
				InsnRemover.removeWithoutUnbind(mth, block, insn);
			}
		}
		return true;
	}

	private static boolean isSupportedOrderedPrefixAssignment(InsnNode insn) {
		return insn instanceof InvokeNode || insn.getType() == InsnType.IGET;
	}

	private static boolean isIgnoredIdentityNullCheck(InsnNode insn) {
		if (!(insn instanceof InvokeNode) || insn.getResult() != null) {
			return false;
		}
		String callId = ((InvokeNode) insn).getCallMth().getRawFullId();
		return callId.equals("java.util.Objects.requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;")
				|| callId.equals("java.util.Objects.requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;")
				|| callId.equals("com.google.android.gms.common.internal.Preconditions.checkNotNull(Ljava/lang/Object;)Ljava/lang/Object;");
	}

	/**
	 * Fold the common prefix
	 * {@code finalStatic = OWNER.FIELD; requireNonNull(value); this/super(...)} into constructor arguments.
	 * Both operations are inserted at their first direct constructor uses only when the argument order
	 * reproduces the original prefix evaluation order exactly.
	 */
	private static boolean inlineFinalStaticRequireNonNullPrefix(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs, BlockNode block) {
		if (regArgs.size() != 1 || BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		RegisterArg staticReg = regArgs.iterator().next();
		if (staticReg.getSVar() == null) {
			return false;
		}
		InsnNode staticRead = staticReg.getSVar().getAssignInsn();
		if (!isSafeFinalStaticConstructorRead(mth, staticRead)) {
			return false;
		}
		InvokeNode nullCheck = null;
		int meaningfulInsns = 0;
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				break;
			}
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (meaningfulInsns == 0 && insn == staticRead) {
				meaningfulInsns++;
				continue;
			}
			if (meaningfulInsns == 1 && isIgnoredObjectsRequireNonNull(insn)) {
				nullCheck = (InvokeNode) insn;
				meaningfulInsns++;
				continue;
			}
			return false;
		}
		if (meaningfulInsns != 2 || nullCheck == null || !nullCheck.getArg(0).isRegister()) {
			return false;
		}

		RegisterArg checkedReg = (RegisterArg) nullCheck.getArg(0);
		RegisterArg staticCtorUse = findConstructorArgUse(ctrInsn, staticReg);
		if (staticCtorUse == null) {
			return false;
		}
		List<RegisterArg> staticUses = new ArrayList<>(staticReg.getSVar().getUseList());
		boolean checksStaticValue = checkedReg.getSVar() == staticReg.getSVar();
		if (checksStaticValue) {
			if (staticUses.size() != 2 || !staticUses.contains(checkedReg) || !staticUses.contains(staticCtorUse)
					|| !hasSafeConstructorArgOrder(ctrInsn, staticCtorUse, null)) {
				return false;
			}
			nullCheck.replaceArg(checkedReg, InsnArg.wrapArg(staticRead.copyWithoutResult()));
			if (staticCtorUse.wrapInstruction(mth, nullCheck, false) == null) {
				return false;
			}
		} else {
			if (!isMethodArgument(mth, checkedReg) || staticUses.size() != 1 || !staticUses.contains(staticCtorUse)) {
				return false;
			}
			RegisterArg checkedCtorUse = findConstructorArgUse(ctrInsn, checkedReg);
			if (checkedCtorUse == null
					|| !hasSafeConstructorArgOrder(ctrInsn, staticCtorUse, checkedCtorUse)) {
				return false;
			}
			if (checkedCtorUse.wrapInstruction(mth, nullCheck, false) == null) {
				return false;
			}
			staticCtorUse.getParentInsn().replaceArg(staticCtorUse, InsnArg.wrapArg(staticRead.copyWithoutResult()));
		}
		InsnRemover.removeWithoutUnbind(mth, block, nullCheck);
		InsnList.remove(block, staticRead);
		return true;
	}

	private static @Nullable RegisterArg findConstructorArgUse(ConstructorInsn ctrInsn, RegisterArg target) {
		return ctrInsn.visitArgs(arg -> arg.isRegister() && ((RegisterArg) arg).getSVar() == target.getSVar()
				? (RegisterArg) arg
				: null);
	}

	private static boolean hasSafeConstructorArgOrder(
			ConstructorInsn ctrInsn, RegisterArg first, @Nullable RegisterArg second) {
		return hasSafeConstructorArgOrder(ctrInsn, second == null ? List.of(first) : List.of(first, second));
	}

	private static boolean hasSafeConstructorArgOrder(ConstructorInsn ctrInsn, List<RegisterArg> targets) {
		int[] index = new int[] { 0 };
		for (InsnArg arg : ctrInsn.getArguments()) {
			if (!checkSafeConstructorArgOrder(arg, targets, index)) {
				return false;
			}
			if (index[0] == targets.size()) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkSafeConstructorArgOrder(InsnArg arg, List<RegisterArg> targets, int[] index) {
		if (arg == targets.get(index[0])) {
			index[0]++;
			return true;
		}
		if (!arg.isInsnWrap()) {
			return true;
		}
		InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
		for (InsnArg innerArg : wrapInsn.getArguments()) {
			if (!checkSafeConstructorArgOrder(innerArg, targets, index)) {
				return false;
			}
			if (index[0] == targets.size()) {
				return true;
			}
		}
		return wrapInsn.canReorder();
	}

	private static boolean isSafeConstructorEvaluationBefore(ConstructorInsn ctrInsn, RegisterArg target) {
		boolean[] state = new boolean[] { true, false };
		for (InsnArg arg : ctrInsn.getArguments()) {
			checkEvaluationBeforeTarget(arg, target, state);
			if (!state[0] || state[1]) {
				break;
			}
		}
		return state[0] && state[1];
	}

	private static void checkEvaluationBeforeTarget(InsnArg arg, RegisterArg target, boolean[] state) {
		if (!state[0] || state[1]) {
			return;
		}
		if (arg == target
				|| arg.isRegister() && target.getSVar() != null && ((RegisterArg) arg).getSVar() == target.getSVar()) {
			state[1] = true;
			return;
		}
		if (!arg.isInsnWrap()) {
			return;
		}
		InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
		for (InsnArg innerArg : wrapInsn.getArguments()) {
			checkEvaluationBeforeTarget(innerArg, target, state);
			if (!state[0] || state[1]) {
				return;
			}
		}
		if (!wrapInsn.canReorder()) {
			state[0] = false;
		}
	}

	private static boolean inlineConstConstructorArgs(MethodNode mth, Set<RegisterArg> regArgs) {
		Map<RegisterArg, BlockNode> literals = new java.util.LinkedHashMap<>();
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null) {
				continue;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (assignInsn == null
					|| !isSafeConstAssign(assignInsn)
					|| regArg.getSVar().getUseList().stream().anyMatch(use -> use.getParentInsn() == null)) {
				continue;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock != null) {
				literals.put(regArg, assignBlock);
			}
		}
		if (literals.isEmpty()) {
			return false;
		}
		for (Map.Entry<RegisterArg, BlockNode> entry : literals.entrySet()) {
			RegisterArg regArg = entry.getKey();
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			InsnArg constArg;
			if (assignInsn.getType() == InsnType.CONST_STR) {
				constArg = InsnArg.wrapArg(assignInsn.copyWithoutResult());
				constArg.setType(ArgType.STRING);
			} else {
				constArg = assignInsn.getArg(0);
			}
			for (RegisterArg use : new ArrayList<>(regArg.getSVar().getUseList())) {
				use.getParentInsn().replaceArg(use, constArg.duplicate());
			}
			InsnList.remove(entry.getValue(), assignInsn);
		}
		return true;
	}

	private static boolean isSafeConstAssign(InsnNode assignInsn) {
		InsnType type = assignInsn.getType();
		return type == InsnType.CONST_STR
				|| (type == InsnType.CONST || type == InsnType.MOVE)
						&& assignInsn.getArgsCount() == 1
						&& assignInsn.getArg(0).isLiteral();
	}

	private static boolean inlineMethodArgAliases(MethodNode mth, Set<RegisterArg> regArgs) {
		Map<RegisterArg, BlockNode> aliases = new java.util.LinkedHashMap<>();
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null) {
				continue;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (assignInsn == null
					|| (assignInsn.getType() != InsnType.MOVE && assignInsn.getType() != InsnType.CHECK_CAST)
					|| assignInsn.getArgsCount() != 1
					|| !assignInsn.getArg(0).isRegister()
					|| regArg.getSVar().getUseList().stream().anyMatch(use -> use.getParentInsn() == null)) {
				continue;
			}
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			if (source.getSVar() == null) {
				continue;
			}
			RegisterArg methodArg = mth.getArgRegs().stream()
					.filter(arg -> arg.getSVar() == source.getSVar())
					.findFirst()
					.orElse(null);
			if (methodArg == null || !isSafeMethodArgAlias(mth, assignInsn, methodArg)) {
				continue;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock != null) {
				aliases.put(regArg, assignBlock);
			}
		}
		if (aliases.isEmpty()) {
			return false;
		}
		for (Map.Entry<RegisterArg, BlockNode> entry : aliases.entrySet()) {
			RegisterArg regArg = entry.getKey();
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			for (RegisterArg use : new ArrayList<>(regArg.getSVar().getUseList())) {
				use.getParentInsn().replaceArg(use, source.duplicate());
			}
			InsnList.remove(entry.getValue(), assignInsn);
		}
		return true;
	}

	private static boolean isSafeMethodArgAlias(MethodNode mth, InsnNode assignInsn, RegisterArg methodArg) {
		if (assignInsn.getType() == InsnType.MOVE) {
			return true;
		}
		RegisterArg result = assignInsn.getResult();
		if (result == null) {
			return false;
		}
		ArgType aliasType = result.getType();
		ArgType methodArgType = methodArg.getType();
		return aliasType.isTypeKnown()
				&& methodArgType.isTypeKnown()
				&& aliasType.isObject()
				&& methodArgType.isObject()
				&& mth.root().getTypeCompare().compareTypes(aliasType, methodArgType).isWiderOrEqual();
	}

	private static boolean inlineStringValueOfMethodArg(MethodNode mth, Set<RegisterArg> regArgs) {
		if (regArgs.size() != 1) {
			return false;
		}
		RegisterArg regArg = regArgs.iterator().next();
		if (regArg.getSVar() == null || regArg.getSVar().getUseCount() < 2) {
			return false;
		}
		InsnNode assignInsn = regArg.getSVar().getAssignInsn();
		if (!(assignInsn instanceof InvokeNode)
				|| !((InvokeNode) assignInsn).getCallMth().getRawFullId()
						.equals("java.lang.String.valueOf(Ljava/lang/Object;)Ljava/lang/String;")
				|| assignInsn.getArgsCount() != 1
				|| !assignInsn.getArg(0).isRegister()) {
			return false;
		}
		RegisterArg source = (RegisterArg) assignInsn.getArg(0);
		if (source.getSVar() == null || source.getSVar().getUseCount() != 1) {
			return false;
		}
		RegisterArg methodArg = mth.getArgRegs().stream()
				.filter(arg -> arg.getSVar() == source.getSVar() && ArgType.STRING.equals(arg.getInitType()))
				.findFirst()
				.orElse(null);
		if (methodArg == null) {
			return false;
		}
		RegisterArg lengthUse = regArg.getSVar().getUseList().stream()
				.filter(PrepareForCodeGen::isStringLengthReceiver)
				.findFirst()
				.orElse(null);
		if (lengthUse == null) {
			return false;
		}
		InsnNode lengthInsn = lengthUse.getParentInsn();
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		InsnNode topLengthInsn = getTopLevelInsn(mth, lengthInsn);
		BlockNode lengthBlock = findInsnBlock(mth, topLengthInsn);
		if (assignBlock == null || lengthBlock == null) {
			return false;
		}
		for (RegisterArg use : regArg.getSVar().getUseList()) {
			if (use == lengthUse) {
				continue;
			}
			BlockNode useBlock = BlockUtils.getBlockByInsn(mth, use.getParentInsn());
			if (useBlock == null || useBlock == lengthBlock || !useBlock.isDominator(lengthBlock)) {
				return false;
			}
		}
		assignInsn.add(AFlag.FORCE_ASSIGN_INLINE);
		if (lengthUse.wrapInstruction(mth, assignInsn) == null) {
			assignInsn.remove(AFlag.FORCE_ASSIGN_INLINE);
			return false;
		}
		assignInsn.remove(AFlag.DECLARE_VAR);
		assignInsn.getResult().getSVar().setCodeVar(methodArg.getSVar().getCodeVar());
		InsnRemover.removeWithoutUnbind(mth, assignBlock, assignInsn);
		return true;
	}

	private static @Nullable BlockNode findInsnBlock(MethodNode mth, @Nullable InsnNode insn) {
		BlockNode block = BlockUtils.getBlockByInsn(mth, insn);
		if (block != null || insn == null) {
			return block;
		}
		int offset = insn.getOffset();
		return mth.getBasicBlocks().stream()
				.filter(candidate -> candidate.getStartOffset() == offset)
				.findFirst()
				.orElse(null);
	}

	private static boolean isStringLengthReceiver(RegisterArg use) {
		InsnNode parentInsn = use.getParentInsn();
		return parentInsn instanceof InvokeNode
				&& ((InvokeNode) parentInsn).getInstanceArg() == use
				&& ((InvokeNode) parentInsn).getCallMth().getRawFullId().equals("java.lang.String.length()I");
	}

	private static @Nullable InsnNode getTopLevelInsn(MethodNode mth, InsnNode insn) {
		InsnNode current = insn;
		while (current.contains(AFlag.WRAPPED)) {
			InsnNode parent = BlockUtils.searchInsnParent(mth, current);
			if (parent == null) {
				return null;
			}
			current = parent;
		}
		return current;
	}

	private static boolean inlineFinalStaticConstructorArgs(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		if (regArgs.size() > 1) {
			return inlineMultipleFinalStaticConstructorArgs(mth, ctrInsn, regArgs);
		}
		if (regArgs.size() != 1) {
			return false;
		}
		RegisterArg regArg = regArgs.iterator().next();
		if (regArg.getSVar() == null) {
			return false;
		}
		InsnNode assignInsn = regArg.getSVar().getAssignInsn();
		if (!isSafeFinalStaticConstructorRead(mth, assignInsn)) {
			return false;
		}
		List<RegisterArg> uses = new ArrayList<>(regArg.getSVar().getUseList());
		RegisterArg firstUse = ctrInsn.visitArgs(arg -> uses.contains(arg) ? (RegisterArg) arg : null);
		boolean safeEvaluationBefore = firstUse != null && isSafeConstructorEvaluationBefore(ctrInsn, firstUse);
		if (!safeEvaluationBefore) {
			firstUse = findSafeFirstConditionUse(ctrInsn, uses);
			safeEvaluationBefore = firstUse != null;
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		BlockNode ctrBlock = BlockUtils.getBlockByInsn(mth, ctrInsn);
		if (firstUse == null
				|| assignBlock == null
				|| ctrBlock == null
				|| BlockUtils.followEmptyPath(mth.getEnterBlock()) != assignBlock
				|| assignBlock.getInstructions().indexOf(assignInsn) != 0
				|| !safeEvaluationBefore
				|| !canMoveAcrossSegment(assignBlock, assignInsn, ctrBlock, ctrInsn, Set.of(assignInsn))) {
			return false;
		}
		for (RegisterArg use : uses) {
			InsnNode parentInsn = use.getParentInsn();
			if (parentInsn == null || !parentInsn.containsArg(use)) {
				return false;
			}
			if (!isConstructorArgUse(ctrInsn, use) && !isUseAfterConstructor(mth, ctrInsn, ctrBlock, use)) {
				return false;
			}
		}
		for (RegisterArg use : uses) {
			use.getParentInsn().replaceArg(use, InsnArg.wrapArg(assignInsn.copyWithoutResult()));
		}
		InsnList.remove(assignBlock, assignInsn);
		return true;
	}

	private static boolean inlineMultipleFinalStaticConstructorArgs(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		BlockNode ctrBlock = BlockUtils.getBlockByInsn(mth, ctrInsn);
		if (ctrBlock == null || BlockUtils.followEmptyPath(mth.getEnterBlock()) != ctrBlock) {
			return false;
		}
		List<InsnNode> blockInsns = ctrBlock.getInstructions();
		int ctrIndex = blockInsns.indexOf(ctrInsn);
		if (ctrIndex <= 0) {
			return false;
		}
		Map<RegisterArg, InsnNode> assignments = new java.util.LinkedHashMap<>();
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null) {
				return false;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (!isSafeFinalStaticConstructorRead(mth, assignInsn)
					|| BlockUtils.getBlockByInsn(mth, assignInsn) != ctrBlock) {
				return false;
			}
			assignments.put(regArg, assignInsn);
		}
		Set<InsnNode> assignmentInsns = new HashSet<>(assignments.values());
		for (int i = 0; i < ctrIndex; i++) {
			InsnNode insn = blockInsns.get(i);
			if (!insn.contains(AFlag.DONT_GENERATE) && !assignmentInsns.contains(insn)) {
				return false;
			}
		}
		List<RegisterArg> assignmentOrder = new ArrayList<>(regArgs);
		assignmentOrder.sort((first, second) -> Integer.compare(
				blockInsns.indexOf(assignments.get(first)), blockInsns.indexOf(assignments.get(second))));
		List<RegisterArg> firstUseOrder = new ArrayList<>(regArgs.size());
		for (InsnArg arg : ctrInsn.getArguments()) {
			RegisterArg matchingReg = findMatchingRegister(arg, regArgs);
			if (matchingReg != null) {
				if (!firstUseOrder.contains(matchingReg)) {
					firstUseOrder.add(matchingReg);
				}
				if (firstUseOrder.size() == regArgs.size()) {
					break;
				}
				continue;
			}
			if (containsAnyRegister(arg, regArgs) || !isReorderableConstructorArg(arg)) {
				return false;
			}
		}
		if (firstUseOrder.size() != assignmentOrder.size()) {
			return false;
		}
		for (int i = 0; i < assignmentOrder.size(); i++) {
			if (assignmentOrder.get(i).getSVar() != firstUseOrder.get(i).getSVar()) {
				return false;
			}
		}
		for (RegisterArg regArg : assignmentOrder) {
			List<RegisterArg> uses = new ArrayList<>(regArg.getSVar().getUseList());
			for (RegisterArg use : uses) {
				InsnNode parentInsn = use.getParentInsn();
				if (parentInsn == null || !parentInsn.containsArg(use)) {
					return false;
				}
				if (!isConstructorArgUse(ctrInsn, use) && !isUseAfterConstructor(mth, ctrInsn, ctrBlock, use)) {
					return false;
				}
			}
		}
		for (RegisterArg regArg : assignmentOrder) {
			InsnNode assignInsn = assignments.get(regArg);
			for (RegisterArg use : new ArrayList<>(regArg.getSVar().getUseList())) {
				use.getParentInsn().replaceArg(use, InsnArg.wrapArg(assignInsn.copyWithoutResult()));
			}
			InsnList.remove(ctrBlock, assignInsn);
		}
		return true;
	}

	private static @Nullable RegisterArg findMatchingRegister(InsnArg arg, Set<RegisterArg> candidates) {
		if (!arg.isRegister()) {
			return null;
		}
		RegisterArg registerArg = (RegisterArg) arg;
		return candidates.stream()
				.filter(candidate -> candidate.getSVar() == registerArg.getSVar())
				.findFirst()
				.orElse(null);
	}

	private static boolean containsAnyRegister(InsnArg arg, Set<RegisterArg> candidates) {
		if (!arg.isInsnWrap()) {
			return false;
		}
		List<RegisterArg> registerArgs = new ArrayList<>();
		((InsnWrapArg) arg).getWrapInsn().getRegisterArgs(registerArgs);
		return registerArgs.stream()
				.anyMatch(registerArg -> candidates.stream()
						.anyMatch(candidate -> candidate.getSVar() == registerArg.getSVar()));
	}

	private static boolean isReorderableConstructorArg(InsnArg arg) {
		return !arg.isInsnWrap()
				|| ((InsnWrapArg) arg).getWrapInsn().visitInsns(insn -> !insn.canReorder() ? Boolean.TRUE : null) == null;
	}

	private static @Nullable RegisterArg findSafeFirstConditionUse(
			ConstructorInsn ctrInsn, List<RegisterArg> uses) {
		for (InsnArg arg : ctrInsn.getArguments()) {
			if (arg.isInsnWrap()) {
				InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
				if (wrapInsn instanceof TernaryInsn) {
					RegisterArg conditionUse = findUseInFirstCondition(((TernaryInsn) wrapInsn).getCondition(), uses);
					if (conditionUse != null) {
						return conditionUse;
					}
				}
				if (containsAnyUse(wrapInsn, uses)
						|| wrapInsn.visitInsns(insn -> !insn.canReorder() ? Boolean.TRUE : null) != null) {
					return null;
				}
			} else if (arg.isRegister() && isAnyUse((RegisterArg) arg, uses)) {
				return null;
			}
		}
		return null;
	}

	private static @Nullable RegisterArg findUseInFirstCondition(
			IfCondition condition, List<RegisterArg> uses) {
		IfCondition firstCondition = condition;
		while (!firstCondition.isCompare()) {
			if (firstCondition.getArgs().isEmpty()) {
				return null;
			}
			firstCondition = firstCondition.first();
		}
		InsnArg first = firstCondition.getCompare().getA();
		InsnArg second = firstCondition.getCompare().getB();
		for (RegisterArg use : uses) {
			boolean[] state = new boolean[] { true, false };
			checkEvaluationBeforeTarget(first, use, state);
			if (state[0] && !state[1]) {
				checkEvaluationBeforeTarget(second, use, state);
			}
			if (state[0] && state[1]) {
				return use;
			}
		}
		return null;
	}

	private static boolean containsAnyUse(InsnNode insn, List<RegisterArg> uses) {
		List<RegisterArg> registerArgs = new ArrayList<>();
		insn.getRegisterArgs(registerArgs);
		return registerArgs.stream().anyMatch(arg -> isAnyUse(arg, uses));
	}

	private static boolean isAnyUse(RegisterArg arg, List<RegisterArg> uses) {
		return uses.stream().anyMatch(use -> use.getSVar() == arg.getSVar());
	}

	private static boolean isUseAfterConstructor(
			MethodNode mth, ConstructorInsn ctrInsn, BlockNode ctrBlock, RegisterArg use) {
		InsnNode topUseInsn = getTopLevelInsn(mth, use.getParentInsn());
		BlockNode useBlock = findInsnBlock(mth, topUseInsn);
		if (topUseInsn == null || useBlock == null) {
			return false;
		}
		if (useBlock == ctrBlock) {
			return useBlock.getInstructions().indexOf(topUseInsn) > useBlock.getInstructions().indexOf(ctrInsn);
		}
		return useBlock.isDominator(ctrBlock);
	}

	private static boolean isConstructorArgUse(ConstructorInsn ctrInsn, RegisterArg use) {
		if (ctrInsn.visitArgs(arg -> arg == use ? Boolean.TRUE : null) != null) {
			return true;
		}
		List<RegisterArg> registerArgs = new ArrayList<>();
		ctrInsn.getRegisterArgs(registerArgs);
		return registerArgs.stream().anyMatch(arg -> arg == use);
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
					|| !isOrderedConstructorExpression(assignInsn)
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

	static boolean isOrderedConstructorExpression(InsnNode assignInsn) {
		InsnType type = assignInsn.getType();
		return type == InsnType.TERNARY || type == InsnType.INVOKE;
	}

	private static boolean isSafeFinalStaticConstructorRead(MethodNode mth, @Nullable InsnNode assignInsn) {
		if (!(assignInsn instanceof IndexInsnNode)
				|| assignInsn.getType() != InsnType.SGET
				|| !(((IndexInsnNode) assignInsn).getIndex() instanceof FieldInfo)) {
			return false;
		}
		FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) assignInsn).getIndex();
		FieldNode field = mth.root().resolveField(fieldInfo);
		return field != null ? field.getAccessFlags().isFinal() : isKnownFinalClasspathField(fieldInfo);
	}

	private static boolean isKnownFinalClasspathField(FieldInfo fieldInfo) {
		String cls = fieldInfo.getDeclClass().getFullName();
		String name = fieldInfo.getName();
		if (cls.equals("java.util.Collections")) {
			return name.equals("EMPTY_LIST") || name.equals("EMPTY_MAP") || name.equals("EMPTY_SET");
		}
		return cls.equals("java.nio.charset.CodingErrorAction") && name.equals("REPLACE");
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
			int fromIndex = block == fromBlock ? insns.indexOf(fromInsn) : -1;
			int toIndex = block == toBlock ? insns.indexOf(toInsn) : -1;
			if ((block == fromBlock && fromIndex == -1) || (block == toBlock && toIndex == -1)) {
				return false;
			}
			int start = block == fromBlock ? fromIndex + 1 : 0;
			int end = block == toBlock ? toIndex : insns.size();
			if (start > end) {
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

	/**
	 * Fold a statement-form StringBuilder mutation into a constructor argument:
	 *
	 * <pre>
	 * StringBuilder sb = makeBuilder();
	 * sb.append(value);
	 * super(sb.toString());
	 * </pre>
	 *
	 * The exact StringBuilder receiver chain and a single straight-line block are required so moving
	 * the
	 * constructor does not duplicate the builder or change evaluation order.
	 */
	private static boolean inlineFluentStringBuilderConstructorArg(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		if (regArgs.size() != 1) {
			return false;
		}
		RegisterArg regArg = regArgs.iterator().next();
		if (regArg.getSVar() == null || regArg.getSVar().getUseCount() != 2) {
			return false;
		}
		InsnNode assignInsn = regArg.getSVar().getAssignInsn();
		if (!(assignInsn instanceof InvokeNode)) {
			return false;
		}
		ArgType returnType = ((InvokeNode) assignInsn).getCallMth().getReturnType();
		if (!returnType.isObject() || !returnType.getObject().equals("java.lang.StringBuilder")) {
			return false;
		}

		InvokeNode appendInsn = null;
		RegisterArg appendReceiver = null;
		RegisterArg terminalReceiver = null;
		for (RegisterArg use : regArg.getSVar().getUseList()) {
			InsnNode parentInsn = use.getParentInsn();
			if (!(parentInsn instanceof InvokeNode)) {
				return false;
			}
			InvokeNode invoke = (InvokeNode) parentInsn;
			if (invoke.getInstanceArg() != use || !isStringBuilderCall(invoke)) {
				return false;
			}
			if (invoke.getCallMth().getName().equals("append") && invoke.getResult() == null) {
				if (appendInsn != null) {
					return false;
				}
				appendInsn = invoke;
				appendReceiver = use;
			} else if (invoke.getCallMth().getName().equals("toString")
					&& invoke.getArgsCount() == 1
					&& isConstructorArgUse(ctrInsn, use)) {
				if (terminalReceiver != null) {
					return false;
				}
				terminalReceiver = use;
			} else {
				return false;
			}
		}
		if (appendInsn == null || appendReceiver == null || terminalReceiver == null) {
			return false;
		}

		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		BlockNode appendBlock = BlockUtils.getBlockByInsn(mth, appendInsn);
		BlockNode ctrBlock = BlockUtils.getBlockByInsn(mth, ctrInsn);
		if (assignBlock == null
				|| appendBlock == null
				|| appendBlock != ctrBlock
				|| BlockUtils.followEmptyPath(mth.getEnterBlock()) != assignBlock
				|| (assignBlock == appendBlock
						&& assignBlock.getInstructions().indexOf(assignInsn) >= appendBlock.getInstructions().indexOf(appendInsn))
				|| !BlockUtils.isPathExists(assignBlock, appendBlock)
				|| appendBlock.getInstructions().indexOf(appendInsn) >= appendBlock.getInstructions().indexOf(ctrInsn)
				|| !isSafeConstructorEvaluationBefore(ctrInsn, terminalReceiver)
				|| !canMoveAcrossSegment(assignBlock, assignInsn, ctrBlock, ctrInsn, Set.of(assignInsn, appendInsn))) {
			return false;
		}

		// Both parent/receiver relationships were validated above, so these wraps cannot partially fail.
		if (terminalReceiver.wrapInstruction(mth, appendInsn, false) == null
				|| appendReceiver.wrapInstruction(mth, assignInsn, false) == null) {
			return false;
		}
		InsnRemover.removeWithoutUnbind(mth, appendBlock, appendInsn);
		InsnRemover.removeWithoutUnbind(mth, assignBlock, assignInsn);
		return true;
	}

	private static boolean isStringBuilderCall(InvokeNode invoke) {
		return invoke.getCallMth().getDeclClass().getFullName().equals("java.lang.StringBuilder");
	}

	private static boolean inlinePureMultiUseConstructorAssignment(
			MethodNode mth, ConstructorInsn ctrInsn, Set<RegisterArg> regArgs) {
		for (RegisterArg regArg : regArgs) {
			if (regArg.getSVar() == null || regArg.getSVar().getUseCount() < 2) {
				continue;
			}
			InsnNode assignInsn = regArg.getSVar().getAssignInsn();
			if (isDuplicablePrimitiveLiteralCast(assignInsn)) {
				if (inlineDuplicatedConstructorExpression(mth, ctrInsn, regArg, assignInsn)) {
					return true;
				}
				continue;
			}
			if (assignInsn == null
					|| assignInsn.getType() != InsnType.TERNARY
					|| !isPureConstructorExpression(assignInsn)
					|| regArg.getSVar().getUseList().stream().anyMatch(use -> !isConstructorArgUse(ctrInsn, use))) {
				continue;
			}
			RegisterArg result = assignInsn.getResult();
			if (result == null || mth.getArgRegs().stream().noneMatch(result::sameCodeVar)) {
				continue;
			}
			BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
			if (assignBlock == null) {
				continue;
			}
			List<RegisterArg> uses = regArg.getSVar().getUseList();
			RegisterArg firstUse = ctrInsn.visitArgs(arg -> uses.contains(arg) ? (RegisterArg) arg : null);
			if (firstUse == null) {
				continue;
			}
			assignInsn.add(AFlag.FORCE_ASSIGN_INLINE);
			if (firstUse.wrapInstruction(mth, assignInsn) == null) {
				assignInsn.remove(AFlag.FORCE_ASSIGN_INLINE);
				continue;
			}
			InsnRemover.removeWithoutUnbind(mth, assignBlock, assignInsn);
			return true;
		}
		return false;
	}

	private static boolean isDuplicablePrimitiveLiteralCast(@Nullable InsnNode assignInsn) {
		if (!(assignInsn instanceof IndexInsnNode)
				|| assignInsn.getType() != InsnType.CAST
				|| assignInsn.getArgsCount() != 1
				|| !assignInsn.getArg(0).isLiteral()) {
			return false;
		}
		Object castType = ((IndexInsnNode) assignInsn).getIndex();
		return castType instanceof ArgType && ((ArgType) castType).isPrimitive();
	}

	private static boolean inlineDuplicatedConstructorExpression(
			MethodNode mth, ConstructorInsn ctrInsn, RegisterArg regArg, InsnNode assignInsn) {
		List<RegisterArg> uses = new ArrayList<>(regArg.getSVar().getUseList());
		for (RegisterArg use : uses) {
			InsnNode parentInsn = use.getParentInsn();
			if (parentInsn == null || !parentInsn.containsArg(use) || !isConstructorArgUse(ctrInsn, use)) {
				return false;
			}
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		if (assignBlock == null) {
			return false;
		}
		for (RegisterArg use : uses) {
			use.getParentInsn().replaceArg(use, InsnArg.wrapArg(assignInsn.copyWithoutResult()));
		}
		InsnList.remove(assignBlock, assignInsn);
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

	private static boolean isSafeLocalConstructorPrefixMove(MethodNode mth, ConstructorInsn ctrInsn, BlockNode block) {
		if (BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		RegisterArg thisArg = mth.getThisArg();
		boolean prefixFound = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				return prefixFound;
			}
			if (insn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			prefixFound = true;
			if (!isSafeLocalConstructorPrefixInsn(insn, thisArg)) {
				return false;
			}
		}
		return false;
	}

	private static boolean isSafeLocalConstructorPrefixInsn(InsnNode insn, @Nullable RegisterArg thisArg) {
		RegisterArg result = insn.getResult();
		if (result == null || (thisArg != null && result.sameCodeVar(thisArg))) {
			return false;
		}
		if (thisArg != null && insn.visitArgs(arg -> isSameRegister(arg, thisArg) ? Boolean.TRUE : null) != null) {
			return false;
		}
		if (insn.visitInsns(inner -> inner == insn || isSafeLocalConstructorPrefixInsn(inner, thisArg)
				? null
				: Boolean.FALSE) != null) {
			return false;
		}
		switch (insn.getType()) {
			case CONST:
			case CONST_STR:
			case MOVE:
			case NEG:
			case CMP_L:
			case CMP_G:
				return true;

			case ARITH:
				ArithOp op = ((ArithNode) insn).getOp();
				return op != ArithOp.DIV && op != ArithOp.REM;

			default:
				return false;
		}
	}

	private static boolean isSafeKotlinLambdaConstructorMove(MethodNode mth, ConstructorInsn ctrInsn, BlockNode block) {
		if (!ctrInsn.isSuper() || !isKotlinLambdaClass(mth.getParentClass().getSuperClass())) {
			return false;
		}
		if (BlockUtils.followEmptyPath(mth.getEnterBlock()) != block) {
			return false;
		}
		RegisterArg thisArg = mth.getThisArg();
		boolean captureFound = false;
		for (InsnNode insn : block.getInstructions()) {
			if (insn == ctrInsn) {
				return captureFound;
			}
			if (insn.getType() != InsnType.IPUT
					|| !isSameRegister(insn.getArg(1), thisArg)
					|| !isMethodArgument(mth, insn.getArg(0))) {
				return false;
			}
			captureFound = true;
		}
		return false;
	}

	private static boolean isKotlinLambdaClass(@Nullable ArgType superClass) {
		return superClass != null
				&& superClass.isObject()
				&& superClass.getObject().equals("kotlin.jvm.internal.Lambda");
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
		annotationsList.forEach((type, annotation) -> {
			if (annotation.getVisibility() == AnnotationVisibility.SYSTEM) {
				return;
			}
			annotation.forEachValue((name, value) -> checkEncodedValue(mth, value));
		});
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
				annotation.forEachValue((k, v) -> checkEncodedValue(mth, v));
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
