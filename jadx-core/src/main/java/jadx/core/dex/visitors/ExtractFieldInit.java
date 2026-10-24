package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jadx.api.plugins.input.data.AccessFlags;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.api.plugins.input.insns.InsnIndexType;
import jadx.api.plugins.input.insns.Opcode;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.FieldInitInsnAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.shrink.CodeShrinkVisitor;
import jadx.core.utils.BlockUtils;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxException;

@JadxVisitor(
		name = "ExtractFieldInit",
		desc = "Move duplicated field initialization from constructors",
		runAfter = ModVisitor.class,
		runBefore = ClassModifier.class
)
public class ExtractFieldInit extends AbstractVisitor {

	@Override
	public boolean visit(ClassNode cls) throws JadxException {
		for (ClassNode inner : cls.getInnerClasses()) {
			visit(inner);
		}
		if (!cls.getFields().isEmpty()) {
			moveStaticFieldsInit(cls);
			moveCommonFieldsInit(cls);
		}
		return false;
	}

	private static final class FieldInitInfo {
		final FieldNode fieldNode;
		final IndexInsnNode putInsn;
		final boolean canMove;

		public FieldInitInfo(FieldNode fieldNode, IndexInsnNode putInsn, boolean canMove) {
			this.fieldNode = fieldNode;
			this.putInsn = putInsn;
			this.canMove = canMove;
		}
	}

	private static final class ConstructorInitInfo {
		final MethodNode constructorMth;
		final List<FieldInitInfo> fieldInits;

		private ConstructorInitInfo(MethodNode constructorMth, List<FieldInitInfo> fieldInits) {
			this.constructorMth = constructorMth;
			this.fieldInits = fieldInits;
		}
	}

	private static void moveStaticFieldsInit(ClassNode cls) {
		MethodNode classInitMth = cls.getClassInitMth();
		if (classInitMth == null
				|| !classInitMth.getAccessFlags().isStatic()
				|| classInitMth.isNoCode()
				|| classInitMth.getBasicBlocks() == null) {
			return;
		}
		if (ListUtils.noneMatch(cls.getFields(), FieldNode::isStatic)) {
			return;
		}
		while (processStaticFields(cls, classInitMth)) {
			// sometimes instructions moved to field init prevent from vars inline -> inline and try again
			CodeShrinkVisitor.shrinkMethod(classInitMth);
		}
	}

	private static boolean processStaticFields(ClassNode cls, MethodNode classInitMth) {
		List<FieldInitInfo> inits = collectFieldsInit(cls, classInitMth, InsnType.SPUT);
		if (inits.isEmpty()) {
			return false;
		}
		List<FieldInitInfo> movableInits = new ArrayList<>(inits);
		filterFieldsInit(movableInits);
		Set<FieldNode> possiblyObservedFinalFields = inits.stream()
				.filter(fi -> fi.fieldNode.getAccessFlags().isFinal())
				.filter(fi -> isIndirectStaticFieldReadBeforeWrite(cls, classInitMth, fi))
				.map(fi -> fi.fieldNode)
				.collect(Collectors.toSet());
		// Encoded values are installed before <clinit> starts. If one of them is
		// overwritten there, moving any write into a field initializer can place
		// it before the encoded value and change reads through other methods.
		if (inits.stream().anyMatch(fi -> fi.fieldNode.get(JadxAttrType.CONSTANT_VALUE) != null
				&& fi.fieldNode.get(JadxAttrType.CONSTANT_VALUE) != EncodedValue.NULL)
				|| !possiblyObservedFinalFields.isEmpty()) {
			movableInits.clear();
		}
		Set<FieldNode> movableFields = movableInits.stream().map(v -> v.fieldNode).collect(Collectors.toSet());
		for (FieldInitInfo fieldInit : inits) {
			FieldNode field = fieldInit.fieldNode;
			if (field.getAccessFlags().isFinal()) {
				EncodedValue encodedValue = field.get(JadxAttrType.CONSTANT_VALUE);
				if (movableFields.contains(field)) {
					field.remove(JadxAttrType.CONSTANT_VALUE);
				} else if (encodedValue != null && encodedValue != EncodedValue.NULL) {
					// Java cannot express both an encoded static value and a later assignment
					// to a final field. Preserve the values and report the lost modifier.
					field.setAccessFlags(field.getAccessFlags().remove(AccessFlags.FINAL));
					field.addWarnComment("Final modifier removed: encoded static value is overwritten in class initializer");
				} else if (isStaticFieldReadBeforeWrite(classInitMth, fieldInit)) {
					// A direct read before assignment is also illegal for a Java final field.
					// Keep the default value explicit so the read remains observable.
					field.setAccessFlags(field.getAccessFlags().remove(AccessFlags.FINAL));
					field.addWarnComment("Final modifier removed: static field is read before assignment in class initializer");
				} else if (possiblyObservedFinalFields.contains(field)) {
					// A call can read the default value before the field is assigned.
					// Keeping it final would turn a literal write into a Java constant.
					field.setAccessFlags(field.getAccessFlags().remove(AccessFlags.FINAL));
					field.addWarnComment("Final modifier removed: static field is read through a call before assignment");
				} else {
					field.remove(JadxAttrType.CONSTANT_VALUE);
				}
			}
		}
		if (movableInits.isEmpty()) {
			return false;
		}
		for (FieldInitInfo fieldInit : movableInits) {
			IndexInsnNode insn = fieldInit.putInsn;
			InsnArg arg = insn.getArg(0);
			if (arg instanceof InsnWrapArg) {
				((InsnWrapArg) arg).getWrapInsn().add(AFlag.DECLARE_VAR);
			}
			InsnRemover.remove(classInitMth, insn);
			addFieldInitAttr(classInitMth, fieldInit.fieldNode, insn);
		}
		fixFieldsOrder(cls, movableInits);
		return true;
	}

	private static boolean isStaticFieldReadBeforeWrite(MethodNode mth, FieldInitInfo target) {
		FieldInfo fieldInfo = target.fieldNode.getFieldInfo();
		return hasInsnBeforeStaticFieldWrite(mth, target, innerInsn -> innerInsn.getType() == InsnType.SGET
				&& fieldInfo.equals(((IndexInsnNode) innerInsn).getIndex()), true);
	}

	private static boolean isIndirectStaticFieldReadBeforeWrite(ClassNode cls, MethodNode mth, FieldInitInfo target) {
		FieldInfo fieldInfo = target.fieldNode.getFieldInfo();
		return hasInsnBeforeStaticFieldWrite(mth, target, innerInsn -> innerInsn instanceof BaseInvokeNode
				&& methodReadsStaticField(cls, ((BaseInvokeNode) innerInsn).getCallMth(), fieldInfo, new HashSet<>()), false);
	}

	private static boolean methodReadsStaticField(ClassNode cls, MethodInfo calledMth, FieldInfo fieldInfo, Set<MethodInfo> visited) {
		if (!visited.add(calledMth)) {
			return false;
		}
		MethodNode callee = cls.root().resolveMethod(calledMth);
		if (callee == null) {
			return false;
		}
		// Other classes may be processed concurrently, so inspect their immutable
		// input code instead of partially built or changing basic blocks.
		if (callee.getParentClass() != cls || callee.getBasicBlocks() == null) {
			return rawMethodReadsStaticField(cls, callee, fieldInfo, visited);
		}
		for (BlockNode block : callee.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				Boolean read = insn.visitInsns(innerInsn -> {
					if (innerInsn.getType() == InsnType.SGET
							&& fieldInfo.equals(((IndexInsnNode) innerInsn).getIndex())) {
						return true;
					}
					if (innerInsn instanceof BaseInvokeNode
							&& methodReadsStaticField(cls, ((BaseInvokeNode) innerInsn).getCallMth(), fieldInfo, visited)) {
						return true;
					}
					return null;
				});
				if (Objects.equals(read, Boolean.TRUE)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean rawMethodReadsStaticField(ClassNode cls, MethodNode callee, FieldInfo fieldInfo, Set<MethodInfo> visited) {
		ICodeReader codeReader = callee.getCodeReader();
		if (codeReader == null) {
			return false;
		}
		boolean[] found = { false };
		codeReader.copy().visitInstructions(insnData -> {
			if (found[0]) {
				return;
			}
			Opcode opcode = insnData.getOpcode();
			if (opcode.name().startsWith("SGET") && insnData.getIndexType() == InsnIndexType.FIELD_REF) {
				insnData.decode();
				found[0] = fieldInfo.equals(FieldInfo.fromRef(cls.root(), insnData.getIndexAsField()));
			} else if (opcode.name().startsWith("INVOKE") && insnData.getIndexType() == InsnIndexType.METHOD_REF) {
				insnData.decode();
				MethodInfo calledMth = MethodInfo.fromRef(cls.root(), insnData.getIndexAsMethod());
				found[0] = methodReadsStaticField(cls, calledMth, fieldInfo, visited);
			}
		});
		return found[0];
	}

	private static boolean hasInsnBeforeStaticFieldWrite(MethodNode mth, FieldInitInfo target,
			Predicate<InsnNode> match, boolean includeWriteInsn) {
		BlockNode putBlock = null;
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn == target.putInsn) {
					putBlock = block;
					break;
				}
			}
			if (putBlock != null) {
				break;
			}
		}
		if (putBlock == null) {
			return false;
		}
		for (BlockNode block : mth.getBasicBlocks()) {
			if (block != putBlock && !BlockUtils.isAnyPathExists(block, putBlock)) {
				continue;
			}
			for (InsnNode insn : block.getInstructions()) {
				if (block == putBlock && insn == target.putInsn && !includeWriteInsn) {
					return false;
				}
				if (Objects.equals(insn.visitInsns(innerInsn -> match.test(innerInsn) ? true : null), Boolean.TRUE)) {
					return true;
				}
				if (block == putBlock && insn == target.putInsn) {
					return false;
				}
			}
		}
		return false;
	}

	private static void moveCommonFieldsInit(ClassNode cls) {
		if (ListUtils.noneMatch(cls.getFields(), FieldNode::isInstance)) {
			return;
		}
		List<MethodNode> constructors = getConstructorsList(cls);
		if (constructors.isEmpty()) {
			return;
		}
		List<ConstructorInitInfo> infoList = new ArrayList<>(constructors.size());
		for (MethodNode constructorMth : constructors) {
			List<FieldInitInfo> inits = collectFieldsInit(cls, constructorMth, InsnType.IPUT);
			filterFieldsInit(inits);
			if (inits.isEmpty()) {
				return;
			}
			infoList.add(new ConstructorInitInfo(constructorMth, inits));
		}
		// compare collected instructions
		ConstructorInitInfo common = null;
		for (ConstructorInitInfo info : infoList) {
			if (common == null) {
				common = info;
				continue;
			}
			if (!compareFieldInits(common.fieldInits, info.fieldInits)) {
				return;
			}
		}
		if (common == null) {
			return;
		}
		// all checks passed
		for (ConstructorInitInfo info : infoList) {
			for (FieldInitInfo fieldInit : info.fieldInits) {
				IndexInsnNode putInsn = fieldInit.putInsn;
				InsnArg arg = putInsn.getArg(0);
				if (arg instanceof InsnWrapArg) {
					((InsnWrapArg) arg).getWrapInsn().add(AFlag.DECLARE_VAR);
				}
				InsnRemover.remove(info.constructorMth, putInsn);
			}
		}
		for (FieldInitInfo fieldInit : common.fieldInits) {
			addFieldInitAttr(common.constructorMth, fieldInit.fieldNode, fieldInit.putInsn);
		}
		fixFieldsOrder(cls, common.fieldInits);
	}

	private static List<FieldInitInfo> collectFieldsInit(ClassNode cls, MethodNode mth, InsnType putType) {
		List<FieldInitInfo> fieldsInit = new ArrayList<>();
		Set<BlockNode> singlePathBlocks = new HashSet<>();
		BlockUtils.visitSinglePath(mth.getEnterBlock(), singlePathBlocks::add);

		boolean canReorder = true;
		for (BlockNode block : mth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				boolean fieldInsn = false;
				if (insn.getType() == putType) {
					IndexInsnNode putInsn = (IndexInsnNode) insn;
					FieldInfo field = (FieldInfo) putInsn.getIndex();
					if (field.getDeclClass().equals(cls.getClassInfo())) {
						FieldNode fn = cls.searchField(field);
						if (fn != null) {
							boolean canMove = canReorder && singlePathBlocks.contains(block);
							FieldInitInfo initInfo = new FieldInitInfo(fn, putInsn, canMove);
							fieldsInit.add(initInfo);
							fieldInsn = true;
							// A write that stays in the constructor must not be passed by a later initializer.
							if (!checkInsn(initInfo)) {
								canReorder = false;
							}
						}
					}
				}
				if (!fieldInsn && canReorder && !insn.canReorder()) {
					canReorder = false;
				}
			}
		}
		return fieldsInit;
	}

	private static void filterFieldsInit(List<FieldInitInfo> inits) {
		// exclude fields initialized several times
		Set<FieldInfo> excludedFields = inits
				.stream()
				.collect(Collectors.toMap(fi -> fi.fieldNode, fi -> 1, Integer::sum))
				.entrySet()
				.stream()
				.filter(v -> v.getValue() > 1)
				.map(v -> v.getKey().getFieldInfo())
				.collect(Collectors.toSet());

		for (FieldInitInfo initInfo : inits) {
			if (!checkInsn(initInfo)) {
				excludedFields.add(initInfo.fieldNode.getFieldInfo());
			}
		}
		// A static initializer reading a field assigned at or after this write must
		// remain in the class initializer to preserve the value it observes.
		Set<FieldInfo> staticFieldsWrittenAtOrAfter = new HashSet<>();
		for (int i = inits.size() - 1; i >= 0; i--) {
			FieldInitInfo initInfo = inits.get(i);
			if (initInfo.putInsn.getType() == InsnType.SPUT) {
				staticFieldsWrittenAtOrAfter.add(initInfo.fieldNode.getFieldInfo());
				if (insnUseExcludedField(initInfo, staticFieldsWrittenAtOrAfter)) {
					excludedFields.add(initInfo.fieldNode.getFieldInfo());
				}
			}
		}
		if (!excludedFields.isEmpty()) {
			boolean changed;
			do {
				changed = false;
				boolean excludedWriteSeen = false;
				for (FieldInitInfo initInfo : inits) {
					FieldInfo fieldInfo = initInfo.fieldNode.getFieldInfo();
					if (excludedFields.contains(fieldInfo)) {
						excludedWriteSeen = true;
						continue;
					}
					// A write left in the constructor is an ordering barrier for later writes.
					if (excludedWriteSeen || insnUseExcludedField(initInfo, excludedFields)) {
						excludedFields.add(fieldInfo);
						changed = true;
						excludedWriteSeen = true;
					}
				}
			} while (changed);
		}

		// apply
		if (!excludedFields.isEmpty()) {
			inits.removeIf(fi -> excludedFields.contains(fi.fieldNode.getFieldInfo()));
		}
	}

	private static boolean checkInsn(FieldInitInfo initInfo) {
		if (!initInfo.canMove) {
			return false;
		}
		IndexInsnNode insn = initInfo.putInsn;
		// Only writes to this instance can become instance field initializers.
		// A literal value must not let a write to another object pass this check.
		if (insn.getType() == InsnType.IPUT && (insn.getArgsCount() < 2 || !insn.getArg(1).isThis())) {
			return false;
		}
		InsnArg arg = insn.getArg(0);
		if (arg.isInsnWrap()) {
			InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
			if (!wrapInsn.canReorder() && insn.contains(AType.EXC_CATCH)) {
				return false;
			}
		} else {
			return arg.isLiteral() || arg.isThis();
		}
		Set<RegisterArg> regs = new HashSet<>();
		insn.getRegisterArgs(regs);
		if (!regs.isEmpty()) {
			for (RegisterArg reg : regs) {
				if (!reg.isThis()) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean insnUseExcludedField(FieldInitInfo initInfo, Set<FieldInfo> excludedFields) {
		if (excludedFields.isEmpty()) {
			return false;
		}
		IndexInsnNode insn = initInfo.putInsn;
		boolean staticField = insn.getType() == InsnType.SPUT;
		InsnType useType = staticField ? InsnType.SGET : InsnType.IGET;
		// exclude if init code use any excluded field
		Boolean exclude = insn.visitInsns(innerInsn -> {
			if (innerInsn.getType() == useType) {
				FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) innerInsn).getIndex();
				if (excludedFields.contains(fieldInfo)) {
					return true;
				}
			}
			return null;
		});
		return Objects.equals(exclude, Boolean.TRUE);
	}

	private static void fixFieldsOrder(ClassNode cls, List<FieldInitInfo> inits) {
		// Preserve the writes' execution order. A forward read must observe the field's
		// default value, not the value assigned by a later initializer.
		List<FieldNode> orderedFields = Utils.collectionMap(inits, v -> v.fieldNode);
		applyFieldsOrder(cls, orderedFields);
	}

	private static void applyFieldsOrder(ClassNode cls, List<FieldNode> orderedFields) {
		List<FieldNode> clsFields = cls.getFields();
		// Put all extracted initializers after untouched fields. This also makes a
		// read of an uninitialized static field a legal Java reference.
		boolean ordered = Collections.indexOfSubList(clsFields, orderedFields) == clsFields.size() - orderedFields.size();
		if (!ordered) {
			cls.moveFieldsToEnd(orderedFields);
		}
	}

	private static boolean compareFieldInits(List<FieldInitInfo> base, List<FieldInitInfo> other) {
		if (base.size() != other.size()) {
			return false;
		}
		int count = base.size();
		for (int i = 0; i < count; i++) {
			InsnNode baseInsn = base.get(i).putInsn;
			InsnNode otherInsn = other.get(i).putInsn;
			if (!baseInsn.isSame(otherInsn) || !isSameInitArg(baseInsn.getArg(0), otherInsn.getArg(0))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSameInitArg(InsnArg base, InsnArg other) {
		if (base.isThis() || other.isThis()) {
			return base.isThis() && other.isThis();
		}
		if (base.isInsnWrap() && other.isInsnWrap()) {
			InsnNode baseInsn = ((InsnWrapArg) base).getWrapInsn();
			InsnNode otherInsn = ((InsnWrapArg) other).getWrapInsn();
			if (!baseInsn.isSame(otherInsn)) {
				return false;
			}
			for (int i = 0; i < baseInsn.getArgsCount(); i++) {
				if (!isSameInitArg(baseInsn.getArg(i), otherInsn.getArg(i))) {
					return false;
				}
			}
			return true;
		}
		return base.equals(other);
	}

	private static List<MethodNode> getConstructorsList(ClassNode cls) {
		List<MethodNode> list = new ArrayList<>();
		for (MethodNode mth : cls.getMethods()) {
			AccessInfo accFlags = mth.getAccessFlags();
			if (!accFlags.isStatic() && accFlags.isConstructor()) {
				list.add(mth);
				if (mth.isNoCode() || BlockUtils.isAllBlocksEmpty(mth.getBasicBlocks())) {
					return Collections.emptyList();
				}
			}
		}
		return list;
	}

	private static void addFieldInitAttr(MethodNode mth, FieldNode field, IndexInsnNode putInsn) {
		InsnNode assignInsn;
		InsnArg fldArg = putInsn.getArg(0);
		if (fldArg.isInsnWrap()) {
			assignInsn = ((InsnWrapArg) fldArg).getWrapInsn();
		} else {
			assignInsn = InsnNode.wrapArg(fldArg);
		}
		field.addAttr(new FieldInitInsnAttr(mth, assignInsn));
	}
}
