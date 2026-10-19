package jadx.core.codegen;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import jadx.api.ICodeWriter;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.ArithNode;
import jadx.core.dex.instructions.BaseInvokeNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.PrimitiveType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.regions.conditions.Compare;
import jadx.core.dex.regions.conditions.IfCondition;
import jadx.core.dex.regions.conditions.IfCondition.Mode;
import jadx.core.utils.exceptions.CodegenException;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class ConditionGen extends InsnGen {

	private static class CondStack {
		private final Queue<IfCondition> stack = new ArrayDeque<>();

		public Queue<IfCondition> getStack() {
			return stack;
		}

		public void push(IfCondition cond) {
			stack.add(cond);
		}

		public IfCondition pop() {
			return stack.poll();
		}
	}

	public ConditionGen(InsnGen insnGen) {
		super(insnGen.mgen, insnGen.fallback);
	}

	public void add(ICodeWriter code, IfCondition condition) throws CodegenException {
		add(code, new CondStack(), condition);
	}

	void wrap(ICodeWriter code, IfCondition condition) throws CodegenException {
		wrap(code, new CondStack(), condition);
	}

	private void add(ICodeWriter code, CondStack stack, IfCondition condition) throws CodegenException {
		stack.push(condition);
		switch (condition.getMode()) {
			case COMPARE:
				addCompare(code, stack, condition.getCompare(), condition.getCompare().getOp());
				break;

			case TERNARY:
				addTernary(code, stack, condition);
				break;

			case NOT:
				addNot(code, stack, condition);
				break;

			case AND:
			case OR:
				addAndOr(code, stack, condition);
				break;

			default:
				throw new JadxRuntimeException("Unknown condition mode: " + condition.getMode());
		}
		stack.pop();
	}

	private void wrap(ICodeWriter code, CondStack stack, IfCondition cond) throws CodegenException {
		boolean wrap = isWrapNeeded(cond);
		if (wrap) {
			code.add('(');
		}
		add(code, stack, cond);
		if (wrap) {
			code.add(')');
		}
	}

	private void wrap(ICodeWriter code, InsnArg firstArg) throws CodegenException {
		boolean wrap = isArgWrapNeeded(firstArg);
		if (wrap) {
			code.add('(');
		}
		addArg(code, firstArg, false);
		if (wrap) {
			code.add(')');
		}
	}

	private void addCompare(ICodeWriter code, CondStack stack, Compare compare, IfOp op) throws CodegenException {
		InsnArg firstArg = compare.getA();
		InsnArg secondArg = compare.getB();
		if (addFloatingCmp(code, firstArg, secondArg, op)) {
			return;
		}
		boolean firstBoolean = isBooleanConditionArg(firstArg);
		boolean secondBoolean = isBooleanConditionArg(secondArg);
		if ((firstBoolean || secondBoolean)
				&& op != IfOp.EQ && op != IfOp.NE) {
			throw new CodegenException("Unsupported relational boolean condition " + op.getSymbol());
		}
		if (firstBoolean
				&& secondArg.isLiteral()
				&& isBooleanLiteral((LiteralArg) secondArg)) {
			LiteralArg lit = (LiteralArg) secondArg;
			if (lit.getLiteral() == 0) {
				op = op.invert();
			}
			if (op == IfOp.EQ) {
				// == true
				if (stack.getStack().size() == 1) {
					addArg(code, firstArg, false);
				} else {
					wrap(code, firstArg);
				}
				return;
			} else if (op == IfOp.NE) {
				// != true
				code.add('!');
				wrap(code, firstArg);
				return;
			}
		}
		if (secondBoolean && firstArg.isLiteral() && isBooleanLiteral((LiteralArg) firstArg)) {
			IfOp effectiveOp = ((LiteralArg) firstArg).getLiteral() == 0 ? op.invert() : op;
			if (effectiveOp == IfOp.EQ) {
				addArg(code, secondArg, false);
			} else {
				code.add('!');
				wrap(code, secondArg);
			}
			return;
		}
		if (firstBoolean != secondBoolean) {
			throw new CodegenException("Unsupported mixed boolean condition " + op.getSymbol());
		}
		checkReferenceComparison(firstArg, secondArg);

		if (!addLambdaObjectOperandIfNeeded(code, firstArg)) {
			addArg(code, firstArg, isArgWrapNeeded(firstArg));
		}
		code.add(' ').add(op.getSymbol()).add(' ');
		if (!addLambdaObjectOperandIfNeeded(code, secondArg)) {
			addArg(code, secondArg, isArgWrapNeeded(secondArg));
		}
	}

	private void checkReferenceComparison(InsnArg firstArg, InsnArg secondArg) throws CodegenException {
		if (fallback || firstArg.isZeroLiteral() || secondArg.isZeroLiteral()) {
			return;
		}
		ArgType firstType = getExpressionArgType(firstArg);
		ArgType secondType = getExpressionArgType(secondArg);
		if (firstType != null && secondType != null && firstType.isObject() && secondType.isObject()
				&& !firstType.isGenericType() && !secondType.isGenericType()
				&& isKnownFinalClassType(firstType) && isKnownFinalClassType(secondType)
				&& root.getTypeCompare().compareTypes(firstType, secondType).isConflict()) {
			throw new CodegenException("Incompatible reference comparison: " + firstType + " and " + secondType);
		}
	}

	/**
	 * Render a floating-point {@code cmp_l/cmp_g} followed by a comparison with zero without
	 * losing the bytecode instruction's NaN bias. In particular, negating {@code >=} is not
	 * equivalent to {@code <} for NaN, so the unsafe relations must remain explicitly negated.
	 */
	private boolean addFloatingCmp(ICodeWriter code, InsnArg firstArg, InsnArg secondArg, IfOp op)
			throws CodegenException {
		if (!firstArg.isInsnWrap()
				|| !secondArg.isLiteral()
				|| ((LiteralArg) secondArg).getLiteral() != 0) {
			return false;
		}
		InsnNode cmpInsn = firstArg.unwrap();
		InsnType cmpType = cmpInsn.getType();
		if (cmpType != InsnType.CMP_L && cmpType != InsnType.CMP_G) {
			return false;
		}
		ArgType cmpArgType = cmpInsn.getArg(0).getType();
		if (!cmpArgType.contains(PrimitiveType.FLOAT) && !cmpArgType.contains(PrimitiveType.DOUBLE)) {
			return false;
		}
		IfOp directOp = op;
		boolean negate = false;
		if (cmpType == InsnType.CMP_L) {
			if (op == IfOp.LT || op == IfOp.LE) {
				directOp = op.invert();
				negate = true;
			}
		} else if (op == IfOp.GT || op == IfOp.GE) {
			directOp = op.invert();
			negate = true;
		}
		if (negate) {
			code.add("!(");
		}
		InsnArg left = cmpInsn.getArg(0);
		InsnArg right = cmpInsn.getArg(1);
		addArg(code, left, isArgWrapNeeded(left));
		code.add(' ').add(directOp.getSymbol()).add(' ');
		addArg(code, right, isArgWrapNeeded(right));
		if (negate) {
			code.add(')');
		}
		return true;
	}

	private static boolean isBooleanConditionArg(InsnArg arg) {
		if (ArgType.BOOLEAN.equals(arg.getType())) {
			return true;
		}
		if (arg.isRegister()) {
			RegisterArg register = (RegisterArg) arg;
			if (register.getSVar() != null && register.getSVar().getCodeVar() != null
					&& ArgType.BOOLEAN.equals(register.getSVar().getCodeVar().getType())) {
				return true;
			}
		}
		if (!arg.isInsnWrap()) {
			return false;
		}
		InsnNode wrappedInsn = arg.unwrap();
		RegisterArg result = wrappedInsn.getResult();
		if (result != null && ArgType.BOOLEAN.equals(result.getInitType())) {
			return true;
		}
		return wrappedInsn instanceof BaseInvokeNode
				&& ArgType.BOOLEAN.equals(((BaseInvokeNode) wrappedInsn).getCallMth().getReturnType());
	}

	private static boolean isBooleanLiteral(LiteralArg literal) {
		long value = literal.getLiteral();
		return value == 0 || value == 1;
	}

	private void addTernary(ICodeWriter code, CondStack stack, IfCondition condition) throws CodegenException {
		add(code, stack, condition.first());
		code.add(" ? ");
		add(code, stack, condition.second());
		code.add(" : ");
		add(code, stack, condition.third());
	}

	private void addNot(ICodeWriter code, CondStack stack, IfCondition condition) throws CodegenException {
		code.add('!');
		wrap(code, stack, condition.getArgs().get(0));
	}

	private void addAndOr(ICodeWriter code, CondStack stack, IfCondition condition) throws CodegenException {
		String mode = condition.getMode() == Mode.AND ? " && " : " || ";
		Iterator<IfCondition> it = condition.getArgs().iterator();
		IfCondition current = it.next();
		while (it.hasNext()) {
			IfCondition next = it.next();
			wrap(code, stack, current);
			code.add(mode);
			current = next;
		}
		wrap(code, stack, current);
	}

	private boolean isWrapNeeded(IfCondition condition) {
		if (condition.isCompare() || condition.contains(AFlag.DONT_WRAP)) {
			return false;
		}
		return condition.getMode() != Mode.NOT;
	}

	private static boolean isArgWrapNeeded(InsnArg arg) {
		if (!arg.isInsnWrap()) {
			return false;
		}
		InsnNode insn = ((InsnWrapArg) arg).getWrapInsn();
		InsnType insnType = insn.getType();
		if (insnType == InsnType.ARITH) {
			switch (((ArithNode) insn).getOp()) {
				case ADD:
				case SUB:
				case MUL:
				case DIV:
				case REM:
					return false;

				default:
					return true;
			}
		} else {
			switch (insnType) {
				case INVOKE:
				case SGET:
				case IGET:
				case AGET:
				case CONST:
				case ARRAY_LENGTH:
					return false;

				default:
					return true;
			}
		}
	}
}
