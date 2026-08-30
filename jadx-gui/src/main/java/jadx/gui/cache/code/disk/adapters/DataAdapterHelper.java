package jadx.gui.cache.code.disk.adapters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

public class DataAdapterHelper {

	public static void writeNullableUTF(DataOutput out, @Nullable String str) throws IOException {
		if (str == null) {
			out.writeByte(0);
		} else {
			out.writeByte(1);
			out.writeUTF(str);
		}
	}

	public static @Nullable String readNullableUTF(DataInput in) throws IOException {
		if (in.readByte() == 0) {
			return null;
		}
		return in.readUTF();
	}

	/**
	 * Write unsigned variable length integer (ULEB128 encoding)
	 */
	public static void writeUVInt(DataOutput out, int val) throws IOException {
		if (val < 0) {
			throw new IllegalArgumentException("Expect value >= 0, got: " + val);
		}
		int current = val;
		int next = val;
		while (true) {
			next >>>= 7;
			if (next == 0) {
				// last byte
				out.writeByte(current & 0x7f);
				return;
			}
			out.writeByte((current & 0x7f) | 0x80);
			current = next;
		}
	}

	/**
	 * Read unsigned variable length integer (ULEB128 encoding)
	 */
	public static int readUVInt(DataInput in) throws IOException {
		int result = 0;
		for (int shift = 0; shift <= 28; shift += 7) {
			int value = in.readUnsignedByte();
			if (shift == 28 && (value & 0x78) != 0) {
				throw new IOException("Unsigned variable integer overflow");
			}
			result |= (value & 0x7f) << shift;
			if ((value & 0x80) == 0) {
				return result;
			}
		}
		throw new IOException("Unsigned variable integer is too long");
	}
}
