package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.plugins.input.data.CodeFormat;
import jadx.api.plugins.input.data.ICatch;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.ITry;
import jadx.api.plugins.input.data.impl.CatchData;
import jadx.api.plugins.input.data.impl.TryData;
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
		CodeFormat codeFormat = codeReader.getCodeFormat();
		List<ITry> tries = codeReader.getTries();
		if (codeFormat == CodeFormat.JVM) {
			tries = splitJvmShadowedHandlers(mth, tries);
		}
		initTryCatches(mth, mth.getInstructions(), tries, codeFormat == CodeFormat.DEX || codeFormat == CodeFormat.JVM);
	}

	private static List<ITry> splitJvmShadowedHandlers(MethodNode mth, List<ITry> tries) {
		List<ITry> result = new ArrayList<>(tries.size());
		for (ITry current : tries) {
			List<ITry> parts = new ArrayList<>(1);
			parts.add(current);
			for (ITry preceding : result) {
				List<ITry> updated = new ArrayList<>(parts.size());
				for (ITry part : parts) {
					int overlapStart = Math.max(part.getStartOffset(), preceding.getStartOffset());
					int overlapEnd = Math.min(part.getEndOffset(), preceding.getEndOffset());
					if (overlapStart > overlapEnd) {
						updated.add(part);
						continue;
					}
					ICatch filteredCatch = removeShadowedTypes(mth, part.getCatch(), preceding.getCatch());
					if (filteredCatch == part.getCatch()) {
						updated.add(part);
						continue;
					}
					if (part.getStartOffset() < overlapStart) {
						updated.add(new TryData(part.getStartOffset(), overlapStart - 1, part.getCatch()));
					}
					if (filteredCatch.getHandlers().length != 0 || filteredCatch.getCatchAllHandler() != -1) {
						updated.add(new TryData(overlapStart, overlapEnd, filteredCatch));
					}
					if (overlapEnd < part.getEndOffset()) {
						updated.add(new TryData(overlapEnd + 1, part.getEndOffset(), part.getCatch()));
					}
				}
				parts = updated;
				if (parts.isEmpty()) {
					break;
				}
			}
			result.addAll(parts);
		}
		return result;
	}

	private static ICatch removeShadowedTypes(MethodNode mth, ICatch current, ICatch preceding) {
		int[] handlers = current.getHandlers();
		String[] types = current.getTypes();
		if (handlers.length != types.length) {
			throw new JadxRuntimeException("Exception handler types count does not match targets count");
		}
		if (handlers.length == 0) {
			return current;
		}
		int[] precedingHandlers = preceding.getHandlers();
		String[] precedingTypes = preceding.getTypes();
		if (precedingHandlers.length != precedingTypes.length) {
			throw new JadxRuntimeException("Exception handler types count does not match targets count");
		}
		if (precedingHandlers.length == 0 && preceding.getCatchAllHandler() == -1) {
			return current;
		}
		int[] keptHandlers = new int[handlers.length];
		String[] keptTypes = new String[types.length];
		int keptCount = 0;
		for (int i = 0; i < handlers.length; i++) {
			ClassInfo currentType = ClassInfo.fromName(mth.root(), types[i]);
			boolean shadowed = false;
			if (!Consts.CLASS_THROWABLE.equals(currentType.getFullName())) {
				shadowed = preceding.getCatchAllHandler() != -1;
				for (String precedingTypeName : precedingTypes) {
					ClassInfo precedingType = ClassInfo.fromName(mth.root(), precedingTypeName);
					if (mth.root().getTypeCompare().compareTypes(precedingType, currentType).isWiderOrEqual()) {
						shadowed = true;
						break;
					}
				}
			}
			if (!shadowed) {
				keptHandlers[keptCount] = handlers[i];
				keptTypes[keptCount] = types[i];
				keptCount++;
			}
		}
		if (keptCount == handlers.length) {
			return current;
		}
		return new CatchData(Arrays.copyOf(keptHandlers, keptCount), Arrays.copyOf(keptTypes, keptCount),
				current.getCatchAllHandler());
	}

	private static void initTryCatches(MethodNode mth, InsnNode[] insnByOffset, List<ITry> tries, boolean validateBounds) {
		if (tries.isEmpty()) {
			return;
		}
		if (Consts.DEBUG_EXC_HANDLERS) {
			LOG.debug("Raw try blocks in {}", mth);
			tries.forEach(tryData -> LOG.debug(" - {}", tryData));
		}
		if (tries.size() > 1) {
			validateSharedHandlerTypes(tries);
		}
		for (ITry tryData : tries) {
			if (validateBounds) {
				validateTryBounds(tryData, insnByOffset);
			}
			List<ExceptionHandler> handlers = convertToHandlers(mth, tryData.getCatch(), insnByOffset);
			if (handlers.isEmpty()) {
				continue;
			}
			markTryBounds(insnByOffset, tryData, CatchAttr.build(handlers));
		}
	}

	private static void validateSharedHandlerTypes(List<ITry> tries) {
		// ExceptionHandler stores catch types per handler offset, not per try range.
		// Different type sets at one offset would widen one catch and silently drop another range.
		Map<Integer, Set<String>> handlerTypes = new HashMap<>();
		for (ITry tryData : tries) {
			ICatch catchData = tryData.getCatch();
			int[] handlers = catchData.getHandlers();
			String[] types = catchData.getTypes();
			if (handlers.length != types.length) {
				throw new JadxRuntimeException("Exception handler types count does not match targets count");
			}
			Map<Integer, Set<String>> currentTypes = new HashMap<>();
			for (int i = 0; i < handlers.length; i++) {
				currentTypes.computeIfAbsent(handlers[i], key -> new HashSet<>()).add(types[i]);
			}
			int catchAll = catchData.getCatchAllHandler();
			if (catchAll != -1) {
				currentTypes.computeIfAbsent(catchAll, key -> new HashSet<>()).add(null);
			}
			for (Map.Entry<Integer, Set<String>> entry : currentTypes.entrySet()) {
				Set<String> previousTypes = handlerTypes.putIfAbsent(entry.getKey(), entry.getValue());
				if (previousTypes != null && !previousTypes.equals(entry.getValue())) {
					throw new JadxRuntimeException("Exception handler shared by try ranges with different catch types: " + entry.getKey());
				}
			}
		}
	}

	private static void validateTryBounds(ITry tryData, InsnNode[] insnByOffset) {
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
		boolean jvm = mth.getCodeReader().getCodeFormat() == CodeFormat.JVM;
		List<ClassInfo> precedingTypes = jvm ? new ArrayList<>(handlersCount) : null;
		for (int i = 0; i < handlersCount; i++) {
			int handlerOffset = handlerOffsetArr[i];
			ClassInfo type = ClassInfo.fromName(mth.root(), handlerTypes[i]);
			if (jvm) {
				boolean shadowed = false;
				for (ClassInfo precedingType : precedingTypes) {
					if (mth.root().getTypeCompare().compareTypes(precedingType, type).isWiderOrEqual()) {
						shadowed = true;
						break;
					}
				}
				if (shadowed) {
					continue;
				}
				precedingTypes.add(type);
			}
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
