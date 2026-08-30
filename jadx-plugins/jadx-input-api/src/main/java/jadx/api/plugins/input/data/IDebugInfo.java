package jadx.api.plugins.input.data;

import java.util.List;
import java.util.Map;

public interface IDebugInfo {
	@FunctionalInterface
	interface SourceLineConsumer {
		void accept(int codeOffset, int sourceLine);
	}

	/**
	 * Map instruction offset to source line number
	 */
	Map<Integer, Integer> getSourceLineMapping();

	default int getSourceLineMappingSize() {
		return getSourceLineMapping().size();
	}

	default void forEachSourceLine(SourceLineConsumer consumer) {
		getSourceLineMapping().forEach(consumer::accept);
	}

	List<ILocalVar> getLocalVars();
}
