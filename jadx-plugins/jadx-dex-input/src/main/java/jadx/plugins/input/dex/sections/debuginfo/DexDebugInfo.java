package jadx.plugins.input.dex.sections.debuginfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.plugins.input.data.IDebugInfo;
import jadx.api.plugins.input.data.ILocalVar;

final class DexDebugInfo implements IDebugInfo {
	private final int[] offsets;
	private final int[] lines;
	private final int size;
	private final List<ILocalVar> localVars;

	private volatile Map<Integer, Integer> sourceLineMap;

	private DexDebugInfo(int[] offsets, int[] lines, int size, List<ILocalVar> localVars) {
		this.offsets = offsets;
		this.lines = lines;
		this.size = size;
		this.localVars = localVars;
	}

	@Override
	public Map<Integer, Integer> getSourceLineMapping() {
		Map<Integer, Integer> map = sourceLineMap;
		if (map == null) {
			synchronized (this) {
				map = sourceLineMap;
				if (map == null) {
					map = new HashMap<>(Math.max(16, (int) (size / 0.75f) + 1));
					for (int i = 0; i < size; i++) {
						map.put(offsets[i], lines[i]);
					}
					sourceLineMap = map;
				}
			}
		}
		return map;
	}

	@Override
	public int getSourceLineMappingSize() {
		Map<Integer, Integer> map = sourceLineMap;
		return map == null ? size : map.size();
	}

	@Override
	public void forEachSourceLine(SourceLineConsumer consumer) {
		Map<Integer, Integer> map = sourceLineMap;
		if (map != null) {
			map.forEach(consumer::accept);
			return;
		}
		for (int i = 0; i < size; i++) {
			consumer.accept(offsets[i], lines[i]);
		}
	}

	@Override
	public List<ILocalVar> getLocalVars() {
		return localVars;
	}

	static final class Builder {
		private int[] offsets = new int[0];
		private int[] lines = new int[0];
		private int size;

		void put(int offset, int line) {
			if (size != 0 && offsets[size - 1] == offset) {
				lines[size - 1] = line;
				return;
			}
			ensureCapacity(size + 1);
			offsets[size] = offset;
			lines[size] = line;
			size++;
		}

		DexDebugInfo build(List<ILocalVar> localVars) {
			return new DexDebugInfo(offsets, lines, size, localVars);
		}

		private void ensureCapacity(int required) {
			if (required <= offsets.length) {
				return;
			}
			int newLength = Math.max(8, offsets.length * 2);
			offsets = Arrays.copyOf(offsets, newLength);
			lines = Arrays.copyOf(lines, newLength);
		}
	}
}
