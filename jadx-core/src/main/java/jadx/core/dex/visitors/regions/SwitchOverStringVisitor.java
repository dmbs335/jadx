package jadx.core.dex.visitors.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.api.CommentsLevel;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.CodeFeaturesAttr;
import jadx.core.dex.attributes.nodes.CodeFeaturesAttr.CodeFeature;
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
import jadx.core.dex.attributes.nodes.RegionRefAttr;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IBlock;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.nodes.InsnContainer;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.regions.Region;
import jadx.core.dex.regions.SwitchRegion;
import jadx.core.dex.regions.conditions.IfRegion;
import jadx.core.dex.regions.loops.LoopRegion;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.regions.maker.SwitchRegionMaker;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.EncodedValueUtils;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.ListUtils;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.JadxException;

/**
 * A switch(string) java code will be compiled to two switches in class code.
 * Sometimes, android's d8/r8 will make some further modification.
 * 1st switch could be changed to ifs to reduce size of dex.
 * 2nd switch could be flattened and removed if there are many cases using a same block.
 */
@JadxVisitor(
		name = "SwitchOverStringVisitor",
		desc = "Restore switch over string",
		runAfter = IfRegionVisitor.class,
		runBefore = ReturnVisitor.class
)
public class SwitchOverStringVisitor extends AbstractVisitor implements IRegionIterativeVisitor {
	private static final Integer DEFAULT_NUM_VALUE = -1;

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (!CodeFeaturesAttr.contains(mth, CodeFeature.SWITCH)) {
			return;
		}
		DepthRegionTraversal.traverseIterative(mth, this);
	}

	@Override
	public boolean visitRegion(MethodNode mth, IRegion region) {
		if (region instanceof SwitchRegion || region instanceof IfRegion) {
			return restoreSwitchOverString(mth, region);
		}
		return false;
	}

	private boolean restoreSwitchOverString(MethodNode mth, IRegion part1Region) {
		try {
			InsnNode strHashInsn = BlockUtils.getLastInsn(RegionUtils.getFirstBlockNode(part1Region));
			InvokeNode hashcodeInv = strHashInsn == null ? null : getStrHashcodeInvokeInsn(strHashInsn.getArg(0));
			InsnArg strArg = hashcodeInv == null ? null : hashcodeInv.getInstanceArg();
			if (strArg == null || !strArg.isRegister()) {
				return false;
			}
			SwitchData data = new SwitchData(mth, part1Region);
			data.setHashcodeInvokeInsn(hashcodeInv);
			data.setStrArg((RegisterArg) strArg);

			IContainer nextContainer = RegionUtils.getNextContainer(mth, part1Region);
			boolean isPart1Switch = part1Region instanceof SwitchRegion;
			boolean directPart2Switch = nextContainer instanceof SwitchRegion;
			SwitchRegion part2Region = directPart2Switch
					? (SwitchRegion) nextContainer
					: getSwitchAfterEmptyBridge(part1Region, nextContainer);
			if (part2Region != null) {
				InsnNode part2SwInsn = BlockUtils.getLastInsnWithType(part2Region.getHeader(), InsnType.SWITCH);
				if (part2SwInsn == null || !part2SwInsn.getArg(0).isRegister()) {
					return false;
				}
				data.setType(isPart1Switch ? SwitchStringType.SWITCH_SWITCH : SwitchStringType.IF_SWITCH);
				data.setPart2Region(part2Region);
				data.setNumArg((RegisterArg) part2SwInsn.getArg(0));
			} else if (isPart1Switch) {
				data.setType(SwitchStringType.SINGLE_SWITCH);
			} else {
				return false;
			}

			if (!collectPart1RegionCases(data)) {
				return false;
			}
			if (!prepareMergedSwitchCases(data) || !replaceWithMergedSwitch(data)) {
				if (directPart2Switch || part2Region == null) {
					mth.addWarnComment("Failed to restore switch over string. Please report as a decompilation issue");
				}
				return false;
			}
			return true;
		} catch (StackOverflowError | Exception e) {
			mth.addWarnComment("Failed to restore switch over string. Please report as a decompilation issue", e);
			return false;
		}
	}

	static @Nullable SwitchRegion getSwitchAfterEmptyBridge(IRegion part1Region, @Nullable IContainer nextContainer) {
		if (!(nextContainer instanceof BlockNode) || !((BlockNode) nextContainer).getInstructions().isEmpty()) {
			return null;
		}
		List<IContainer> siblings = part1Region.getParent().getSubBlocks();
		int bridgePos = siblings.indexOf(nextContainer);
		if (bridgePos == -1 || bridgePos + 1 >= siblings.size()) {
			return null;
		}
		IContainer afterBridge = siblings.get(bridgePos + 1);
		return afterBridge instanceof SwitchRegion ? (SwitchRegion) afterBridge : null;
	}

	/**
	 * store str and num/caseBlock in switchData
	 * validate:
	 * - case key is hashcode of strValue
	 * - case block is str.equals compare
	 * - str compare thenBlock is num assign (SWITCH_SWITCH and IF_SWITCH)
	 */
	private static boolean collectPart1RegionCases(SwitchData data) {
		// a map of hashcode keys and case blocks
		Map<Integer, BlockNode> hashCases = new LinkedHashMap<>();
		if (data.getType() == SwitchStringType.IF_SWITCH) {
			IfRegion part1If = (IfRegion) data.getPart1Region();
			BlockNode part2Header = Objects.requireNonNull(data.getPart2Region()).getHeader();
			RegisterArg strHashArg = null;
			BlockNode ifStartBlock = part1If.getConditionBlocks().get(0);
			BlockNode hashCmpBlock = getOnlyOneInsnBlock(ifStartBlock);
			do {
				IfNode ifNode = (IfNode) BlockUtils.getLastInsnWithType(hashCmpBlock, InsnType.IF);
				if (ifNode == null || (ifNode.getOp() != IfOp.NE && ifNode.getOp() != IfOp.EQ)) {
					return false;
				}
				boolean isNE = ifNode.getOp() == IfOp.NE;
				BlockNode thenBlock = getOnlyOneInsnBlock(isNE ? ifNode.getElseBlock() : ifNode.getThenBlock());
				BlockNode elseBlock = getOnlyOneInsnBlock(isNE ? ifNode.getThenBlock() : ifNode.getElseBlock());
				if (thenBlock == null || elseBlock == null || !ifNode.getArg(0).isRegister() || !ifNode.getArg(1).isLiteral()) {
					return false;
				}
				RegisterArg tmpStrHashArg = (RegisterArg) ifNode.getArg(0);
				LiteralArg literalArg = (LiteralArg) ifNode.getArg(1);
				if (strHashArg == null) {
					strHashArg = tmpStrHashArg;
				} else if (!strHashArg.sameCodeVar(tmpStrHashArg)) {
					return false;
				}
				hashCases.put((int) literalArg.getLiteral(), thenBlock);
				hashCmpBlock = elseBlock;
				// end of part1If: no further hash compare, next block is part2switch
				Integer fallbackNum = extractConstNumber(data, BlockUtils.getLastInsn(hashCmpBlock));
				BlockNode nextBlock = BlockUtils.getNextBlock(hashCmpBlock);
				if (elseBlock == part2Header || (DEFAULT_NUM_VALUE.equals(fallbackNum) && nextBlock == part2Header)) {
					break;
				}
			} while (true);
		} else {
			SwitchRegion part1Switch = (SwitchRegion) data.getPart1Region();
			SwitchInsn swInsn = (SwitchInsn) BlockUtils.getLastInsnWithType(part1Switch.getHeader(), InsnType.SWITCH);
			Objects.requireNonNull(swInsn);
			for (int i = 0; i < swInsn.getKeys().length; i++) {
				BlockNode caseBlock = getOnlyOneInsnBlock(swInsn.getTargetBlocks()[i]);
				if (caseBlock == null) {
					return false;
				}
				hashCases.put(swInsn.getKeys()[i], caseBlock);
			}
		}

		// save to switchData
		data.setCases(new ArrayList<>(hashCases.size()));
		for (Integer hashcode : hashCases.keySet()) {
			BlockNode caseBlock = hashCases.get(hashcode);
			IfNode ifStrEqualsInsn = (IfNode) BlockUtils.getLastInsnWithType(caseBlock, InsnType.IF);
			if (!isIfStringEqualsInsn(ifStrEqualsInsn)) {
				return false;
			}
			do {
				InsnNode strEqualsInsn = InsnUtils.getWrappedInsn(ifStrEqualsInsn.getArg(0));
				Objects.requireNonNull(strEqualsInsn);
				InsnArg strArg = strEqualsInsn.getArg(0);
				InsnArg valArg = strEqualsInsn.getArg(1);
				Object strValue = InsnUtils.getConstValueByArg(data.getMth().root(), valArg);
				if (!data.getStrArg().equals(strArg) || !(strValue instanceof String) || strValue.hashCode() != hashcode) {
					return false;
				}
				boolean isCmpNE = (ifStrEqualsInsn.getOp() == IfOp.EQ) != ifStrEqualsInsn.getArg(1).isTrue();
				BlockNode thenBlock = isCmpNE ? ifStrEqualsInsn.getElseBlock() : ifStrEqualsInsn.getThenBlock();
				BlockNode elseBlock = isCmpNE ? ifStrEqualsInsn.getThenBlock() : ifStrEqualsInsn.getElseBlock();
				Integer numValue = null;
				if (data.getType() == SwitchStringType.SWITCH_SWITCH || data.getType() == SwitchStringType.IF_SWITCH) {
					InsnNode numInsn = BlockUtils.getLastInsn(getOnlyOneInsnBlock(thenBlock));
					RegisterArg numArg = Objects.requireNonNull(data.getNumArg());
					if (thenBlock != null && (numInsn == null || numInsn.getType() == InsnType.SWITCH)) {
						// num is assigned before 1st region. find nearest assign
						BlockNode iDom = thenBlock.getIDom();
						while (iDom != null && numValue == null) {
							for (InsnNode insn : iDom.getInstructions()) {
								numValue = extractConstNumber(data, insn);
							}
							iDom = iDom.getIDom();
						}
						if (numValue == null) {
							return false;
						}
					} else if (numInsn != null && numArg.sameCodeVar(numInsn.getResult())) {
						numValue = extractConstNumber(data, numInsn);
					} else {
						return false;
					}
				}
				// store str and num assign in switchData
				data.getCases().add(new CaseData(strValue, numValue, thenBlock));
				// there may be more string compare (same hashcode)
				BlockNode nextIfBlock = getOnlyOneInsnBlock(elseBlock);
				ifStrEqualsInsn = (IfNode) BlockUtils.getLastInsnWithType(nextIfBlock, InsnType.IF);
			} while (isIfStringEqualsInsn(ifStrEqualsInsn));
		}
		return true;
	}

	/**
	 * create cases according to part2Region (part1Region if is SINGLE_SWITCH).
	 * replace keys with strings.
	 */
	private static boolean prepareMergedSwitchCases(SwitchData data) {
		SwitchRegion part2Region = data.getPart2Region();
		List<CaseData> cases = data.getCases();
		List<SwitchRegion.CaseInfo> newCases = new ArrayList<>();
		data.setNewCases(newCases);
		if (data.getType() == SwitchStringType.SWITCH_SWITCH || data.getType() == SwitchStringType.IF_SWITCH) {
			// group by num
			Map<Integer, List<Object>> casesMap = new HashMap<>(cases.size());
			for (CaseData caseData : cases) {
				casesMap.computeIfAbsent(caseData.getCodeNum(), v -> new ArrayList<>()).add(caseData.getStrValue());
			}
			SwitchRegion.CaseInfo defaultCase = null;
			for (SwitchRegion.CaseInfo caseInfo : Objects.requireNonNull(part2Region).getCases()) {
				SwitchRegion.CaseInfo newCase = new SwitchRegion.CaseInfo(new ArrayList<>(), caseInfo.getContainer());
				for (Object key : caseInfo.getKeys()) {
					Integer intKey = unwrapIntKey(key);
					if (key != SwitchRegion.DEFAULT_CASE_KEY) {
						List<Object> strings = casesMap.remove(Objects.requireNonNull(intKey));
						if (strings == null || strings.isEmpty()) {
							return false;
						}
						newCase.getKeys().addAll(strings);
					} else {
						newCase.getKeys().add(SwitchRegion.DEFAULT_CASE_KEY);
						defaultCase = newCase;
					}
				}
				newCases.add(newCase);
			}
			if (defaultCase != null) {
				int defaultKeyPos = defaultCase.getKeys().indexOf(SwitchRegion.DEFAULT_CASE_KEY);
				for (List<Object> strings : casesMap.values()) {
					defaultCase.getKeys().addAll(defaultKeyPos, strings);
					defaultKeyPos += strings.size();
				}
				casesMap.clear();
			}
			if (!casesMap.isEmpty()) {
				data.getMth().addWarnComment("switch over string: strings are not added: " + casesMap.values());
			}
		} else if (data.getType() == SwitchStringType.SINGLE_SWITCH) {
			SwitchRegion part1Region = (SwitchRegion) data.getPart1Region();
			SwitchInsn swInsn = (SwitchInsn) BlockUtils.getLastInsnWithType(part1Region.getHeader(), InsnType.SWITCH);
			BlockNode defBlock = Objects.requireNonNull(swInsn).getDefTargetBlock();
			if (defBlock != null) {
				BlockNode defActionBlock = getFirstInsnBlock(defBlock);
				boolean externalDefault = RegionUtils.getBlockContainer(part1Region, defActionBlock) == null;
				boolean externalTerminal = externalDefault && isTerminalPath(defActionBlock);
				boolean externalFallthrough = externalDefault
						&& isExternalFallthrough(data.getMth(), part1Region, defActionBlock);
				if (!externalTerminal && !externalFallthrough) {
					cases.add(new CaseData(SwitchRegion.DEFAULT_CASE_KEY, DEFAULT_NUM_VALUE, defBlock));
				}
			}
			CaseData lastCaseData = null;
			for (CaseData caseData : cases) {
				if (lastCaseData != null && lastCaseData.getCode() == caseData.getCode()) {
					// combine cases whose blocks are the same
					SwitchRegion.CaseInfo lastInfo = Objects.requireNonNull(ListUtils.last(newCases));
					lastInfo.getKeys().add(caseData.getStrValue());
				} else {
					IContainer container = RegionUtils.getBlockContainer(part1Region, caseData.getCode());
					if (container == null) {
						BlockNode actionBlock = getFirstInsnBlock(caseData.getCode());
						container = RegionUtils.getBlockContainer(part1Region, actionBlock);
						if (container == null) {
							if (isTerminalBlock(actionBlock)) {
								container = actionBlock;
							} else if (isExternalDefaultBeforeSharedJoin(caseData, actionBlock)) {
								container = actionBlock;
							} else if (isExternalFallthrough(data.getMth(), part1Region, actionBlock)) {
								Region fallthroughCase = new Region(part1Region);
								data.addExternalFallthroughCase(fallthroughCase);
								container = fallthroughCase;
							} else {
								container = getExternalCaseRegion(part1Region, actionBlock);
							}
						}
					}
					if (container == null) {
						return false;
					}
					SwitchRegion.CaseInfo newInfo = new SwitchRegion.CaseInfo(new ArrayList<>(), container);
					newInfo.getKeys().add(caseData.getStrValue());
					newCases.add(newInfo);
				}
				lastCaseData = caseData;
			}
			List<IContainer> sharedTail = getSharedCaseTail(part1Region);
			if (!sharedTail.isEmpty()
					&& part1Region.getParent() instanceof Region
					&& isSharedTailExtractionNeeded(newCases, sharedTail.get(0))
					&& canNormalizeSharedTailExits(data.getMth(), part1Region, sharedTail)
					&& newCases.stream().allMatch(caseInfo -> canStripSharedTail(caseInfo.getContainer(), sharedTail))) {
				for (int i = 0; i < newCases.size(); i++) {
					SwitchRegion.CaseInfo caseInfo = newCases.get(i);
					IContainer caseContainer = stripSharedTail(caseInfo.getContainer(), sharedTail);
					newCases.set(i, new SwitchRegion.CaseInfo(caseInfo.getKeys(), caseContainer));
				}
				data.setSharedCaseTail(sharedTail);
			}
		}
		return true;
	}

	private static List<IContainer> getSharedCaseTail(SwitchRegion switchRegion) {
		List<IContainer> sharedTail = null;
		for (SwitchRegion.CaseInfo caseInfo : switchRegion.getCases()) {
			IContainer caseContainer = caseInfo.getContainer();
			if (!(caseContainer instanceof Region)) {
				return Collections.emptyList();
			}
			List<IContainer> subBlocks = withoutTrailingSwitchBreak(((Region) caseContainer).getSubBlocks());
			if (sharedTail == null) {
				sharedTail = new ArrayList<>(subBlocks);
				continue;
			}
			int sharedPos = sharedTail.size() - 1;
			int casePos = subBlocks.size() - 1;
			while (sharedPos >= 0 && casePos >= 0
					&& isSameContainer(sharedTail.get(sharedPos), subBlocks.get(casePos))) {
				sharedPos--;
				casePos--;
			}
			sharedTail = new ArrayList<>(sharedTail.subList(sharedPos + 1, sharedTail.size()));
			if (sharedTail.isEmpty()) {
				return Collections.emptyList();
			}
		}
		return sharedTail == null ? Collections.emptyList() : sharedTail;
	}

	private static List<IContainer> withoutTrailingSwitchBreak(List<IContainer> containers) {
		int end = containers.size();
		if (end != 0) {
			IBlock lastBlock = RegionUtils.getLastBlock(containers.get(end - 1));
			InsnNode lastInsn = BlockUtils.getLastInsn(lastBlock);
			if (lastInsn != null && lastInsn.getType() == InsnType.BREAK) {
				end--;
			}
		}
		return containers.subList(0, end);
	}

	private static boolean isSameContainer(IContainer first, IContainer second) {
		if (first == second) {
			return true;
		}
		if (first.getClass() != second.getClass()) {
			return false;
		}
		BlockNode firstBlock = RegionUtils.getFirstBlockNode(first);
		return firstBlock != null && firstBlock == RegionUtils.getFirstBlockNode(second);
	}

	private static boolean isSharedTailExtractionNeeded(
			List<SwitchRegion.CaseInfo> cases, IContainer tailStart) {
		for (SwitchRegion.CaseInfo caseInfo : cases) {
			if (!containsContainer(caseInfo.getContainer(), tailStart)) {
				return true;
			}
		}
		return false;
	}

	private static boolean canStripSharedTail(IContainer container, List<IContainer> sharedTail) {
		if (!containsContainer(container, sharedTail.get(0))) {
			return true;
		}
		if (!(container instanceof Region)) {
			return false;
		}
		List<IContainer> subBlocks = withoutTrailingSwitchBreak(((Region) container).getSubBlocks());
		int tailPos = subBlocks.size() - sharedTail.size();
		if (tailPos < 0) {
			return false;
		}
		for (int i = 0; i < sharedTail.size(); i++) {
			if (!isSameContainer(subBlocks.get(tailPos + i), sharedTail.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static IContainer stripSharedTail(IContainer container, List<IContainer> sharedTail) {
		if (!containsContainer(container, sharedTail.get(0))) {
			return container;
		}
		Region region = (Region) container;
		List<IContainer> subBlocks = withoutTrailingSwitchBreak(region.getSubBlocks());
		int tailPos = subBlocks.size() - sharedTail.size();
		Region prefix = new Region(region.getParent());
		for (int i = 0; i < tailPos; i++) {
			prefix.add(subBlocks.get(i));
		}
		return prefix;
	}

	private static boolean containsContainer(IContainer container, IContainer target) {
		if (container == target) {
			return true;
		}
		if (target instanceof BlockNode) {
			return RegionUtils.isRegionContainsBlock(container, (BlockNode) target);
		}
		return target instanceof IRegion
				&& RegionUtils.isRegionContainsRegion(container, (IRegion) target);
	}

	private static boolean isTerminalBlock(@Nullable BlockNode block) {
		InsnNode lastInsn = BlockUtils.getLastInsn(block);
		return lastInsn != null && (lastInsn.getType() == InsnType.RETURN || lastInsn.getType() == InsnType.THROW);
	}

	private static boolean isTerminalPath(@Nullable BlockNode block) {
		Set<BlockNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		while (block != null && visited.add(block)) {
			if (isTerminalBlock(block)) {
				return true;
			}
			List<BlockNode> successors = block.getCleanSuccessors();
			if (successors.size() != 1) {
				return false;
			}
			BlockNode next = successors.get(0);
			if (next.getPredecessors().size() != 1) {
				return false;
			}
			block = next;
		}
		return false;
	}

	private static boolean isExternalFallthrough(MethodNode mth, IRegion switchRegion, @Nullable BlockNode actionBlock) {
		if (actionBlock == null) {
			return false;
		}
		IRegion currentRegion = switchRegion;
		while (currentRegion.getParent() != null) {
			IContainer nextContainer = RegionUtils.getNextContainer(mth, currentRegion);
			if (nextContainer != null) {
				BlockNode nextBlock = RegionUtils.getFirstBlockNode(nextContainer);
				return getFirstInsnBlock(nextBlock) == actionBlock;
			}
			currentRegion = currentRegion.getParent();
		}
		return false;
	}

	private static boolean isExternalDefaultBeforeSharedJoin(CaseData caseData, @Nullable BlockNode actionBlock) {
		if (caseData.getStrValue() != SwitchRegion.DEFAULT_CASE_KEY || actionBlock == null) {
			return false;
		}
		List<BlockNode> successors = actionBlock.getCleanSuccessors();
		return successors.size() == 1 && successors.get(0).getPredecessors().size() > 1;
	}

	private static @Nullable IContainer getExternalCaseRegion(
			SwitchRegion part1Region, @Nullable BlockNode actionBlock) {
		if (actionBlock == null) {
			return null;
		}
		IRegion parent = part1Region.getParent();
		IContainer container = RegionUtils.getBlockContainer(parent, actionBlock);
		return container instanceof IRegion && container != parent ? container : null;
	}

	/** replace with new switch. remove original code */
	private static boolean replaceWithMergedSwitch(SwitchData data) {
		MethodNode mth = data.getMth();
		IRegion part1Region = data.getPart1Region();
		IRegion part1Parent = part1Region.getParent();
		SwitchRegion part2Region = data.getPart2Region();
		List<InsnNode> keptInsns = new ArrayList<>();
		BlockNode newHeader;
		if (data.getType() == SwitchStringType.SWITCH_SWITCH || data.getType() == SwitchStringType.SINGLE_SWITCH) {
			newHeader = ((SwitchRegion) part1Region).getHeader();
		} else {
			newHeader = Objects.requireNonNull(part2Region).getHeader();
		}
		// use string arg directly in switch
		InsnNode swInsn = BlockUtils.getLastInsnWithType(newHeader, InsnType.SWITCH);
		InsnNode newSwInsn = Objects.requireNonNull(swInsn).copyWithoutResult();
		newSwInsn.replaceArg(swInsn.getArg(0), data.getStrArg().duplicate());
		BlockUtils.replaceInsn(mth, newHeader, swInsn, newSwInsn);
		keptInsns.add(newSwInsn);

		SwitchRegion replaceRegion = new SwitchRegion(part1Parent, newHeader, (SwitchInsn) newSwInsn);
		for (SwitchRegion.CaseInfo caseInfo : data.getNewCases()) {
			IContainer container = caseInfo.getContainer();
			removeRedundantTrailingBreak(container, part1Region);
			if (data.isExternalFallthroughCase(container)) {
				container = addBreakForSharedTail(replaceRegion, container);
			}
			if (!data.getSharedCaseTail().isEmpty()) {
				container = addBreakForSharedTail(replaceRegion, container);
			}
			RegionUtils.visitBlocks(mth, container, b -> keptInsns.addAll(b.getInstructions()));
			replaceRegion.addCase(Collections.unmodifiableList(caseInfo.getKeys()), container);
			replaceRegion.updateParent(container, replaceRegion);
		}
		if (!part1Parent.replaceSubBlock(part1Region, replaceRegion)) {
			return false;
		}
		if (!data.getSharedCaseTail().isEmpty()) {
			Region parentRegion = (Region) part1Parent;
			normalizeSharedTailExits(mth, part1Region, data.getSharedCaseTail());
			int insertPos = parentRegion.getSubBlocks().indexOf(replaceRegion) + 1;
			for (IContainer tailContainer : data.getSharedCaseTail()) {
				parentRegion.getSubBlocks().add(insertPos++, tailContainer);
				parentRegion.updateParent(tailContainer, parentRegion);
				RegionUtils.visitBlocks(mth, tailContainer, b -> keptInsns.addAll(b.getInstructions()));
			}
		}

		// remove original code
		try {
			List<InsnNode> removeInsns = RegionUtils.collectInsns(mth, part1Region);
			if (part2Region != null) {
				removeInsns.addAll(RegionUtils.collectInsns(mth, part2Region));
				part2Region.getParent().getSubBlocks().remove(part2Region);
			}
			removeInsns.removeAll(keptInsns);
			removeInsns.forEach(i -> i.add(AFlag.REMOVE));
			// may be assigned before 1st region
			RegisterArg numArg = data.getNumArg();
			if (numArg != null) {
				for (SSAVar ssaVar : numArg.getSVar().getCodeVar().getSsaVars()) {
					InsnNode assignInsn = ssaVar.getAssignInsn();
					if (assignInsn != null) {
						assignInsn.add(AFlag.REMOVE);
					}
					for (RegisterArg useArg : ssaVar.getUseList()) {
						InsnNode parentInsn = useArg.getParentInsn();
						if (parentInsn != null) {
							parentInsn.add(AFlag.REMOVE);
						}
					}
					mth.removeSVar(ssaVar);
				}
			}
			InsnRemover.removeAllMarked(mth);
			InsnNode hashcodeInvokeInsn = data.getHashcodeInvokeInsn();
			if (hashcodeInvokeInsn.contains(AFlag.WRAPPED)
					|| BlockUtils.getBlockByInsn(mth, hashcodeInvokeInsn) != null) {
				InsnRemover.remove(mth, hashcodeInvokeInsn);
			}
		} catch (StackOverflowError | Exception e) {
			mth.addWarnComment("Failed to clean up code after switch over string restore", e);
		}
		if (!data.getSharedCaseTail().isEmpty()) {
			clearResolvedDuplicationWarning(mth);
		}
		return true;
	}

	private static IContainer addBreakForSharedTail(SwitchRegion switchRegion, IContainer container) {
		if (isSwitchExitContainer(container)) {
			return container;
		}
		if (container instanceof Region) {
			Region region = (Region) container;
			if (SwitchRegionMaker.canAppendBreak(region)) {
				region.add(SwitchRegionMaker.buildBreakContainer(switchRegion));
			}
			return region;
		}
		if (container instanceof BlockNode && !isTerminalBlock((BlockNode) container)) {
			Region region = new Region(switchRegion);
			region.add(container);
			region.add(SwitchRegionMaker.buildBreakContainer(switchRegion));
			return region;
		}
		return container;
	}

	private static void removeRedundantTrailingBreak(IContainer container, IRegion oldSwitch) {
		if (!(container instanceof Region)) {
			return;
		}
		List<IContainer> subBlocks = ((Region) container).getSubBlocks();
		if (subBlocks.size() < 2) {
			return;
		}
		IContainer trailingContainer = subBlocks.get(subBlocks.size() - 1);
		InsnNode trailingInsn = RegionUtils.getLastInsn(trailingContainer);
		if (trailingInsn == null || trailingInsn.getType() != InsnType.BREAK) {
			return;
		}
		RegionRefAttr regionRef = trailingInsn.get(AType.REGION_REF);
		if (regionRef == null || regionRef.getRegion() != oldSwitch) {
			return;
		}
		for (int i = 0; i < subBlocks.size() - 1; i++) {
			IContainer subBlock = subBlocks.get(i);
			if (subBlock instanceof IBlock && isExplicitExitInsn(RegionUtils.getLastInsn(subBlock))) {
				subBlocks.remove(subBlocks.size() - 1);
				return;
			}
		}
		IContainer previousContainer = subBlocks.get(subBlocks.size() - 2);
		if (isSwitchExitContainer(previousContainer)) {
			subBlocks.remove(subBlocks.size() - 1);
			return;
		}
		InsnNode previousInsn = RegionUtils.getLastInsn(previousContainer);
		if (previousInsn == null) {
			return;
		}
		switch (previousInsn.getType()) {
			case RETURN:
			case THROW:
			case BREAK:
			case CONTINUE:
				subBlocks.remove(subBlocks.size() - 1);
				break;

			default:
				break;
		}
	}

	private static boolean isExplicitExitInsn(@Nullable InsnNode insn) {
		if (insn == null) {
			return false;
		}
		switch (insn.getType()) {
			case RETURN:
			case THROW:
			case BREAK:
			case CONTINUE:
				return true;

			default:
				return false;
		}
	}

	private static boolean isSwitchExitContainer(IContainer container) {
		IBlock lastBlock = RegionUtils.getLastBlock(container);
		if (lastBlock instanceof BlockNode) {
			BlockNode block = (BlockNode) lastBlock;
			if (block.getCleanSuccessors().stream().anyMatch(successor -> successor.contains(AFlag.LOOP_START))
					|| block.getAll(AType.EDGE_INSN).stream()
							.anyMatch(edge -> edge.getStart() == block
									&& edge.getInsn().getType() == InsnType.CONTINUE)) {
				return true;
			}
		}
		InsnNode lastInsn = BlockUtils.getLastInsn(lastBlock);
		if (lastInsn == null) {
			return false;
		}
		switch (lastInsn.getType()) {
			case RETURN:
			case THROW:
			case BREAK:
			case CONTINUE:
				return true;

			default:
				return false;
		}
	}

	private static boolean canNormalizeSharedTailExits(
			MethodNode mth, IRegion oldSwitch, List<IContainer> sharedTail) {
		IRegion parent = oldSwitch.getParent();
		while (parent != null) {
			if (parent instanceof LoopRegion) {
				return true;
			}
			parent = parent.getParent();
		}
		boolean[] switchBreakFound = { false };
		for (IContainer tailContainer : sharedTail) {
			RegionUtils.visitBlocks(mth, tailContainer, block -> {
				for (InsnNode insn : block.getInstructions()) {
					RegionRefAttr regionRef = insn.get(AType.REGION_REF);
					if (insn.getType() == InsnType.BREAK
							&& regionRef != null
							&& regionRef.getRegion() == oldSwitch) {
						switchBreakFound[0] = true;
					}
				}
			});
		}
		return !switchBreakFound[0];
	}

	private static void normalizeSharedTailExits(
			MethodNode mth, IRegion oldSwitch, List<IContainer> sharedTail) {
		for (IContainer tailContainer : sharedTail) {
			RegionUtils.visitBlocks(mth, tailContainer, block -> {
				List<InsnNode> insns = block.getInstructions();
				for (int i = 0; i < insns.size(); i++) {
					InsnNode insn = insns.get(i);
					RegionRefAttr regionRef = insn.get(AType.REGION_REF);
					if (insn.getType() == InsnType.BREAK
							&& regionRef != null
							&& regionRef.getRegion() == oldSwitch) {
						InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
						continueInsn.add(AFlag.SYNTHETIC);
						insns.set(i, continueInsn);
					}
				}
			});
			restoreEmptyLoopContinues(tailContainer);
		}
	}

	private static void restoreEmptyLoopContinues(IContainer container) {
		if (!(container instanceof IRegion)) {
			return;
		}
		if (container instanceof IfRegion) {
			IfRegion ifRegion = (IfRegion) container;
			if (hasLoopContinueSuccessor(ifRegion)) {
				addContinueToEmptyRegion(ifRegion.getThenRegion());
				addContinueToEmptyRegion(ifRegion.getElseRegion());
			}
		}
		for (IContainer subBlock : ((IRegion) container).getSubBlocks()) {
			restoreEmptyLoopContinues(subBlock);
		}
	}

	private static boolean hasLoopContinueSuccessor(IfRegion ifRegion) {
		BlockNode conditionBlock = ListUtils.last(ifRegion.getConditionBlocks());
		if (conditionBlock == null) {
			return false;
		}
		if (conditionBlock.getCleanSuccessors().stream().anyMatch(block -> block.contains(AFlag.LOOP_START))) {
			return true;
		}
		return conditionBlock.getAll(AType.EDGE_INSN).stream()
				.anyMatch(edge -> edge.getStart() == conditionBlock
						&& edge.getInsn().getType() == InsnType.CONTINUE);
	}

	private static void addContinueToEmptyRegion(@Nullable IContainer container) {
		if (container instanceof Region && ((Region) container).getSubBlocks().isEmpty()) {
			InsnNode continueInsn = new InsnNode(InsnType.CONTINUE, 0);
			continueInsn.add(AFlag.SYNTHETIC);
			((Region) container).add(new InsnContainer(continueInsn));
		}
	}

	private static void clearResolvedDuplicationWarning(MethodNode mth) {
		Map<BlockNode, Integer> occurrences = new IdentityHashMap<>();
		RegionUtils.visitBlockNodes(mth, mth.getRegion(),
				block -> occurrences.merge(block, 1, Integer::sum));
		mth.getBasicBlocks().stream()
				.filter(block -> block.contains(AFlag.DUPLICATED))
				.filter(block -> occurrences.getOrDefault(block, 0) <= 1)
				.forEach(block -> block.remove(AFlag.DUPLICATED));
		if (mth.getBasicBlocks().stream().anyMatch(block -> block.contains(AFlag.DUPLICATED))) {
			return;
		}
		JadxCommentsAttr commentsAttr = mth.get(AType.JADX_COMMENTS);
		if (commentsAttr != null) {
			commentsAttr.getComments()
					.getOrDefault(CommentsLevel.WARN, Collections.emptySet())
					.removeIf(comment -> comment.startsWith("Code duplicated in "));
			if (commentsAttr.getComments().values().stream().allMatch(Set::isEmpty)) {
				mth.remove(AType.JADX_COMMENTS);
			}
		}
	}

	private static @Nullable Integer extractConstNumber(SwitchData switchData, @Nullable InsnNode numInsn) {
		if (numInsn == null) {
			return null;
		}
		RegisterArg numArg = switchData.getNumArg();
		RegisterArg result = numInsn.getResult();
		if (numArg == null || result == null || !numArg.sameCodeVar(result)) {
			return null;
		}
		Object constVal = numInsn.getArgsCount() == 1
				? InsnUtils.getConstValueByArg(switchData.getMth().root(), numInsn.getArg(0))
				: InsnUtils.getConstValueByInsn(switchData.getMth().root(), numInsn);
		Integer intValue = unwrapIntKey(constVal);
		if (intValue == null && numInsn.getArgsCount() == 1) {
			intValue = resolveConstNumber(switchData, numInsn.getArg(0));
		}
		return intValue;
	}

	private static @Nullable Integer resolveConstNumber(SwitchData switchData, InsnArg startArg) {
		List<InsnArg> pending = new ArrayList<>();
		pending.add(startArg);
		Set<SSAVar> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		Integer result = null;
		for (int pos = 0; pos < pending.size(); pos++) {
			InsnArg arg = pending.get(pos);
			if (arg.isLiteral()) {
				Integer value = unwrapIntKey(arg);
				if (value == null || (result != null && !result.equals(value))) {
					return null;
				}
				result = value;
				continue;
			}
			InsnNode assignInsn;
			if (arg.isRegister()) {
				SSAVar ssaVar = ((RegisterArg) arg).getSVar();
				if (ssaVar == null || !visited.add(ssaVar)) {
					continue;
				}
				assignInsn = ssaVar.getAssignInsn();
			} else if (arg.isInsnWrap()) {
				assignInsn = ((InsnWrapArg) arg).getWrapInsn();
			} else {
				return null;
			}
			if (assignInsn == null) {
				return null;
			}
			switch (assignInsn.getType()) {
				case CONST:
				case SGET:
					Integer value = unwrapIntKey(InsnUtils.getConstValueByInsn(switchData.getMth().root(), assignInsn));
					if (value == null || (result != null && !result.equals(value))) {
						return null;
					}
					result = value;
					break;

				case MOVE:
				case PHI:
					for (InsnArg insnArg : assignInsn.getArguments()) {
						pending.add(insnArg);
					}
					break;

				default:
					return null;
			}
		}
		return result;
	}

	private static Integer unwrapIntKey(Object key) {
		if (key instanceof Integer) {
			return (Integer) key;
		}
		if (key instanceof LiteralArg) {
			return (int) ((LiteralArg) key).getLiteral();
		}
		if (key instanceof FieldNode) {
			EncodedValue encodedValue = ((FieldNode) key).get(JadxAttrType.CONSTANT_VALUE);
			return unwrapIntKey(EncodedValueUtils.convertToConstValue(encodedValue));
		}
		return null;
	}

	static @Nullable InvokeNode getStrHashcodeInvokeInsn(InsnArg arg) {
		InsnNode insn = null;
		if (arg.isRegister()) {
			insn = ((RegisterArg) arg).getAssignInsn();
		} else if (arg.isInsnWrap()) {
			insn = ((InsnWrapArg) arg).getWrapInsn();
		}
		if (insn != null && insn.getType() == InsnType.INVOKE) {
			InvokeNode invInsn = (InvokeNode) insn;
			if (invInsn.getCallMth().getRawFullId().equals("java.lang.String.hashCode()I")) {
				return invInsn;
			}
		}
		return null;
	}

	private static boolean isIfStringEqualsInsn(InsnNode ifInsn) {
		if (ifInsn != null && ifInsn.getType() == InsnType.IF && ifInsn.getArgsCount() == 2) {
			InsnNode wrapped = InsnUtils.getWrappedInsn(ifInsn.getArg(0));
			return wrapped != null && wrapped.getType() == InsnType.INVOKE
					&& ((InvokeNode) wrapped).getCallMth().getRawFullId().equals("java.lang.String.equals(Ljava/lang/Object;)Z");
		}
		return false;
	}

	private static @Nullable BlockNode getOnlyOneInsnBlock(BlockNode b) {
		while (b != null) {
			int size = b.getInstructions().size();
			if (size == 0) {
				b = BlockUtils.getNextBlock(b);
				continue;
			}
			return size == 1 ? b : null;
		}
		return null;
	}

	private static @Nullable BlockNode getFirstInsnBlock(BlockNode b) {
		while (b != null) {
			if (!b.getInstructions().isEmpty()) {
				return b;
			}
			b = BlockUtils.getNextBlock(b);
		}
		return null;
	}

	private static final class SwitchData {
		private final MethodNode mth;
		private SwitchStringType type = SwitchStringType.SWITCH_SWITCH;
		// first switch/if region
		private final IRegion part1Region;
		// second switch region, null if type is SINGLE_SWITCH
		private @Nullable SwitchRegion part2Region;
		// each case is a str in part1Region, with its num or code block
		private List<CaseData> cases;
		private List<SwitchRegion.CaseInfo> newCases;
		private List<IContainer> sharedCaseTail = Collections.emptyList();
		private final Set<IContainer> externalFallthroughCases = Collections.newSetFromMap(new IdentityHashMap<>());
		private RegisterArg numArg;
		private RegisterArg strArg;
		private InsnNode hashcodeInvokeInsn;

		private SwitchData(MethodNode mth, IRegion part1Region) {
			this.mth = mth;
			this.part1Region = part1Region;
		}

		public SwitchStringType getType() {
			return type;
		}

		public void setType(SwitchStringType type) {
			this.type = type;
		}

		public List<CaseData> getCases() {
			return cases;
		}

		public void setCases(List<CaseData> cases) {
			this.cases = cases;
		}

		public List<SwitchRegion.CaseInfo> getNewCases() {
			return newCases;
		}

		public void setNewCases(List<SwitchRegion.CaseInfo> cases) {
			this.newCases = cases;
		}

		public List<IContainer> getSharedCaseTail() {
			return sharedCaseTail;
		}

		public void setSharedCaseTail(List<IContainer> sharedCaseTail) {
			this.sharedCaseTail = sharedCaseTail;
		}

		public void addExternalFallthroughCase(IContainer container) {
			externalFallthroughCases.add(container);
		}

		public boolean isExternalFallthroughCase(IContainer container) {
			return externalFallthroughCases.contains(container);
		}

		public MethodNode getMth() {
			return mth;
		}

		public IRegion getPart1Region() {
			return part1Region;
		}

		public @Nullable SwitchRegion getPart2Region() {
			return part2Region;
		}

		public void setPart2Region(@Nullable SwitchRegion part2Region) {
			this.part2Region = part2Region;
		}

		public @Nullable RegisterArg getNumArg() {
			return numArg;
		}

		public void setNumArg(RegisterArg numArg) {
			this.numArg = numArg;
		}

		public RegisterArg getStrArg() {
			return strArg;
		}

		public void setStrArg(RegisterArg strArg) {
			this.strArg = strArg;
		}

		public InsnNode getHashcodeInvokeInsn() {
			return hashcodeInvokeInsn;
		}

		public void setHashcodeInvokeInsn(InsnNode hashcodeInvokeInsn) {
			this.hashcodeInvokeInsn = hashcodeInvokeInsn;
		}
	}

	private static final class CaseData {
		private final Object strValue;
		private final BlockNode code;
		private final Integer codeNum;

		private CaseData(Object strValue, Integer codeNum, BlockNode code) {
			this.strValue = strValue;
			this.code = code;
			this.codeNum = codeNum;
		}

		public Object getStrValue() {
			return strValue;
		}

		public @Nullable BlockNode getCode() {
			return code;
		}

		public Integer getCodeNum() {
			return codeNum;
		}

		@Override
		public String toString() {
			return "CaseData{" + strValue + '}';
		}
	}

	private enum SwitchStringType {
		SINGLE_SWITCH,
		IF_SWITCH,
		SWITCH_SWITCH
	}
}
