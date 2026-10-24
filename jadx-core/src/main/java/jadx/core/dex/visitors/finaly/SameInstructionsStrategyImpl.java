package jadx.core.dex.visitors.finaly;

import java.util.HashSet;
import java.util.Set;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.RegDebugInfoAttr;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.utils.Pair;

public final class SameInstructionsStrategyImpl extends SameInstructionsStrategy {

	private static boolean sameDebugInfo(RegisterArg dupReg, RegisterArg fReg) {
		RegDebugInfoAttr fDbgInfo = fReg.get(AType.REG_DEBUG_INFO);
		RegDebugInfoAttr dupDbgInfo = dupReg.get(AType.REG_DEBUG_INFO);
		if (fDbgInfo == null || dupDbgInfo == null) {
			return false;
		}
		return dupDbgInfo.equals(fDbgInfo);
	}

	private boolean assignInsnDifferent(RegisterArg dupReg, RegisterArg fReg, Set<Pair<SSAVar>> comparing) {
		InsnNode assignInsn = fReg.getAssignInsn();
		InsnNode dupAssign = dupReg.getAssignInsn();
		if (assignInsn == null || dupAssign == null) {
			return true;
		}
		return !sameInsns(dupAssign, assignInsn, comparing);
	}

	@Override
	public boolean sameInsns(InsnNode dupInsn, InsnNode fInsn) {
		return sameInsns(dupInsn, fInsn, new HashSet<>());
	}

	private boolean sameInsns(InsnNode dupInsn, InsnNode fInsn, Set<Pair<SSAVar>> comparing) {
		if (!dupInsn.isSame(fInsn)) {
			return false;
		}
		for (int i = 0; i < dupInsn.getArgsCount(); i++) {
			InsnArg dupArg = dupInsn.getArg(i);
			InsnArg fArg = fInsn.getArg(i);
			if (!isSameArgs(dupArg, fArg, comparing)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSameArgs(InsnArg dupArg, InsnArg fArg) {
		return isSameArgs(dupArg, fArg, new HashSet<>());
	}

	private boolean isSameArgs(InsnArg dupArg, InsnArg fArg, Set<Pair<SSAVar>> comparing) {
		if (dupArg == null || fArg == null) {
			return false;
		}
		if (dupArg.isInsnWrap() || fArg.isInsnWrap()) {
			if (!dupArg.isInsnWrap() || !fArg.isInsnWrap()) {
				return false;
			}
			return sameInsns(((InsnWrapArg) dupArg).getWrapInsn(), ((InsnWrapArg) fArg).getWrapInsn(), comparing);
		}
		boolean isReg = dupArg.isRegister();
		if (isReg != fArg.isRegister()) {
			return false;
		}
		if (isReg) {
			RegisterArg dupReg = (RegisterArg) dupArg;
			RegisterArg fReg = (RegisterArg) fArg;
			CodeVar dupCodeVar = dupReg.getSVar().getCodeVarOrNull();
			if (!(dupCodeVar != null && dupCodeVar == fReg.getSVar().getCodeVarOrNull())
					&& !sameDebugInfo(dupReg, fReg)) {
				Pair<SSAVar> pair = new Pair<>(dupReg.getSVar(), fReg.getSVar());
				if (!comparing.add(pair)) {
					return false;
				}
				try {
					if (assignInsnDifferent(dupReg, fReg, comparing)) {
						return false;
					}
				} finally {
					comparing.remove(pair);
				}
			}
		}
		boolean remConst = dupArg.isConst();
		if (remConst != fArg.isConst()) {
			return false;
		}
		return !(remConst && !dupArg.isSameConst(fArg));
	}
}
