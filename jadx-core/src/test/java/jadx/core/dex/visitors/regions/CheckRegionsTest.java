package jadx.core.dex.visitors.regions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.InsnNode;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

class CheckRegionsTest {
	@Test
	void testRejectMalformedLoopConditionOnlyWithoutExplicitAllowance() {
		assertThat(CheckRegions.hasInvalidLoopCondition(null)).isFalse();
		BlockNode header = block(0);
		assertThat(CheckRegions.hasInvalidLoopCondition(header)).isTrue();
		header.getInstructions().add(new InsnNode(InsnType.IF, 0));
		assertThat(CheckRegions.hasInvalidLoopCondition(header)).isFalse();
		header.getInstructions().add(new InsnNode(InsnType.MOVE, 0));
		assertThat(CheckRegions.hasInvalidLoopCondition(header)).isTrue();
		header.add(AFlag.ALLOW_MULTIPLE_INSNS_LOOP_COND);
		assertThat(CheckRegions.hasInvalidLoopCondition(header)).isFalse();
	}

	@Test
	void testReportOnlyDuplicationRemainingAfterRegionCleanup() {
		BlockNode collapsed = block(0);
		BlockNode emittedTwice = block(1);
		assertThat(CheckRegions.findRemainingUnsafeDuplications(
				List.of(collapsed, emittedTwice), Map.of(collapsed, 1, emittedTwice, 2)))
						.containsExactly(emittedTwice);
		assertThat(CheckRegions.findRemainingUnsafeDuplications(
				List.of(collapsed), Map.of(collapsed, 1))).isEmpty();
	}

	@Test
	void testSkipGeneratedSyntheticDuplicate() {
		BlockNode exit = block(2);
		BlockNode generated = block(0);
		BlockNode duplicate = block(1);
		generated.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		duplicate.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		generated.getSuccessors().add(exit);
		duplicate.getSuccessors().add(exit);
		duplicate.add(AFlag.SYNTHETIC);

		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isTrue();
	}

	@Test
	void testKeepNonSyntheticOrDifferentPath() {
		BlockNode generated = block(0);
		BlockNode duplicate = block(1);
		generated.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		duplicate.getInstructions().add(new InsnNode(InsnType.THROW, 0));
		generated.getSuccessors().add(block(2));
		duplicate.getSuccessors().add(block(3));

		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isFalse();
		duplicate.add(AFlag.SYNTHETIC);
		assertThat(CheckRegions.isGeneratedSyntheticDuplicate(duplicate, Set.of(generated))).isFalse();
	}

	@Test
	void testRecognizeOnlySyntheticSameRegisterMove() {
		BlockNode block = block(0);
		RegisterArg source = register(0, 0, new CodeVar());
		RegisterArg result = register(0, 1, new CodeVar());
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.add(AFlag.SYNTHETIC);
		move.setResult(result);
		move.addArg(source);
		block.getInstructions().add(move);

		assertThat(CheckRegions.isSyntheticSameRegisterMoveBlock(block)).isTrue();

		BlockNode different = block(1);
		InsnNode differentMove = new InsnNode(InsnType.MOVE, 1);
		differentMove.add(AFlag.SYNTHETIC);
		differentMove.setResult(register(1, 1, new CodeVar()));
		differentMove.addArg(register(0, 1, new CodeVar()));
		different.getInstructions().add(differentMove);
		assertThat(CheckRegions.isSyntheticSameRegisterMoveBlock(different)).isFalse();

		block.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		assertThat(CheckRegions.isSyntheticSameRegisterMoveBlock(block)).isFalse();
	}

	@Test
	void testRecognizeMoveUsedOnlyByBoundPhiInput() {
		BlockNode block = block(0);
		RegisterArg source = register(0, 0, new CodeVar());
		source.add(AFlag.METHOD_ARGUMENT);
		RegisterArg result = register(1, 1, new CodeVar());
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(result);
		RegisterArg sourceUse = source.duplicate();
		source.getSVar().use(sourceUse);
		move.addArg(sourceUse);
		block.getInstructions().add(move);

		PhiInsn phi = new PhiInsn(1, 1);
		RegisterArg phiUse = result.duplicate();
		result.getSVar().use(phiUse);
		phi.bindArg(phiUse, block);

		assertThat(CheckRegions.isOrphanPhiProjectionMoveBlock(block)).isTrue();

		RegisterArg secondSource = register(2, 0, new CodeVar());
		secondSource.add(AFlag.METHOD_ARGUMENT);
		RegisterArg secondResult = register(3, 1, new CodeVar());
		InsnNode secondMove = new InsnNode(InsnType.MOVE, 1);
		secondMove.setResult(secondResult);
		RegisterArg secondSourceUse = secondSource.duplicate();
		secondSource.getSVar().use(secondSourceUse);
		secondMove.addArg(secondSourceUse);
		block.getInstructions().add(secondMove);
		PhiInsn secondPhi = new PhiInsn(3, 1);
		RegisterArg secondPhiUse = secondResult.duplicate();
		secondResult.getSVar().use(secondPhiUse);
		secondPhi.bindArg(secondPhiUse, block);

		assertThat(CheckRegions.isOrphanPhiProjectionMoveBlock(block)).isTrue();

		InsnNode nonPhiUse = new InsnNode(InsnType.RETURN, 1);
		RegisterArg returnUse = result.duplicate();
		result.getSVar().use(returnUse);
		nonPhiUse.addArg(returnUse);
		assertThat(CheckRegions.isOrphanPhiProjectionMoveBlock(block)).isFalse();
	}

	@Test
	void testRecognizeTypedProjectionMoveBundle() {
		BlockNode block = block(0);
		RegisterArg source = register(0, 0, new CodeVar());
		RegisterArg result = register(1, 1, new CodeVar());
		InsnNode move = new InsnNode(InsnType.MOVE, 1);
		move.setResult(result);
		RegisterArg sourceUse = source.duplicate();
		source.getSVar().use(sourceUse);
		move.addArg(sourceUse);
		block.getInstructions().add(move);
		InsnNode returnInsn = new InsnNode(InsnType.RETURN, 1);
		RegisterArg resultUse = result.duplicate();
		result.getSVar().use(resultUse);
		returnInsn.addArg(resultUse);

		assertThat(CheckRegions.isOrphanProjectionMoveBundle(block)).isTrue();

		block.getInstructions().add(0, new PhiInsn(2, 0));
		assertThat(CheckRegions.isOrphanProjectionMoveBundle(block)).isTrue();

		block.getInstructions().add(new InsnNode(InsnType.CONST, 0));
		assertThat(CheckRegions.isOrphanProjectionMoveBundle(block)).isFalse();
	}

	private static RegisterArg register(int regNum, int version, CodeVar codeVar) {
		RegisterArg register = InsnArg.reg(regNum, ArgType.INT);
		SSAVar ssaVar = new SSAVar(regNum, version, register);
		ssaVar.forceSetType(ArgType.INT);
		ssaVar.setCodeVar(codeVar);
		return register;
	}

	private static BlockNode block(int id) {
		return new BlockNode(id, id, id);
	}
}
