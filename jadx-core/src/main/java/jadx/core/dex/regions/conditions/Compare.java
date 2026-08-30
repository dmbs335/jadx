package jadx.core.dex.regions.conditions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.IfOp;
import jadx.core.dex.instructions.args.InsnArg;

public final class Compare {
	private final IfNode insn;
	private final @Nullable IfOp opOverride;
	private final boolean sharedView;

	public Compare(IfNode insn) {
		this(insn, null, false);
	}

	private Compare(IfNode insn, @Nullable IfOp opOverride, boolean sharedView) {
		insn.add(AFlag.HIDDEN);
		this.insn = insn;
		this.opOverride = opOverride;
		this.sharedView = sharedView;
	}

	public IfOp getOp() {
		return opOverride != null ? opOverride : insn.getOp();
	}

	public InsnArg getA() {
		return insn.getArg(0);
	}

	public InsnArg getB() {
		return insn.getArg(1);
	}

	public IfNode getInsn() {
		return insn;
	}

	public Compare invert() {
		if (sharedView) {
			return new Compare(insn, getOp().invert(), true);
		}
		insn.invertCondition();
		return this;
	}

	public Compare copyForSharedView() {
		return new Compare(insn, getOp(), true);
	}

	public boolean isSharedView() {
		return sharedView;
	}

	public void normalize() {
		if (!sharedView && opOverride == null) {
			insn.normalize();
		}
	}

	@Override
	public String toString() {
		return getA() + " " + getOp().getSymbol() + ' ' + getB();
	}
}
