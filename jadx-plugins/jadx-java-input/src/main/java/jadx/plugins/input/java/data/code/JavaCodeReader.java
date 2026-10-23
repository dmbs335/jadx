package jadx.plugins.input.java.data.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.CodeFormat;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.IDebugInfo;
import jadx.api.plugins.input.data.ILocalVar;
import jadx.api.plugins.input.data.ITry;
import jadx.api.plugins.input.data.impl.CatchData;
import jadx.api.plugins.input.data.impl.DebugInfo;
import jadx.api.plugins.input.insns.InsnData;
import jadx.plugins.input.java.data.ConstPoolReader;
import jadx.plugins.input.java.data.DataReader;
import jadx.plugins.input.java.data.JavaClassData;
import jadx.plugins.input.java.data.attributes.JavaAttrStorage;
import jadx.plugins.input.java.data.attributes.JavaAttrType;
import jadx.plugins.input.java.data.attributes.debuginfo.JavaLocalVar;
import jadx.plugins.input.java.data.attributes.debuginfo.LineNumberTableAttr;
import jadx.plugins.input.java.data.attributes.debuginfo.LocalVarTypesAttr;
import jadx.plugins.input.java.data.attributes.debuginfo.LocalVarsAttr;
import jadx.plugins.input.java.data.attributes.types.StackMapTableAttr;
import jadx.plugins.input.java.data.code.trycatch.JavaSingleCatch;
import jadx.plugins.input.java.data.code.trycatch.JavaTryData;
import jadx.plugins.input.java.utils.JavaClassParseException;

public class JavaCodeReader implements ICodeReader {
	@Override
	public CodeFormat getCodeFormat() {
		return CodeFormat.JVM;
	}

	private final JavaClassData clsData;
	private final DataReader reader;
	private final int codeOffset;

	public JavaCodeReader(JavaClassData clsData, int offset) {
		this.clsData = clsData;
		this.reader = clsData.getData();
		this.codeOffset = offset;
	}

	@Override
	public ICodeReader copy() {
		return this;
	}

	@Override
	public void visitInstructions(Consumer<InsnData> insnConsumer) {
		Set<Integer> excHandlers = getExcHandlers();
		jumpToCodeAttributes();
		StackMapTableAttr stackMapTable = clsData.getAttributesReader().loadOne(reader, JavaAttrType.STACK_MAP_TABLE);

		int maxStack = readMaxStack();
		reader.skip(2);
		int codeSize = reader.readU4();

		CodeDecodeState state = new CodeDecodeState(clsData, reader, maxStack, excHandlers, stackMapTable);
		JavaInsnData insn = new JavaInsnData(state);
		state.setInsn(insn);
		int offset = 0;
		while (offset < codeSize) {
			insn.setDecoded(false);
			insn.setOffset(offset);
			insn.setInsnStart(reader.getOffset());

			int opcode = reader.readU1();
			JavaInsnInfo insnInfo = JavaInsnsRegister.get(opcode);
			if (insnInfo == null) {
				throw new JavaClassParseException("Unknown opcode: 0x" + Integer.toHexString(opcode));
			}
			insn.setOpcodeUnit(opcode);
			insn.setInsnInfo(insnInfo);
			insn.setRegsCount(insnInfo.getRegsCount());
			insn.setOpcode(insnInfo.getApiOpcode());
			insn.setPayloadSize(insnInfo.getPayloadSize());
			insn.setPayload(null);

			state.onInsn(offset);
			insnConsumer.accept(insn);

			int payloadSize = insn.getPayloadSize();
			if (!insn.isDecoded()) {
				if (payloadSize == -1) {
					insn.skip();
					payloadSize = insn.getPayloadSize();
				} else {
					reader.skip(payloadSize);
				}
			}
			offset += 1 + payloadSize;
		}
	}

	@Override
	public int getRegistersCount() {
		int maxStack = readMaxStack();
		int maxLocals = reader.readU2();
		return maxStack + maxLocals;
	}

	@Override
	public int getArgsStartReg() {
		return readMaxStack();
	}

	private int readMaxStack() {
		reader.absPos(codeOffset);
		int maxStack = reader.readU2();
		return maxStack + 1; // add one temporary register (for `swap` opcode)
	}

	@Override
	public int getUnitsCount() {
		return reader.absPos(codeOffset + 4).readU4();
	}

	private static final Set<JavaAttrType<?>> DEBUG_INFO_ATTRIBUTES = Set.of(
			JavaAttrType.LINE_NUMBER_TABLE,
			JavaAttrType.LOCAL_VAR_TABLE,
			JavaAttrType.LOCAL_VAR_TYPE_TABLE);

	@Override
	@Nullable
	public IDebugInfo getDebugInfo() {
		int maxStack = readMaxStack();
		jumpToCodeAttributes();
		JavaAttrStorage attrs = clsData.getAttributesReader().loadMulti(reader, DEBUG_INFO_ATTRIBUTES);
		LineNumberTableAttr linesAttr = attrs.get(JavaAttrType.LINE_NUMBER_TABLE);
		LocalVarsAttr varsAttr = attrs.get(JavaAttrType.LOCAL_VAR_TABLE);
		if (linesAttr == null && varsAttr == null) {
			return null;
		}
		Map<Integer, Integer> linesMap = linesAttr != null ? linesAttr.getLineMap() : Collections.emptyMap();

		List<ILocalVar> vars;
		if (varsAttr == null) {
			vars = Collections.emptyList();
		} else {
			List<JavaLocalVar> javaVars = varsAttr.getVars();
			LocalVarTypesAttr typedVars = attrs.get(JavaAttrType.LOCAL_VAR_TYPE_TABLE);
			if (typedVars != null && !typedVars.getVars().isEmpty()) {
				// merge signature from typedVars into javaVars
				Map<JavaLocalVar, JavaLocalVar> varsMap = new HashMap<>(javaVars.size());
				javaVars.forEach(v -> varsMap.put(v, v));
				for (JavaLocalVar typedVar : typedVars.getVars()) {
					JavaLocalVar jv = varsMap.get(typedVar);
					if (jv != null) {
						jv.setSignature(typedVar.getSignature());
					}
				}
			}
			javaVars.forEach(v -> v.shiftRegNum(maxStack));
			vars = Collections.unmodifiableList(javaVars);
		}
		return new DebugInfo(linesMap, vars);
	}

	@Override
	public int getCodeOffset() {
		return codeOffset;
	}

	@Override
	public List<ITry> getTries() {
		jumpToTries();
		int excTableLen = reader.readU2();
		if (excTableLen == 0) {
			return Collections.emptyList();
		}
		ConstPoolReader constPool = clsData.getConstPoolReader();
		Map<JavaTryData, Integer> currentRun = new HashMap<>();
		List<JavaTryData> orderedTries = new ArrayList<>(excTableLen);
		List<List<JavaSingleCatch>> orderedCatches = new ArrayList<>(excTableLen);
		List<JavaTryData> precedingCatchAllRanges = new ArrayList<>();
		JavaTryData previousOriginalRange = null;
		for (int i = 0; i < excTableLen; i++) {
			int start = reader.readU2();
			int end = reader.readU2();
			int handler = reader.readU2();
			int type = reader.readU2();
			if (end <= start) {
				throw new JavaClassParseException("Invalid exception table range: " + start + " .. " + end);
			}
			String catchType = type == 0 ? null : constPool.getClass(type);
			if (type != 0 && catchType == null) {
				throw new JavaClassParseException("Invalid exception handler type at index: " + type);
			}
			boolean catchAll = catchType == null || "Ljava/lang/Throwable;".equals(catchType);
			// JVM exception table end_pc is exclusive; ITry end offset is inclusive.
			JavaTryData originalRange = new JavaTryData(start, end - 1);
			if (!originalRange.equals(previousOriginalRange)) {
				// Only adjacent entries for one range can be grouped. Grouping across
				// intervening ranges would change JVM exception-table precedence.
				currentRun.clear();
				previousOriginalRange = originalRange;
			}
			List<JavaTryData> availableRanges = new ArrayList<>(1);
			availableRanges.add(originalRange);
			// The JVM tests exception entries in table order. Earlier catch-all
			// entries shadow later typed handlers on their overlapping ranges.
			// Keep catch-all ranges intact: overlapping synthetic finally handlers
			// are needed by the downstream try/finally region reconstruction.
			if (!catchAll) {
				for (JavaTryData catchAllRange : precedingCatchAllRanges) {
					availableRanges = subtractRange(availableRanges, catchAllRange);
				}
			}
			JavaSingleCatch singleCatch = new JavaSingleCatch(handler, catchType);
			for (JavaTryData availableRange : availableRanges) {
				Integer index = currentRun.get(availableRange);
				if (index == null) {
					index = orderedTries.size();
					currentRun.put(availableRange, index);
					orderedTries.add(availableRange);
					orderedCatches.add(new ArrayList<>());
				}
				orderedCatches.get(index).add(singleCatch);
			}
			if (catchAll) {
				precedingCatchAllRanges.add(originalRange);
			}
		}
		for (int i = 0; i < orderedTries.size(); i++) {
			orderedTries.get(i).setCatch(convertSingleCatches(orderedCatches.get(i)));
		}
		return new ArrayList<>(orderedTries);
	}

	private static List<JavaTryData> subtractRange(List<JavaTryData> ranges, JavaTryData excluded) {
		List<JavaTryData> result = new ArrayList<>(ranges.size() + 1);
		for (JavaTryData range : ranges) {
			if (excluded.getEndOffset() < range.getStartOffset() || excluded.getStartOffset() > range.getEndOffset()) {
				result.add(range);
				continue;
			}
			if (range.getStartOffset() < excluded.getStartOffset()) {
				result.add(new JavaTryData(range.getStartOffset(), excluded.getStartOffset() - 1));
			}
			if (range.getEndOffset() > excluded.getEndOffset()) {
				result.add(new JavaTryData(excluded.getEndOffset() + 1, range.getEndOffset()));
			}
		}
		return result;
	}

	private static CatchData convertSingleCatches(List<JavaSingleCatch> list) {
		int allHandler = -1;
		int typedCount = 0;
		for (JavaSingleCatch singleCatch : list) {
			if (singleCatch.getType() == null) {
				allHandler = singleCatch.getHandler();
				// JVM exception entries are tested in table order. A catch-all shadows
				// every later entry for this same protected range.
				break;
			}
			typedCount++;
			if ("Ljava/lang/Throwable;".equals(singleCatch.getType())) {
				// Keep an explicit Throwable type for try-with-resources/finally
				// reconstruction, but ignore handlers it shadows in table order.
				break;
			}
		}
		int[] handlers = new int[typedCount];
		String[] types = new String[typedCount];
		for (int i = 0; i < typedCount; i++) {
			JavaSingleCatch singleCatch = list.get(i);
			handlers[i] = singleCatch.getHandler();
			types[i] = singleCatch.getType();
		}
		return new CatchData(handlers, types, allHandler);
	}

	private Set<Integer> getExcHandlers() {
		jumpToTries();
		int excTableLen = reader.readU2();
		if (excTableLen == 0) {
			return Collections.emptySet();
		}
		Set<Integer> set = new HashSet<>(excTableLen);
		for (int i = 0; i < excTableLen; i++) {
			reader.skip(4);
			int handler = reader.readU2();
			reader.skip(2);
			set.add(handler);
		}
		return set;
	}

	private void jumpToTries() {
		reader.absPos(codeOffset + 4);
		reader.skip(reader.readU4()); // code length
	}

	private void jumpToCodeAttributes() {
		jumpToTries();
		reader.skip(reader.readU2() * 8); // exceptions table
	}
}
