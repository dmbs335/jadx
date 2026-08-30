package jadx.plugins.input.dex.utils;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jadx.plugins.input.dex.DexException;
import jadx.plugins.input.dex.sections.SectionReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class Leb128Test {

	@Test
	void readsUnsignedValuesOfEveryEncodedLength() {
		assertThat(read(0x00)).isZero();
		assertThat(read(0x7f)).isEqualTo(0x7f);
		assertThat(read(0x80, 0x01)).isEqualTo(0x80);
		assertThat(read(0xff, 0x7f)).isEqualTo(0x3fff);
		assertThat(read(0x80, 0x80, 0x01)).isEqualTo(0x4000);
		assertThat(read(0x80, 0x80, 0x80, 0x01)).isEqualTo(0x20_0000);
		assertThat(read(0xff, 0xff, 0xff, 0xff, 0x0f)).isEqualTo(-1);
	}

	@Test
	void rejectsContinuationAfterFifthByte() {
		assertThatExceptionOfType(DexException.class)
				.isThrownBy(() -> read(0x80, 0x80, 0x80, 0x80, 0x80));
	}

	@Test
	void matchesReferenceDecoderForRandomIntDomain() {
		Random random = new Random(0x4a414458L);
		for (int i = 0; i < 100_000; i++) {
			int value = random.nextInt();
			int[] encoded = encode(value);
			assertThat(read(encoded)).isEqualTo(readReference(encoded));
		}
	}

	private static int read(int... bytes) {
		return Leb128.readUnsignedLeb128(new ByteArraySectionReader(bytes));
	}

	private static int readReference(int[] bytes) {
		int result = 0;
		int count = 0;
		int cur;
		do {
			cur = bytes[count];
			result |= (cur & 0x7f) << (count * 7);
			count++;
		} while ((cur & 0x80) != 0 && count < 5);
		return result;
	}

	private static int[] encode(int value) {
		int[] encoded = new int[5];
		int count = 0;
		do {
			int next = value >>> 7;
			encoded[count++] = (value & 0x7f) | (next == 0 ? 0 : 0x80);
			value = next;
		} while (value != 0);
		return java.util.Arrays.copyOf(encoded, count);
	}

	private static final class ByteArraySectionReader extends SectionReader {
		private final int[] bytes;
		private int pos;

		private ByteArraySectionReader(int[] bytes) {
			super(null, 0);
			this.bytes = bytes;
		}

		@Override
		public int readUByte() {
			return bytes[pos++];
		}
	}
}
