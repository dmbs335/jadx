package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.InsnNode;

public abstract class BaseInvokeNode extends InsnNode {
	private int invalidArgIndex = -1;
	private @Nullable ArgType invalidSourceType;
	private @Nullable ArgType invalidExpectedType;

	public BaseInvokeNode(InsnType type, int argsCount) {
		super(type, argsCount);
	}

	public int getInvalidArgIndex() {
		return invalidArgIndex;
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public @Nullable ArgType getInvalidExpectedType() {
		return invalidExpectedType;
	}

	public void setInvalidArgType(int argIndex, ArgType sourceType, ArgType expectedType) {
		this.invalidArgIndex = argIndex;
		this.invalidSourceType = sourceType;
		this.invalidExpectedType = expectedType;
	}

	public void copyInvalidArgTypeTo(BaseInvokeNode target) {
		target.invalidArgIndex = invalidArgIndex;
		target.invalidSourceType = invalidSourceType;
		target.invalidExpectedType = invalidExpectedType;
	}

	public abstract MethodInfo getCallMth();

	@Nullable
	public abstract InsnArg getInstanceArg();

	public abstract boolean isStaticCall();

	/**
	 * Return offset to match method args from {@link #getCallMth()}
	 */
	public abstract int getFirstArgOffset();
}
