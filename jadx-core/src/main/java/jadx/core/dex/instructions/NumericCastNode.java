package jadx.core.dex.instructions;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX numeric conversion with a source type specified by its opcode. */
public final class NumericCastNode extends IndexInsnNode {
	private final ArgType sourceType;
	private @Nullable ArgType invalidSourceType;

	public NumericCastNode(ArgType sourceType, ArgType resultType) {
		super(InsnType.CAST, resultType, 1);
		this.sourceType = sourceType;
	}

	public ArgType getSourceType() {
		return sourceType;
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidSourceType(ArgType invalidSourceType) {
		this.invalidSourceType = invalidSourceType;
	}

	@Override
	public NumericCastNode copy() {
		NumericCastNode copy = new NumericCastNode(sourceType, getIndexAsType());
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NumericCastNode) || !super.isSame(obj)) {
			return false;
		}
		NumericCastNode other = (NumericCastNode) obj;
		return sourceType.equals(other.sourceType)
				&& Objects.equals(invalidSourceType, other.invalidSourceType);
	}
}
