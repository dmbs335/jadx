package jadx.core.dex.visitors.typeinference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxOverflowException;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class TypeUpdateInfo {
	private final MethodNode mth;
	private final TypeUpdateFlags flags;
	private @Nullable Map<InsnArg, TypeUpdateEntry> updateMap;
	private final List<TypeUpdateRequest> queue = new ArrayList<>();
	private final List<TypeUpdateRequest> callbackQueue = new ArrayList<>();
	private final int updatesLimitCount;
	private int updateSeq = 0;

	public TypeUpdateInfo(MethodNode mth, TypeUpdateFlags flags, JadxArgs args) {
		this.mth = mth;
		this.flags = flags;
		this.updatesLimitCount = mth.getInsnsCount() * args.getTypeUpdatesLimitCount();
	}

	public void queueRequest(TypeUpdateRequest request) {
		queue.add(request);
	}

	public void saveCallback(TypeUpdateRequest request) {
		if (request.getCallback() != null) {
			callbackQueue.add(request);
		}
	}

	public @Nullable TypeUpdateRequest pollNextRequest() {
		return ListUtils.removeLast(queue);
	}

	public @Nullable TypeUpdateRequest pollNextCallback() {
		return ListUtils.removeLast(callbackQueue);
	}

	public void requestUpdate(InsnArg arg, ArgType changeType) {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		if (map == null) {
			map = new IdentityHashMap<>();
			updateMap = map;
		}
		TypeUpdateEntry prev = map.put(arg, new TypeUpdateEntry(updateSeq++, arg, changeType));
		if (prev != null) {
			throw new JadxRuntimeException("Unexpected type update override for arg: " + arg
					+ " types: prev=" + prev.getType() + ", new=" + changeType
					+ ", insn: " + arg.getParentInsn());
		}
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
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		if (map == null) {
			return;
		}
		TypeUpdateEntry removed = map.remove(arg);
		if (removed != null) {
			int seq = removed.getSeq();
			map.values().removeIf(upd -> upd.getSeq() > seq);
		}
	}

	public void applyUpdates() {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		if (map == null) {
			return;
		}
		map.values().stream().sorted()
				.forEach(upd -> upd.getArg().setType(upd.getType()));
	}

	public boolean isProcessed(InsnArg arg) {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		return map != null && map.containsKey(arg);
	}

	public boolean hasUpdateWithType(InsnArg arg, ArgType type) {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		TypeUpdateEntry updateEntry = map == null ? null : map.get(arg);
		if (updateEntry != null) {
			return updateEntry.getType().equals(type);
		}
		return false;
	}

	public ArgType getType(InsnArg arg) {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		TypeUpdateEntry updateEntry = map == null ? null : map.get(arg);
		if (updateEntry != null) {
			return updateEntry.getType();
		}
		return arg.getType();
	}

	public MethodNode getMth() {
		return mth;
	}

	public boolean isEmpty() {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		return map == null || map.isEmpty();
	}

	public List<TypeUpdateEntry> getSortedUpdates() {
		Map<InsnArg, TypeUpdateEntry> map = updateMap;
		if (map == null) {
			return Collections.emptyList();
		}
		return map.values().stream().sorted().collect(Collectors.toList());
	}

	public TypeUpdateFlags getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "TypeUpdateInfo{" + flags + ' ' + getSortedUpdates() + '}';
	}
}
