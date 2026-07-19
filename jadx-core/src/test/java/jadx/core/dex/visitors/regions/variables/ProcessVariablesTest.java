package jadx.core.dex.visitors.regions.variables;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessVariablesTest {
	private static final ArgType SOLVER_VAR_TYPE = ArgType.object("androidx.constraintlayout.core.SolverVariable");

	@Test
	void initializesOrphanCodeVariableWithoutTraversingRemovedPhiResult() {
		PhiInsn phiInsn = new PhiInsn(24, 1);
		RegisterArg assign = phiInsn.getResult();
		SSAVar orphanVar = new SSAVar(24, 0, assign);
		orphanVar.setType(SOLVER_VAR_TYPE);
		phiInsn.setResult(null);

		ProcessVariables.initOrphanRegionSsaVar(orphanVar);

		assertThat(orphanVar.isCodeVarSet()).isTrue();
		assertThat(orphanVar.getCodeVar().getType()).isEqualTo(SOLVER_VAR_TYPE);
	}

	@Test
	void initializesOrphanCodeVariableWithUnknownType() {
		PhiInsn phiInsn = new PhiInsn(1, 1);
		SSAVar orphanVar = new SSAVar(1, 97, phiInsn.getResult());
		phiInsn.setResult(null);

		ProcessVariables.initOrphanRegionSsaVar(orphanVar);

		assertThat(orphanVar.isCodeVarSet()).isTrue();
		assertThat(orphanVar.getCodeVar().getType()).isNull();
	}

	@Test
	void checksUsePlacesAgainstSiblingOrderWithoutAllocatingASet() {
		Region root = new Region(null);
		BlockNode before = new BlockNode(0, 0, 0);
		BlockNode check = new BlockNode(1, 1, 1);
		BlockNode after = new BlockNode(2, 2, 2);
		Region nestedAfter = new Region(root);
		BlockNode nestedBlock = new BlockNode(3, 3, 3);
		nestedAfter.getSubBlocks().add(nestedBlock);
		root.getSubBlocks().addAll(List.of(before, check, after, nestedAfter));

		UsePlace checkPlace = new UsePlace(root, check);
		assertThat(ProcessVariables.isAllUseAfter(checkPlace, List.of())).isTrue();
		assertThat(ProcessVariables.isAllUseAfter(
				checkPlace,
				List.of(new UsePlace(root, check), new UsePlace(root, after), new UsePlace(nestedAfter, nestedBlock))))
				.isTrue();
		assertThat(ProcessVariables.isAllUseAfter(checkPlace, List.of(new UsePlace(root, before)))).isFalse();
	}

	@Test
	void rejectsCheckBlockOutsideRegion() {
		Region root = new Region(null);
		BlockNode check = new BlockNode(0, 0, 0);

		assertThat(ProcessVariables.isAllUseAfter(new UsePlace(root, check), List.of())).isFalse();
	}
}
