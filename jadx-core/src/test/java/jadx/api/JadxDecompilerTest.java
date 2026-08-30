package jadx.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.api.args.GeneratedRenamesMappingFileMode;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.JadxPluginInfoBuilder;
import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.IClassData;
import jadx.api.plugins.loader.JadxPluginLoader;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.FieldReplaceAttr;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.PackageNode;
import jadx.core.dex.nodes.ProcessState;
import jadx.core.dex.nodes.RootNode;
import jadx.core.xmlgen.ResContainer;
import jadx.plugins.input.dex.DexInputPlugin;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JadxDecompilerTest {

	@TempDir
	File testDir;

	@Test
	public void testExampleUsage() {
		File sampleApk = getFileFromSampleDir("app-with-fake-dex.apk");

		// test simple apk loading
		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(sampleApk);
		args.setOutDir(testDir);

		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			jadx.load();
			jadx.save();
			jadx.printErrorsReport();

			// test class print
			for (JavaClass cls : jadx.getClasses()) {
				System.out.println(cls.getCode());
			}

			assertThat(jadx.getClasses()).hasSize(3);
			assertThat(jadx.getErrorsCount()).isEqualTo(1);
			assertThat(jadx.getAnalysisLossCounts())
					.containsExactlyEntriesOf(Collections.singletonMap("input-load.embedded-dex", 1));
		}
	}

	@Test
	public void testDirectDexInput() throws IOException {
		try (JadxDecompiler jadx = new JadxDecompiler();
				InputStream in = new FileInputStream(getFileFromSampleDir("hello.dex"))) {
			jadx.addCustomCodeLoader(new DexInputPlugin().loadDexFromInputStream(in, "input"));
			jadx.load();
			for (JavaClass cls : jadx.getClasses()) {
				System.out.println(cls.getCode());
			}
			assertThat(jadx.getClasses()).hasSize(1);
			assertThat(jadx.getErrorsCount()).isEqualTo(0);
		}
	}

	@Test
	public void testClosesInputBeforePluginUnload() {
		List<String> closeOrder = new ArrayList<>();
		JadxPlugin plugin = new JadxPlugin() {
			@Override
			public JadxPluginInfo getPluginInfo() {
				return JadxPluginInfoBuilder.pluginId("close-order-test")
						.name("Close order test")
						.description("Verify plugin-owned input lifecycle")
						.build();
			}

			@Override
			public void init(JadxPluginContext context) {
				context.addCodeInput(input -> new ICodeLoader() {
					@Override
					public void visitClasses(Consumer<IClassData> consumer) {
					}

					@Override
					public boolean isEmpty() {
						return false;
					}

					@Override
					public void close() {
						closeOrder.add("input");
					}
				});
			}

			@Override
			public void unload() {
				closeOrder.add("plugin");
			}
		};

		JadxArgs args = new JadxArgs();
		args.addInputFile(getFileFromSampleDir("hello.dex"));
		args.setPluginLoader(new JadxPluginLoader() {
			@Override
			public List<JadxPlugin> load() {
				return List.of(plugin);
			}

			@Override
			public void close() {
			}
		});
		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			jadx.load();
		}

		assertThat(closeOrder).containsSubsequence("input", "plugin");
	}

	@Test
	public void testUnloadClassesAtTaskBoundary() throws IOException {
		try (JadxDecompiler jadx = new JadxDecompiler();
				InputStream in = new FileInputStream(getFileFromSampleDir("hello.dex"))) {
			jadx.addCustomCodeLoader(new DexInputPlugin().loadDexFromInputStream(in, "input"));
			jadx.load();
			ClassNode cls = jadx.getClasses().get(0).getClassNode();
			jadx.getRoot().getProcessClasses().forceProcess(cls);
			assertThat(cls.getState()).isEqualTo(ProcessState.PROCESS_COMPLETE);

			assertThat(jadx.unloadClasses()).isEqualTo(1);
			assertThat(cls.getState()).isEqualTo(ProcessState.GENERATED_AND_UNLOADED);
			assertThat(cls.contains(AFlag.CLASS_DEEP_RELOAD)).isTrue();

			String code = jadx.getClasses().get(0).getCode();
			assertThat(code).contains("class HelloWorld");
			assertThat(cls.getState()).isEqualTo(ProcessState.GENERATED_AND_UNLOADED);
		}
	}

	@Test
	public void testUnloadClassesDropsFieldReplacementIrReference() {
		JadxArgs args = new JadxArgs();
		args.addInputFile(getFileFromSampleDir("app-with-fake-dex.apk"));
		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			jadx.load();
			ClassNode cls = jadx.getRoot().getClasses().stream()
					.filter(node -> !node.getFields().isEmpty())
					.findFirst()
					.orElseThrow();
			jadx.getRoot().getProcessClasses().forceProcess(cls.getTopParentClass());

			FieldNode field = cls.getFields().get(0);
			field.addAttr(new FieldReplaceAttr(InsnArg.reg(0, ArgType.INT)));
			assertThat(field.get(AType.FIELD_REPLACE)).isNotNull();

			jadx.unloadClasses();
			assertThat(field.get(AType.FIELD_REPLACE)).isNull();
		}
	}

	@Test
	public void testRejectSamePrimaryAndDependencyInput() {
		File sampleDex = getFileFromSampleDir("hello.dex");
		JadxArgs args = new JadxArgs();
		args.addInputFile(sampleDex);
		args.getDependencyInputFiles().add(sampleDex);
		assertThat(args.isDependencyInputFile(sampleDex.getAbsolutePath())).isTrue();

		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			assertThatThrownBy(jadx::load)
					.hasMessageContaining("Input file can't also be a dependency");
		}
	}

	@Test
	public void testVirtualClassSourceIsNotDependencyInput() {
		JadxArgs args = new JadxArgs();
		String virtualClassSource = "input.jar:pkg/Test.class";
		assertThat(args.isDependencyInputFile(virtualClassSource)).isFalse();

		args.getDependencyInputFiles().add(getFileFromSampleDir("hello.dex"));
		assertThat(args.isDependencyInputFile(virtualClassSource)).isFalse();
	}

	@Test
	public void testResourcesLoad() {
		File sampleApk = getFileFromSampleDir("app-with-fake-dex.apk");

		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(sampleApk);
		args.setOutDir(testDir);
		args.setSkipSources(true);
		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			jadx.load();
			List<ResourceFile> resources = jadx.getResources();
			assertThat(resources).hasSize(8);
			ResourceFile arsc = resources.stream()
					.filter(r -> r.getType() == ResourceType.ARSC)
					.findFirst().orElseThrow();
			ResContainer resContainer = arsc.loadContent();
			ResContainer xmlRes = resContainer.getSubFiles().stream()
					.filter(r -> r.getName().equals("res/values/colors.xml"))
					.findFirst().orElseThrow();
			assertThat(xmlRes.getText())
					.code()
					.containsOne("<color name=\"colorPrimary\">#008577</color>");
		}
	}

	private static final String TEST_SAMPLES_DIR = "test-samples/";

	public static File getFileFromSampleDir(String fileName) {
		URL resource = JadxDecompilerTest.class.getClassLoader().getResource(TEST_SAMPLES_DIR + fileName);
		assertThat(resource).isNotNull();
		String pathStr = resource.getFile();
		return new File(pathStr);
	}

	// TODO add more tests

	@Test
	public void testConvertPackageHierarchy() {
		JadxDecompiler jadx = new JadxDecompiler();
		RootNode root = new RootNode(jadx);
		PackageNode leafPkgNode = PackageNode.getOrBuild(root, "com.example.app");
		PackageNode rootPkgNode = leafPkgNode.getParentPkg().getParentPkg();

		JavaPackage rootPkg = jadx.convertPackageNode(rootPkgNode);

		assertThat(rootPkg.getFullName()).isEqualTo("com");
		assertThat(rootPkg.isRoot()).isTrue();
		assertThat(rootPkg.isLeaf()).isFalse();
		assertThat(rootPkg.getSubPackages())
				.extracting(JavaPackage::getFullName)
				.containsExactly("com.example");
		JavaPackage middlePkg = rootPkg.getSubPackages().get(0);
		assertThat(middlePkg.getFullName()).isEqualTo("com.example");
		assertThat(middlePkg.getSubPackages())
				.extracting(JavaPackage::getFullName)
				.containsExactly("com.example.app");
		JavaPackage leafPkg = middlePkg.getSubPackages().get(0);
		assertThat(leafPkg.getFullName()).isEqualTo("com.example.app");
		assertThat(leafPkg.isLeaf()).isTrue();
	}

	@Test
	public void testGetJavaNodeByRefReusesConvertedPackage() {
		JadxDecompiler jadx = new JadxDecompiler();
		RootNode root = new RootNode(jadx);
		PackageNode pkgNode = PackageNode.getOrBuild(root, "com.example");

		JavaNode javaNode = jadx.getJavaNodeByRef(pkgNode);

		assertThat(javaNode).isNotNull();
		assertThat(javaNode.getCodeNodeRef()).isSameAs(pkgNode);
		assertThat(jadx.getJavaNodeByRef(pkgNode)).isSameAs(javaNode);
		assertThat(jadx.convertPackageNode(pkgNode)).isSameAs(javaNode);
	}

	@Test
	public void testGeneratedRenamesMappingRoundTrip() {
		File input = getFileFromSampleDir("app-with-fake-dex.apk");
		File mapping = new File(testDir, "roundtrip.jobf");
		List<String> first;
		try (JadxDecompiler jadx = new JadxDecompiler(buildMappingArgs(
				input, mapping, GeneratedRenamesMappingFileMode.OVERWRITE))) {
			jadx.load();
			first = collectAliases(jadx);
		}
		assertThat(mapping).exists().isNotEmpty();

		try (JadxDecompiler jadx = new JadxDecompiler(buildMappingArgs(
				input, mapping, GeneratedRenamesMappingFileMode.READ))) {
			jadx.load();
			assertThat(collectAliases(jadx)).containsExactlyElementsOf(first);
		}
	}

	private static JadxArgs buildMappingArgs(File input, File mapping, GeneratedRenamesMappingFileMode mode) {
		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(input);
		args.setSkipSources(true);
		args.setSkipResources(true);
		args.setCodeCache(NoOpCodeCache.INSTANCE);
		args.setUsageInfoCache(new EmptyUsageInfoCache());
		args.setDeobfuscationOn(true);
		args.setDeobfuscationMinLength(64);
		args.setGeneratedRenamesMappingFile(mapping);
		args.setGeneratedRenamesMappingFileMode(mode);
		return args;
	}

	private static List<String> collectAliases(JadxDecompiler jadx) {
		List<String> aliases = new ArrayList<>();
		for (PackageNode pkg : jadx.getRoot().getPackages()) {
			if (pkg.hasAlias() || pkg.hasParentAlias()) {
				aliases.add("p " + pkg.getPkgInfo().getFullName() + " = " + pkg.getAliasPkgInfo().getFullName());
			}
		}
		for (ClassNode cls : jadx.getRoot().getClasses()) {
			if (cls.getClassInfo().hasAlias()) {
				aliases.add("c " + cls.getRawName() + " = " + cls.getFullName());
			}
			cls.getFields().stream()
					.filter(field -> field.getFieldInfo().hasAlias())
					.forEach(field -> aliases.add("f " + field.getFieldInfo().getRawFullId() + " = " + field.getAlias()));
			cls.getMethods().stream()
					.filter(method -> method.getMethodInfo().hasAlias())
					.forEach(method -> aliases.add("m " + method.getMethodInfo().getRawFullId() + " = " + method.getAlias()));
		}
		Collections.sort(aliases);
		return aliases;
	}
}
