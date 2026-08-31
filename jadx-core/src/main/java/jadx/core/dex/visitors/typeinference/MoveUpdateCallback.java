package jadx.core.dex.visitors.typeinference;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.InsnNode;

final class MoveUpdateCallback implements ITypeUpdateCallback {
	private TypeUpdate typeUpdate;
	private TypeUpdateInfo updateInfo;
	private InsnNode insn;
	private InsnArg changeArg;
	private ArgType candidateType;
	private boolean correctType;
	private @Nullable MoveUpdateCallback nextFree;

	MoveUpdateCallback(TypeUpdate typeUpdate, TypeUpdateInfo updateInfo, InsnNode insn,
			InsnArg changeArg, ArgType candidateType, boolean correctType) {
		init(typeUpdate, updateInfo, insn, changeArg, candidateType, correctType);
	}

	void init(TypeUpdate typeUpdate, TypeUpdateInfo updateInfo, InsnNode insn,
			InsnArg changeArg, ArgType candidateType, boolean correctType) {
		this.typeUpdate = typeUpdate;
		this.updateInfo = updateInfo;
		this.insn = insn;
		this.changeArg = changeArg;
		this.candidateType = candidateType;
		this.correctType = correctType;
		this.nextFree = null;
	}

	@Override
	public TypeUpdateResult updateCallback(TypeUpdateResult result) {
		try {
			return typeUpdate.processMoveResult(result, insn, changeArg, candidateType, correctType);
		} finally {
			updateInfo.releaseMoveUpdateCallback(this);
		}
	}

	void recycle(@Nullable MoveUpdateCallback nextFree) {
		typeUpdate = null;
		updateInfo = null;
		insn = null;
		changeArg = null;
		candidateType = null;
		this.nextFree = nextFree;
	}

	@Nullable
	MoveUpdateCallback getNextFree() {
		return nextFree;
	}
}
