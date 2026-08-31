package jadx.core.dex.visitors.regions.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.DeclareVariablesAttr;
import jadx.core.dex.attributes.nodes.InitAtDeclareVarsAttr;
import jadx.core.dex.attributes.nodes.SkipMethodArgsAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.regions.AbstractRegionVisitor;
import jadx.core.dex.visitors.regions.DepthRegionTraversal;
import jadx.core.dex.visitors.typeinference.TypeCompare;
import jadx.core.dex.visitors.typeinference.TypeCompareEnum;
import jadx.core.utils.ListUtils;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxException;

public class ProcessVariables extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ProcessVariables.class);
	// Expected size 11 selects the same initial table as IdentityHashMap's default constructor.
	private static final int IDENTITY_MAP_DEFAULT_EXPECTED_SIZE = 11;

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		removeUnusedResults(mth);
		if (mth.getSVars().isEmpty()) {
			return;
		}

		List<CodeVar> codeVars = collectCodeVars(mth);
		if (codeVars.isEmpty()) {
			return;
		}
		checkCodeVars(mth, codeVars);
		// TODO: reduce code vars by name if debug info applied (need checks for variable scopes)

		// collect all variables usage
		CollectUsageRegionVisitor usageCollector = new CollectUsageRegionVisitor();
		DepthRegionTraversal.traverse(mth, usageCollector);
		Map<SSAVar, VarUsage> ssaUsageMap = usageCollector.getUsageMap();
		if (ssaUsageMap.isEmpty()) {
			return;
		}

		Map<CodeVar, List<VarUsage>> codeVarUsage = mergeUsageMaps(codeVars, ssaUsageMap);

		for (Entry<CodeVar, List<VarUsage>> entry : codeVarUsage.entrySet()) {
			declareVar(mth, entry.getKey(), entry.getValue());
		}
	}

	private static void removeUnusedResults(MethodNode mth) {
		int varCount = mth.getSVars().size();
		int initialSize = varCount <= 2 ? varCount : IDENTITY_MAP_DEFAULT_EXPECTED_SIZE;
		Set<SSAVar> knownVars = Collections.newSetFromMap(new IdentityHashMap<>(initialSize));
		List<SSAVar> sVars = mth.getSVars();
		for (int i = 0; i < varCount; i++) {
			knownVars.add(sVars.get(i));
		}
		DepthRegionTraversal.traverse(mth, new AbstractRegionVisitor() {
			private final List<RegisterArg> args = new ArrayList<>();

			@Override
			public void processBlock(MethodNode mth, IBlock container) {
				List<InsnNode> insns = container.getInstructions();
				for (int i = 0, count = insns.size(); i < count; i++) {
					InsnNode insn = insns.get(i);
					if (!insn.contains(AFlag.DONT_GENERATE) && !insn.contains(AFlag.REMOVE)) {
						initOrphanCodeVars(insn);
					}
					RegisterArg resultArg = insn.getResult();
					if (resultArg == null) {
						continue;
					}
					SSAVar ssaVar = resultArg.getSVar();
					if (ssaVar == null) {
						continue;
					}
					if (isVarUnused(mth, ssaVar)) {
						boolean remove = false;
						if (insn.canRemoveResult()) {
							// remove unused result
							remove = true;
						} else if (canRemoveInsn(insn)) {
							// remove whole insn
							insn.add(AFlag.REMOVE);
							insn.add(AFlag.DONT_GENERATE);
							remove = true;
						}
						if (remove) {
							insn.setResult(null);
							mth.removeSVar(ssaVar);
							List<RegisterArg> useList = ssaVar.getUseList();
							for (int j = 0, useCount = useList.size(); j < useCount; j++) {
								useList.get(j).resetSSAVar();
							}
						}
					}
				}
			}

			private void initOrphanCodeVars(InsnNode insn) {
				insn.visitInsns(innerInsn -> {
					initOrphanSsaVar(knownVars, innerInsn.getResult());
				});
				args.clear();
				insn.getRegisterArgs(args);
				for (int i = 0, count = args.size(); i < count; i++) {
					initOrphanSsaVar(knownVars, args.get(i));
				}
			}

			/**
			 * Remove insn if a result is not used
			 */
			private boolean canRemoveInsn(InsnNode insn) {
				if (insn.isConstInsn()) {
					return true;
				}
				switch (insn.getType()) {
					case CAST:
					case CHECK_CAST:
						return true;
					default:
						return false;
				}
			}

			private boolean isVarUnused(MethodNode mth, @Nullable SSAVar ssaVar) {
				if (ssaVar == null) {
					return true;
				}
				List<RegisterArg> useList = ssaVar.getUseList();
				if (useList.isEmpty()) {
					return true;
				}
				if (ssaVar.isUsedInPhi()) {
					return false;
				}
				return ListUtils.allMatch(useList, arg -> isArgUnused(mth, arg));
			}

			private boolean isArgUnused(MethodNode mth, RegisterArg arg) {
				if (arg.contains(AFlag.REMOVE)) {
					return true;
				}
				// check constructors for removed args
				InsnNode parentInsn = arg.getParentInsn();
				if (parentInsn != null
						&& parentInsn.getType() == InsnType.CONSTRUCTOR
						&& parentInsn.contains(AType.METHOD_DETAILS)) {
					MethodNode resolveMth = mth.root().getMethodUtils().resolveMethod(((ConstructorInsn) parentInsn));
					if (resolveMth != null) {
						SkipMethodArgsAttr skipArgs = resolveMth.get(AType.SKIP_MTH_ARGS);
						int insnPos = parentInsn.getArgIndex(arg);
						if (skipArgs != null && skipArgs.isRemovedArg(insnPos, arg.getType())) {
							arg.add(AFlag.DONT_GENERATE);
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	private static void initOrphanSsaVar(Set<SSAVar> knownVars, @Nullable RegisterArg arg) {
		if (arg == null || arg.contains(AFlag.DONT_GENERATE)) {
			return;
		}
		SSAVar ssaVar = arg.getSVar();
		if (ssaVar != null && knownVars.add(ssaVar)) {
			initOrphanRegionSsaVar(ssaVar);
		}
	}

	static void initOrphanRegionSsaVar(SSAVar ssaVar) {
		if (!ssaVar.isCodeVarSet()) {
			ssaVar.setCodeVar(new CodeVar());
		}
		ArgType type = ssaVar.getAssign().getType();
		ArgType codeVarType = ssaVar.getCodeVar().getType();
		if ((codeVarType == null || !codeVarType.isTypeKnown()) && type.isTypeKnown()) {
			ssaVar.getCodeVar().setType(type);
		}
	}

	private void checkCodeVars(MethodNode mth, List<CodeVar> codeVars) {
		int unknownTypesCount = 0;
		for (int i = 0, count = codeVars.size(); i < count; i++) {
			CodeVar codeVar = codeVars.get(i);
			ArgType codeVarType = codeVar.getType();
			if (codeVarType == null) {
				codeVar.setType(ArgType.UNKNOWN);
				unknownTypesCount++;
			} else {
				List<SSAVar> ssaVars = codeVar.getSsaVars();
				for (int j = 0, ssaCount = ssaVars.size(); j < ssaCount; j++) {
					SSAVar ssaVar = ssaVars.get(j);
					ArgType ssaType = ssaVar.getImmutableType();
					if (ssaType != null && ssaType.isTypeKnown()) {
						TypeCompare comparator = mth.root().getTypeUpdate().getTypeCompare();
						TypeCompareEnum result = comparator.compareTypes(ssaType, codeVarType);
						if (result == TypeCompareEnum.CONFLICT || result.isNarrow()) {
							mth.addWarn("Incorrect type for immutable var: ssa=" + ssaType
									+ ", code=" + codeVarType
									+ ", for " + ssaVar.getDetailedVarInfo(mth));
						}
					}
				}
			}
		}
		if (unknownTypesCount != 0) {
			mth.addWarn("Unknown variable types count: " + unknownTypesCount);
		}
	}

	private void declareVar(MethodNode mth, CodeVar codeVar, List<VarUsage> usageList) {
		if (codeVar.isDeclared()) {
			return;
		}

		VarUsage mergedUsage = new VarUsage(null);
		int usageCount = usageList.size();
		for (int i = 0; i < usageCount; i++) {
			VarUsage varUsage = usageList.get(i);
			mergedUsage.getAssigns().addAll(varUsage.getAssigns());
			mergedUsage.getUses().addAll(varUsage.getUses());
		}
		if (mergedUsage.getAssigns().isEmpty() && mergedUsage.getUses().isEmpty()) {
			return;
		}

		// check if variable can be declared at one of assigns
		if (checkDeclareAtAssign(usageList, mergedUsage)) {
			return;
		}
		// TODO: search closest region for declare

		// region not found, declare at method start
		declareVarInRegion(mth.getRegion(), codeVar);
	}

	private List<CodeVar> collectCodeVars(MethodNode mth) {
		List<SSAVar> methodVars = mth.getSVars();
		int varsCount = methodVars.size();
		int mapCapacity = Math.max(1, varsCount * 4 / 3 + 1);
		Map<CodeVar, List<SSAVar>> codeVars = new LinkedHashMap<>(mapCapacity);
		InitAtDeclareVarsAttr initVars = mth.get(AType.INIT_AT_DECLARE_VARS);
		for (int i = 0; i < varsCount; i++) {
			SSAVar ssaVar = methodVars.get(i);
			if (ssaVar.getCodeVar().isThis()) {
				continue;
			}
			CodeVar codeVar = ssaVar.getCodeVar();
			List<SSAVar> list = codeVars.get(codeVar);
			if (list == null) {
				list = new ArrayList<>(Math.max(1, codeVar.getSsaVars().size()));
				codeVars.put(codeVar, list);
			}
			list.add(ssaVar);
		}

		for (Entry<CodeVar, List<SSAVar>> entry : codeVars.entrySet()) {
			CodeVar codeVar = entry.getKey();
			List<SSAVar> list = entry.getValue();
			int groupSize = list.size();
			for (int i = 0; i < groupSize; i++) {
				CodeVar localCodeVar = list.get(i).getCodeVar();
				codeVar.mergeFlagsFrom(localCodeVar);
			}
			if (groupSize > 1) {
				for (int i = 0; i < groupSize; i++) {
					list.get(i).setCodeVar(codeVar);
				}
			}
			codeVar.setSsaVars(list);
			if (initVars != null) {
				for (int i = 0; i < groupSize; i++) {
					if (initVars.contains(list.get(i).getRegNum())) {
						codeVar.setInitAtDeclaration(true);
						break;
					}
				}
			}
		}
		return new ArrayList<>(codeVars.keySet());
	}

	private Map<CodeVar, List<VarUsage>> mergeUsageMaps(List<CodeVar> codeVars, Map<SSAVar, VarUsage> ssaUsageMap) {
		int codeVarsCount = codeVars.size();
		int mapCapacity = Math.max(1, codeVarsCount * 4 / 3 + 1);
		Map<CodeVar, List<VarUsage>> codeVarUsage = new LinkedHashMap<>(mapCapacity);
		for (int codeVarIndex = 0; codeVarIndex < codeVarsCount; codeVarIndex++) {
			CodeVar codeVar = codeVars.get(codeVarIndex);
			List<SSAVar> ssaVars = codeVar.getSsaVars();
			int ssaVarsCount = ssaVars.size();
			List<VarUsage> list = new ArrayList<>(ssaVarsCount);
			for (int ssaVarIndex = 0; ssaVarIndex < ssaVarsCount; ssaVarIndex++) {
				SSAVar ssaVar = ssaVars.get(ssaVarIndex);
				VarUsage usage = ssaUsageMap.get(ssaVar);
				if (usage != null) {
					list.add(usage);
				}
			}
			codeVarUsage.put(codeVar, Utils.lockList(list));
		}
		return codeVarUsage;
	}

	private boolean checkDeclareAtAssign(List<VarUsage> list, VarUsage mergedUsage) {
		if (mergedUsage.getAssigns().isEmpty()) {
			return false;
		}
		int usageCount = list.size();
		for (int usageIndex = 0; usageIndex < usageCount; usageIndex++) {
			VarUsage u = list.get(usageIndex);
			List<UsePlace> assigns = u.getAssigns();
			int assignsCount = assigns.size();
			for (int assignIndex = 0; assignIndex < assignsCount; assignIndex++) {
				UsePlace assign = assigns.get(assignIndex);
				if (canDeclareAt(mergedUsage, assign)) {
					return checkDeclareAtAssign(u.getVar());
				}
			}
		}
		return false;
	}

	private static boolean canDeclareAt(VarUsage usage, UsePlace usePlace) {
		IRegion region = usePlace.getRegion();
		// workaround for declare variables used in several loops
		if (region instanceof LoopRegion) {
			List<UsePlace> assigns = usage.getAssigns();
			int assignsCount = assigns.size();
			for (int i = 0; i < assignsCount; i++) {
				UsePlace use = assigns.get(i);
				if (!RegionUtils.isRegionContainsRegion(region, use.getRegion())) {
					return false;
				}
			}
		}
		// can't declare in else-if chain between 'else' and next 'if'
		if (region.contains(AFlag.ELSE_IF_CHAIN)) {
			return false;
		}
		return isAllUseAfter(usePlace, usage.getAssigns())
				&& isAllUseAfter(usePlace, usage.getUses());
	}

	/**
	 * Check if all {@code usePlaces} are after {@code checkPlace}
	 */
	static boolean isAllUseAfter(UsePlace checkPlace, List<UsePlace> usePlaces) {
		IRegion region = checkPlace.getRegion();
		IBlock block = checkPlace.getBlock();
		List<IContainer> subBlocks = region.getSubBlocks();
		int subBlocksCount = subBlocks.size();
		int blockIndex = -1;
		for (int i = 0; i < subBlocksCount; i++) {
			if (subBlocks.get(i) == block) {
				blockIndex = i;
				break;
			}
		}
		if (blockIndex == -1) {
			return false;
		}
		int usePlacesCount = usePlaces.size();
		for (int i = 0; i < usePlacesCount; i++) {
			IContainer directContainer = resolveDirectContainer(region, subBlocks, usePlaces.get(i));
			if (directContainer == null || !containsIdentity(subBlocks, blockIndex, directContainer)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsIdentity(List<IContainer> containers, int fromIndex, IContainer target) {
		int count = containers.size();
		for (int i = fromIndex; i < count; i++) {
			if (containers.get(i) == target) {
				return true;
			}
		}
		return false;
	}

	private static IContainer resolveDirectContainer(
			IRegion region, List<IContainer> subBlocks, UsePlace usePlace) {
		IRegion useRegion = usePlace.getRegion();
		if (useRegion == region) {
			return usePlace.getBlock();
		}
		IRegion current = useRegion;
		while (current != null) {
			IRegion parent = current.getParent();
			if (parent == region) {
				return current;
			}
			current = parent;
		}
		// Exception-handler regions can have no single parent, use the full containment check as a
		// fallback.
		int subBlocksCount = subBlocks.size();
		for (int i = 0; i < subBlocksCount; i++) {
			IContainer subBlock = subBlocks.get(i);
			if (isContainerContainsUsePlace(subBlock, usePlace)) {
				return subBlock;
			}
		}
		return null;
	}

	private static boolean isContainerContainsUsePlace(IContainer subBlock, UsePlace usePlace) {
		if (subBlock == usePlace.getBlock()) {
			return true;
		}
		if (subBlock instanceof IRegion) {
			// TODO: make index for faster check
			return RegionUtils.isRegionContainsRegion(subBlock, usePlace.getRegion());
		}
		return false;
	}

	private static boolean checkDeclareAtAssign(SSAVar var) {
		RegisterArg arg = var.getAssign();
		InsnNode parentInsn = arg.getParentInsn();
		if (parentInsn == null
				|| parentInsn.contains(AFlag.WRAPPED)
				|| parentInsn.getType() == InsnType.PHI) {
			return false;
		}
		if (!arg.equals(parentInsn.getResult())) {
			return false;
		}
		parentInsn.add(AFlag.DECLARE_VAR);
		var.getCodeVar().setDeclared(true);
		return true;
	}

	private static void declareVarInRegion(IContainer region, CodeVar var) {
		if (var.isDeclared()) {
			LOG.warn("Try to declare already declared variable: {}", var);
			return;
		}
		DeclareVariablesAttr dv = region.get(AType.DECLARE_VARIABLES);
		if (dv == null) {
			dv = new DeclareVariablesAttr();
			region.addAttr(dv);
		}
		dv.addVar(var);
		var.setDeclared(true);
	}
}
