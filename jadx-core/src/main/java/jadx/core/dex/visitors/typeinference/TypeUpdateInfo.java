package jadx.core.dex.visitors.typeinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxOverflowException;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class TypeUpdateInfo {
	private static final int LIST_UPDATES_LIMIT = 8;

	private MethodNode mth;
	private TypeUpdateFlags flags;
	private InsnArg @Nullable [] updateArgs;
	private ArgType @Nullable [] updateTypes;
	private int updateCount;
	private @Nullable IdentityIntMap<InsnArg> updateMap;
	private @Nullable IdentityIntMap<InsnArg> cachedUpdateMap;
	private @Nullable List<TypeUpdateRequest> queue;
	private @Nullable List<TypeUpdateRequest> callbackQueue;
	private @Nullable TypeUpdateRequest requestPool;
	private @Nullable ArgsListUpdateCallback<?> argsListCallbackPool;
	private @Nullable MoveUpdateCallback moveCallbackPool;
	private int updatesLimitCount;
	private int updateSeq = 0;
	private @Nullable TypeUpdateInfo nextFree;

	public TypeUpdateInfo(MethodNode mth, TypeUpdateFlags flags, JadxArgs args) {
		init(mth, flags, args);
	}

	void init(MethodNode mth, TypeUpdateFlags flags, JadxArgs args) {
		this.mth = mth;
		this.flags = flags;
		this.updatesLimitCount = mth.getInsnsCount() * args.getTypeUpdatesLimitCount();
		this.nextFree = null;
	}

	void recycle(@Nullable TypeUpdateInfo nextFree) {
		recycleRequests(queue);
		recycleRequests(callbackQueue);
		if (updateArgs != null) {
			Arrays.fill(updateArgs, 0, updateCount, null);
			Arrays.fill(updateTypes, 0, updateCount, null);
		}
		updateCount = 0;
		updateSeq = 0;
		IdentityIntMap<InsnArg> map = updateMap;
		if (map != null) {
			map.clear();
			cachedUpdateMap = map;
			updateMap = null;
		}
		mth = null;
		flags = null;
		this.nextFree = nextFree;
	}

	private void recycleRequests(@Nullable List<TypeUpdateRequest> requests) {
		if (requests == null) {
			return;
		}
		for (int i = 0, count = requests.size(); i < count; i++) {
			releaseRequest(requests.get(i));
		}
		requests.clear();
	}

	@Nullable
	TypeUpdateInfo getNextFree() {
		return nextFree;
	}

	public void queueRequest(InsnArg arg, ArgType candidateType, boolean direct, @Nullable ITypeUpdateCallback callback) {
		List<TypeUpdateRequest> requests = queue;
		if (requests == null) {
			requests = new ArrayList<>();
			queue = requests;
		}
		requests.add(acquireRequest(arg, candidateType, direct, callback));
	}

	public void saveCallback(InsnArg arg, ArgType candidateType, boolean direct, ITypeUpdateCallback callback) {
		addCallback(acquireRequest(arg, candidateType, direct, callback));
	}

	public void saveCallback(TypeUpdateRequest request) {
		if (request.getCallback() != null) {
			addCallback(request);
		} else {
			releaseRequest(request);
		}
	}

	private void addCallback(TypeUpdateRequest request) {
		List<TypeUpdateRequest> callbacks = callbackQueue;
		if (callbacks == null) {
			callbacks = new ArrayList<>();
			callbackQueue = callbacks;
		}
		callbacks.add(request);
	}

	public void releaseRequest(TypeUpdateRequest request) {
		request.recycle(requestPool);
		requestPool = request;
	}

	private TypeUpdateRequest acquireRequest(
			InsnArg arg, ArgType candidateType, boolean direct, @Nullable ITypeUpdateCallback callback) {
		TypeUpdateRequest request = requestPool;
		if (request == null) {
			return new TypeUpdateRequest(arg, candidateType, direct, callback);
		}
		requestPool = request.getNextFree();
		request.init(arg, candidateType, direct, callback);
		return request;
	}

	@SuppressWarnings("unchecked")
	<T extends InsnArg> ArgsListUpdateCallback<T> acquireArgsListUpdateCallback(
			TypeUpdate typeUpdate, List<T> args, ArgType candidateType, boolean direct) {
		ArgsListUpdateCallback<?> pooled = argsListCallbackPool;
		if (pooled == null) {
			return new ArgsListUpdateCallback<>(typeUpdate, this, args, candidateType, direct);
		}
		argsListCallbackPool = pooled.getNextFree();
		ArgsListUpdateCallback<T> callback = (ArgsListUpdateCallback<T>) pooled;
		callback.init(typeUpdate, this, args, candidateType, direct);
		return callback;
	}

	void releaseArgsListUpdateCallback(ArgsListUpdateCallback<?> callback) {
		callback.recycle(argsListCallbackPool);
		argsListCallbackPool = callback;
	}

	MoveUpdateCallback acquireMoveUpdateCallback(TypeUpdate typeUpdate, InsnNode insn,
			InsnArg changeArg, ArgType candidateType, boolean correctType) {
		MoveUpdateCallback callback = moveCallbackPool;
		if (callback == null) {
			return new MoveUpdateCallback(typeUpdate, this, insn, changeArg, candidateType, correctType);
		}
		moveCallbackPool = callback.getNextFree();
		callback.init(typeUpdate, this, insn, changeArg, candidateType, correctType);
		return callback;
	}

	void releaseMoveUpdateCallback(MoveUpdateCallback callback) {
		callback.recycle(moveCallbackPool);
		moveCallbackPool = callback;
	}

	public @Nullable TypeUpdateRequest pollNextRequest() {
		List<TypeUpdateRequest> requests = queue;
		return requests == null ? null : ListUtils.removeLast(requests);
	}

	public @Nullable TypeUpdateRequest pollNextCallback() {
		List<TypeUpdateRequest> callbacks = callbackQueue;
		return callbacks == null ? null : ListUtils.removeLast(callbacks);
	}

	public void requestUpdate(InsnArg arg, ArgType changeType) {
		int updateIndex = getUpdateIndex(arg);
		if (updateIndex != -1) {
			ArgType[] types = updateTypes;
			throwUpdateOverride(arg, changeType, types[updateIndex]);
		}
		IdentityIntMap<InsnArg> map = updateMap;
		if (map == null && updateCount == LIST_UPDATES_LIMIT) {
			map = promoteToMap();
		}
		ensureUpdateCapacity();
		InsnArg[] args = updateArgs;
		ArgType[] types = updateTypes;
		args[updateCount] = arg;
		types[updateCount] = changeType;
		if (map != null) {
			map.put(arg, updateCount);
		}
		updateSeq++;
		updateCount++;
		checkUpdatesLimit();
	}

	private void throwUpdateOverride(InsnArg arg, ArgType changeType, ArgType prevType) {
		throw new JadxRuntimeException("Unexpected type update override for arg: " + arg
				+ " types: prev=" + prevType + ", new=" + changeType
				+ ", insn: " + arg.getParentInsn());
	}

	private IdentityIntMap<InsnArg> promoteToMap() {
		IdentityIntMap<InsnArg> map = cachedUpdateMap;
		if (map == null) {
			map = new IdentityIntMap<>();
		} else {
			cachedUpdateMap = null;
		}
		InsnArg[] args = updateArgs;
		for (int i = 0; i < updateCount; i++) {
			map.put(args[i], i);
		}
		updateMap = map;
		return map;
	}

	private void ensureUpdateCapacity() {
		InsnArg[] args = updateArgs;
		if (args == null) {
			updateArgs = new InsnArg[LIST_UPDATES_LIMIT];
			updateTypes = new ArgType[LIST_UPDATES_LIMIT];
			return;
		}
		if (updateCount == args.length) {
			int newLength = args.length * 2;
			updateArgs = Arrays.copyOf(args, newLength);
			updateTypes = Arrays.copyOf(updateTypes, newLength);
		}
	}

	private void checkUpdatesLimit() {
		if (updateSeq > updatesLimitCount) {
			throw new JadxOverflowException("Type inference error: updates count limit reached"
					+ " with updateSeq = " + updateSeq + ". Try increasing type updates limit count.");
		}
		if (updateSeq % 100 == 0) {
			// check for interruption sometimes (every update is too often)
			Utils.checkThreadInterrupt();
		}
	}

	public void rollbackUpdate(InsnArg arg) {
		int updateIndex = getUpdateIndex(arg);
		if (updateIndex == -1) {
			return;
		}
		InsnArg[] args = updateArgs;
		ArgType[] types = updateTypes;
		IdentityIntMap<InsnArg> map = updateMap;
		for (int i = updateCount - 1; i >= updateIndex; i--) {
			if (map != null) {
				map.remove(args[i]);
			}
			args[i] = null;
			types[i] = null;
		}
		updateCount = updateIndex;
	}

	public void applyUpdates() {
		InsnArg[] args = updateArgs;
		ArgType[] types = updateTypes;
		for (int i = 0; i < updateCount; i++) {
			args[i].setType(types[i]);
		}
	}

	public boolean isProcessed(InsnArg arg) {
		IdentityIntMap<InsnArg> map = updateMap;
		return map != null ? map.get(arg) != -1 : getUpdateIndex(arg) != -1;
	}

	public boolean hasUpdateWithType(InsnArg arg, ArgType type) {
		ArgType updateType = getUpdateType(arg);
		return updateType != null && updateType.equals(type);
	}

	public ArgType getType(InsnArg arg) {
		ArgType updateType = getUpdateType(arg);
		return updateType == null ? arg.getType() : updateType;
	}

	public MethodNode getMth() {
		return mth;
	}

	public boolean isEmpty() {
		return updateCount == 0;
	}

	public List<TypeUpdateEntry> getSortedUpdates() {
		if (updateCount == 0) {
			return Collections.emptyList();
		}
		List<TypeUpdateEntry> result = new ArrayList<>(updateCount);
		InsnArg[] args = updateArgs;
		ArgType[] types = updateTypes;
		for (int i = 0; i < updateCount; i++) {
			result.add(new TypeUpdateEntry(i, args[i], types[i]));
		}
		return result;
	}

	private @Nullable ArgType getUpdateType(InsnArg arg) {
		IdentityIntMap<InsnArg> map = updateMap;
		if (map != null) {
			int updateIndex = map.get(arg);
			return updateIndex == -1 ? null : updateTypes[updateIndex];
		}
		int updateIndex = getUpdateIndex(arg);
		return updateIndex == -1 ? null : updateTypes[updateIndex];
	}

	private int getUpdateIndex(InsnArg arg) {
		IdentityIntMap<InsnArg> map = updateMap;
		if (map != null) {
			return map.get(arg);
		}
		InsnArg[] args = updateArgs;
		if (args != null) {
			for (int i = updateCount - 1; i >= 0; i--) {
				if (args[i] == arg) {
					return i;
				}
			}
		}
		return -1;
	}

	public TypeUpdateFlags getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "TypeUpdateInfo{" + flags + ' ' + getSortedUpdates() + '}';
	}
}
