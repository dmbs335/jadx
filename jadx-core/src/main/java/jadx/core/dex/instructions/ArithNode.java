package jadx.core.dex.instructions;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.insns.InsnData;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class ArithNode extends InsnNode {

	public static ArithNode build(InsnData insn, ArithOp op, ArgType type) {
		RegisterArg resArg = InsnArg.reg(insn, 0, fixResultType(op, type));
		ArgType argType = fixArgType(op, type);
		switch (insn.getRegsCount()) {
			case 2:
				return new ArithNode(op, resArg, InsnArg.reg(insn, 0, argType), InsnArg.reg(insn, 1, argType), type);
			case 3:
				return new ArithNode(op, resArg, InsnArg.reg(insn, 1, argType), InsnArg.reg(insn, 2, argType), type);
			default:
				throw new JadxRuntimeException("Unexpected registers count in " + insn);
		}
	}

	public static ArithNode buildLit(InsnData insn, ArithOp op, ArgType type) {
		RegisterArg resArg = InsnArg.reg(insn, 0, fixResultType(op, type));
		ArgType argType = fixArgType(op, type);
		LiteralArg litArg = InsnArg.lit(insn, argType);
		switch (insn.getRegsCount()) {
			case 1:
				return new ArithNode(op, resArg, InsnArg.reg(insn, 0, argType), litArg, type);
			case 2:
				return new ArithNode(op, resArg, InsnArg.reg(insn, 1, argType), litArg, type);
			default:
				throw new JadxRuntimeException("Unexpected registers count in " + insn);
		}
	}

	private static ArgType fixResultType(ArithOp op, ArgType type) {
		if (type == ArgType.INT && op.isBitOp()) {
			return ArgType.INT_BOOLEAN;
		}
		return type;
	}

	private static ArgType fixArgType(ArithOp op, ArgType type) {
		if (type == ArgType.INT && op.isBitOp()) {
			return ArgType.NARROW_NUMBERS_NO_FLOAT;
		}
		return type;
	}

	private final ArithOp op;
	private final @Nullable ArgType opcodeType;
	private @Nullable ArgType invalidSourceType;
	private @Nullable ArgType invalidExpectedType;

	public ArithNode(ArithOp op, @Nullable RegisterArg res, InsnArg a, InsnArg b) {
		this(op, res, a, b, null);
	}

	public ArithNode(ArithOp op, @Nullable RegisterArg res, InsnArg a, InsnArg b, @Nullable ArgType opcodeType) {
		super(InsnType.ARITH, 2);
		this.op = op;
		this.opcodeType = opcodeType;
		setResult(res);
		addArg(a);
		addArg(b);
	}

	/**
	 * Create one argument arithmetic instructions (a+=2).
	 * Result is not set (null).
	 *
	 * @param res argument to change
	 */
	public static ArithNode oneArgOp(ArithOp op, InsnArg res, InsnArg a) {
		ArithNode insn = new ArithNode(op, null, res, a);
		insn.add(AFlag.ARITH_ONEARG);
		return insn;
	}

	public ArithOp getOp() {
		return op;
	}

	public @Nullable ArgType getOpcodeType() {
		return opcodeType;
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public @Nullable ArgType getInvalidExpectedType() {
		return invalidExpectedType;
	}

	public void setInvalidSourceType(ArgType sourceType, ArgType expectedType) {
		this.invalidSourceType = sourceType;
		this.invalidExpectedType = expectedType;
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ArithNode) || !super.isSame(obj)) {
			return false;
		}
		ArithNode other = (ArithNode) obj;
		return op == other.op && Objects.equals(opcodeType, other.opcodeType) && isSameLiteral(other);
	}

	private boolean isSameLiteral(ArithNode other) {
		InsnArg thisSecond = getArg(1);
		InsnArg otherSecond = other.getArg(1);
		if (thisSecond.isLiteral() != otherSecond.isLiteral()) {
			return false;
		}
		if (!thisSecond.isLiteral()) {
			// both not literals
			return true;
		}
		// both literals
		long thisLit = ((LiteralArg) thisSecond).getLiteral();
		long otherLit = ((LiteralArg) otherSecond).getLiteral();
		return thisLit == otherLit;
	}

	@Override
	public InsnNode copy() {
		ArithNode copy = new ArithNode(op, null, getArg(0).duplicate(), getArg(1).duplicate(), opcodeType);
		copy.invalidSourceType = invalidSourceType;
		copy.invalidExpectedType = invalidExpectedType;
		return copyCommonParams(copy);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(InsnUtils.formatOffset(offset));
		sb.append(": ARITH ");
		if (contains(AFlag.ARITH_ONEARG)) {
			sb.append(getArg(0)).append(' ').append(op.getSymbol()).append("= ").append(getArg(1));
		} else {
			RegisterArg result = getResult();
			if (result != null) {
				sb.append(result).append(" = ");
			}
			sb.append(getArg(0)).append(' ').append(op.getSymbol()).append(' ').append(getArg(1));
		}

		appendAttributes(sb);
		return sb.toString();
	}
}
