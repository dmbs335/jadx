package jadx.tests.integration.types;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.zip.Adler32;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.android.tools.smali.smali.SmaliOptions;

import jadx.core.dex.nodes.ClassNode;
import jadx.plugins.input.smali.SmaliUtils;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidDexTryBoundary extends SmaliTest {
	@TempDir
	Path dexDir;

	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void testTryStartInsideInstruction() throws Exception {
		assertInvalidTry((data, tryOffset) -> {
			org.assertj.core.api.Assertions.assertThat(data.getInt(tryOffset)).isZero();
			data.putInt(tryOffset, 1); // middle of the two-unit const/16 instruction
		}, "Try range starts inside instruction", "try-start-inside-instruction.dex");
	}

	@Test
	public void testTryEndInsideInstruction() throws Exception {
		assertInvalidTry((data, tryOffset) -> {
			org.assertj.core.api.Assertions.assertThat(data.getShort(tryOffset + 4)).isEqualTo((short) 2);
			data.putShort(tryOffset + 4, (short) 1); // end falls inside const/16
		}, "Try range ends inside instruction", "try-end-inside-instruction.dex");
	}

	@Test
	public void testHandlerInsideInstruction() throws Exception {
		assertInvalidTry((data, tryOffset) -> {
			int handlerListOffset = tryOffset + 8;
			org.assertj.core.api.Assertions.assertThat(data.get(handlerListOffset)).isEqualTo((byte) 1); // one handler list
			org.assertj.core.api.Assertions.assertThat(data.get(handlerListOffset + 1)).isZero(); // catchall only
			data.put(handlerListOffset + 2, (byte) 1); // middle of const/16
		}, "Exception handler target is not an instruction", "handler-inside-instruction.dex");
	}

	private void assertInvalidTry(BiConsumer<ByteBuffer, Integer> mutate, String error, String fileName) throws Exception {
		disableCompilation();
		File smaliFile = new File("src/test/smali/types/TestInvalidDexTryBoundary.smali");
		if (!smaliFile.exists()) {
			smaliFile = new File("jadx-core", smaliFile.getPath());
		}
		SmaliOptions options = new SmaliOptions();
		options.apiLevel = 28;
		byte[] dex = SmaliUtils.assemble(smaliFile, options);
		ByteBuffer data = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
		int classDataOffset = data.getInt(data.getInt(0x64) + 24);
		int[] cursor = { classDataOffset };
		for (int i = 0; i < 6; i++) {
			readUleb128(dex, cursor); // class data counts, method index, access flags
		}
		int codeOffset = readUleb128(dex, cursor);
		int insnsSize = data.getInt(codeOffset + 12);
		org.assertj.core.api.Assertions.assertThat(data.getShort(codeOffset + 6)).isEqualTo((short) 1);
		int tryOffset = codeOffset + 16 + insnsSize * 2 + (insnsSize % 2) * 2;
		mutate.accept(data, tryOffset);
		byte[] signature = MessageDigest.getInstance("SHA-1").digest(Arrays.copyOfRange(dex, 32, dex.length));
		System.arraycopy(signature, 0, dex, 12, signature.length);
		Adler32 checksum = new Adler32();
		checksum.update(dex, 12, dex.length - 12);
		data.putInt(8, (int) checksum.getValue());
		Path dexFile = dexDir.resolve(fileName);
		Files.write(dexFile, dex);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestInvalidDexTryBoundary"))
				.code()
				.contains(error)
				.contains("Method not decompiled: types.TestInvalidDexTryBoundary.run():int");
	}

	private static int readUleb128(byte[] data, int[] cursor) {
		int value = 0;
		for (int shift = 0; shift < 35; shift += 7) {
			int octet = data[cursor[0]++] & 0xff;
			value |= (octet & 0x7f) << shift;
			if ((octet & 0x80) == 0) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid ULEB128 value");
	}
}
