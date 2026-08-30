package jadx.plugins.input.dex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.AccessFlags;
import jadx.api.plugins.input.data.AccessFlagsScope;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.data.IFieldRef;
import jadx.plugins.input.dex.sections.DexClassData;
import jadx.plugins.input.dex.sections.DexCodeReader;
import jadx.plugins.input.dex.sections.SectionReader;
import jadx.plugins.input.dex.sections.annotations.AnnotationsParser;
import jadx.plugins.input.dex.utils.SmaliTestUtils;
import jadx.zip.IZipEntry;
import jadx.zip.IZipParser;
import jadx.zip.ZipContent;
import jadx.zip.ZipReader;

import static org.assertj.core.api.Assertions.assertThat;

class DexInputPluginTest {

	@Test
	public void loadSampleApk() throws Exception {
		processFile(Paths.get(ClassLoader.getSystemResource("samples/app-with-fake-dex.apk").toURI()));
	}

	@Test
	void inspectsNonDexEntriesWithoutMaterializingThem() {
		byte[] resource = new byte[1024 * 1024];
		resource[0] = (byte) 0x89;
		resource[1] = 'P';
		resource[2] = 'N';
		resource[3] = 'G';
		AtomicInteger streamBytesRead = new AtomicInteger();
		IZipEntry entry = new IZipEntry() {
			@Override
			public String getName() {
				return "res/drawable/large.png";
			}

			@Override
			public byte[] getBytes() {
				throw new AssertionError("Non-DEX entry must not be materialized");
			}

			@Override
			public InputStream getInputStream() {
				return new ByteArrayInputStream(resource) {
					@Override
					public synchronized int read(byte[] bytes, int offset, int length) {
						int count = super.read(bytes, offset, length);
						if (count > 0) {
							streamBytesRead.addAndGet(count);
						}
						return count;
					}
				};
			}

			@Override
			public long getCompressedSize() {
				return resource.length;
			}

			@Override
			public long getUncompressedSize() {
				return resource.length;
			}

			@Override
			public boolean isDirectory() {
				return false;
			}

			@Override
			public File getZipFile() {
				return new File("sample.apk");
			}

			@Override
			public boolean preferBytes() {
				return true;
			}
		};
		IZipParser parser = new IZipParser() {
			@Override
			public ZipContent open() {
				return new ZipContent(this, List.of(entry));
			}

			@Override
			public void close() {
			}
		};
		DexFileLoader loader = new DexFileLoader(new DexInputOptions());
		loader.setZipReader(new ZipReader() {
			@Override
			public ZipContent open(File file) throws IOException {
				return parser.open();
			}
		});

		assertThat(loader.collectDexFiles(List.of(Paths.get("sample.apk")))).isEmpty();
		assertThat(streamBytesRead).hasValueLessThan(resource.length);
	}

	@Test
	public void loadHelloWorld() throws Exception {
		processFile(Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI()));
	}

	@Test
	public void loadTestSmali() throws Exception {
		processFile(SmaliTestUtils.compileSmaliFromResource("samples/test.smali"));
	}

	@Test
	public void cacheDexStringAfterSecondRead() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		DexReader dexReader = new DexFileLoader(new DexInputOptions())
				.loadDexReaders(sample.toString(), content)
				.get(0);
		SectionReader reader = new SectionReader(dexReader, 0);

		String first = reader.getString(0);
		String second = reader.getString(0);
		String third = reader.getString(0);

		assertThat(first).isEqualTo(second).isNotSameAs(second);
		assertThat(second).isSameAs(third);
	}

	@Test
	public void cacheDexStructuralStringOnFirstRead() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		DexReader dexReader = new DexFileLoader(new DexInputOptions())
				.loadDexReaders(sample.toString(), content)
				.get(0);
		SectionReader reader = new SectionReader(dexReader, 0);

		String first = reader.getStringCached(0);
		String second = reader.getString(0);

		assertThat(first).isSameAs(second);
	}

	@Test
	public void sectionReaderCopiesKeepIndependentLittleEndianPositions() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		DexReader dexReader = new DexFileLoader(new DexInputOptions())
				.loadDexReaders(sample.toString(), content)
				.get(0);
		SectionReader first = new SectionReader(dexReader, 0).absPos(32);
		SectionReader second = first.copy(32);

		assertThat(first.getByteCode(0, 8)).containsExactly(
				(byte) 'd', (byte) 'e', (byte) 'x', (byte) '\n', (byte) '0', (byte) '3', (byte) '5', (byte) 0);
		assertThat(first.getAbsPos()).isEqualTo(32);
		assertThat(first.readInt()).isEqualTo(content.length);
		assertThat(second.readInt()).isEqualTo(content.length);
		assertThat(first.getAbsPos()).isEqualTo(36);
		assertThat(second.getAbsPos()).isEqualTo(36);
	}

	@Test
	public void dexCodeReaderDoesNotRetainNestedSectionReader() {
		assertThat(DexCodeReader.class).isAssignableTo(SectionReader.class);
		assertThat(DexCodeReader.class.getDeclaredFields())
				.noneMatch(field -> field.getType() == SectionReader.class);
	}

	@Test
	public void classAndAnnotationReadersDoNotRetainNestedSectionReaders() {
		assertThat(DexClassData.class).isAssignableTo(SectionReader.class);
		assertThat(AnnotationsParser.class).isAssignableTo(SectionReader.class);
		assertThat(DexClassData.class.getDeclaredFields())
				.noneMatch(field -> field.getType() == SectionReader.class);
		assertThat(AnnotationsParser.class.getDeclaredFields())
				.noneMatch(field -> field.getType() == SectionReader.class);
	}

	@Test
	public void classIterationAdvancesEmbeddedReaderOffset() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/app-with-fake-dex.apk").toURI());
		List<String> classTypes = new java.util.ArrayList<>();
		try (ICodeLoader loader = new DexInputPlugin().loadFiles(List.of(sample))) {
			loader.visitClasses(cls -> classTypes.add(cls.getType()));
		}

		assertThat(classTypes).hasSizeGreaterThan(1).doesNotHaveDuplicates();
	}

	@Test
	public void visitsDexFieldRefWithoutMaterializedRefContractChange() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		DexReader dexReader = new DexFileLoader(new DexInputOptions())
				.loadDexReaders(sample.toString(), content)
				.get(0);
		SectionReader reader = new SectionReader(dexReader, 0);
		IFieldRef materialized = reader.getFieldRef(0);
		String[] visited = new String[3];

		reader.visitFieldRef(0, (owner, name, type) -> {
			visited[0] = owner;
			visited[1] = name;
			visited[2] = type;
		});

		assertThat(visited).containsExactly(materialized.getParentClassType(),
				materialized.getName(), materialized.getType());
	}

	@Test
	public void reportsRejectedZipDex(@TempDir Path tempDir) throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		content[content.length - 1] ^= 1;
		Path apk = tempDir.resolve("bad-checksum.apk");
		try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(apk))) {
			output.putNextEntry(new ZipEntry("assets/bad.dex"));
			output.write(content);
			output.closeEntry();
		}

		DexInputOptions options = new DexInputOptions();
		options.setOptions(Collections.emptyMap());
		List<String> errors = new java.util.ArrayList<>();
		DexFileLoader loader = new DexFileLoader(options);
		loader.setLoadErrorHandler((category, message, error) -> errors.add(category + ": " + message + ": " + error.getMessage()));

		assertThat(loader.collectDexFiles(List.of(apk))).isEmpty();
		assertThat(errors).singleElement()
				.asString()
				.contains("input-load.embedded-dex")
				.contains("provenance=EMBEDDED_DEX")
				.contains("sha256=")
				.contains("assets/bad.dex")
				.contains("Bad dex file checksum");
	}

	@Test
	void excludesOnlyFingerprintMatchedEmbeddedDex(@TempDir Path tempDir) throws Exception {
		byte[] content = badChecksumDex();
		String sha256 = toHex(MessageDigest.getInstance("SHA-256").digest(content));
		Path apk = zipDex(tempDir.resolve("embedded.apk"), "assets/bad.dex", content);
		DexInputOptions options = new DexInputOptions();
		options.setOptions(Map.of("dex-input.audit-excluded-sha256", sha256));
		List<String> errors = new java.util.ArrayList<>();
		List<String> exclusions = new java.util.ArrayList<>();
		DexFileLoader loader = new DexFileLoader(options);
		loader.setLoadErrorHandler((category, message, error) -> errors.add(category));
		loader.setLoadExclusionHandler((category, message, error) -> exclusions.add(category + ":" + message));

		assertThat(loader.collectDexFiles(List.of(apk))).isEmpty();
		assertThat(errors).isEmpty();
		assertThat(exclusions).singleElement().asString()
				.contains("input-load.embedded-dex")
				.contains("sha256=" + sha256);
	}

	@Test
	void neverExcludesApkRootDex(@TempDir Path tempDir) throws Exception {
		byte[] content = badChecksumDex();
		String sha256 = toHex(MessageDigest.getInstance("SHA-256").digest(content));
		Path apk = zipDex(tempDir.resolve("root.apk"), "classes.dex", content);
		DexInputOptions options = new DexInputOptions();
		options.setOptions(Map.of("dex-input.audit-excluded-sha256", sha256));
		List<String> errors = new java.util.ArrayList<>();
		List<String> exclusions = new java.util.ArrayList<>();
		DexFileLoader loader = new DexFileLoader(options);
		loader.setLoadErrorHandler((category, message, error) -> errors.add(category + ":" + message));
		loader.setLoadExclusionHandler((category, message, error) -> exclusions.add(category));

		assertThat(loader.collectDexFiles(List.of(apk))).isEmpty();
		assertThat(errors).singleElement().asString()
				.contains("input-load.apk-root-dex")
				.contains("provenance=APK_ROOT_DEX");
		assertThat(exclusions).isEmpty();
	}

	private static byte[] badChecksumDex() throws Exception {
		Path sample = Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI());
		byte[] content = Files.readAllBytes(sample);
		content[content.length - 1] ^= 1;
		return content;
	}

	private static Path zipDex(Path apk, String entry, byte[] content) throws IOException {
		try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(apk))) {
			output.putNextEntry(new ZipEntry(entry));
			output.write(content);
			output.closeEntry();
		}
		return apk;
	}

	private static String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			result.append(String.format("%02x", value & 0xff));
		}
		return result.toString();
	}

	private static void processFile(Path sample) throws IOException {
		System.out.println("Input file: " + sample.toAbsolutePath());
		long start = System.currentTimeMillis();
		List<Path> files = Collections.singletonList(sample);
		try (ICodeLoader result = new DexInputPlugin().loadFiles(files)) {
			assertThat(result.getClassesCount()).isPositive();
			assertThat(result.getMethodsCount()).isPositive();
			assertThat(result.getFieldsCount()).isNotNegative();
			assertThat(result.getTypesCount()).isPositive();
			AtomicInteger count = new AtomicInteger();
			result.visitClasses(cls -> {
				System.out.println();
				System.out.println("Class: " + cls.getType());
				System.out.println("AccessFlags: " + AccessFlags.format(cls.getAccessFlags(), AccessFlagsScope.CLASS));
				System.out.println("SuperType: " + cls.getSuperType());
				System.out.println("Interfaces: " + cls.getInterfacesTypes());
				System.out.println("Attributes: " + cls.getAttributes());
				count.getAndIncrement();

				cls.visitFieldsAndMethods(
						System.out::println,
						mth -> {
							System.out.println("---");
							System.out.println(mth);
							ICodeReader codeReader = mth.getCodeReader();
							if (codeReader != null) {
								codeReader.visitInstructions(insn -> {
									insn.decode();
									System.out.println(insn);
								});
							}
							System.out.println("---");
							System.out.println(mth.disassembleMethod());
							System.out.println("---");
						});
				System.out.println("----");
				System.out.println(cls.getDisassembledCode());
				System.out.println("----");
			});
			assertThat(count.get()).isEqualTo(result.getClassesCount());
		}
		System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
	}
}
