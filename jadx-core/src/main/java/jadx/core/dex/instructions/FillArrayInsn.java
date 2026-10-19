package jadx.core.dex.instructions;

import java.util.List;
import java.util.Objects;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.nodes.InsnNode;

public final class FillArrayInsn extends InsnNode {
	private final int target;
	private FillArrayData arrayData;
	private int invalidArraySize = -1;

	public FillArrayInsn(InsnArg arg, int target) {
		super(InsnType.FILL_ARRAY, 1);
		this.target = target;
		addArg(arg);
	}

	public int getTarget() {
		return target;
	}

	public void setArrayData(FillArrayData arrayData) {
		this.arrayData = arrayData;
	}

	public int getInvalidArraySize() {
		return invalidArraySize;
	}

	public void setInvalidArraySize(int invalidArraySize) {
		this.invalidArraySize = invalidArraySize;
	}

	@Override
	public boolean isSame(InsnNode obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FillArrayInsn) || !super.isSame(obj)) {
			return false;
		}
		FillArrayInsn other = (FillArrayInsn) obj;
		return Objects.equals(arrayData, other.arrayData);
	}

	@Override
	public InsnNode copy() {
		FillArrayInsn copy = new FillArrayInsn(getArg(0), target);
		copy.arrayData = arrayData;
		copy.invalidArraySize = invalidArraySize;
		return copyCommonParams(copy);
	}

	@Override
	public String toString() {
		return super.toString() + ", data: " + arrayData;
	}

	public int getSize() {
		return arrayData.getSize();
	}

	public ArgType getElementType() {
		return arrayData.getElementType();
	}

	public int getElementSize() {
		return arrayData.getElementSize();
	}

	public boolean hasCompatibleArrayType(ArgType arrayType) {
		if (!arrayType.isArray()) {
			return true;
		}
		ArgType elementType = arrayType.getArrayElement();
		if (!elementType.isTypeKnown()) {
			return true;
		}
		return elementType.isPrimitive() && getElementType().contains(elementType.getPrimitiveType());
	}

	public List<LiteralArg> getLiteralArgs(ArgType elType) {
		return arrayData.getLiteralArgs(elType);
	}

	public String dataToString() {
		return Objects.toString(arrayData);
	}
}
