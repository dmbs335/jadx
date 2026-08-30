package jadx.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import jadx.api.JadxDecompiler;
import jadx.api.metadata.ICodeAnnotation.AnnType;
import jadx.api.metadata.ICodeNodeRef;
import jadx.gui.JadxWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class JNodeCacheTest {
	@Test
	void staleSearchReferencesAreSkippedWithoutConcurrentNullCacheFailures() throws Exception {
		try (JadxDecompiler decompiler = new JadxDecompiler()) {
			JadxWrapper wrapper = new JadxWrapper(null) {
				@Override
				public JadxDecompiler getDecompiler() {
					return decompiler;
				}
			};
			JNodeCache cache = new JNodeCache(wrapper);
			ICodeNodeRef staleRef = new TestCodeNodeRef();
			ExecutorService executor = Executors.newFixedThreadPool(8);
			try {
				List<Callable<Object>> calls = new ArrayList<>();
				for (int i = 0; i < 10_000; i++) {
					calls.add(() -> cache.makeFrom(staleRef));
				}
				for (Future<Object> result : executor.invokeAll(calls)) {
					assertThat(result.get()).isNull();
				}
			} finally {
				executor.shutdownNow();
			}
		}
	}

	private static final class TestCodeNodeRef implements ICodeNodeRef {
		private int defPosition;

		@Override
		public int getDefPosition() {
			return defPosition;
		}

		@Override
		public void setDefPosition(int pos) {
			defPosition = pos;
		}

		@Override
		public AnnType getAnnType() {
			// OFFSET annotations intentionally have no Java node and exercise the
			// same null resolution path as a stale method reference after reload.
			return AnnType.OFFSET;
		}
	}
}
