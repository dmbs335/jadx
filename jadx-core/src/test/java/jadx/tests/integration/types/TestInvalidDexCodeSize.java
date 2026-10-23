package jadx.tests.integration.types;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Adler32;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.android.tools.smali.smali.SmaliOptions;

import jadx.core.dex.nodes.ClassNode;
import jadx.plugins.input.smali.SmaliUtils;
import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestInvalidDexCodeSize extends SmaliTest {
	@TempDir
	Path dexDir;

	@Override
	protected void runChecks(List<ClassNode> classes) {
		org.assertj.core.api.Assertions.assertThat(classes.get(0).root().getErrorsCounter().getErrorCount()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test() throws Exception {
		disableCompilation();
		Path dexFile = assembleWithInstructionCount("TestEmptyDexMethod", 1, 0);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestEmptyDexMethod"))
				.code()
				.contains("Control flow exits empty method without return or throw")
				.contains("Method not decompiled: types.TestEmptyDexMethod.run():void");
	}

	@Test
	public void testTruncatedInstruction() throws Exception {
		disableCompilation();
		Path dexFile = assembleWithInstructionCount("TestTruncatedDexInstruction", 2, 1);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestTruncatedDexInstruction"))
				.code()
				.contains("Instruction extends past declared method code")
				.contains("Method not decompiled: types.TestTruncatedDexInstruction.run():void");
	}

	@Test
	public void testTruncatedPayload() throws Exception {
		disableCompilation();
		Path dexFile = assembleWithInstructionCount("TestTruncatedDexPayload", -1, -1);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestTruncatedDexPayload"))
				.code()
				.contains("Instruction extends past declared method code")
				.contains("Method not decompiled: types.TestTruncatedDexPayload.run():int[]");
	}

	@Test
	public void testMissingConcreteCode() throws Exception {
		disableCompilation();
		File smaliFile = new File("src/test/smali/types/TestEmptyDexMethod.smali");
		if (!smaliFile.exists()) {
			smaliFile = new File("jadx-core", smaliFile.getPath());
		}
		SmaliOptions options = new SmaliOptions();
		options.apiLevel = 28;
		byte[] dex = SmaliUtils.assemble(smaliFile, options);
		ByteBuffer data = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
		int classDataOffset = data.getInt(data.getInt(0x64) + 24);
		int[] cursor = { classDataOffset };
		for (int i = 0; i < 4; i++) {
			readUleb128(dex, cursor); // class data counts
		}
		readUleb128(dex, cursor); // method index delta
		readUleb128(dex, cursor); // access flags
		int start = cursor[0];
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isPositive();
		for (int i = start; i < cursor[0] - 1; i++) {
			dex[i] = (byte) 0x80;
		}
		dex[cursor[0] - 1] = 0;
		updateDexIntegrity(dex, data);
		Path dexFile = dexDir.resolve("missing-code.dex");
		Files.write(dexFile, dex);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestEmptyDexMethod"))
				.code()
				.contains("Concrete DEX method has no code item")
				.contains("Method not decompiled: types.TestEmptyDexMethod.run():void");
	}

	@Test
	public void testNativeMethodWithCode() throws Exception {
		disableCompilation();
		File smaliFile = new File("src/test/smali/types/TestNativeDexCode.smali");
		if (!smaliFile.exists()) {
			smaliFile = new File("jadx-core", smaliFile.getPath());
		}
		SmaliOptions options = new SmaliOptions();
		options.apiLevel = 28;
		byte[] dex = SmaliUtils.assemble(smaliFile, options);
		ByteBuffer data = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
		int classDataOffset = data.getInt(data.getInt(0x64) + 24);
		int[] cursor = { classDataOffset };
		for (int i = 0; i < 4; i++) {
			readUleb128(dex, cursor);
		}
		readUleb128(dex, cursor); // method index delta
		int flagsStart = cursor[0];
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isEqualTo(0x1009);
		org.assertj.core.api.Assertions.assertThat(cursor[0] - flagsStart).isEqualTo(2);
		dex[flagsStart] = (byte) 0x89; // public static native (0x109)
		dex[flagsStart + 1] = 0x02;
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isPositive(); // code offset remains nonzero
		updateDexIntegrity(dex, data);
		Path dexFile = dexDir.resolve("native-with-code.dex");
		Files.write(dexFile, dex);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestNativeDexCode"))
				.code()
				.contains("Native DEX method unexpectedly has a code item")
				.contains("public static void run() {")
				.doesNotContain("native void run() {")
				.contains("Method not decompiled: types.TestNativeDexCode.run():void");
	}

	@Test
	public void testAbstractMethodWithCode() throws Exception {
		disableCompilation();
		File smaliFile = new File("src/test/smali/types/TestAbstractDexCode.smali");
		if (!smaliFile.exists()) {
			smaliFile = new File("jadx-core", smaliFile.getPath());
		}
		SmaliOptions options = new SmaliOptions();
		options.apiLevel = 28;
		byte[] dex = SmaliUtils.assemble(smaliFile, options);
		ByteBuffer data = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
		int classDataOffset = data.getInt(data.getInt(0x64) + 24);
		int[] cursor = { classDataOffset };
		for (int i = 0; i < 4; i++) {
			readUleb128(dex, cursor);
		}
		readUleb128(dex, cursor); // method index delta
		int flagsStart = cursor[0];
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isEqualTo(0x1001);
		org.assertj.core.api.Assertions.assertThat(cursor[0] - flagsStart).isEqualTo(2);
		dex[flagsStart] = (byte) 0x81; // public abstract (0x401)
		dex[flagsStart + 1] = 0x08;
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isPositive(); // code offset remains nonzero
		updateDexIntegrity(dex, data);
		Path dexFile = dexDir.resolve("abstract-with-code.dex");
		Files.write(dexFile, dex);
		assertThat(getClassNodeFromFiles(List.of(dexFile.toFile()), "types.TestAbstractDexCode"))
				.code()
				.contains("Abstract DEX method unexpectedly has a code item")
				.contains("public void run() {")
				.doesNotContain("abstract void run() {")
				.contains("Method not decompiled: types.TestAbstractDexCode.run():void");
	}

	private Path assembleWithInstructionCount(String className, int originalSize, int newSize) throws Exception {
		File smaliFile = new File("src/test/smali/types/" + className + ".smali");
		if (!smaliFile.exists()) {
			smaliFile = new File("jadx-core", smaliFile.getPath());
		}
		SmaliOptions options = new SmaliOptions();
		options.apiLevel = 28;
		byte[] dex = SmaliUtils.assemble(smaliFile, options);
		ByteBuffer data = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
		int classDefsOffset = data.getInt(0x64);
		int classDataOffset = data.getInt(classDefsOffset + 24);
		int[] cursor = { classDataOffset };
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isZero(); // static fields
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isZero(); // instance fields
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isEqualTo(1); // direct methods
		org.assertj.core.api.Assertions.assertThat(readUleb128(dex, cursor)).isZero(); // virtual methods
		readUleb128(dex, cursor); // method index delta
		readUleb128(dex, cursor); // access flags
		int codeOffset = readUleb128(dex, cursor);
		int actualSize = data.getInt(codeOffset + 12);
		if (originalSize >= 0) {
			org.assertj.core.api.Assertions.assertThat(actualSize).isEqualTo(originalSize);
		}
		data.putInt(codeOffset + 12, newSize >= 0 ? newSize : actualSize + newSize);

		updateDexIntegrity(dex, data);

		Path dexFile = dexDir.resolve(className + ".dex");
		Files.write(dexFile, dex);
		return dexFile;
	}

	private static void updateDexIntegrity(byte[] dex, ByteBuffer data) throws Exception {
		byte[] signature = MessageDigest.getInstance("SHA-1").digest(Arrays.copyOfRange(dex, 32, dex.length));
		System.arraycopy(signature, 0, dex, 12, signature.length);
		Adler32 checksum = new Adler32();
		checksum.update(dex, 12, dex.length - 12);
		data.putInt(8, (int) checksum.getValue());
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
