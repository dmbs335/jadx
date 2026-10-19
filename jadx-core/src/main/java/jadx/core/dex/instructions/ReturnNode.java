package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX return whose source type must survive later inference and simplification. */
public final class ReturnNode extends InsnNode {
	private @Nullable ArgType invalidSourceType;

	public ReturnNode() {
		super(InsnType.RETURN, 1);
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidSourceType(ArgType invalidSourceType) {
		this.invalidSourceType = invalidSourceType;
	}

	@Override
	public ReturnNode copy() {
		ReturnNode copy = new ReturnNode();
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		return obj instanceof ReturnNode && super.isSame(obj);
	}
}
