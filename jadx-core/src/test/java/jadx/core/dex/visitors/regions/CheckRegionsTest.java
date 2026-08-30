package jadx.core.dex.visitors.regions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.InsnType;
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
