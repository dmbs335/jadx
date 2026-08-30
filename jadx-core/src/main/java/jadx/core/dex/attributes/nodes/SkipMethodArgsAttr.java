package jadx.core.dex.attributes.nodes;

import java.util.BitSet;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class SkipMethodArgsAttr extends PinnedAttribute {

	public static void skipArg(MethodNode mth, RegisterArg arg) {
		int argNum = Utils.indexInListByRef(mth.getArgRegs(), arg);
		if (argNum == -1) {
			throw new JadxRuntimeException("Arg not found: " + arg);
		}
		SkipMethodArgsAttr attr = getOrCreate(mth);
		attr.skip(argNum);
		if (arg.contains(AFlag.REMOVE)) {
			attr.markRemovedArg(argNum, arg.getType());
		}
	}

	public static void skipArg(MethodNode mth, int argNum) {
		getOrCreate(mth).skip(argNum);
	}

	private static SkipMethodArgsAttr getOrCreate(MethodNode mth) {
		SkipMethodArgsAttr attr = mth.get(AType.SKIP_MTH_ARGS);
		if (attr == null) {
			attr = new SkipMethodArgsAttr(mth);
			mth.addAttr(attr);
		}
		return attr;
	}

	public static boolean isSkip(@Nullable MethodNode mth, int argNum) {
		if (mth == null) {
			return false;
		}
		if (argNum == 0 && mth.contains(AFlag.SKIP_FIRST_ARG)) {
			return true;
		}
		SkipMethodArgsAttr attr = mth.get(AType.SKIP_MTH_ARGS);
		if (attr == null) {
			return false;
		}
		return attr.isSkip(argNum);
	}

	private final BitSet skipArgs;
	@Nullable
	private ArgType[] removedArgTypes;

	private SkipMethodArgsAttr(MethodNode mth) {
		this.skipArgs = new BitSet(mth.getMethodInfo().getArgsCount());
	}

	public void skip(int argNum) {
		skipArgs.set(argNum);
	}

	private void markRemovedArg(int argNum, ArgType type) {
		if (removedArgTypes == null || argNum >= removedArgTypes.length) {
			ArgType[] newTypes = new ArgType[argNum + 1];
			if (removedArgTypes != null) {
				System.arraycopy(removedArgTypes, 0, newTypes, 0, removedArgTypes.length);
			}
			removedArgTypes = newTypes;
		}
		removedArgTypes[argNum] = type;
	}

	public boolean isRemovedArg(int argNum, ArgType type) {
		return removedArgTypes != null
				&& 0 <= argNum
				&& argNum < removedArgTypes.length
				&& Objects.equals(removedArgTypes[argNum], type);
	}

	public boolean isSkip(int argNum) {
		return skipArgs.get(argNum);
	}

	public int getSkipCount() {
		return skipArgs.cardinality();
	}

	@Override
	public AType<SkipMethodArgsAttr> getAttrType() {
		return AType.SKIP_MTH_ARGS;
	}

	@Override
	public String toString() {
		return "SKIP_MTH_ARGS: " + skipArgs;
	}
}
