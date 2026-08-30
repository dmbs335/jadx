package jadx.core.utils;

import java.util.BitSet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BitSetCycleDetectorTest {

	@Test
	void detectFixedPoint() {
		BlockUtils.BitSetCycleDetector detector = new BlockUtils.BitSetCycleDetector(bits(1, 3));

		assertThat(detector.update(bits(1, 3))).isTrue();
		assertThat(detector.getCycleLength()).isEqualTo(1);
	}

	@Test
	void detectTwoStateCycle() {
		BlockUtils.BitSetCycleDetector detector = new BlockUtils.BitSetCycleDetector(bits(1));

		assertThat(detector.update(bits(2))).isFalse();
		assertThat(detector.update(bits(1))).isFalse();
		assertThat(detector.update(bits(2))).isTrue();
		assertThat(detector.getCycleLength()).isEqualTo(2);
	}

	@Test
	void detectCycleAfterNonRepeatingPrefix() {
		BlockUtils.BitSetCycleDetector detector = new BlockUtils.BitSetCycleDetector(bits(0));

		assertThat(detector.update(bits(1))).isFalse();
		assertThat(detector.update(bits(2))).isFalse();
		assertThat(detector.update(bits(3))).isFalse();
		assertThat(detector.update(bits(4))).isFalse();
		assertThat(detector.update(bits(2))).isFalse();
		assertThat(detector.update(bits(3))).isTrue();
		assertThat(detector.getCycleLength()).isEqualTo(3);
	}

	@Test
	void doNotReportDistinctStatesAsCycle() {
		BlockUtils.BitSetCycleDetector detector = new BlockUtils.BitSetCycleDetector(bits(0));

		for (int i = 1; i < 20; i++) {
			assertThat(detector.update(bits(i))).isFalse();
		}
	}

	private static BitSet bits(int... values) {
		BitSet bitSet = new BitSet();
		for (int value : values) {
			bitSet.set(value);
		}
		return bitSet;
	}
}
