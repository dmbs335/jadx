package jadx.core.codegen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.CommentsLevel;
import jadx.api.ICodeWriter;
import jadx.api.metadata.annotations.InsnCodeOffset;
import jadx.api.metadata.annotations.VarNode;
import jadx.api.plugins.input.data.AccessFlags;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.core.clsp.ClspClass;
import jadx.core.clsp.ClspMethod;
import jadx.core.codegen.utils.CodeGenUtils;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.FieldInitInsnAttr;
import jadx.core.dex.attributes.nodes.ClassTypeVarsAttr;
import jadx.core.dex.attributes.nodes.FieldReplaceAttr;
import jadx.core.dex.attributes.nodes.GenericInfoAttr;
import jadx.core.dex.attributes.nodes.InvalidMoveResultAttr;
import jadx.core.dex.attributes.nodes.LoopLabelAttr;
import jadx.core.dex.attributes.nodes.MethodReplaceAttr;
import jadx.core.dex.attributes.nodes.SkipMethodArgsAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.ArithOp;
import jadx.core.dex.instructions.ArrayPutNode;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.CmpInsnNode;
import jadx.core.dex.instructions.ConstClassNode;
import jadx.core.dex.instructions.ConstStringNode;
import jadx.core.dex.instructions.FieldPutNode;
import jadx.core.dex.instructions.FillArrayInsn;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.GotoNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeCustomNode;
import jadx.core.dex.instructions.InvokeCustomRawNode;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.NewArrayNode;
import jadx.core.dex.instructions.NumericCastNode;
import jadx.core.dex.instructions.ReturnNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.UnaryNumericNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.Named;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.java.JsrNode;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IMethodDetails;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.ProcessAnonymous;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.CodegenException;
import jadx.core.utils.exceptions.JadxRuntimeException;

import static jadx.core.utils.android.AndroidResourcesUtils.handleAppResField;

public class InsnGen {
	private static final Logger LOG = LoggerFactory.getLogger(InsnGen.class);

	protected final MethodGen mgen;
	protected final MethodNode mth;
	protected final RootNode root;
	protected final boolean fallback;

	protected enum Flags {
		BODY_ONLY,
		BODY_ONLY_NOWRAP,
		INLINE
	}

	public InsnGen(MethodGen mgen, boolean fallback) {
		this.mgen = mgen;
		this.mth = mgen.getMethodNode();
		this.root = mth.root();
		this.fallback = fallback;
	}

	private boolean isFallback() {
		return fallback;
	}

	public void addArgDot(ICodeWriter code, InsnArg arg) throws CodegenException {
		int len = code.getLength();
		addArg(code, arg, true);
		if (len != code.getLength()) {
			code.add('.');
		}
	}

	public void addArg(ICodeWriter code, InsnArg arg) throws CodegenException {
		addArg(code, arg, true);
	}

	public void addArg(ICodeWriter code, InsnArg arg, boolean wrap) throws CodegenException {
		addArg(code, arg, wrap ? BODY_ONLY_FLAG : BODY_ONLY_NOWRAP_FLAGS);
	}

	public void addArg(ICodeWriter code, InsnArg arg, Set<Flags> flags) throws CodegenException {
		if (arg.isRegister()) {
			RegisterArg reg = (RegisterArg) arg;
			if (code.isMetadataSupported()) {
				code.attachAnnotation(VarNode.getRef(mth, reg));
			}
			code.add(mgen.getNameGen().useArg(reg));
		} else if (arg.isLiteral()) {
			addLiteralArg(code, (LiteralArg) arg, flags);
		} else if (arg.isInsnWrap()) {
			addWrappedArg(code, (InsnWrapArg) arg, flags);
		} else if (arg.isNamed()) {
			code.add(((Named) arg).getName());
		} else {
			throw new CodegenException("Unknown arg type " + arg);
		}
	}

	private void addLiteralArg(ICodeWriter code, LiteralArg litArg, Set<Flags> flags) {
		String literalStr = lit(litArg);
		if (!flags.contains(Flags.BODY_ONLY_NOWRAP) && literalStr.startsWith("-")) {
			code.add('(').add(literalStr).add(')');
		} else {
			code.add(literalStr);
		}
	}

	private void addWrappedArg(ICodeWriter code, InsnWrapArg arg, Set<Flags> flags) throws CodegenException {
		InsnNode wrapInsn = arg.getWrapInsn();
		if (wrapInsn.contains(AFlag.FORCE_ASSIGN_INLINE)) {
			code.add('(');
			makeInsn(wrapInsn, code, Flags.INLINE);
			code.add(')');
		} else {
			makeInsnBody(code, wrapInsn, flags);
		}
	}

	public void assignVar(ICodeWriter code, InsnNode insn) throws CodegenException {
		RegisterArg arg = insn.getResult();
		if (insn.contains(AFlag.DECLARE_VAR)) {
			declareVar(code, arg);
		} else {
			addArg(code, arg, false);
		}
	}

	public void declareVar(ICodeWriter code, RegisterArg arg) {
		declareVar(code, arg.getSVar().getCodeVar());
	}

	public void declareVar(ICodeWriter code, CodeVar codeVar) {
		if (codeVar.isFinal()) {
			code.add("final ");
		}
		useType(code, codeVar.getType());
		code.add(' ');
		defVar(code, codeVar);
	}

	/**
	 * Variable definition without type, only var name
	 */
	private void defVar(ICodeWriter code, CodeVar codeVar) {
		String varName = mgen.getNameGen().assignArg(codeVar);
		if (code.isMetadataSupported()) {
			code.attachDefinition(VarNode.get(mth, codeVar));
		}
		code.add(varName);
	}

	private String lit(LiteralArg arg) {
		return TypeGen.literalToString(arg, mth, fallback);
	}

	private void instanceField(ICodeWriter code, FieldInfo field, InsnArg arg) throws CodegenException {
		instanceField(code, field, arg, false);
	}

	private void instanceField(ICodeWriter code, FieldInfo field, InsnArg arg, boolean castReceiver) throws CodegenException {
		ClassNode pCls = mth.getParentClass();
		FieldNode fieldNode = pCls.root().resolveField(field);
		if (fieldNode != null) {
			FieldReplaceAttr replace = fieldNode.get(AType.FIELD_REPLACE);
			if (replace != null) {
				switch (replace.getReplaceType()) {
					case CLASS_INSTANCE:
						useClass(code, replace.getClsRef());
						code.add(".this");
						break;
					case VAR:
						InsnArg varRef = replace.getVarRef();
						if (isOuterThisVar(varRef)) {
							useClass(code, varRef.getType());
							code.add(".this");
						} else {
							addArg(code, varRef);
						}
						break;
				}
				return;
			}
		}
		ArgType receiverType = arg.getType();
		ArgType ownerType = field.getDeclClass().getType();
		checkInstanceReceiver(arg, ownerType, castReceiver, "field");
		if (castReceiver && receiverType.isObject() && !ArgType.isInstanceOf(root, receiverType, ownerType)) {
			code.add("((");
			useClass(code, ownerType);
			code.add(") ");
			addArg(code, arg);
			code.add(").");
		} else {
			addArgDot(code, arg);
		}
		if (fieldNode != null) {
			code.attachAnnotation(fieldNode);
		}
		if (fieldNode == null) {
			code.add(field.getAlias());
		} else {
			code.add(fieldNode.getAlias());
		}
	}

	private static boolean isOuterThisVar(InsnArg varRef) {
		if (!varRef.isRegister()) {
			return false;
		}
		SSAVar ssaVar = ((RegisterArg) varRef).getSVar();
		return ssaVar != null && ssaVar.getCodeVar().isThis();
	}

	protected void staticField(ICodeWriter code, FieldInfo field) throws CodegenException {
		FieldNode fieldNode = root.resolveField(field);
		if (fieldNode != null
				&& fieldNode.contains(AFlag.INLINE_INSTANCE_FIELD)
				&& fieldNode.getParentClass().contains(AType.ANONYMOUS_CLASS)) {
			FieldInitInsnAttr initInsnAttr = fieldNode.get(AType.FIELD_INIT_INSN);
			if (initInsnAttr != null) {
				InsnNode insn = initInsnAttr.getInsn();
				if (insn instanceof ConstructorInsn) {
					fieldNode.add(AFlag.DONT_GENERATE);
					inlineAnonymousConstructor(code, fieldNode.getParentClass(), (ConstructorInsn) insn);
					return;
				}
			}
		}
		makeStaticFieldAccess(code, field, fieldNode, mgen.getClassGen());
	}

	public static void makeStaticFieldAccess(ICodeWriter code, FieldInfo field, ClassGen clsGen) {
		FieldNode fieldNode = clsGen.getClassNode().root().resolveField(field);
		makeStaticFieldAccess(code, field, fieldNode, clsGen);
	}

	private static void makeStaticFieldAccess(ICodeWriter code,
			FieldInfo field, @Nullable FieldNode fieldNode, ClassGen clsGen) {
		ClassInfo declClass = field.getDeclClass();
		// TODO
		boolean fieldFromThisClass = clsGen.getClassNode().getClassInfo().equals(declClass);
		if (!fieldFromThisClass || !clsGen.isBodyGenStarted()) {
			// Android specific resources class handler
			if (!handleAppResField(code, clsGen, declClass)) {
				clsGen.useClass(code, declClass);
			}
			code.add('.');
		}
		if (fieldNode != null) {
			code.attachAnnotation(fieldNode);
		}
		if (fieldNode == null) {
			code.add(field.getAlias());
		} else {
			code.add(fieldNode.getAlias());
		}
	}

	public void useClass(ICodeWriter code, ArgType type) {
		mgen.getClassGen().useClass(code, type);
	}

	public void useClass(ICodeWriter code, ClassInfo cls) {
		mgen.getClassGen().useClass(code, cls);
	}

	protected void useType(ICodeWriter code, ArgType type) {
		mgen.getClassGen().useType(code, type);
	}

	public void makeInsn(InsnNode insn, ICodeWriter code) throws CodegenException {
		makeInsn(insn, code, null);
	}

	private static final Set<Flags> EMPTY_FLAGS = EnumSet.noneOf(Flags.class);
	private static final Set<Flags> BODY_ONLY_FLAG = EnumSet.of(Flags.BODY_ONLY);
	private static final Set<Flags> BODY_ONLY_NOWRAP_FLAGS = EnumSet.of(Flags.BODY_ONLY_NOWRAP);

	protected void makeInsn(InsnNode insn, ICodeWriter code, Flags flag) throws CodegenException {
		if (insn.getType() == InsnType.REGION_ARG) {
			return;
		}
		try {
			if (flag == Flags.BODY_ONLY || flag == Flags.BODY_ONLY_NOWRAP) {
				makeInsnBody(code, insn, flag == Flags.BODY_ONLY ? BODY_ONLY_FLAG : BODY_ONLY_NOWRAP_FLAGS);
			} else {
				if (flag != Flags.INLINE) {
					code.startLineWithNum(insn.getSourceLine());
					InsnCodeOffset.attach(code, insn);
					if (insn.contains(AFlag.COMMENT_OUT)) {
						code.add("// ");
					}
				}
				RegisterArg resArg = insn.getResult();
				if (resArg != null) {
					SSAVar var = resArg.getSVar();
					if (var == null || var.getUseCount() != 0 || insn.getType() != InsnType.CONSTRUCTOR) {
						assignVar(code, insn);
						code.add(" = ");
						if (isGenericAssignCastNeeded(insn, resArg)) {
							code.add('(');
							useType(code, resArg.getSVar().getCodeVar().getType());
							code.add(") ");
						}
						if (var != null && var.getCodeVar() != null) {
							addLambdaObjectCast(code, insn, var.getCodeVar().getType());
						}
					}
				}
				makeInsnBody(code, insn, EMPTY_FLAGS);
				if (flag != Flags.INLINE) {
					code.add(';');
					CodeGenUtils.addCodeComments(code, mth, insn);
				}
			}
		} catch (Exception e) {
			throw new CodegenException(mth, "Error generate insn: " + insn, e);
		}
	}

	private static boolean isGenericAssignCastNeeded(InsnNode insn, RegisterArg result) {
		if (result.getSVar() == null) {
			return false;
		}
		ArgType codeVarType = result.getSVar().getCodeVar().getType();
		if (codeVarType == null
				|| !codeVarType.isGenericType()
				|| !ArgType.OBJECT.equals(result.getInitType())) {
			return false;
		}
		InvokeNode sourceInvoke = findSourceInvoke(insn, new HashSet<>());
		return sourceInvoke == null
				|| sourceInvoke.getInvokeType() == InvokeType.STATIC
				|| sourceInvoke.getArgsCount() == 0
				|| !sourceInvoke.getArg(0).getType().containsGeneric();
	}

	@Nullable
	private static InvokeNode findSourceInvoke(InsnNode insn, Set<InsnNode> visited) {
		if (!visited.add(insn)) {
			return null;
		}
		if (insn instanceof InvokeNode) {
			return (InvokeNode) insn;
		}
		for (InsnArg arg : insn.getArguments()) {
			InsnNode assignInsn = null;
			if (arg instanceof InsnWrapArg) {
				assignInsn = ((InsnWrapArg) arg).getWrapInsn();
			} else if (arg instanceof RegisterArg && ((RegisterArg) arg).getSVar() != null) {
				assignInsn = ((RegisterArg) arg).getSVar().getAssignInsn();
			}
			if (assignInsn != null) {
				InvokeNode sourceInvoke = findSourceInvoke(assignInsn, visited);
				if (sourceInvoke != null) {
					return sourceInvoke;
				}
			}
		}
		return null;
	}

	private void makeInsnBody(ICodeWriter code, InsnNode insn, Set<Flags> state) throws CodegenException {
		checkInvalidMoveResult(insn);
		switch (insn.getType()) {
			case CONST_STR:
				String str = ((ConstStringNode) insn).getString();
				code.add(mth.root().getStringUtils().unescapeString(str));
				break;

			case CONST_CLASS:
				ArgType clsType = ((ConstClassNode) insn).getClsType();
				useType(code, clsType);
				code.add(".class");
				break;

			case CONST:
				LiteralArg arg = (LiteralArg) insn.getArg(0);
				code.add(lit(arg));
				break;

			case MOVE:
				InsnArg moveArg = insn.getArg(0);
				if (isBooleanToIntMove(insn, moveArg)) {
					addArg(code, moveArg, false);
					code.add(" ? 1 : 0");
					break;
				}
				if (isIntToBooleanMove(insn, moveArg)) {
					addArg(code, moveArg, false);
					code.add(" != 0");
					break;
				}
				boolean moveCastNeeded = isMoveCastNeeded(insn, moveArg);
				if (!moveCastNeeded && insn.getResult() != null && insn.getResult().getSVar() != null) {
					ArgType resultType = insn.getResult().getSVar().getCodeVar().getType();
					ArgType sourceType = getAssignmentExpressionType(moveArg, resultType);
					if (resultType != null && sourceType != null && resultType.isTypeKnown() && sourceType.isTypeKnown()) {
						checkAssignmentOperand(moveArg, resultType, "local assignment");
					}
				}
				if (moveCastNeeded) {
					code.add('(');
					useType(code, insn.getResult().getSVar().getCodeVar().getType());
					code.add(") ");
				}
				addArg(code, moveArg, false);
				break;

			case CHECK_CAST:
			case CAST: {
				if (!isFallback() && insn instanceof NumericCastNode) {
					NumericCastNode numericCast = (NumericCastNode) insn;
					ArgType invalidSourceType = numericCast.getInvalidSourceType();
					if (invalidSourceType != null) {
						throw new CodegenException("Numeric cast operand type " + invalidSourceType
								+ " does not match opcode source type " + numericCast.getSourceType());
					}
				}
				if (isRedundantLambdaInterfaceCast(insn)) {
					addArg(code, insn.getArg(0), true);
					break;
				}
				ArgType castType = (ArgType) ((IndexInsnNode) insn).getIndex();
				checkNonNullReferenceLiteral(insn.getArg(0), castType);
				if (insn.getType() == InsnType.CHECK_CAST && !isFallback() && !insn.getArg(0).isZeroLiteral()) {
					ArgType sourceType = getExpressionArgType(insn.getArg(0));
					if (sourceType != null && sourceType.isPrimitive()) {
						throw new CodegenException("Primitive value used as check-cast operand: " + sourceType);
					}
					if (sourceType != null && isDefinitelyIncompatibleReferenceTypes(sourceType, castType)) {
						throw new CodegenException("Incompatible check-cast types: " + sourceType + " and " + castType);
					}
				}
				boolean wrap = state.contains(Flags.BODY_ONLY);
				if (wrap) {
					code.add('(');
				}
				code.add('(');
				useType(code, castType);
				code.add(") ");
				addLambdaCastForNonTargetType(code, insn.getArg(0), castType);
				addArg(code, insn.getArg(0), true);
				if (wrap) {
					code.add(')');
				}
				break;
			}

			case ARITH:
				makeArith((ArithNode) insn, code, state);
				break;

			case NEG:
				oneArgInsn(code, insn, state, '-');
				break;

			case NOT:
				char op = insn.getArg(0).getType() == ArgType.BOOLEAN ? '!' : '~';
				oneArgInsn(code, insn, state, op);
				break;

			case RETURN:
				if (insn.getArgsCount() != 0) {
					if (!isFallback() && insn instanceof ReturnNode) {
						ReturnNode returnNode = (ReturnNode) insn;
						if (!returnNode.isOpcodeCompatible(mth.getReturnType())) {
							throw new CodegenException("Return opcode " + returnNode.getReturnOpcode()
									+ " does not match method return type " + mth.getReturnType());
						}
						ArgType invalidSourceType = returnNode.getInvalidSourceType();
						if (invalidSourceType != null) {
							throw new CodegenException("Return operand type " + invalidSourceType
									+ " does not match method return type " + mth.getReturnType());
						}
					}
					checkReturnOperand(insn.getArg(0));
					code.add("return ");
					addArgWithExpectedType(code, insn.getArg(0), mth.getReturnType());
				} else {
					if (!isFallback() && !ArgType.VOID.equals(mth.getReturnType())) {
						throw new CodegenException("Missing return value for " + mth.getReturnType());
					}
					code.add("return");
				}
				break;

			case BREAK:
				code.add("break");
				LoopLabelAttr labelAttr = insn.get(AType.LOOP_LABEL);
				if (labelAttr != null) {
					code.add(' ').add(mgen.getNameGen().getLoopLabel(labelAttr));
				}
				break;

			case CONTINUE:
				code.add("continue");
				LoopLabelAttr continueLabelAttr = insn.get(AType.LOOP_LABEL);
				if (continueLabelAttr != null) {
					code.add(' ').add(mgen.getNameGen().getLoopLabel(continueLabelAttr));
				}
				break;

			case THROW:
				checkThrowOperand(insn.getArg(0));
				code.add("throw ");
				addArgWithExpectedType(code, insn.getArg(0), ArgType.THROWABLE);
				break;

			case CMP_L:
				checkNumericCmpOperand(insn, insn.getArg(0));
				checkNumericCmpOperand(insn, insn.getArg(1));
				if (ArgType.LONG.equals(getExpressionArgType(insn.getArg(0)))
						&& ArgType.LONG.equals(getExpressionArgType(insn.getArg(1)))) {
					code.add("Long.compare(");
					addArg(code, insn.getArg(0));
					code.add(", ");
					addArg(code, insn.getArg(1));
					code.add(')');
					break;
				}
				checkRepeatedCmpOperand(insn.getArg(0));
				checkRepeatedCmpOperand(insn.getArg(1));
				code.add('(');
				addArg(code, insn.getArg(0));
				code.add(" > ");
				addArg(code, insn.getArg(1));
				code.add(" ? 1 : (");
				addArg(code, insn.getArg(0));
				code.add(" == ");
				addArg(code, insn.getArg(1));
				code.add(" ? 0 : -1))");
				break;

			case CMP_G:
				checkNumericCmpOperand(insn, insn.getArg(0));
				checkNumericCmpOperand(insn, insn.getArg(1));
				checkRepeatedCmpOperand(insn.getArg(0));
				checkRepeatedCmpOperand(insn.getArg(1));
				code.add('(');
				addArg(code, insn.getArg(0));
				code.add(" < ");
				addArg(code, insn.getArg(1));
				code.add(" ? -1 : (");
				addArg(code, insn.getArg(0));
				code.add(" == ");
				addArg(code, insn.getArg(1));
				code.add(" ? 0 : 1))");
				break;

			case INSTANCE_OF: {
				boolean wrap = state.contains(Flags.BODY_ONLY);
				if (wrap) {
					code.add('(');
				}
				InsnArg instanceArg = insn.getArg(0);
				checkNonNullReferenceLiteral(instanceArg, ArgType.OBJECT);
				ArgType targetType = (ArgType) ((IndexInsnNode) insn).getIndex();
				if (!isFallback() && !instanceArg.isZeroLiteral()) {
					ArgType instanceType = getExpressionArgType(instanceArg);
					if (instanceType != null && instanceType.isPrimitive()) {
						throw new CodegenException("Primitive value used as instanceof operand: " + instanceType);
					}
					if (instanceType != null && isDefinitelyIncompatibleReferenceTypes(instanceType, targetType)) {
						throw new CodegenException("Incompatible instanceof types: " + instanceType + " and " + targetType);
					}
				}
				if (instanceArg.isLiteral() && instanceArg.contains(AType.AUTO_BOXED_LITERAL)) {
					code.add("((Object) ");
					addArg(code, instanceArg);
					code.add(')');
				} else {
					addLambdaObjectOperand(code, instanceArg);
				}
				code.add(" instanceof ");
				useType(code, targetType);
				if (wrap) {
					code.add(')');
				}
				break;
			}
			case CONSTRUCTOR:
				makeConstructor((ConstructorInsn) insn, code);
				break;

			case INVOKE:
				makeInvoke((InvokeNode) insn, code);
				break;

			case NEW_ARRAY: {
				ArgType arrayType = ((NewArrayNode) insn).getArrayType();
				if (!fallback && !arrayType.isArray()) {
					throw new CodegenException("new-array type is not an array: " + arrayType);
				}
				code.add("new ");
				useType(code, arrayType.getArrayRootElement());
				int k = 0;
				int argsCount = insn.getArgsCount();
				for (; k < argsCount; k++) {
					checkArrayIndex(insn.getArg(k), "dimension");
					code.add('[');
					addArg(code, insn.getArg(k), false);
					code.add(']');
				}
				int dim = arrayType.getArrayDimension();
				for (; k < dim; k++) {
					code.add("[]");
				}
				break;
			}

			case ARRAY_LENGTH:
				checkArrayOperand(insn.getArg(0));
				addArg(code, insn.getArg(0));
				code.add(".length");
				break;

			case FILLED_NEW_ARRAY:
				filledNewArray((FilledNewArrayNode) insn, code);
				break;

			case FILL_ARRAY:
				FillArrayInsn arrayNode = (FillArrayInsn) insn;
				if (fallback) {
					String arrStr = arrayNode.dataToString();
					addArg(code, insn.getArg(0));
					code.add(" = {").add(arrStr.substring(1, arrStr.length() - 1)).add("} // fill-array");
				} else {
					fillArray(code, arrayNode);
				}
				break;

			case AGET:
				checkArrayOperand(insn.getArg(0));
				checkArrayIndex(insn.getArg(1), "index");
				addArg(code, insn.getArg(0));
				code.add('[');
				addArg(code, insn.getArg(1), false);
				code.add(']');
				break;

			case APUT: {
				if (!isFallback() && insn instanceof ArrayPutNode) {
					ArrayPutNode arrayPut = (ArrayPutNode) insn;
					ArgType invalidSourceType = arrayPut.getInvalidSourceType();
					if (invalidSourceType != null) {
						throw new CodegenException("Array write operand type " + invalidSourceType
								+ " does not match element type " + arrayPut.getInvalidElementType());
					}
				}
				checkArrayOperand(insn.getArg(0));
				checkArrayIndex(insn.getArg(1), "index");
				ArgType arrayType = insn.getArg(0).getType();
				if (insn.getArg(0) instanceof InsnWrapArg) {
					InsnNode arrayInsn = ((InsnWrapArg) insn.getArg(0)).getWrapInsn();
					if (arrayInsn.getType() == InsnType.CHECK_CAST) {
						arrayType = ((IndexInsnNode) arrayInsn).getIndexAsType();
					}
				}
				ArgType elementType = arrayType.isArray() ? arrayType.getArrayElement() : null;
				checkAssignmentOperand(insn.getArg(2), elementType, "array assignment");
				addArg(code, insn.getArg(0));
				code.add('[');
				addArg(code, insn.getArg(1), false);
				code.add("] = ");
				addArgWithExpectedType(code, insn.getArg(2), elementType);
				break;
			}

			case IGET: {
				FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) insn).getIndex();
				instanceField(code, fieldInfo, insn.getArg(0));
				break;
			}
			case IPUT: {
				checkFieldPutOpcodeOperand(insn);
				FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) insn).getIndex();
				checkAssignmentOperand(insn.getArg(0), fieldInfo.getType(), "field assignment");
				instanceField(code, fieldInfo, insn.getArg(1), insn.contains(AFlag.EXPLICIT_CAST));
				code.add(" = ");
				addArgWithExpectedType(code, insn.getArg(0), fieldInfo.getType());
				break;
			}

			case SGET:
				staticField(code, (FieldInfo) ((IndexInsnNode) insn).getIndex());
				break;
			case SPUT:
				checkFieldPutOpcodeOperand(insn);
				FieldInfo field = (FieldInfo) ((IndexInsnNode) insn).getIndex();
				checkAssignmentOperand(insn.getArg(0), field.getType(), "field assignment");
				staticField(code, field);
				code.add(" = ");
				addArgWithExpectedType(code, insn.getArg(0), field.getType());
				break;

			case STR_CONCAT:
				boolean wrap = state.contains(Flags.BODY_ONLY);
				if (wrap) {
					code.add('(');
				}
				for (Iterator<InsnArg> it = insn.getArguments().iterator(); it.hasNext();) {
					addLambdaObjectOperand(code, it.next());
					if (it.hasNext()) {
						code.add(" + ");
					}
				}
				if (wrap) {
					code.add(')');
				}
				break;

			case MONITOR_ENTER:
				if (isFallback()) {
					code.add("monitor-enter(");
					addArg(code, insn.getArg(0));
					code.add(')');
				}
				break;

			case MONITOR_EXIT:
				if (isFallback()) {
					code.add("monitor-exit(");
					if (insn.getArgsCount() == 1) {
						addArg(code, insn.getArg(0));
					}
					code.add(')');
				}
				break;

			case TERNARY:
				makeTernary((TernaryInsn) insn, code, state);
				break;

			case ONE_ARG:
				addArg(code, insn.getArg(0), state);
				break;

			/* fallback mode instructions */
			case IF:
				fallbackOnlyInsn(insn);
				IfNode ifInsn = (IfNode) insn;
				code.add("if (");
				addArg(code, insn.getArg(0));
				code.add(' ');
				code.add(ifInsn.getOp().getSymbol()).add(' ');
				addArg(code, insn.getArg(1));
				code.add(") goto ").add(MethodGen.getLabelName(ifInsn));
				break;

			case GOTO:
				fallbackOnlyInsn(insn);
				code.add("goto ").add(MethodGen.getLabelName(((GotoNode) insn).getTarget()));
				break;

			case MOVE_EXCEPTION:
				fallbackOnlyInsn(insn);
				code.add("move-exception");
				break;

			case SWITCH:
				fallbackOnlyInsn(insn);
				SwitchInsn sw = (SwitchInsn) insn;
				code.add("switch(");
				addArg(code, insn.getArg(0));
				code.add(") {");
				code.incIndent();
				int[] keys = sw.getKeys();
				int size = keys.length;
				BlockNode[] targetBlocks = sw.getTargetBlocks();
				if (targetBlocks != null) {
					for (int i = 0; i < size; i++) {
						code.startLine("case ").add(Integer.toString(keys[i])).add(": goto ");
						code.add(MethodGen.getLabelName(targetBlocks[i])).add(';');
					}
					code.startLine("default: goto ");
					code.add(MethodGen.getLabelName(sw.getDefTargetBlock())).add(';');
				} else {
					int[] targets = sw.getTargets();
					for (int i = 0; i < size; i++) {
						code.startLine("case ").add(Integer.toString(keys[i])).add(": goto ");
						code.add(MethodGen.getLabelName(targets[i])).add(';');
					}
					code.startLine("default: goto ");
					code.add(MethodGen.getLabelName(sw.getDefaultCaseOffset())).add(';');
				}
				code.decIndent();
				code.startLine('}');
				break;

			case NEW_INSTANCE:
				ArgType newInstanceType = (ArgType) ((IndexInsnNode) insn).getIndex();
				if (!newInstanceType.isObject()) {
					throw new CodegenException("new-instance type must be an object class: " + newInstanceType);
				}
				if (!fallback) {
					throw new CodegenException("new-instance has no matching constructor invocation: " + newInstanceType);
				}
				// only fallback - make new instance in constructor invoke
				fallbackOnlyInsn(insn);
				code.add("new ").add(insn.getResult().getInitType().toString());
				break;

			case PHI:
				fallbackOnlyInsn(insn);
				code.add(insn.getType().toString()).add('(');
				for (InsnArg insnArg : insn.getArguments()) {
					addArg(code, insnArg);
					code.add(' ');
				}
				code.add(')');
				break;

			case MOVE_RESULT:
				fallbackOnlyInsn(insn);
				code.add("move-result");
				break;

			case FILL_ARRAY_DATA:
				fallbackOnlyInsn(insn);
				code.add("fill-array " + insn);
				break;

			case SWITCH_DATA:
				fallbackOnlyInsn(insn);
				code.add(insn.toString());
				break;

			case MOVE_MULTI:
				fallbackOnlyInsn(insn);
				int len = insn.getArgsCount();
				for (int i = 0; i < len - 1; i += 2) {
					addArg(code, insn.getArg(i));
					code.add(" = ");
					addArg(code, insn.getArg(i + 1));
					code.add("; ");
				}
				break;

			case JAVA_JSR:
				fallbackOnlyInsn(insn);
				code.add("jsr -> ").add(MethodGen.getLabelName(((JsrNode) insn).getTarget()));
				break;

			case JAVA_RET:
				fallbackOnlyInsn(insn);
				code.add("ret ");
				addArg(code, insn.getArg(0));
				break;

			default:
				throw new CodegenException(mth, "Unknown instruction: " + insn.getType());
		}
	}

	private static boolean isBooleanToIntMove(InsnNode insn, InsnArg moveArg) {
		if (insn.getResult() == null
				|| insn.getResult().getSVar() == null
				|| !(moveArg instanceof RegisterArg)
				|| ((RegisterArg) moveArg).getSVar() == null) {
			return false;
		}
		ArgType resultType = insn.getResult().getSVar().getCodeVar().getType();
		ArgType sourceType = ((RegisterArg) moveArg).getSVar().getCodeVar().getType();
		return ArgType.INT.equals(resultType) && ArgType.BOOLEAN.equals(sourceType);
	}

	private static boolean isIntToBooleanMove(InsnNode insn, InsnArg moveArg) {
		if (insn.getResult() == null
				|| insn.getResult().getSVar() == null
				|| !(moveArg instanceof RegisterArg)
				|| ((RegisterArg) moveArg).getSVar() == null) {
			return false;
		}
		ArgType resultType = insn.getResult().getSVar().getCodeVar().getType();
		ArgType sourceType = ((RegisterArg) moveArg).getSVar().getCodeVar().getType();
		return ArgType.BOOLEAN.equals(resultType) && ArgType.INT.equals(sourceType);
	}

	private boolean isMoveCastNeeded(InsnNode insn, InsnArg moveArg) {
		if (insn.getResult() == null
				|| insn.getResult().getSVar() == null
				|| !(moveArg instanceof RegisterArg)
				|| ((RegisterArg) moveArg).getSVar() == null) {
			return false;
		}
		ArgType resultType = insn.getResult().getSVar().getCodeVar().getType();
		ArgType sourceType = ((RegisterArg) moveArg).getSVar().getCodeVar().getType();
		if (resultType == null || sourceType == null
				|| !resultType.isTypeKnown() || !sourceType.isTypeKnown()
				|| !resultType.isObject() || !sourceType.isObject()) {
			return false;
		}
		return mth.root().getTypeCompare().compareTypes(resultType, sourceType).isNarrow();
	}

	/**
	 * In most cases must be combined with new array instructions.
	 * Use one by one array fill (can be replaced with System.arrayCopy)
	 */
	private void fillArray(ICodeWriter code, FillArrayInsn arrayNode) throws CodegenException {
		if (!isFallback() && arrayNode.getInvalidArraySize() >= 0) {
			throw new CodegenException("Fill-array payload size " + arrayNode.getSize()
					+ " exceeds array length " + arrayNode.getInvalidArraySize());
		}
		InsnArg arrArg = arrayNode.getArg(0);
		ArgType declaredArrayType = InsnUtils.getDeclaredArrayType(arrArg);
		if (!declaredArrayType.isArray()) {
			declaredArrayType = arrArg.getType();
		}
		if (!isFallback() && !arrayNode.hasCompatibleArrayType(declaredArrayType)) {
			throw new CodegenException("Fill-array payload element width " + arrayNode.getElementSize()
					+ " does not match array element type " + declaredArrayType.getArrayElement());
		}
		if (mth.checkCommentsLevel(CommentsLevel.INFO)) {
			code.add("// fill-array-data instruction");
		}
		code.startLine();
		ArgType arrayType = arrArg.getType();
		ArgType elemType;
		if (arrayType.isTypeKnown() && arrayType.isArray()) {
			elemType = arrayType.getArrayElement();
		} else {
			ArgType elementType = arrayNode.getElementType(); // unknown type
			elemType = elementType.selectFirst();
		}
		List<LiteralArg> args = arrayNode.getLiteralArgs(elemType);
		int len = args.size();
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				code.add(';');
				code.startLine();
			}
			addArg(code, arrArg);
			code.add('[').add(Integer.toString(i)).add("] = ").add(lit(args.get(i)));
		}
	}

	private void oneArgInsn(ICodeWriter code, InsnNode insn, Set<Flags> state, char op) throws CodegenException {
		if (!isFallback() && insn instanceof UnaryNumericNode) {
			UnaryNumericNode unaryInsn = (UnaryNumericNode) insn;
			ArgType invalidSourceType = unaryInsn.getInvalidSourceType();
			if (invalidSourceType != null) {
				throw new CodegenException("Unary operand type " + invalidSourceType
						+ " does not match opcode type " + unaryInsn.getOperandType());
			}
		}
		checkUnaryOperand(insn.getArg(0), op);
		boolean wrap = state.contains(Flags.BODY_ONLY);
		if (wrap) {
			code.add('(');
		}
		code.add(op);
		addArg(code, insn.getArg(0));
		if (wrap) {
			code.add(')');
		}
	}

	private void checkUnaryOperand(InsnArg arg, char op) throws CodegenException {
		if (isFallback()) {
			return;
		}
		ArgType type = getExpressionArgType(arg);
		if (type == null || !type.isTypeKnown() || type.isGenericType()) {
			return;
		}
		if (type.isPrimitive()) {
			if (op == '!') {
				if (ArgType.BOOLEAN.equals(type)) {
					return;
				}
			} else if (op == '-') {
				if (!ArgType.BOOLEAN.equals(type) && !type.isVoid()) {
					return;
				}
			} else if (op == '~') {
				if (ArgType.INT.equals(type) || ArgType.LONG.equals(type)
						|| ArgType.BYTE.equals(type) || ArgType.SHORT.equals(type) || ArgType.CHAR.equals(type)) {
					return;
				}
			}
		} else if (type.isObject()) {
			String cls = type.getObject();
			if (op == '!' && cls.equals("java.lang.Boolean")) {
				return;
			}
			if (op == '-' || op == '~') {
				if (cls.equals("java.lang.Integer") || cls.equals("java.lang.Long")
						|| cls.equals("java.lang.Byte") || cls.equals("java.lang.Short")
						|| cls.equals("java.lang.Character")
						|| (op == '-' && (cls.equals("java.lang.Float") || cls.equals("java.lang.Double")))) {
					return;
				}
			}
		}
		throw new CodegenException("Invalid unary operand type for '" + op + "': " + type);
	}

	private void fallbackOnlyInsn(InsnNode insn) throws CodegenException {
		if (!fallback) {
			String msg = insn.getType() + " instruction can be used only in fallback mode";
			CodegenException e = new CodegenException(msg);
			mth.addError(msg, e);
			mth.getParentClass().getTopParentClass().add(AFlag.RESTART_CODEGEN);
			throw e;
		}
	}

	private void filledNewArray(FilledNewArrayNode insn, ICodeWriter code) throws CodegenException {
		if (!isFallback() && insn.getInvalidArrayType() != null) {
			throw new CodegenException("filled-new-array type is not an array: " + insn.getInvalidArrayType());
		}
		if (!isFallback() && insn.getInvalidElementType() != null) {
			throw new CodegenException("filled-new-array does not support wide element type: " + insn.getInvalidElementType());
		}
		if (!isFallback() && insn.getInvalidArgIndex() >= 0) {
			throw new CodegenException("Array initializer operand " + insn.getInvalidArgIndex() + " type "
					+ insn.getInvalidSourceType() + " does not match element type " + insn.getElemType());
		}
		if (!insn.contains(AFlag.DECLARE_VAR)) {
			code.add("new ");
			useType(code, insn.getArrayType());
		}
		code.add('{');
		int c = insn.getArgsCount();
		int wrap = 0;
		ArgType elementType = insn.getArrayType().getArrayElement();
		for (int i = 0; i < c; i++) {
			checkAssignmentOperand(insn.getArg(i), elementType, "array initializer");
			addArgWithExpectedType(code, insn.getArg(i), elementType);
			if (i + 1 < c) {
				code.add(", ");
			}
			wrap++;
			if (wrap == 1000) {
				code.startLine();
				wrap = 0;
			}
		}
		code.add('}');
	}

	private void makeConstructor(ConstructorInsn insn, ICodeWriter code) throws CodegenException {
		checkInvokeOpcodeOperand(insn);
		ClassNode cls = mth.root().resolveClass(insn.getClassType());
		if (cls != null && cls.isAnonymous() && !fallback) {
			inlineAnonymousConstructor(code, cls, insn);
			return;
		}
		if (insn.isSelf()) {
			throw new JadxRuntimeException("Constructor 'self' invoke must be removed!");
		}
		MethodNode callMth = mth.root().resolveMethod(insn.getCallMth());
		MethodNode refMth = callMth;
		if (callMth != null) {
			MethodReplaceAttr replaceAttr = callMth.get(AType.METHOD_REPLACE);
			if (replaceAttr != null) {
				refMth = replaceAttr.getReplaceMth();
			}
		}

		if (insn.isSuper()) {
			code.attachAnnotation(refMth);
			code.add("super");
		} else if (insn.isThis()) {
			code.attachAnnotation(refMth);
			code.add("this");
		} else {
			boolean forceShortName = addOuterClassInstance(insn, code, callMth);
			code.add("new ");
			if (refMth == null || refMth.contains(AFlag.DONT_GENERATE)) {
				// use class reference if constructor method is missing (default constructor)
				code.attachAnnotation(mth.root().resolveClass(insn.getCallMth().getDeclClass()));
			} else {
				code.attachAnnotation(refMth);
			}
			if (forceShortName) {
				mgen.getClassGen().addClsShortNameForced(code, insn.getClassType());
			} else {
				mgen.getClassGen().addClsName(code, insn.getClassType());
			}
			GenericInfoAttr genericInfoAttr = insn.get(AType.GENERIC_INFO);
			if (genericInfoAttr != null) {
				code.add('<');
				if (genericInfoAttr.isExplicit()) {
					boolean first = true;
					for (ArgType type : genericInfoAttr.getGenericTypes()) {
						if (!first) {
							code.add(',');
						} else {
							first = false;
						}
						mgen.getClassGen().useType(code, type);
					}
				}
				code.add('>');
			} else if (forceShortName && cls != null && !cls.getGenericTypeParameters().isEmpty()) {
				// A parameterized enclosing instance cannot instantiate a raw generic inner class
				// ("outer.new Inner()" is rejected by javac). The erased DEX may not retain the
				// inner arguments, so use diamond inference to preserve a compilable constructor.
				code.add("<>");
			}
		}
		generateMethodArguments(code, insn, 0, callMth);
	}

	private boolean addOuterClassInstance(ConstructorInsn insn, ICodeWriter code, MethodNode callMth) throws CodegenException {
		if (callMth == null || !callMth.contains(AFlag.SKIP_FIRST_ARG)) {
			return false;
		}
		ClassNode ctrCls = callMth.getDeclaringClass();
		if (!ctrCls.isInner() || insn.getArgsCount() == 0) {
			return false;
		}
		InsnArg instArg = insn.getArg(0);
		if (instArg.isThis()) {
			return false;
		}
		// instance arg should be of an outer class type
		ArgType instanceType = instArg.getType();
		ArgType outerType = ctrCls.getDeclaringClass().getType();
		if (!instanceType.equals(outerType)
				&& (!instanceType.isObject() || !outerType.isObject()
						|| !instanceType.getObject().equals(outerType.getObject()))) {
			return false;
		}
		addArgDot(code, instArg);
		// can't use another dot, force short name of class
		return true;
	}

	private void inlineAnonymousConstructor(ICodeWriter code, ClassNode cls, ConstructorInsn insn) throws CodegenException {
		if (!cls.checkProcessed()) {
			mth.root().getProcessClasses().forceProcess(cls);
		}
		cls.ensureProcessed();
		if (this.mth.getParentClass() == cls) {
			ProcessAnonymous.convertToInner(cls);
			mth.getParentClass().getTopParentClass().add(AFlag.RESTART_CODEGEN);
			throw new CodegenException("Anonymous inner class unlimited recursion detected."
					+ " Convert class to inner: " + cls.getClassInfo().getFullName());
		}
		ArgType parent = cls.get(AType.ANONYMOUS_CLASS).getBaseType();
		// hide empty anonymous constructors
		for (MethodNode ctor : cls.getMethods()) {
			if (ctor.contains(AFlag.ANONYMOUS_CONSTRUCTOR)
					&& RegionUtils.isEmpty(ctor.getRegion())) {
				ctor.add(AFlag.DONT_GENERATE);
			}
		}
		code.attachDefinition(cls);
		code.add("new ");
		useClass(code, parent);
		MethodNode callMth = mth.root().resolveMethod(insn.getCallMth());
		if (callMth != null) {
			// copy var names
			List<RegisterArg> mthArgs = callMth.getArgRegs();
			int argsCount = Math.min(insn.getArgsCount(), mthArgs.size());
			for (int i = 0; i < argsCount; i++) {
				InsnArg arg = insn.getArg(i);
				if (arg.isRegister()) {
					RegisterArg mthArg = mthArgs.get(i);
					RegisterArg insnArg = (RegisterArg) arg;
					mthArg.getSVar().setCodeVar(insnArg.getSVar().getCodeVar());
				}
			}
		}
		generateMethodArguments(code, insn, 0, callMth);
		code.add(' ');

		ClassGen classGen = new ClassGen(cls, mgen.getClassGen().getParentGen());
		classGen.setOuterNameGen(mgen.getNameGen());
		classGen.addClassBody(code, true);

		mth.getParentClass().addInlinedClass(cls);
	}

	private void checkInvokeOpcodeOperand(BaseInvokeNode insn) throws CodegenException {
		if (!isFallback() && insn.getInvalidArgIndex() >= 0) {
			throw new CodegenException("Method argument " + insn.getInvalidArgIndex() + " type "
					+ insn.getInvalidSourceType() + " does not match parameter type " + insn.getInvalidExpectedType());
		}
	}

	private void makeInvoke(InvokeNode insn, ICodeWriter code) throws CodegenException {
		checkInvokeOpcodeOperand(insn);
		InvokeType type = insn.getInvokeType();
		if (type == InvokeType.CUSTOM) {
			makeInvokeLambda(code, (InvokeCustomNode) insn);
			return;
		}
		MethodInfo callMth = insn.getCallMth();
		MethodNode callMthNode = mth.root().resolveMethod(callMth);

		if (type == InvokeType.CUSTOM_RAW) {
			makeInvokeCustomRaw((InvokeCustomRawNode) insn, callMthNode, code);
			return;
		}
		if (insn.isPolymorphicCall()) {
			// add missing cast
			code.add('(');
			useType(code, callMth.getReturnType());
			code.add(") ");
		}

		int k = 0;
		switch (type) {
			case DIRECT:
			case VIRTUAL:
			case INTERFACE:
			case POLYMORPHIC:
				InsnArg arg = insn.getArg(0);
				if (needInvokeArg(arg)) {
					ArgType ownerType = callMth.getDeclClass().getType();
					checkInstanceReceiver(arg, ownerType, false, "invoke");
					if (needsObjectReceiverCast(arg, ownerType)) {
						code.add("((");
						useClass(code, ownerType);
						code.add(") ");
						addArg(code, arg);
						code.add(").");
					} else {
						addArgDot(code, arg);
					}
				}
				k++;
				break;

			case SUPER:
				callSuper(code, callMth);
				k++; // use 'super' instead 'this' in 0 arg
				code.add('.');
				break;

			case STATIC:
				ClassInfo insnCls = mth.getParentClass().getClassInfo();
				ClassInfo declClass = callMth.getDeclClass();
				if (!insnCls.equals(declClass)) {
					useClass(code, declClass);
					code.add('.');
				}
				break;
		}
		if (callMthNode != null) {
			code.attachAnnotation(callMthNode);
		}
		if (insn.contains(AFlag.FORCE_RAW_NAME)) {
			code.add(callMth.getName());
		} else {
			if (callMthNode != null) {
				code.add(callMthNode.getAlias());
			} else {
				code.add(callMth.getAlias());
			}
		}
		generateMethodArguments(code, insn, k, callMthNode);
	}

	private void makeInvokeCustomRaw(InvokeCustomRawNode insn,
			@Nullable MethodNode callMthNode, ICodeWriter code) throws CodegenException {
		if (isFallback()) {
			code.add("call_site(");
			code.incIndent();
			for (EncodedValue value : insn.getCallSiteValues()) {
				code.startLine(value.toString());
			}
			code.decIndent();
			code.startLine(").invoke");
			generateMethodArguments(code, insn, 0, callMthNode);
		} else {
			ArgType returnType = insn.getCallMth().getReturnType();
			if (!returnType.isVoid()) {
				code.add('(');
				useType(code, returnType);
				code.add(") ");
			}
			makeInvoke(insn.getResolveInvoke(), code);
			code.add(".dynamicInvoker().invoke");
			generateMethodArguments(code, insn, 0, callMthNode);
			code.add(" /* invoke-custom */");
		}
	}

	// FIXME: add 'this' for equals methods in scope
	private boolean needInvokeArg(InsnArg arg) {
		if (arg.isAnyThis()) {
			if (arg.isThis()) {
				return false;
			}
			ClassNode clsNode = mth.root().resolveClass(arg.getType());
			if (clsNode != null && clsNode.contains(AFlag.DONT_GENERATE)) {
				return false;
			}
		}
		return true;
	}

	private void makeInvokeLambda(ICodeWriter code, InvokeCustomNode customNode) throws CodegenException {
		List<ArgType> lambdaInterfaces = customNode.getLambdaInterfaces();
		if (!lambdaInterfaces.isEmpty()) {
			code.add('(');
			for (int i = 0; i < lambdaInterfaces.size(); i++) {
				if (i != 0) {
					code.add(" & ");
				}
				useType(code, lambdaInterfaces.get(i));
			}
			code.add(") ");
		}
		if (customNode.isUseRef()) {
			makeRefLambda(code, customNode);
			return;
		}
		if (fallback || !customNode.isInlineInsn()) {
			makeSimpleLambda(code, customNode);
			return;
		}
		MethodNode callMth = (MethodNode) customNode.getCallInsn().get(AType.METHOD_DETAILS);
		makeInlinedLambdaMethod(code, customNode, callMth);
	}

	private static boolean isRedundantLambdaInterfaceCast(InsnNode castInsn) {
		if (castInsn.getType() != InsnType.CHECK_CAST) {
			return false;
		}
		ArgType castType = (ArgType) ((IndexInsnNode) castInsn).getIndex();
		InsnArg arg = castInsn.getArg(0);
		while (arg instanceof InsnWrapArg) {
			InsnNode source = ((InsnWrapArg) arg).getWrapInsn();
			if (source instanceof InvokeCustomNode) {
				return ((InvokeCustomNode) source).getLambdaInterfaces().contains(castType);
			}
			if (source.getType() != InsnType.CHECK_CAST || source.getArgsCount() != 1) {
				return false;
			}
			arg = source.getArg(0);
		}
		return false;
	}

	private void makeRefLambda(ICodeWriter code, InvokeCustomNode customNode) throws CodegenException {
		InsnNode callInsn = customNode.getCallInsn();
		if (callInsn instanceof ConstructorInsn) {
			MethodInfo callMth = ((ConstructorInsn) callInsn).getCallMth();
			useClass(code, callMth.getDeclClass());
			code.add("::new");
			return;
		}
		if (callInsn instanceof InvokeNode) {
			InvokeNode invokeInsn = (InvokeNode) callInsn;
			MethodInfo callMth = invokeInsn.getCallMth();
			if (customNode.getHandleType() == MethodHandleType.INVOKE_STATIC) {
				useClass(code, callMth.getDeclClass());
			} else {
				addArg(code, customNode.getArg(0));
			}
			code.add("::").add(callMth.getAlias());
		}
	}

	private void makeSimpleLambda(ICodeWriter code, InvokeCustomNode customNode) {
		try {
			InsnNode callInsn = customNode.getCallInsn();
			MethodInfo implMthInfo = customNode.getImplMthInfo();
			int implArgsCount = implMthInfo.getArgsCount();
			if (implArgsCount == 0) {
				code.add("()");
			} else {
				code.add('(');
				int callArgsCount = callInsn.getArgsCount();
				int startArg = callArgsCount - implArgsCount;
				if (customNode.getHandleType() != MethodHandleType.INVOKE_STATIC
						&& customNode.getArgsCount() > 0
						&& customNode.getArg(0).isThis()) {
					callInsn.getArg(0).add(AFlag.THIS);
				}
				if (startArg >= 0) {
					for (int i = startArg; i < callArgsCount; i++) {
						if (i != startArg) {
							code.add(", ");
						}
						addArg(code, callInsn.getArg(i));
					}
				} else {
					code.add("/* ERROR: " + startArg + " */");
				}
				code.add(')');
			}
			code.add(" -> {");
			if (fallback) {
				code.add(" // ").add(implMthInfo.toString());
			}
			code.incIndent();
			code.startLine();
			if (!implMthInfo.getReturnType().isVoid()) {
				code.add("return ");
			}
			makeInsn(callInsn, code, Flags.INLINE);
			code.add(";");

			code.decIndent();
			code.startLine('}');
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to generate 'invoke-custom' instruction: " + e.getMessage(), e);
		}
	}

	private void makeInlinedLambdaMethod(ICodeWriter code, InvokeCustomNode customNode, MethodNode callMth) throws CodegenException {
		MethodGen callMthGen = new MethodGen(mgen.getClassGen(), callMth);
		NameGen nameGen = callMthGen.getNameGen();
		nameGen.inheritUsedNames(this.mgen.getNameGen());

		List<ArgType> implArgs = customNode.getImplMthInfo().getArgumentsTypes();
		List<RegisterArg> callArgs = callMth.getArgRegs();
		if (implArgs.isEmpty()) {
			code.add("()");
		} else {
			int callArgsCount = callArgs.size();
			int startArg = callArgsCount - implArgs.size();
			if (callArgsCount - startArg > 1) {
				code.add('(');
			}
			for (int i = startArg; i < callArgsCount; i++) {
				if (i != startArg) {
					code.add(", ");
				}
				CodeVar argCodeVar = callArgs.get(i).getSVar().getCodeVar();
				defVar(code, argCodeVar);
			}
			if (callArgsCount - startArg > 1) {
				code.add(')');
			}
		}
		// force set external arg names into call method args
		int extArgsCount = customNode.getArgsCount();
		int startArg = customNode.getHandleType() == MethodHandleType.INVOKE_STATIC ? 0 : 1; // skip 'this' arg
		int callArg = 0;
		for (int i = startArg; i < extArgsCount; i++) {
			InsnArg arg = customNode.getArg(i);
			if (arg.isRegister()) {
				RegisterArg extArg = (RegisterArg) arg;
				RegisterArg callRegArg = callArgs.get(callArg++);
				callRegArg.getSVar().setCodeVar(extArg.getSVar().getCodeVar());
			} else {
				throw new JadxRuntimeException("Unexpected argument type in lambda call: " + arg.getClass().getSimpleName());
			}
		}
		code.add(" -> {");
		code.incIndent();
		callMthGen.addInstructions(code);

		code.decIndent();
		code.startLine('}');
	}

	private void callSuper(ICodeWriter code, MethodInfo callMth) {
		ClassNode callCls = root.resolveClass(callMth.getDeclClass());
		if (callCls != null && callCls.getAccessFlags().isInterface()) {
			useClass(code, callMth.getDeclClass());
			code.add(".super");
			return;
		}
		ClassInfo superCallCls = getClassForSuperCall(callMth);
		if (superCallCls == null) {
			// unknown class, add comment to keep that info
			code.add("super/*").add(callMth.getDeclClass().getFullName()).add("*/");
			return;
		}
		ClassInfo curClass = mth.getParentClass().getClassInfo();
		if (superCallCls.equals(curClass)) {
			code.add("super");
			return;
		}
		// use custom class
		useClass(code, superCallCls);
		code.add(".super");
	}

	/**
	 * Search call class in super types of this
	 * and all parent classes (needed for inlined synthetic calls)
	 */
	@Nullable
	private ClassInfo getClassForSuperCall(MethodInfo callMth) {
		ArgType declClsType = callMth.getDeclClass().getType();
		ClassNode parentNode = mth.getParentClass();
		while (true) {
			ClassInfo parentCls = parentNode.getClassInfo();
			if (ArgType.isInstanceOf(root, parentCls.getType(), declClsType)) {
				return parentCls;
			}
			ClassNode nextParent = parentNode.getParentClass();
			if (nextParent == parentNode) {
				// no parent, class not found
				return null;
			}
			parentNode = nextParent;
		}
	}

	void generateMethodArguments(ICodeWriter code, BaseInvokeNode insn, int startArgNum,
			@Nullable MethodNode mthNode) throws CodegenException {
		int k = startArgNum;
		if (mthNode != null && mthNode.contains(AFlag.SKIP_FIRST_ARG)) {
			k++;
		}
		int argsCount = insn.getArgsCount();
		code.add('(');
		SkipMethodArgsAttr skipAttr = mthNode == null ? null : mthNode.get(AType.SKIP_MTH_ARGS);
		boolean firstArg = true;
		if (k < argsCount) {
			for (int i = k; i < argsCount; i++) {
				InsnArg arg = insn.getArg(i);
				if (arg.contains(AFlag.SKIP_ARG) || (skipAttr != null && skipAttr.isSkip(i - startArgNum))) {
					continue;
				}
				if (firstArg) {
					firstArg = false;
				} else {
					code.add(", ");
				}
				if (i == argsCount - 1 && processVarArg(code, insn, arg)) {
					continue;
				}
				int paramIndex = i - insn.getFirstArgOffset();
				List<ArgType> paramTypes = insn.getCallMth().getArgumentsTypes();
				ArgType expectedType = paramIndex >= 0 && paramIndex < paramTypes.size() ? paramTypes.get(paramIndex) : null;
				checkAssignmentOperand(arg, expectedType, "method argument");
				addArgWithExpectedType(code, arg, expectedType);
			}
		}
		code.add(')');
	}

	void addArgWithExpectedType(ICodeWriter code, InsnArg arg, @Nullable ArgType expectedType) throws CodegenException {
		checkNonNullReferenceLiteral(arg, expectedType);
		if (arg instanceof InsnWrapArg) {
			InsnNode wrapped = ((InsnWrapArg) arg).getWrapInsn();
			if (expectedType != null && expectedType.isGenericType()
					&& wrapped instanceof InvokeCustomNode
					&& ((InvokeCustomNode) wrapped).getLambdaInterfaces().isEmpty()) {
				code.add('(');
				useType(code, expectedType);
				code.add(") ");
				addLambdaObjectCast(code, wrapped, ArgType.OBJECT);
			} else {
				addLambdaObjectCast(code, wrapped, expectedType);
			}
		}
		addArg(code, arg, false);
	}

	private void checkNonNullReferenceLiteral(InsnArg arg, @Nullable ArgType expectedType) throws CodegenException {
		if (!isFallback() && expectedType != null && (expectedType.isObject() || expectedType.isArray())
				&& arg.isLiteral() && ((LiteralArg) arg).getLiteral() != 0
				&& !arg.contains(AType.AUTO_BOXED_LITERAL)) {
			throw new CodegenException("Non-null numeric literal used as reference type: " + expectedType);
		}
	}

	private void checkArrayOperand(InsnArg arg) throws CodegenException {
		checkNonNullReferenceLiteral(arg, ArgType.UNKNOWN_ARRAY);
		if (isFallback()) {
			return;
		}
		ArgType type = getExpressionArgType(arg);
		if (type != null && type.isTypeKnown() && !type.isArray()) {
			throw new CodegenException("Non-array value used as array operand: " + type);
		}
	}

	private void checkArrayIndex(InsnArg arg, String operation) throws CodegenException {
		if (isFallback()) {
			return;
		}
		ArgType type = getExpressionArgType(arg);
		if (type == null || !type.isTypeKnown() || type.isGenericType()) {
			return;
		}
		if (type.isPrimitive()) {
			if (ArgType.INT.equals(type) || ArgType.BYTE.equals(type)
					|| ArgType.SHORT.equals(type) || ArgType.CHAR.equals(type)) {
				return;
			}
		} else if (type.isObject()) {
			String cls = type.getObject();
			if (cls.equals("java.lang.Integer") || cls.equals("java.lang.Byte")
					|| cls.equals("java.lang.Short") || cls.equals("java.lang.Character")) {
				return;
			}
		} else if (!type.isArray()) {
			return;
		}
		throw new CodegenException("Invalid array " + operation + " type: " + type);
	}

	private void checkNumericCmpOperand(InsnNode insn, InsnArg arg) throws CodegenException {
		if (isFallback()) {
			return;
		}
		ArgType expectedType = insn instanceof CmpInsnNode ? ((CmpInsnNode) insn).getOperandType() : null;
		ArgType expressionType = getExpressionArgType(arg);
		checkNumericCmpType(expressionType);
		ArgType invalidType = insn instanceof CmpInsnNode ? ((CmpInsnNode) insn).getInvalidSourceType() : null;
		if (invalidType == null) {
			invalidType = InsnUtils.findDeclaredSourceType(arg, this::isNonNumericCmpType);
		}
		if (invalidType != null) {
			checkNumericCmpType(invalidType);
			if (expectedType != null) {
				throw new CodegenException("CMP operand type " + invalidType + " does not match opcode type " + expectedType);
			}
		}
	}

	private void checkNumericCmpType(ArgType type) throws CodegenException {
		if (isNonNumericCmpType(type)) {
			throw new CodegenException("Non-numeric CMP operand: " + type);
		}
	}

	private boolean isNonNumericCmpType(ArgType type) {
		if (type == null || !type.isTypeKnown() || type.isGenericType()) {
			return false;
		}
		if (type.isPrimitive()) {
			return ArgType.BOOLEAN.equals(type);
		} else if (type.isObject()) {
			String cls = type.getObject();
			return !cls.equals("java.lang.Integer") && !cls.equals("java.lang.Long")
					&& !cls.equals("java.lang.Float") && !cls.equals("java.lang.Double")
					&& !cls.equals("java.lang.Byte") && !cls.equals("java.lang.Short")
					&& !cls.equals("java.lang.Character");
		} else {
			return true;
		}
	}

	private void checkRepeatedCmpOperand(InsnArg arg) throws CodegenException {
		if (arg.isInsnWrap() && !InsnUtils.isRepeatableExpression(arg.unwrap())) {
			throw new CodegenException("CMP operand would be evaluated more than once");
		}
	}

	void checkMonitorOperand(InsnArg arg) throws CodegenException {
		checkNonNullReferenceLiteral(arg, ArgType.OBJECT);
		if (!isFallback()) {
			ArgType type = getExpressionArgType(arg);
			if (type != null && type.isPrimitive()) {
				throw new CodegenException("Primitive value used as monitor operand: " + type);
			}
		}
	}

	private void checkThrowOperand(InsnArg arg) throws CodegenException {
		checkNonNullReferenceLiteral(arg, ArgType.THROWABLE);
		if (isFallback() || arg.isZeroLiteral()) {
			return;
		}
		ArgType type = getExpressionArgType(arg);
		if (type != null && (type.isPrimitive() || type.isArray() || ArgType.OBJECT.equals(type)
				|| (type.isObject() && root.getTypeCompare().compareTypes(type, ArgType.THROWABLE).isConflict()))) {
			throw new CodegenException("Invalid throw operand type: " + type);
		}
	}

	private void checkReturnOperand(InsnArg arg) throws CodegenException {
		checkAssignmentOperand(arg, mth.getReturnType(), "return");
	}

	private void checkFieldPutOpcodeOperand(InsnNode insn) throws CodegenException {
		if (isFallback() || !(insn instanceof FieldPutNode)) {
			return;
		}
		FieldPutNode fieldPut = (FieldPutNode) insn;
		ArgType invalidSourceType = fieldPut.getInvalidSourceType();
		if (invalidSourceType != null) {
			ArgType fieldType = ((FieldInfo) fieldPut.getIndex()).getType();
			throw new CodegenException("Field write operand type " + invalidSourceType
					+ " does not match field type " + fieldType);
		}
	}

	private void checkAssignmentOperand(InsnArg arg, @Nullable ArgType expected, String operation) throws CodegenException {
		if (isFallback()) {
			return;
		}
		if (arg.isInsnWrap()) {
			checkInvalidMoveResult(((InsnWrapArg) arg).getWrapInsn());
		}
		if (arg.isZeroLiteral() && expected != null && (expected.isObject() || expected.isArray())) {
			return; // DEX zero is null in a reference-typed assignment.
		}
		ArgType actual = getAssignmentExpressionType(arg, expected);
		if (actual == null || !actual.isTypeKnown() || expected == null || !expected.isTypeKnown()
				|| actual.isGenericType() || expected.isGenericType()) {
			return;
		}
		if (expected.isPrimitive()) {
			if (actual.isPrimitive() && (isPrimitiveWideningReturn(actual, expected)
					|| isNarrowingIntConstantReturn(arg, actual, expected))) {
				return;
			}
			if (actual.isObject() && isUnboxableReturn(actual, expected)) {
				return;
			}
		} else if (expected.isObject() || expected.isArray()) {
			if (actual.isPrimitive()) {
				ArgType boxed = getBoxedType(actual);
				if (!isMethodParameter(arg) && boxed != null && expected.isObject()
						&& root.getTypeCompare().compareTypes(boxed, expected).isNarrowOrEqual()) {
					return;
				}
			} else if (root.getTypeCompare().compareTypes(actual, expected).isNarrowOrEqual()
					|| root.getTypeCompare().compareTypes(actual,
							expected) == jadx.core.dex.visitors.typeinference.TypeCompareEnum.UNKNOWN) {
				return;
			}
		} else {
			return;
		}
		if (actual.isObject() && expected.isObject()
				&& root.getTypeCompare().compareTypes(actual, expected).isConflict()
				&& !(isKnownClassType(expected)
						? hasCompleteSuperclassChain(actual, new HashSet<>())
						: hasCompleteObjectHierarchy(actual, new HashSet<>()))) {
			// An unresolved superclass or interface can still make this assignment valid.
			return;
		}
		throw new CodegenException("Incompatible " + operation + " type: " + actual + " for " + expected);
	}

	private void checkInvalidMoveResult(InsnNode insn) throws CodegenException {
		if (isFallback()) {
			return;
		}
		InvalidMoveResultAttr invalidMoveResult = insn.get(AType.INVALID_MOVE_RESULT);
		if (invalidMoveResult != null) {
			throw new CodegenException("Move-result opcode " + invalidMoveResult.getOpcode()
					+ " does not match result type " + invalidMoveResult.getResultType());
		}
	}

	private boolean hasCompleteSuperclassChain(ArgType type, Set<String> visited) {
		if (!type.isObject()) {
			return false;
		}
		String name = type.getObject();
		if (!visited.add(name)) {
			return false;
		}
		if (name.equals("java.lang.Object")) {
			return true;
		}
		ClassNode cls = root.resolveClass(ArgType.object(name));
		if (cls != null) {
			ArgType superClass = cls.getSuperClass();
			return superClass != null && hasCompleteSuperclassChain(superClass, visited);
		}
		ClspClass clspCls = root.getClsp().getClsDetails(type);
		if (clspCls == null || clspCls.getParents() == null || clspCls.getParents().length == 0) {
			return false;
		}
		return hasCompleteSuperclassChain(clspCls.getParents()[0], visited);
	}

	private boolean hasCompleteObjectHierarchy(ArgType type, Set<String> visited) {
		if (!type.isObject()) {
			return false;
		}
		String name = type.getObject();
		if (!visited.add(name)) {
			return false;
		}
		try {
			if (name.equals("java.lang.Object")) {
				return true;
			}
			ClassNode cls = root.resolveClass(ArgType.object(name));
			if (cls != null) {
				ArgType superClass = cls.getSuperClass();
				if (superClass == null || !hasCompleteObjectHierarchy(superClass, visited)) {
					return false;
				}
				for (ArgType iface : cls.getInterfaces()) {
					if (!hasCompleteObjectHierarchy(iface, visited)) {
						return false;
					}
				}
				return true;
			}
			ClspClass clspCls = root.getClsp().getClsDetails(type);
			if (clspCls == null || clspCls.getParents() == null) {
				return false;
			}
			for (ArgType parent : clspCls.getParents()) {
				if (!hasCompleteObjectHierarchy(parent, visited)) {
					return false;
				}
			}
			return true;
		} finally {
			visited.remove(name);
		}
	}

	private ArgType getAssignmentExpressionType(InsnArg arg, @Nullable ArgType expected) {
		ArgType declared = getExpressionArgType(arg);
		if (!arg.isInsnWrap()) {
			return declared;
		}
		ArgType rendered = getRenderedArgType(arg);
		if (rendered == null || !rendered.isTypeKnown()) {
			return declared;
		}
		InsnNode producer = ((InsnWrapArg) arg).getWrapInsn();
		if (producer instanceof BaseInvokeNode
				&& root.getMethodUtils().getMethodGenericReturnType((BaseInvokeNode) producer) != null) {
			return rendered;
		}
		if (declared != null && expected != null && expected.containsGeneric()
				&& declared.isObject() && expected.isObject() && rendered.isObject()
				&& declared.getObject().equals(expected.getObject())
				&& declared.getObject().equals(rendered.getObject())) {
			return rendered;
		}
		return declared;
	}

	private static boolean isNarrowingIntConstantReturn(InsnArg arg, ArgType actual, ArgType expected) {
		if (!arg.isLiteral() || !ArgType.INT.equals(actual)) {
			return false;
		}
		long value = ((LiteralArg) arg).getLiteral();
		if (ArgType.BYTE.equals(expected)) {
			return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
		}
		if (ArgType.SHORT.equals(expected)) {
			return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
		}
		return ArgType.CHAR.equals(expected) && value >= Character.MIN_VALUE && value <= Character.MAX_VALUE;
	}

	@Nullable
	private static ArgType getBoxedType(ArgType primitive) {
		if (primitive.equals(ArgType.BOOLEAN)) {
			return ArgType.object("java.lang.Boolean");
		}
		if (primitive.equals(ArgType.BYTE)) {
			return ArgType.object("java.lang.Byte");
		}
		if (primitive.equals(ArgType.SHORT)) {
			return ArgType.object("java.lang.Short");
		}
		if (primitive.equals(ArgType.CHAR)) {
			return ArgType.object("java.lang.Character");
		}
		if (primitive.equals(ArgType.INT)) {
			return ArgType.object("java.lang.Integer");
		}
		if (primitive.equals(ArgType.LONG)) {
			return ArgType.object("java.lang.Long");
		}
		if (primitive.equals(ArgType.FLOAT)) {
			return ArgType.object("java.lang.Float");
		}
		if (primitive.equals(ArgType.DOUBLE)) {
			return ArgType.object("java.lang.Double");
		}
		return null;
	}

	private static boolean isPrimitiveWideningReturn(ArgType actual, ArgType expected) {
		if (actual.equals(expected)) {
			return true;
		}
		if (actual.equals(ArgType.BYTE)) {
			return expected.equals(ArgType.SHORT) || expected.equals(ArgType.INT)
					|| expected.equals(ArgType.LONG) || expected.equals(ArgType.FLOAT) || expected.equals(ArgType.DOUBLE);
		}
		if (actual.equals(ArgType.SHORT) || actual.equals(ArgType.CHAR)) {
			return expected.equals(ArgType.INT) || expected.equals(ArgType.LONG)
					|| expected.equals(ArgType.FLOAT) || expected.equals(ArgType.DOUBLE);
		}
		if (actual.equals(ArgType.INT)) {
			return expected.equals(ArgType.LONG) || expected.equals(ArgType.FLOAT) || expected.equals(ArgType.DOUBLE);
		}
		if (actual.equals(ArgType.LONG)) {
			return expected.equals(ArgType.FLOAT) || expected.equals(ArgType.DOUBLE);
		}
		return actual.equals(ArgType.FLOAT) && expected.equals(ArgType.DOUBLE);
	}

	private static boolean isUnboxableReturn(ArgType actual, ArgType expected) {
		String cls = actual.getObject();
		if (cls.equals("java.lang.Boolean")) {
			return expected.equals(ArgType.BOOLEAN);
		}
		if (cls.equals("java.lang.Byte")) {
			return isPrimitiveWideningReturn(ArgType.BYTE, expected);
		}
		if (cls.equals("java.lang.Short")) {
			return isPrimitiveWideningReturn(ArgType.SHORT, expected);
		}
		if (cls.equals("java.lang.Character")) {
			return isPrimitiveWideningReturn(ArgType.CHAR, expected);
		}
		if (cls.equals("java.lang.Integer")) {
			return isPrimitiveWideningReturn(ArgType.INT, expected);
		}
		if (cls.equals("java.lang.Long")) {
			return isPrimitiveWideningReturn(ArgType.LONG, expected);
		}
		if (cls.equals("java.lang.Float")) {
			return isPrimitiveWideningReturn(ArgType.FLOAT, expected);
		}
		return cls.equals("java.lang.Double") && expected.equals(ArgType.DOUBLE);
	}

	private static ArgType getRenderedArgType(InsnArg arg) {
		if (arg instanceof RegisterArg) {
			RegisterArg reg = (RegisterArg) arg;
			if (reg.getSVar() != null && reg.getSVar().getCodeVar() != null) {
				return reg.getSVar().getCodeVar().getType();
			}
		}
		return arg.getType();
	}

	private void checkInstanceReceiver(InsnArg arg, ArgType ownerType, boolean castReceiver, String operation) throws CodegenException {
		checkNonNullReferenceLiteral(arg, ownerType);
		if (isFallback()) {
			return;
		}
		ArgType renderedType = getExpressionArgType(arg);
		if (renderedType == null) {
			return;
		}
		if (renderedType.isPrimitive()) {
			throw new CodegenException("Primitive value used as " + operation + " receiver: " + renderedType);
		}
		if (!castReceiver && renderedType.isObject() && ownerType.isObject()
				&& root.getTypeCompare().compareTypes(renderedType, ownerType).isConflict()
				&& (isKnownClassType(ownerType)
						? hasCompleteSuperclassChain(renderedType, new HashSet<>())
						: hasCompleteObjectHierarchy(renderedType, new HashSet<>()))) {
			throw new CodegenException("Incompatible " + operation + " receiver: " + renderedType + " for " + ownerType);
		}
		if (!castReceiver && needsObjectReceiverCast(arg, ownerType) && isMethodParameter(arg)) {
			throw new CodegenException("Incompatible " + operation + " receiver: " + renderedType + " for " + ownerType);
		}
	}

	private boolean needsObjectReceiverCast(InsnArg arg, ArgType ownerType) {
		return ArgType.OBJECT.equals(getRenderedArgType(arg))
				&& !ArgType.OBJECT.equals(ownerType) && !arg.isZeroLiteral();
	}

	private boolean isMethodParameter(InsnArg arg) {
		if (!(arg instanceof RegisterArg)) {
			return false;
		}
		SSAVar ssaVar = ((RegisterArg) arg).getSVar();
		if (ssaVar == null) {
			return false;
		}
		for (RegisterArg mthArg : mth.getArgRegs()) {
			if (mthArg.getSVar() == ssaVar) {
				return true;
			}
		}
		return false;
	}

	private void addLambdaObjectOperand(ICodeWriter code, InsnArg arg) throws CodegenException {
		if (!addLambdaObjectOperandIfNeeded(code, arg)) {
			addArg(code, arg);
		}
	}

	boolean addLambdaObjectOperandIfNeeded(ICodeWriter code, InsnArg arg) throws CodegenException {
		if (arg instanceof InsnWrapArg) {
			InsnNode wrapped = ((InsnWrapArg) arg).getWrapInsn();
			if (wrapped instanceof InvokeCustomNode && ((InvokeCustomNode) wrapped).getLambdaInterfaces().isEmpty()) {
				code.add('(');
				addLambdaObjectCast(code, wrapped, ArgType.OBJECT);
				addArg(code, arg, false);
				code.add(')');
				return true;
			}
		}
		return false;
	}

	void addLambdaObjectCast(ICodeWriter code, InsnNode insn, @Nullable ArgType expectedType) {
		if (!ArgType.OBJECT.equals(expectedType) || !(insn instanceof InvokeCustomNode)) {
			return;
		}
		InvokeCustomNode lambda = (InvokeCustomNode) insn;
		if (lambda.getLambdaInterfaces().isEmpty()) {
			code.add('(');
			useType(code, lambda.getCallMth().getReturnType());
			code.add(") ");
		}
	}

	private void addLambdaCastForNonTargetType(ICodeWriter code, InsnArg arg, ArgType castType) {
		if (!(arg instanceof InsnWrapArg)) {
			return;
		}
		InsnNode wrapped = ((InsnWrapArg) arg).getWrapInsn();
		if (!(wrapped instanceof InvokeCustomNode)) {
			return;
		}
		InvokeCustomNode lambda = (InvokeCustomNode) wrapped;
		ArgType lambdaType = lambda.getCallMth().getReturnType();
		if (lambda.getLambdaInterfaces().isEmpty() && !castType.equals(lambdaType)) {
			code.add('(');
			useType(code, lambdaType);
			code.add(") ");
		}
	}

	/**
	 * Expand varArgs from filled array.
	 */
	private boolean processVarArg(ICodeWriter code, BaseInvokeNode invokeInsn, InsnArg lastArg) throws CodegenException {
		if (!invokeInsn.contains(AFlag.VARARG_CALL)) {
			return false;
		}
		if (!lastArg.getType().isArray() || !lastArg.isInsnWrap()) {
			return false;
		}
		InsnNode insn = ((InsnWrapArg) lastArg).getWrapInsn();
		if (insn.getType() != InsnType.FILLED_NEW_ARRAY) {
			return false;
		}
		ArgType arrayType = ((FilledNewArrayNode) insn).getArrayType();
		IMethodDetails details = root.getMethodUtils().getMethodDetails(invokeInsn);
		List<ArgType> paramTypes = details == null
				? invokeInsn.getCallMth().getArgumentsTypes()
				: details.getArgTypes();
		ArgType paramArrayType = paramTypes.isEmpty() ? null : paramTypes.get(paramTypes.size() - 1);
		if (paramArrayType != null && paramArrayType.isArray()
				&& !paramArrayType.getArrayElement().isGenericType()
				&& !sameVarargArrayErasure(paramArrayType, arrayType)) {
			// A covariant array (e.g. String[] passed to Object...) is one array argument,
			// not a sequence of vararg elements.
			return false;
		}
		int count = insn.getArgsCount();
		ArgType elementType = arrayType.getArrayElement();
		for (int i = 0; i < count; i++) {
			InsnArg elemArg = insn.getArg(i);
			InsnNode elemInsn = elemArg instanceof InsnWrapArg ? ((InsnWrapArg) elemArg).getWrapInsn() : null;
			if (elemInsn instanceof InvokeCustomNode && ((InvokeCustomNode) elemInsn).isUseRef()) {
				ArgType targetType = inferMethodRefVarArgType((InvokeCustomNode) elemInsn);
				if (targetType != null) {
					code.add('(');
					useType(code, targetType);
					code.add(") ");
				}
				addArg(code, elemArg, false);
			} else if (elemInsn instanceof InvokeCustomNode) {
				// A varargs call can target T... even when the allocated array has a concrete
				// element type. T gives a lambda expression no functional target at the call site.
				// The instantiated SAM signature supplies the target for lambda parameter types,
				// including erased parameters that the body dereferences or unboxes.
				ArgType targetType = inferMethodRefVarArgType((InvokeCustomNode) elemInsn);
				if (targetType != null) {
					code.add('(');
					useType(code, targetType);
					code.add(") ");
				} else {
					addLambdaObjectCast(code, elemInsn, ArgType.OBJECT);
				}
				addArg(code, elemArg, false);
			} else {
				checkAssignmentOperand(elemArg, elementType, "vararg element");
				if (count == 1 && (elemArg.getType().isArray()
						|| elemArg.isZeroLiteral() && elemArg.getType().isObject())) {
					// Without this cast a single null/array value can bind to the whole
					// varargs array parameter instead of becoming one element.
					code.add('(');
					useType(code, elementType);
					code.add(") ");
				}
				addArgWithExpectedType(code, elemArg, elementType);
			}
			if (i < count - 1) {
				code.add(", ");
			}
		}
		return true;
	}

	private static boolean sameVarargArrayErasure(ArgType formal, ArgType actual) {
		if (formal.equals(actual)) {
			return true;
		}
		ArgType formalElement = formal.getArrayElement();
		ArgType actualElement = actual.getArrayElement();
		return formalElement.isObject() && actualElement.isObject()
				&& formalElement.getObject().equals(actualElement.getObject());
	}

	@Nullable
	private ArgType inferMethodRefVarArgType(InvokeCustomNode lambda) {
		ArgType interfaceType = lambda.getCallMth().getReturnType();
		MethodInfo instantiated = lambda.getInstantiatedMthInfo();
		if (instantiated == null) {
			return null;
		}
		List<ArgType> typeVars = root.getTypeUtils().getClassGenerics(interfaceType);
		if (typeVars.isEmpty()) {
			return interfaceType;
		}
		ClassNode resolved = root.resolveClass(interfaceType);
		ArgType directTarget = inferMethodRefTypeFromClass(interfaceType, typeVars, instantiated,
				interfaceType, Map.of());
		if (directTarget != null) {
			return directTarget;
		}
		if (resolved != null) {
			ClassTypeVarsAttr typeVarsAttr = root.getTypeUtils().getClassTypeVars(interfaceType);
			if (typeVarsAttr != null) {
				ArgType[] inheritedTarget = new ArgType[1];
				root.getTypeUtils().visitSuperTypes(interfaceType, (source, parent) -> {
					if (inheritedTarget[0] == null) {
						Map<ArgType, ArgType> inheritedMap = typeVarsAttr.getTypeVarsMapFor(parent);
						if (!inheritedMap.isEmpty()) {
							inheritedTarget[0] = inferMethodRefTypeFromClass(interfaceType, typeVars,
									instantiated, parent, inheritedMap);
						}
					}
				});
				return inheritedTarget[0];
			}
		}
		return null;
	}

	@Nullable
	private ArgType inferMethodRefTypeFromClass(ArgType interfaceType, List<ArgType> typeVars,
			MethodInfo instantiated, ArgType declaringType, Map<ArgType, ArgType> typeVarMap) {
		ClassNode resolved = root.resolveClass(declaringType);
		if (resolved != null) {
			for (MethodNode candidate : resolved.getMethods()) {
				if (candidate.getAccessFlags().isAbstract()
						&& candidate.getName().equals(instantiated.getName())) {
					ArgType target = inferMethodRefVarArgType(interfaceType, typeVars,
							candidate.getArgTypes(), candidate.getReturnType(), instantiated, typeVarMap);
					if (target != null) {
						return target;
					}
				}
			}
		}
		ClspClass clspClass = root.getClsp().getClsDetails(declaringType);
		if (clspClass != null) {
			for (ClspMethod candidate : clspClass.getMethodsArray()) {
				if (AccessFlags.hasFlag(candidate.getRawAccessFlags(), AccessFlags.ABSTRACT)
						&& candidate.getMethodInfo().getName().equals(instantiated.getName())) {
					ArgType target = inferMethodRefVarArgType(interfaceType, typeVars,
							candidate.getArgTypes(), candidate.getReturnType(), instantiated, typeVarMap);
					if (target != null) {
						return target;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static ArgType inferMethodRefVarArgType(ArgType interfaceType, List<ArgType> typeVars,
			List<ArgType> formalArgs, ArgType formalReturn, MethodInfo instantiated,
			Map<ArgType, ArgType> typeVarMap) {
		List<ArgType> actualArgs = instantiated.getArgumentsTypes();
		if (formalArgs.size() != actualArgs.size()) {
			return null;
		}
		Map<String, ArgType> bindings = new HashMap<>();
		for (int i = 0; i < formalArgs.size(); i++) {
			if (!bindMethodRefType(formalArgs.get(i), actualArgs.get(i), bindings, typeVarMap)) {
				return null;
			}
		}
		if (!bindMethodRefType(formalReturn, instantiated.getReturnType(), bindings, typeVarMap)) {
			return null;
		}
		List<ArgType> resolved = new ArrayList<>(typeVars.size());
		for (ArgType typeVar : typeVars) {
			ArgType type = bindings.get(typeVar.getObject());
			if (type == null || type.isPrimitive()) {
				return null;
			}
			resolved.add(type);
		}
		return ArgType.generic(interfaceType, resolved);
	}

	private static boolean bindMethodRefType(ArgType formal, ArgType actual, Map<String, ArgType> bindings,
			Map<ArgType, ArgType> typeVarMap) {
		if (formal.isGenericType()) {
			ArgType sourceVar = typeVarMap.getOrDefault(formal, formal);
			if (!sourceVar.isGenericType()) {
				return sourceVar.equals(actual);
			}
			ArgType previous = bindings.putIfAbsent(sourceVar.getObject(), actual);
			return previous == null || previous.equals(actual);
		}
		if (formal.isArray() && actual.isArray()) {
			return bindMethodRefType(formal.getArrayElement(), actual.getArrayElement(), bindings, typeVarMap);
		}
		if (formal.isGeneric() && actual.isGeneric() && formal.getObject().equals(actual.getObject())) {
			List<ArgType> formalTypes = formal.getGenericTypes();
			List<ArgType> actualTypes = actual.getGenericTypes();
			if (formalTypes.size() != actualTypes.size()) {
				return false;
			}
			for (int i = 0; i < formalTypes.size(); i++) {
				if (!bindMethodRefType(formalTypes.get(i), actualTypes.get(i), bindings, typeVarMap)) {
					return false;
				}
			}
		}
		return true;
	}

	private void makeTernary(TernaryInsn insn, ICodeWriter code, Set<Flags> state) throws CodegenException {
		boolean wrap = state.contains(Flags.BODY_ONLY);
		if (wrap) {
			code.add('(');
		}
		InsnArg first = insn.getArg(0);
		InsnArg second = insn.getArg(1);
		ConditionGen condGen = new ConditionGen(this);
		if (first.isTrue() && second.isFalse()) {
			condGen.add(code, insn.getCondition());
		} else {
			condGen.wrap(code, insn.getCondition());
			code.add(" ? ");
			if (!addLambdaObjectOperandIfNeeded(code, first)) {
				addArg(code, first, false);
			}
			code.add(" : ");
			if (!addLambdaObjectOperandIfNeeded(code, second)) {
				addArg(code, second, false);
			}
		}
		if (wrap) {
			code.add(')');
		}
	}

	private void makeArith(ArithNode insn, ICodeWriter code, Set<Flags> state) throws CodegenException {
		// String concatenation is represented as ADD by the Java input plugin.
		// A dex numeric opcode retains its numeric result init type, even if an
		// invalid input later causes a String operand to be inferred.
		boolean stringConcat = insn.getOp() == ArithOp.ADD
				&& (insn.contains(AFlag.ARITH_ONEARG)
						? ArgType.STRING.equals(getExpressionArgType(insn.getArg(0)))
						: insn.getResult() != null && ArgType.STRING.equals(insn.getResult().getInitType()));
		if (!stringConcat) {
			checkArithOpcodeOperand(insn);
			checkArithOperand(insn.getArg(0), insn.getOp());
			checkArithOperand(insn.getArg(1), insn.getOp());
		}
		boolean firstBoolean = ArgType.BOOLEAN.equals(getExpressionArgType(insn.getArg(0)));
		boolean secondBoolean = ArgType.BOOLEAN.equals(getExpressionArgType(insn.getArg(1)));
		boolean mixedBooleanBitOp = insn.getOp().isBitOp() && firstBoolean != secondBoolean;
		if (insn.contains(AFlag.ARITH_ONEARG)) {
			if (!isFallback() && mixedBooleanBitOp) {
				throw new CodegenException("Unsupported mixed boolean compound bit operation: " + insn.getOp());
			}
			makeArithOneArg(insn, code);
			return;
		}
		// wrap insn in brackets for save correct operation order
		boolean wrap = state.contains(Flags.BODY_ONLY) && !insn.contains(AFlag.DONT_WRAP);
		if (wrap) {
			code.add('(');
		}
		addNumericBitwiseArg(code, insn.getArg(0), mixedBooleanBitOp && firstBoolean);
		code.add(' ');
		code.add(insn.getOp().getSymbol());
		code.add(' ');
		addNumericBitwiseArg(code, insn.getArg(1), mixedBooleanBitOp && secondBoolean);
		if (wrap) {
			code.add(')');
		}
	}

	private void addNumericBitwiseArg(ICodeWriter code, InsnArg arg, boolean booleanAsInt) throws CodegenException {
		if (booleanAsInt) {
			code.add('(');
			addArg(code, arg);
			code.add(" ? 1 : 0)");
		} else {
			addArg(code, arg);
		}
	}

	private void checkArithOperand(InsnArg arg, ArithOp op) throws CodegenException {
		if (isFallback()) {
			return;
		}
		if (arg.isInsnWrap()) {
			ArgType declared = InsnUtils.getDeclaredResultType(((InsnWrapArg) arg).getWrapInsn());
			if (declared != null && declared.isTypeKnown() && !declared.isPrimitive()) {
				// A dex numeric opcode does not perform Java source-level unboxing.
				throw new CodegenException("Invalid " + op + " operand type: " + declared);
			}
		}
		ArgType type = getExpressionArgType(arg);
		if (type == null || !type.isTypeKnown() || type.isGenericType()) {
			return;
		}
		boolean bitOp = op.isBitOp();
		boolean shift = op == ArithOp.SHL || op == ArithOp.SHR || op == ArithOp.USHR;
		if (type.isPrimitive()) {
			if (ArgType.BOOLEAN.equals(type) && bitOp) {
				return;
			}
			if (ArgType.INT.equals(type) || ArgType.LONG.equals(type)
					|| ArgType.BYTE.equals(type) || ArgType.SHORT.equals(type) || ArgType.CHAR.equals(type)) {
				return;
			}
			if (!bitOp && !shift && (ArgType.FLOAT.equals(type) || ArgType.DOUBLE.equals(type))) {
				return;
			}
		} else if (type.isObject()) {
			if (isMethodParameter(arg)) {
				// A numeric dex opcode cannot implicitly unbox a reference parameter.
				throw new CodegenException("Invalid " + op + " operand type: " + type);
			}
			String cls = type.getObject();
			if (cls.equals("java.lang.Boolean") && bitOp) {
				return;
			}
			if (cls.equals("java.lang.Integer") || cls.equals("java.lang.Long")
					|| cls.equals("java.lang.Byte") || cls.equals("java.lang.Short")
					|| cls.equals("java.lang.Character")) {
				return;
			}
			if (!bitOp && !shift && (cls.equals("java.lang.Float") || cls.equals("java.lang.Double"))) {
				return;
			}
		}
		throw new CodegenException("Invalid " + op + " operand type: " + type);
	}

	private void checkArithOpcodeOperand(ArithNode insn) throws CodegenException {
		if (isFallback()) {
			return;
		}
		ArgType invalidSourceType = insn.getInvalidSourceType();
		if (invalidSourceType != null) {
			throw new CodegenException("ARITH operand type " + invalidSourceType
					+ " does not match opcode type " + insn.getInvalidExpectedType());
		}
	}

	static ArgType getExpressionArgType(InsnArg arg) {
		if (arg.isInsnWrap()) {
			InsnNode wrapped = ((InsnWrapArg) arg).getWrapInsn();
			ArgType declared = InsnUtils.getDeclaredResultType(wrapped);
			if (declared != null) {
				return declared;
			}
		}
		return getRenderedArgType(arg);
	}

	private boolean isKnownClassType(ArgType type) {
		ClassNode cls = root.resolveClass(type);
		if (cls != null) {
			return !cls.getAccessFlags().isInterface();
		}
		ClspClass clspCls = root.getClsp().getClsDetails(type);
		return clspCls != null && !clspCls.isInterface();
	}

	boolean isKnownFinalClassType(ArgType type) {
		ClassNode cls = root.resolveClass(type);
		if (cls != null) {
			return !cls.getAccessFlags().isInterface() && cls.getAccessFlags().isFinal();
		}
		ClspClass clspCls = root.getClsp().getClsDetails(type);
		return clspCls != null && !clspCls.isInterface() && clspCls.hasAccFlag(AccessFlags.FINAL);
	}

	private boolean isKnownInterfaceType(ArgType type) {
		ClassNode cls = root.resolveClass(type);
		if (cls != null) {
			return cls.getAccessFlags().isInterface();
		}
		ClspClass clspCls = root.getClsp().getClsDetails(type);
		return clspCls != null && clspCls.isInterface();
	}

	boolean isDefinitelyIncompatibleObjectTypes(ArgType source, ArgType target) {
		if (!source.isObject() || !target.isObject()
				|| !root.getTypeCompare().compareTypes(source, target).isConflict()) {
			return false;
		}
		return (isKnownClassType(source) && isKnownClassType(target))
				|| (isKnownFinalClassType(source) && isKnownInterfaceType(target))
				|| (isKnownInterfaceType(source) && isKnownFinalClassType(target));
	}

	private boolean isDefinitelyIncompatibleArrayTypes(ArgType source, ArgType target) {
		if (!source.isArray() || !target.isArray()) {
			return false;
		}
		ArgType sourceElement = source.getArrayElement();
		ArgType targetElement = target.getArrayElement();
		if (sourceElement.isPrimitive() || targetElement.isPrimitive()) {
			return !sourceElement.equals(targetElement);
		}
		if (sourceElement.isArray()) {
			return targetElement.isArray()
					? isDefinitelyIncompatibleArrayTypes(sourceElement, targetElement)
					: isDefinitelyIncompatibleArrayObjectTypes(targetElement);
		}
		if (targetElement.isArray()) {
			return isDefinitelyIncompatibleArrayObjectTypes(sourceElement);
		}
		return isDefinitelyIncompatibleObjectTypes(sourceElement, targetElement);
	}

	private boolean isDefinitelyIncompatibleArrayObjectTypes(ArgType type) {
		if (!type.isObject() || type.isGenericType()) {
			return false;
		}
		String name = type.getObject();
		return !name.equals("java.lang.Object")
				&& !name.equals("java.lang.Cloneable")
				&& !name.equals("java.io.Serializable");
	}

	private boolean isDefinitelyIncompatibleArrayAndObjectTypes(ArgType source, ArgType target) {
		return (source.isArray() && target.isObject() && isDefinitelyIncompatibleArrayObjectTypes(target))
				|| (source.isObject() && target.isArray() && isDefinitelyIncompatibleArrayObjectTypes(source));
	}

	boolean isDefinitelyIncompatibleReferenceTypes(ArgType source, ArgType target) {
		return isDefinitelyIncompatibleObjectTypes(source, target)
				|| isDefinitelyIncompatibleArrayTypes(source, target)
				|| isDefinitelyIncompatibleArrayAndObjectTypes(source, target);
	}

	private void makeArithOneArg(ArithNode insn, ICodeWriter code) throws CodegenException {
		ArithOp op = insn.getOp();
		InsnArg resArg = insn.getArg(0);
		InsnArg arg = insn.getArg(1);

		// "++" or "--"
		if (arg.isLiteral() && (op == ArithOp.ADD || op == ArithOp.SUB)) {
			LiteralArg lit = (LiteralArg) arg;
			if (lit.getLiteral() == 1 && lit.isInteger()) {
				addArg(code, resArg, false);
				String opSymbol = op.getSymbol();
				code.add(opSymbol).add(opSymbol);
				return;
			}
		}

		// +=, -=, ...
		addArg(code, resArg, false);
		code.add(' ').add(op.getSymbol()).add("= ");
		addArg(code, arg, false);
	}
}
