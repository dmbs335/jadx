package jadx.core.dex.instructions;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

public class FilledNewArrayNode extends InsnNode {

	private final ArgType elemType;
	private int invalidArgIndex = -1;
	private @Nullable ArgType invalidSourceType;
	private @Nullable ArgType invalidElementType;
	private @Nullable ArgType invalidArrayType;

	public FilledNewArrayNode(@NotNull ArgType elemType, int size) {
		super(InsnType.FILLED_NEW_ARRAY, size);
		this.elemType = elemType;
	}

	public ArgType getElemType() {
		return elemType;
	}

	public ArgType getArrayType() {
		return ArgType.array(elemType);
	}

	public int getInvalidArgIndex() {
		return invalidArgIndex;
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidArgType(int invalidArgIndex, ArgType invalidSourceType) {
		this.invalidArgIndex = invalidArgIndex;
		this.invalidSourceType = invalidSourceType;
	}

	public @Nullable ArgType getInvalidElementType() {
		return invalidElementType;
	}

	public void setInvalidElementType(ArgType invalidElementType) {
		this.invalidElementType = invalidElementType;
	}

	public @Nullable ArgType getInvalidArrayType() {
		return invalidArrayType;
	}

	public void setInvalidArrayType(ArgType invalidArrayType) {
		this.invalidArrayType = invalidArrayType;
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FilledNewArrayNode) || !super.isSame(obj)) {
			return false;
		}
		FilledNewArrayNode other = (FilledNewArrayNode) obj;
		return elemType == other.elemType
				&& invalidArgIndex == other.invalidArgIndex
				&& Objects.equals(invalidSourceType, other.invalidSourceType)
				&& Objects.equals(invalidElementType, other.invalidElementType)
				&& Objects.equals(invalidArrayType, other.invalidArrayType);
	}

	@Override
	public InsnNode copy() {
		FilledNewArrayNode copy = new FilledNewArrayNode(elemType, getArgsCount());
		copy.invalidArgIndex = invalidArgIndex;
		copy.invalidSourceType = invalidSourceType;
		copy.invalidElementType = invalidElementType;
		copy.invalidArrayType = invalidArrayType;
		return copyCommonParams(copy);
	}

	@Override
	public String toString() {
		return super.toString() + " elemType: " + elemType;
	}
}
