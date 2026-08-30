package jadx.core.dex.visitors.typeinference;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.SSAVar;

import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.CHANGED;
import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.REJECT;
import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.SAME;

/**
 * Type update callback to set same type for args from list.
 */
public class ArgsListUpdateCallback<T extends InsnArg> implements ITypeUpdateCallback {
	private TypeUpdate typeUpdate;
	private TypeUpdateInfo updateInfo;
	private List<T> args;
	private int argsIndex;
	private ArgType candidateType;
	private boolean direct;

	private @Nullable Predicate<T> argsFilter;
	private @Nullable ITypeUpdateCallback finalResultCallback;
	private @Nullable SSAVar rollbackSsaVarOnReject;
	private boolean ignoreReject = false;

	private boolean allSame = true;
	private boolean firstQueue = false;
	private int activeCalls;
	private boolean releasePending;
	private @Nullable ArgsListUpdateCallback<?> nextFree;

	public ArgsListUpdateCallback(TypeUpdate typeUpdate, TypeUpdateInfo updateInfo,
			List<T> args, ArgType candidateType, boolean direct) {
		init(typeUpdate, updateInfo, args, candidateType, direct);
	}

	void init(TypeUpdate typeUpdate, TypeUpdateInfo updateInfo,
			List<T> args, ArgType candidateType, boolean direct) {
		this.typeUpdate = typeUpdate;
		this.updateInfo = updateInfo;
		this.args = args;
		this.candidateType = candidateType;
		this.direct = direct;
		this.argsIndex = 0;
		this.argsFilter = null;
		this.finalResultCallback = null;
		this.rollbackSsaVarOnReject = null;
		this.ignoreReject = false;
		this.allSame = true;
		this.firstQueue = false;
		this.activeCalls = 0;
		this.releasePending = false;
		this.nextFree = null;
	}

	@Override
	public @Nullable TypeUpdateResult updateCallback(TypeUpdateResult result) {
		activeCalls++;
		try {
			return processUpdate(result);
		} finally {
			activeCalls--;
			if (activeCalls == 0 && releasePending) {
				TypeUpdateInfo info = updateInfo;
				releasePending = false;
				info.releaseArgsListUpdateCallback(this);
			}
		}
	}

	private @Nullable TypeUpdateResult processUpdate(TypeUpdateResult result) {
		while (true) {
			if (!ignoreReject) {
				if (result == REJECT) {
					return finalResult(result);
				}
			}
			if (result != SAME) {
				allSame = false;
			}
			T next = getNextArg();
			if (next == null) {
				return finalResult(allSame ? SAME : CHANGED);
			}
			result = queueUpdate(next);
			if (result == null) {
				// keep this callback
				return null;
			}
		}
	}

	private @Nullable TypeUpdateResult queueUpdate(T next) {
		ITypeUpdateCallback cb;
		if (firstQueue) {
			cb = this;
			firstQueue = false;
		} else {
			cb = null;
		}
		if (direct) {
			return typeUpdate.queueDirectTypeUpdate(updateInfo, next, candidateType, cb);
		}
		return typeUpdate.queueTypeUpdate(updateInfo, next, candidateType, cb);
	}

	public @Nullable TypeUpdateResult runFirstQueue() {
		firstQueue = true;
		return updateCallback(SAME);
	}

	public void setFinalResultCallback(@Nullable ITypeUpdateCallback finalResultCallback) {
		this.finalResultCallback = finalResultCallback;
	}

	public void setRollbackSsaVarOnReject(SSAVar rollbackSsaVarOnReject) {
		this.rollbackSsaVarOnReject = rollbackSsaVarOnReject;
	}

	public void setArgsFilter(@Nullable Predicate<T> argsFilter) {
		this.argsFilter = argsFilter;
	}

	public void setIgnoreReject(boolean ignoreReject) {
		this.ignoreReject = ignoreReject;
	}

	private @Nullable TypeUpdateResult finalResult(TypeUpdateResult result) {
		SSAVar rollbackSsaVar = rollbackSsaVarOnReject;
		if (result == REJECT && rollbackSsaVar != null) {
			updateInfo.rollbackUpdate(rollbackSsaVar.getAssign());
			List<? extends InsnArg> useList = rollbackSsaVar.getUseList();
			int useCount = useList.size();
			for (int i = 0; i < useCount; i++) {
				updateInfo.rollbackUpdate(useList.get(i));
			}
		}
		TypeUpdateResult finalResult = finalResultCallback == null
				? result
				: finalResultCallback.updateCallback(result);
		if (finalResult != null) {
			// queueTypeUpdate can call this callback recursively when verification returns immediately.
			// Keep the state alive until the outermost invocation has finished using it.
			releasePending = true;
		}
		return finalResult;
	}

	void recycle(@Nullable ArgsListUpdateCallback<?> nextFree) {
		typeUpdate = null;
		updateInfo = null;
		args = null;
		candidateType = null;
		argsFilter = null;
		finalResultCallback = null;
		rollbackSsaVarOnReject = null;
		this.nextFree = nextFree;
	}

	@Nullable
	ArgsListUpdateCallback<?> getNextFree() {
		return nextFree;
	}

	private @Nullable T getNextArg() {
		Predicate<T> filter = argsFilter;
		int argsCount = args.size();
		while (argsIndex < argsCount) {
			T next = args.get(argsIndex++);
			if (filter == null || filter.test(next)) {
				return next;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "ArgsListUpdateCallback";
	}
}
