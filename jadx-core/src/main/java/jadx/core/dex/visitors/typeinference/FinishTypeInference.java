package jadx.core.dex.visitors.typeinference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.BooleanNumericConversionAttr;
import jadx.core.dex.attributes.nodes.DeclareVariablesAttr;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.ModVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnRemover;

@JadxVisitor(
		name = "Finish Type Inference",
		desc = "Check used types",
		runAfter = {
				TypeInferenceVisitor.class
		}
)
public final class FinishTypeInference extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode() || mth.getSVars().isEmpty()) {
			return;
		}
		repairBooleanLiteralNumericPhiInputs(mth);
		repairDynamicBooleanNumericPhiInputs(mth);
		repairSwitchSelectorPhiInputs(mth);
		repairPhiCodeVarTypes(mth);
		normalizeIntBooleanComparisons(mth.getSVars());
		for (SSAVar var : collectWarnVars(mth.getSVars())) {
			mth.addWarnComment("Type inference failed for: " + var.getDetailedVarInfo(mth));
		}
		mth.getSVars().forEach(var -> {
			CodeVar codeVar = var.getCodeVar();
			if (codeVar.getType() == null) {
				codeVar.setType(ArgType.UNKNOWN);
			}
		});
	}

	/**
	 * A register holding a dynamic boolean can be reused as the 0/1 input of an integer PHI. Type
	 * splitting represents that edge with a synthetic MOVE, but Java cannot assign a boolean directly
	 * to an int. Convert only PHIs whose every input is proven integer-compatible, preserving the DEX
	 * value with {@code flag ? 1 : 0}.
	 */
	private static void repairDynamicBooleanNumericPhiInputs(MethodNode mth) {
		if (isCoroutineMethod(mth)) {
			return;
		}
		boolean largeMethod = mth.getBasicBlocks().size() > 128;
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			InsnNode assignInsn = var.getAssignInsn();
			ArgType codeVarType = var.getCodeVar().getType();
			if (codeVarType != null && codeVarType.isTypeKnown()
					|| !(assignInsn instanceof PhiInsn) || assignInsn.getArgsCount() < 2) {
				continue;
			}
			boolean intArrayStoreUse = isSingleIntArrayStoreUse(var);
			if (largeMethod && !intArrayStoreUse) {
				continue;
			}
			List<InsnNode> booleanMoves = new ArrayList<>();
			boolean hasIntInput = false;
			boolean valid = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister()) {
					valid = false;
					break;
				}
				RegisterArg reg = (RegisterArg) arg;
				if (isIntegerPhiInput(reg)) {
					hasIntInput = true;
					continue;
				}
				SSAVar inputVar = reg.getSVar();
				InsnNode moveInsn = inputVar == null ? null : inputVar.getAssignInsn();
				if (moveInsn == null || moveInsn.getType() != InsnType.MOVE
						|| !moveInsn.contains(AFlag.SYNTHETIC) && (!intArrayStoreUse
								|| inputVar.getUseCount() != 1
								|| BlockUtils.getBlockByInsn(mth, moveInsn) == null)
						|| moveInsn.getArgsCount() != 1 || !moveInsn.getArg(0).isRegister()
						|| !ArgType.BOOLEAN.equals(
								getKnownValueType((RegisterArg) moveInsn.getArg(0), new HashSet<>()))) {
					valid = false;
					break;
				}
				booleanMoves.add(moveInsn);
			}
			if (!valid || !hasIntInput || booleanMoves.isEmpty()) {
				continue;
			}
			for (InsnNode moveInsn : booleanMoves) {
				RegisterArg result = moveInsn.getResult();
				if (result == null || result.getSVar() == null) {
					valid = false;
					break;
				}
				RegisterArg booleanArg = ((RegisterArg) moveInsn.getArg(0)).duplicate();
				TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(result, booleanArg, ArgType.INT);
				convertInsn.add(AFlag.SYNTHETIC);
				convertInsn.addAttr(BooleanNumericConversionAttr.INSTANCE);
				if (!BlockUtils.replaceInsn(mth, moveInsn, convertInsn)) {
					valid = false;
					break;
				}
				markAsInt(result.getSVar());
			}
			if (valid) {
				markAsInt(var);
			}
		}
	}

	/**
	 * Large methods are normally skipped by the dynamic boolean/numeric PHI repair. Keep one narrow
	 * exception for a value written only to an {@code int[]}: the array's exact element type proves
	 * that the DEX boolean value must be represented as Java's numeric 0/1 value.
	 */
	static boolean isSingleIntArrayStoreUse(SSAVar var) {
		InsnNode arrayPut = null;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (arrayPut != null || useInsn.getType() != InsnType.APUT
					|| useInsn.getArgsCount() != 3 || useInsn.getArg(2) != use
					|| !useInsn.getArg(0).isRegister()) {
				return false;
			}
			arrayPut = useInsn;
		}
		if (arrayPut == null) {
			return false;
		}
		RegisterArg arrayArg = (RegisterArg) arrayPut.getArg(0);
		ArgType arrayType = getKnownValueType(arrayArg, new HashSet<>());
		if (arrayType == null) {
			SSAVar arrayVar = arrayArg.getSVar();
			if (arrayVar != null) {
				List<SSAVar> arrayVars = arrayVar.getCodeVar().getSsaVars();
				arrayType = selectSingleArrayType(arrayVars.isEmpty() ? List.of(arrayVar) : arrayVars);
			}
		}
		return arrayType != null && arrayType.isArray() && ArgType.INT.equals(arrayType.getArrayElement());
	}

	/**
	 * A DEX register reused by a boolean and integral values can become a switch selector PHI.
	 * Java permits byte/short/char to widen to int, but boolean needs an explicit 0/1 conversion.
	 * Rewrite only a PHI whose sole generated use is SWITCH and whose every edge is integral or a
	 * single-use boolean MOVE.
	 */
	private static void repairSwitchSelectorPhiInputs(MethodNode mth) {
		if (isCoroutineMethod(mth)) {
			return;
		}
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			InsnNode assignInsn = var.getAssignInsn();
			ArgType codeVarType = var.getCodeVar().getType();
			if (codeVarType != null && codeVarType.isTypeKnown()
					|| !(assignInsn instanceof PhiInsn) || !isSwitchSelectorUse(var)) {
				continue;
			}
			List<InsnNode> booleanMoves = new ArrayList<>();
			boolean hasIntegralInput = false;
			boolean valid = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister()) {
					valid = false;
					break;
				}
				RegisterArg reg = (RegisterArg) arg;
				ArgType inputType = getKnownValueType(reg, new HashSet<>());
				if (isSwitchIntegralType(inputType) || isIntegerPhiInput(reg)) {
					hasIntegralInput = true;
					continue;
				}
				SSAVar inputVar = reg.getSVar();
				InsnNode moveInsn = inputVar == null ? null : inputVar.getAssignInsn();
				if (!ArgType.BOOLEAN.equals(inputType)
						|| moveInsn == null || moveInsn.getType() != InsnType.MOVE
						|| moveInsn.getArgsCount() != 1 || !moveInsn.getArg(0).isRegister()
						|| inputVar.getUseCount() != 1 || BlockUtils.getBlockByInsn(mth, moveInsn) == null) {
					valid = false;
					break;
				}
				booleanMoves.add(moveInsn);
			}
			if (!valid || !hasIntegralInput || booleanMoves.isEmpty()) {
				continue;
			}
			for (InsnNode moveInsn : booleanMoves) {
				RegisterArg result = moveInsn.getResult();
				RegisterArg booleanArg = ((RegisterArg) moveInsn.getArg(0)).duplicate();
				TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(result, booleanArg, ArgType.INT);
				convertInsn.add(AFlag.SYNTHETIC);
				if (!BlockUtils.replaceInsn(mth, moveInsn, convertInsn)) {
					valid = false;
					break;
				}
				markAsInt(result.getSVar());
			}
			if (valid) {
				markAsInt(var);
			}
		}
	}

	private static boolean isSwitchSelectorUse(SSAVar var) {
		int generatedUses = 0;
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (useInsn.getType() != InsnType.SWITCH || ++generatedUses > 1) {
				return false;
			}
		}
		return generatedUses == 1;
	}

	private static boolean isSwitchIntegralType(ArgType type) {
		return ArgType.INT.equals(type)
				|| ArgType.BYTE.equals(type)
				|| ArgType.SHORT.equals(type)
				|| ArgType.CHAR.equals(type);
	}

	private static boolean isCoroutineMethod(MethodNode mth) {
		return mth.getName().equals("invokeSuspend")
				|| mth.getMethodInfo().getArgumentsTypes().stream()
						.anyMatch(type -> type.toString().startsWith("kotlin.coroutines.Continuation"));
	}

	private static boolean isIntegerPhiInput(RegisterArg arg) {
		ArgType type = getKnownValueType(arg, new HashSet<>());
		if (ArgType.INT.equals(type)) {
			return true;
		}
		SSAVar var = arg.getSVar();
		InsnNode assignInsn = var == null ? null : var.getAssignInsn();
		if (assignInsn == null || assignInsn.getArgsCount() != 1) {
			return false;
		}
		InsnArg value = assignInsn.getArg(0);
		return (assignInsn.getType() == InsnType.CONST || assignInsn.getType() == InsnType.MOVE)
				&& value.isLiteral()
				&& value.getType().canBePrimitive(PrimitiveType.INT);
	}

	/**
	 * DEX can reuse a boolean register containing a constant {@code false} as the zero branch of a
	 * nullable integer expression. If that value enters a numeric PHI, Java needs an integer literal
	 * instead of an assignment such as {@code int value = flag}. Rewrite only a single-use MOVE whose
	 * boolean source is proven to be the same 0/1 literal on every path; no conversion instruction is
	 * added and dynamic boolean values are deliberately rejected.
	 */
	private static void repairBooleanLiteralNumericPhiInputs(MethodNode mth) {
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn) || !isDirectNumericPhiUse(var)) {
				continue;
			}
			List<InsnNode> booleanMoves = new ArrayList<>();
			List<Integer> literalValues = new ArrayList<>();
			boolean intArgFound = false;
			boolean valid = true;
			for (InsnArg insnArg : assignInsn.getArguments()) {
				RegisterArg arg = (RegisterArg) insnArg;
				ArgType type = getKnownValueType(arg, new HashSet<>());
				if (ArgType.INT.equals(type)) {
					intArgFound = true;
					continue;
				}
				SSAVar argVar = arg.getSVar();
				InsnNode moveInsn = argVar == null ? null : argVar.getAssignInsn();
				if (!ArgType.BOOLEAN.equals(type)
						|| moveInsn == null || moveInsn.getType() != InsnType.MOVE
						|| moveInsn.getArgsCount() != 1 || !moveInsn.getArg(0).isRegister()
						|| argVar.getUseCount() != 1) {
					valid = false;
					break;
				}
				Integer literal = resolveBooleanLiteral((RegisterArg) moveInsn.getArg(0), new HashSet<>());
				if (literal == null) {
					valid = false;
					break;
				}
				booleanMoves.add(moveInsn);
				literalValues.add(literal);
			}
			if (!valid || !intArgFound || booleanMoves.isEmpty()) {
				continue;
			}
			for (int i = 0; i < booleanMoves.size(); i++) {
				InsnNode moveInsn = booleanMoves.get(i);
				moveInsn.replaceArg(moveInsn.getArg(0), LiteralArg.make(literalValues.get(i), ArgType.INT));
				markAsInt(moveInsn.getResult().getSVar());
			}
			markAsInt(var);
			foldSingleUseSyntheticPhiMoves(mth, (PhiInsn) assignInsn, var.getCodeVar());
		}
		if (!mth.isConstructor()) {
			foldBooleanLiteralPhiEdgeMoves(mth);
		}
	}

	/**
	 * Type inference can split a PHI input by inserting a synthetic MOVE on the incoming edge.
	 * Once the mixed boolean/numeric PHI above is proven to be entirely numeric, keeping that MOVE
	 * makes region code generation declare the source and PHI target as two Java locals and can place
	 * the transfer after its first use. Fold only an edge-local, single-use copy back into the PHI.
	 */
	private static void foldSingleUseSyntheticPhiMoves(MethodNode mth, PhiInsn phiInsn, CodeVar targetCodeVar) {
		List<RegisterArg> phiArgs = new ArrayList<>(phiInsn.getArgsCount());
		for (InsnArg arg : phiInsn.getArguments()) {
			phiArgs.add((RegisterArg) arg);
		}
		for (RegisterArg phiArg : phiArgs) {
			SSAVar moveVar = phiArg.getSVar();
			InsnNode moveInsn = moveVar.getAssignInsn();
			if (moveInsn == null || moveInsn.getType() != InsnType.MOVE || !moveInsn.contains(AFlag.SYNTHETIC)
					|| moveInsn.getArgsCount() != 1 || !moveInsn.getArg(0).isRegister()
					|| moveVar.getUseCount() != 1) {
				continue;
			}
			RegisterArg sourceArg = (RegisterArg) moveInsn.getArg(0);
			SSAVar sourceVar = sourceArg.getSVar();
			CodeVar sourceCodeVar = sourceVar.getCodeVar();
			if (sourceVar.getUseCount() != 1
					|| sourceArg.getRegNum() != phiArg.getRegNum()
					|| !ArgType.INT.equals(sourceCodeVar.getType())
					|| sourceCodeVar == targetCodeVar
					|| sourceCodeVar.isThis() || sourceCodeVar.isDeclared()
					|| BlockUtils.getBlockByInsn(mth, moveInsn) != phiInsn.getBlockByArg(phiArg)) {
				continue;
			}
			if (!phiInsn.replaceArg(phiArg, sourceArg.duplicate())) {
				continue;
			}
			List<SSAVar> oldGroup = new ArrayList<>(sourceCodeVar.getSsaVars());
			oldGroup.remove(sourceVar);
			sourceCodeVar.setSsaVars(oldGroup);
			sourceVar.setCodeVar(targetCodeVar);
			BlockUtils.getBlockByInsn(mth, moveInsn).getInstructions().remove(moveInsn);
			InsnRemover.unbindInsn(mth, moveInsn);
		}
	}

	/**
	 * Fold the remaining copy in a two-input boolean PHI when the other edge is a proven literal.
	 * This exposes the actual {@code flag / true} values to region processing, which can then emit a
	 * complete conditional assignment instead of dropping the copy block selected as the IF out.
	 */
	private static void foldBooleanLiteralPhiEdgeMoves(MethodNode mth) {
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn) || assignInsn.getArgsCount() != 2
					|| !ArgType.BOOLEAN.equals(var.getCodeVar().getType())) {
				continue;
			}
			PhiInsn phiInsn = (PhiInsn) assignInsn;
			RegisterArg first = phiInsn.getArg(0);
			RegisterArg second = phiInsn.getArg(1);
			RegisterArg moveArg;
			if (resolveBooleanLiteral(first, new HashSet<>()) != null) {
				moveArg = second;
			} else if (resolveBooleanLiteral(second, new HashSet<>()) != null) {
				moveArg = first;
			} else {
				continue;
			}
			SSAVar moveVar = moveArg.getSVar();
			InsnNode moveInsn = moveVar.getAssignInsn();
			if (moveInsn == null || moveInsn.getType() != InsnType.MOVE
					|| moveInsn.getArgsCount() != 1 || !moveInsn.getArg(0).isRegister()
					|| moveVar.getUseCount() != 1
					|| BlockUtils.getBlockByInsn(mth, moveInsn) != phiInsn.getBlockByArg(moveArg)) {
				continue;
			}
			RegisterArg sourceArg = (RegisterArg) moveInsn.getArg(0);
			SSAVar sourceVar = sourceArg.getSVar();
			CodeVar sourceCodeVar = sourceVar.getCodeVar();
			CodeVar targetCodeVar = var.getCodeVar();
			if (!ArgType.BOOLEAN.equals(sourceCodeVar.getType())
					|| sourceCodeVar == targetCodeVar
					|| !phiInsn.replaceArg(moveArg, sourceArg.duplicate())) {
				continue;
			}
			BlockUtils.getBlockByInsn(mth, moveInsn).getInstructions().remove(moveInsn);
			InsnRemover.unbindInsn(mth, moveInsn);
		}
	}

	private static boolean isDirectNumericPhiUse(SSAVar var) {
		if (var.getUseList().isEmpty()) {
			return false;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (!(useInsn instanceof IfNode)) {
				return false;
			}
			IfOp op = ((IfNode) useInsn).getOp();
			if (op != IfOp.LT && op != IfOp.LE && op != IfOp.GT && op != IfOp.GE) {
				return false;
			}
		}
		return true;
	}

	private static Integer resolveBooleanLiteral(RegisterArg arg, Set<SSAVar> visited) {
		SSAVar var = arg.getSVar();
		if (var == null || !visited.add(var)) {
			return null;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return null;
		}
		if ((assignInsn.getType() == InsnType.CONST || assignInsn.getType() == InsnType.MOVE)
				&& assignInsn.getArgsCount() == 1
				&& assignInsn.getArg(0) instanceof LiteralArg) {
			long literal = ((LiteralArg) assignInsn.getArg(0)).getLiteral();
			return literal == 0 || literal == 1 ? (int) literal : null;
		}
		if (assignInsn.getType() == InsnType.MOVE && assignInsn.getArgsCount() == 1
				&& assignInsn.getArg(0).isRegister()) {
			return resolveBooleanLiteral((RegisterArg) assignInsn.getArg(0), visited);
		}
		if (!(assignInsn instanceof PhiInsn) || assignInsn.getArgsCount() == 0) {
			return null;
		}
		Integer value = null;
		for (InsnArg phiArg : assignInsn.getArguments()) {
			if (!phiArg.isRegister()) {
				return null;
			}
			Integer input = resolveBooleanLiteral((RegisterArg) phiArg, new HashSet<>(visited));
			if (input == null || value != null && !value.equals(input)) {
				return null;
			}
			value = input;
		}
		return value;
	}

	private static void markAsInt(SSAVar var) {
		CodeVar codeVar = var.getCodeVar();
		codeVar.setType(ArgType.INT);
		List<SSAVar> group = codeVar.getSsaVars();
		if (group.isEmpty()) {
			group = Collections.singletonList(var);
		}
		for (SSAVar groupVar : group) {
			groupVar.setType(ArgType.INT);
			if (groupVar.getAssign() != null) {
				groupVar.getAssign().forceSetInitType(ArgType.INT);
			}
			for (RegisterArg use : groupVar.getUseList()) {
				use.forceSetInitType(ArgType.INT);
			}
		}
	}

	/**
	 * DEX represents boolean constants as 0/1. If the variable was finally proven to be an int,
	 * keep a hidden condition using an int literal too, so code generation can't emit
	 * {@code int == true}.
	 */
	private static void normalizeIntBooleanComparisons(List<SSAVar> vars) {
		for (SSAVar var : vars) {
			if (!ArgType.INT.equals(var.getCodeVar().getType())) {
				continue;
			}
			for (RegisterArg useArg : var.getUseList()) {
				InsnNode parentInsn = useArg.getParentInsn();
				if (!(parentInsn instanceof IfNode)) {
					continue;
				}
				IfNode ifInsn = (IfNode) parentInsn;
				IfOp op = ifInsn.getOp();
				if (op != IfOp.EQ && op != IfOp.NE) {
					continue;
				}
				InsnArg firstArg = ifInsn.getArg(0);
				InsnArg secondArg = ifInsn.getArg(1);
				InsnArg otherArg = firstArg == useArg ? secondArg : secondArg == useArg ? firstArg : null;
				if (otherArg != null && otherArg.isLiteral() && ArgType.BOOLEAN.equals(otherArg.getType())) {
					otherArg.setType(ArgType.INT);
				}
			}
		}
	}

	static List<SSAVar> collectWarnVars(List<SSAVar> vars) {
		Set<CodeVar> reportedCodeVars = Collections.newSetFromMap(new IdentityHashMap<>());
		List<SSAVar> warnVars = new ArrayList<>();
		for (SSAVar var : vars) {
			ArgType type = var.getTypeInfo().getType();
			CodeVar codeVar = var.getCodeVar();
			ArgType codeVarType = codeVar.getType();
			if (!type.isTypeKnown()
					&& (codeVarType == null || !codeVarType.isTypeKnown())
					&& hasGeneratedUse(var)
					&& reportedCodeVars.add(codeVar)) {
				warnVars.add(var);
			}
		}
		return warnVars;
	}

	private static void repairPhiCodeVarTypes(MethodNode mth) {
		normalizeProvenNullMoveSources(mth);
		Map<CodeVar, List<SSAVar>> groups = collectCodeVarGroups(mth);
		for (List<SSAVar> group : groups.values()) {
			replaceNullOnlyPhiUses(mth, group);
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			ArgType codeVarType = entry.getKey().getType();
			if (codeVarType != null && codeVarType.isTypeKnown()) {
				continue;
			}
			for (SSAVar var : new ArrayList<>(entry.getValue())) {
				if (var.getUseCount() >= 2 && hasDefaultConstructorMarkerUse(var)) {
					replaceProvenNullMultiTypeUses(mth, var);
				}
			}
		}
		int cleanupSplitCount = repairExceptionCleanupSiblingNullFlows(
				mth.getSVars(), groups, mth.root().getTypeCompare(), insn -> isInExceptionHandler(mth, insn));
		if (cleanupSplitCount != 0) {
			groups = collectCodeVarGroups(mth);
		}
		boolean coroutineMethod = isCoroutineMethod(mth);
		int splitCount = cleanupSplitCount + splitMixedPrimitiveCodeVars(groups,
				coroutineMethod ? FinishTypeInference::getCoroutineLabelType : FinishTypeInference::getNonCoroutinePrimitiveType);
		if (!coroutineMethod) {
			int referenceMoveSplitCount = splitStructuralReferenceMoveRootLifetimes(groups);
			splitCount += referenceMoveSplitCount;
			if (referenceMoveSplitCount != 0) {
				groups = collectCodeVarGroups(mth);
			}
			splitCount += splitMixedReferenceLifetimes(groups);
		}
		if (splitCount != 0) {
			groups = collectCodeVarGroups(mth);
		}
		if (coroutineMethod) {
			repairCoroutineContinuationPathCasts(mth, groups);
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType == null || !currentType.isTypeKnown()) {
				repairBooleanLiteralPhiFlow(entry.getValue());
			}
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType == null || !currentType.isTypeKnown()) {
				ArgType primitiveType = selectSinglePrimitiveType(entry.getValue());
				if (primitiveType != null) {
					codeVar.setType(primitiveType);
				} else {
					ArgType arrayType = selectSingleArrayType(entry.getValue());
					if (arrayType != null) {
						codeVar.setType(arrayType);
					}
				}
			}
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			boolean refineObject = ArgType.OBJECT.equals(currentType);
			if (currentType != null && currentType.isTypeKnown() && !refineObject) {
				continue;
			}
			ArgType candidate = selectObjectType(entry.getValue(), mth.root().getTypeCompare());
			if (candidate != null && (!refineObject || candidate.isGenericType())) {
				codeVar.setType(candidate);
				if (candidate.isGenericType() && candidate.equals(selectGenericReturnType(entry.getValue()))) {
					insertGenericConcreteUseCasts(mth, entry.getValue(), candidate);
				}
			}
		}
		boolean changed;
		do {
			changed = false;
			for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
				CodeVar codeVar = entry.getKey();
				ArgType currentType = codeVar.getType();
				if (currentType == null || !currentType.isTypeKnown()) {
					ArgType moveType = selectMoveSourceType(entry.getValue(), codeVar);
					if (moveType == null) {
						moveType = selectSyntheticIntMoveTargetType(entry.getValue(), codeVar);
					}
					if (moveType == null) {
						moveType = selectMoveTargetType(entry.getValue(), codeVar);
					}
					if (moveType != null) {
						codeVar.setType(moveType);
						changed = true;
					}
				}
			}
		} while (changed);
		boolean hasMixedReferencePrimitiveCandidate;
		do {
			changed = false;
			hasMixedReferencePrimitiveCandidate = false;
			for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
				CodeVar codeVar = entry.getKey();
				ArgType currentType = codeVar.getType();
				if (currentType != null && currentType.isTypeKnown()) {
					continue;
				}
				if (!codeVar.isThis() && isMixedReferencePrimitivePhiCandidate(entry.getValue())) {
					hasMixedReferencePrimitiveCandidate = true;
				}
				ArgType closedReferenceType = selectClosedReferenceMoveType(entry.getValue(), codeVar);
				if (closedReferenceType != null) {
					codeVar.setType(closedReferenceType);
					changed = true;
				}
			}
		} while (changed);
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if ((currentType == null || !currentType.isTypeKnown())
					&& isReferenceOnlyPhiFlow(entry.getValue(), codeVar)) {
				codeVar.setType(ArgType.OBJECT);
			}
		}
		int mixedReferencePrimitiveRepairs = hasMixedReferencePrimitiveCandidate
				? repairMixedReferencePrimitivePhiLifetimes(
						groups, (root, input) -> isAssignDominatingPhiInput(mth, root, input))
				: 0;
		if (mixedReferencePrimitiveRepairs != 0) {
			groups = collectCodeVarGroups(mth);
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if ((currentType == null || !currentType.isTypeKnown())
					&& isOnlyUsedByObjectCasts(entry.getValue())) {
				codeVar.setType(ArgType.OBJECT);
			}
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			ArgType nullablePhiType = selectNullablePhiUseType(entry.getValue());
			if (nullablePhiType != null) {
				codeVar.setType(nullablePhiType);
			}
		}
		// A nullable consumer reached through an unknown MOVE/PHI chain can only become a
		// concrete boundary after the regular group inference above has completed.
		for (List<SSAVar> group : groups.values()) {
			replaceClosedNullFlowUses(group);
		}
	}

	private static boolean isAssignDominatingPhiInput(MethodNode mth, SSAVar root, RegisterArg input) {
		InsnNode assignInsn = root.getAssignInsn();
		InsnNode parentInsn = input.getParentInsn();
		if (assignInsn == null || !(parentInsn instanceof PhiInsn)) {
			return false;
		}
		BlockNode rootBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		BlockNode inputBlock = ((PhiInsn) parentInsn).getBlockByArg(input);
		return rootBlock != null && inputBlock != null
				&& (rootBlock == inputBlock || inputBlock.isDominator(rootBlock));
	}

	private static boolean isMixedReferencePrimitivePhiCandidate(List<SSAVar> group) {
		if (group.size() != 5) {
			return false;
		}
		int phiCount = 0;
		int moveCount = 0;
		for (SSAVar var : group) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn && assignInsn.getArgsCount() == 2
					&& assignInsn.contains(AFlag.DONT_GENERATE)) {
				phiCount++;
			} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
				moveCount++;
			} else {
				return false;
			}
		}
		return phiCount == 2 && moveCount == 3;
	}

	/**
	 * Recover the Java type for a 0/1 PHI consumed exclusively as boolean control flow.
	 * Compose changed-flag checks often reach this late stage after their PHIs were introduced,
	 * so changing the shared code-variable type here avoids inventing casts or new instructions.
	 */
	private static boolean repairBooleanLiteralPhiFlow(List<SSAVar> vars) {
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn) || assignInsn.getArgsCount() == 0) {
				continue;
			}
			boolean validInputs = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister() || !isBooleanLiteralValue((RegisterArg) arg, new HashSet<>())) {
					validInputs = false;
					break;
				}
			}
			Set<SSAVar> booleanFlowVars = new HashSet<>();
			if (validInputs && isBooleanControlFlow(var, booleanFlowVars)) {
				for (SSAVar booleanVar : booleanFlowVars) {
					CodeVar codeVar = booleanVar.getCodeVar();
					ArgType type = codeVar.getType();
					if (type == null || !type.isTypeKnown()) {
						codeVar.setType(ArgType.BOOLEAN);
					}
				}
				return true;
			}
		}
		return false;
	}

	private static boolean isBooleanLiteralValue(RegisterArg arg, Set<SSAVar> visited) {
		SSAVar var = arg.getSVar();
		if (var == null || !visited.add(var)) {
			return false;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getArgsCount() != 1) {
			return false;
		}
		InsnArg value = assignInsn.getArg(0);
		if (assignInsn.getType() == InsnType.MOVE && value.isRegister()) {
			return isBooleanLiteralValue((RegisterArg) value, visited);
		}
		if ((assignInsn.getType() != InsnType.CONST && assignInsn.getType() != InsnType.MOVE) || !value.isLiteral()) {
			return false;
		}
		long literal = ((LiteralArg) value).getLiteral();
		return literal == 0 || literal == 1;
	}

	private static boolean isBooleanControlFlow(SSAVar var, Set<SSAVar> visited) {
		if (!visited.add(var)) {
			return true;
		}
		if (var.getUseList().isEmpty()) {
			return false;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn instanceof IfNode && isZeroComparison(useArg)) {
				continue;
			}
			if (!(useInsn instanceof ArithNode) || !((ArithNode) useInsn).getOp().isBitOp()) {
				return false;
			}
			RegisterArg result = useInsn.getResult();
			if (result == null || result.getSVar() == null
					|| !isBooleanControlFlow(result.getSVar(), visited)) {
				return false;
			}
		}
		return true;
	}

	static void repairLateBooleanBitFlows(List<SSAVar> vars) {
		boolean changed;
		do {
			changed = false;
			for (SSAVar var : vars) {
				CodeVar codeVar = var.getCodeVar();
				ArgType type = codeVar.getType();
				if (type != null && type.isTypeKnown()
						|| !isBooleanBitValueInsn(var.getAssignInsn(), new HashSet<>())) {
					continue;
				}
				Set<SSAVar> flowVars = new HashSet<>();
				if (!isLateBooleanControlFlow(var, flowVars)) {
					continue;
				}
				for (SSAVar flowVar : flowVars) {
					CodeVar flowCodeVar = flowVar.getCodeVar();
					ArgType flowType = flowCodeVar.getType();
					if (flowType == null || !flowType.isTypeKnown()) {
						List<SSAVar> codeVars = flowCodeVar.getSsaVars();
						markCodeVarType(flowCodeVar,
								codeVars.isEmpty() ? List.of(flowVar) : codeVars,
								ArgType.BOOLEAN);
						changed = true;
					}
				}
			}
		} while (changed);
	}

	/**
	 * Recover a reference type for a late PHI rooted in a null check. Region
	 * transformations can introduce synthetic moves after the regular inference pass, leaving a
	 * reference-only PHI with the flexible DEX comparison {@code value == 0}. Treat zero as null
	 * only when every value edge has reference-only evidence and all generated uses agree. Preserve
	 * a single concrete array input when an array-length use proves that the PHI must remain an array.
	 */
	static void repairLateReferenceNullFlows(List<SSAVar> vars) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			boolean hasPhi = false;
			boolean hasReferenceEvidence = false;
			boolean hasNullCheck = false;
			boolean hasArrayLengthUse = false;
			boolean valid = true;
			Set<ArgType> concreteInputTypes = new LinkedHashSet<>();
			Set<ArgType> terminalUseTypes = new LinkedHashSet<>();
			for (SSAVar var : entry.getValue()) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn == null
						|| assignInsn.getType() != InsnType.MOVE
								&& assignInsn.getType() != InsnType.PHI
								&& assignInsn.getType() != InsnType.CONST) {
					valid = false;
					break;
				}
				hasPhi |= assignInsn.getType() == InsnType.PHI;
				for (InsnArg arg : assignInsn.getArguments()) {
					if (arg.isZeroConst()) {
						continue;
					}
					if (!arg.isRegister()) {
						valid = false;
						break;
					}
					RegisterArg reg = (RegisterArg) arg;
					SSAVar inputVar = reg.getSVar();
					if (inputVar != null && inputVar.getCodeVar() == codeVar) {
						continue;
					}
					ArgType inputType = getKnownValueType(reg, new HashSet<>());
					if (inputType != null && inputType.isTypeKnown()
							&& (inputType.isObject() || inputType.isArray())) {
						hasReferenceEvidence = true;
						concreteInputTypes.add(inputType);
						continue;
					}
					if (!isReferenceOnlyType(reg.getInitType())) {
						valid = false;
						break;
					}
					hasReferenceEvidence = true;
				}
				if (!valid) {
					break;
				}
				for (RegisterArg useArg : var.getUseList()) {
					InsnNode useInsn = useArg.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					if (useInsn instanceof IfNode && isZeroComparison(useArg)) {
						hasNullCheck = true;
						continue;
					}
					if (useInsn.getType() == InsnType.ARRAY_LENGTH) {
						hasArrayLengthUse = true;
						continue;
					}
					ArgType useType = useArg.getInitType();
					if (useType.isTypeKnown() && (useType.isObject() || useType.isArray())) {
						terminalUseTypes.add(useType);
						continue;
					}
					RegisterArg result = useInsn.getResult();
					if ((useInsn.getType() == InsnType.MOVE || useInsn.getType() == InsnType.PHI)
							&& result != null && result.getSVar() != null
							&& result.getSVar().getCodeVar() == codeVar) {
						continue;
					}
					if (useInsn.getType() == InsnType.MOVE && useInsn.contains(AFlag.SYNTHETIC)
							&& result != null && result.getSVar() != null) {
						ArgType targetType = result.getSVar().getCodeVar().getType();
						if (targetType != null && targetType.isTypeKnown()
								&& (targetType.isObject() || targetType.isArray())) {
							terminalUseTypes.add(targetType);
							continue;
						}
					}
					valid = false;
					break;
				}
				if (!valid) {
					break;
				}
			}
			ArgType singleConcreteType = concreteInputTypes.size() == 1
					? concreteInputTypes.iterator().next()
					: null;
			boolean compatibleConcreteUses = singleConcreteType != null && terminalUseTypes.stream()
					.allMatch(type -> type.equals(ArgType.OBJECT) || type.equals(singleConcreteType));
			if (valid && hasPhi && hasReferenceEvidence && hasNullCheck
					&& (terminalUseTypes.size() <= 1 || compatibleConcreteUses)) {
				ArgType inferredType = singleConcreteType != null
						&& (hasArrayLengthUse || terminalUseTypes.size() > 1)
						? singleConcreteType
						: terminalUseTypes.isEmpty()
								? ArgType.OBJECT
						: terminalUseTypes.iterator().next();
				boolean validArrayType = !hasArrayLengthUse || inferredType.isArray();
				boolean compatibleUses = terminalUseTypes.stream()
						.allMatch(type -> type.equals(ArgType.OBJECT) || type.equals(inferredType));
				if (validArrayType && compatibleUses
						&& (inferredType == ArgType.OBJECT
								|| concreteInputTypes.stream().allMatch(type -> type.equals(inferredType)))) {
					markCodeVarType(codeVar, entry.getValue(), inferredType);
				}
			}
		}
	}

	/**
	 * Preserve an exact array type when generic erasure adds an {@link Object} use after inference.
	 * This recovery is deliberately limited to one SSA value produced directly by NEW_ARRAY; PHIs,
	 * moves and conflicting concrete consumers still require normal type inference.
	 */
	static void repairLateExactArrayFlows(List<SSAVar> vars) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (currentType != null && currentType.isTypeKnown() || group.size() != 1) {
				continue;
			}
			SSAVar var = group.get(0);
			InsnNode assignInsn = var.getAssignInsn();
			RegisterArg assign = var.getAssign();
			if (assignInsn == null || assignInsn.getType() != InsnType.NEW_ARRAY || assign == null) {
				continue;
			}
			ArgType arrayType = assign.getInitType();
			if (!arrayType.isTypeKnown() || !arrayType.isArray()) {
				continue;
			}
			boolean hasGeneratedUse = false;
			boolean compatibleUses = true;
			for (RegisterArg useArg : var.getUseList()) {
				InsnNode useInsn = useArg.getParentInsn();
				if (useInsn == null) {
					compatibleUses = false;
					break;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				hasGeneratedUse = true;
				ArgType useType = useArg.getInitType();
				if (!useType.equals(arrayType) && !useType.equals(ArgType.OBJECT)) {
					compatibleUses = false;
					break;
				}
			}
			if (hasGeneratedUse && compatibleUses) {
				markCodeVarType(codeVar, group, arrayType);
			}
		}
	}

	/**
	 * Recover {@link Object} for a PHI merging incompatible reference or array implementations.
	 * DEX can reuse one register for {@code Object[]}, primitive arrays and null; Java cannot assign
	 * one array type to another, but all are safely represented by {@code Object}. Require at least
	 * two distinct concrete reference inputs and reject every primitive consumer.
	 */
	static void repairLateMixedReferenceObjectFlows(List<SSAVar> vars) {
		boolean changed;
		do {
			changed = false;
			for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
				CodeVar codeVar = entry.getKey();
				ArgType currentType = codeVar.getType();
				if (currentType != null && currentType.isTypeKnown()) {
					continue;
				}
				Set<ArgType> inputTypes = new LinkedHashSet<>();
				boolean hasPhi = false;
				boolean hasGeneratedUse = false;
				boolean valid = true;
				for (SSAVar var : entry.getValue()) {
					InsnNode assignInsn = var.getAssignInsn();
					if (assignInsn instanceof PhiInsn) {
						hasPhi = true;
						for (InsnArg arg : assignInsn.getArguments()) {
							if (!isRegisterFromCodeVar(arg, codeVar)) {
								valid = false;
								break;
							}
						}
					} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
							&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
						ArgType inputType = getKnownValueType(
								(RegisterArg) assignInsn.getArg(0), new HashSet<>());
						if (!isConcreteReferenceType(inputType)) {
							valid = false;
						} else {
							inputTypes.add(inputType);
						}
					} else if (assignInsn instanceof FilledNewArrayNode) {
						inputTypes.add(((FilledNewArrayNode) assignInsn).getArrayType());
					} else if (assignInsn != null && assignInsn.getType() == InsnType.CONST
							&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()) {
						// Null is compatible with the selected Object type but is not type evidence.
					} else {
						valid = false;
					}
					if (!valid) {
						break;
					}
					for (RegisterArg use : var.getUseList()) {
						InsnNode useInsn = use.getParentInsn();
						if (useInsn == null) {
							valid = false;
							break;
						}
						if (useInsn.contains(AFlag.DONT_GENERATE)) {
							continue;
						}
						hasGeneratedUse = true;
						if (useInsn.getType() == InsnType.INSTANCE_OF
								|| useInsn.getType() == InsnType.CHECK_CAST
								|| isSyntheticReferenceForwardMove(useInsn, codeVar)) {
							continue;
						}
						valid = false;
						break;
					}
					if (!valid) {
						break;
					}
				}
				if (valid && hasPhi && hasGeneratedUse && inputTypes.size() >= 2) {
					markCodeVarType(codeVar, entry.getValue(), ArgType.OBJECT);
					changed = true;
				}
			}
		} while (changed);
	}

	private static boolean isConcreteReferenceType(ArgType type) {
		return type != null && type.isTypeKnown() && (type.isObject() || type.isArray());
	}

	/**
	 * A catch-all edge can observe a DEX register before a reference constructor completes. If that
	 * register previously held a compare result or coroutine state label, SSA can connect the
	 * primitive value to the handler's cleanup local and emit invalid Java such as
	 * {@code closeable = compareResult}. Java represents the not-yet-initialized cleanup value as
	 * {@code null}. Repair only an isolated, exactly proven int producer whose normal generated uses
	 * are compatible branches and whose conflicting uses are reference MOVEs entirely inside an
	 * exception handler.
	 */
	static int repairLateExceptionPrimitiveToReferenceMoves(MethodNode mth) {
		return repairLateExceptionPrimitiveToReferenceMoves(
				mth.getSVars(), insn -> isInExceptionHandler(mth, insn));
	}

	static int repairLateStaleConstStringHandlerPhis(MethodNode mth) {
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg == null || thisArg.getSVar() == null) {
			return 0;
		}
		ArgType thisType = mth.getParentClass().getType();
		int repaired = 0;
		for (List<SSAVar> group : collectCodeVarGroups(mth.getSVars()).values()) {
			boolean fixed = repairStaleConstStringHandlerPhi(
					group, thisArg.getSVar(), thisType, insn -> isInExceptionHandler(mth, insn));
			if (fixed) {
				repaired++;
			}
		}
		return repaired;
	}

	/**
	 * A protected reflective call can reuse the register holding its method-name string as an
	 * integer status before the call. Exception SSA occasionally keeps the earlier string on the
	 * call's catch PHI, joining it to an otherwise closed integer MOVE/PHI flow. Detach the string
	 * lifetime only when the complete connected flow has exact integer roots and terminal integer
	 * comparisons; this avoids choosing a primitive type for a genuinely mixed register.
	 */
	static int repairLateStaleConstStringHandlerIntFlows(MethodNode mth) {
		if (!hasStaleConstStringHandlerIntFlowCandidate(mth)) {
			return 0;
		}
		Map<CodeVar, List<SSAVar>> groups = collectCodeVarGroups(mth);
		Set<CodeVar> oldCodeVars = Collections.newSetFromMap(new IdentityHashMap<>());
		oldCodeVars.addAll(groups.keySet());
		if (!repairStaleConstStringHandlerIntFlow(
				groups, insn -> isInExceptionHandler(mth, insn))) {
			return 0;
		}
		DeclareVariablesAttr declareVariables = mth.getRegion().get(AType.DECLARE_VARIABLES);
		if (declareVariables == null) {
			declareVariables = new DeclareVariablesAttr();
			mth.getRegion().addAttr(declareVariables);
		}
		Set<CodeVar> declared = Collections.newSetFromMap(new IdentityHashMap<>());
		for (SSAVar var : mth.getSVars()) {
			CodeVar codeVar = var.getCodeVar();
			if (!oldCodeVars.contains(codeVar) && ArgType.INT.equals(codeVar.getType())
					&& !codeVar.isDeclared() && declared.add(codeVar)) {
				declareVariables.addVar(codeVar);
				codeVar.setDeclared(true);
			}
		}
		return 1;
	}

	private static boolean hasStaleConstStringHandlerIntFlowCandidate(MethodNode mth) {
		for (SSAVar var : mth.getSVars()) {
			if (!ArgType.STRING.equals(var.getCodeVar().getType()) || var.getUseCount() != 1) {
				continue;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null || assignInsn.getType() != InsnType.CONST_STR) {
				continue;
			}
			InsnNode useInsn = var.getUseList().get(0).getParentInsn();
			if (useInsn instanceof PhiInsn && useInsn.getArgsCount() == 2
					&& isInExceptionHandler(mth, useInsn)) {
				return true;
			}
		}
		return false;
	}

	static boolean repairStaleConstStringHandlerIntFlow(
			Map<CodeVar, List<SSAVar>> groups, Predicate<InsnNode> isHandlerInsn) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar stringCodeVar = entry.getKey();
			List<SSAVar> stringGroup = entry.getValue();
			if (!ArgType.STRING.equals(stringCodeVar.getType()) || stringGroup.size() < 3) {
				continue;
			}
			SSAVar staleString = null;
			RegisterArg stalePhiUse = null;
			PhiInsn handlerPhi = null;
			for (SSAVar var : stringGroup) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn == null || assignInsn.getType() != InsnType.CONST_STR || var.getUseCount() != 1) {
					continue;
				}
				RegisterArg use = var.getUseList().get(0);
				InsnNode useInsn = use.getParentInsn();
				if (!(useInsn instanceof PhiInsn) || useInsn.getArgsCount() != 2
						|| !isHandlerInsn.test(useInsn)
						|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null
						|| useInsn.getResult().getSVar().getCodeVar() != stringCodeVar
						|| staleString != null) {
					return false;
				}
				staleString = var;
				stalePhiUse = use;
				handlerPhi = (PhiInsn) useInsn;
			}
			if (staleString == null) {
				continue;
			}
			RegisterArg livePhiInput = null;
			for (InsnArg arg : handlerPhi.getArguments()) {
				if (arg == stalePhiUse) {
					continue;
				}
				if (!arg.isRegister() || ((RegisterArg) arg).getSVar() == null
						|| ((RegisterArg) arg).getSVar().getCodeVar() != stringCodeVar) {
					return false;
				}
				livePhiInput = (RegisterArg) arg;
			}
			if (livePhiInput == null) {
				return false;
			}

			Set<SSAVar> intFlow = Collections.newSetFromMap(new IdentityHashMap<>());
			ArrayDeque<SSAVar> queue = new ArrayDeque<>();
			for (SSAVar var : stringGroup) {
				if (var != staleString && intFlow.add(var)) {
					queue.add(var);
				}
			}
			while (!queue.isEmpty()) {
				SSAVar var = queue.removeFirst();
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn != null) {
					for (InsnArg arg : assignInsn.getArguments()) {
						if (arg.isRegister()) {
							addUnknownIntFlowGroup(groups, stringCodeVar, staleString,
									((RegisterArg) arg).getSVar(), intFlow, queue);
						}
					}
				}
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					RegisterArg result = useInsn == null ? null : useInsn.getResult();
					if (result != null && result.getSVar() != null
							&& (useInsn instanceof PhiInsn || useInsn.getType() == InsnType.MOVE)) {
						addUnknownIntFlowGroup(groups, stringCodeVar, staleString,
								result.getSVar(), intFlow, queue);
					}
				}
			}
			String intName = validateClosedHandlerIntFlow(intFlow, staleString, handlerPhi);
			if (intName == null) {
				return false;
			}

			RegisterArg replacement = livePhiInput.duplicate();
			replacement.forceSetInitType(ArgType.INT);
			if (!handlerPhi.replaceArg(stalePhiUse, replacement)) {
				return false;
			}
			livePhiInput.getSVar().use(replacement);

			Map<CodeVar, List<SSAVar>> intGroups = new IdentityHashMap<>();
			for (SSAVar var : intFlow) {
				intGroups.computeIfAbsent(var.getCodeVar(), key -> new ArrayList<>()).add(var);
			}
			List<SSAVar> splitStringVars = intGroups.remove(stringCodeVar);
			if (splitStringVars == null || splitStringVars.isEmpty()) {
				return false;
			}
			stringCodeVar.setSsaVars(List.of(staleString));
			CodeVar splitIntCodeVar = new CodeVar();
			splitIntCodeVar.setSsaVars(splitStringVars);
			for (SSAVar var : splitStringVars) {
				var.setCodeVar(splitIntCodeVar);
			}
			markCodeVarType(splitIntCodeVar, splitStringVars, ArgType.INT);
			for (Map.Entry<CodeVar, List<SSAVar>> intEntry : intGroups.entrySet()) {
				CodeVar intCodeVar = intEntry.getKey();
				List<SSAVar> intVars = intEntry.getValue();
				intCodeVar.setSsaVars(intVars);
				markCodeVarType(intCodeVar, intVars, ArgType.INT);
			}
			return true;
		}
		return false;
	}

	private static void addUnknownIntFlowGroup(
			Map<CodeVar, List<SSAVar>> groups, CodeVar stringCodeVar, SSAVar staleString,
			SSAVar candidate, Set<SSAVar> intFlow, ArrayDeque<SSAVar> queue) {
		if (candidate == null || candidate == staleString) {
			return;
		}
		CodeVar codeVar = candidate.getCodeVar();
		ArgType type = codeVar.getType();
		if (codeVar != stringCodeVar && type != null && type.isTypeKnown()) {
			return;
		}
		List<SSAVar> group = groups.get(codeVar);
		if (group == null) {
			return;
		}
		for (SSAVar var : group) {
			if (var != staleString && intFlow.add(var)) {
				queue.add(var);
			}
		}
	}

	private static String validateClosedHandlerIntFlow(
			Set<SSAVar> intFlow, SSAVar staleString, PhiInsn handlerPhi) {
		Set<CodeVar> flowCodeVars = Collections.newSetFromMap(new IdentityHashMap<>());
		String boundaryName = null;
		boolean hasExactIntRoot = false;
		boolean hasIntLiteral = false;
		boolean hasTerminalComparison = false;
		for (SSAVar var : intFlow) {
			flowCodeVars.add(var.getCodeVar());
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null) {
				return null;
			}
			if (assignInsn.getType() == InsnType.CONST) {
				if (assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isLiteral()
						|| !assignInsn.getArg(0).getType().canBePrimitive(PrimitiveType.INT)) {
					return null;
				}
				hasIntLiteral = true;
			} else if (assignInsn instanceof PhiInsn || assignInsn.getType() == InsnType.MOVE) {
				for (InsnArg arg : assignInsn.getArguments()) {
					if (!arg.isRegister()) {
						return null;
					}
					SSAVar source = ((RegisterArg) arg).getSVar();
					if (source == staleString && assignInsn == handlerPhi) {
						continue;
					}
					if (source != null && intFlow.contains(source)) {
						continue;
					}
					if (!ArgType.INT.equals(getKnownValueType((RegisterArg) arg, new HashSet<>()))) {
						return null;
					}
					hasExactIntRoot = true;
					String name = source == null ? null : source.getCodeVar().getName();
					if (name != null && !name.isEmpty() && boundaryName == null) {
						boundaryName = name;
					}
				}
			} else {
				return null;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				RegisterArg result = useInsn.getResult();
				if ((useInsn instanceof PhiInsn || useInsn.getType() == InsnType.MOVE)
						&& result != null && result.getSVar() != null
						&& intFlow.contains(result.getSVar())) {
					continue;
				}
				if (isIntegralLiteralComparison(use)) {
					hasTerminalComparison = true;
					continue;
				}
				return null;
			}
		}
		return flowCodeVars.size() >= 2 && hasExactIntRoot && hasIntLiteral
				&& hasTerminalComparison && boundaryName != null ? boundaryName : null;
	}

	/**
	 * A catch PHI can retain an earlier string constant from a reused DEX register even though every
	 * live handler path needs the enclosing instance. Replace only that dead edge with the dominating
	 * {@code this} value after proving that all other assignments and generated uses are of the
	 * enclosing type.
	 */
	static boolean repairStaleConstStringHandlerPhi(
			List<SSAVar> group, SSAVar thisVar, ArgType thisType, Predicate<InsnNode> isHandlerInsn) {
		if (group.size() < 3 || !sameRawObjectType(thisType, thisVar.getCodeVar().getType())) {
			return false;
		}
		SSAVar staleString = null;
		RegisterArg stalePhiUse = null;
		PhiInsn handlerPhi = null;
		for (SSAVar var : group) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null || assignInsn.getType() != InsnType.CONST_STR || var.getUseCount() != 1) {
				continue;
			}
			RegisterArg use = var.getUseList().get(0);
			InsnNode useInsn = use.getParentInsn();
			if (!(useInsn instanceof PhiInsn) || !isHandlerInsn.test(useInsn)
					|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null
					|| useInsn.getResult().getSVar().getCodeVar() != var.getCodeVar()
					|| staleString != null) {
				return false;
			}
			staleString = var;
			stalePhiUse = use;
			handlerPhi = (PhiInsn) useInsn;
		}
		if (staleString == null || !isThisReferenceGroupAfterStaleInput(
				group, staleString, handlerPhi, thisType)) {
			return false;
		}
		String boundaryName = findThisReferenceBoundaryName(group, staleString.getCodeVar(), thisType);
		if (boundaryName == null) {
			return false;
		}
		RegisterArg replacement = thisVar.getAssign().duplicate();
		replacement.forceSetInitType(thisType);
		if (!handlerPhi.replaceArg(stalePhiUse, replacement)) {
			return false;
		}
		thisVar.use(replacement);
		staleString.getAssignInsn().add(AFlag.DONT_GENERATE);

		CodeVar sharedCodeVar = staleString.getCodeVar();
		List<SSAVar> remaining = new ArrayList<>(group);
		remaining.remove(staleString);
		CodeVar stringCodeVar = new CodeVar();
		stringCodeVar.setType(ArgType.STRING);
		stringCodeVar.setSsaVars(List.of(staleString));
		staleString.setCodeVar(stringCodeVar);
		sharedCodeVar.setSsaVars(remaining);
		sharedCodeVar.setName(boundaryName);
		markCodeVarType(sharedCodeVar, remaining, thisType);
		return true;
	}

	private static String findThisReferenceBoundaryName(
			List<SSAVar> group, CodeVar sharedCodeVar, ArgType thisType) {
		for (SSAVar var : group) {
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				RegisterArg result = useInsn == null ? null : useInsn.getResult();
				if (useInsn == null || useInsn.getType() != InsnType.MOVE
						|| result == null || result.getSVar() == null
						|| result.getSVar().getCodeVar() == sharedCodeVar
						|| !sameRawObjectType(thisType, result.getSVar().getCodeVar().getType())) {
					continue;
				}
				String name = result.getSVar().getCodeVar().getName();
				if (name != null && !name.isEmpty()) {
					return name;
				}
			}
		}
		return null;
	}

	private static boolean isThisReferenceGroupAfterStaleInput(
			List<SSAVar> group, SSAVar staleString, PhiInsn handlerPhi, ArgType thisType) {
		CodeVar sharedCodeVar = staleString.getCodeVar();
		for (InsnArg arg : handlerPhi.getArguments()) {
			if (!arg.isRegister()) {
				return false;
			}
			SSAVar input = ((RegisterArg) arg).getSVar();
			if (input != staleString && (input == null || input.getCodeVar() != sharedCodeVar)) {
				return false;
			}
		}
		boolean hasThisRoot = false;
		for (SSAVar var : group) {
			if (var == staleString) {
				continue;
			}
			ArgType knownType = var.getTypeInfo().getType();
			if (knownType.isTypeKnown() && !sameRawObjectType(thisType, knownType)) {
				return false;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn) && (assignInsn == null || assignInsn.getType() != InsnType.MOVE)) {
				return false;
			}
			for (InsnArg assignArg : assignInsn.getArguments()) {
				if (!assignArg.isRegister()) {
					return false;
				}
				SSAVar sourceVar = ((RegisterArg) assignArg).getSVar();
				if (sourceVar != null && sourceVar.getCodeVar() == sharedCodeVar) {
					if (sourceVar == staleString && assignInsn != handlerPhi) {
						return false;
					}
					continue;
				}
				if (!sameRawObjectType(thisType, getKnownValueType(
						(RegisterArg) assignArg, new HashSet<>()))) {
					return false;
				}
				hasThisRoot = true;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return false;
				}
				RegisterArg result = useInsn.getResult();
				if (useInsn instanceof PhiInsn && result != null && result.getSVar() != null
						&& result.getSVar().getCodeVar() == staleString.getCodeVar()) {
					continue;
				}
				if (useInsn.getType() == InsnType.MOVE && result != null && result.getSVar() != null
						&& sameRawObjectType(thisType, result.getSVar().getCodeVar().getType())) {
					continue;
				}
				if (useInsn instanceof InvokeNode && ((InvokeNode) useInsn).getInstanceArg() == use
						&& sameRawObjectType(thisType, ((InvokeNode) useInsn).getCallMth().getDeclClass().getType())) {
					continue;
				}
				return false;
			}
		}
		return hasThisRoot;
	}

	static int repairLateExceptionPrimitiveToReferenceMoves(
			List<SSAVar> vars, Function<InsnNode, Boolean> isHandlerInsn) {
		int repaired = reconnectHandlerPhiInvokeReceiver(vars, isHandlerInsn);
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeType = codeVar.getType();
			if (codeVar.isThis()) {
				continue;
			}
			if (ArgType.INT.equals(codeType)) {
				repaired += replaceKnownIntExceptionReferenceMoves(vars, group, isHandlerInsn);
				continue;
			}
			if (group.size() != 1 || codeType != null && codeType.isTypeKnown()) {
				continue;
			}
			SSAVar var = group.get(0);
			InsnNode assignInsn = var.getAssignInsn();
			ArgType primitiveType = getExactExceptionPrimitiveType(assignInsn);
			if (primitiveType == null) {
				continue;
			}
			List<Map.Entry<InsnNode, ArgType>> handlerMoves = new ArrayList<>();
			List<Map.Entry<InsnNode, ArgType>> handlerCasts = new ArrayList<>();
			List<Map.Entry<InsnNode, ArgType>> handlerInvokes = new ArrayList<>();
			List<Map.Entry<RegisterArg, SSAVar>> handlerPhiInputs = new ArrayList<>();
			List<Map.Entry<RegisterArg, Map.Entry<SSAVar, ArgType>>> handlerPhiDirectInputs = new ArrayList<>();
			boolean hasIntUse = false;
			boolean valid = true;
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					valid = false;
					break;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					SSAVar methodArg = findHandlerPhiReferenceMethodArg(vars, useInsn, use, isHandlerInsn);
					if (methodArg != null) {
						handlerPhiInputs.add(Map.entry(use, methodArg));
					} else {
						Map.Entry<SSAVar, ArgType> directInput = findHandlerPhiDirectReferenceInput(
								useInsn, use, isHandlerInsn);
						if (directInput != null) {
							handlerPhiDirectInputs.add(Map.entry(use, directInput));
						}
					}
					continue;
				}
				if (isIntegralLiteralComparison(use)
						|| useInsn.getType() == InsnType.SWITCH
								&& useInsn.getArgsCount() == 1 && useInsn.getArg(0) == use) {
					hasIntUse = true;
					continue;
				}
				ArgType castType = getHandlerReferenceCastType(useInsn, use, isHandlerInsn);
				if (castType != null) {
					handlerCasts.add(Map.entry(useInsn, castType));
					continue;
				}
				ArgType invokeType = getHandlerReferenceInvokeType(useInsn, use, isHandlerInsn);
				if (invokeType == null) {
					invokeType = getHandlerStaticFirstArgType(useInsn, use, isHandlerInsn);
				}
				if (invokeType != null) {
					handlerInvokes.add(Map.entry(useInsn, invokeType));
					continue;
				}
				if (useInsn.getType() != InsnType.MOVE || useInsn.getArgsCount() != 1
						|| useInsn.getArg(0) != use || !isHandlerInsn.apply(useInsn)
						|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null) {
					valid = false;
					break;
				}
				ArgType targetType = useInsn.getResult().getSVar().getCodeVar().getType();
				if (!isConcreteReferenceType(targetType)) {
					valid = false;
					break;
				}
				handlerMoves.add(Map.entry(useInsn, targetType));
			}
			if (!valid || !hasIntUse
					|| handlerMoves.isEmpty() && handlerCasts.isEmpty()
							&& handlerInvokes.isEmpty() && handlerPhiInputs.isEmpty()
							&& handlerPhiDirectInputs.isEmpty()) {
				continue;
			}
			Map<InsnNode, SSAVar> castCleanupVars = new LinkedHashMap<>();
			List<Map.Entry<InsnNode, ArgType>> handlerReferenceUses = new ArrayList<>(handlerCasts);
			handlerReferenceUses.addAll(handlerInvokes);
			for (Map.Entry<InsnNode, ArgType> handlerReferenceUse : handlerReferenceUses) {
				SSAVar cleanupVar = findExceptionCleanupVar(vars, var, handlerReferenceUse.getValue());
				if (cleanupVar == null) {
					valid = false;
					break;
				}
				castCleanupVars.put(handlerReferenceUse.getKey(), cleanupVar);
			}
			if (!valid) {
				continue;
			}
			for (Map.Entry<InsnNode, SSAVar> handlerCast : castCleanupVars.entrySet()) {
				InsnNode castInsn = handlerCast.getKey();
				SSAVar cleanupVar = handlerCast.getValue();
				RegisterArg cleanupUse = cleanupVar.getAssign().duplicate();
				cleanupUse.forceSetInitType(cleanupVar.getCodeVar().getType());
				if (castInsn.replaceArg(castInsn.getArg(0), cleanupUse)) {
					cleanupVar.use(cleanupUse);
					repaired++;
				}
			}
			for (Map.Entry<RegisterArg, SSAVar> handlerPhiInput : handlerPhiInputs) {
				RegisterArg primitiveUse = handlerPhiInput.getKey();
				PhiInsn phi = (PhiInsn) primitiveUse.getParentInsn();
				SSAVar methodArg = handlerPhiInput.getValue();
				RegisterArg referenceUse = methodArg.getAssign().duplicate();
				referenceUse.forceSetInitType(methodArg.getCodeVar().getType());
				if (phi.replaceArg(primitiveUse, referenceUse)) {
					methodArg.use(referenceUse);
					markHandlerPhiReferenceCodeVar(vars, phi, methodArg.getCodeVar().getType());
					repaired++;
				}
			}
			for (Map.Entry<RegisterArg, Map.Entry<SSAVar, ArgType>> handlerPhiInput : handlerPhiDirectInputs) {
				RegisterArg primitiveUse = handlerPhiInput.getKey();
				PhiInsn phi = (PhiInsn) primitiveUse.getParentInsn();
				SSAVar referenceVar = handlerPhiInput.getValue().getKey();
				ArgType referenceType = handlerPhiInput.getValue().getValue();
				RegisterArg referenceUse = referenceVar.getAssign().duplicate();
				referenceUse.forceSetInitType(referenceType);
				if (phi.replaceArg(primitiveUse, referenceUse)) {
					referenceVar.use(referenceUse);
					markHandlerPhiReferenceCodeVar(vars, phi, referenceType);
					repaired++;
				}
			}
			for (Map.Entry<InsnNode, ArgType> handlerMove : handlerMoves) {
				InsnNode moveInsn = handlerMove.getKey();
				if (moveInsn.replaceArg(moveInsn.getArg(0), LiteralArg.make(0, handlerMove.getValue()))) {
					repaired++;
				}
			}
			markCodeVarType(codeVar, group, primitiveType);
		}
		return repaired;
	}

	/**
	 * A catch-all PHI can merge a coroutine state integer with a later {@code instanceof} result,
	 * even though the handler actually needs the reference cleanup local occupying the same DEX
	 * register on the protected path. Reconnect an invoke receiver only when it is the PHI's sole
	 * generated role and a unique, already typed cleanup PHI exists. Detach the normal
	 * {@code instanceof} lifetime as boolean instead of assigning a reference type to the mixed PHI.
	 */
	private static int reconnectHandlerPhiInvokeReceiver(
			List<SSAVar> vars, Function<InsnNode, Boolean> isHandlerInsn) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType codeType = codeVar.getType();
			if (codeType != null && codeType.isTypeKnown() || codeVar.isThis()) {
				continue;
			}
			List<SSAVar> group = entry.getValue();
			List<SSAVar> instanceOfVars = new ArrayList<>();
			SSAVar handlerPhiVar = null;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn != null && assignInsn.getType() == InsnType.INSTANCE_OF) {
					instanceOfVars.add(var);
				} else if (assignInsn instanceof PhiInsn && handlerPhiVar == null) {
					handlerPhiVar = var;
				} else {
					handlerPhiVar = null;
					break;
				}
			}
			if (handlerPhiVar == null || instanceOfVars.isEmpty()) {
				continue;
			}
			List<RegisterArg> handlerReceivers = new ArrayList<>();
			ArgType receiverType = null;
			boolean valid = true;
			for (RegisterArg use : new ArrayList<>(handlerPhiVar.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					valid = false;
					break;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				ArgType invokeType = getHandlerReferenceInvokeType(useInsn, use, isHandlerInsn);
				if (invokeType == null || receiverType != null && !receiverType.equals(invokeType)) {
					valid = false;
					break;
				}
				receiverType = invokeType;
				handlerReceivers.add(use);
			}
			if (!valid || receiverType == null || handlerReceivers.isEmpty()) {
				continue;
			}
			SSAVar cleanupVar = findExceptionCleanupVar(vars, handlerPhiVar, receiverType);
			if (cleanupVar == null) {
				cleanupVar = findUniqueExceptionCleanupVar(vars, receiverType);
			}
			if (cleanupVar == null) {
				continue;
			}
			for (RegisterArg handlerReceiver : handlerReceivers) {
				InsnNode invokeInsn = handlerReceiver.getParentInsn();
				RegisterArg cleanupUse = cleanupVar.getAssign().duplicate();
				cleanupUse.forceSetInitType(receiverType);
				if (!invokeInsn.replaceArg(handlerReceiver, cleanupUse)) {
					valid = false;
					break;
				}
				cleanupVar.use(cleanupUse);
			}
			if (!valid) {
				continue;
			}
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.removeAll(instanceOfVars);
			CodeVar hiddenHandlerCodeVar = new CodeVar();
			for (SSAVar remainingVar : remaining) {
				remainingVar.setCodeVar(hiddenHandlerCodeVar);
			}
			hiddenHandlerCodeVar.setSsaVars(remaining);
			codeVar.setSsaVars(new ArrayList<>(instanceOfVars));
			markCodeVarType(codeVar, instanceOfVars, ArgType.BOOLEAN);
			return handlerReceivers.size();
		}
		return 0;
	}

	private static SSAVar findUniqueExceptionCleanupVar(List<SSAVar> vars, ArgType targetType) {
		CodeVar foundCodeVar = null;
		SSAVar foundPhiVar = null;
		for (SSAVar candidate : vars) {
			CodeVar candidateCodeVar = candidate.getCodeVar();
			if (!targetType.equals(candidateCodeVar.getType())
					|| !(candidate.getAssignInsn() instanceof PhiInsn)
					|| !hasGeneratedUse(candidate)) {
				continue;
			}
			if (foundCodeVar != null && foundCodeVar != candidateCodeVar) {
				return null;
			}
			foundCodeVar = candidateCodeVar;
			foundPhiVar = candidate;
		}
		return foundPhiVar;
	}

	private static SSAVar findHandlerPhiReferenceMethodArg(
			List<SSAVar> vars, InsnNode useInsn, RegisterArg primitiveUse,
			Function<InsnNode, Boolean> isHandlerInsn) {
		if (!(useInsn instanceof PhiInsn) || !isHandlerInsn.apply(useInsn)
				|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null) {
			return null;
		}
		CodeVar phiCodeVar = useInsn.getResult().getSVar().getCodeVar();
		boolean hasReferenceInput = false;
		for (InsnArg arg : useInsn.getArguments()) {
			if (arg == primitiveUse) {
				continue;
			}
			if (!(arg instanceof RegisterArg)
					|| ((RegisterArg) arg).getSVar() == null
					|| ((RegisterArg) arg).getSVar().getCodeVar() != phiCodeVar) {
				return null;
			}
			hasReferenceInput = true;
		}
		if (!hasReferenceInput) {
			return null;
		}

		Set<ArgType> terminalTypes = new LinkedHashSet<>();
		for (SSAVar candidate : vars) {
			if (candidate.getCodeVar() != phiCodeVar) {
				continue;
			}
			for (RegisterArg use : candidate.getUseList()) {
				InsnNode terminalInsn = use.getParentInsn();
				if (terminalInsn == null || terminalInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				ArgType type = use.getInitType();
				if (type.isPrimitive()) {
					return null;
				}
				if (isConcreteReferenceType(type) && !ArgType.OBJECT.equals(type)) {
					terminalTypes.add(type);
				}
			}
		}
		if (terminalTypes.size() != 1) {
			return null;
		}
		ArgType targetType = terminalTypes.iterator().next();
		SSAVar found = null;
		for (SSAVar candidate : vars) {
			if (candidate.getAssignInsn() != null || candidate.getCodeVar() == phiCodeVar
					|| !targetType.equals(candidate.getCodeVar().getType())) {
				continue;
			}
			if (found != null && found.getCodeVar() != candidate.getCodeVar()) {
				return null;
			}
			found = candidate;
		}
		return found;
	}

	/**
	 * A coroutine catch-all can attach its state {@code label:int} to a reference cleanup PHI whose
	 * real input is already connected to an exact typed synthetic MOVE. Recover that input only when
	 * the handler result has one generated role: a cast to the same concrete reference type.
	 */
	private static Map.Entry<SSAVar, ArgType> findHandlerPhiDirectReferenceInput(
			InsnNode useInsn, RegisterArg primitiveUse, Function<InsnNode, Boolean> isHandlerInsn) {
		if (!(useInsn instanceof PhiInsn)
				|| !isHandlerInsn.apply(useInsn)
						&& !isHandlerPhiInput((PhiInsn) useInsn, primitiveUse, isHandlerInsn)
				|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null) {
			return null;
		}
		SSAVar phiResult = useInsn.getResult().getSVar();
		CodeVar phiCodeVar = phiResult.getCodeVar();
		SSAVar referenceInput = null;
		for (InsnArg arg : useInsn.getArguments()) {
			if (arg == primitiveUse) {
				continue;
			}
			if (!(arg instanceof RegisterArg) || ((RegisterArg) arg).getSVar() == null
					|| ((RegisterArg) arg).getSVar().getCodeVar() != phiCodeVar) {
				return null;
			}
			SSAVar inputVar = ((RegisterArg) arg).getSVar();
			if (referenceInput != null && referenceInput != inputVar) {
				return null;
			}
			referenceInput = inputVar;
		}
		if (referenceInput == null) {
			return null;
		}

		ArgType targetType = null;
		int generatedUses = 0;
		for (RegisterArg use : phiResult.getUseList()) {
			InsnNode terminalInsn = use.getParentInsn();
			if (terminalInsn == null) {
				return null;
			}
			if (terminalInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			generatedUses++;
			if (!(terminalInsn instanceof IndexInsnNode)
					|| terminalInsn.getType() != InsnType.CHECK_CAST
					|| terminalInsn.getArgsCount() != 1 || terminalInsn.getArg(0) != use
					|| !(((IndexInsnNode) terminalInsn).getIndex() instanceof ArgType)) {
				return null;
			}
			ArgType castType = (ArgType) ((IndexInsnNode) terminalInsn).getIndex();
			if (!isConcreteReferenceType(castType)
					|| targetType != null && !targetType.equals(castType)) {
				return null;
			}
			targetType = castType;
		}
		if (generatedUses != 1 || targetType == null) {
			return null;
		}

		boolean hasExactBoundary = false;
		for (RegisterArg use : referenceInput.getUseList()) {
			InsnNode boundaryInsn = use.getParentInsn();
			if (boundaryInsn == useInsn || boundaryInsn == null || boundaryInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (boundaryInsn.getType() != InsnType.MOVE || !boundaryInsn.contains(AFlag.SYNTHETIC)
					|| boundaryInsn.getResult() == null || boundaryInsn.getResult().getSVar() == null) {
				continue;
			}
			ArgType boundaryType = boundaryInsn.getResult().getSVar().getCodeVar().getType();
			if (targetType.equals(boundaryType)) {
				hasExactBoundary = true;
			} else if (isConcreteReferenceType(boundaryType)) {
				return null;
			}
		}
		return hasExactBoundary ? Map.entry(referenceInput, targetType) : null;
	}

	private static boolean isHandlerPhiInput(
			PhiInsn phi, RegisterArg input, Function<InsnNode, Boolean> isHandlerInsn) {
		BlockNode inputBlock = phi.getBlockByArg(input);
		if (inputBlock == null) {
			return false;
		}
		for (InsnNode insn : inputBlock.getInstructions()) {
			if (isHandlerInsn.apply(insn)) {
				return true;
			}
		}
		return false;
	}

	private static void markHandlerPhiReferenceCodeVar(List<SSAVar> vars, PhiInsn phi, ArgType type) {
		CodeVar codeVar = phi.getResult().getSVar().getCodeVar();
		List<SSAVar> group = new ArrayList<>();
		for (SSAVar candidate : vars) {
			if (candidate.getCodeVar() == codeVar) {
				group.add(candidate);
			}
		}
		markCodeVarType(codeVar, group, type);
	}

	private static boolean isIntegralLiteralComparison(RegisterArg use) {
		InsnNode useInsn = use.getParentInsn();
		if (!(useInsn instanceof IfNode) || useInsn.getArgsCount() != 2) {
			return false;
		}
		InsnArg first = useInsn.getArg(0);
		InsnArg second = useInsn.getArg(1);
		InsnArg other = first == use ? second : second == use ? first : null;
		return other instanceof LiteralArg && other.getType().canBePrimitive(PrimitiveType.INT);
	}

	private static ArgType getHandlerReferenceCastType(
			InsnNode useInsn, RegisterArg use, Function<InsnNode, Boolean> isHandlerInsn) {
		if (!(useInsn instanceof IndexInsnNode)
				|| useInsn.getType() != InsnType.CHECK_CAST && useInsn.getType() != InsnType.CAST
				|| useInsn.getArgsCount() != 1 || useInsn.getArg(0) != use
				|| !isHandlerInsn.apply(useInsn)) {
			return null;
		}
		Object index = ((IndexInsnNode) useInsn).getIndex();
		return index instanceof ArgType && isConcreteReferenceType((ArgType) index)
				? (ArgType) index
				: null;
	}

	private static ArgType getHandlerReferenceInvokeType(
			InsnNode useInsn, RegisterArg use, Function<InsnNode, Boolean> isHandlerInsn) {
		if (!(useInsn instanceof InvokeNode)
				|| ((InvokeNode) useInsn).getInstanceArg() != use
				|| !isHandlerInsn.apply(useInsn)) {
			return null;
		}
		ArgType receiverType = ((InvokeNode) useInsn).getCallMth().getDeclClass().getType();
		return isConcreteReferenceType(receiverType) && !ArgType.OBJECT.equals(receiverType)
				? receiverType
				: null;
	}

	/**
	 * A catch cleanup helper can receive a stale primitive register as its first static argument,
	 * for example a coroutine {@code label:int} passed to
	 * {@code closeFinally(Closeable, Throwable)}. Return the exact formal reference type only for
	 * that first argument; callers still require a unique typed cleanup PHI in the same register.
	 */
	private static ArgType getHandlerStaticFirstArgType(
			InsnNode useInsn, RegisterArg use, Function<InsnNode, Boolean> isHandlerInsn) {
		if (!(useInsn instanceof InvokeNode) || !((InvokeNode) useInsn).isStaticCall()
				|| useInsn.getArgsCount() == 0 || useInsn.getArg(0) != use
				|| !isHandlerInsn.apply(useInsn)) {
			return null;
		}
		List<ArgType> argTypes = ((InvokeNode) useInsn).getCallMth().getArgumentsTypes();
		if (argTypes.isEmpty()) {
			return null;
		}
		ArgType firstArgType = argTypes.get(0);
		return isConcreteReferenceType(firstArgType) && !ArgType.OBJECT.equals(firstArgType)
				? firstArgType
				: null;
	}

	private static SSAVar findExceptionCleanupVar(List<SSAVar> vars, SSAVar sourceVar, ArgType targetType) {
		CodeVar foundCodeVar = null;
		SSAVar foundPhiVar = null;
		for (SSAVar candidate : vars) {
			CodeVar candidateCodeVar = candidate.getCodeVar();
			if (candidate.getRegNum() != sourceVar.getRegNum()
					|| candidateCodeVar == sourceVar.getCodeVar()
					|| !targetType.equals(candidateCodeVar.getType())
					|| !(candidate.getAssignInsn() instanceof PhiInsn)
					|| !hasGeneratedUse(candidate)) {
				continue;
			}
			if (foundCodeVar != null && foundCodeVar != candidateCodeVar) {
				return null;
			}
			foundCodeVar = candidateCodeVar;
			foundPhiVar = candidate;
		}
		return foundPhiVar;
	}

	private static int replaceKnownIntExceptionReferenceMoves(
			List<SSAVar> allVars, List<SSAVar> vars, Function<InsnNode, Boolean> isHandlerInsn) {
		int repaired = 0;
		for (SSAVar var : vars) {
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode moveInsn = use.getParentInsn();
				SSAVar methodArg = findHandlerPhiReferenceMethodArg(allVars, moveInsn, use, isHandlerInsn);
				if (methodArg != null) {
					PhiInsn phi = (PhiInsn) moveInsn;
					RegisterArg referenceUse = methodArg.getAssign().duplicate();
					referenceUse.forceSetInitType(methodArg.getCodeVar().getType());
					if (phi.replaceArg(use, referenceUse)) {
						methodArg.use(referenceUse);
						markHandlerPhiReferenceCodeVar(allVars, phi, methodArg.getCodeVar().getType());
						repaired++;
					}
					continue;
				}
				Map.Entry<SSAVar, ArgType> directInput = findHandlerPhiDirectReferenceInput(
						moveInsn, use, isHandlerInsn);
				if (directInput != null) {
					PhiInsn phi = (PhiInsn) moveInsn;
					SSAVar referenceVar = directInput.getKey();
					RegisterArg referenceUse = referenceVar.getAssign().duplicate();
					referenceUse.forceSetInitType(directInput.getValue());
					if (phi.replaceArg(use, referenceUse)) {
						referenceVar.use(referenceUse);
						markHandlerPhiReferenceCodeVar(allVars, phi, directInput.getValue());
						repaired++;
					}
					continue;
				}
				if (moveInsn == null || moveInsn.getType() != InsnType.MOVE
						|| !moveInsn.contains(AFlag.SYNTHETIC)
						|| moveInsn.getArgsCount() != 1 || moveInsn.getArg(0) != use
						|| !isHandlerInsn.apply(moveInsn)
						|| moveInsn.getResult() == null || moveInsn.getResult().getSVar() == null) {
					continue;
				}
				ArgType targetType = moveInsn.getResult().getSVar().getCodeVar().getType();
				if (isConcreteReferenceType(targetType)
						&& moveInsn.replaceArg(use, LiteralArg.make(0, targetType))) {
					repaired++;
				}
			}
		}
		return repaired;
	}

	private static ArgType getExactExceptionPrimitiveType(InsnNode assignInsn) {
		if (assignInsn == null) {
			return null;
		}
		if (assignInsn.getType() == InsnType.CMP_L || assignInsn.getType() == InsnType.CMP_G) {
			return ArgType.INT;
		}
		if (assignInsn.getType() == InsnType.INSTANCE_OF) {
			return ArgType.BOOLEAN;
		}
		if (!(assignInsn instanceof IndexInsnNode) || assignInsn.getType() != InsnType.IGET
				|| !(((IndexInsnNode) assignInsn).getIndex() instanceof FieldInfo)) {
			return null;
		}
		FieldInfo field = (FieldInfo) ((IndexInsnNode) assignInsn).getIndex();
		return field.getName().equals("label") && ArgType.INT.equals(field.getType())
				? ArgType.INT
				: null;
	}

	private static boolean isInExceptionHandler(MethodNode mth, InsnNode insn) {
		BlockNode block = BlockUtils.getBlockByInsn(mth, insn);
		if (block == null) {
			return false;
		}
		for (var handler : mth.getExceptionHandlers()) {
			BlockNode handlerBlock = handler.getHandlerBlock();
			if (handlerBlock != null
					&& (block == handlerBlock || block.isDominator(handlerBlock))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isSyntheticReferenceForwardMove(InsnNode insn, CodeVar sourceCodeVar) {
		if (insn.getType() != InsnType.MOVE || !insn.contains(AFlag.SYNTHETIC)
				|| insn.getResult() == null || insn.getResult().getSVar() == null) {
			return false;
		}
		CodeVar targetCodeVar = insn.getResult().getSVar().getCodeVar();
		ArgType targetType = targetCodeVar.getType();
		return targetCodeVar != sourceCodeVar
				&& (targetType == null || !targetType.isTypeKnown() || isConcreteReferenceType(targetType));
	}

	/**
	 * Recover Kotlin coroutine integer spill variables after loop normalization has merged their
	 * synthetic PHI moves. Require an exact {@code I$* : int} field load and prove every internal or
	 * external value edge to be {@code int}; this deliberately excludes boolean spills and mixed
	 * coroutine lifetimes such as an integer state later reusing the register for an object.
	 */
	static void repairLateCoroutineIntSpillFlows(List<SSAVar> vars) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			List<SSAVar> group = entry.getValue();
			if (isProvenCoroutineIntSpillGroup(codeVar, group)) {
				markCodeVarType(codeVar, group, ArgType.INT);
			}
		}
	}

	/**
	 * Loop normalization can preserve two unrelated DEX register lifetimes through one synthetic
	 * PHI relay. If the relay inputs and outputs prove the same exact primitive/reference pair,
	 * represent the shared Java local as Object and cast only its typed synthetic output boundaries.
	 */
	static void repairLateMixedPrimitiveReferencePhiRelays(MethodNode mth) {
		repairMixedPrimitiveReferencePhiRelays(mth, collectCodeVarGroups(mth));
	}

	static void repairMixedPrimitiveReferencePhiRelays(
			MethodNode mth, Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (group.size() != 3
					|| currentType != null && currentType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			Set<ArgType> inputTypes = new LinkedHashSet<>();
			Set<ArgType> outputTypes = new LinkedHashSet<>();
			Map<RegisterArg, ArgType> castUses = new IdentityHashMap<>();
			int phiCount = 0;
			boolean valid = true;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					phiCount++;
					for (InsnArg arg : assignInsn.getArguments()) {
						if (!arg.isRegister() || ((RegisterArg) arg).getSVar() == null
								|| ((RegisterArg) arg).getSVar().getCodeVar() != codeVar) {
							valid = false;
							break;
						}
					}
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
					ArgType inputType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
					if (!isExactPrimitiveOrReferenceType(inputType)) {
						valid = false;
					} else {
						inputTypes.add(inputType);
					}
				} else {
					valid = false;
				}
				if (!valid) {
					break;
				}
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					RegisterArg result = useInsn.getResult();
					if (useInsn.getType() != InsnType.MOVE || !useInsn.contains(AFlag.SYNTHETIC)
							|| result == null || result.getSVar() == null) {
						valid = false;
						break;
					}
					ArgType outputType = result.getSVar().getCodeVar().getType();
					if (!isExactPrimitiveOrReferenceType(outputType)) {
						valid = false;
						break;
					}
					outputTypes.add(outputType);
					castUses.put(use, outputType);
				}
				if (!valid) {
					break;
				}
			}
			if (!valid || phiCount != 1 || inputTypes.size() != 2
					|| !inputTypes.equals(outputTypes)
					|| inputTypes.stream().filter(ArgType::isPrimitive).count() != 1
					|| inputTypes.stream().filter(FinishTypeInference::isConcreteReferenceType).count() != 1) {
				continue;
			}
			codeVar.setType(ArgType.OBJECT);
			for (Map.Entry<RegisterArg, ArgType> castEntry : castUses.entrySet()) {
				castUseFromObject(mth, castEntry.getKey(), castEntry.getValue());
			}
		}
	}

	private static boolean isExactPrimitiveOrReferenceType(ArgType type) {
		return type != null && type.isTypeKnown()
				&& (type.isPrimitive() && !type.isVoid() || isConcreteReferenceType(type));
	}

	/**
	 * Coroutine loop normalization can connect otherwise disjoint register lifetimes through a
	 * chain of synthetic PHIs and MOVEs. Represent such a chain as a nullable {@link Object} only
	 * when it has a zero/null input and every observable output is reference-only. Primitive inputs
	 * are allowed solely as internal carrier edges: any generated primitive consumer rejects the
	 * repair. Concrete reference outputs get an explicit cast at their boundary.
	 */
	static void repairLateNullableCoroutineObjectCarriers(MethodNode mth) {
		if (!isCoroutineMethod(mth)) {
			return;
		}
		repairNullableCoroutineObjectCarriers(mth, collectCodeVarGroups(mth));
	}

	static void repairNullableCoroutineObjectCarriers(
			MethodNode mth, Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (group.size() < 3
					|| currentType != null && currentType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			int phiCount = 0;
			int zeroCount = 0;
			int externalMoveCount = 0;
			int referenceOutputCount = 0;
			boolean valid = true;
			Map<RegisterArg, ArgType> castUses = new IdentityHashMap<>();
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					phiCount++;
					for (InsnArg arg : assignInsn.getArguments()) {
						if (!isRegisterFromCodeVar(arg, codeVar)) {
							valid = false;
							break;
						}
					}
				} else if (assignInsn != null && assignInsn.getType() == InsnType.CONST
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()) {
					zeroCount++;
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
					SSAVar inputVar = ((RegisterArg) assignInsn.getArg(0)).getSVar();
					if (inputVar == null) {
						valid = false;
					} else if (inputVar.getCodeVar() != codeVar) {
						externalMoveCount++;
					}
				} else {
					valid = false;
				}
				if (!valid) {
					break;
				}
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					RegisterArg result = useInsn.getResult();
					if (useInsn.getType() != InsnType.MOVE
							|| result == null || result.getSVar() == null) {
						valid = false;
						break;
					}
					CodeVar targetCodeVar = result.getSVar().getCodeVar();
					if (targetCodeVar == codeVar) {
						continue;
					}
					ArgType targetType = targetCodeVar.getType();
					if (isConcreteReferenceType(targetType)) {
						referenceOutputCount++;
						if (!ArgType.OBJECT.equals(targetType)) {
							castUses.put(use, targetType);
						}
					} else if (isReferenceOnlyType(use.getInitType())) {
						referenceOutputCount++;
					} else {
						valid = false;
						break;
					}
				}
				if (!valid) {
					break;
				}
			}
			if (!valid || phiCount == 0 || zeroCount != 1
					|| externalMoveCount == 0 || referenceOutputCount == 0) {
				continue;
			}
			markCodeVarType(codeVar, group, ArgType.OBJECT);
			for (Map.Entry<RegisterArg, ArgType> castEntry : castUses.entrySet()) {
				castUseFromObject(mth, castEntry.getKey(), castEntry.getValue());
			}
		}
	}

	/**
	 * Region splitting can leave a nullable reference in a three-value relay: a zero constant, one
	 * synthetic MOVE from the concrete reference, and their PHI. Recover the concrete Java type when
	 * every emitted output is either the exact same type or a widening MOVE to {@link Object}.
	 */
	static void repairLateNullableReferencePhiRelays(List<SSAVar> vars) {
		repairNullableReferencePhiRelays(collectCodeVarGroups(vars));
	}

	static void repairLateZeroAndNullablePhiRelays(List<SSAVar> vars) {
		Map<CodeVar, List<SSAVar>> groups = collectCodeVarGroups(vars);
		repairZeroPrimitivePhiRelays(groups);
		repairNullableReferencePhiRelays(groups);
	}

	/**
	 * Region splitting can leave a primitive in a three-value relay: the DEX-wide zero constant,
	 * one synthetic MOVE from an exact primitive, and their PHI. Recover the primitive type only
	 * when every emitted boundary is a synthetic MOVE to that same exact type.
	 */
	static void repairLateZeroPrimitivePhiRelays(List<SSAVar> vars) {
		repairZeroPrimitivePhiRelays(collectCodeVarGroups(vars));
	}

	static void repairZeroPrimitivePhiRelays(Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (group.size() != 3
					|| currentType != null && currentType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			ArgType candidate = null;
			int zeroCount = 0;
			int phiCount = 0;
			boolean valid = true;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					phiCount++;
					for (InsnArg arg : assignInsn.getArguments()) {
						if (!isRegisterFromCodeVar(arg, codeVar)) {
							valid = false;
							break;
						}
					}
				} else if (assignInsn != null && assignInsn.getType() == InsnType.CONST
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()) {
					zeroCount++;
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.contains(AFlag.SYNTHETIC)
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
					ArgType inputType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
					if (!isExactPrimitiveType(inputType)
							|| candidate != null && !candidate.equals(inputType)) {
						valid = false;
					} else {
						candidate = inputType;
					}
				} else {
					valid = false;
				}
				if (!valid) {
					break;
				}
			}
			if (!valid || candidate == null || zeroCount != 1 || phiCount != 1) {
				continue;
			}
			int outputCount = 0;
			for (SSAVar var : group) {
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					RegisterArg result = useInsn.getResult();
					if (useInsn.getType() != InsnType.MOVE || !useInsn.contains(AFlag.SYNTHETIC)
							|| result == null || result.getSVar() == null
							|| !candidate.equals(result.getSVar().getCodeVar().getType())) {
						valid = false;
						break;
					}
					outputCount++;
				}
				if (!valid) {
					break;
				}
			}
			if (valid && outputCount != 0) {
				markCodeVarType(codeVar, group, candidate);
			}
		}
	}

	private static boolean isExactPrimitiveType(ArgType type) {
		return type != null && type.isTypeKnown() && type.isPrimitive() && !type.isVoid();
	}

	static void repairNullableReferencePhiRelays(Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (group.size() != 3
					|| currentType != null && currentType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			ArgType candidate = null;
			int nullCount = 0;
			int phiCount = 0;
			boolean valid = true;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					phiCount++;
					for (InsnArg arg : assignInsn.getArguments()) {
						if (!isRegisterFromCodeVar(arg, codeVar)) {
							valid = false;
							break;
						}
					}
				} else if (assignInsn != null && assignInsn.getType() == InsnType.CONST
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()) {
					nullCount++;
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.contains(AFlag.SYNTHETIC)
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
					ArgType inputType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
					if (!isConcreteReferenceType(inputType)
							|| candidate != null && !candidate.equals(inputType)) {
						valid = false;
					} else {
						candidate = inputType;
					}
				} else {
					valid = false;
				}
				if (!valid) {
					break;
				}
			}
			if (!valid || candidate == null || nullCount != 1 || phiCount != 1) {
				continue;
			}
			int exactOutputCount = 0;
			int objectOutputCount = 0;
			for (SSAVar var : group) {
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					RegisterArg result = useInsn.getResult();
					if (useInsn.getType() != InsnType.MOVE || result == null || result.getSVar() == null) {
						valid = false;
						break;
					}
					ArgType targetType = result.getSVar().getCodeVar().getType();
					if (ArgType.OBJECT.equals(targetType)) {
						objectOutputCount++;
					} else if (candidate.equals(targetType)
							&& useInsn.contains(AFlag.SYNTHETIC)) {
						exactOutputCount++;
					} else {
						valid = false;
						break;
					}
				}
				if (!valid) {
					break;
				}
			}
			if (valid && exactOutputCount != 0 && objectOutputCount != 0) {
				markCodeVarType(codeVar, group, candidate);
			}
		}
	}

	/**
	 * Kotlin stores a boolean across suspension in an {@code I$* : int} field as 0/1. After resume,
	 * structural PHI moves can merge that integer load with the original boolean path. Normalize the
	 * boolean edge to an explicit 0/1 ternary only when every value edge is either that exact spill or
	 * a boolean and every generated consumer is boolean control flow (or an integer spill move).
	 */
	static void repairLateCoroutineBooleanIntSpillFlows(MethodNode mth) {
		List<SSAVar> vars = mth.getSVars();
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			List<InsnNode> booleanMoves = collectCoroutineBooleanIntSpillMoves(mth, codeVar, entry.getValue());
			if (booleanMoves == null) {
				continue;
			}
			boolean replaced = true;
			for (InsnNode moveInsn : booleanMoves) {
				RegisterArg result = moveInsn.getResult();
				RegisterArg booleanArg = ((RegisterArg) moveInsn.getArg(0)).duplicate();
				TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(result, booleanArg, ArgType.INT);
				convertInsn.add(AFlag.SYNTHETIC);
				convertInsn.addAttr(BooleanNumericConversionAttr.INSTANCE);
				if (!BlockUtils.replaceInsn(mth, moveInsn, convertInsn)) {
					replaced = false;
					break;
				}
			}
			if (replaced) {
				markCodeVarType(codeVar, entry.getValue(), ArgType.INT);
			}
		}
		normalizeIntBooleanComparisons(vars);
	}

	/**
	 * Coroutine loop normalization can expose one path-dependent carrier at the resume join: on a
	 * direct entry it contains a float spill value, while on a resumed entry it contains a 0/1 int
	 * spill. Region construction previously represented the two edge copies as boolean ternaries,
	 * losing an arbitrary float value. Recover a float carrier only for the closed seven-value shape
	 * produced by that split, widening the proven 0/1 input and casting it back at the sole int exit.
	 */
	static boolean repairLateCoroutineFloatIntCarrierFlows(MethodNode mth) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(mth.getSVars()).entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType currentType = codeVar.getType();
			if (group.size() != 7 || currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			LateCoroutineFloatIntCarrier carrier = collectLateCoroutineFloatIntCarrier(mth.getSVars(), codeVar, group);
			if (carrier == null
					|| BlockUtils.getBlockByInsn(mth, carrier.floatOutput) == null
					|| BlockUtils.getBlockByInsn(mth, carrier.floatLoopBack) == null) {
				continue;
			}
			InsnNode floatOutputMove = makeSyntheticMove(
					carrier.floatOutput.getResult(), carrier.floatOutputSource);
			InsnNode floatLoopBackMove = makeSyntheticMove(
					carrier.floatLoopBack.getResult(), carrier.floatLoopBackSource);
			unbindTernaryConditionUses(mth, carrier.floatOutput);
			unbindTernaryConditionUses(mth, carrier.floatLoopBack);
			if (!BlockUtils.replaceInsn(mth, carrier.floatOutput, floatOutputMove)
					|| !BlockUtils.replaceInsn(mth, carrier.floatLoopBack, floatLoopBackMove)) {
				continue;
			}
			markOrphanFloatIdentityConditions(mth, group);
			markOrphanFloatIdentityConditions(
					mth, carrier.floatLoopBackSource.getSVar().getCodeVar().getSsaVars());
			markCodeVarType(codeVar, group, ArgType.FLOAT);
			for (RegisterArg intOutput : carrier.intOutputs) {
				castPrimitiveUse(mth, intOutput, ArgType.FLOAT, ArgType.INT);
			}
			return true;
		}
		return false;
	}

	static LateCoroutineFloatIntCarrier collectLateCoroutineFloatIntCarrier(
			List<SSAVar> allVars, CodeVar codeVar, List<SSAVar> group) {
		int phiCount = 0;
		int floatRootCount = 0;
		int intInputCount = 0;
		int intSpillCount = 0;
		TernaryInsn floatLoopBack = null;
		RegisterArg floatLoopBackSource = null;
		for (SSAVar var : group) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn) {
				phiCount++;
				for (InsnArg arg : assignInsn.getArguments()) {
					if (!isRegisterFromCodeVar(arg, codeVar)) {
						return null;
					}
				}
				continue;
			}
			if (isCoroutineIntSpillFieldGet(assignInsn)) {
				intSpillCount++;
				continue;
			}
			if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
				ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
				if (ArgType.FLOAT.equals(sourceType)) {
					floatRootCount++;
				} else if (ArgType.INT.equals(sourceType)) {
					intInputCount++;
				} else {
					return null;
				}
				continue;
			}
			if (assignInsn instanceof TernaryInsn) {
				RegisterArg conditionSource = getZeroOneTernaryConditionSource((TernaryInsn) assignInsn);
				if (conditionSource == null
						|| !ArgType.FLOAT.equals(getKnownValueType(conditionSource, new HashSet<>()))
						|| floatLoopBack != null) {
					return null;
				}
				floatLoopBack = (TernaryInsn) assignInsn;
				floatLoopBackSource = conditionSource;
				continue;
			}
			return null;
		}
		if (phiCount != 3 || floatRootCount != 1 || intInputCount != 1 || intSpillCount != 1
				|| floatLoopBack == null) {
			return null;
		}

		TernaryInsn floatOutput = null;
		RegisterArg floatOutputSource = null;
		for (SSAVar candidate : allVars) {
			InsnNode assignInsn = candidate.getAssignInsn();
			if (!(assignInsn instanceof TernaryInsn)) {
				continue;
			}
			RegisterArg source = getZeroOneTernaryConditionSource((TernaryInsn) assignInsn);
			RegisterArg result = assignInsn.getResult();
			if (source == null || source.getSVar() == null || source.getSVar().getCodeVar() != codeVar
					|| result == null || result.getSVar() == null
					|| !ArgType.FLOAT.equals(result.getSVar().getCodeVar().getType())) {
				continue;
			}
			if (floatOutput != null) {
				return null;
			}
			floatOutput = (TernaryInsn) assignInsn;
			floatOutputSource = source;
		}
		if (floatOutput == null || floatOutputSource == null) {
			return null;
		}
		List<RegisterArg> intOutputs = new ArrayList<>();
		for (SSAVar var : group) {
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (useInsn instanceof IfNode) {
					if (isZeroOrOneComparison(use)) {
						continue;
					}
					if (!useInsn.contains(AFlag.HIDDEN) || floatOutputSource.getSVar() != var
							|| !isOneComparison(use)) {
						return null;
					}
					continue;
				}
				RegisterArg result = useInsn.getResult();
				if (useInsn.getType() == InsnType.MOVE && result != null && result.getSVar() != null
						&& ArgType.INT.equals(result.getSVar().getCodeVar().getType())) {
					intOutputs.add(use);
					continue;
				}
				return null;
			}
		}
		if (intOutputs.size() != 1) {
			return null;
		}
		return new LateCoroutineFloatIntCarrier(
				floatOutput, floatOutputSource, floatLoopBack, floatLoopBackSource, intOutputs);
	}

	private static void unbindTernaryConditionUses(MethodNode mth, TernaryInsn ternary) {
		for (RegisterArg conditionArg : ternary.getCondition().getRegisterArgs()) {
			InsnRemover.unbindArgUsage(mth, conditionArg);
		}
	}

	private static void markOrphanFloatIdentityConditions(MethodNode mth, List<SSAVar> vars) {
		for (SSAVar var : vars) {
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof IfNode && useInsn.contains(AFlag.HIDDEN) && isOneComparison(use)
						&& BlockUtils.getBlockByInsn(mth, useInsn) == null) {
					useInsn.addAttr(BooleanNumericConversionAttr.INSTANCE);
				}
			}
		}
	}

	private static RegisterArg getZeroOneTernaryConditionSource(TernaryInsn ternary) {
		if (!isLiteralValue(ternary.getArg(0), 1) || !isLiteralValue(ternary.getArg(1), 0)
				|| !ternary.getCondition().isCompare()) {
			return null;
		}
		InsnArg first = ternary.getCondition().getCompare().getA();
		InsnArg second = ternary.getCondition().getCompare().getB();
		if (ternary.getCondition().getCompare().getOp() != IfOp.EQ) {
			return null;
		}
		if (first.isRegister() && isLiteralValue(second, 1)) {
			return (RegisterArg) first;
		}
		if (second.isRegister() && isLiteralValue(first, 1)) {
			return (RegisterArg) second;
		}
		return null;
	}

	private static boolean isOneComparison(RegisterArg use) {
		InsnNode parentInsn = use.getParentInsn();
		if (!(parentInsn instanceof IfNode)) {
			return false;
		}
		IfNode ifInsn = (IfNode) parentInsn;
		InsnArg first = ifInsn.getArg(0);
		InsnArg second = ifInsn.getArg(1);
		InsnArg other = first == use ? second : second == use ? first : null;
		return ifInsn.getOp() == IfOp.EQ && isLiteralValue(other, 1);
	}

	private static InsnNode makeSyntheticMove(RegisterArg result, RegisterArg source) {
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(result);
		move.addArg(source.duplicate());
		move.add(AFlag.SYNTHETIC);
		return move;
	}

	private static void castPrimitiveUse(MethodNode mth, RegisterArg use, ArgType from, ArgType to) {
		InsnNode useInsn = use.getParentInsn();
		IndexInsnNode castInsn = new IndexInsnNode(InsnType.CAST, to, 1);
		RegisterArg source = use.duplicate();
		source.forceSetInitType(from);
		castInsn.addArg(source);
		castInsn.add(AFlag.SYNTHETIC);
		castInsn.add(AFlag.EXPLICIT_CAST);
		InsnArg castArg = InsnArg.wrapInsnIntoArg(castInsn);
		castArg.setType(to);
		useInsn.replaceArg(use, castArg);
		InsnRemover.unbindArgUsage(mth, use);
	}

	static final class LateCoroutineFloatIntCarrier {
		final TernaryInsn floatOutput;
		final RegisterArg floatOutputSource;
		final TernaryInsn floatLoopBack;
		final RegisterArg floatLoopBackSource;
		final List<RegisterArg> intOutputs;

		LateCoroutineFloatIntCarrier(TernaryInsn floatOutput, RegisterArg floatOutputSource,
				TernaryInsn floatLoopBack, RegisterArg floatLoopBackSource, List<RegisterArg> intOutputs) {
			this.floatOutput = floatOutput;
			this.floatOutputSource = floatOutputSource;
			this.floatLoopBack = floatLoopBack;
			this.floatLoopBackSource = floatLoopBackSource;
			this.intOutputs = intOutputs;
		}
	}

	static List<InsnNode> collectCoroutineBooleanIntSpillMoves(MethodNode mth, CodeVar codeVar, List<SSAVar> group) {
		List<InsnNode> booleanMoves = new ArrayList<>();
		boolean hasIntSpill = false;
		boolean hasPhi = false;
		for (SSAVar var : group) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn) {
				hasPhi = true;
				for (InsnArg arg : assignInsn.getArguments()) {
					if (!arg.isRegister()
							|| ((RegisterArg) arg).getSVar() == null
							|| ((RegisterArg) arg).getSVar().getCodeVar() != codeVar) {
						return null;
					}
				}
			} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
				RegisterArg source = (RegisterArg) assignInsn.getArg(0);
				ArgType sourceType = getKnownValueType(source, new HashSet<>());
				if (ArgType.BOOLEAN.equals(sourceType)) {
					if (!assignInsn.contains(AFlag.SYNTHETIC) || var.getUseCount() != 1
							|| mth != null && BlockUtils.getBlockByInsn(mth, assignInsn) == null) {
						return null;
					}
					booleanMoves.add(assignInsn);
				} else if (ArgType.INT.equals(sourceType)
						&& source.getSVar() != null
						&& isCoroutineIntSpillFieldGet(source.getSVar().getAssignInsn())) {
					hasIntSpill = true;
				} else {
					return null;
				}
			} else {
				return null;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (useInsn instanceof IfNode && isZeroOrOneComparison(use)) {
					continue;
				}
				RegisterArg result = useInsn.getResult();
				if (useInsn.getType() == InsnType.MOVE && useInsn.contains(AFlag.SYNTHETIC)
						&& result != null && result.getSVar() != null
						&& ArgType.INT.equals(result.getSVar().getCodeVar().getType())) {
					continue;
				}
				return null;
			}
		}
		return hasPhi && hasIntSpill && !booleanMoves.isEmpty() ? booleanMoves : null;
	}

	/**
	 * Region loop normalization can merge a boolean loop flag with an integer loop-carried copy of
	 * the same DEX register. Keep the loop itself boolean and make the two Java representation
	 * boundaries explicit. The detector is intentionally strict: the group must contain a boolean
	 * producer and a PHI, every generated use must be boolean control flow or a synthetic MOVE, and
	 * both an integer input and output boundary must be present.
	 */
	static void repairLateBooleanIntLoopFlows(MethodNode mth) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(mth.getSVars()).entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			LateBooleanIntLoopConversions conversions = collectLateBooleanIntLoopConversions(
					mth, codeVar, group);
			if (conversions == null) {
				if (group.size() != 3) {
					continue;
				}
				InsnNode intInput = collectClosedBooleanIntPhiInput(mth, codeVar, group);
				if (intInput == null) {
					continue;
				}
				conversions = new LateBooleanIntLoopConversions(List.of(intInput), List.of());
			}
			boolean replaced = true;
			for (InsnNode moveInsn : conversions.intInputs) {
				RegisterArg result = moveInsn.getResult();
				RegisterArg intArg = ((RegisterArg) moveInsn.getArg(0)).duplicate();
				IfNode ifNode = new IfNode(IfOp.NE, -1, intArg, LiteralArg.make(0, ArgType.INT));
				TernaryInsn convertInsn = new TernaryInsn(
						IfCondition.fromIfNode(ifNode), result, LiteralArg.litTrue(), LiteralArg.litFalse());
				convertInsn.add(AFlag.SYNTHETIC);
				convertInsn.addAttr(BooleanNumericConversionAttr.INSTANCE);
				if (!BlockUtils.replaceInsn(mth, moveInsn, convertInsn)) {
					replaced = false;
					break;
				}
			}
			if (!replaced) {
				continue;
			}
			for (InsnNode moveInsn : conversions.intOutputs) {
				RegisterArg result = moveInsn.getResult();
				RegisterArg booleanArg = ((RegisterArg) moveInsn.getArg(0)).duplicate();
				TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(result, booleanArg, ArgType.INT);
				convertInsn.add(AFlag.SYNTHETIC);
				convertInsn.addAttr(BooleanNumericConversionAttr.INSTANCE);
				if (!BlockUtils.replaceInsn(mth, moveInsn, convertInsn)) {
					replaced = false;
					break;
				}
			}
			if (replaced) {
				markCodeVarType(codeVar, group, ArgType.BOOLEAN);
				normalizeBooleanGroupLiterals(group);
			}
		}
	}

	/**
	 * A non-coroutine loop can merge a boolean method result with a loop-carried integer value that
	 * is proven to contain only 0/1. Accept only one exact boolean producer, one integer MOVE input and
	 * one PHI, with no generated role except boolean control flow or a boolean synthetic MOVE.
	 */
	static InsnNode collectClosedBooleanIntPhiInput(
			MethodNode mth, CodeVar codeVar, List<SSAVar> group) {
		if (group.size() != 3) {
			return null;
		}
		SSAVar booleanProducer = null;
		SSAVar intInputVar = null;
		SSAVar phiResult = null;
		InsnNode intInputMove = null;
		for (SSAVar var : group) {
			if (var.getCodeVar() != codeVar) {
				return null;
			}
			ArgType varType = var.getTypeInfo().getType();
			if (varType.isTypeKnown() && !ArgType.BOOLEAN.equals(varType)) {
				return null;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn && assignInsn.getArgsCount() == 2
					&& assignInsn.contains(AFlag.DONT_GENERATE)) {
				if (phiResult != null) {
					return null;
				}
				phiResult = var;
			} else if (assignInsn instanceof InvokeNode
					&& ArgType.BOOLEAN.equals(((InvokeNode) assignInsn).getCallMth().getReturnType())) {
				if (booleanProducer != null) {
					return null;
				}
				booleanProducer = var;
			} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()
					&& isClosedZeroOneIntFlow((RegisterArg) assignInsn.getArg(0))) {
				if (intInputMove != null) {
					return null;
				}
				intInputVar = var;
				intInputMove = assignInsn;
			} else {
				return null;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (var == phiResult && useInsn instanceof IfNode && isZeroOrOneComparison(use)) {
					continue;
				}
				RegisterArg result = useInsn.getResult();
				if (var != phiResult || useInsn.getType() != InsnType.MOVE
						|| !useInsn.contains(AFlag.SYNTHETIC) || result == null || result.getSVar() == null
						|| !ArgType.BOOLEAN.equals(result.getSVar().getCodeVar().getType())) {
					return null;
				}
			}
		}
		if (booleanProducer == null || intInputVar == null || phiResult == null
				|| mth != null && BlockUtils.getBlockByInsn(mth, intInputMove) == null) {
			return null;
		}
		PhiInsn phi = (PhiInsn) phiResult.getAssignInsn();
		if (findPhiInput(phi, booleanProducer) == null || findPhiInput(phi, intInputVar) == null) {
			return null;
		}
		return intInputMove;
	}

	static boolean isClosedZeroOneIntFlow(RegisterArg source) {
		boolean[] foundLiteral = { false };
		return collectClosedZeroOneIntFlow(
				source, new HashSet<>(), new HashSet<>(), foundLiteral) && foundLiteral[0];
	}

	private static boolean collectClosedZeroOneIntFlow(
			RegisterArg source,
			Set<SSAVar> visiting,
			Set<SSAVar> resolved,
			boolean[] foundLiteral) {
		SSAVar var = source.getSVar();
		if (var == null || !ArgType.INT.equals(var.getCodeVar().getType())) {
			return false;
		}
		if (resolved.contains(var) || !visiting.add(var)) {
			return true;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return false;
		}
		boolean valid;
		if ((assignInsn.getType() == InsnType.CONST || assignInsn.getType() == InsnType.MOVE)
				&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isLiteral()) {
			long literal = ((LiteralArg) assignInsn.getArg(0)).getLiteral();
			valid = literal == 0 || literal == 1;
			foundLiteral[0] |= valid;
		} else if (assignInsn.getType() == InsnType.MOVE && assignInsn.getArgsCount() == 1
				&& assignInsn.getArg(0).isRegister()) {
			valid = collectClosedZeroOneIntFlow(
					(RegisterArg) assignInsn.getArg(0), visiting, resolved, foundLiteral);
		} else if (assignInsn instanceof PhiInsn && assignInsn.getArgsCount() != 0) {
			valid = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister() || !collectClosedZeroOneIntFlow(
						(RegisterArg) arg, visiting, resolved, foundLiteral)) {
					valid = false;
					break;
				}
			}
		} else {
			valid = false;
		}
		visiting.remove(var);
		if (valid) {
			resolved.add(var);
		}
		return valid;
	}

	static LateBooleanIntLoopConversions collectLateBooleanIntLoopConversions(
			MethodNode mth, CodeVar codeVar, List<SSAVar> group) {
		List<InsnNode> intInputs = new ArrayList<>();
		List<InsnNode> intOutputs = new ArrayList<>();
		boolean hasPhi = false;
		boolean hasBooleanProducer = false;
		for (SSAVar var : group) {
			ArgType varType = var.getTypeInfo().getType();
			if (varType.isTypeKnown() && !ArgType.BOOLEAN.equals(varType)) {
				return null;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn) {
				hasPhi = true;
				for (InsnArg arg : assignInsn.getArguments()) {
					if (!isRegisterFromCodeVar(arg, codeVar)) {
						return null;
					}
				}
			} else if (assignInsn != null && assignInsn.getType() == InsnType.INSTANCE_OF) {
				hasBooleanProducer = true;
			} else if (isZeroOrOneConst(assignInsn)) {
				hasBooleanProducer = true;
			} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.contains(AFlag.SYNTHETIC)
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
				RegisterArg source = (RegisterArg) assignInsn.getArg(0);
				if (source.getSVar() != null && source.getSVar().getCodeVar() == codeVar) {
					// Loop-normalization copy inside this group.
				} else {
					ArgType sourceType = getKnownValueType(source, new HashSet<>());
					if (ArgType.INT.equals(sourceType)) {
						intInputs.add(assignInsn);
					} else if (ArgType.BOOLEAN.equals(sourceType)) {
						hasBooleanProducer = true;
					} else {
						return null;
					}
				}
			} else {
				return null;
			}

			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (useInsn instanceof IfNode && isZeroOrOneComparison(use)) {
					continue;
				}
				RegisterArg result = useInsn.getResult();
				if (useInsn.getType() != InsnType.MOVE || !useInsn.contains(AFlag.SYNTHETIC)
						|| result == null || result.getSVar() == null) {
					return null;
				}
				CodeVar resultCodeVar = result.getSVar().getCodeVar();
				if (resultCodeVar == codeVar || ArgType.BOOLEAN.equals(resultCodeVar.getType())) {
					continue;
				}
				if (!ArgType.INT.equals(resultCodeVar.getType())) {
					return null;
				}
				intOutputs.add(useInsn);
			}
		}
		if (!hasPhi || !hasBooleanProducer || intInputs.isEmpty() || intOutputs.isEmpty()) {
			return null;
		}
		Set<InsnNode> conversionInsns = Collections.newSetFromMap(new IdentityHashMap<>());
		conversionInsns.addAll(intInputs);
		conversionInsns.addAll(intOutputs);
		if (conversionInsns.size() != intInputs.size() + intOutputs.size()
				|| mth != null && conversionInsns.stream().anyMatch(insn -> BlockUtils.getBlockByInsn(mth, insn) == null)) {
			return null;
		}
		return new LateBooleanIntLoopConversions(intInputs, intOutputs);
	}

	private static boolean isRegisterFromCodeVar(InsnArg arg, CodeVar codeVar) {
		return arg.isRegister() && ((RegisterArg) arg).getSVar() != null
				&& ((RegisterArg) arg).getSVar().getCodeVar() == codeVar;
	}

	private static boolean isZeroOrOneConst(InsnNode insn) {
		if (insn == null || insn.getType() != InsnType.CONST || insn.getArgsCount() != 1
				|| !insn.getArg(0).isLiteral()) {
			return false;
		}
		long literal = ((LiteralArg) insn.getArg(0)).getLiteral();
		return literal == 0 || literal == 1;
	}

	private static void normalizeBooleanGroupLiterals(List<SSAVar> group) {
		for (SSAVar var : group) {
			InsnNode assignInsn = var.getAssignInsn();
			if (isZeroOrOneConst(assignInsn)) {
				long literal = ((LiteralArg) assignInsn.getArg(0)).getLiteral();
				assignInsn.setArg(0, LiteralArg.make(literal, ArgType.BOOLEAN));
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof IfNode && isZeroOrOneComparison(use)) {
					for (InsnArg arg : useInsn.getArguments()) {
						if (arg.isLiteral()) {
							arg.setType(ArgType.BOOLEAN);
						}
					}
				}
			}
		}
	}

	static final class LateBooleanIntLoopConversions {
		final List<InsnNode> intInputs;
		final List<InsnNode> intOutputs;

		LateBooleanIntLoopConversions(List<InsnNode> intInputs, List<InsnNode> intOutputs) {
			this.intInputs = intInputs;
			this.intOutputs = intOutputs;
		}
	}

	private static boolean isProvenCoroutineIntSpillGroup(CodeVar codeVar, List<SSAVar> group) {
		boolean hasIntSpillField = false;
		boolean hasPhi = false;
		for (SSAVar var : group) {
			ArgType type = var.getTypeInfo().getType();
			if (type.isTypeKnown() && !ArgType.INT.equals(type)
					|| !type.isTypeKnown() && !contains(type.getPossibleTypes(), PrimitiveType.INT)) {
				return false;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null) {
				return false;
			}
			if (assignInsn instanceof PhiInsn) {
				hasPhi = true;
			} else if (assignInsn.getType() == InsnType.MOVE) {
				// checked below
			} else if (isCoroutineIntSpillFieldGet(assignInsn)) {
				hasIntSpillField = true;
				continue;
			} else {
				return false;
			}
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister()) {
					return false;
				}
				RegisterArg reg = (RegisterArg) arg;
				SSAVar inputVar = reg.getSVar();
				if (inputVar != null && inputVar.getCodeVar() == codeVar) {
					continue;
				}
				if (!ArgType.INT.equals(getKnownValueType(reg, new HashSet<>()))) {
					return false;
				}
			}
		}
		return hasIntSpillField && hasPhi;
	}

	private static boolean isCoroutineIntSpillFieldGet(InsnNode insn) {
		if (!(insn instanceof IndexInsnNode) || insn.getType() != InsnType.IGET
				|| !(((IndexInsnNode) insn).getIndex() instanceof FieldInfo)
				|| insn.getResult() == null || !ArgType.INT.equals(insn.getResult().getInitType())) {
			return false;
		}
		FieldInfo field = (FieldInfo) ((IndexInsnNode) insn).getIndex();
		return field.getName().startsWith("I$") && ArgType.INT.equals(field.getType());
	}

	private static void markCodeVarType(CodeVar codeVar, List<SSAVar> vars, ArgType type) {
		codeVar.setType(type);
		for (SSAVar var : vars) {
			var.setType(type);
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null && assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()
					&& (assignInsn.getType() == InsnType.CONST
							|| assignInsn.getType() == InsnType.MOVE && isReferenceOnlyResult(assignInsn))) {
				assignInsn.setArg(0, InsnArg.lit(0, type));
			}
			RegisterArg assign = var.getAssign();
			if (assign != null) {
				assign.forceSetInitType(type);
			}
			for (RegisterArg use : var.getUseList()) {
				use.forceSetInitType(type);
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof IfNode && isZeroComparison(use)) {
					for (int i = 0; i < useInsn.getArgsCount(); i++) {
						if (useInsn.getArg(i).isZeroConst()) {
							useInsn.setArg(i, InsnArg.lit(0, type));
						}
					}
				}
			}
		}
	}

	private static boolean isReferenceOnlyType(ArgType type) {
		if (type.isTypeKnown()) {
			return type.isObject() || type.isArray();
		}
		PrimitiveType[] possibleTypes = type.getPossibleTypes();
		if (possibleTypes.length == 0) {
			return false;
		}
		for (PrimitiveType possibleType : possibleTypes) {
			if (possibleType != PrimitiveType.OBJECT && possibleType != PrimitiveType.ARRAY) {
				return false;
			}
		}
		return true;
	}

	private static boolean isBooleanBitValueInsn(InsnNode insn, Set<SSAVar> visited) {
		if (insn == null || insn.getArgsCount() == 0) {
			return false;
		}
		InsnType type = insn.getType();
		if (type != InsnType.CONST
				&& type != InsnType.MOVE
				&& type != InsnType.PHI
				&& type != InsnType.TERNARY
				&& (!(insn instanceof ArithNode) || !((ArithNode) insn).getOp().isBitOp())) {
			return false;
		}
		for (InsnArg arg : insn.getArguments()) {
			if (!isBooleanBitValue(arg, new HashSet<>(visited))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isBooleanBitValue(InsnArg arg, Set<SSAVar> visited) {
		ArgType type = arg.getType();
		if (ArgType.BOOLEAN.equals(type)) {
			return true;
		}
		if (arg.isLiteral()) {
			long literal = ((LiteralArg) arg).getLiteral();
			return literal == 0 || literal == 1;
		}
		if (arg.isInsnWrap()) {
			return isBooleanBitValueInsn(arg.unwrap(), visited);
		}
		if (!arg.isRegister()) {
			return false;
		}
		SSAVar var = ((RegisterArg) arg).getSVar();
		if (var == null || !visited.add(var)) {
			return false;
		}
		ArgType codeVarType = var.getCodeVar().getType();
		if (ArgType.BOOLEAN.equals(codeVarType)) {
			return true;
		}
		return isBooleanBitValueInsn(var.getAssignInsn(), visited);
	}

	private static boolean isLateBooleanControlFlow(SSAVar var, Set<SSAVar> visited) {
		if (!visited.add(var)) {
			return true;
		}
		if (var.getUseList().isEmpty()) {
			return false;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn instanceof IfNode && isZeroOrOneComparison(useArg)) {
				continue;
			}
			if (!(useInsn instanceof ArithNode) || !((ArithNode) useInsn).getOp().isBitOp()) {
				return false;
			}
			RegisterArg result = useInsn.getResult();
			if (result == null || result.getSVar() == null
					|| !isLateBooleanControlFlow(result.getSVar(), visited)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isZeroOrOneComparison(RegisterArg useArg) {
		IfNode ifInsn = (IfNode) useArg.getParentInsn();
		IfOp op = ifInsn.getOp();
		if (op != IfOp.EQ && op != IfOp.NE) {
			return false;
		}
		InsnArg firstArg = ifInsn.getArg(0);
		InsnArg secondArg = ifInsn.getArg(1);
		InsnArg other = firstArg == useArg ? secondArg : secondArg == useArg ? firstArg : null;
		if (other == null || !other.isLiteral()) {
			return false;
		}
		long literal = ((LiteralArg) other).getLiteral();
		return literal == 0 || literal == 1;
	}

	private static boolean isZeroComparison(RegisterArg useArg) {
		IfNode ifInsn = (IfNode) useArg.getParentInsn();
		IfOp op = ifInsn.getOp();
		if (op != IfOp.EQ && op != IfOp.NE) {
			return false;
		}
		InsnArg firstArg = ifInsn.getArg(0);
		InsnArg secondArg = ifInsn.getArg(1);
		return firstArg == useArg && secondArg.isZeroLiteral()
				|| secondArg == useArg && firstArg.isZeroLiteral();
	}

	private static Map<CodeVar, List<SSAVar>> collectCodeVarGroups(MethodNode mth) {
		return collectCodeVarGroups(mth.getSVars());
	}

	private static Map<CodeVar, List<SSAVar>> collectCodeVarGroups(List<SSAVar> vars) {
		Map<CodeVar, List<SSAVar>> groups = new LinkedHashMap<>();
		for (SSAVar var : vars) {
			groups.computeIfAbsent(var.getCodeVar(), key -> new ArrayList<>()).add(var);
		}
		return groups;
	}

	static int splitMixedPrimitiveCodeVars(Map<CodeVar, List<SSAVar>> groups) {
		return splitMixedPrimitiveCodeVars(groups, FinishTypeInference::getCoroutineLabelType);
	}

	private static ArgType getNonCoroutinePrimitiveType(SSAVar var) {
		ArgType coroutineLabelType = getCoroutineLabelType(var);
		return coroutineLabelType != null ? coroutineLabelType : getExactBooleanTerminalType(var);
	}

	static ArgType getExactBooleanTerminalType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getResult() == null
				|| !ArgType.BOOLEAN.equals(assignInsn.getResult().getInitType())) {
			return null;
		}
		boolean generatedUse = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return null;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			generatedUse = true;
			if (useInsn instanceof IfNode && isZeroComparison(use)) {
				continue;
			}
			if (!ArgType.BOOLEAN.equals(use.getInitType())) {
				return null;
			}
		}
		return generatedUse ? ArgType.BOOLEAN : null;
	}

	/**
	 * Recover a coroutine branch flag materialized as a 0/1 ternary. Register reuse can merge the
	 * flag with numeric or reference loop state, but a terminal equality branch proves that this
	 * single SSA lifetime has Java boolean semantics.
	 */
	static ArgType getExactBooleanTernaryTerminalType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (!(assignInsn instanceof TernaryInsn)
				|| assignInsn.getArgsCount() != 2
				|| !isLiteralValue(assignInsn.getArg(0), 1)
				|| !isLiteralValue(assignInsn.getArg(1), 0)) {
			return null;
		}
		boolean generatedUse = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return null;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			generatedUse = true;
			if (!(useInsn instanceof IfNode) || !isZeroOrOneComparison(use)) {
				return null;
			}
		}
		return generatedUse ? ArgType.BOOLEAN : null;
	}

	static int splitTerminalCoroutineBooleanCodeVars(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeVarType = codeVar.getType();
			if (group.size() < 2
					|| codeVarType != null && codeVarType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			List<SSAVar> booleanVars = new ArrayList<>();
			for (SSAVar var : group) {
				if (ArgType.BOOLEAN.equals(getExactBooleanTernaryTerminalType(var))
						|| ArgType.BOOLEAN.equals(getExactBooleanTerminalType(var))) {
					booleanVars.add(var);
				}
			}
			if (booleanVars.isEmpty() || booleanVars.size() == group.size()) {
				continue;
			}
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.removeAll(booleanVars);
			CodeVar booleanCodeVar = new CodeVar();
			booleanCodeVar.setType(ArgType.BOOLEAN);
			for (SSAVar var : booleanVars) {
				var.setCodeVar(booleanCodeVar);
			}
			booleanCodeVar.setSsaVars(booleanVars);
			codeVar.setSsaVars(remaining);
			markCodeVarType(booleanCodeVar, booleanVars, ArgType.BOOLEAN);
			splitCount += booleanVars.size();
		}
		return splitCount;
	}

	/**
	 * After a terminal boolean lifetime is detached, the remaining coroutine register can contain
	 * only the suspended call result and the resumed {@code Object}. Recover {@code Object} only when
	 * generated uses prove both boundaries: comparison with {@code getCOROUTINE_SUSPENDED()} and a
	 * reference CHECK_CAST. Boolean inputs left on non-generated PHIs belong to the already detached
	 * terminal lifetime and are safe to ignore.
	 */
	static void repairLateCoroutineObjectResultFlows(List<SSAVar> vars) {
		Map<CodeVar, List<SSAVar>> groups = collectCodeVarGroups(vars);
		if (splitTerminalCoroutineReferenceCastCodeVars(groups) != 0) {
			groups = collectCodeVarGroups(vars);
		}
		if (splitTerminalCoroutineReferenceInvokeCodeVars(groups) != 0) {
			groups = collectCodeVarGroups(vars);
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType != null && currentType.isTypeKnown()) {
				continue;
			}
			List<SSAVar> group = entry.getValue();
			boolean hasPhi = false;
			boolean hasSuspendedComparison = false;
			boolean hasReferenceCast = false;
			int objectRoots = 0;
			boolean valid = true;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					hasPhi = true;
					for (InsnArg arg : assignInsn.getArguments()) {
						if (!arg.isRegister() || ((RegisterArg) arg).getSVar() == null) {
							valid = false;
							break;
						}
						CodeVar inputCodeVar = ((RegisterArg) arg).getSVar().getCodeVar();
						ArgType inputType = inputCodeVar.getType();
						if (inputCodeVar != codeVar && !ArgType.BOOLEAN.equals(inputType)
								&& !isConcreteReferenceType(inputType)) {
							valid = false;
							break;
						}
					}
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()
						&& ArgType.OBJECT.equals(getKnownValueType(
								(RegisterArg) assignInsn.getArg(0), new HashSet<>()))) {
					objectRoots++;
				} else if (assignInsn instanceof InvokeNode && assignInsn.getResult() != null
						&& ArgType.OBJECT.equals(assignInsn.getResult().getInitType())) {
					objectRoots++;
				} else if (isCoroutineResultFieldObjectRoot(assignInsn)) {
					objectRoots++;
				} else {
					valid = false;
				}
				if (!valid) {
					break;
				}
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					if (useInsn instanceof IfNode && isCoroutineSuspendedComparison(use)) {
						hasSuspendedComparison = true;
						continue;
					}
					if (useInsn instanceof IndexInsnNode && useInsn.getType() == InsnType.CHECK_CAST
							&& ((IndexInsnNode) useInsn).getIndex() instanceof ArgType
							&& ((ArgType) ((IndexInsnNode) useInsn).getIndex()).isObject()) {
						hasReferenceCast = true;
						continue;
					}
					if (ArgType.OBJECT.equals(use.getInitType())) {
						continue;
					}
					valid = false;
					break;
				}
				if (!valid) {
					break;
				}
			}
			if (valid && hasPhi && objectRoots >= 2 && hasSuspendedComparison && hasReferenceCast) {
				markCodeVarType(codeVar, group, ArgType.OBJECT);
			}
		}
	}

	private static boolean isCoroutineResultFieldObjectRoot(InsnNode assignInsn) {
		if (!(assignInsn instanceof IndexInsnNode) || assignInsn.getType() != InsnType.IGET
				|| assignInsn.getResult() == null
				|| !ArgType.OBJECT.equals(assignInsn.getResult().getInitType())
				|| !(((IndexInsnNode) assignInsn).getIndex() instanceof FieldInfo)) {
			return false;
		}
		FieldInfo field = (FieldInfo) ((IndexInsnNode) assignInsn).getIndex();
		return field.getName().equals("result") && ArgType.OBJECT.equals(field.getType());
	}

	/**
	 * A coroutine loop can feed a checked reference result back through the same non-generated PHIs
	 * as the raw suspend/resume {@code Object}. Keep the explicit CHECK_CAST result in its concrete
	 * Java lifetime before recovering {@code Object} for the remaining state-machine value.
	 */
	static int splitTerminalCoroutineReferenceCastCodeVars(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType codeVarType = codeVar.getType();
			List<SSAVar> group = entry.getValue();
			if (group.size() < 2 || codeVar.isThis()
					|| codeVarType != null && codeVarType.isTypeKnown()) {
				continue;
			}
			Map<ArgType, List<SSAVar>> castGroups = new LinkedHashMap<>();
			List<SSAVar> remaining = new ArrayList<>();
			for (SSAVar var : group) {
				ArgType castType = getTerminalReferenceCastType(var);
				if (castType == null) {
					remaining.add(var);
				} else {
					castGroups.computeIfAbsent(castType, key -> new ArrayList<>()).add(var);
				}
			}
			if (castGroups.isEmpty() || remaining.isEmpty()) {
				continue;
			}
			codeVar.setSsaVars(remaining);
			for (Map.Entry<ArgType, List<SSAVar>> castEntry : castGroups.entrySet()) {
				CodeVar castCodeVar = new CodeVar();
				castCodeVar.setName(codeVar.getName());
				castCodeVar.setType(castEntry.getKey());
				castCodeVar.mergeFlagsFrom(codeVar);
				for (SSAVar var : castEntry.getValue()) {
					var.setCodeVar(castCodeVar);
				}
				castCodeVar.setSsaVars(castEntry.getValue());
				markCodeVarType(castCodeVar, castEntry.getValue(), castEntry.getKey());
				splitCount += castEntry.getValue().size();
			}
		}
		return splitCount;
	}

	/**
	 * A resumed coroutine loop can reuse its raw {@code Object} result register for a concrete
	 * reference returned near the loop tail. Split only one unambiguous invoke result whose exact
	 * type is consumed directly and which feeds a non-generated PHI back into the mixed group.
	 */
	static int splitTerminalCoroutineReferenceInvokeCodeVars(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeVarType = codeVar.getType();
			if (group.size() < 2 || codeVar.isThis()
					|| codeVarType != null && codeVarType.isTypeKnown()) {
				continue;
			}
			SSAVar candidate = null;
			ArgType candidateType = null;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				ArgType type = getExactReferenceRootType(var);
				if (!(assignInsn instanceof InvokeNode) || type == null || ArgType.OBJECT.equals(type)
						|| !hasOnlyCompatibleTerminalReferenceUses(var, type)
						|| !hasNonGeneratedPhiUse(var)) {
					continue;
				}
				if (candidate != null) {
					candidate = null;
					break;
				}
				candidate = var;
				candidateType = type;
			}
			if (candidate == null) {
				continue;
			}
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.remove(candidate);
			CodeVar terminalCodeVar = new CodeVar();
			terminalCodeVar.setType(candidateType);
			candidate.setCodeVar(terminalCodeVar);
			terminalCodeVar.setSsaVars(List.of(candidate));
			candidate.getAssignInsn().add(AFlag.DECLARE_VAR);
			terminalCodeVar.setDeclared(true);
			codeVar.setSsaVars(remaining);
			markCodeVarType(terminalCodeVar, List.of(candidate), candidateType);
			splitCount++;
		}
		return splitCount;
	}

	private static boolean hasOnlyCompatibleTerminalReferenceUses(SSAVar var, ArgType expectedType) {
		boolean hasExactGeneratedUse = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (expectedType.equals(use.getInitType())) {
				hasExactGeneratedUse = true;
				continue;
			}
			if (useInsn instanceof IndexInsnNode && useInsn.getType() == InsnType.CHECK_CAST
					&& ((IndexInsnNode) useInsn).getIndex() instanceof ArgType
					&& isConcreteReferenceType((ArgType) ((IndexInsnNode) useInsn).getIndex())) {
				continue;
			}
			return false;
		}
		return hasExactGeneratedUse;
	}

	private static ArgType getTerminalReferenceCastType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (!(assignInsn instanceof IndexInsnNode) || assignInsn.getType() != InsnType.CHECK_CAST
				|| !(((IndexInsnNode) assignInsn).getIndex() instanceof ArgType)) {
			return null;
		}
		ArgType castType = (ArgType) ((IndexInsnNode) assignInsn).getIndex();
		return isConcreteReferenceType(castType) && hasOnlyExactGeneratedUses(var, castType)
				? castType
				: null;
	}

	private static boolean isCoroutineSuspendedComparison(RegisterArg use) {
		InsnNode parentInsn = use.getParentInsn();
		if (!(parentInsn instanceof IfNode)) {
			return false;
		}
		IfOp op = ((IfNode) parentInsn).getOp();
		if (op != IfOp.EQ && op != IfOp.NE) {
			return false;
		}
		InsnArg firstArg = parentInsn.getArg(0);
		InsnArg secondArg = parentInsn.getArg(1);
		InsnArg otherArg = firstArg == use ? secondArg : secondArg == use ? firstArg : null;
		if (otherArg == null || !otherArg.isRegister()) {
			return false;
		}
		SSAVar otherVar = ((RegisterArg) otherArg).getSVar();
		InsnNode otherAssign = otherVar == null ? null : otherVar.getAssignInsn();
		return otherAssign instanceof InvokeNode
				&& ((InvokeNode) otherAssign).getCallMth().getName().equals("getCOROUTINE_SUSPENDED")
				&& ArgType.OBJECT.equals(otherAssign.getResult().getInitType());
	}

	static void repairLateTerminalCoroutineBooleanFlows(MethodNode mth) {
		if (isCoroutineMethod(mth)) {
			splitCoroutineBooleanSpillCasts(collectCodeVarGroups(mth));
			Map<CodeVar, List<SSAVar>> groups = collectCodeVarGroups(mth);
			Set<CodeVar> oldCodeVars = Collections.newSetFromMap(new IdentityHashMap<>());
			oldCodeVars.addAll(groups.keySet());
			if (splitTerminalCoroutineBooleanCodeVars(groups) != 0) {
				DeclareVariablesAttr declareVariables = mth.getRegion().get(AType.DECLARE_VARIABLES);
				if (declareVariables == null) {
					declareVariables = new DeclareVariablesAttr();
					mth.getRegion().addAttr(declareVariables);
				}
				Set<CodeVar> declared = Collections.newSetFromMap(new IdentityHashMap<>());
				for (SSAVar var : mth.getSVars()) {
					CodeVar codeVar = var.getCodeVar();
					if (!oldCodeVars.contains(codeVar) && ArgType.BOOLEAN.equals(codeVar.getType())
							&& !codeVar.isDeclared() && declared.add(codeVar)) {
						declareVariables.addVar(codeVar);
						codeVar.setDeclared(true);
					}
				}
			}
		}
	}

	/**
	 * A resumed Kotlin boolean local is loaded from a {@code Z$*} continuation field. Register
	 * reuse can leave a synthetic reference CHECK_CAST around that primitive load and attach the SSA
	 * value to the preceding reference lifetime. Detach only the exact boolean spill lifetime whose
	 * generated path feeds boolean control flow; internal PHI edges of the old reference lifetime are
	 * ignored because they are not emitted.
	 */
	static int splitCoroutineBooleanSpillCasts(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar oldCodeVar = entry.getKey();
			List<SSAVar> oldVars = entry.getValue();
			ArgType oldType = oldCodeVar.getType();
			if (oldVars.size() < 2 || oldType == null || !oldType.isTypeKnown() || !oldType.isObject()) {
				continue;
			}
			for (SSAVar var : new ArrayList<>(oldVars)) {
				IndexInsnNode castInsn = getCoroutineBooleanSpillCast(var);
				if (castInsn == null || !hasOnlyCoroutineBooleanSpillUses(var, oldCodeVar)) {
					continue;
				}
				List<SSAVar> remaining = new ArrayList<>(oldCodeVar.getSsaVars());
				if (!remaining.remove(var) || remaining.isEmpty()) {
					continue;
				}
				CodeVar booleanCodeVar = new CodeVar();
				booleanCodeVar.setType(ArgType.BOOLEAN);
				booleanCodeVar.setSsaVars(List.of(var));
				var.setCodeVar(booleanCodeVar);
				oldCodeVar.setSsaVars(remaining);
				castInsn.updateIndex(ArgType.BOOLEAN);
				markCodeVarType(booleanCodeVar, List.of(var), ArgType.BOOLEAN);
				splitCount++;
			}
		}
		return splitCount;
	}

	private static IndexInsnNode getCoroutineBooleanSpillCast(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (!(assignInsn instanceof IndexInsnNode) || assignInsn.getType() != InsnType.CHECK_CAST
				|| assignInsn.getResult() == null
				|| !ArgType.BOOLEAN.equals(assignInsn.getResult().getInitType())
				|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isInsnWrap()) {
			return null;
		}
		InsnNode wrappedInsn = assignInsn.getArg(0).unwrap();
		if (!(wrappedInsn instanceof IndexInsnNode) || wrappedInsn.getType() != InsnType.IGET
				|| !(((IndexInsnNode) wrappedInsn).getIndex() instanceof FieldInfo)) {
			return null;
		}
		FieldInfo field = (FieldInfo) ((IndexInsnNode) wrappedInsn).getIndex();
		return field.getName().startsWith("Z$") && ArgType.BOOLEAN.equals(field.getType())
				? (IndexInsnNode) assignInsn
				: null;
	}

	private static boolean hasOnlyCoroutineBooleanSpillUses(SSAVar var, CodeVar oldCodeVar) {
		boolean hasBooleanFlow = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			RegisterArg result = useInsn.getResult();
			if (useInsn.contains(AFlag.DONT_GENERATE)
					&& result != null && result.getSVar() != null
					&& result.getSVar().getCodeVar() == oldCodeVar) {
				continue;
			}
			if (useInsn.getType() != InsnType.MOVE || !useInsn.contains(AFlag.SYNTHETIC)
					|| result == null || result.getSVar() == null
					|| !hasOnlyStructuralBooleanControlUses(result.getSVar(), new HashSet<>())) {
				return false;
			}
			hasBooleanFlow = true;
		}
		return hasBooleanFlow;
	}

	private static boolean hasOnlyStructuralBooleanControlUses(SSAVar var, Set<SSAVar> visited) {
		if (!visited.add(var)) {
			return true;
		}
		boolean hasGeneratedUse = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn instanceof IfNode && isZeroOrOneComparison(use)) {
				hasGeneratedUse = true;
				continue;
			}
			RegisterArg result = useInsn.getResult();
			if (!useInsn.contains(AFlag.DONT_GENERATE)
					|| result == null || result.getSVar() == null
					|| !hasOnlyStructuralBooleanControlUses(result.getSVar(), visited)) {
				return false;
			}
			hasGeneratedUse = true;
		}
		return hasGeneratedUse;
	}

	private static boolean isLiteralValue(InsnArg arg, long expected) {
		return arg instanceof LiteralArg && ((LiteralArg) arg).getLiteral() == expected;
	}

	/**
	 * Nested resource cleanup handlers can observe a register before the corresponding Java local is
	 * initialized. DEX then carries either an older primitive value or a different resource through
	 * the handler PHI, although the cleanup local must be {@code null} on that edge. Recover the
	 * resource lifetime only when a terminal PHI is guarded by a null check and invokes
	 * {@code close()}, every compatible input is a structural MOVE/PHI/null flow, and each conflicting
	 * input occurs inside an exception handler with an already existing null SSA sibling.
	 */
	static int repairExceptionCleanupSiblingNullFlows(
			List<SSAVar> allVars,
			Map<CodeVar, List<SSAVar>> groups,
			TypeCompare typeCompare,
			Function<InsnNode, Boolean> isHandlerInsn) {
		int repaired = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : new ArrayList<>(groups.entrySet())) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			boolean primitiveLifetime = currentType != null
					&& currentType.isTypeKnown() && currentType.isPrimitive();
			if (codeVar.isThis()
					|| currentType != null && currentType.isTypeKnown() && !primitiveLifetime) {
				continue;
			}
			List<SSAVar> group = entry.getValue();
			SSAVar terminalVar = null;
			ArgType cleanupType = null;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (!(assignInsn instanceof PhiInsn)) {
					continue;
				}
				ArgType candidateType = getCloseCleanupReceiverType(var);
				if (candidateType != null) {
					terminalVar = var;
					cleanupType = candidateType;
					break;
				}
			}
			if (terminalVar == null) {
				continue;
			}
			SSAVar nullVar = primitiveLifetime
					? null
					: findAnyProvenNull(group);
			if (nullVar == null) {
				nullVar = findCleanupNullSibling(group, cleanupType, typeCompare);
			}
			if (nullVar == null && primitiveLifetime) {
				nullVar = findCleanupNullSibling(allVars, cleanupType, typeCompare);
			}
			if (nullVar == null) {
				continue;
			}
			Set<SSAVar> cleanupFlow = Collections.newSetFromMap(new IdentityHashMap<>());
			Map<RegisterArg, SSAVar> replacements = new IdentityHashMap<>();
			if (!collectCleanupSiblingNullFlow(terminalVar, codeVar, cleanupType, typeCompare,
					isHandlerInsn, nullVar, cleanupFlow, replacements)
					|| replacements.isEmpty() || cleanupFlow.size() < 2) {
				continue;
			}
			for (Map.Entry<RegisterArg, SSAVar> replacement : replacements.entrySet()) {
				RegisterArg oldArg = replacement.getKey();
				PhiInsn phi = (PhiInsn) oldArg.getParentInsn();
				SSAVar oldVar = oldArg.getSVar();
				SSAVar replacementVar = replacement.getValue();
				RegisterArg replacementArg = replacementVar.getAssign().duplicate();
				replacementArg.forceSetInitType(cleanupType);
				if (!phi.replaceArg(oldArg, replacementArg)) {
					continue;
				}
				if (oldVar != null) {
					CodeVar oldInputCodeVar = oldVar.getCodeVar();
					ArgType oldInputType = oldInputCodeVar.getType();
					if (oldInputCodeVar != codeVar
							&& oldInputType != null && oldInputType.isTypeKnown() && oldInputType.isPrimitive()) {
						markCodeVarType(oldInputCodeVar, List.of(oldVar), oldInputType);
						InsnNode oldAssignInsn = oldVar.getAssignInsn();
						if (oldAssignInsn != null) {
							oldAssignInsn.add(AFlag.DONT_INLINE);
						}
					}
				}
				normalizeCleanupNullValue(replacementVar, cleanupType);
				replacementVar.use(replacementArg);
				replacementVar.addUsedInPhi(phi);
			}
			List<SSAVar> cleanupVars = new ArrayList<>(cleanupFlow);
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.removeAll(cleanupVars);
			if (remaining.isEmpty()) {
				markCodeVarType(codeVar, cleanupVars, cleanupType);
			} else {
				CodeVar cleanupCodeVar = new CodeVar();
				cleanupCodeVar.setType(cleanupType);
				for (SSAVar var : cleanupVars) {
					var.setCodeVar(cleanupCodeVar);
				}
				cleanupCodeVar.setSsaVars(cleanupVars);
				codeVar.setSsaVars(remaining);
				markCodeVarType(cleanupCodeVar, cleanupVars, cleanupType);
				if (primitiveLifetime) {
					markCodeVarType(codeVar, remaining, currentType);
				}
			}
			repaired += replacements.size();
		}
		return repaired;
	}

	private static void normalizeCleanupNullValue(SSAVar nullVar, ArgType cleanupType) {
		InsnNode assignInsn = nullVar.getAssignInsn();
		if (assignInsn == null || assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isZeroConst()) {
			return;
		}
		assignInsn.setArg(0, InsnArg.lit(0, cleanupType));
		RegisterArg assign = nullVar.getAssign();
		if (assign != null) {
			assign.forceSetInitType(cleanupType);
		}
	}

	private static ArgType getCloseCleanupReceiverType(SSAVar var) {
		ArgType closeType = null;
		boolean hasNullCheck = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn instanceof IfNode && isZeroComparison(use)) {
				hasNullCheck = true;
				continue;
			}
			if (useInsn instanceof InvokeNode
					&& ((InvokeNode) useInsn).getInstanceArg() == use
					&& ((InvokeNode) useInsn).getCallMth().getName().equals("close")
					&& ((InvokeNode) useInsn).getCallMth().getArgsCount() == 0) {
				ArgType candidate = use.getInitType();
				if (!isConcreteReferenceType(candidate)
						|| closeType != null && !closeType.equals(candidate)) {
					return null;
				}
				closeType = candidate;
			}
		}
		return hasNullCheck ? closeType : null;
	}

	private static SSAVar findAnyProvenNull(List<SSAVar> vars) {
		for (SSAVar var : vars) {
			RegisterArg assign = var.getAssign();
			if (assign != null && isProvenNullValue(assign, new HashSet<>())) {
				return var;
			}
		}
		return null;
	}

	private static SSAVar findCleanupNullSibling(
			List<SSAVar> vars, ArgType cleanupType, TypeCompare typeCompare) {
		for (SSAVar var : vars) {
			RegisterArg assign = var.getAssign();
			if (assign == null || !isProvenNullValue(assign, new HashSet<>())) {
				continue;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (!(useInsn instanceof PhiInsn)) {
					continue;
				}
				for (InsnArg arg : useInsn.getArguments()) {
					if (!arg.isRegister() || arg == use) {
						continue;
					}
					ArgType siblingType = getCleanupFlowValueType((RegisterArg) arg);
					if (isCleanupTypeCompatible(siblingType, cleanupType, typeCompare)) {
						return var;
					}
				}
			}
		}
		return null;
	}

	private static boolean collectCleanupSiblingNullFlow(
			SSAVar var,
			CodeVar codeVar,
			ArgType cleanupType,
			TypeCompare typeCompare,
			Function<InsnNode, Boolean> isHandlerInsn,
			SSAVar fallbackNullVar,
			Set<SSAVar> cleanupFlow,
			Map<RegisterArg, SSAVar> replacements) {
		if (!cleanupFlow.add(var)) {
			return true;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return false;
		}
		if (assignInsn.getType() == InsnType.CONST) {
			return assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst();
		}
		if (assignInsn.getType() == InsnType.MOVE) {
			if (assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()) {
				return false;
			}
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			ArgType sourceType = getCleanupFlowValueType(source);
			if (isConcreteReferenceType(sourceType)) {
				return isCleanupTypeCompatible(sourceType, cleanupType, typeCompare);
			}
			SSAVar sourceVar = source.getSVar();
			if (sourceVar != null && sourceVar.getCodeVar() == codeVar) {
				return collectCleanupSiblingNullFlow(sourceVar, codeVar, cleanupType, typeCompare,
						isHandlerInsn, fallbackNullVar, cleanupFlow, replacements);
			}
			ArgType initType = source.getInitType();
			return (initType.canBeObject() || initType.canBeArray()) && !initType.canBeAnyNumber();
		}
		if (!(assignInsn instanceof PhiInsn)) {
			return false;
		}
		PhiInsn phi = (PhiInsn) assignInsn;
		for (InsnArg arg : assignInsn.getArguments()) {
			if (!arg.isRegister()) {
				return false;
			}
			RegisterArg input = (RegisterArg) arg;
			SSAVar inputVar = input.getSVar();
			if (inputVar != null && isProvenNullValue(input, new HashSet<>())) {
				if (inputVar.getCodeVar() == codeVar) {
					cleanupFlow.add(inputVar);
				}
				continue;
			}
			boolean compatible;
			if (inputVar != null && inputVar.getCodeVar() == codeVar) {
				Set<SSAVar> branchFlow = Collections.newSetFromMap(new IdentityHashMap<>());
				branchFlow.addAll(cleanupFlow);
				Map<RegisterArg, SSAVar> branchReplacements = new IdentityHashMap<>();
				compatible = collectCleanupSiblingNullFlow(inputVar, codeVar, cleanupType, typeCompare,
						isHandlerInsn, fallbackNullVar, branchFlow, branchReplacements);
				if (compatible) {
					cleanupFlow.addAll(branchFlow);
					replacements.putAll(branchReplacements);
				}
			} else {
				ArgType inputType = getCleanupFlowValueType(input);
				compatible = isCleanupTypeCompatible(inputType, cleanupType, typeCompare);
			}
			if (!compatible) {
				if (!isHandlerInsn.apply(phi)) {
					return false;
				}
				SSAVar nullVar = findDirectPhiNullVar(phi);
				replacements.put(input, nullVar != null ? nullVar : fallbackNullVar);
			}
		}
		return true;
	}

	private static SSAVar findDirectPhiNullVar(PhiInsn phi) {
		for (InsnArg arg : phi.getArguments()) {
			if (arg.isRegister() && isProvenNullValue((RegisterArg) arg, new HashSet<>())) {
				return ((RegisterArg) arg).getSVar();
			}
		}
		return null;
	}

	private static ArgType getCleanupFlowValueType(RegisterArg arg) {
		SSAVar var = arg.getSVar();
		if (var != null) {
			ArgType closeType = getDirectCloseReceiverType(var);
			if (closeType != null) {
				return closeType;
			}
		}
		return getKnownValueType(arg, new HashSet<>());
	}

	private static ArgType getDirectCloseReceiverType(SSAVar var) {
		ArgType closeType = null;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn instanceof InvokeNode
					&& ((InvokeNode) useInsn).getInstanceArg() == use
					&& ((InvokeNode) useInsn).getCallMth().getName().equals("close")
					&& ((InvokeNode) useInsn).getCallMth().getArgsCount() == 0) {
				ArgType candidate = use.getInitType();
				if (!isConcreteReferenceType(candidate)
						|| closeType != null && !closeType.equals(candidate)) {
					return null;
				}
				closeType = candidate;
			}
		}
		return closeType;
	}

	private static boolean isCleanupTypeCompatible(
			ArgType sourceType, ArgType cleanupType, TypeCompare typeCompare) {
		if (!isConcreteReferenceType(sourceType)) {
			return false;
		}
		return sourceType.equals(cleanupType)
				|| typeCompare.compareTypes(sourceType, cleanupType).isNarrowOrEqual();
	}

	/**
	 * An exception edge can preserve the primitive state of a reused DEX register while two Java
	 * reference lifetimes cross the same edge. SSA then gives both reference PHIs the shared primitive
	 * bridge, making the whole code variable impossible to type. Reconnect each PHI to its dominating
	 * reference root only for this exact five-variable shape, leaving the isolated primitive relay for
	 * the regular redundant PHI/MOVE cleanup.
	 */
	static int repairMixedReferencePrimitivePhiLifetimes(
			Map<CodeVar, List<SSAVar>> groups,
			BiPredicate<SSAVar, RegisterArg> dominatesPhiInput) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeVarType = codeVar.getType();
			if (group.size() != 5 || codeVar.isThis()
					|| codeVarType != null && codeVarType.isTypeKnown()) {
				continue;
			}
			List<SSAVar> referenceRoots = new ArrayList<>(2);
			List<SSAVar> phiResults = new ArrayList<>(2);
			SSAVar bridge = null;
			boolean validShape = true;
			for (SSAVar var : group) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn instanceof PhiInsn) {
					phiResults.add(var);
					continue;
				}
				ArgType sourceType = getExactMoveSourceType(var);
				if (isConcreteReferenceType(sourceType)) {
					referenceRoots.add(var);
				} else if (sourceType != null && sourceType.isTypeKnown() && sourceType.isPrimitive()
						&& bridge == null) {
					bridge = var;
				} else {
					validShape = false;
					break;
				}
			}
			if (!validShape || referenceRoots.size() != 2 || phiResults.size() != 2 || bridge == null) {
				continue;
			}
			ArgType referenceTypeA = getExactMoveSourceType(referenceRoots.get(0));
			ArgType referenceTypeB = getExactMoveSourceType(referenceRoots.get(1));
			if (referenceTypeA.equals(referenceTypeB)) {
				continue;
			}

			Map<SSAVar, SSAVar> phiToRoot = new IdentityHashMap<>();
			Map<SSAVar, RegisterArg> phiToBridgeInput = new IdentityHashMap<>();
			for (SSAVar phiResult : phiResults) {
				PhiInsn phi = (PhiInsn) phiResult.getAssignInsn();
				if (phi.getArgsCount() != 2 || !phi.contains(AFlag.DONT_GENERATE)) {
					validShape = false;
					break;
				}
				SSAVar root = null;
				RegisterArg bridgeInput = null;
				for (InsnArg arg : phi.getArguments()) {
					if (!arg.isRegister()) {
						validShape = false;
						break;
					}
					RegisterArg reg = (RegisterArg) arg;
					if (reg.getSVar() == bridge) {
						bridgeInput = reg;
					} else if (referenceRoots.contains(reg.getSVar())) {
						root = reg.getSVar();
					} else {
						validShape = false;
						break;
					}
				}
				if (!validShape || root == null || bridgeInput == null || phiToRoot.containsValue(root)) {
					validShape = false;
					break;
				}
				phiToRoot.put(phiResult, root);
				phiToBridgeInput.put(phiResult, bridgeInput);
			}
			if (!validShape || phiToRoot.size() != 2) {
				continue;
			}

			InsnNode bridgeAssign = bridge.getAssignInsn();
			RegisterArg relayUse = (RegisterArg) bridgeAssign.getArg(0);
			SSAVar relay = relayUse.getSVar();
			if (relay == null || relay.getCodeVar() == codeVar) {
				continue;
			}
			ArgType primitiveType = relay.getCodeVar().getType();
			InsnNode relayAssign = relay.getAssignInsn();
			if (primitiveType == null || !primitiveType.isTypeKnown() || !primitiveType.isPrimitive()
					|| relayAssign == null || relayAssign.getType() != InsnType.MOVE
					|| relayAssign.getArgsCount() != 1 || !relayAssign.getArg(0).isRegister()) {
				continue;
			}
			SSAVar relayedPhi = ((RegisterArg) relayAssign.getArg(0)).getSVar();
			SSAVar relayedRoot = phiToRoot.get(relayedPhi);
			if (relayedRoot == null) {
				continue;
			}
			SSAVar otherPhi = phiResults.get(0) == relayedPhi ? phiResults.get(1) : phiResults.get(0);
			SSAVar otherRoot = phiToRoot.get(otherPhi);
			SSAVar copiedOtherRoot = findUniqueMoveCopy(groups, otherRoot);
			PhiInsn relayPrimitivePhi = findUniquePhiUse(relay, null);
			PhiInsn copiedPrimitivePhi = findUniquePrimitivePhiUse(copiedOtherRoot, primitiveType);
			if (copiedOtherRoot == null || relayPrimitivePhi == null || copiedPrimitivePhi == null) {
				continue;
			}
			RegisterArg relayPhiInput = findPhiInput(relayPrimitivePhi, relay);
			RegisterArg updatedPrimitiveInput = findOtherPhiInput(relayPrimitivePhi, relay);
			RegisterArg copiedPhiInput = findPhiInput(copiedPrimitivePhi, copiedOtherRoot);
			RegisterArg primitiveRootInput = findOtherPhiInput(copiedPrimitivePhi, copiedOtherRoot);
			if (relayPhiInput == null || updatedPrimitiveInput == null
					|| copiedPhiInput == null || primitiveRootInput == null) {
				continue;
			}
			SSAVar updatedPrimitive = updatedPrimitiveInput.getSVar();
			SSAVar primitiveRoot = primitiveRootInput.getSVar();
			if (!primitiveType.equals(getKnownValueType(updatedPrimitiveInput, new HashSet<>()))
					|| !isIntegralConstRoot(primitiveRoot, primitiveType)
					|| relayPrimitivePhi.getResult() == null
					|| relayPrimitivePhi.getResult().getSVar().getCodeVar() != relay.getCodeVar()
					|| copiedPrimitivePhi.getResult() == null
					|| copiedPrimitivePhi.getResult().getSVar().getCodeVar() != copiedOtherRoot.getCodeVar()
					|| primitiveRoot.getCodeVar() != copiedOtherRoot.getCodeVar()) {
				continue;
			}
			RegisterArg otherBridgeInput = phiToBridgeInput.get(otherPhi);
			if (!dominatesPhiInput.test(copiedOtherRoot, otherBridgeInput)
					|| !dominatesPhiInput.test(bridge, updatedPrimitiveInput)
					|| !dominatesPhiInput.test(updatedPrimitive, copiedPhiInput)) {
				continue;
			}
			CodeVar relayCodeVar = relay.getCodeVar();
			CodeVar copiedCodeVar = copiedOtherRoot.getCodeVar();

			replacePhiInput((PhiInsn) otherPhi.getAssignInsn(), otherBridgeInput, copiedOtherRoot,
					getExactMoveSourceType(otherRoot));
			replacePhiInput(relayPrimitivePhi, updatedPrimitiveInput, bridge,
					getExactMoveSourceType(relayedRoot));
			replacePhiInput(copiedPrimitivePhi, copiedPhiInput, updatedPrimitive, primitiveType);

			ArgType relayedType = getExactMoveSourceType(relayedRoot);
			ArgType otherType = getExactMoveSourceType(otherRoot);
			SSAVar relayPhiResult = relayPrimitivePhi.getResult().getSVar();
			List<SSAVar> relayedLifetime = List.of(relayedRoot, relayedPhi, relay, bridge, relayPhiResult);
			List<SSAVar> otherLifetime = List.of(otherRoot, otherPhi);
			codeVar.setSsaVars(new ArrayList<>(relayedLifetime));
			for (SSAVar var : relayedLifetime) {
				var.setCodeVar(codeVar);
			}
			markCodeVarType(codeVar, relayedLifetime, relayedType);
			CodeVar otherCodeVar = new CodeVar();
			otherCodeVar.setName(codeVar.getName());
			otherCodeVar.setSsaVars(new ArrayList<>(otherLifetime));
			for (SSAVar var : otherLifetime) {
				var.setCodeVar(otherCodeVar);
			}
			markCodeVarType(otherCodeVar, otherLifetime, otherType);

			List<SSAVar> remainingPrimitive = new ArrayList<>(relayCodeVar.getSsaVars());
			remainingPrimitive.remove(relay);
			remainingPrimitive.remove(relayPhiResult);
			relayCodeVar.setSsaVars(remainingPrimitive);
			markCodeVarType(relayCodeVar, remainingPrimitive, primitiveType);

			SSAVar copiedPrimitiveResult = copiedPrimitivePhi.getResult().getSVar();
			List<SSAVar> remainingReference = new ArrayList<>(copiedCodeVar.getSsaVars());
			remainingReference.remove(primitiveRoot);
			remainingReference.remove(copiedPrimitiveResult);
			copiedCodeVar.setSsaVars(remainingReference);
			markCodeVarType(copiedCodeVar, remainingReference, otherType);
			CodeVar primitiveCodeVar = new CodeVar();
			primitiveCodeVar.setName(copiedCodeVar.getName());
			List<SSAVar> detachedPrimitive = List.of(primitiveRoot, copiedPrimitiveResult);
			primitiveCodeVar.setSsaVars(new ArrayList<>(detachedPrimitive));
			for (SSAVar var : detachedPrimitive) {
				var.setCodeVar(primitiveCodeVar);
			}
			markCodeVarType(primitiveCodeVar, detachedPrimitive, primitiveType);
			return 1;
		}
		return 0;
	}

	private static ArgType getExactMoveSourceType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getType() != InsnType.MOVE
				|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()) {
			return null;
		}
		ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
		return sourceType != null && sourceType.isTypeKnown() ? sourceType : null;
	}

	private static SSAVar findUniqueMoveCopy(Map<CodeVar, List<SSAVar>> groups, SSAVar source) {
		SSAVar result = null;
		for (List<SSAVar> vars : groups.values()) {
			for (SSAVar var : vars) {
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn != null && assignInsn.getType() == InsnType.MOVE && assignInsn.getArgsCount() == 1
						&& assignInsn.getArg(0).isRegister()
						&& ((RegisterArg) assignInsn.getArg(0)).getSVar() == source) {
					if (result != null) {
						return null;
					}
					result = var;
				}
			}
		}
		return result;
	}

	private static PhiInsn findUniquePhiUse(SSAVar var, SSAVar excludedResult) {
		if (var == null) {
			return null;
		}
		PhiInsn result = null;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (!(useInsn instanceof PhiInsn) || useInsn.getResult() == null
					|| useInsn.getResult().getSVar() == excludedResult) {
				continue;
			}
			if (result != null) {
				return null;
			}
			result = (PhiInsn) useInsn;
		}
		return result;
	}

	private static PhiInsn findUniquePrimitivePhiUse(SSAVar var, ArgType primitiveType) {
		if (var == null) {
			return null;
		}
		PhiInsn result = null;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (!(useInsn instanceof PhiInsn)) {
				continue;
			}
			RegisterArg otherInput = findOtherPhiInput((PhiInsn) useInsn, var);
			if (otherInput == null || !isIntegralConstRoot(otherInput.getSVar(), primitiveType)) {
				continue;
			}
			if (result != null) {
				return null;
			}
			result = (PhiInsn) useInsn;
		}
		return result;
	}

	private static RegisterArg findPhiInput(PhiInsn phi, SSAVar var) {
		for (InsnArg arg : phi.getArguments()) {
			if (arg.isRegister() && ((RegisterArg) arg).getSVar() == var) {
				return (RegisterArg) arg;
			}
		}
		return null;
	}

	private static RegisterArg findOtherPhiInput(PhiInsn phi, SSAVar var) {
		if (phi.getArgsCount() != 2) {
			return null;
		}
		for (InsnArg arg : phi.getArguments()) {
			if (arg.isRegister() && ((RegisterArg) arg).getSVar() != var) {
				return (RegisterArg) arg;
			}
		}
		return null;
	}

	private static boolean isIntegralConstRoot(SSAVar var, ArgType primitiveType) {
		InsnNode assignInsn = var.getAssignInsn();
		return primitiveType.getPrimitiveType() == PrimitiveType.INT
				&& assignInsn != null && assignInsn.getType() == InsnType.CONST
				&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isLiteral();
	}

	private static void replacePhiInput(
			PhiInsn phi, RegisterArg oldInput, SSAVar replacementVar, ArgType replacementType) {
		RegisterArg replacement = replacementVar.getAssign().duplicate();
		replacement.forceSetInitType(replacementType);
		if (!phi.replaceArg(oldInput, replacement)) {
			throw new IllegalStateException("Failed to reconnect validated PHI input");
		}
	}

	/**
	 * Exception edges can connect two lifetimes of the same DEX register even when Java needs two
	 * different reference locals. Split the terminal lifetime only when its assignment and every
	 * generated use agree on one exact type, the remaining roots agree on a second exact type, and a
	 * null/PHI flow proves this is an exception-merge pattern. This avoids masking genuine conflicting
	 * reference uses by simply selecting one of their types.
	 */
	static int splitMixedReferenceLifetimes(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeVarType = codeVar.getType();
			if (group.size() < 3
					|| codeVarType != null && codeVarType.isTypeKnown()
					|| codeVar.isThis() || codeVar.isDeclared()
					|| !hasNullConstAndPhi(group)) {
				continue;
			}
			ArgType firstRootType = null;
			ArgType secondRootType = null;
			boolean validRoots = true;
			for (SSAVar var : group) {
				ArgType rootType = getExactReferenceRootType(var);
				if (rootType == null) {
					continue;
				}
				if (firstRootType == null) {
					firstRootType = rootType;
				} else if (!rootType.equals(firstRootType)) {
					if (secondRootType == null) {
						secondRootType = rootType;
					} else if (!rootType.equals(secondRootType)) {
						validRoots = false;
						break;
					}
				}
			}
			if (!validRoots || firstRootType == null || secondRootType == null) {
				continue;
			}
			boolean firstTerminal = hasOnlyExactRootUses(group, firstRootType);
			boolean secondTerminal = hasOnlyExactRootUses(group, secondRootType);
			if (!firstTerminal && !secondTerminal) {
				continue;
			}
			ArgType detachedType;
			ArgType remainingType;
			if (firstTerminal && secondTerminal) {
				if (firstRootType.isArray() == secondRootType.isArray()) {
					continue;
				}
				detachedType = firstRootType.isArray() ? firstRootType : secondRootType;
				remainingType = firstRootType.isArray() ? secondRootType : firstRootType;
			} else {
				detachedType = firstTerminal ? firstRootType : secondRootType;
				remainingType = firstTerminal ? secondRootType : firstRootType;
			}
			List<SSAVar> detachedVars = new ArrayList<>();
			for (SSAVar var : group) {
				if (detachedType.equals(getExactReferenceRootType(var))) {
					detachedVars.add(var);
				}
			}
			if (detachedVars.isEmpty()) {
				continue;
			}
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.removeAll(detachedVars);
			CodeVar detachedCodeVar = new CodeVar();
			detachedCodeVar.setName(codeVar.getName());
			detachedCodeVar.setType(detachedType);
			detachedCodeVar.mergeFlagsFrom(codeVar);
			for (SSAVar var : detachedVars) {
				var.setCodeVar(detachedCodeVar);
			}
			detachedCodeVar.setSsaVars(new ArrayList<>(detachedVars));
			codeVar.setSsaVars(remaining);
			markCodeVarType(codeVar, remaining, remainingType);
			splitCount += detachedVars.size();
		}
		return splitCount;
	}

	/**
	 * A protected block can reuse a parameter/local register for a different reference immediately
	 * before entering an exception merge. Split the original MOVE lifetime only when its source type,
	 * every emitted use and the remaining lifetime's independent MOVE/use evidence are each exact,
	 * while the only connection between them is a non-generated PHI.
	 */
	static int splitStructuralReferenceMoveRootLifetimes(Map<CodeVar, List<SSAVar>> groups) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> group = entry.getValue();
			ArgType codeVarType = codeVar.getType();
			if (group.size() < 2 || codeVar.isThis() || codeVar.isDeclared()
					|| codeVarType != null && codeVarType.isTypeKnown()) {
				continue;
			}
			SSAVar moveRoot = null;
			ArgType moveRootType = null;
			for (SSAVar var : group) {
				ArgType sourceType = getExactReferenceMoveSourceType(var);
				if (sourceType == null || !hasOnlyExactGeneratedUses(var, sourceType)
						|| !hasNonGeneratedPhiUse(var)) {
					continue;
				}
				if (moveRoot != null) {
					moveRoot = null;
					break;
				}
				moveRoot = var;
				moveRootType = sourceType;
			}
			if (moveRoot == null) {
				continue;
			}
			List<SSAVar> remaining = new ArrayList<>(group);
			remaining.remove(moveRoot);
			ArgType remainingType = selectStructuralReferenceLifetimeType(remaining);
			if (remainingType == null || remainingType.equals(moveRootType)) {
				continue;
			}
			CodeVar moveCodeVar = new CodeVar();
			moveCodeVar.setName(codeVar.getName());
			moveCodeVar.setType(moveRootType);
			moveCodeVar.mergeFlagsFrom(codeVar);
			moveCodeVar.setSsaVars(List.of(moveRoot));
			moveRoot.setCodeVar(moveCodeVar);
			codeVar.setSsaVars(remaining);
			markCodeVarType(moveCodeVar, List.of(moveRoot), moveRootType);
			markCodeVarType(codeVar, remaining, remainingType);
			splitCount++;
		}
		return splitCount;
	}

	private static ArgType getExactReferenceMoveSourceType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getType() != InsnType.MOVE
				|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()) {
			return null;
		}
		ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
		return isConcreteReferenceType(sourceType) ? sourceType : null;
	}

	private static boolean hasNonGeneratedPhiUse(SSAVar var) {
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn instanceof PhiInsn && useInsn.contains(AFlag.DONT_GENERATE)) {
				return true;
			}
		}
		return false;
	}

	private static ArgType selectStructuralReferenceLifetimeType(List<SSAVar> vars) {
		Set<ArgType> exactTypes = new LinkedHashSet<>();
		for (SSAVar var : vars) {
			ArgType moveSourceType = getExactReferenceMoveSourceType(var);
			if (moveSourceType != null) {
				exactTypes.add(moveSourceType);
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				ArgType useType = use.getInitType();
				if (isConcreteReferenceType(useType)) {
					exactTypes.add(useType);
					continue;
				}
				if (useInsn.getType() == InsnType.MOVE && useInsn.contains(AFlag.SYNTHETIC)) {
					continue;
				}
				return null;
			}
		}
		return exactTypes.size() == 1 ? exactTypes.iterator().next() : null;
	}

	/**
	 * A coroutine resume PHI can be path-sensitive: one successor consumes the value as the
	 * concrete state object while another observes a restored local in the same DEX register.
	 * Preserve the shared Java local as Object and cast only the proven state-field and
	 * Continuation boundaries. This avoids assigning either path's type to the other path.
	 */
	static void repairCoroutineContinuationPathCasts(
			MethodNode mth, Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType codeVarType = codeVar.getType();
			if (entry.getValue().size() < 2
					|| codeVarType != null && codeVarType.isTypeKnown()
					|| codeVar.isThis() || codeVar.isDeclared()) {
				continue;
			}
			for (SSAVar var : entry.getValue()) {
				if (!(var.getAssignInsn() instanceof PhiInsn)) {
					continue;
				}
				List<RegisterArg> castUses = new ArrayList<>();
				Set<ArgType> receiverTypes = new LinkedHashSet<>();
				boolean hasContinuationUse = false;
				boolean valid = true;
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					if (useInsn instanceof IndexInsnNode
							&& useInsn.getType() == InsnType.IPUT
							&& useInsn.getArgsCount() >= 2
							&& useInsn.getArg(1) == use
							&& ((IndexInsnNode) useInsn).getIndex() instanceof FieldInfo) {
						FieldInfo field = (FieldInfo) ((IndexInsnNode) useInsn).getIndex();
						receiverTypes.add(field.getDeclClass().getType());
						castUses.add(use);
						continue;
					}
					ArgType useType = use.getInitType();
					if (useType.isObject() && useType.getObject().equals("kotlin.coroutines.Continuation")) {
						hasContinuationUse = true;
						castUses.add(use);
						continue;
					}
					if (useInsn.getType() != InsnType.MOVE) {
						valid = false;
						break;
					}
				}
				if (!valid || !hasContinuationUse || receiverTypes.size() != 1) {
					continue;
				}
				codeVar.setType(ArgType.OBJECT);
				castObjectUses(mth, castUses);
				break;
			}
		}
	}

	/**
	 * A suspend state object can share its DEX register with a resource restored only for a catch
	 * cleanup. The handler PHI is path-sensitive, so neither concrete type is a valid Java local
	 * type. Accept this shape only when an exact state root and all field receivers prove one type,
	 * exactly one different instance receiver proves the cleanup type, and an optional external
	 * MOVE relay returns directly to the same code variable. Keep the shared local as Object and
	 * cast only these proven receiver boundaries.
	 */
	static void repairCoroutineStateCleanupPathCasts(
			MethodNode mth, Map<CodeVar, List<SSAVar>> groups) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType codeVarType = codeVar.getType();
			if (entry.getValue().size() < 4
					|| codeVarType != null && codeVarType.isTypeKnown()
					|| codeVar.isThis()) {
				continue;
			}
			Set<ArgType> stateRootTypes = new LinkedHashSet<>();
			Set<ArgType> fieldReceiverTypes = new LinkedHashSet<>();
			Set<ArgType> cleanupReceiverTypes = new LinkedHashSet<>();
			List<RegisterArg> castUses = new ArrayList<>();
			boolean hasPhi = false;
			boolean valid = true;
			for (SSAVar var : entry.getValue()) {
				InsnNode assignInsn = var.getAssignInsn();
				hasPhi |= assignInsn instanceof PhiInsn;
				if (assignInsn instanceof ConstructorInsn && ((ConstructorInsn) assignInsn).isNewInstance()) {
					stateRootTypes.add(((ConstructorInsn) assignInsn).getClassType().getType());
				} else if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
						&& assignInsn.contains(AFlag.SYNTHETIC) && assignInsn.getArgsCount() == 1
						&& assignInsn.getArg(0).isRegister()) {
					ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
					if (isConcreteReferenceType(sourceType)) {
						stateRootTypes.add(sourceType);
					}
				}
				for (RegisterArg use : var.getUseList()) {
					InsnNode useInsn = use.getParentInsn();
					if (useInsn == null) {
						valid = false;
						break;
					}
					if (useInsn.contains(AFlag.DONT_GENERATE)) {
						continue;
					}
					ArgType fieldReceiverType = getFieldReceiverType(useInsn, use);
					if (fieldReceiverType != null) {
						fieldReceiverTypes.add(fieldReceiverType);
						castUses.add(use);
						continue;
					}
					if (useInsn instanceof InvokeNode && ((InvokeNode) useInsn).getInstanceArg() == use) {
						ArgType receiverType = ((InvokeNode) useInsn).getCallMth().getDeclClass().getType();
						if (!isConcreteReferenceType(receiverType)) {
							valid = false;
							break;
						}
						cleanupReceiverTypes.add(receiverType);
						castUses.add(use);
						continue;
					}
					if (useInsn.getType() == InsnType.MOVE
							|| useInsn instanceof InvokeNode && ArgType.OBJECT.equals(use.getInitType())) {
						continue;
					}
					valid = false;
					break;
				}
				if (!valid) {
					break;
				}
			}
			if (!valid || !hasPhi
					|| stateRootTypes.size() != 1
					|| !stateRootTypes.equals(fieldReceiverTypes)
					|| cleanupReceiverTypes.size() != 1
					|| stateRootTypes.equals(cleanupReceiverTypes)) {
				continue;
			}
			List<CodeVar> relays = findRoundTripMoveRelays(codeVar, groups);
			codeVar.setType(ArgType.OBJECT);
			for (CodeVar relay : relays) {
				relay.setType(ArgType.OBJECT);
			}
			castObjectUses(mth, castUses);
		}
	}

	static void repairLateCoroutineStateCleanupPathCasts(MethodNode mth) {
		if (isCoroutineMethod(mth)) {
			repairCoroutineStateCleanupPathCasts(mth, collectCodeVarGroups(mth));
		}
	}

	private static ArgType getFieldReceiverType(InsnNode insn, RegisterArg use) {
		if (!(insn instanceof IndexInsnNode)
				|| insn.getType() != InsnType.IGET && insn.getType() != InsnType.IPUT
				|| !(((IndexInsnNode) insn).getIndex() instanceof FieldInfo)) {
			return null;
		}
		int receiverIndex = insn.getArgsCount() - 1;
		if (receiverIndex < 0 || insn.getArg(receiverIndex) != use) {
			return null;
		}
		return ((FieldInfo) ((IndexInsnNode) insn).getIndex()).getDeclClass().getType();
	}

	private static List<CodeVar> findRoundTripMoveRelays(
			CodeVar mainCodeVar, Map<CodeVar, List<SSAVar>> groups) {
		List<CodeVar> relays = new ArrayList<>();
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar relayCodeVar = entry.getKey();
			ArgType relayType = relayCodeVar.getType();
			if (relayCodeVar == mainCodeVar
					|| relayType != null && relayType.isTypeKnown()
					|| entry.getValue().size() != 1) {
				continue;
			}
			SSAVar relayVar = entry.getValue().get(0);
			InsnNode assignInsn = relayVar.getAssignInsn();
			if (!isMoveBetweenCodeVars(assignInsn, mainCodeVar, false)) {
				continue;
			}
			boolean hasReturnMove = false;
			boolean valid = true;
			for (RegisterArg use : relayVar.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null || useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (!isMoveBetweenCodeVars(useInsn, mainCodeVar, true)) {
					valid = false;
					break;
				}
				hasReturnMove = true;
			}
			if (valid && hasReturnMove) {
				relays.add(relayCodeVar);
			}
		}
		return relays;
	}

	private static boolean isMoveBetweenCodeVars(InsnNode move, CodeVar expectedCodeVar, boolean checkResult) {
		if (move == null || move.getType() != InsnType.MOVE || move.getArgsCount() != 1) {
			return false;
		}
		InsnArg arg = checkResult ? move.getResult() : move.getArg(0);
		return arg != null && arg.isRegister()
				&& ((RegisterArg) arg).getSVar() != null
				&& ((RegisterArg) arg).getSVar().getCodeVar() == expectedCodeVar;
	}

	private static void castObjectUses(MethodNode mth, List<RegisterArg> castUses) {
		for (RegisterArg use : castUses) {
			castUseFromObject(mth, use, use.getInitType());
		}
	}

	private static void castUseFromObject(MethodNode mth, RegisterArg use, ArgType useType) {
		InsnNode useInsn = use.getParentInsn();
		IndexInsnNode castInsn = new IndexInsnNode(InsnType.CAST, useType, 1);
		RegisterArg castSource = use.duplicate();
		castSource.forceSetInitType(ArgType.OBJECT);
		castInsn.addArg(castSource);
		castInsn.add(AFlag.SYNTHETIC);
		castInsn.add(AFlag.EXPLICIT_CAST);
		InsnArg castArg = InsnArg.wrapInsnIntoArg(castInsn);
		castArg.setType(useType);
		useInsn.replaceArg(use, castArg);
		InsnRemover.unbindArgUsage(mth, use);
	}

	private static boolean hasOnlyExactRootUses(List<SSAVar> vars, ArgType expectedType) {
		boolean rootFound = false;
		for (SSAVar var : vars) {
			if (expectedType.equals(getExactReferenceRootType(var))) {
				rootFound = true;
				if (!hasOnlyExactGeneratedUses(var, expectedType)) {
					return false;
				}
			}
		}
		return rootFound;
	}

	private static boolean hasNullConstAndPhi(List<SSAVar> vars) {
		boolean hasNull = false;
		boolean hasPhi = false;
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null) {
				continue;
			}
			hasPhi |= assignInsn.getType() == InsnType.PHI;
			if (assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst()) {
				hasNull |= assignInsn.getType() == InsnType.CONST
						|| assignInsn.getType() == InsnType.MOVE && isReferenceOnlyResult(assignInsn);
			}
		}
		return hasNull && hasPhi;
	}

	private static boolean isReferenceOnlyResult(InsnNode insn) {
		RegisterArg result = insn.getResult();
		if (result == null) {
			return false;
		}
		ArgType type = result.getInitType();
		return (type.canBeObject() || type.canBeArray()) && !type.canBeAnyNumber();
	}

	private static ArgType getExactReferenceRootType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null
				|| assignInsn.getType() == InsnType.MOVE
				|| assignInsn.getType() == InsnType.PHI
				|| assignInsn.getType() == InsnType.CONST
				|| assignInsn.getResult() == null) {
			return null;
		}
		ArgType type = assignInsn.getResult().getInitType();
		return type.isTypeKnown() && (type.isObject() || type.isArray()) && !type.containsGeneric()
				? type
				: null;
	}

	private static boolean hasOnlyExactGeneratedUses(SSAVar var, ArgType expectedType) {
		boolean hasGeneratedUse = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			hasGeneratedUse = true;
			if (!expectedType.equals(use.getInitType())) {
				return false;
			}
		}
		return hasGeneratedUse;
	}

	/**
	 * A coroutine state machine can reuse the register holding {@code Continuation.label} for an
	 * object after the state dispatch. A synthetic PHI can then join both lifetimes into one
	 * code variable. Split only this proven primitive producer from a group with reference type
	 * evidence; ordinary primitive PHIs and declared variables remain untouched.
	 */
	static int splitMixedPrimitiveCodeVars(Map<CodeVar, List<SSAVar>> groups,
			Function<SSAVar, ArgType> primitiveTypeProvider) {
		int splitCount = 0;
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType codeVarType = codeVar.getType();
			if (entry.getValue().size() < 2
					|| codeVarType != null && codeVarType.isTypeKnown()
					|| codeVar.isThis()
					|| codeVar.isDeclared()) {
				continue;
			}
			Map<ArgType, List<SSAVar>> primitiveGroups = new LinkedHashMap<>();
			List<SSAVar> remaining = new ArrayList<>();
			for (SSAVar var : entry.getValue()) {
				ArgType primitiveType = primitiveTypeProvider.apply(var);
				if (primitiveType == null) {
					remaining.add(var);
				} else {
					primitiveGroups.computeIfAbsent(primitiveType, key -> new ArrayList<>()).add(var);
				}
			}
			if (primitiveGroups.isEmpty() || remaining.isEmpty() || !hasReferenceTypeEvidence(remaining)) {
				continue;
			}
			codeVar.setSsaVars(remaining);
			for (Map.Entry<ArgType, List<SSAVar>> primitiveEntry : primitiveGroups.entrySet()) {
				CodeVar primitiveCodeVar = new CodeVar();
				primitiveCodeVar.setName(codeVar.getName());
				primitiveCodeVar.setType(primitiveEntry.getKey());
				primitiveCodeVar.mergeFlagsFrom(codeVar);
				for (SSAVar var : primitiveEntry.getValue()) {
					var.setCodeVar(primitiveCodeVar);
				}
				primitiveCodeVar.setSsaVars(primitiveEntry.getValue());
				if (ArgType.BOOLEAN.equals(primitiveEntry.getKey())) {
					markCodeVarType(primitiveCodeVar, primitiveEntry.getValue(), ArgType.BOOLEAN);
				}
				splitCount += primitiveEntry.getValue().size();
			}
		}
		return splitCount;
	}

	private static boolean hasReferenceTypeEvidence(List<SSAVar> vars) {
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
					&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
				ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
				if (sourceType != null && sourceType.isTypeKnown()
						&& (sourceType.isObject() || sourceType.isArray())) {
					return true;
				}
			}
			ArgType type = var.getTypeInfo().getType();
			if (type.isTypeKnown() && (type.isObject() || type.isArray())) {
				return true;
			}
			ArgType immutableType = var.getImmutableType();
			if (immutableType != null && immutableType.isTypeKnown()
					&& (immutableType.isObject() || immutableType.isArray())) {
				return true;
			}
			for (ITypeBound bound : var.getTypeInfo().getBounds()) {
				ArgType boundType = bound.getType();
				if (boundType.isTypeKnown() && (boundType.isObject() || boundType.isArray())) {
					return true;
				}
			}
		}
		return false;
	}

	private static ArgType getCoroutineLabelType(SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (!(assignInsn instanceof IndexInsnNode)
				|| assignInsn.getType() != InsnType.IGET
				|| !(((IndexInsnNode) assignInsn).getIndex() instanceof FieldInfo)
				|| !((FieldInfo) ((IndexInsnNode) assignInsn).getIndex()).getName().equals("label")
				|| assignInsn.getResult() == null) {
			return null;
		}
		ArgType assignType = assignInsn.getResult().getInitType();
		if (!assignType.isTypeKnown() || !assignType.isPrimitive()) {
			return null;
		}
		return assignType;
	}

	/**
	 * Recover a late code-variable type for a PHI which merges one concrete reference value with
	 * a value proven to be null. Compose default-argument code can carry the same null through
	 * unrelated typed calls, leaving the PHI use itself as just {@code OBJECT|ARRAY}.
	 */
	static ArgType selectNullablePhiUseType(List<SSAVar> vars) {
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn)) {
				continue;
			}
			Set<ArgType> useTypes = new LinkedHashSet<>();
			if (!collectTerminalReferenceUseTypes(var, new HashSet<>(), useTypes) || useTypes.size() != 1) {
				continue;
			}
			ArgType useType = useTypes.iterator().next();
			boolean hasNull = false;
			boolean hasConcrete = false;
			boolean valid = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister()) {
					valid = false;
					break;
				}
				RegisterArg reg = (RegisterArg) arg;
				if (isProvenNullValue(reg, new HashSet<>())) {
					hasNull = true;
					continue;
				}
				ArgType inputType = getKnownValueType(reg, new HashSet<>());
				if (!useType.equals(inputType)) {
					valid = false;
					break;
				}
				hasConcrete = true;
			}
			if (valid && hasNull && hasConcrete) {
				return useType;
			}
		}
		return null;
	}

	/**
	 * A DEX register can retain an unrelated reference type after every incoming PHI edge has
	 * collapsed to null. If the PHI feeds several incompatible reference consumers, no useful Java
	 * local type exists. Replace terminal uses, including boundaries reached through synthetic moves,
	 * with a typed null literal; this preserves the exact value and avoids both an invalid shared
	 * declaration and speculative casts.
	 */
	static boolean replaceNullOnlyPhiUses(MethodNode mth, List<SSAVar> vars) {
		if (replaceClosedNullFlowUses(vars)) {
			return true;
		}
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn) || assignInsn.getArgsCount() == 0) {
				continue;
			}
			boolean allNull = true;
			for (InsnArg arg : assignInsn.getArguments()) {
				if (!arg.isRegister() || !isProvenNullValue((RegisterArg) arg, new HashSet<>())) {
					allNull = false;
					break;
				}
			}
			if (!allNull) {
				continue;
			}
			Map<RegisterArg, ArgType> terminalUses = new IdentityHashMap<>();
			List<RegisterArg> passThroughUses = new ArrayList<>();
			boolean collected = hasGeneratedMoveUse(var)
					? collectTypedNullBoundaries(var, new HashSet<>(), terminalUses, passThroughUses)
					: collectDirectTypedNullUses(var, terminalUses);
			if (!collected
					|| terminalUses.isEmpty() || new HashSet<>(terminalUses.values()).size() < 2) {
				continue;
			}
			for (SSAVar groupVar : vars) {
				InsnNode groupAssign = groupVar.getAssignInsn();
				RegisterArg groupResult = groupVar.getAssign();
				if (groupAssign == null
						|| groupAssign.getType() != InsnType.MOVE
								&& groupAssign.getType() != InsnType.PHI
								&& groupAssign.getType() != InsnType.CONST
						|| groupResult == null || !isProvenNullValue(groupResult, new HashSet<>())
						|| groupVar != var && hasGeneratedUse(groupVar)) {
					return false;
				}
			}
			for (Map.Entry<RegisterArg, ArgType> entry : terminalUses.entrySet()) {
				RegisterArg use = entry.getKey();
				InsnNode useInsn = use.getParentInsn();
				if (!useInsn.replaceArg(use, LiteralArg.make(0, entry.getValue()))) {
					return false;
				}
				InsnRemover.unbindArgUsage(mth, use);
			}
			for (RegisterArg use : passThroughUses) {
				use.getParentInsn().add(AFlag.DONT_GENERATE);
				InsnRemover.unbindArgUsage(mth, use);
			}
			for (SSAVar groupVar : vars) {
				groupVar.getAssignInsn().add(AFlag.DONT_GENERATE);
			}
			var.getCodeVar().setType(ArgType.OBJECT);
			return true;
		}
		return false;
	}

	/**
	 * Normalize a null value graph which spans several code variables and contains MOVE/PHI cycles.
	 * A local recursive proof rejects cycles, although a loop PHI such as {@code phi(null, self)} is
	 * still unconditionally null. Collect the complete unknown-type pass-through component instead,
	 * and require every member to have a path to an actual zero constant. Generated exits must be
	 * concrete reference consumers; each can then receive its own correctly typed null literal.
	 */
	private static boolean replaceClosedNullFlowUses(List<SSAVar> candidates) {
		for (SSAVar root : candidates) {
			InsnNode rootAssign = root.getAssignInsn();
			if (!(rootAssign instanceof PhiInsn) || rootAssign.getArgsCount() == 0) {
				continue;
			}
			Set<SSAVar> flowVars = Collections.newSetFromMap(new IdentityHashMap<>());
			boolean collected = collectClosedNullFlow(root, flowVars);
			boolean cyclic = collected && hasNullFlowCycle(flowVars);
			boolean grounded = cyclic && isGroundedNullFlow(flowVars);
			if (!collected || !cyclic || !grounded) {
				continue;
			}
			Map<RegisterArg, ArgType> terminalUses = new IdentityHashMap<>();
			boolean terminalsCollected = collectClosedNullFlowTerminals(flowVars, terminalUses);
			long concreteTypeCount = terminalUses.values().stream()
					.filter(type -> !type.equals(ArgType.OBJECT))
					.distinct()
					.count();
			if (!terminalsCollected
					|| terminalUses.isEmpty()
					|| terminalUses.containsValue(ArgType.OBJECT)
					|| concreteTypeCount < 2) {
				continue;
			}
			Set<CodeVar> referenceBoundaries = Collections.newSetFromMap(new IdentityHashMap<>());
			for (Map.Entry<RegisterArg, ArgType> entry : terminalUses.entrySet()) {
				RegisterArg use = entry.getKey();
				InsnNode useInsn = use.getParentInsn();
				ArgType useType = entry.getValue();
				RegisterArg result = useInsn.getResult();
				if (useInsn.getType() == InsnType.MOVE && result != null && result.getSVar() != null) {
					referenceBoundaries.add(result.getSVar().getCodeVar());
				}
				if (!useInsn.replaceArg(use, LiteralArg.make(0, useType))) {
					return false;
				}
				if (useInsn instanceof IfNode) {
					for (int i = 0; i < useInsn.getArgsCount(); i++) {
						if (useInsn.getArg(i).isZeroConst()) {
							useInsn.setArg(i, LiteralArg.make(0, useType));
						}
					}
				}
			}
			for (SSAVar flowVar : flowVars) {
				flowVar.getAssignInsn().add(AFlag.DONT_GENERATE);
			}
			Set<CodeVar> flowCodeVars = Collections.newSetFromMap(new IdentityHashMap<>());
			for (SSAVar flowVar : flowVars) {
				flowCodeVars.add(flowVar.getCodeVar());
			}
			for (CodeVar codeVar : flowCodeVars) {
				List<SSAVar> codeVars = codeVar.getSsaVars();
				if (codeVars.isEmpty() || codeVars.stream().allMatch(flowVars::contains)) {
					codeVar.setType(ArgType.OBJECT);
				}
			}
			for (CodeVar boundary : referenceBoundaries) {
				normalizeReferenceZeroComparisons(boundary);
			}
			return true;
		}
		return false;
	}

	private static boolean collectClosedNullFlow(SSAVar root, Set<SSAVar> flowVars) {
		ArrayDeque<SSAVar> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			SSAVar var = queue.removeFirst();
			if (!flowVars.add(var)) {
				continue;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null) {
				return false;
			}
			InsnType assignType = assignInsn.getType();
			if (assignType == InsnType.CONST) {
				if (assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isZeroConst()) {
					return false;
				}
			} else if (assignType == InsnType.MOVE) {
				if (assignInsn.getArgsCount() != 1) {
					return false;
				}
				InsnArg source = assignInsn.getArg(0);
				if (!source.isZeroConst()) {
					if (!source.isRegister() || ((RegisterArg) source).getSVar() == null) {
						return false;
					}
					queue.add(((RegisterArg) source).getSVar());
				}
			} else if (assignType == InsnType.PHI) {
				if (assignInsn.getArgsCount() == 0) {
					return false;
				}
				for (InsnArg arg : assignInsn.getArguments()) {
					if (!arg.isRegister() || ((RegisterArg) arg).getSVar() == null) {
						return false;
					}
					queue.add(((RegisterArg) arg).getSVar());
				}
			} else {
				return false;
			}
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return false;
				}
				InsnType useType = useInsn.getType();
				if (useType != InsnType.MOVE && useType != InsnType.PHI) {
					continue;
				}
				RegisterArg result = useInsn.getResult();
				if (result == null || result.getSVar() == null) {
					return false;
				}
				ArgType targetType = result.getSVar().getCodeVar().getType();
				if (targetType == null || !targetType.isTypeKnown()) {
					queue.add(result.getSVar());
				}
			}
		}
		return true;
	}

	private static void normalizeReferenceZeroComparisons(CodeVar codeVar) {
		ArgType type = codeVar.getType();
		if (type == null || !type.isTypeKnown() || !type.isObject() && !type.isArray()) {
			return;
		}
		for (SSAVar var : codeVar.getSsaVars()) {
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (!(useInsn instanceof IfNode) || !isZeroComparison(use)) {
					continue;
				}
				use.forceSetInitType(type);
				for (int i = 0; i < useInsn.getArgsCount(); i++) {
					if (useInsn.getArg(i).isZeroConst()) {
						useInsn.setArg(i, LiteralArg.make(0, type));
					}
				}
			}
		}
	}

	private static boolean hasNullFlowCycle(Set<SSAVar> flowVars) {
		Set<SSAVar> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<SSAVar> active = Collections.newSetFromMap(new IdentityHashMap<>());
		for (SSAVar var : flowVars) {
			if (hasNullFlowCycle(var, flowVars, visited, active)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasNullFlowCycle(SSAVar var, Set<SSAVar> flowVars,
			Set<SSAVar> visited, Set<SSAVar> active) {
		if (active.contains(var)) {
			return true;
		}
		if (!visited.add(var)) {
			return false;
		}
		active.add(var);
		for (InsnArg arg : var.getAssignInsn().getArguments()) {
			if (arg.isRegister()) {
				SSAVar input = ((RegisterArg) arg).getSVar();
				if (input != null && flowVars.contains(input)
						&& hasNullFlowCycle(input, flowVars, visited, active)) {
					return true;
				}
			}
		}
		active.remove(var);
		return false;
	}

	private static boolean isGroundedNullFlow(Set<SSAVar> flowVars) {
		Set<SSAVar> grounded = Collections.newSetFromMap(new IdentityHashMap<>());
		boolean changed;
		do {
			changed = false;
			for (SSAVar var : flowVars) {
				if (grounded.contains(var)) {
					continue;
				}
				InsnNode assignInsn = var.getAssignInsn();
				if (assignInsn.getType() == InsnType.CONST
						|| hasGroundedNullInput(assignInsn, grounded)) {
					grounded.add(var);
					changed = true;
				}
			}
		} while (changed);
		return grounded.size() == flowVars.size();
	}

	private static boolean hasGroundedNullInput(InsnNode assignInsn, Set<SSAVar> grounded) {
		for (InsnArg arg : assignInsn.getArguments()) {
			if (arg.isZeroConst()
					|| arg.isRegister() && grounded.contains(((RegisterArg) arg).getSVar())) {
				return true;
			}
		}
		return false;
	}

	private static boolean collectClosedNullFlowTerminals(Set<SSAVar> flowVars,
			Map<RegisterArg, ArgType> terminalUses) {
		for (SSAVar var : flowVars) {
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return false;
				}
				RegisterArg result = useInsn.getResult();
				if ((useInsn.getType() == InsnType.MOVE || useInsn.getType() == InsnType.PHI)
						&& result != null && result.getSVar() != null && flowVars.contains(result.getSVar())) {
					continue;
				}
				if (useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				if (useInsn instanceof IfNode && isZeroComparison(use)) {
					terminalUses.put(use, ArgType.OBJECT);
					continue;
				}
				ArgType useType;
				if (useInsn.getType() == InsnType.MOVE && result != null && result.getSVar() != null) {
					useType = result.getSVar().getCodeVar().getType();
				} else if (useInsn.getType() == InsnType.PHI) {
					return false;
				} else {
					useType = use.getInitType();
				}
				if (useType == null || !useType.isTypeKnown()
						|| !useType.isObject() && !useType.isArray()
						|| useType.containsGeneric() && !isConcreteGenericNullExit(useInsn, useType)) {
					return false;
				}
				terminalUses.put(use, useType);
			}
		}
		return true;
	}

	private static boolean isConcreteGenericNullExit(InsnNode useInsn, ArgType useType) {
		InsnType insnType = useInsn.getType();
		return (insnType == InsnType.RETURN || insnType == InsnType.THROW)
				&& useType.isGeneric() && !useType.containsTypeVariable();
	}

	private static boolean hasGeneratedMoveUse(SSAVar var) {
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn != null && !useInsn.contains(AFlag.DONT_GENERATE) && useInsn.getType() == InsnType.MOVE) {
				return true;
			}
		}
		return false;
	}

	private static boolean collectDirectTypedNullUses(SSAVar var, Map<RegisterArg, ArgType> terminalUses) {
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			ArgType useType = use.getInitType();
			if (!useType.isTypeKnown() || !useType.isObject() && !useType.isArray() || useType.containsGeneric()
					|| useInsn.getType() == InsnType.PHI) {
				return false;
			}
			terminalUses.put(use, useType);
		}
		return true;
	}

	private static boolean collectTypedNullBoundaries(SSAVar var, Set<SSAVar> visited,
			Map<RegisterArg, ArgType> terminalUses, List<RegisterArg> passThroughUses) {
		if (!visited.add(var)) {
			return true;
		}
		for (RegisterArg use : new ArrayList<>(var.getUseList())) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			if (useInsn.getType() == InsnType.MOVE) {
				RegisterArg result = useInsn.getResult();
				if (result == null || result.getSVar() == null) {
					return false;
				}
				ArgType targetType = result.getSVar().getCodeVar().getType();
				if (targetType != null && targetType.isTypeKnown()
						&& (targetType.isObject() || targetType.isArray()) && !targetType.containsGeneric()) {
					terminalUses.put(use, targetType);
				} else {
					passThroughUses.add(use);
					if (!collectTypedNullBoundaries(result.getSVar(), visited, terminalUses, passThroughUses)) {
						return false;
					}
				}
				continue;
			}
			ArgType useType = use.getInitType();
			if (!useType.isTypeKnown() || !useType.isObject() && !useType.isArray() || useType.containsGeneric()
					|| useInsn.getType() == InsnType.PHI) {
				return false;
			}
			terminalUses.put(use, useType);
		}
		return true;
	}

	/**
	 * DEX uses the same zero literal for null values of every reference type. Kotlin default-argument
	 * calls can consume one such register as both a real nullable argument and a
	 * {@code DefaultConstructorMarker}. Emit a typed null at each terminal use only when the value is
	 * recursively proven null and at least two incompatible reference types are required.
	 */
	static boolean replaceProvenNullMultiTypeUses(MethodNode mth, SSAVar var) {
		RegisterArg assign = var.getAssign();
		if (assign == null) {
			return false;
		}
		List<RegisterArg> terminalUses = new ArrayList<>();
		Set<ArgType> useTypes = new LinkedHashSet<>();
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			ArgType useType = use.getInitType();
			if (!useType.isTypeKnown() || !useType.isObject() && !useType.isArray()
					|| useType.containsGeneric()
					|| useInsn.getType() == InsnType.MOVE || useInsn.getType() == InsnType.PHI) {
				return false;
			}
			terminalUses.add(use);
			useTypes.add(useType);
		}
		if (terminalUses.isEmpty() || useTypes.size() < 2) {
			return false;
		}
		if (!isProvenNullValue(assign, new HashSet<>())) {
			return false;
		}
		for (RegisterArg use : terminalUses) {
			InsnNode useInsn = use.getParentInsn();
			if (!useInsn.replaceArg(use, LiteralArg.make(0, use.getInitType()))) {
				return false;
			}
			InsnRemover.unbindArgUsage(mth, use);
		}
		if (mth != null && var.getUseList().isEmpty()) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null
					&& (assignInsn.getType() == InsnType.CONST || assignInsn.getType() == InsnType.MOVE)
					&& BlockUtils.getBlockByInsn(mth, assignInsn) != null) {
				InsnRemover.remove(mth, assignInsn);
			}
		}
		return true;
	}

	private static boolean hasDefaultConstructorMarkerUse(SSAVar var) {
		for (RegisterArg use : var.getUseList()) {
			ArgType type = use.getInitType();
			if (type.isObject() && type.getObject().equals("kotlin.jvm.internal.DefaultConstructorMarker")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Replace a reference MOVE whose source is recursively proven null with a typed null literal.
	 * Register reuse can otherwise leak the source's stale class into an unrelated target local.
	 */
	private static void normalizeProvenNullMoveSources(MethodNode mth) {
		for (SSAVar var : mth.getSVars()) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null || assignInsn.getType() != InsnType.MOVE
					|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()) {
				continue;
			}
			ArgType targetType = var.getCodeVar().getType();
			if (targetType == null || !targetType.isTypeKnown()) {
				RegisterArg result = assignInsn.getResult();
				targetType = result == null ? null : result.getInitType();
			}
			if (targetType == null || !targetType.isTypeKnown()
					|| !targetType.isObject() && !targetType.isArray()) {
				continue;
			}
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			if (!isProvenNullValue(source, new HashSet<>())) {
				continue;
			}
			if (assignInsn.replaceArg(source, LiteralArg.make(0, targetType))) {
				InsnRemover.unbindArgUsage(mth, source);
			}
		}
	}

	private static boolean collectTerminalReferenceUseTypes(SSAVar var, Set<SSAVar> visited, Set<ArgType> useTypes) {
		if (!visited.add(var)) {
			return true;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn instanceof IfNode && isZeroComparison(useArg)) {
				continue;
			}
			if (useInsn.getType() == InsnType.MOVE || useInsn.getType() == InsnType.PHI) {
				RegisterArg result = useInsn.getResult();
				if (result == null || result.getSVar() == null
						|| !collectTerminalReferenceUseTypes(result.getSVar(), visited, useTypes)) {
					return false;
				}
				continue;
			}
			ArgType type = useArg.getInitType();
			if (!type.isTypeKnown() || (!type.isObject() && !type.isArray()) || type.containsGeneric()) {
				return false;
			}
			useTypes.add(type);
		}
		return true;
	}

	private static ArgType getKnownValueType(RegisterArg arg, Set<SSAVar> visited) {
		SSAVar var = arg.getSVar();
		if (var == null || !visited.add(var)) {
			return null;
		}
		ArgType type = var.getCodeVar().getType();
		if (type != null && type.isTypeKnown()) {
			return type;
		}
		type = var.getTypeInfo().getType();
		if (type.isTypeKnown()) {
			return type;
		}
		type = var.getImmutableType();
		if (type != null && type.isTypeKnown()) {
			return type;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn != null && assignInsn.getType() == InsnType.MOVE
				&& assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isRegister()) {
			return getKnownValueType((RegisterArg) assignInsn.getArg(0), visited);
		}
		return null;
	}

	static boolean isProvenNullValue(RegisterArg arg, Set<SSAVar> visited) {
		SSAVar var = arg.getSVar();
		if (var == null || !visited.add(var)) {
			return false;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return false;
		}
		if (assignInsn.getType() == InsnType.CONST) {
			return assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst();
		}
		if (assignInsn.getType() == InsnType.MOVE) {
			if (assignInsn.getArgsCount() != 1) {
				return false;
			}
			InsnArg source = assignInsn.getArg(0);
			return source.isZeroConst()
					|| source.isRegister() && isProvenNullValue((RegisterArg) source, visited);
		}
		if (assignInsn.getType() != InsnType.PHI || assignInsn.getArgsCount() == 0) {
			return false;
		}
		for (InsnArg phiArg : assignInsn.getArguments()) {
			if (!phiArg.isRegister() || !isProvenNullValue((RegisterArg) phiArg, new HashSet<>(visited))) {
				return false;
			}
		}
		return true;
	}

	private static ArgType selectSinglePrimitiveType(List<SSAVar> vars) {
		Set<ArgType> candidates = new LinkedHashSet<>();
		for (SSAVar var : vars) {
			ArgType type = var.getTypeInfo().getType();
			if (type.isTypeKnown()) {
				if (!type.isPrimitive()) {
					return null;
				}
				candidates.add(type);
				continue;
			}
			PrimitiveType[] possibleTypes = type.getPossibleTypes();
			if (possibleTypes.length == 1
					&& possibleTypes[0] != PrimitiveType.OBJECT
					&& possibleTypes[0] != PrimitiveType.ARRAY) {
				candidates.add(ArgType.unknown(possibleTypes[0]).selectFirst());
			}
			for (ITypeBound bound : var.getTypeInfo().getBounds()) {
				ArgType boundType = bound.getType();
				if (boundType.isTypeKnown()
						&& boundType.isPrimitive()
						&& bound.getBound() == BoundEnum.USE) {
					candidates.add(boundType);
				}
			}
		}
		if (candidates.size() != 1) {
			return null;
		}
		ArgType candidate = candidates.iterator().next();
		PrimitiveType primitiveType = candidate.getPrimitiveType();
		for (SSAVar var : vars) {
			ArgType type = var.getTypeInfo().getType();
			if (type.isTypeKnown()) {
				if (!candidate.equals(type)) {
					return null;
				}
			} else if (!contains(type.getPossibleTypes(), primitiveType)) {
				return null;
			}
		}
		return candidate;
	}

	/**
	 * Recover an array declaration when all concrete evidence names the same array type. Unknown
	 * array-element uses ({@code aget} and {@code array-length}) don't provide a competing type.
	 * Reject any concrete object or primitive evidence to avoid merging register lifetimes such as
	 * a query-arguments array later reused for a Cursor.
	 */
	static ArgType selectSingleArrayType(List<SSAVar> vars) {
		Set<ArgType> candidates = new LinkedHashSet<>();
		for (SSAVar var : vars) {
			List<ArgType> types = new ArrayList<>();
			types.add(var.getTypeInfo().getType());
			types.add(var.getImmutableType());
			var.getTypeInfo().getBounds().forEach(bound -> types.add(bound.getType()));
			for (ArgType type : types) {
				if (type == null || !type.isTypeKnown()) {
					continue;
				}
				if (!type.isArray()) {
					return null;
				}
				candidates.add(type);
			}
		}
		return candidates.size() == 1 ? candidates.iterator().next() : null;
	}

	private static boolean contains(PrimitiveType[] types, PrimitiveType expected) {
		for (PrimitiveType type : types) {
			if (type == expected) {
				return true;
			}
		}
		return false;
	}

	static ArgType selectMoveSourceType(List<SSAVar> vars, CodeVar codeVar) {
		Set<ArgType> sourceTypes = new LinkedHashSet<>();
		boolean hasExternalMove = false;
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null || assignInsn.getType() == InsnType.PHI) {
				continue;
			}
			if (assignInsn.getType() != InsnType.MOVE || assignInsn.getArgsCount() != 1) {
				return null;
			}
			if (!assignInsn.getArg(0).isRegister()) {
				return null;
			}
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			CodeVar sourceCodeVar = source.getSVar().getCodeVar();
			if (sourceCodeVar == codeVar) {
				continue;
			}
			ArgType sourceType = sourceCodeVar.getType();
			if (sourceType == null || !sourceType.isTypeKnown()) {
				continue;
			}
			hasExternalMove = true;
			sourceTypes.add(sourceType);
		}
		return hasExternalMove && sourceTypes.size() == 1 ? sourceTypes.iterator().next() : null;
	}

	private static boolean isOnlyUsedByObjectCasts(List<SSAVar> vars) {
		boolean hasObjectCast = false;
		Set<SSAVar> visited = new HashSet<>();
		ArrayDeque<SSAVar> queue = new ArrayDeque<>(vars);
		while (!queue.isEmpty()) {
			SSAVar var = queue.removeFirst();
			if (!visited.add(var)) {
				continue;
			}
			for (RegisterArg use : var.getUseList()) {
				if (use.getParentInsn() == null || use.getParentInsn().contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				InsnType useType = use.getParentInsn().getType();
				if (useType != InsnType.CHECK_CAST && useType != InsnType.MOVE) {
					return false;
				}
				hasObjectCast |= useType == InsnType.CHECK_CAST;
				if (useType == InsnType.MOVE && use.getParentInsn().getResult() != null
						&& use.getParentInsn().getResult().getSVar() != null) {
					queue.add(use.getParentInsn().getResult().getSVar());
				}
			}
		}
		return hasObjectCast;
	}

	static ArgType selectMoveTargetType(List<SSAVar> vars, CodeVar codeVar) {
		Set<ArgType> targetTypes = new LinkedHashSet<>();
		boolean hasUse = false;
		for (SSAVar var : vars) {
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof PhiInsn
						&& useInsn.getResult() != null
						&& useInsn.getResult().getSVar() != null
						&& useInsn.getResult().getSVar().getCodeVar() == codeVar) {
					// Internal PHI edges only connect SSA versions of this Java variable.
					continue;
				}
				hasUse = true;
				if (useInsn == null
						|| useInsn.getType() != InsnType.MOVE
						|| useInsn.getResult() == null
						|| useInsn.getResult().getSVar() == null) {
					return null;
				}
				ArgType targetType = useInsn.getResult().getSVar().getCodeVar().getType();
				if (targetType == null || !targetType.isTypeKnown() || !targetType.isObject()) {
					return null;
				}
				targetTypes.add(targetType);
			}
		}
		return hasUse && targetTypes.size() == 1 ? targetTypes.iterator().next() : null;
	}

	/**
	 * Exception-region splitting can create a second code variable for a reference loop while its
	 * incoming values still arrive through the original {@link Object}-typed null/PHI lifetime. If
	 * every concrete input agrees on one reference type, every broad input is a closed MOVE/PHI flow
	 * rooted in that type or null, and every output is assignable to that type, recover the narrow
	 * Java local. Declared variables are accepted only when the same closed-flow proof succeeds; a
	 * genuinely mixed declared lifetime fails the concrete input and output checks below.
	 */
	static ArgType selectClosedReferenceMoveType(List<SSAVar> vars, CodeVar codeVar) {
		if (vars.size() < 3 || codeVar.isThis()) {
			return null;
		}
		Set<ArgType> concreteTypes = new LinkedHashSet<>();
		boolean broadInput = false;
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn) {
				continue;
			}
			if (assignInsn == null || assignInsn.getType() != InsnType.MOVE
					|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()) {
				return null;
			}
			RegisterArg source = (RegisterArg) assignInsn.getArg(0);
			if (source.getSVar() != null && source.getSVar().getCodeVar() == codeVar) {
				continue;
			}
			ArgType sourceType = getKnownValueType(source, new HashSet<>());
			if (ArgType.OBJECT.equals(sourceType)) {
				broadInput = true;
			} else if (isConcreteReferenceType(sourceType) && sourceType.isObject()) {
				concreteTypes.add(eraseGenericObjectType(sourceType));
			} else {
				return null;
			}
		}
		if (!broadInput || concreteTypes.size() != 1) {
			return null;
		}
		ArgType candidate = concreteTypes.iterator().next();
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null && assignInsn.getType() == InsnType.MOVE) {
				boolean[] rootFound = { false };
				if (!isClosedReferenceValue(
						(RegisterArg) assignInsn.getArg(0), candidate, new HashSet<>(), rootFound)
						|| !rootFound[0]) {
					return null;
				}
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return null;
				}
				RegisterArg result = useInsn.getResult();
				if ((useInsn instanceof PhiInsn || useInsn.getType() == InsnType.MOVE)
						&& result != null && result.getSVar() != null
						&& result.getSVar().getCodeVar() == codeVar) {
					continue;
				}
				if (isMatchingInvokeArgUse(useInsn, use, candidate)) {
					continue;
				}
				if (useInsn.getType() != InsnType.MOVE || result == null || result.getSVar() == null) {
					return null;
				}
				ArgType targetType = result.getSVar().getCodeVar().getType();
				if (!sameRawObjectType(candidate, targetType) && !ArgType.OBJECT.equals(targetType)) {
					return null;
				}
			}
		}
		return candidate;
	}

	private static ArgType eraseGenericObjectType(ArgType type) {
		return type.containsGeneric() ? ArgType.object(type.getObject()) : type;
	}

	private static boolean sameRawObjectType(ArgType first, ArgType second) {
		return first != null && second != null && first.isObject() && second.isObject()
				&& first.getObject().equals(second.getObject());
	}

	private static boolean isMatchingInvokeArgUse(InsnNode useInsn, RegisterArg use, ArgType candidate) {
		if (!(useInsn instanceof InvokeNode)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) useInsn;
		if (invoke.getInstanceArg() == use) {
			return false;
		}
		int argIndex = invoke.getArgIndex(use) - invoke.getFirstArgOffset();
		List<ArgType> argTypes = invoke.getCallMth().getArgumentsTypes();
		return argIndex >= 0 && argIndex < argTypes.size()
				&& sameRawObjectType(candidate, argTypes.get(argIndex));
	}

	/**
	 * Exception PHIs can merge several reference lifetimes whose common concrete subtype is no longer
	 * recoverable. Java's {@link Object} is still an exact, lossless representation for the DEX
	 * register. Select it only when every incoming edge is reference-only and every generated boundary
	 * consumes a reference; primitive-capable values and receiver calls are deliberately rejected.
	 */
	static boolean isReferenceOnlyPhiFlow(List<SSAVar> vars, CodeVar codeVar) {
		if (vars.size() < 2 || codeVar.isThis()) {
			return false;
		}
		boolean hasPhi = false;
		boolean hasConcreteBoundary = false;
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn instanceof PhiInsn) {
				hasPhi = true;
			} else if (assignInsn == null || assignInsn.getType() != InsnType.MOVE
					|| assignInsn.getArgsCount() != 1 || !assignInsn.getArg(0).isRegister()
					|| !isReferenceOnlyArg((RegisterArg) assignInsn.getArg(0))) {
				return false;
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null) {
					return false;
				}
				RegisterArg result = useInsn.getResult();
				if ((useInsn instanceof PhiInsn || useInsn.getType() == InsnType.MOVE)
						&& result != null && result.getSVar() != null
						&& result.getSVar().getCodeVar() == codeVar) {
					continue;
				}
				if (useInsn.getType() == InsnType.MOVE && result != null && result.getSVar() != null) {
					ArgType targetType = result.getSVar().getCodeVar().getType();
					if (!isConcreteReferenceType(targetType)) {
						return false;
					}
					hasConcreteBoundary = true;
					continue;
				}
				if (!(useInsn instanceof InvokeNode)) {
					return false;
				}
				InvokeNode invoke = (InvokeNode) useInsn;
				if (invoke.getInstanceArg() == use) {
					return false;
				}
				int argIndex = invoke.getArgIndex(use) - invoke.getFirstArgOffset();
				List<ArgType> argTypes = invoke.getCallMth().getArgumentsTypes();
				if (argIndex < 0 || argIndex >= argTypes.size()
						|| !isConcreteReferenceType(argTypes.get(argIndex))) {
					return false;
				}
				hasConcreteBoundary = true;
			}
		}
		return hasPhi && hasConcreteBoundary;
	}

	static void repairLateReferenceOnlyPhiFlows(MethodNode mth, List<SSAVar> vars) {
		for (Map.Entry<CodeVar, List<SSAVar>> entry : collectCodeVarGroups(vars).entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if ((currentType == null || !currentType.isTypeKnown())
					&& isReferenceOnlyPhiFlow(entry.getValue(), codeVar)) {
				insertReferenceInvokeBoundaryCasts(mth, entry.getValue());
				markCodeVarType(codeVar, entry.getValue(), ArgType.OBJECT);
			}
		}
	}

	private static void insertReferenceInvokeBoundaryCasts(MethodNode mth, List<SSAVar> vars) {
		for (SSAVar var : vars) {
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (!(useInsn instanceof InvokeNode)) {
					continue;
				}
				InvokeNode invoke = (InvokeNode) useInsn;
				if (invoke.getInstanceArg() == use) {
					continue;
				}
				int argIndex = invoke.getArgIndex(use) - invoke.getFirstArgOffset();
				List<ArgType> argTypes = invoke.getCallMth().getArgumentsTypes();
				if (argIndex < 0 || argIndex >= argTypes.size()) {
					continue;
				}
				ArgType formalType = argTypes.get(argIndex);
				if (!isConcreteReferenceType(formalType) || ArgType.OBJECT.equals(formalType)) {
					continue;
				}
				ArgType castType = formalType.containsGeneric() ? ArgType.object(formalType.getObject()) : formalType;
				IndexInsnNode castInsn = new IndexInsnNode(InsnType.CHECK_CAST, castType, 1);
				RegisterArg castSource = use.duplicate();
				castSource.forceSetInitType(ArgType.OBJECT);
				castInsn.addArg(castSource);
				castInsn.add(AFlag.SYNTHETIC);
				castInsn.add(AFlag.EXPLICIT_CAST);
				InsnArg castArg = InsnArg.wrapInsnIntoArg(castInsn);
				castArg.setType(castType);
				if (useInsn.replaceArg(use, castArg)) {
					InsnRemover.unbindArgUsage(mth, use);
				}
			}
		}
	}

	private static boolean isReferenceOnlyArg(RegisterArg arg) {
		ArgType type = getKnownValueType(arg, new HashSet<>());
		if (type != null && type.isTypeKnown()) {
			return type.isObject() || type.isArray();
		}
		PrimitiveType[] possibleTypes = arg.getInitType().getPossibleTypes();
		if (possibleTypes.length == 0) {
			return false;
		}
		for (PrimitiveType possibleType : possibleTypes) {
			if (possibleType != PrimitiveType.OBJECT && possibleType != PrimitiveType.ARRAY) {
				return false;
			}
		}
		return true;
	}

	private static boolean isClosedReferenceValue(
			RegisterArg arg, ArgType candidate, Set<SSAVar> visiting, boolean[] rootFound) {
		SSAVar var = arg.getSVar();
		if (var == null) {
			return false;
		}
		ArgType codeType = var.getCodeVar().getType();
		if (codeType != null && codeType.isTypeKnown() && !ArgType.OBJECT.equals(codeType)) {
			if (sameRawObjectType(candidate, codeType)) {
				rootFound[0] = true;
				return true;
			}
			return false;
		}
		if (!visiting.add(var)) {
			return true;
		}
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null) {
			return false;
		}
		if (assignInsn.getType() == InsnType.CONST) {
			boolean isNull = assignInsn.getArgsCount() == 1 && assignInsn.getArg(0).isZeroConst();
			if (isNull) {
				rootFound[0] = true;
			}
			return isNull;
		}
		if (assignInsn.getType() == InsnType.MOVE) {
			if (assignInsn.getArgsCount() != 1) {
				return false;
			}
			InsnArg source = assignInsn.getArg(0);
			if (source.isZeroConst()) {
				rootFound[0] = true;
				return true;
			}
			return source.isRegister()
					&& isClosedReferenceValue((RegisterArg) source, candidate, visiting, rootFound);
		}
		if (assignInsn instanceof PhiInsn) {
			for (InsnArg phiArg : assignInsn.getArguments()) {
				if (!phiArg.isRegister()
						|| !isClosedReferenceValue(
								(RegisterArg) phiArg, candidate, new HashSet<>(visiting), rootFound)) {
					return false;
				}
			}
			return true;
		}
		RegisterArg result = assignInsn.getResult();
		if (result != null && sameRawObjectType(candidate, result.getInitType())) {
			rootFound[0] = true;
			return true;
		}
		return false;
	}

	/**
	 * Type splitting can leave an integer loop state behind an unknown PHI code variable and expose
	 * its type only through a synthetic MOVE into an already proven {@code int} local. Recover that
	 * type only when every external assignment is independently int-compatible and every generated
	 * boundary is such a synthetic int MOVE. This deliberately excludes boolean and other primitive
	 * targets, which can require a Java-level conversion rather than a plain DEX value copy.
	 */
	static ArgType selectSyntheticIntMoveTargetType(List<SSAVar> vars, CodeVar codeVar) {
		boolean hasSyntheticIntTarget = false;
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null && assignInsn.getType() != InsnType.PHI) {
				if (assignInsn.getType() == InsnType.CONST && assignInsn.getArgsCount() == 1) {
					if (!assignInsn.getArg(0).isLiteral()
							|| !assignInsn.getArg(0).getType().canBePrimitive(PrimitiveType.INT)) {
						return null;
					}
				} else if (assignInsn.getType() == InsnType.MOVE && assignInsn.getArgsCount() == 1
						&& assignInsn.getArg(0).isRegister()) {
					ArgType sourceType = getKnownValueType((RegisterArg) assignInsn.getArg(0), new HashSet<>());
					if (!ArgType.INT.equals(sourceType)) {
						return null;
					}
				} else {
					return null;
				}
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof PhiInsn
						&& useInsn.getResult() != null
						&& useInsn.getResult().getSVar() != null
						&& useInsn.getResult().getSVar().getCodeVar() == codeVar) {
					continue;
				}
				if (useInsn == null || useInsn.getType() != InsnType.MOVE
						|| !useInsn.contains(AFlag.SYNTHETIC)
						|| useInsn.getResult() == null || useInsn.getResult().getSVar() == null
						|| !ArgType.INT.equals(useInsn.getResult().getSVar().getCodeVar().getType())) {
					return null;
				}
				hasSyntheticIntTarget = true;
			}
		}
		return hasSyntheticIntTarget ? ArgType.INT : null;
	}

	private static ArgType selectObjectType(List<SSAVar> vars, TypeCompare typeCompare) {
		Set<ArgType> specificTypes = new LinkedHashSet<>();
		boolean hasObject = false;
		for (SSAVar var : vars) {
			List<ArgType> types = new ArrayList<>();
			types.add(var.getTypeInfo().getType());
			types.add(var.getImmutableType());
			var.getTypeInfo().getBounds().forEach(bound -> types.add(bound.getType()));
			for (ArgType type : types) {
				if (type == null || !type.isTypeKnown()) {
					continue;
				}
				if (type.isWildcard()) {
					type = type.getWildcardType();
					if (type == null) {
						continue;
					}
				}
				if (!type.isObject()) {
					return null;
				}
				if (type.equals(ArgType.OBJECT)) {
					hasObject = true;
				} else {
					specificTypes.add(type);
				}
			}
		}
		if (specificTypes.size() > 1) {
			ArgType genericReturnType = selectGenericReturnType(vars);
			if (genericReturnType != null) {
				return genericReturnType;
			}
			ArgType functionType = selectKotlinFunctionAssignType(vars);
			if (functionType != null) {
				return functionType;
			}
			ArgType receiverType = selectInvokeReceiverAssignType(vars);
			if (receiverType != null) {
				return receiverType;
			}
			ArgType assignUseType = selectAssignUseType(vars);
			if (assignUseType != null) {
				return assignUseType;
			}
			ArgType mostSpecificType = selectMostSpecificType(specificTypes, typeCompare);
			if (mostSpecificType != null) {
				return mostSpecificType;
			}
			return hasObject ? ArgType.OBJECT : null;
		}
		if (specificTypes.size() == 1) {
			ArgType specificType = specificTypes.iterator().next();
			if (specificType.isGenericType()) {
				return specificType;
			}
		}
		if (hasObject) {
			return ArgType.OBJECT;
		}
		return specificTypes.stream().findFirst().orElse(null);
	}

	/**
	 * A compiler can cast a newly created concrete collection to an unconstrained method type
	 * variable and then keep using the same register through a collection interface. Preserve the
	 * generic local so code generation inserts explicit casts at the concrete interface uses.
	 */
	static ArgType selectGenericReturnType(List<SSAVar> vars) {
		if (vars.size() != 1) {
			return null;
		}
		SSAVar var = vars.get(0);
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getType() != InsnType.CHECK_CAST) {
			return null;
		}
		Set<ArgType> candidates = new LinkedHashSet<>();
		for (ITypeBound bound : var.getTypeInfo().getBounds()) {
			ArgType type = bound.getType();
			if (type.isGenericType()) {
				candidates.add(type);
			}
		}
		if (candidates.size() != 1) {
			return null;
		}
		ArgType candidate = candidates.iterator().next();
		boolean hasGenericReturn = false;
		for (RegisterArg use : var.getUseList()) {
			InsnNode useInsn = use.getParentInsn();
			if (useInsn == null || useInsn.contains(AFlag.DONT_GENERATE)) {
				continue;
			}
			ArgType useType = use.getInitType();
			if (useInsn.getType() == InsnType.RETURN && candidate.equals(useType)) {
				hasGenericReturn = true;
				continue;
			}
			if (!useType.isTypeKnown() || !useType.isObject()) {
				return null;
			}
		}
		return hasGenericReturn ? candidate : null;
	}

	private static void insertGenericConcreteUseCasts(MethodNode mth, List<SSAVar> vars, ArgType genericType) {
		for (SSAVar var : vars) {
			for (RegisterArg use : new ArrayList<>(var.getUseList())) {
				InsnNode useInsn = use.getParentInsn();
				if (!(useInsn instanceof InvokeNode)
						|| ((InvokeNode) useInsn).getInstanceArg() != use
						|| useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				ArgType useType = use.getInitType();
				if (!useType.isTypeKnown() || !useType.isObject() || useType.equals(genericType)) {
					continue;
				}
				IndexInsnNode castInsn = new IndexInsnNode(InsnType.CAST, useType, 1);
				RegisterArg castSource = use.duplicate();
				castSource.forceSetInitType(genericType);
				castInsn.addArg(castSource);
				castInsn.add(AFlag.SYNTHETIC);
				castInsn.add(AFlag.EXPLICIT_CAST);
				InsnArg castArg = InsnArg.wrapInsnIntoArg(castInsn);
				castArg.setType(useType);
				useInsn.replaceArg(use, castArg);
				InsnRemover.unbindArgUsage(mth, use);
			}
		}
	}

	private static ArgType selectMostSpecificType(Set<ArgType> types, TypeCompare typeCompare) {
		ArgType result = null;
		for (ArgType candidate : types) {
			boolean mostSpecific = true;
			for (ArgType other : types) {
				if (candidate != other && !typeCompare.compareTypes(candidate, other).isNarrowOrEqual()) {
					mostSpecific = false;
					break;
				}
			}
			if (mostSpecific) {
				if (result != null) {
					return null;
				}
				result = candidate;
			}
		}
		return result;
	}

	/**
	 * If hierarchy metadata is unavailable, a matching assignment and virtual-call receiver still
	 * provide direct bytecode evidence for the Java declaration type.
	 */
	static ArgType selectInvokeReceiverAssignType(List<SSAVar> vars) {
		Set<ArgType> assignTypes = new LinkedHashSet<>();
		Set<ArgType> receiverTypes = new LinkedHashSet<>();
		for (SSAVar var : vars) {
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn != null && assignInsn.getResult() != null) {
				ArgType resultType = assignInsn.getResult().getInitType();
				if (resultType.isTypeKnown() && resultType.isObject()) {
					assignTypes.add(resultType);
				}
			}
			for (ITypeBound bound : var.getTypeInfo().getBounds()) {
				ArgType type = bound.getType();
				if (bound.getBound() == BoundEnum.ASSIGN && type.isTypeKnown() && type.isObject()) {
					assignTypes.add(type);
				}
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof InvokeNode && ((InvokeNode) useInsn).getInstanceArg() == use) {
					receiverTypes.add(((InvokeNode) useInsn).getCallMth().getDeclClass().getType());
				}
			}
		}
		receiverTypes.retainAll(assignTypes);
		return receiverTypes.size() == 1 ? receiverTypes.iterator().next() : null;
	}

	static ArgType selectAssignUseType(List<SSAVar> vars) {
		Set<ArgType> assignTypes = new LinkedHashSet<>();
		Set<ArgType> useTypes = new LinkedHashSet<>();
		for (SSAVar var : vars) {
			for (ITypeBound bound : var.getTypeInfo().getBounds()) {
				ArgType type = bound.getType();
				if (!type.isTypeKnown() || !type.isObject()) {
					continue;
				}
				if (bound.getBound() == BoundEnum.ASSIGN) {
					assignTypes.add(type);
				} else if (bound.getBound() == BoundEnum.USE) {
					useTypes.add(type);
				}
			}
		}
		Set<ArgType> candidates = new LinkedHashSet<>(assignTypes);
		candidates.retainAll(useTypes);
		if (candidates.size() != 1) {
			return null;
		}
		return candidates.iterator().next();
	}

	/**
	 * Kotlin lambda instances can carry a broad FunctionN assignment bound and a concrete synthetic
	 * class use bound for the same invoke call. The FunctionN interface is the valid Java declaration
	 * type when every generated use is that interface's invoke operation.
	 */
	static ArgType selectKotlinFunctionAssignType(List<SSAVar> vars) {
		Set<ArgType> assignTypes = new LinkedHashSet<>();
		boolean hasGeneratedUse = false;
		for (SSAVar var : vars) {
			for (ITypeBound bound : var.getTypeInfo().getBounds()) {
				ArgType type = bound.getType();
				if (bound.getBound() == BoundEnum.ASSIGN && isKotlinFunctionType(type)) {
					assignTypes.add(type);
				}
			}
			for (RegisterArg use : var.getUseList()) {
				InsnNode useInsn = use.getParentInsn();
				if (useInsn == null || useInsn.contains(AFlag.DONT_GENERATE)) {
					continue;
				}
				hasGeneratedUse = true;
				if (!(useInsn instanceof InvokeNode)
						|| !((InvokeNode) useInsn).getCallMth().getName().equals("invoke")) {
					return null;
				}
			}
		}
		return hasGeneratedUse && assignTypes.size() == 1 ? assignTypes.iterator().next() : null;
	}

	private static boolean isKotlinFunctionType(ArgType type) {
		return type != null
				&& type.isTypeKnown()
				&& type.isObject()
				&& type.getObject().startsWith("kotlin.jvm.functions.Function");
	}

	static boolean hasGeneratedUse(SSAVar var) {
		return var.getUseList().stream()
				.map(arg -> arg.getParentInsn())
				.anyMatch(insn -> insn != null && !insn.contains(AFlag.DONT_GENERATE));
	}

	@Override
	public String getName() {
		return "FinishTypeInference";
	}
}
