package jadx.core.dex.visitors.typeinference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;

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
		repairPhiCodeVarTypes(mth);
		mth.getSVars().forEach(var -> {
			ArgType type = var.getTypeInfo().getType();
			ArgType codeVarType = var.getCodeVar().getType();
			if (!type.isTypeKnown()
					&& (codeVarType == null || !codeVarType.isTypeKnown())
					&& hasGeneratedUse(var)) {
				mth.addWarnComment("Type inference failed for: " + var.getDetailedVarInfo(mth));
			}
			if (codeVarType == null) {
				var.getCodeVar().setType(ArgType.UNKNOWN);
			}
		});
	}

	private static void repairPhiCodeVarTypes(MethodNode mth) {
		Map<CodeVar, List<SSAVar>> groups = new IdentityHashMap<>();
		for (SSAVar var : mth.getSVars()) {
			groups.computeIfAbsent(var.getCodeVar(), key -> new ArrayList<>()).add(var);
		}
		for (Map.Entry<CodeVar, List<SSAVar>> entry : groups.entrySet()) {
			CodeVar codeVar = entry.getKey();
			ArgType currentType = codeVar.getType();
			if (currentType == null || !currentType.isTypeKnown()) {
				ArgType primitiveType = selectSinglePrimitiveType(entry.getValue());
				if (primitiveType != null) {
					codeVar.setType(primitiveType);
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
			ArgType candidate = selectObjectType(entry.getValue());
			if (candidate != null && (!refineObject || candidate.isGenericType())) {
				codeVar.setType(candidate);
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
						moveType = selectMoveTargetType(entry.getValue());
					}
					if (moveType != null) {
						codeVar.setType(moveType);
						changed = true;
					}
				}
			}
		} while (changed);
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
	}

	/**
	 * Recover a late code-variable type for a PHI which merges one concrete reference value with
	 * a value proven to be null. Compose default-argument code can carry the same null through
	 * unrelated typed calls, leaving the PHI use itself as just {@code OBJECT|ARRAY}.
	 */
	private static ArgType selectNullablePhiUseType(List<SSAVar> vars) {
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

	private static boolean collectTerminalReferenceUseTypes(SSAVar var, Set<SSAVar> visited, Set<ArgType> useTypes) {
		if (!visited.add(var)) {
			return true;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null) {
				return false;
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

	private static boolean contains(PrimitiveType[] types, PrimitiveType expected) {
		for (PrimitiveType type : types) {
			if (type == expected) {
				return true;
			}
		}
		return false;
	}

	private static ArgType selectMoveSourceType(List<SSAVar> vars, CodeVar codeVar) {
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
			if (!sourceType.isObject()) {
				return null;
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

	private static ArgType selectMoveTargetType(List<SSAVar> vars) {
		Set<ArgType> targetTypes = new LinkedHashSet<>();
		boolean hasUse = false;
		for (SSAVar var : vars) {
			for (RegisterArg use : var.getUseList()) {
				hasUse = true;
				if (use.getParentInsn() == null
						|| use.getParentInsn().getType() != InsnType.MOVE
						|| use.getParentInsn().getResult() == null
						|| use.getParentInsn().getResult().getSVar() == null) {
					return null;
				}
				ArgType targetType = use.getParentInsn().getResult().getSVar().getCodeVar().getType();
				if (targetType == null || !targetType.isTypeKnown() || !targetType.isObject()) {
					return null;
				}
				targetTypes.add(targetType);
			}
		}
		return hasUse && targetTypes.size() == 1 ? targetTypes.iterator().next() : null;
	}

	private static ArgType selectObjectType(List<SSAVar> vars) {
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
