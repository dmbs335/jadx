package jadx.core.dex.visitors.shrink;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.mods.TernaryInsn;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.utils.EmptyBitSet;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;

final class ArgsInfo {
	private final InsnNode insn;
	private final ArgsInfo[] argsList;
	private final @Nullable Object args;
	private final int pos;
	private int inlineBorder;
	private ArgsInfo inlinedInsn;
	private @Nullable List<ArgsInfo> wrappedInsns;

	public ArgsInfo(InsnNode insn, ArgsInfo[] argsList, int pos) {
		this.insn = insn;
		this.argsList = argsList;
		this.pos = pos;
		this.inlineBorder = pos;
		this.args = addArgs(insn, null);
	}

	public static void fillArgsSet(InsnNode insn, BitSet set) {
		if (insn.getType() == InsnType.TERNARY) {
			List<RegisterArg> conditionArgs = ((TernaryInsn) insn).getCondition().getRegisterArgs();
			int conditionArgsCount = conditionArgs.size();
			for (int i = 0; i < conditionArgsCount; i++) {
				set.set(conditionArgs.get(i).getRegNum());
			}
		}
		int argsCount = insn.getArgsCount();
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = insn.getArg(i);
			if (arg.isRegister()) {
				set.set(((RegisterArg) arg).getRegNum());
			} else if (arg.isInsnWrap()) {
				fillArgsSet(((InsnWrapArg) arg).getWrapInsn(), set);
			}
		}
	}

	private static @Nullable Object addArgs(InsnNode insn, @Nullable Object args) {
		int argsCount = insn.getArgsCount();
		if (insn.getType() == InsnType.TERNARY) {
			List<RegisterArg> conditionArgs = ((TernaryInsn) insn).getCondition().getRegisterArgs();
			int conditionArgsCount = conditionArgs.size();
			for (int i = 0; i < conditionArgsCount; i++) {
				args = addArg(args, conditionArgs.get(i));
			}
		}
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = insn.getArg(i);
			if (arg.isRegister()) {
				args = addArg(args, (RegisterArg) arg);
			}
		}
		for (int i = 0; i < argsCount; i++) {
			InsnArg arg = insn.getArg(i);
			if (arg.isInsnWrap()) {
				args = addArgs(((InsnWrapArg) arg).getWrapInsn(), args);
			}
		}
		return args;
	}

	@SuppressWarnings("unchecked")
	private static Object addArg(@Nullable Object args, RegisterArg arg) {
		if (args == null) {
			return arg;
		}
		if (args instanceof RegisterArg) {
			return new ArgsPair((RegisterArg) args, arg);
		}
		if (args instanceof ArgsPair) {
			ArgsPair pair = (ArgsPair) args;
			List<RegisterArg> list = new ArrayList<>(4);
			list.add(pair.first);
			list.add(pair.second);
			list.add(arg);
			return list;
		}
		((List<RegisterArg>) args).add(arg);
		return args;
	}

	public InsnNode getInsn() {
		return insn;
	}

	int getArgsCount() {
		if (args == null) {
			return 0;
		}
		if (args instanceof RegisterArg) {
			return 1;
		}
		return args instanceof ArgsPair ? 2 : ((List<?>) args).size();
	}

	@SuppressWarnings("unchecked")
	RegisterArg getArg(int index) {
		if (args instanceof RegisterArg) {
			if (index != 0) {
				throw new IndexOutOfBoundsException(index);
			}
			return (RegisterArg) args;
		}
		if (args instanceof ArgsPair) {
			ArgsPair pair = (ArgsPair) args;
			if (index == 0) {
				return pair.first;
			}
			if (index == 1) {
				return pair.second;
			}
			throw new IndexOutOfBoundsException(index);
		}
		return ((List<RegisterArg>) args).get(index);
	}

	public BitSet getArgsSet() {
		if (args == null && Utils.isEmpty(wrappedInsns)) {
			return EmptyBitSet.EMPTY;
		}
		BitSet set = new BitSet();
		fillArgsSet(set);
		return set;
	}

	private void fillArgsSet(BitSet set) {
		if (args instanceof RegisterArg) {
			set.set(((RegisterArg) args).getRegNum());
		} else if (args instanceof ArgsPair) {
			ArgsPair pair = (ArgsPair) args;
			set.set(pair.first.getRegNum());
			set.set(pair.second.getRegNum());
		} else if (args != null) {
			@SuppressWarnings("unchecked")
			List<RegisterArg> argsList = (List<RegisterArg>) args;
			int argsCount = argsList.size();
			for (int i = 0; i < argsCount; i++) {
				set.set(argsList.get(i).getRegNum());
			}
		}
		List<ArgsInfo> wrapList = wrappedInsns;
		if (wrapList != null) {
			int wrappedCount = wrapList.size();
			for (int i = 0; i < wrappedCount; i++) {
				wrapList.get(i).fillArgsSet(set);
			}
		}
	}

	public WrapInfo checkInline(int assignPos, RegisterArg arg) {
		if (assignPos >= inlineBorder || !canMove(assignPos, inlineBorder)) {
			return null;
		}
		inlineBorder = assignPos;
		return inline(assignPos, arg);
	}

	private boolean canMove(int from, int to) {
		ArgsInfo startInfo = argsList[from];
		int start = from + 1;
		if (start == to) {
			// previous instruction or on edge of inline border
			return true;
		}
		if (start > to) {
			throw new JadxRuntimeException("Invalid inline insn positions: " + start + " - " + to);
		}
		BitSet movedSet = startInfo.getArgsSet();
		if (movedSet == EmptyBitSet.EMPTY && startInfo.insn.isConstInsn()) {
			return true;
		}
		boolean canReorder = startInfo.canReorder();
		for (int i = start; i < to; i++) {
			ArgsInfo argsInfo = argsList[i];
			if (argsInfo.getInlinedInsn() == this) {
				continue;
			}
			InsnNode curInsn = argsInfo.insn;
			if (canReorder) {
				if (usedArgAssign(curInsn, movedSet)) {
					return false;
				}
			} else {
				if (!curInsn.canReorder() || usedArgAssign(curInsn, movedSet)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean canReorder() {
		if (!insn.canReorder()) {
			return false;
		}
		List<ArgsInfo> wrapList = wrappedInsns;
		if (wrapList != null) {
			int wrappedCount = wrapList.size();
			for (int i = 0; i < wrappedCount; i++) {
				if (!wrapList.get(i).canReorder()) {
					return false;
				}
			}
		}
		return true;
	}

	static boolean usedArgAssign(InsnNode insn, BitSet args) {
		if (args.isEmpty()) {
			return false;
		}
		RegisterArg result = insn.getResult();
		if (result == null) {
			return false;
		}
		return args.get(result.getRegNum());
	}

	WrapInfo inline(int assignInsnPos, RegisterArg arg) {
		ArgsInfo argsInfo = argsList[assignInsnPos];
		argsInfo.inlinedInsn = this;
		if (wrappedInsns == null) {
			wrappedInsns = new ArrayList<>(Math.max(1, getArgsCount()));
		}
		wrappedInsns.add(argsInfo);
		return new WrapInfo(argsInfo.insn, arg);
	}

	ArgsInfo getInlinedInsn() {
		if (inlinedInsn != null) {
			ArgsInfo parent = inlinedInsn.getInlinedInsn();
			if (parent != null) {
				inlinedInsn = parent;
			}
		}
		return inlinedInsn;
	}

	private static final class ArgsPair {
		private final RegisterArg first;
		private final RegisterArg second;

		private ArgsPair(RegisterArg first, RegisterArg second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public String toString() {
			return '[' + first.toString() + ", " + second + ']';
		}
	}

	@Override
	public String toString() {
		return "ArgsInfo: |" + inlineBorder
				+ " ->" + (inlinedInsn == null ? "-" : inlinedInsn.pos)
				+ ' ' + args + " : " + insn;
	}
}
