package jadx.core.dex.visitors.ssa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.InitAtDeclareVarsAttr;
import jadx.core.dex.attributes.nodes.PhiListAttr;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlockAttr;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.visitors.JadxVisitor;
import jadx.core.dex.visitors.blocks.BlockProcessor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.exceptions.JadxRuntimeException;

@JadxVisitor(
		name = "SSATransform",
		desc = "Calculate Single Side Assign (SSA) variables",
		runAfter = BlockProcessor.class
)
public class SSATransform extends AbstractVisitor {
	private static final String KOTLIN_NULL_OUT_SPILLED_VAR =
			"kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;";

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		process(mth);
	}

	private static void process(MethodNode mth) {
		if (!mth.getSVars().isEmpty()) {
			return;
		}
		replaceKotlinSpillingArgs(mth);
		LiveVarAnalysis la = new LiveVarAnalysis(mth);
		la.runAnalysis();
		int regsCount = mth.getRegsCount();
		for (int i = 0; i < regsCount; i++) {
			placePhi(mth, i, la);
		}
		ExceptionPhiData exceptionPhiData = placeExceptionHandlerPhis(mth, la);
		renameVariables(mth, exceptionPhiData);
		exceptionPhiData.checkComplete(mth);
		fixLastAssignInTry(mth);
		removeBlockerInsns(mth);
		tryToFixUselessPhi(mth);
		markThisArgs(mth.getThisArg());
		hidePhiInsns(mth);
		removeUnusedInvokeResults(mth);
	}

	/**
	 * Exception handlers are connected to a common synthetic top splitter instead of every protected
	 * block. This keeps the region CFG manageable, but a value restored independently in coroutine
	 * resume branches no longer dominates the handler and normal SSA placement cannot see it.
	 *
	 * Recover only values which are unchanged inside every protected block. In that case the value at
	 * the end of a block is also the value visible to every throwing instruction in that block, so it
	 * is safe to use the protected blocks as the exceptional PHI inputs.
	 */
	private static ExceptionPhiData placeExceptionHandlerPhis(MethodNode mth, LiveVarAnalysis la) {
		ExceptionPhiData data = new ExceptionPhiData();
		List<ExceptionPhiCandidate> candidates = new ArrayList<>();
		BitSet singleLiteralRegs = null;
		for (ExceptionHandler handler : mth.getExceptionHandlers()) {
			if (handler.isRemoved()) {
				continue;
			}
			BlockNode handlerBlock = handler.getHandlerBlock();
			TryCatchBlockAttr tryBlock = handler.getTryBlock();
			if (handlerBlock == null || tryBlock == null) {
				continue;
			}
			List<BlockNode> sources = tryBlock.getBlocks();
			if (sources.isEmpty() || hasDuplicateBlocks(sources)) {
				continue;
			}
			int regsCount = mth.getRegsCount();
			BitSet assignedInSources = collectAssignedRegs(sources, regsCount);
			for (int regNum = 0; regNum < regsCount; regNum++) {
				if (!la.isLive(handlerBlock, regNum)
						|| hasPhiForReg(handlerBlock, regNum)
						|| hasDefinitionDominating(mth, la, handlerBlock, regNum)) {
					continue;
				}
				boolean assignedInTry = assignedInSources.get(regNum);
				boolean singleLiteral = false;
				if (assignedInTry) {
					if (singleLiteralRegs == null) {
						singleLiteralRegs = collectSingleLiteralRegs(mth);
					}
					singleLiteral = singleLiteralRegs.get(regNum);
				}
				if (!hasSafeDefinitionForAllSources(la, sources, regNum, assignedInTry, singleLiteral)) {
					continue;
				}
				candidates.add(new ExceptionPhiCandidate(handlerBlock, regNum, sources));
			}
		}
		for (ExceptionPhiCandidate candidate : resolveExceptionPhiCandidates(mth, candidates)) {
			PhiInsn phiInsn = addPhi(mth, candidate.getHandlerBlock(), candidate.getRegNum());
			candidate.getHandlerBlock().getInstructions().add(0, phiInsn);
			data.add(phiInsn, candidate.getSources());
		}
		return data;
	}

	private static boolean hasDuplicateBlocks(List<BlockNode> blocks) {
		Set<BlockNode> unique = Collections.newSetFromMap(new IdentityHashMap<>());
		for (BlockNode block : blocks) {
			if (!unique.add(block)) {
				return true;
			}
		}
		return false;
	}

	private static List<ExceptionPhiCandidate> resolveExceptionPhiCandidates(
			MethodNode mth, List<ExceptionPhiCandidate> candidates) {
		List<ExceptionPhiCandidate> resolved = new ArrayList<>(candidates);
		boolean changed;
		do {
			BitSet[] availableRegs = collectRenameAvailableRegs(mth, resolved);
			changed = false;
			Iterator<ExceptionPhiCandidate> iterator = resolved.iterator();
			while (iterator.hasNext()) {
				ExceptionPhiCandidate candidate = iterator.next();
				if (!isExceptionPhiCandidateResolvable(candidate, availableRegs)) {
					iterator.remove();
					changed = true;
				}
			}
		} while (changed);
		return resolved;
	}

	private static boolean isExceptionPhiCandidateResolvable(
			ExceptionPhiCandidate candidate, BitSet[] availableRegs) {
		int regNum = candidate.getRegNum();
		for (BlockNode source : candidate.getSources()) {
			BitSet available = availableRegs[source.getId()];
			if (available == null || !available.get(regNum)) {
				return false;
			}
		}
		return true;
	}

	private static BitSet[] collectRenameAvailableRegs(
			MethodNode mth, List<ExceptionPhiCandidate> candidates) {
		BitSet initial = new BitSet(mth.getRegsCount());
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg != null) {
			initial.set(thisArg.getRegNum());
		}
		for (RegisterArg arg : mth.getArgRegs()) {
			initial.set(arg.getRegNum());
		}

		Map<BlockNode, BitSet> candidateDefs = new IdentityHashMap<>();
		for (ExceptionPhiCandidate candidate : candidates) {
			candidateDefs.computeIfAbsent(candidate.getHandlerBlock(), k -> new BitSet())
					.set(candidate.getRegNum());
		}

		BitSet[] availableRegs = new BitSet[mth.getBasicBlocks().size()];
		Map<BlockNode, BitSet> blockStates = new IdentityHashMap<>();
		Deque<BlockNode> stack = new ArrayDeque<>();
		BlockNode enterBlock = mth.getEnterBlock();
		blockStates.put(enterBlock, initial);
		stack.push(enterBlock);
		while (!stack.isEmpty()) {
			BlockNode block = stack.pop();
			BitSet state = blockStates.get(block);
			BitSet candidateDef = candidateDefs.get(block);
			if (candidateDef != null) {
				state.or(candidateDef);
			}
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() != InsnType.PHI) {
					for (InsnArg arg : insn.getArguments()) {
						if (arg.isRegister()) {
							state.set(((RegisterArg) arg).getRegNum());
						}
					}
				}
				RegisterArg result = insn.getResult();
				if (result != null) {
					state.set(result.getRegNum());
				}
			}
			availableRegs[block.getId()] = state;
			for (BlockNode dominated : block.getDominatesOn()) {
				BitSet dominatedState = (BitSet) state.clone();
				blockStates.put(dominated, dominatedState);
				stack.push(dominated);
			}
		}
		return availableRegs;
	}

	private static boolean hasPhiForReg(BlockNode block, int regNum) {
		PhiListAttr phiList = block.get(AType.PHI_LIST);
		if (phiList == null) {
			return false;
		}
		for (PhiInsn phiInsn : phiList.getList()) {
			if (phiInsn.getResult().getRegNum() == regNum) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasDefinitionDominating(MethodNode mth, LiveVarAnalysis la, BlockNode block, int regNum) {
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg != null && thisArg.getRegNum() == regNum) {
			return true;
		}
		for (RegisterArg arg : mth.getArgRegs()) {
			if (arg.getRegNum() == regNum) {
				return true;
			}
		}
		return block.getDoms().intersects(la.getAssignBlocks(regNum));
	}

	private static boolean hasSafeDefinitionForAllSources(
			LiveVarAnalysis la, List<BlockNode> sources, int regNum,
			boolean assignedInSources, boolean singleLiteral) {
		boolean stableLiteral = !assignedInSources || singleLiteral;
		if (!stableLiteral) {
			return false;
		}
		BitSet assignBlocks = la.getAssignBlocks(regNum);
		for (BlockNode source : sources) {
			if (hasPhiForReg(source, regNum) || source.getDoms().intersects(assignBlocks)) {
				continue;
			}
			if (!la.isDefinedOnAllPaths(source, regNum)
					&& !startsWithLiteralAssign(source, regNum)) {
				return false;
			}
		}
		return true;
	}

	private static BitSet collectAssignedRegs(List<BlockNode> blocks, int regsCount) {
		BitSet assigned = new BitSet(regsCount);
		for (BlockNode block : blocks) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.PHI) {
					continue;
				}
				RegisterArg result = insn.getResult();
				if (result != null) {
					assigned.set(result.getRegNum());
				}
			}
		}
		return assigned;
	}

	private static boolean startsWithLiteralAssign(BlockNode block, int regNum) {
		for (InsnNode insn : block.getInstructions()) {
			if (insn.getType() == InsnType.PHI) {
				continue;
			}
			RegisterArg result = insn.getResult();
			return result != null
					&& result.getRegNum() == regNum
					&& insn.getType() == InsnType.CONST
					&& insn.getArgsCount() == 1
					&& insn.getArg(0).isLiteral();
		}
		return false;
	}

	/**
	 * An exception edge is attached to the synthetic try splitter, which can precede the real try
	 * entry and hide an otherwise dominating definition. Assignments inside protected blocks are
	 * normally unsafe exception-PHI inputs because an instruction can throw before the assignment.
	 * If every concrete definition of the register is the same literal, however, its value cannot
	 * change at any throw point and recovering it is safe.
	 */
	private static BitSet collectSingleLiteralRegs(MethodNode mth) {
		int regsCount = mth.getRegsCount();
		BitSet found = new BitSet(regsCount);
		BitSet unstable = new BitSet(regsCount);
		long[] literals = new long[regsCount];
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				RegisterArg result = insn.getResult();
				if (result == null || insn.getType() == InsnType.PHI) {
					continue;
				}
				int regNum = result.getRegNum();
				if (unstable.get(regNum)) {
					continue;
				}
				if (insn.getType() != InsnType.CONST
						|| insn.getArgsCount() != 1
						|| !insn.getArg(0).isLiteral()) {
					unstable.set(regNum);
					continue;
				}
				long value = ((LiteralArg) insn.getArg(0)).getLiteral();
				if (found.get(regNum) && literals[regNum] != value) {
					unstable.set(regNum);
					continue;
				}
				literals[regNum] = value;
				found.set(regNum);
			}
		}
		found.andNot(unstable);
		return found;
	}

	private static void replaceKotlinSpillingArgs(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.INVOKE
						&& insn.getArgsCount() == 1
						&& ((InvokeNode) insn).getCallMth().getRawFullId().equals(KOTLIN_NULL_OUT_SPILLED_VAR)) {
					insn.setArg(0, InsnArg.lit(0, ArgType.OBJECT));
				}
			}
		}
	}

	private static void placePhi(MethodNode mth, int regNum, LiveVarAnalysis la) {
		List<BlockNode> blocks = mth.getBasicBlocks();
		int blocksCount = blocks.size();
		BitSet hasPhi = new BitSet(blocksCount);
		BitSet processed = new BitSet(blocksCount);
		Deque<BlockNode> workList = new ArrayDeque<>();

		BitSet assignBlocks = la.getAssignBlocks(regNum);
		for (int id = assignBlocks.nextSetBit(0); id >= 0; id = assignBlocks.nextSetBit(id + 1)) {
			processed.set(id);
			workList.add(blocks.get(id));
		}
		while (!workList.isEmpty()) {
			BlockNode block = workList.pop();
			BitSet domFrontier = block.getDomFrontier();
			for (int id = domFrontier.nextSetBit(0); id >= 0; id = domFrontier.nextSetBit(id + 1)) {
				if (!hasPhi.get(id) && la.isLive(id, regNum)) {
					BlockNode df = blocks.get(id);
					PhiInsn phiInsn = addPhi(mth, df, regNum);
					df.getInstructions().add(0, phiInsn);
					hasPhi.set(id);
					if (!processed.get(id)) {
						processed.set(id);
						workList.add(df);
					}
				}
			}
		}
	}

	public static PhiInsn addPhi(MethodNode mth, BlockNode block, int regNum) {
		PhiListAttr phiList = block.get(AType.PHI_LIST);
		if (phiList == null) {
			phiList = new PhiListAttr();
			block.addAttr(phiList);
		}
		int size = block.getPredecessors().size();
		if (mth.getEnterBlock() == block) {
			RegisterArg thisArg = mth.getThisArg();
			if (thisArg != null && thisArg.getRegNum() == regNum) {
				size++;
			} else {
				for (RegisterArg arg : mth.getArgRegs()) {
					if (arg.getRegNum() == regNum) {
						size++;
						break;
					}
				}
			}
		}
		PhiInsn phiInsn = new PhiInsn(regNum, size);
		phiList.getList().add(phiInsn);
		phiInsn.setOffset(block.getStartOffset());
		return phiInsn;
	}

	private static void renameVariables(MethodNode mth, ExceptionPhiData exceptionPhiData) {
		RenameState initState = RenameState.init(mth);
		initPhiInEnterBlock(initState);
		List<NotInitializedVar> notInitialized = new ArrayList<>();

		Deque<RenameState> stack = new ArrayDeque<>();
		stack.push(initState);
		while (!stack.isEmpty()) {
			RenameState state = stack.pop();
			renameVarsInBlock(state, exceptionPhiData, notInitialized);
			for (BlockNode dominated : state.getBlock().getDominatesOn()) {
				stack.push(RenameState.copyFrom(state, dominated));
			}
		}
		reportNotInitialized(mth, notInitialized);
	}

	private static void initPhiInEnterBlock(RenameState initState) {
		PhiListAttr phiList = initState.getBlock().get(AType.PHI_LIST);
		if (phiList != null) {
			for (PhiInsn phiInsn : phiList.getList()) {
				bindPhiArg(initState, phiInsn);
			}
		}
	}

	private static void renameVarsInBlock(RenameState state, ExceptionPhiData exceptionPhiData,
			List<NotInitializedVar> notInitialized) {
		BlockNode block = state.getBlock();
		List<InsnNode> insns = block.getInstructions();
		int insnsCount = insns.size();
		for (int insnIndex = 0; insnIndex < insnsCount; insnIndex++) {
			InsnNode insn = insns.get(insnIndex);
			if (insn.getType() != InsnType.PHI) {
				int argsCount = insn.getArgsCount();
				for (int argIndex = 0; argIndex < argsCount; argIndex++) {
					InsnArg arg = insn.getArg(argIndex);
					if (!arg.isRegister()) {
						continue;
					}
					RegisterArg reg = (RegisterArg) arg;
					int regNum = reg.getRegNum();
					SSAVar var = state.getVar(regNum);
					if (var == null) {
						// TODO: in most cases issue in incorrectly attached exception handlers
						String warning = "Not initialized variable reg: " + regNum
								+ ", insn: " + insn + ", block:" + block;
						var = state.startVar(reg);
						notInitialized.add(new NotInitializedVar(warning, var));
					}
					var.use(reg);
				}
			}
			RegisterArg result = insn.getResult();
			if (result != null) {
				state.startVar(result);
			}
		}
		for (BlockNode s : block.getSuccessors()) {
			PhiListAttr phiList = s.get(AType.PHI_LIST);
			if (phiList == null) {
				continue;
			}
			for (PhiInsn phiInsn : phiList.getList()) {
				if (!exceptionPhiData.isExceptionPhi(phiInsn)) {
					bindPhiArg(state, phiInsn);
				}
			}
		}
		for (PhiInsn phiInsn : exceptionPhiData.getForSource(block)) {
			bindPhiArg(state, phiInsn);
		}
	}

	private static void reportNotInitialized(MethodNode mth, List<NotInitializedVar> notInitialized) {
		for (NotInitializedVar entry : notInitialized) {
			Set<SSAVar> visited = Collections.newSetFromMap(new IdentityHashMap<>());
			if (!isUsedOnlyInDeadMoves(entry.getVar(), visited)) {
				mth.addWarnComment(entry.getMessage());
			}
		}
	}

	static boolean isUsedOnlyInDeadMoves(SSAVar var, Set<SSAVar> visited) {
		if (!visited.add(var)) {
			return false;
		}
		for (RegisterArg useArg : var.getUseList()) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null || useInsn.getType() != InsnType.MOVE) {
				return false;
			}
			RegisterArg result = useInsn.getResult();
			if (result == null || result.getSVar() == null
					|| !isUsedOnlyInDeadMoves(result.getSVar(), visited)) {
				return false;
			}
		}
		return true;
	}

	private static final class NotInitializedVar {
		private final String message;
		private final SSAVar var;

		private NotInitializedVar(String message, SSAVar var) {
			this.message = message;
			this.var = var;
		}

		public String getMessage() {
			return message;
		}

		public SSAVar getVar() {
			return var;
		}
	}

	private static final class ExceptionPhiData {
		private final Map<BlockNode, List<PhiInsn>> bySource = new HashMap<>();
		private final Map<PhiInsn, Integer> expectedArgs = new IdentityHashMap<>();

		public void add(PhiInsn phiInsn, List<BlockNode> sources) {
			expectedArgs.put(phiInsn, sources.size());
			for (BlockNode source : sources) {
				bySource.computeIfAbsent(source, k -> new ArrayList<>()).add(phiInsn);
			}
		}

		public boolean isExceptionPhi(PhiInsn phiInsn) {
			return expectedArgs.containsKey(phiInsn);
		}

		public List<PhiInsn> getForSource(BlockNode source) {
			return bySource.getOrDefault(source, List.of());
		}

		public void checkComplete(MethodNode mth) {
			for (Map.Entry<PhiInsn, Integer> entry : expectedArgs.entrySet()) {
				PhiInsn phiInsn = entry.getKey();
				int expected = entry.getValue();
				if (phiInsn.getArgsCount() != expected) {
					mth.addWarnComment("Incomplete exception PHI for reg: "
							+ phiInsn.getResult().getRegNum() + ", expected: " + expected
							+ ", actual: " + phiInsn.getArgsCount());
				} else {
					InitAtDeclareVarsAttr initVars = mth.get(AType.INIT_AT_DECLARE_VARS);
					if (initVars == null) {
						initVars = new InitAtDeclareVarsAttr();
						mth.addAttr(initVars);
					}
					initVars.add(phiInsn.getResult().getRegNum());
				}
			}
		}
	}

	private static final class ExceptionPhiCandidate {
		private final BlockNode handlerBlock;
		private final int regNum;
		private final List<BlockNode> sources;

		private ExceptionPhiCandidate(BlockNode handlerBlock, int regNum, List<BlockNode> sources) {
			this.handlerBlock = handlerBlock;
			this.regNum = regNum;
			this.sources = sources;
		}

		public BlockNode getHandlerBlock() {
			return handlerBlock;
		}

		public int getRegNum() {
			return regNum;
		}

		public List<BlockNode> getSources() {
			return sources;
		}
	}

	private static void bindPhiArg(RenameState state, PhiInsn phiInsn) {
		int regNum = phiInsn.getResult().getRegNum();
		SSAVar var = state.getVar(regNum);
		if (var == null) {
			return;
		}
		RegisterArg arg = phiInsn.bindArg(state.getBlock());
		var.use(arg);
		var.addUsedInPhi(phiInsn);
	}

	/**
	 * Fix last try/catch assign instruction
	 */
	private static void fixLastAssignInTry(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiList = block.get(AType.PHI_LIST);
			if (phiList != null) {
				ExcHandlerAttr handlerAttr = block.get(AType.EXC_HANDLER);
				if (handlerAttr != null) {
					for (PhiInsn phi : phiList.getList()) {
						fixPhiInTryCatch(mth, phi, handlerAttr);
					}
				}
			}
		}
	}

	private static void fixPhiInTryCatch(MethodNode mth, PhiInsn phi, ExcHandlerAttr handlerAttr) {
		int argsCount = phi.getArgsCount();
		int k = 0;
		while (k < argsCount) {
			RegisterArg arg = phi.getArg(k);
			if (shouldSkipInsnResult(mth, arg.getAssignInsn(), handlerAttr)) {
				phi.removeArg(arg);
				argsCount--;
			} else {
				k++;
			}
		}
		if (phi.getArgsCount() == 0) {
			throw new JadxRuntimeException("PHI empty after try-catch fix!");
		}
	}

	private static boolean shouldSkipInsnResult(MethodNode mth, InsnNode insn, ExcHandlerAttr handlerAttr) {
		if (insn != null
				&& insn.getResult() != null
				&& insn.contains(AFlag.TRY_LEAVE)) {
			CatchAttr catchAttr = BlockUtils.getCatchAttrForInsn(mth, insn);
			return catchAttr != null && catchAttr.getHandlers().contains(handlerAttr.getHandler());
		}
		return false;
	}

	private static boolean removeBlockerInsns(MethodNode mth) {
		boolean removed = false;
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiList = block.get(AType.PHI_LIST);
			if (phiList == null) {
				continue;
			}
			// check if args must be removed
			for (PhiInsn phi : phiList.getList()) {
				for (int i = 0; i < phi.getArgsCount(); i++) {
					RegisterArg arg = phi.getArg(i);
					InsnNode parentInsn = arg.getAssignInsn();
					if (parentInsn != null && parentInsn.contains(AFlag.REMOVE)) {
						phi.removeArg(arg);
						InsnRemover.remove(mth, block, parentInsn);
						removed = true;
					}
				}
			}
		}
		return removed;
	}

	private static void tryToFixUselessPhi(MethodNode mth) {
		int k = 0;
		int maxTries = mth.getSVars().size() * 2;
		while (fixUselessPhi(mth)) {
			if (k++ > maxTries) {
				throw new JadxRuntimeException("Phi nodes fix limit reached!");
			}
		}
	}

	private static boolean fixUselessPhi(MethodNode mth) {
		boolean changed = false;
		List<PhiInsn> insnToRemove = new ArrayList<>();
		for (SSAVar var : mth.getSVars()) {
			// phi result not used
			if (var.getUseCount() == 0) {
				InsnNode assignInsn = var.getAssign().getParentInsn();
				if (assignInsn != null && assignInsn.getType() == InsnType.PHI) {
					insnToRemove.add((PhiInsn) assignInsn);
					changed = true;
				}
			}
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiList = block.get(AType.PHI_LIST);
			if (phiList == null) {
				continue;
			}
			Iterator<PhiInsn> it = phiList.getList().iterator();
			while (it.hasNext()) {
				PhiInsn phi = it.next();
				if (fixPhiWithSameArgs(mth, block, phi)) {
					it.remove();
					changed = true;
				}
			}
		}
		removePhiList(mth, insnToRemove);
		return changed;
	}

	private static boolean fixPhiWithSameArgs(MethodNode mth, BlockNode block, PhiInsn phi) {
		if (phi.getArgsCount() == 0) {
			for (RegisterArg useArg : phi.getResult().getSVar().getUseList()) {
				InsnNode useInsn = useArg.getParentInsn();
				if (useInsn != null && useInsn.getType() == InsnType.PHI) {
					phi.removeArg(useArg);
				}
			}
			InsnRemover.remove(mth, block, phi);
			return true;
		}
		boolean allSame = phi.getArgsCount() == 1 || isSameArgs(phi);
		if (allSame) {
			return replacePhiWithMove(mth, block, phi, phi.getArg(0));
		}
		SSAVar sameVar = isSameMove(phi);
		if (sameVar != null) {
			RegisterArg sameArg = sameVar.getAssign().duplicate();
			if (inlinePhiInsn(mth, block, phi, sameArg)) {
				for (InsnArg arg : phi.getArguments()) {
					InsnNode moveInsn = ((RegisterArg) arg).getAssignInsn();
					if (moveInsn != null) {
						moveInsn.add(AFlag.REMOVE);
						InsnRemover.remove(mth, moveInsn);
					}
				}
				return true;
			}
		}
		return false;
	}

	private static boolean isSameArgs(PhiInsn phi) {
		boolean allSame = true;
		SSAVar var = null;
		for (int i = 0; i < phi.getArgsCount(); i++) {
			RegisterArg arg = phi.getArg(i);
			if (var == null) {
				var = arg.getSVar();
			} else if (var != arg.getSVar()) {
				allSame = false;
				break;
			}
		}
		return allSame;
	}

	private static SSAVar isSameMove(PhiInsn phi) {
		SSAVar var = null;
		int argsCount = phi.getArgsCount();
		for (int i = 0; i < argsCount; i++) {
			RegisterArg arg = phi.getArg(i);
			if (arg.getSVar().getUseCount() != 1) {
				return null;
			}
			InsnNode assignInsn = arg.getAssignInsn();
			if (assignInsn == null || assignInsn.getType() != InsnType.MOVE) {
				return null;
			}
			InsnArg moveArg = assignInsn.getArg(0);
			if (!moveArg.isRegister()) {
				return null;
			}
			SSAVar moveVar = ((RegisterArg) moveArg).getSVar();
			if (var == null) {
				var = moveVar;
			} else if (var != moveVar) {
				return null;
			}
		}
		return var;
	}

	private static boolean removePhiList(MethodNode mth, List<PhiInsn> insnToRemove) {
		for (BlockNode block : mth.getBasicBlocks()) {
			PhiListAttr phiList = block.get(AType.PHI_LIST);
			if (phiList == null) {
				continue;
			}
			List<PhiInsn> list = phiList.getList();
			for (PhiInsn phiInsn : insnToRemove) {
				if (list.remove(phiInsn)) {
					for (InsnArg arg : phiInsn.getArguments()) {
						if (arg == null) {
							continue;
						}
						SSAVar sVar = ((RegisterArg) arg).getSVar();
						if (sVar != null) {
							sVar.removeUsedInPhi(phiInsn);
						}
					}
					InsnRemover.remove(mth, block, phiInsn);
				}
			}
			if (list.isEmpty()) {
				block.remove(AType.PHI_LIST);
			}
		}
		insnToRemove.clear();
		return true;
	}

	private static boolean replacePhiWithMove(MethodNode mth, BlockNode block, PhiInsn phi, RegisterArg arg) {
		List<InsnNode> insns = block.getInstructions();
		int phiIndex = InsnList.getIndex(insns, phi);
		if (phiIndex == -1) {
			return false;
		}
		SSAVar assign = phi.getResult().getSVar();
		SSAVar argVar = arg.getSVar();
		if (argVar != null) {
			argVar.removeUse(arg);
			argVar.removeUsedInPhi(phi);
		}
		// try inline
		if (inlinePhiInsn(mth, block, phi, phi.getArg(0))) {
			insns.remove(phiIndex);
		} else {
			assign.removeUsedInPhi(phi);

			InsnNode m = new InsnNode(InsnType.MOVE, 1);
			m.add(AFlag.SYNTHETIC);
			m.setResult(phi.getResult());
			m.addArg(arg);
			arg.getSVar().use(arg);
			insns.set(phiIndex, m);
		}
		return true;
	}

	private static boolean inlinePhiInsn(MethodNode mth, BlockNode block, PhiInsn phi, RegisterArg inlineArg) {
		SSAVar resVar = phi.getResult().getSVar();
		if (resVar == null) {
			return false;
		}
		if (inlineArg.getSVar() == null) {
			return false;
		}
		List<RegisterArg> useList = resVar.getUseList();
		for (RegisterArg useArg : new ArrayList<>(useList)) {
			InsnNode useInsn = useArg.getParentInsn();
			if (useInsn == null || useInsn == phi) {
				return false;
			}
			if (useArg.getRegNum() == inlineArg.getRegNum()) {
				// replace SSAVar in 'useArg' to SSAVar from 'arg'
				// no need to replace whole RegisterArg
				useArg.getSVar().removeUse(useArg);
				inlineArg.getSVar().use(useArg);
			} else {
				if (!useInsn.replaceArg(useArg, inlineArg)) {
					return false;
				}
			}
		}
		if (block.contains(AType.EXC_HANDLER)) {
			// don't inline into exception handler
			InsnNode assignInsn = inlineArg.getAssignInsn();
			if (assignInsn != null && !assignInsn.isConstInsn()) {
				assignInsn.add(AFlag.DONT_INLINE);
			}
		}
		InsnRemover.unbindInsn(mth, phi);
		return true;
	}

	private static void markThisArgs(RegisterArg thisArg) {
		if (thisArg != null) {
			markOneArgAsThis(thisArg);
			thisArg.getSVar().getUseList().forEach(SSATransform::markOneArgAsThis);
		}
	}

	private static void markOneArgAsThis(RegisterArg arg) {
		if (arg == null) {
			return;
		}
		arg.add(AFlag.THIS);
		arg.add(AFlag.IMMUTABLE_TYPE);
		// mark all moved 'this'
		InsnNode parentInsn = arg.getParentInsn();
		if (parentInsn != null
				&& parentInsn.getType() == InsnType.MOVE
				&& parentInsn.getArg(0) == arg) {
			RegisterArg resArg = parentInsn.getResult();
			if (resArg.getRegNum() != arg.getRegNum()
					&& !resArg.getSVar().isUsedInPhi()) {
				markThisArgs(resArg);
				parentInsn.add(AFlag.DONT_GENERATE);
			}
		}
	}

	private static void hidePhiInsns(MethodNode mth) {
		for (BlockNode block : mth.getBasicBlocks()) {
			block.getInstructions().removeIf(insn -> insn.getType() == InsnType.PHI);
		}
	}

	private static void removeUnusedInvokeResults(MethodNode mth) {
		Iterator<SSAVar> it = mth.getSVars().iterator();
		while (it.hasNext()) {
			SSAVar ssaVar = it.next();
			if (ssaVar.getUseCount() == 0) {
				InsnNode parentInsn = ssaVar.getAssign().getParentInsn();
				if (parentInsn != null && parentInsn.getType() == InsnType.INVOKE) {
					parentInsn.setResult(null);
					it.remove();
				}
			}
		}
	}
}
