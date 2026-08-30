package jadx.core.dex.visitors.typeinference;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;

public class TypeUpdateRequest {
	private InsnArg arg;
	private ArgType candidateType;
	private boolean direct;
	private @Nullable ITypeUpdateCallback callback;
	private @Nullable TypeUpdateRequest nextFree;

	public TypeUpdateRequest(InsnArg arg, ArgType candidateType, boolean direct, @Nullable ITypeUpdateCallback callback) {
		init(arg, candidateType, direct, callback);
	}

	void init(InsnArg arg, ArgType candidateType, boolean direct, @Nullable ITypeUpdateCallback callback) {
		this.arg = arg;
		this.candidateType = candidateType;
		this.direct = direct;
		this.callback = callback;
		this.nextFree = null;
	}

	public InsnArg getArg() {
		return arg;
	}

	public ArgType getCandidateType() {
		return candidateType;
	}

	public boolean isDirect() {
		return direct;
	}

	public @Nullable ITypeUpdateCallback getCallback() {
		return callback;
	}

	@Nullable TypeUpdateRequest getNextFree() {
		return nextFree;
	}

	void recycle(@Nullable TypeUpdateRequest nextFree) {
		this.callback = null;
		this.nextFree = nextFree;
	}

	@Override
	public String toString() {
		return "TypeUpdateRequest{arg=" + arg + ", candidateType=" + candidateType + '}';
	}
}
