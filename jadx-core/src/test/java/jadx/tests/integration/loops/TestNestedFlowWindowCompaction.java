package jadx.tests.integration.loops;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

/**
 * Regression for a cyclic phi graph formed by an outer allocation loop and an
 * inner in-place stream compaction loop. LoopRegionVisitor used to follow that
 * graph recursively without recording visited SSA variables.
 */
public class TestNestedFlowWindowCompaction extends IntegrationTest {

	public static class TestCls {
		private int connectionWindow;

		public int writeStreams(Stream[] streams) {
			Collections.shuffle(Arrays.asList(streams));
			int window = connectionWindow;
			int activeCount = streams.length;
			while (activeCount > 0 && window > 0) {
				int chunk = (int) Math.ceil((float) window / activeCount);
				int nextCount = 0;
				for (int index = 0; index < activeCount && window > 0; index++) {
					Stream stream = streams[index];
					int allocation = Math.min(window, Math.min(chunk,
							Math.max(0, Math.min(stream.window, stream.pending) - stream.allocated)));
					if (allocation > 0) {
						stream.allocated += allocation;
						window -= allocation;
					}
					int remaining = Math.max(0, Math.min(stream.window, stream.pending) - stream.allocated);
					if (remaining > 0) {
						streams[nextCount++] = stream;
					}
				}
				activeCount = nextCount;
			}
			return window;
		}
	}

	public static class Stream {
		int window;
		int pending;
		int allocated;
	}

	@Test
	public void test() {
		assertThat(getClassNode(TestCls.class))
				.code()
				.containsOne("while (activeCount > 0 && window > 0) {")
				.containsOne("Math.ceil(((float) window) / ((float) activeCount))")
				.containsOne("activeCount = nextCount2;")
				.doesNotContain("Method not decompiled")
				.doesNotContain("JADX ERROR");
	}
}
