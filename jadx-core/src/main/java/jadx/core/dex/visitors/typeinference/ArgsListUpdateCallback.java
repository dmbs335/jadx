package jadx.core.dex.visitors.typeinference;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;

import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.CHANGED;
import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.REJECT;
import static jadx.core.dex.visitors.typeinference.TypeUpdateResult.SAME;

/**
 * Type update callback to set same type for args from list.
 */
public class ArgsListUpdateCallback<T extends InsnArg> implements ITypeUpdateCallback {
	private final TypeUpdate typeUpdate;
	private final TypeUpdateInfo updateInfo;
	private final List<T> args;
	private int argsIndex;
	private final ArgType candidateType;
	private final boolean direct;

	private @Nullable Predicate<T> argsFilter;
	private @Nullable ITypeUpdateCallback finalResultCallback;
	private boolean ignoreReject = false;

	private boolean allSame = true;
	private boolean firstQueue = false;

	public ArgsListUpdateCallback(TypeUpdate typeUpdate, TypeUpdateInfo updateInfo,
			List<T> args, ArgType candidateType, boolean direct) {
		this.typeUpdate = typeUpdate;
		this.updateInfo = updateInfo;
		this.args = args;
		this.candidateType = candidateType;
		this.direct = direct;
	}

	@Override
	public @Nullable TypeUpdateResult updateCallback(TypeUpdateResult result) {
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

	public void setArgsFilter(@Nullable Predicate<T> argsFilter) {
		this.argsFilter = argsFilter;
	}

	public void setIgnoreReject(boolean ignoreReject) {
		this.ignoreReject = ignoreReject;
	}

	private @Nullable TypeUpdateResult finalResult(TypeUpdateResult result) {
		if (finalResultCallback != null) {
			return finalResultCallback.updateCallback(result);
		}
		return result;
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
