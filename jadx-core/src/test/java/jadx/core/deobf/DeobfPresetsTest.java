package jadx.core.deobf;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.core.dex.nodes.PackageNode;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;

class DeobfPresetsTest {

	@TempDir
	Path tempDir;

	@Test
	void testMiddlePackageAliasRoundTrip() throws Exception {
		Path mappingFile = tempDir.resolve("aliases.jobf");
		RootNode writeRoot = buildRoot(mappingFile);
		PackageNode writeLeaf = PackageNode.getOrBuild(writeRoot, "aa.bb.cc");
		PackageNode writeMiddle = writeLeaf.getParentPkg();
		writeMiddle.rename("renamed");

		DeobfPresets writePresets = DeobfPresets.build(writeRoot);
		writePresets.fill(writeRoot);
		writePresets.save();

		RootNode readRoot = buildRoot(mappingFile);
		PackageNode readLeaf = PackageNode.getOrBuild(readRoot, "aa.bb.cc");
		PackageNode readMiddle = readLeaf.getParentPkg();
		DeobfPresets readPresets = DeobfPresets.build(readRoot);
		assertThat(readPresets.load()).isTrue();
		readPresets.apply(readRoot);

		assertThat(readMiddle.getAliasPkgInfo().getName()).isEqualTo("renamed");
		assertThat(readLeaf.getAliasPkgInfo().getFullName()).isEqualTo("aa.renamed.cc");
	}

	private static RootNode buildRoot(Path mappingFile) {
		JadxArgs args = new JadxArgs();
		args.setGeneratedRenamesMappingFile(mappingFile.toFile());
		return new RootNode(new JadxDecompiler(args));
	}
}
