package jadx.core.dex.visitors;

import java.util.ArrayDeque;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.CodeFormat;
import jadx.api.plugins.input.data.ICatch;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.ITry;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.InvalidMoveResultAttr;
import jadx.core.dex.attributes.nodes.JumpInfo;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.FillArrayData;
import jadx.core.dex.instructions.FillArrayInsn;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.GotoNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.MoveResultNode;
import jadx.core.dex.instructions.SwitchData;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.java.JsrNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.blocks.BlockSplitter;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.exceptions.JadxRuntimeException;

@JadxVisitor(
		name = "Process Instructions Visitor",
		desc = "Init instructions info",
		runBefore = {
				BlockSplitter.class
		}
)
public class ProcessInstructionsVisitor extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}

		initJumps(mth, mth.getInstructions());
	}

	private static void initJumps(MethodNode mth, InsnNode[] insnByOffset) {
		ICodeReader codeReader = mth.getCodeReader();
		if (codeReader != null && codeReader.getCodeFormat() == CodeFormat.DEX) {
			validateDexSpecialInstructions(codeReader, insnByOffset);
		}
		for (int offset = 0; offset < insnByOffset.length; offset++) {
			InsnNode insn = insnByOffset[offset];
			if (insn == null) {
				continue;
			}
			switch (insn.getType()) {
				case SWITCH:
					SwitchInsn sw = (SwitchInsn) insn;
					if (sw.needData()) {
						attachSwitchData(insnByOffset, offset, sw);
					}
					int defCaseOffset = sw.getDefaultCaseOffset();
					if (defCaseOffset != -1) {
						addJump(insnByOffset, offset, defCaseOffset);
					}
					for (int target : sw.getTargets()) {
						addJump(insnByOffset, offset, target);
					}
					break;

				case IF:
					int next = getNextInsnOffset(insnByOffset, offset);
					if (next != -1) {
						addJump(insnByOffset, offset, next);
					}
					addJump(insnByOffset, offset, ((IfNode) insn).getTarget());
					break;

				case GOTO:
					addJump(insnByOffset, offset, ((GotoNode) insn).getTarget());
					break;

				case JAVA_JSR:
					addJump(insnByOffset, offset, ((JsrNode) insn).getTarget());
					int onRet = getNextInsnOffset(insnByOffset, offset);
					if (onRet != -1) {
						addJump(insnByOffset, offset, onRet);
					}
					break;

				case INVOKE:
					if (insn.getResult() == null) {
						ArgType retType = ((BaseInvokeNode) insn).getCallMth().getReturnType();
						mergeMoveResult(insnByOffset, offset, insn, retType);
					}
					break;

				case STR_CONCAT:
					// invoke-custom with string concatenation translated directly to STR_CONCAT, merge next move-result
					if (insn.getResult() == null) {
						mergeMoveResult(insnByOffset, offset, insn, ArgType.STRING);
					}
					break;

				case FILLED_NEW_ARRAY:
					ArgType arrType = ((FilledNewArrayNode) insn).getArrayType();
					mergeMoveResult(insnByOffset, offset, insn, arrType);
					break;

				case FILL_ARRAY:
					FillArrayInsn fillArrayInsn = (FillArrayInsn) insn;
					int target = fillArrayInsn.getTarget();
					InsnNode arrDataInsn = getInsnAtOffset(insnByOffset, target);
					if (arrDataInsn != null && arrDataInsn.getType() == InsnType.FILL_ARRAY_DATA) {
						fillArrayInsn.setArrayData((FillArrayData) arrDataInsn);
						removeInsn(insnByOffset, arrDataInsn);
					} else {
						throw new JadxRuntimeException("Payload for fill-array not found at " + InsnUtils.formatOffset(target));
					}
					break;

				default:
					break;
			}
		}
	}

	private static void attachSwitchData(InsnNode[] insnByOffset, int offset, SwitchInsn sw) {
		int nextInsnOffset = getNextInsnOffset(insnByOffset, offset);
		int dataTarget = sw.getDataTarget();
		InsnNode switchDataInsn = getInsnAtOffset(insnByOffset, dataTarget);
		if (switchDataInsn != null && switchDataInsn.getType() == InsnType.SWITCH_DATA) {
			SwitchData data = (SwitchData) switchDataInsn;
			data.fixTargets(offset);
			sw.attachSwitchData(data, nextInsnOffset);
			removeInsn(insnByOffset, switchDataInsn);
		} else {
			throw new JadxRuntimeException("Payload for switch not found at " + InsnUtils.formatOffset(dataTarget));
		}
	}

	private static void mergeMoveResult(InsnNode[] insnByOffset, int offset, InsnNode insn, ArgType resType) {
		int nextInsnOffset = getNextInsnOffset(insnByOffset, offset);
		if (nextInsnOffset == -1) {
			return;
		}
		InsnNode nextInsn = insnByOffset[nextInsnOffset];
		if (nextInsn.getType() != InsnType.MOVE_RESULT) {
			return;
		}
		RegisterArg moveRes = nextInsn.getResult();
		insn.setResult(moveRes.duplicate(resType));
		insn.copyAttributesFrom(nextInsn);
		if (nextInsn instanceof MoveResultNode) {
			MoveResultNode moveResultNode = (MoveResultNode) nextInsn;
			if (!moveResultNode.isOpcodeCompatible(resType)) {
				insn.addAttr(new InvalidMoveResultAttr(moveResultNode.getMoveResultOpcode(), resType));
			}
		}
		removeInsn(insnByOffset, nextInsn);
	}

	static void addJump(InsnNode[] insnByOffset, int offset, int target) {
		try {
			insnByOffset[target].addAttr(AType.JUMP, new JumpInfo(offset, target));
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to set jump: "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(target), e);
		}
	}

	private static void validateDexSpecialInstructions(ICodeReader codeReader, InsnNode[] insnByOffset) {
		validateDexMethodHasInstructions(insnByOffset);
		boolean[] handlerStarts = new boolean[insnByOffset.length];
		boolean[] payloadStarts = new boolean[insnByOffset.length];
		boolean hasPayload = false;
		boolean hasMoveException = false;
		for (int offset = 0; offset < insnByOffset.length; offset++) {
			InsnNode insn = insnByOffset[offset];
			if (insn != null && (insn.getType() == InsnType.SWITCH_DATA || insn.getType() == InsnType.FILL_ARRAY_DATA)) {
				payloadStarts[offset] = true;
				hasPayload = true;
			}
		}
		for (ITry tryBlock : codeReader.getTries()) {
			ICatch catchData = tryBlock.getCatch();
			for (int handler : catchData.getHandlers()) {
				markHandlerStart(insnByOffset, handlerStarts, payloadStarts, handler);
			}
			markHandlerStart(insnByOffset, handlerStarts, payloadStarts, catchData.getCatchAllHandler());
		}
		for (int offset = 0; offset < insnByOffset.length; offset++) {
			InsnNode insn = insnByOffset[offset];
			if (insn != null && insn.getType() == InsnType.MOVE_EXCEPTION) {
				hasMoveException = true;
				if (!handlerStarts[offset] || offset == 0) {
					throw new JadxRuntimeException("move-exception is not the first instruction of an exception handler: "
							+ InsnUtils.formatOffset(offset));
				}
			}
			if (insn instanceof SwitchInsn) {
				SwitchInsn sw = (SwitchInsn) insn;
				if (sw.needData()) {
					attachSwitchData(insnByOffset, offset, sw);
				}
				for (int target : sw.getTargets()) {
					validateDexBranchTarget(insnByOffset, payloadStarts, offset, target);
				}
			} else if (insn instanceof GotoNode) {
				validateDexBranchTarget(insnByOffset, payloadStarts, offset, ((GotoNode) insn).getTarget());
			}
		}
		if (hasPayload || hasMoveException || mayFallOffMethodEnd(insnByOffset, payloadStarts)) {
			validateDexSpecialReachability(insnByOffset, payloadStarts, handlerStarts);
		}
	}

	static void validateDexMethodHasInstructions(InsnNode[] insnByOffset) {
		if (insnByOffset.length == 0) {
			throw new JadxRuntimeException("Control flow exits empty method without return or throw");
		}
	}

	private static boolean mayFallOffMethodEnd(InsnNode[] insnByOffset, boolean[] payloadStarts) {
		for (int offset = insnByOffset.length - 1; offset >= 0; offset--) {
			InsnNode insn = insnByOffset[offset];
			if (insn != null && !payloadStarts[offset]) {
				InsnType type = insn.getType();
				return type != InsnType.RETURN && type != InsnType.THROW && type != InsnType.GOTO;
			}
		}
		return false;
	}

	private static void validateDexSpecialReachability(InsnNode[] insnByOffset, boolean[] payloadStarts, boolean[] handlerStarts) {
		boolean[] visited = new boolean[insnByOffset.length];
		ArrayDeque<Integer> pending = new ArrayDeque<>();
		queueDexOffset(pending, visited, 0);
		for (int offset = 0; offset < handlerStarts.length; offset++) {
			if (handlerStarts[offset]) {
				queueDexOffset(pending, visited, offset);
			}
		}
		while (!pending.isEmpty()) {
			int offset = pending.removeFirst();
			if (payloadStarts[offset]) {
				throw new JadxRuntimeException("Control flow reaches data payload: " + InsnUtils.formatOffset(offset));
			}
			InsnNode insn = insnByOffset[offset];
			if (insn == null) {
				continue;
			}
			switch (insn.getType()) {
				case GOTO:
					queueDexOffset(pending, visited, ((GotoNode) insn).getTarget());
					break;
				case IF:
					queueDexOffset(pending, visited, ((IfNode) insn).getTarget());
					queueDexFallthrough(pending, visited, insnByOffset, payloadStarts, offset);
					break;
				case SWITCH:
					for (int target : ((SwitchInsn) insn).getTargets()) {
						queueDexOffset(pending, visited, target);
					}
					queueDexFallthrough(pending, visited, insnByOffset, payloadStarts, offset);
					break;
				case RETURN:
				case THROW:
					break;
				default:
					queueDexFallthrough(pending, visited, insnByOffset, payloadStarts, offset);
					break;
			}
		}
	}

	private static void queueDexFallthrough(ArrayDeque<Integer> pending, boolean[] visited,
			InsnNode[] insnByOffset, boolean[] payloadStarts, int offset) {
		int next = nextDexCodeOffset(insnByOffset, payloadStarts, offset);
		if (next == -1) {
			throw new JadxRuntimeException("Control flow exits method without return or throw: " + InsnUtils.formatOffset(offset));
		}
		InsnNode nextInsn = insnByOffset[next];
		if (nextInsn != null && nextInsn.getType() == InsnType.MOVE_EXCEPTION) {
			throw new JadxRuntimeException("Control flow falls through to move-exception: "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(next));
		}
		queueDexOffset(pending, visited, next);
	}

	private static int nextDexCodeOffset(InsnNode[] insnByOffset, boolean[] payloadStarts, int offset) {
		for (int next = offset + 1; next < insnByOffset.length; next++) {
			if (insnByOffset[next] != null || payloadStarts[next]) {
				return next;
			}
		}
		return -1;
	}

	private static void queueDexOffset(ArrayDeque<Integer> pending, boolean[] visited, int offset) {
		if (offset >= 0 && offset < visited.length && !visited[offset]) {
			visited[offset] = true;
			pending.add(offset);
		}
	}

	static void markHandlerStart(InsnNode[] insnByOffset, boolean[] handlerStarts, boolean[] payloadStarts, int handler) {
		if (handler == -1) {
			return; // no catch-all handler
		}
		if (handler < 0 || handler >= handlerStarts.length) {
			throw new JadxRuntimeException("Exception handler target is outside method: " + InsnUtils.formatOffset(handler));
		}
		if (insnByOffset[handler] == null) {
			throw new JadxRuntimeException("Exception handler target is not an instruction: " + InsnUtils.formatOffset(handler));
		}
		handlerStarts[handler] = true;
		if (payloadStarts[handler]) {
			throw new JadxRuntimeException("Exception handler targets data payload: " + InsnUtils.formatOffset(handler));
		}
		if (insnByOffset[handler] instanceof MoveResultNode) {
			throw new JadxRuntimeException("Exception handler targets move-result: " + InsnUtils.formatOffset(handler));
		}
	}

	static void validateDexBranchTarget(InsnNode[] insnByOffset, boolean[] payloadStarts, int offset, int target) {
		if (target < 0 || target >= insnByOffset.length) {
			throw new JadxRuntimeException("Branch target is outside method: "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(target));
		}
		if (payloadStarts[target]) {
			throw new JadxRuntimeException("Branch targets data payload: "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(target));
		}
		InsnNode targetInsn = insnByOffset[target];
		if (targetInsn == null) {
			throw new JadxRuntimeException("Branch target is not an instruction: "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(target));
		}
		if (targetInsn instanceof MoveResultNode || targetInsn.getType() == InsnType.MOVE_EXCEPTION) {
			String targetName = targetInsn instanceof MoveResultNode ? "move-result" : "move-exception";
			throw new JadxRuntimeException("Branch targets " + targetName + ": "
					+ InsnUtils.formatOffset(offset) + " -> " + InsnUtils.formatOffset(target));
		}
	}

	public static int getNextInsnOffset(InsnNode[] insnByOffset, int offset) {
		int len = insnByOffset.length;
		for (int i = offset + 1; i < len; i++) {
			InsnNode insnNode = insnByOffset[i];
			if (insnNode != null && insnNode.getType() != InsnType.NOP) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	private static InsnNode getInsnAtOffset(InsnNode[] insnByOffset, int offset) {
		int len = insnByOffset.length;
		for (int i = offset; i < len; i++) {
			InsnNode insnNode = insnByOffset[i];
			if (insnNode != null && insnNode.getType() != InsnType.NOP) {
				return insnNode;
			}
		}
		return null;
	}

	private static void removeInsn(InsnNode[] insnByOffset, InsnNode insn) {
		insnByOffset[insn.getOffset()] = null;
	}
}
