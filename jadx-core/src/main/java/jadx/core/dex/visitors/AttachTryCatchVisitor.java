package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.input.data.CodeFormat;
import jadx.api.plugins.input.data.ICatch;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.ITry;
import jadx.api.plugins.utils.Utils;
import jadx.core.Consts;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.trycatch.CatchAttr;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.exceptions.JadxRuntimeException;

import static jadx.core.dex.visitors.ProcessInstructionsVisitor.getNextInsnOffset;

@JadxVisitor(
		name = "Attach Try/Catch Visitor",
		desc = "Attach try/catch info to instructions",
		runBefore = {
				ProcessInstructionsVisitor.class
		}
)
public class AttachTryCatchVisitor extends AbstractVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(AttachTryCatchVisitor.class);

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		ICodeReader codeReader = mth.getCodeReader();
		initTryCatches(mth, mth.getInstructions(), codeReader.getTries(), codeReader.getCodeFormat() == CodeFormat.DEX);
	}

	private static void initTryCatches(MethodNode mth, InsnNode[] insnByOffset, List<ITry> tries, boolean dexCode) {
		if (tries.isEmpty()) {
			return;
		}
		if (Consts.DEBUG_EXC_HANDLERS) {
			LOG.debug("Raw try blocks in {}", mth);
			tries.forEach(tryData -> LOG.debug(" - {}", tryData));
		}
		for (ITry tryData : tries) {
			if (dexCode) {
				validateDexTry(tryData, insnByOffset);
			}
			List<ExceptionHandler> handlers = convertToHandlers(mth, tryData.getCatch(), insnByOffset);
			if (handlers.isEmpty()) {
				continue;
			}
			markTryBounds(insnByOffset, tryData, CatchAttr.build(handlers));
		}
	}

	private static void validateDexTry(ITry tryData, InsnNode[] insnByOffset) {
		int start = tryData.getStartOffset();
		long endExclusive = (long) tryData.getEndOffset() + 1;
		if (start < 0 || start >= insnByOffset.length) {
			throw new JadxRuntimeException("Try range starts outside method: " + start);
		}
		if (endExclusive <= start || endExclusive > insnByOffset.length) {
			throw new JadxRuntimeException("Try range ends outside method: " + endExclusive);
		}
		if (insnByOffset[start] == null) {
			throw new JadxRuntimeException("Try range starts inside instruction: " + start);
		}
		if (endExclusive < insnByOffset.length && insnByOffset[(int) endExclusive] == null) {
			throw new JadxRuntimeException("Try range ends inside instruction: " + endExclusive);
		}
		ICatch catchData = tryData.getCatch();
		for (int handler : catchData.getHandlers()) {
			validateDexHandler(handler, insnByOffset);
		}
		int catchAll = catchData.getCatchAllHandler();
		if (catchAll != -1) {
			validateDexHandler(catchAll, insnByOffset);
		}
	}

	private static void validateDexHandler(int handler, InsnNode[] insnByOffset) {
		if (handler < 0 || handler >= insnByOffset.length) {
			throw new JadxRuntimeException("Exception handler target is outside method: " + handler);
		}
		if (insnByOffset[handler] == null) {
			throw new JadxRuntimeException("Exception handler target is not an instruction: " + handler);
		}
	}

	private static void markTryBounds(InsnNode[] insnByOffset, ITry aTry, CatchAttr catchAttr) {
		int offset = aTry.getStartOffset();
		int end = aTry.getEndOffset();

		boolean tryBlockStarted = false;
		InsnNode insn = null;
		while (offset <= end) {
			InsnNode insnAtOffset = insnByOffset[offset];
			if (insnAtOffset != null) {
				insn = insnAtOffset;
				attachCatchAttr(catchAttr, insn);
				if (!tryBlockStarted) {
					insn.add(AFlag.TRY_ENTER);
					tryBlockStarted = true;
				}
			}
			offset = getNextInsnOffset(insnByOffset, offset);
			if (offset == -1) {
				break;
			}
		}
		if (tryBlockStarted) {
			insn.add(AFlag.TRY_LEAVE);
		} else {
			// no instructions found in range -> add nop at start offset
			InsnNode nop = insertNOP(insnByOffset, aTry.getStartOffset());
			nop.add(AFlag.TRY_ENTER);
			nop.add(AFlag.TRY_LEAVE);
			nop.addAttr(catchAttr);
		}
	}

	private static void attachCatchAttr(CatchAttr catchAttr, InsnNode insn) {
		CatchAttr existAttr = insn.get(AType.EXC_CATCH);
		if (existAttr != null) {
			// merge handlers
			List<ExceptionHandler> handlers = Utils.concat(existAttr.getHandlers(), catchAttr.getHandlers());
			insn.addAttr(CatchAttr.build(handlers));
		} else {
			insn.addAttr(catchAttr);
		}
	}

	private static List<ExceptionHandler> convertToHandlers(MethodNode mth, ICatch catchBlock, InsnNode[] insnByOffset) {
		int[] handlerOffsetArr = catchBlock.getHandlers();
		String[] handlerTypes = catchBlock.getTypes();

		int handlersCount = handlerOffsetArr.length;
		List<ExceptionHandler> list = new ArrayList<>(handlersCount);
		for (int i = 0; i < handlersCount; i++) {
			int handlerOffset = handlerOffsetArr[i];
			ClassInfo type = ClassInfo.fromName(mth.root(), handlerTypes[i]);
			Utils.addToList(list, createHandler(mth, insnByOffset, handlerOffset, type));
		}
		int allHandlerOffset = catchBlock.getCatchAllHandler();
		if (allHandlerOffset >= 0) {
			Utils.addToList(list, createHandler(mth, insnByOffset, allHandlerOffset, null));
		}
		return list;
	}

	@Nullable
	private static ExceptionHandler createHandler(MethodNode mth, InsnNode[] insnByOffset, int handlerOffset, @Nullable ClassInfo type) {
		InsnNode insn = insnByOffset[handlerOffset];
		if (insn != null) {
			ExcHandlerAttr excHandlerAttr = insn.get(AType.EXC_HANDLER);
			if (excHandlerAttr != null) {
				ExceptionHandler handler = excHandlerAttr.getHandler();
				if (handler.addCatchType(mth, type)) {
					// exist handler updated (assume from same try block) - don't add again
					return null;
				}
				// same handler (can be used in different try blocks)
				return handler;
			}
		} else {
			insn = insertNOP(insnByOffset, handlerOffset);
		}
		ExceptionHandler handler = ExceptionHandler.build(mth, handlerOffset, type);
		mth.addExceptionHandler(handler);
		insn.addAttr(new ExcHandlerAttr(handler));
		return handler;
	}

	private static InsnNode insertNOP(InsnNode[] insnByOffset, int offset) {
		InsnNode nop = new InsnNode(InsnType.NOP, 0);
		nop.setOffset(offset);
		nop.add(AFlag.SYNTHETIC);
		insnByOffset[offset] = nop;
		return nop;
	}
}
