package jadx.core.dex.visitors.typeinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.Consts;
import jadx.core.clsp.ClspClass;
import jadx.core.clsp.ClspGraph;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IMethodDetails;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.InitCodeVariables;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.ModVisitor;
import jadx.core.dex.visitors.blocks.BlockSplitter;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxOverflowException;

@JadxVisitor(
		name = "Fix Types Visitor",
		desc = "Try various methods to fix unresolved types",
		runAfter = {
				TypeInferenceVisitor.class
		},
		runBefore = {
				FinishTypeInference.class
		}
)
public final class FixTypesVisitor extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(FixTypesVisitor.class);
	private static final int EXCEPTION_MOVE_MAX_BLOCKS = 64;

	private final TypeInferenceVisitor typeInference = new TypeInferenceVisitor();

	private TypeUpdate typeUpdate;
	private List<Function<MethodNode, Boolean>> resolvers;

	@Override
	public void init(RootNode root) {
		this.typeUpdate = root.getTypeUpdate();
		this.typeInference.init(root);
		this.resolvers = Arrays.asList(
				this::applyFieldType,
				this::trySplitWideAssignUses,
				this::tryRestoreTypeVarCasts,
				this::tryRestorePhiConcreteType,
				this::tryInsertCasts,
				this::tryDeduceTypes,
				this::trySplitConstInsns,
				this::tryToFixIncompatiblePrimitives,
				this::tryToForceImmutableTypes,
				this::tryInsertAdditionalMove,
				this::runMultiVariableSearch,
				this::tryRemoveGenerics);
	}

	/**
	 * Split a value assigned or explicitly cast to a wide erased type from uses which require a narrower type.
	 *
	 * <p>This occurs for generic overrides whose resolved return type is more specific than
	 * the erased DEX implementation, for example a method returning {@code Parcelable} which
	 * implements {@code NavType<Model>}. Keep the erased assignment and add the Java-level
	 * narrowing cast only at the concrete use.</p>
	 */
	private boolean trySplitWideAssignUses(MethodNode mth) {
		if (mth.getAccessFlags().isBridge()
				|| !mth.contains(AType.METHOD_OVERRIDE)
				|| mth.getReturnType().equals(mth.getMethodInfo().getReturnType())) {
			return false;
		}
		int added = 0;
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			if (var.getTypeInfo().getType().isTypeKnown() || var.isTypeImmutable()) {
				continue;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null
					|| assignInsn.getType() != InsnType.CHECK_CAST && assignInsn.getType() != InsnType.INVOKE) {
				continue;
			}
			ArgType assignType = assignInsn.getType() == InsnType.CHECK_CAST
					? ((IndexInsnNode) assignInsn).getIndexAsType()
					: assignInsn.getResult().getInitType();
			if (!assignType.isTypeKnown() || (!assignType.isObject() && !assignType.isArray())) {
				continue;
			}
			for (RegisterArg useArg : new ArrayList<>(var.getUseList())) {
				ArgType useType = useArg.getInitType();
				if (!useType.isTypeKnown() || (!useType.isObject() && !useType.isArray())) {
					continue;
				}
				TypeCompareEnum compare = typeUpdate.getTypeCompare().compareTypes(useType, assignType);
				if (!compare.isNarrow()) {
					continue;
				}
				IndexInsnNode castInsn = insertUseCast(mth, useArg, useType);
				if (castInsn != null) {
					((RegisterArg) castInsn.getArg(0)).forceSetInitType(assignType);
					castInsn.add(AFlag.EXPLICIT_CAST);
					added++;
				}
			}
		}
		if (added == 0) {
			return false;
		}
		InitCodeVariables.rerun(mth);
		typeInference.initTypeBounds(mth);
		return typeInference.runTypePropagation(mth);
	}

	@Override
	public void visit(MethodNode mth) {
		if (mth.isNoCode() || checkTypes(mth)) {
			return;
		}
		try {
			for (Function<MethodNode, Boolean> resolver : resolvers) {
				try {
					if (resolver.apply(mth) && checkTypes(mth)) {
						break;
					}
				} catch (JadxOverflowException e) {
					LOG.debug("Type update limit reached in resolver for method: {}", mth, e);
				}
			}
		} catch (Exception e) {
			mth.addError("Types fix failed", e);
		}
	}

	/**
	 * Check if all types resolved
	 */
	private static boolean checkTypes(MethodNode mth) {
		for (SSAVar var : mth.getSVars()) {
			ArgType type = var.getTypeInfo().getType();
			if (!type.isTypeKnown()) {
				return false;
			}
		}
		return true;
	}

	private boolean runMultiVariableSearch(MethodNode mth) {
		try {
			TypeSearch typeSearch = new TypeSearch(mth);
			typeSearch.run();
			for (SSAVar var : mth.getSVars()) {
				if (!var.getTypeInfo().getType().isTypeKnown()) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			LOG.debug("Multi-variable type inference failed in method: {}", mth, e);
			return false;
		}
	}

	private boolean setBestType(MethodNode mth, SSAVar ssaVar) {
		try {
			return calculateFromBounds(mth, ssaVar);
		} catch (JadxOverflowException e) {
			throw e;
		} catch (Exception e) {
			mth.addWarnComment("Failed to calculate best type for var: " + ssaVar, e);
			return false;
		}
	}

	private boolean calculateFromBounds(MethodNode mth, SSAVar ssaVar) {
		TypeInfo typeInfo = ssaVar.getTypeInfo();
		Set<ITypeBound> bounds = typeInfo.getBounds();
		Optional<ArgType> bestTypeOpt = selectBestTypeFromBounds(bounds);
		if (bestTypeOpt.isEmpty()) {
			if (Consts.DEBUG_TYPE_INFERENCE) {
				LOG.warn("Failed to select best type from bounds, count={} : ", bounds.size());
				for (ITypeBound bound : bounds) {
					LOG.warn("  {}", bound);
				}
			}
			return false;
		}
		ArgType candidateType = bestTypeOpt.get();
		TypeUpdateResult result = typeUpdate.apply(mth, ssaVar, candidateType);
		if (result == TypeUpdateResult.REJECT) {
			if (Consts.DEBUG_TYPE_INFERENCE) {
				if (ssaVar.getTypeInfo().getType().equals(candidateType)) {
					LOG.info("Same type rejected: {} -> {}, bounds: {}", ssaVar, candidateType, bounds);
				} else if (candidateType.isTypeKnown()) {
					LOG.debug("Type rejected: {} -> {}, bounds: {}", ssaVar, candidateType, bounds);
				}
			}
			return false;
		}
		return result == TypeUpdateResult.CHANGED;
	}

	private Optional<ArgType> selectBestTypeFromBounds(Set<ITypeBound> bounds) {
		return bounds.stream()
				.map(ITypeBound::getType)
				.filter(Objects::nonNull)
				.max(typeUpdate.getTypeCompare().getComparator());
	}

	private boolean tryPossibleTypes(MethodNode mth, SSAVar var, ArgType type) {
		List<ArgType> types = makePossibleTypesList(type, var);
		if (types.isEmpty()) {
			return false;
		}
		for (ArgType candidateType : types) {
			TypeUpdateResult result = typeUpdate.apply(mth, var, candidateType);
			if (result == TypeUpdateResult.CHANGED) {
				return true;
			}
		}
		return false;
	}

	private List<ArgType> makePossibleTypesList(ArgType type, @Nullable SSAVar var) {
		if (type.isArray()) {
			List<ArgType> list = new ArrayList<>();
			for (ArgType arrElemType : makePossibleTypesList(type.getArrayElement(), null)) {
				list.add(ArgType.array(arrElemType));
			}
			return list;
		}
		if (var != null) {
			for (ITypeBound b : var.getTypeInfo().getBounds()) {
				ArgType boundType = b.getType();
				if (boundType.isObject() || boundType.isArray()) {
					// don't add primitive types
					return Collections.emptyList();
				}
			}
		}
		List<ArgType> list = new ArrayList<>();
		for (PrimitiveType possibleType : type.getPossibleTypes()) {
			if (possibleType == PrimitiveType.VOID) {
				continue;
			}
			list.add(ArgType.convertFromPrimitiveType(possibleType));
		}
		return list;
	}

	private boolean tryDeduceTypes(MethodNode mth) {
		boolean fixed = false;
		for (SSAVar ssaVar : mth.getSVars()) {
			if (deduceType(mth, ssaVar)) {
				fixed = true;
			}
		}
		return fixed;
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean deduceType(MethodNode mth, SSAVar var) {
		if (var.isTypeImmutable()) {
			return false;
		}
		ArgType type = var.getTypeInfo().getType();
		if (type.isTypeKnown()) {
			return false;
		}
		// try best type from bounds again
		if (setBestType(mth, var)) {
			return true;
		}
		// try all possible types (useful for primitives)
		if (tryPossibleTypes(mth, var, type)) {
			return true;
		}
		// for objects try super types
		if (tryWiderObjects(mth, var)) {
			return true;
		}
		return false;
	}

	private boolean tryRemoveGenerics(MethodNode mth) {
		boolean resolved = true;
		for (SSAVar var : mth.getSVars()) {
			ArgType type = var.getTypeInfo().getType();
			if (!type.isTypeKnown()
					&& !var.isTypeImmutable()
					&& !tryRawType(mth, var)) {
				resolved = false;
			}
		}
		return resolved;
	}

	private boolean tryRawType(MethodNode mth, SSAVar var) {
		Set<ArgType> objTypes = new LinkedHashSet<>();
		for (ITypeBound bound : var.getTypeInfo().getBounds()) {
			ArgType boundType = bound.getType();
			if (boundType.isTypeKnown() && boundType.isObject()) {
				objTypes.add(boundType);
			}
		}
		if (objTypes.isEmpty()) {
			return false;
		}
		for (ArgType objType : objTypes) {
			if (checkRawType(mth, var, objType)) {
				mth.addDebugComment("Type inference failed for " + var.toShortString() + "."
						+ " Raw type applied. Possible types: " + Utils.listToString(objTypes));
				return true;
			}
		}
		return false;
	}

	private boolean checkRawType(MethodNode mth, SSAVar var, ArgType objType) {
		if (objType.isObject() && objType.containsGeneric()) {
			ArgType rawType = objType.isGenericType() ? ArgType.OBJECT : ArgType.object(objType.getObject());
			TypeUpdateResult result = typeUpdate.applyWithWiderAllow(mth, var, rawType);
			return result == TypeUpdateResult.CHANGED;
		}
		return false;
	}

	/**
	 * Use type for var assigned from field (IGET or SGET).
	 * Insert additional casts at var use places.
	 */
	private Boolean applyFieldType(MethodNode mth) {
		try {
			if (trySplitSiblingStaticFieldPhi(mth) && checkTypes(mth)) {
				return true;
			}
			boolean changed = false;
			InsnNode firstCastFieldAssignment = null;
			Set<InsnNode> additionalCastFieldAssignments = null;
			// will add new SSA vars, can't use for-each loop
			List<SSAVar> sVars = mth.getSVars();
			for (int i = 0, varsCount = sVars.size(); i < varsCount; i++) {
				SSAVar ssaVar = sVars.get(i);
				if (tryFieldTypeWithNewCasts(mth, ssaVar, true)) {
					changed = true;
					InsnNode assignInsn = ssaVar.getAssignInsn();
					if (firstCastFieldAssignment == null) {
						firstCastFieldAssignment = assignInsn;
					} else if (assignInsn != firstCastFieldAssignment) {
						if (additionalCastFieldAssignments == null) {
							additionalCastFieldAssignments = new LinkedHashSet<>();
						}
						additionalCastFieldAssignments.add(assignInsn);
					}
				}
			}
			if (!changed) {
				return false;
			}
			// rerun full type inference
			InitCodeVariables.rerun(mth);
			typeInference.initTypeBounds(mth);
			typeInference.runTypePropagation(mth);

			// check if changed var types are fixed
			boolean forcedFieldType = false;
			boolean incomplete = false;
			for (SSAVar ssaVar : mth.getSVars()) {
				if (tryFieldTypeWithNewCasts(mth, ssaVar, false)) {
					forcedFieldType = true;
					// Ignore unrelated unknown field reads discovered only after another field inserted casts.
					// They still receive their declared field type below, but don't imply a missing cast.
					InsnNode assignInsn = ssaVar.getAssignInsn();
					boolean insertedCastForField = assignInsn == firstCastFieldAssignment
							|| additionalCastFieldAssignments != null && additionalCastFieldAssignments.contains(assignInsn);
					if (insertedCastForField
							&& !isOnlyCompatibleFieldTypeConflict(ssaVar)) {
						incomplete = true;
					}
				}
			}
			if (forcedFieldType) {
				typeInference.initTypeBounds(mth);
				typeInference.runTypePropagation(mth);
			}
			if (incomplete) {
				mth.addWarnComment("Type inference incomplete: some casts might be missing");
			}
			return !incomplete;
		} catch (Exception e) {
			mth.addWarnComment("Type inference fix 'apply assigned field type' failed", e);
			return false;
		}
	}

	/**
	 * Split a PHI before field fallback forces one concrete singleton subtype onto its sibling
	 * branches.
	 * Common for sealed state hierarchies: {@code condition ? new Loading() : Idle.INSTANCE}.
	 */
	private boolean trySplitSiblingStaticFieldPhi(MethodNode mth) {
		int insnsAdded = 0;
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiListAttr = block.get(AType.PHI_LIST);
			if (phiListAttr == null) {
				continue;
			}
			for (PhiInsn phiInsn : phiListAttr.getList()) {
				if (isSiblingStaticFieldPhi(phiInsn)) {
					insnsAdded += tryInsertAdditionalInsn(mth, phiInsn);
				}
			}
		}
		if (insnsAdded == 0) {
			return false;
		}
		InitCodeVariables.rerun(mth);
		typeInference.initTypeBounds(mth);
		return typeInference.runTypePropagation(mth);
	}

	private boolean isSiblingStaticFieldPhi(PhiInsn phiInsn) {
		boolean hasStaticField = false;
		List<ArgType> types = new ArrayList<>(phiInsn.getArgsCount());
		for (InsnArg arg : phiInsn.getArguments()) {
			if (!arg.isRegister()) {
				return false;
			}
			SSAVar var = ((RegisterArg) arg).getSVar();
			if (var == null) {
				return false;
			}
			InsnNode assignInsn = var.getAssignInsn();
			hasStaticField |= assignInsn != null && assignInsn.getType() == InsnType.SGET;
			ArgType type = getKnownMoveSourceType((RegisterArg) arg);
			if (type == null || !type.isTypeKnown() || !type.isObject() || type.containsGeneric()) {
				return false;
			}
			types.add(type);
		}
		if (!hasStaticField) {
			return false;
		}
		for (int i = 1; i < types.size(); i++) {
			if (typeUpdate.getTypeCompare().compareTypes(types.get(0), types.get(i)) == TypeCompareEnum.CONFLICT) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static ArgType getKnownMoveSourceType(RegisterArg arg) {
		RegisterArg currentArg = arg;
		for (int i = 0; i < 10; i++) {
			SSAVar var = currentArg.getSVar();
			if (var == null) {
				return null;
			}
			ArgType type = currentArg.getType();
			if (type.isTypeKnown()) {
				return type;
			}
			type = var.getImmutableType();
			if (type != null && type.isTypeKnown()) {
				return type;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (assignInsn == null) {
				return null;
			}
			if (assignInsn.getResult() != null) {
				type = assignInsn.getResult().getInitType();
				if (type.isTypeKnown()) {
					return type;
				}
			}
			if (assignInsn.getType() != InsnType.MOVE
					|| assignInsn.getArgsCount() != 1
					|| !assignInsn.getArg(0).isRegister()) {
				return null;
			}
			currentArg = (RegisterArg) assignInsn.getArg(0);
		}
		return null;
	}

	private boolean tryFieldTypeWithNewCasts(MethodNode mth, SSAVar ssaVar, boolean insertCasts) {
		ArgType type = ssaVar.getTypeInfo().getType();
		if (type.isTypeKnown() || ssaVar.isTypeImmutable()) {
			return false;
		}
		InsnNode assignInsn = ssaVar.getAssignInsn();
		if (assignInsn == null) {
			return false;
		}
		InsnType insnType = assignInsn.getType();
		if (insnType != InsnType.IGET && insnType != InsnType.SGET) {
			return false;
		}
		ArgType fieldType = getFieldType(ssaVar, assignInsn.getResult().getInitType());
		// field type should be used
		if (insertCasts) {
			// try to find a use place and insert cast
			boolean inserted = false;
			for (RegisterArg useArg : List.copyOf(ssaVar.getUseList())) {
				if (insertExplicitUseCast(mth, ssaVar, useArg, fieldType)) {
					inserted = true;
				}
			}
			return inserted;
		}
		ArgType rawType = getWildcardCaptureRawType(ssaVar, fieldType);
		if (rawType != null) {
			// Java can't pass Object values back into an unbounded wildcard capture.
			// Use the raw local type so linked generic calls remain source-compilable.
			ssaVar.setType(rawType);
		} else {
			// force field type, will make type inference incomplete,
			// but it is better that completely unknown type
			ssaVar.setType(fieldType);
		}
		return true;
	}

	private boolean isOnlyCompatibleFieldTypeConflict(SSAVar ssaVar) {
		InsnNode assignInsn = ssaVar.getAssignInsn();
		if (assignInsn == null || assignInsn.getResult() == null) {
			return false;
		}
		ArgType fieldType = getFieldType(ssaVar, assignInsn.getResult().getInitType());
		if (!fieldType.isObject() || !fieldType.containsGeneric() || fieldType.isWildcard()) {
			return false;
		}
		if (getWildcardCaptureRawType(ssaVar, fieldType) != null) {
			return true;
		}
		boolean compatibleBoundFound = false;
		for (ITypeBound bound : ssaVar.getTypeInfo().getBounds()) {
			ArgType boundType = bound.getType();
			if (!boundType.isTypeKnown()) {
				if (isCompatibleUnknownObjectUse(bound)) {
					compatibleBoundFound = true;
					continue;
				}
				return false;
			}
			if (!boundType.isObject()
					|| boundType.isWildcard()) {
				return false;
			}
			TypeCompareEnum compare = typeUpdate.getTypeCompare().compareTypes(fieldType, boundType);
			if (!compare.isNarrowOrEqual()) {
				return false;
			}
			if (!compare.isEqual()) {
				compatibleBoundFound = true;
			}
		}
		return compatibleBoundFound;
	}

	@Nullable
	private static ArgType getWildcardCaptureRawType(SSAVar ssaVar, ArgType fieldType) {
		List<ArgType> fieldGenerics = fieldType.getGenericTypes();
		if (fieldGenerics == null || fieldGenerics.isEmpty()) {
			return null;
		}
		for (ArgType generic : fieldGenerics) {
			if (!generic.isWildcard() || generic.getWildcardBound() != ArgType.WildcardBound.UNBOUND) {
				return null;
			}
		}
		boolean captureBoundFound = false;
		for (ITypeBound bound : ssaVar.getTypeInfo().getBounds()) {
			ArgType boundType = bound.getType();
			if (!boundType.isTypeKnown() || !boundType.isObject()) {
				return null;
			}
			if (boundType.equals(fieldType) || boundType.equals(ArgType.OBJECT)) {
				continue;
			}
			if (!boundType.getObject().equals(fieldType.getObject())) {
				return null;
			}
			if (!boundType.containsGeneric()) {
				continue;
			}
			if (!(bound instanceof TypeBoundInvokeUse)
					|| !isUnboundedWildcardCapture(fieldType, boundType)) {
				return null;
			}
			captureBoundFound = true;
		}
		return captureBoundFound ? ArgType.object(fieldType.getObject()) : null;
	}

	static boolean isUnboundedWildcardCapture(ArgType fieldType, ArgType useType) {
		if (!fieldType.isObject() || !useType.isObject()
				|| !fieldType.getObject().equals(useType.getObject())) {
			return false;
		}
		List<ArgType> fieldGenerics = fieldType.getGenericTypes();
		List<ArgType> useGenerics = useType.getGenericTypes();
		if (fieldGenerics == null || useGenerics == null
				|| fieldGenerics.isEmpty() || fieldGenerics.size() != useGenerics.size()) {
			return false;
		}
		for (int i = 0; i < fieldGenerics.size(); i++) {
			ArgType fieldGeneric = fieldGenerics.get(i);
			if (!fieldGeneric.isWildcard()
					|| fieldGeneric.getWildcardBound() != ArgType.WildcardBound.UNBOUND
					|| !useGenerics.get(i).isGenericType()) {
				return false;
			}
		}
		return true;
	}

	static boolean isCompatibleUnknownObjectUse(ITypeBound bound) {
		ArgType boundType = bound.getType();
		if (bound.getBound() != BoundEnum.USE
				|| boundType.isTypeKnown()
				|| !boundType.canBeObject()) {
			return false;
		}
		if (!boundType.canBeAnyNumber()) {
			return true;
		}
		RegisterArg arg = bound.getArg();
		if (arg == null || !(arg.getParentInsn() instanceof IfNode)) {
			return false;
		}
		IfNode ifInsn = (IfNode) arg.getParentInsn();
		IfOp op = ifInsn.getOp();
		if (op != IfOp.EQ && op != IfOp.NE) {
			return false;
		}
		InsnArg firstArg = ifInsn.getArg(0);
		InsnArg secondArg = ifInsn.getArg(1);
		return firstArg == arg && secondArg.isZeroLiteral()
				|| secondArg == arg && firstArg.isZeroLiteral();
	}

	private static ArgType getFieldType(SSAVar ssaVar, ArgType initType) {
		for (ITypeBound bound : ssaVar.getTypeInfo().getBounds()) {
			if (bound instanceof TypeBoundFieldGetAssign) {
				ArgType resolvedType = ((TypeBoundFieldGetAssign) bound).getTypeForFallback();
				if (resolvedType.isTypeKnown() && !resolvedType.isWildcard()) {
					return resolvedType;
				}
			}
		}
		return initType;
	}

	private boolean insertExplicitUseCast(MethodNode mth, SSAVar ssaVar, RegisterArg useArg, ArgType fieldType) {
		InsnNode parentInsn = useArg.getParentInsn();
		if (!InsnUtils.isInsnType(parentInsn, InsnType.INVOKE)) {
			return false;
		}
		InvokeNode invoke = (InvokeNode) parentInsn;
		InsnArg instanceArg = invoke.getInstanceArg();
		if (instanceArg == null || !instanceArg.isSameVar(ssaVar)) {
			return false;
		}
		IMethodDetails details = mth.root().getMethodUtils().getMethodDetails(invoke);
		if (details == null) {
			return false;
		}
		int newCasts = 0;
		int k = -1;
		for (InsnArg invArg : invoke.getArgList()) {
			if (invArg == instanceArg) {
				continue;
			}
			k++;
			if (!invArg.isRegister()) {
				continue;
			}
			ArgType detailsArg = details.getArgTypes().get(k);
			ArgType invArgType = invArg.getType();
			ArgType resolvedType = mth.root().getTypeUtils().replaceClassGenerics(fieldType, invArgType, detailsArg);
			if (resolvedType != null && !resolvedType.equals(invArgType)) {
				IndexInsnNode castInsn = insertUseCast(mth, (RegisterArg) invArg, resolvedType);
				if (castInsn != null) {
					castInsn.add(AFlag.EXPLICIT_CAST);
					newCasts++;
				}
			}
		}
		return newCasts > 0;
	}

	/**
	 * Fix check casts to type var extend type:
	 * <br>
	 * {@code <T extends Comparable> T var = (Comparable) obj; => T var = (T) obj; }
	 */
	private boolean tryRestoreTypeVarCasts(MethodNode mth) {
		int changed = 0;
		List<SSAVar> mthSVars = mth.getSVars();
		for (SSAVar var : mthSVars) {
			changed += restoreTypeVarCasts(var);
		}
		if (changed == 0) {
			return false;
		}
		if (Consts.DEBUG_TYPE_INFERENCE) {
			mth.addDebugComment("Restore " + changed + " type vars casts");
		}
		typeInference.initTypeBounds(mth);
		return typeInference.runTypePropagation(mth);
	}

	private int restoreTypeVarCasts(SSAVar var) {
		TypeInfo typeInfo = var.getTypeInfo();
		Set<ITypeBound> bounds = typeInfo.getBounds();
		if (!ListUtils.anyMatch(bounds, t -> t.getType().isGenericType())) {
			return 0;
		}
		List<ITypeBound> casts = ListUtils.filter(bounds, TypeBoundCheckCastAssign.class::isInstance);
		if (casts.isEmpty()) {
			return 0;
		}
		ArgType bestType = selectBestTypeFromBounds(bounds).orElse(ArgType.UNKNOWN);
		if (!bestType.isGenericType()) {
			return 0;
		}
		List<ArgType> extendTypes = bestType.getExtendTypes();
		if (extendTypes.size() != 1) {
			return 0;
		}
		int fixed = 0;
		ArgType extendType = extendTypes.get(0);
		for (ITypeBound bound : casts) {
			TypeBoundCheckCastAssign cast = (TypeBoundCheckCastAssign) bound;
			ArgType castType = cast.getType();
			TypeCompareEnum result = typeUpdate.getTypeCompare().compareTypes(extendType, castType);
			if (result.isEqual() || result == TypeCompareEnum.NARROW_BY_GENERIC) {
				cast.getInsn().updateIndex(bestType);
				fixed++;
			}
		}
		return fixed;
	}

	/**
	 * Restore a concrete PHI type hidden behind move chains when the merged value is used through
	 * several sibling interfaces. This is common in generated Compose view-model lookup code:
	 * a checked {@code NavBackStackEntry} is merged with a null carried by a register which was
	 * assigned another interface type on an unrelated path.
	 */
	private boolean tryRestorePhiConcreteType(MethodNode mth) {
		boolean fixed = false;
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			if (var.isTypeImmutable() || var.getTypeInfo().getType().isTypeKnown()) {
				continue;
			}
			InsnNode assignInsn = var.getAssignInsn();
			if (!(assignInsn instanceof PhiInsn)) {
				continue;
			}
			PhiInsn phiInsn = (PhiInsn) assignInsn;
			Set<ArgType> useTypes = collectConflictingInterfaceUseTypes(mth, var);
			if (useTypes.size() < 2) {
				continue;
			}
			ArgType concreteType = findPhiConcreteType(mth, phiInsn, useTypes);
			if (concreteType == null) {
				continue;
			}
			var.markAsImmutable(concreteType);
			var.setType(concreteType);
			fixed = true;
		}
		return fixed;
	}

	private Set<ArgType> collectConflictingInterfaceUseTypes(MethodNode mth, SSAVar var) {
		Set<ArgType> useTypes = new LinkedHashSet<>();
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null || useInsn.getType() == InsnType.PHI
					|| useInsn.getType() == InsnType.IF && useInsn.getArg(1).isZeroConst()) {
				continue;
			}
			ArgType useType = useArg.getInitType();
			if (!isKnownInterface(mth, useType)) {
				continue;
			}
			useTypes.add(useType);
		}
		if (useTypes.size() < 2) {
			return Collections.emptySet();
		}
		List<ArgType> list = new ArrayList<>(useTypes);
		for (int i = 0; i < list.size(); i++) {
			for (int j = i + 1; j < list.size(); j++) {
				if (typeUpdate.getTypeCompare().compareTypes(list.get(i), list.get(j)) != TypeCompareEnum.CONFLICT) {
					return Collections.emptySet();
				}
			}
		}
		return useTypes;
	}

	private static boolean isKnownInterface(MethodNode mth, ArgType type) {
		if (!type.isTypeKnown() || !type.isObject() || type.containsGeneric()) {
			return false;
		}
		ClspClass cls = mth.root().getClsp().getClsDetails(type);
		return cls != null && cls.isInterface();
	}

	@Nullable
	private static ArgType findPhiConcreteType(MethodNode mth, PhiInsn phiInsn, Set<ArgType> useTypes) {
		ClspGraph clsp = mth.root().getClsp();
		for (InsnArg arg : phiInsn.getArguments()) {
			if (!arg.isRegister()) {
				continue;
			}
			ArgType type = getKnownMoveSourceType((RegisterArg) arg);
			if (type == null || !type.isObject() || type.containsGeneric()) {
				continue;
			}
			ClspClass cls = clsp.getClsDetails(type);
			if (cls == null || cls.isInterface()) {
				continue;
			}
			boolean implementsAll = true;
			for (ArgType useType : useTypes) {
				if (!type.equals(useType) && !clsp.isImplements(type.getObject(), useType.getObject())) {
					implementsAll = false;
					break;
				}
			}
			if (implementsAll) {
				return type;
			}
		}
		return null;
	}

	@SuppressWarnings({ "ForLoopReplaceableByWhile", "ForLoopReplaceableByForEach" })
	private boolean tryInsertCasts(MethodNode mth) {
		int added = 0;
		List<SSAVar> mthSVars = mth.getSVars();
		int varsCount = mthSVars.size();
		for (int i = 0; i < varsCount; i++) {
			SSAVar var = mthSVars.get(i);
			ArgType type = var.getTypeInfo().getType();
			if (!type.isTypeKnown() && !var.isTypeImmutable()) {
				added += tryInsertVarCast(mth, var);
			}
		}
		if (added != 0) {
			InitCodeVariables.rerun(mth);
			typeInference.initTypeBounds(mth);
			return typeInference.runTypePropagation(mth);
		}
		return false;
	}

	private int tryInsertVarCast(MethodNode mth, SSAVar var) {
		for (ITypeBound bound : var.getTypeInfo().getBounds()) {
			ArgType boundType = bound.getType();
			if (boundType.isTypeKnown()
					&& !boundType.equals(var.getTypeInfo().getType())
					&& boundType.containsTypeVariable()
					&& !mth.root().getTypeUtils().containsUnknownTypeVar(mth, boundType)) {
				IndexInsnNode castInsn = insertAssignCast(mth, var, boundType);
				if (castInsn != null) {
					castInsn.add(AFlag.SOFT_CAST);
					return 1;
				}
				return insertUseCasts(mth, var);
			}
		}
		return 0;
	}

	private int insertUseCasts(MethodNode mth, SSAVar var) {
		List<RegisterArg> useList = var.getUseList();
		if (useList.isEmpty()) {
			return 0;
		}
		int useCasts = 0;
		for (RegisterArg useReg : new ArrayList<>(useList)) {
			IndexInsnNode castInsn = insertUseCast(mth, useReg, useReg.getInitType());
			if (castInsn != null) {
				castInsn.add(AFlag.SOFT_CAST);
				useCasts++;
			}
		}
		return useCasts;
	}

	private @Nullable IndexInsnNode insertAssignCast(MethodNode mth, SSAVar var, ArgType castType) {
		RegisterArg assignArg = var.getAssign();
		InsnNode assignInsn = assignArg.getParentInsn();
		if (assignInsn == null || assignInsn.getType() == InsnType.PHI) {
			return null;
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		if (assignBlock == null) {
			return null;
		}
		assignInsn.setResult(assignArg.duplicateWithNewSSAVar(mth));
		IndexInsnNode castInsn = makeCastInsn(assignArg.duplicate(), assignInsn.getResult().duplicate(), castType);
		if (!BlockUtils.insertAfterInsn(assignBlock, assignInsn, castInsn)) {
			return null;
		}
		return castInsn;
	}

	private @Nullable IndexInsnNode insertUseCast(MethodNode mth, RegisterArg useArg, ArgType castType) {
		InsnNode useInsn = useArg.getParentInsn();
		if (useInsn == null || useInsn.getType() == InsnType.PHI) {
			return null;
		}
		if (useInsn.getType() == InsnType.IF && useInsn.getArg(1).isZeroConst()) {
			// cast isn't needed if compare with null
			return null;
		}
		BlockNode useBlock = BlockUtils.getBlockByInsn(mth, useInsn);
		if (useBlock == null) {
			return null;
		}
		IndexInsnNode castInsn = makeCastInsn(
				useArg.duplicateWithNewSSAVar(mth),
				useArg.duplicate(),
				castType);
		useInsn.replaceArg(useArg, castInsn.getResult().duplicate());
		boolean inserted = BlockUtils.insertBeforeInsn(useBlock, useInsn, castInsn);
		if (!inserted) {
			return null;
		}
		if (Consts.DEBUG_TYPE_INFERENCE) {
			LOG.info("Insert cast for {} before {} in {}", useArg, useInsn, useBlock);
		}
		return castInsn;
	}

	private IndexInsnNode makeCastInsn(RegisterArg result, RegisterArg arg, ArgType castType) {
		IndexInsnNode castInsn = new IndexInsnNode(InsnType.CHECK_CAST, castType, 1);
		castInsn.setResult(result);
		castInsn.addArg(arg);
		castInsn.add(AFlag.SYNTHETIC);
		return castInsn;
	}

	private boolean trySplitConstInsns(MethodNode mth) {
		boolean constSplit = false;
		for (SSAVar var : new ArrayList<>(mth.getSVars())) {
			if (checkAndSplitConstInsn(mth, var)) {
				constSplit = true;
			}
		}
		if (!constSplit) {
			return false;
		}
		InitCodeVariables.rerun(mth);
		typeInference.initTypeBounds(mth);
		return typeInference.runTypePropagation(mth);
	}

	private boolean checkAndSplitConstInsn(MethodNode mth, SSAVar var) {
		ArgType type = var.getTypeInfo().getType();
		if (type.isTypeKnown() || var.isTypeImmutable()) {
			return false;
		}
		return splitByPhi(mth, var) || dupConst(mth, var);
	}

	private boolean dupConst(MethodNode mth, SSAVar var) {
		InsnNode assignInsn = var.getAssign().getAssignInsn();
		if (!InsnUtils.isInsnType(assignInsn, InsnType.CONST)) {
			return false;
		}
		if (var.getUseList().size() < 2) {
			return false;
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		if (assignBlock == null) {
			return false;
		}
		assignInsn.remove(AFlag.DONT_INLINE);
		int insertIndex = 1 + BlockUtils.getInsnIndexInBlock(assignBlock, assignInsn);
		List<RegisterArg> useList = new ArrayList<>(var.getUseList());
		for (int i = 0, useCount = useList.size(); i < useCount; i++) {
			RegisterArg useArg = useList.get(i);
			useArg.remove(AFlag.DONT_INLINE_CONST);
			if (i == 0) {
				continue;
			}
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null) {
				continue;
			}
			InsnNode newInsn = assignInsn.copyWithNewSsaVar(mth);
			assignBlock.getInstructions().add(insertIndex, newInsn);
			useInsn.replaceArg(useArg, newInsn.getResult().duplicate());
		}
		if (Consts.DEBUG_TYPE_INFERENCE) {
			LOG.debug("Duplicate const insn {} times: {} in {}", useList.size(), assignInsn, assignBlock);
		}
		return true;
	}

	/**
	 * For every PHI make separate CONST insn
	 */
	private static boolean splitByPhi(MethodNode mth, SSAVar var) {
		if (var.getUsedInPhi().size() < 2) {
			return false;
		}
		InsnNode assignInsn = var.getAssign().getAssignInsn();
		InsnNode constInsn = InsnUtils.checkInsnType(assignInsn, InsnType.CONST);
		if (constInsn == null) {
			return false;
		}
		BlockNode blockNode = BlockUtils.getBlockByInsn(mth, constInsn);
		if (blockNode == null) {
			return false;
		}
		boolean first = true;
		for (PhiInsn phiInsn : var.getUsedInPhi()) {
			if (first) {
				first = false;
				continue;
			}
			InsnNode copyInsn = constInsn.copyWithNewSsaVar(mth);
			copyInsn.add(AFlag.SYNTHETIC);
			BlockUtils.insertAfterInsn(blockNode, constInsn, copyInsn);

			RegisterArg phiArg = phiInsn.getArgBySsaVar(var);
			phiInsn.replaceArg(phiArg, copyInsn.getResult().duplicate());
		}
		return true;
	}

	private boolean tryInsertAdditionalMove(MethodNode mth) {
		int insnsAdded = 0;
		// In large methods and coroutine state machines, splitting every exception PHI can
		// perturb unrelated type groups. Keep this fallback to small resource-cleanup methods.
		boolean allowExceptionMove = mth.getBasicBlocks().size() <= EXCEPTION_MOVE_MAX_BLOCKS
				&& !mth.getName().equals("invokeSuspend")
				&& hasCloseablePhi(mth);
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiListAttr = block.get(AType.PHI_LIST);
			if (phiListAttr != null) {
				for (PhiInsn phiInsn : phiListAttr.getList()) {
					insnsAdded += tryInsertAdditionalInsn(mth, phiInsn, allowExceptionMove);
				}
			}
		}
		if (insnsAdded == 0) {
			return false;
		}
		if (Consts.DEBUG_TYPE_INFERENCE) {
			mth.addDebugComment("Additional " + insnsAdded + " move instructions added to help type inference");
		}
		InitCodeVariables.rerun(mth);
		typeInference.initTypeBounds(mth);
		if (typeInference.runTypePropagation(mth) && checkTypes(mth)) {
			return true;
		}
		return tryDeduceTypes(mth);
	}

	/**
	 * Add MOVE instruction before PHI in bound blocks to make 'soft' type link.
	 * This allows using different types in blocks merged by PHI.
	 */
	private int tryInsertAdditionalInsn(MethodNode mth, PhiInsn phiInsn) {
		return tryInsertAdditionalInsn(mth, phiInsn, false);
	}

	private int tryInsertAdditionalInsn(MethodNode mth, PhiInsn phiInsn, boolean allowExceptionMove) {
		ArgType phiType = getCommonTypeForPhiArgs(phiInsn);
		if (phiType != null && phiType.isTypeKnown()) {
			// all args have the same known type => nothing to do here
			return 0;
		}
		// check if instructions can be inserted
		if (insertMovesForPhi(mth, phiInsn, false, allowExceptionMove) == 0) {
			return 0;
		}
		// check passed => apply
		return insertMovesForPhi(mth, phiInsn, true, allowExceptionMove);
	}

	@Nullable
	private ArgType getCommonTypeForPhiArgs(PhiInsn phiInsn) {
		ArgType phiArgType = null;
		for (InsnArg arg : phiInsn.getArguments()) {
			ArgType type = arg.getType();
			if (phiArgType == null) {
				phiArgType = type;
			} else if (!phiArgType.equals(type)) {
				return null;
			}
		}
		return phiArgType;
	}

	private int insertMovesForPhi(MethodNode mth, PhiInsn phiInsn, boolean apply, boolean allowExceptionMove) {
		int argsCount = phiInsn.getArgsCount();
		int count = 0;
		// An empty 1-in/1-out synthetic block is an edge split, so a MOVE placed there
		// still executes on exactly one PHI input. Loop exits commonly use such blocks.
		boolean allowSynthetic = hasGenericTypeBound(phiInsn) || hasSimpleSyntheticEdge(phiInsn);
		for (int argIndex = 0; argIndex < argsCount; argIndex++) {
			RegisterArg reg = phiInsn.getArg(argIndex);
			BlockNode startBlock = phiInsn.getBlockByArgIndex(argIndex);
			BlockNode blockNode = checkBlockForInsnInsert(startBlock, allowSynthetic, allowExceptionMove);
			if (blockNode == null
					|| blockNode != startBlock
							&& allowExceptionMove
							&& isExceptionSplitter(startBlock)
							&& !isMoveSourceAvailableAt(mth, reg, blockNode)) {
				mth.addDebugComment("Failed to insert an additional move for type inference into block " + startBlock);
				return 0;
			}
			boolean add = true;
			SSAVar var = reg.getSVar();
			InsnNode assignInsn = var.getAssign().getAssignInsn();
			if (assignInsn != null) {
				InsnType assignType = assignInsn.getType();
				if (assignType == InsnType.CONST
						|| (assignType == InsnType.MOVE && var.getUseCount() == 1)) {
					add = false;
				}
			}
			if (add) {
				count++;
				if (apply) {
					insertMove(mth, blockNode, phiInsn, reg);
				}
			}
		}
		return count;
	}

	private static boolean hasSimpleSyntheticEdge(PhiInsn phiInsn) {
		for (int i = 0; i < phiInsn.getArgsCount(); i++) {
			BlockNode block = phiInsn.getBlockByArgIndex(i);
			if (block.isSynthetic()
					&& block.isEmpty()
					&& block.getPredecessors().size() == 1
					&& block.getSuccessors().size() == 1) {
				return true;
			}
		}
		return false;
	}

	private static boolean isExceptionSplitter(BlockNode block) {
		return block.contains(AFlag.EXC_TOP_SPLITTER)
				|| block.contains(AFlag.EXC_BOTTOM_SPLITTER);
	}

	private static boolean isMoveSourceAvailableAt(MethodNode mth, RegisterArg reg, BlockNode block) {
		InsnNode assignInsn = reg.getSVar().getAssignInsn();
		if (assignInsn == null) {
			// method arguments and exception-state vars have no concrete assignment instruction
			return true;
		}
		BlockNode assignBlock = BlockUtils.getBlockByInsn(mth, assignInsn);
		return assignBlock != null
				&& (assignBlock == block || block.isDominator(assignBlock));
	}

	private static boolean hasGenericTypeBound(PhiInsn phiInsn) {
		for (InsnArg arg : phiInsn.getArguments()) {
			if (arg.isRegister()) {
				SSAVar var = ((RegisterArg) arg).getSVar();
				if (var != null && var.getTypeInfo().getBounds().stream()
						.map(ITypeBound::getType)
						.map(type -> type.isWildcard() ? type.getWildcardType() : type)
						.anyMatch(type -> type != null && type.isGenericType())) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasCloseablePhi(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiListAttr = block.get(AType.PHI_LIST);
			if (phiListAttr != null) {
				for (PhiInsn phiInsn : phiListAttr.getList()) {
					if (hasCloseableTypeBound(phiInsn)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasCloseableTypeBound(PhiInsn phiInsn) {
		RegisterArg result = phiInsn.getResult();
		if (result != null && hasCloseableTypeBound(result.getSVar())) {
			return true;
		}
		for (InsnArg arg : phiInsn.getArguments()) {
			if (arg.isRegister() && hasCloseableTypeBound(((RegisterArg) arg).getSVar())) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasCloseableTypeBound(@Nullable SSAVar var) {
		return var != null && var.getTypeInfo().getBounds().stream()
				.map(ITypeBound::getType)
				.anyMatch(type -> type.isObject() && "java.io.Closeable".equals(type.getObject()));
	}

	private void insertMove(MethodNode mth, BlockNode blockNode, PhiInsn phiInsn, RegisterArg reg) {
		SSAVar var = reg.getSVar();
		int regNum = reg.getRegNum();
		RegisterArg resultArg = reg.duplicate(regNum, null);
		SSAVar newSsaVar = mth.makeNewSVar(resultArg);
		RegisterArg arg = reg.duplicate(regNum, var);

		InsnNode moveInsn = new InsnNode(InsnType.MOVE, 1);
		moveInsn.setResult(resultArg);
		moveInsn.addArg(arg);
		moveInsn.add(AFlag.SYNTHETIC);
		blockNode.getInstructions().add(moveInsn);

		phiInsn.replaceArg(reg, reg.duplicate(regNum, newSsaVar));
	}

	@Nullable
	private BlockNode checkBlockForInsnInsert(BlockNode blockNode, boolean allowSynthetic, boolean allowExceptionMove) {
		if (blockNode.isSynthetic()) {
			if (allowExceptionMove
					&& blockNode.isEmpty()
					&& (blockNode.contains(AFlag.EXC_TOP_SPLITTER)
							|| blockNode.contains(AFlag.EXC_BOTTOM_SPLITTER))) {
				List<BlockNode> preds = blockNode.getPredecessors();
				if (preds.size() == 1) {
					// Splitters can have several exception successors. Insert before one only
					// when the source SSA value is available there (checked by the caller).
					return checkBlockForInsnInsert(preds.get(0), allowSynthetic, true);
				}
			}
			if (allowExceptionMove
					&& blockNode.isEmpty()
					&& blockNode.contains(AType.EXC_HANDLER)
					&& blockNode.getSuccessors().size() == 1) {
				// A synthetic handler merge has one unambiguous outgoing value.
				return blockNode;
			}
			if (allowSynthetic
					&& blockNode.isEmpty()
					&& blockNode.getPredecessors().size() == 1
					&& blockNode.getSuccessors().size() == 1) {
				return blockNode;
			}
			return null;
		}
		InsnNode lastInsn = BlockUtils.getLastInsn(blockNode);
		if (lastInsn != null && BlockSplitter.isSeparate(lastInsn.getType())) {
			if (allowExceptionMove
					&& lastInsn.getType() == InsnType.MOVE_EXCEPTION
					&& blockNode.getSuccessors().size() == 1) {
				// MOVE_EXCEPTION must stay first, but a synthetic move can safely follow it
				// when the handler has a single normal continuation.
				return blockNode;
			}
			// can't insert move in a block with 'separate' instruction => try previous block by simple path
			List<BlockNode> preds = blockNode.getPredecessors();
			if (preds.size() == 1) {
				return checkBlockForInsnInsert(preds.get(0), allowSynthetic, allowExceptionMove);
			}
			return null;
		}
		return blockNode;
	}

	private boolean tryWiderObjects(MethodNode mth, SSAVar var) {
		Set<ArgType> objTypes = new LinkedHashSet<>();
		for (ITypeBound bound : var.getTypeInfo().getBounds()) {
			ArgType boundType = bound.getType();
			if (boundType.isTypeKnown() && boundType.isObject()) {
				objTypes.add(boundType);
			}
		}
		if (objTypes.isEmpty()) {
			return false;
		}
		ClspGraph clsp = mth.root().getClsp();
		for (ArgType objType : objTypes) {
			for (String ancestor : clsp.getSuperTypes(objType.getObject())) {
				ArgType ancestorType = ArgType.object(ancestor);
				TypeUpdateResult result = typeUpdate.applyWithWiderAllow(mth, var, ancestorType);
				if (result == TypeUpdateResult.CHANGED) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("ForLoopReplaceableByForEach")
	private boolean tryToFixIncompatiblePrimitives(MethodNode mth) {
		boolean fixed = false;
		List<SSAVar> ssaVars = mth.getSVars();
		int ssaVarsCount = ssaVars.size();
		// new vars will be added at a list end if fix is applied (can't use for-each loop here)
		for (int i = 0; i < ssaVarsCount; i++) {
			if (processIncompatiblePrimitives(mth, ssaVars.get(i))) {
				fixed = true;
			}
		}
		if (!fixed) {
			return false;
		}
		InitCodeVariables.rerun(mth);
		typeInference.initTypeBounds(mth);
		return typeInference.runTypePropagation(mth);
	}

	private boolean processIncompatiblePrimitives(MethodNode mth, SSAVar var) {
		TypeInfo typeInfo = var.getTypeInfo();
		if (typeInfo.getType().isTypeKnown()) {
			return false;
		}
		if (splitIntBooleanPhiInputs(mth, var)) {
			return true;
		}
		if (splitIntBooleanPhiUses(mth, var)) {
			return true;
		}
		boolean assigned = false;
		for (ITypeBound bound : typeInfo.getBounds()) {
			ArgType boundType = bound.getType();
			switch (bound.getBound()) {
				case ASSIGN:
					if (!boundType.contains(PrimitiveType.BOOLEAN)) {
						return false;
					}
					assigned = true;
					break;
				case USE:
					if (!boundType.canBeAnyNumber()) {
						return false;
					}
					break;
			}
		}
		if (!assigned) {
			return false;
		}

		boolean fixed = false;
		for (RegisterArg arg : new ArrayList<>(var.getUseList())) {
			if (fixBooleanUsage(mth, arg)) {
				fixed = true;
				if (Consts.DEBUG_TYPE_INFERENCE) {
					LOG.info("Fixed boolean usage for arg {} from {}", arg, arg.getParentInsn());
				}
			}
		}
		return fixed;
	}

	/**
	 * Convert an int input of a boolean control-flow PHI on its incoming edge.
	 * DEX commonly reuses an int zero local as the null/default branch of a boolean expression.
	 */
	private boolean splitIntBooleanPhiInputs(MethodNode mth, SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (!(assignInsn instanceof PhiInsn)) {
			return false;
		}
		PhiInsn phiInsn = (PhiInsn) assignInsn;
		List<RegisterArg> intArgs = new ArrayList<>();
		boolean booleanArgFound = false;
		for (InsnArg insnArg : phiInsn.getArguments()) {
			RegisterArg arg = (RegisterArg) insnArg;
			ArgType type = getKnownMoveSourceType(arg);
			if (ArgType.BOOLEAN.equals(type)) {
				booleanArgFound = true;
			} else if (ArgType.INT.equals(type)) {
				intArgs.add(arg);
			} else {
				return false;
			}
		}
		Set<SSAVar> booleanFlowVars = new LinkedHashSet<>();
		if (!booleanArgFound || intArgs.isEmpty()
				|| !isBooleanControlFlowVar(var, new LinkedHashSet<>(), booleanFlowVars)) {
			return false;
		}
		List<BlockNode> insertBlocks = new ArrayList<>(intArgs.size());
		for (RegisterArg intArg : intArgs) {
			BlockNode startBlock = phiInsn.getBlockByArg(intArg);
			if (startBlock == null) {
				return false;
			}
			BlockNode insertBlock = checkBlockForInsnInsert(startBlock, true, false);
			if (insertBlock == null || !isMoveSourceAvailableAt(mth, intArg, insertBlock)) {
				return false;
			}
			insertBlocks.add(insertBlock);
		}
		for (int i = 0; i < intArgs.size(); i++) {
			insertIntToBooleanPhiConversion(mth, phiInsn, intArgs.get(i), insertBlocks.get(i));
		}
		for (SSAVar booleanVar : booleanFlowVars) {
			booleanVar.markAsImmutable(ArgType.BOOLEAN);
			booleanVar.setType(ArgType.BOOLEAN);
			booleanVar.getAssign().forceSetInitType(ArgType.BOOLEAN);
			for (RegisterArg use : booleanVar.getUseList()) {
				use.forceSetInitType(ArgType.BOOLEAN);
				InsnNode useInsn = use.getParentInsn();
				if (useInsn instanceof IfNode) {
					for (InsnArg arg : useInsn.getArguments()) {
						if (arg.isZeroLiteral()) {
							arg.setType(ArgType.BOOLEAN);
						}
					}
				}
			}
		}
		return true;
	}

	private boolean isBooleanControlFlowVar(SSAVar var, Set<SSAVar> visited, Set<SSAVar> booleanFlowVars) {
		if (!visited.add(var) || var.getUseList().isEmpty()) {
			return false;
		}
		booleanFlowVars.add(var);
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null) {
				return false;
			}
			if (useInsn instanceof IfNode && isIntZeroComparison(useArg)) {
				continue;
			}
			if (!(useInsn instanceof ArithNode) || !((ArithNode) useInsn).getOp().isBitOp()) {
				return false;
			}
			RegisterArg result = useInsn.getResult();
			if (result == null || result.getSVar() == null
					|| !isBooleanControlFlowVar(result.getSVar(), visited, booleanFlowVars)) {
				return false;
			}
		}
		return true;
	}

	private void insertIntToBooleanPhiConversion(MethodNode mth, PhiInsn phiInsn, RegisterArg intArg, BlockNode block) {
		RegisterArg resultArg = intArg.duplicateWithNewSSAVar(mth);
		resultArg.forceSetInitType(ArgType.BOOLEAN);
		RegisterArg sourceArg = intArg.duplicate();
		sourceArg.forceSetInitType(ArgType.INT);
		IfNode ifNode = new IfNode(IfOp.NE, -1, sourceArg, LiteralArg.make(0, ArgType.INT));
		TernaryInsn convertInsn = new TernaryInsn(
				IfCondition.fromIfNode(ifNode), resultArg, LiteralArg.litTrue(), LiteralArg.litFalse());
		convertInsn.add(AFlag.SYNTHETIC);
		block.getInstructions().add(convertInsn);
		phiInsn.replaceArg(intArg, resultArg.duplicate());
	}

	private boolean splitIntBooleanPhiUses(MethodNode mth, SSAVar var) {
		InsnNode assignInsn = var.getAssignInsn();
		if (assignInsn == null || assignInsn.getType() != InsnType.PHI) {
			return false;
		}
		List<RegisterArg> uses = var.getUseList();
		if (uses.size() < 2) {
			return false;
		}
		boolean booleanUseFound = false;
		boolean intUseFound = false;
		List<RegisterArg> booleanUses = new ArrayList<>();
		for (RegisterArg useArg : uses) {
			ArgType useType = useArg.getInitType();
			if (useType.equals(ArgType.BOOLEAN)) {
				booleanUseFound = true;
				if (!canInsertIntToBooleanConversion(mth, useArg)) {
					return false;
				}
				booleanUses.add(useArg);
			} else if (useType.equals(ArgType.INT)) {
				intUseFound = true;
			} else if (isIntZeroComparison(useArg)) {
				intUseFound = true;
			} else {
				return false;
			}
		}
		if (!booleanUseFound || !intUseFound) {
			return false;
		}
		for (RegisterArg booleanUse : booleanUses) {
			insertIntToBooleanConversion(mth, booleanUse);
		}
		return true;
	}

	private static boolean isIntZeroComparison(RegisterArg useArg) {
		InsnNode parentInsn = useArg.getParentInsn();
		if (!(parentInsn instanceof IfNode)) {
			return false;
		}
		IfNode ifInsn = (IfNode) parentInsn;
		IfOp op = ifInsn.getOp();
		if (op != IfOp.EQ && op != IfOp.NE) {
			return false;
		}
		InsnArg firstArg = ifInsn.getArg(0);
		InsnArg secondArg = ifInsn.getArg(1);
		return firstArg == useArg && secondArg.isZeroLiteral()
				|| secondArg == useArg && firstArg.isZeroLiteral();
	}

	private static boolean canInsertIntToBooleanConversion(MethodNode mth, RegisterArg useArg) {
		InsnNode useInsn = useArg.getParentInsn();
		if (useInsn == null) {
			return false;
		}
		BlockNode block = BlockUtils.getBlockByInsn(mth, useInsn);
		return block != null && InsnList.getIndex(block.getInstructions(), useInsn) != -1;
	}

	private void insertIntToBooleanConversion(MethodNode mth, RegisterArg useArg) {
		InsnNode useInsn = Objects.requireNonNull(useArg.getParentInsn());
		BlockNode block = Objects.requireNonNull(BlockUtils.getBlockByInsn(mth, useInsn));
		List<InsnNode> insns = block.getInstructions();
		int useIndex = InsnList.getIndex(insns, useInsn);

		RegisterArg resultArg = useArg.duplicateWithNewSSAVar(mth);
		RegisterArg intArg = useArg.duplicate();
		intArg.forceSetInitType(ArgType.INT);
		IfNode ifNode = new IfNode(IfOp.NE, -1, intArg, LiteralArg.make(0, ArgType.INT));
		TernaryInsn convertInsn = new TernaryInsn(
				IfCondition.fromIfNode(ifNode), resultArg, LiteralArg.litTrue(), LiteralArg.litFalse());
		convertInsn.add(AFlag.SYNTHETIC);
		insns.add(useIndex, convertInsn);
		useInsn.replaceArg(useArg, resultArg.duplicate());
	}

	private boolean fixBooleanUsage(MethodNode mth, RegisterArg boundArg) {
		ArgType boundType = boundArg.getInitType();
		if (boundType == ArgType.BOOLEAN
				|| (boundType.isTypeKnown() && !boundType.isPrimitive())) {
			return false;
		}
		InsnNode insn = boundArg.getParentInsn();
		if (insn == null || insn.getType() == InsnType.IF) {
			return false;
		}
		BlockNode blockNode = BlockUtils.getBlockByInsn(mth, insn);
		if (blockNode == null) {
			return false;
		}
		List<InsnNode> insnList = blockNode.getInstructions();
		int insnIndex = InsnList.getIndex(insnList, insn);
		if (insnIndex == -1) {
			return false;
		}
		InsnType insnType = insn.getType();
		if (insnType == InsnType.CAST) {
			// replace cast
			ArgType type = (ArgType) ((IndexInsnNode) insn).getIndex();
			TernaryInsn convertInsn = prepareBooleanConvertInsn(insn.getResult(), boundArg, type);
			BlockUtils.replaceInsn(mth, blockNode, insnIndex, convertInsn);
			return true;
		}
		if (insnType == InsnType.ARITH) {
			ArithNode arithInsn = (ArithNode) insn;
			if (arithInsn.getOp() == ArithOp.XOR && arithInsn.getArgsCount() == 2) {
				// replace (boolean ^ 1) with (!boolean)
				InsnArg secondArg = arithInsn.getArg(1);
				if (secondArg.isLiteral() && ((LiteralArg) secondArg).getLiteral() == 1) {
					InsnNode convertInsn = notBooleanToInt(arithInsn, boundArg);
					BlockUtils.replaceInsn(mth, blockNode, insnIndex, convertInsn);
					return true;
				}
			}
		}

		// insert before insn
		RegisterArg resultArg = boundArg.duplicateWithNewSSAVar(mth);
		TernaryInsn convertInsn = prepareBooleanConvertInsn(resultArg, boundArg, boundType);
		insnList.add(insnIndex, convertInsn);
		insn.replaceArg(boundArg, convertInsn.getResult().duplicate());
		return true;
	}

	private InsnNode notBooleanToInt(ArithNode insn, RegisterArg boundArg) {
		InsnNode notInsn = new InsnNode(InsnType.NOT, 1);
		notInsn.addArg(boundArg.duplicate());
		notInsn.add(AFlag.SYNTHETIC);

		ArgType resType = insn.getResult().getType();
		if (resType.canBePrimitive(PrimitiveType.BOOLEAN)) {
			notInsn.setResult(insn.getResult());
			return notInsn;
		}
		InsnArg notArg = InsnArg.wrapArg(notInsn);
		notArg.setType(ArgType.BOOLEAN);
		TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(insn.getResult(), notArg, ArgType.INT);
		convertInsn.add(AFlag.SYNTHETIC);
		return convertInsn;
	}

	private TernaryInsn prepareBooleanConvertInsn(RegisterArg resultArg, RegisterArg boundArg, ArgType useType) {
		RegisterArg useArg = boundArg.getSVar().getAssign().duplicate();
		TernaryInsn convertInsn = ModVisitor.makeBooleanConvertInsn(resultArg, useArg, useType);
		convertInsn.add(AFlag.SYNTHETIC);
		return convertInsn;
	}

	private boolean tryToForceImmutableTypes(MethodNode mth) {
		boolean fixed = false;
		for (SSAVar ssaVar : mth.getSVars()) {
			ArgType type = ssaVar.getTypeInfo().getType();
			if (!type.isTypeKnown() && ssaVar.isTypeImmutable()) {
				if (forceImmutableType(ssaVar)) {
					fixed = true;
				}
			}
		}
		if (!fixed) {
			return false;
		}
		return typeInference.runTypePropagation(mth);
	}

	private boolean forceImmutableType(SSAVar ssaVar) {
		for (RegisterArg useArg : ssaVar.getUseList()) {
			InsnNode parentInsn = useArg.getParentInsn();
			if (parentInsn != null) {
				InsnType insnType = parentInsn.getType();
				if (insnType == InsnType.AGET || insnType == InsnType.APUT) {
					ssaVar.setType(ssaVar.getImmutableType());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "FixTypesVisitor";
	}
}
