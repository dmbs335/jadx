package jadx.core.dex.instructions;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.InsnNode;

/** A DEX field write that retains a primitive source/field mismatch through simplification. */
public final class FieldPutNode extends IndexInsnNode {
	private @Nullable ArgType invalidSourceType;

	public FieldPutNode(InsnType insnType, FieldInfo fieldInfo, int argsCount) {
		super(insnType, fieldInfo, argsCount);
	}

	public @Nullable ArgType getInvalidSourceType() {
		return invalidSourceType;
	}

	public void setInvalidSourceType(ArgType invalidSourceType) {
		this.invalidSourceType = invalidSourceType;
	}

	@Override
	public FieldPutNode copy() {
		FieldPutNode copy = new FieldPutNode(insnType, (FieldInfo) getIndex(), getArgsCount());
		copy.invalidSourceType = invalidSourceType;
		return copyCommonParams(copy);
	}

	@Override
	public boolean isSame(InsnNode obj) {
		return obj instanceof FieldPutNode && super.isSame(obj);
	}
}
