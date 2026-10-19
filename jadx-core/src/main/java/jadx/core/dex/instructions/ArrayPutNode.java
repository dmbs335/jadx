package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX array write that retains a primitive source/element mismatch through simplification. */
public final class ArrayPutNode extends InsnNode {
	private @Nullable ArgType invalidSourceType;
	private @Nullable ArgType invalidElementType;

	public ArrayPutNode() {
		super(InsnType.APUT, 3);
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public @Nullable ArgType getInvalidElementType() {
		return invalidElementType;
	}

	public void setInvalidSourceType(ArgType sourceType, ArgType elementType) {
		this.invalidSourceType = sourceType;
		this.invalidElementType = elementType;
	}

	@Override
	public ArrayPutNode copy() {
		ArrayPutNode copy = new ArrayPutNode();
		copy.invalidSourceType = invalidSourceType;
		copy.invalidElementType = invalidElementType;
		return copyCommonParams(copy);
	}
}
