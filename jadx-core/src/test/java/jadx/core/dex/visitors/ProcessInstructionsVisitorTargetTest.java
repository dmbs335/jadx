package jadx.core.dex.visitors;

import org.junit.jupiter.api.Test;

import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessInstructionsVisitorTargetTest {
	private final InsnNode[] instructions = new InsnNode[4];
	private final boolean[] payloadStarts = new boolean[4];

	@Test
	void rejectEmptyDexCode() {
		assertThatThrownBy(() -> ProcessInstructionsVisitor.validateDexMethodHasInstructions(new InsnNode[0]))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Control flow exits empty method without return or throw");
		assertThatCode(() -> ProcessInstructionsVisitor.validateDexMethodHasInstructions(instructions))
				.doesNotThrowAnyException();
	}

	@Test
	void rejectBranchOutsideMethod() {
		assertThatThrownBy(() -> ProcessInstructionsVisitor.validateDexBranchTarget(instructions, payloadStarts, 1, -1))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Branch target is outside method");
		assertThatThrownBy(() -> ProcessInstructionsVisitor.validateDexBranchTarget(instructions, payloadStarts, 1, 4))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Branch target is outside method");
	}

	@Test
	void rejectHandlerOutsideMethodButAllowAbsentCatchAll() {
		boolean[] handlerStarts = new boolean[4];
		ProcessInstructionsVisitor.markHandlerStart(instructions, handlerStarts, payloadStarts, -1);
		assertThat(handlerStarts).containsOnly(false);
		assertThatThrownBy(() -> ProcessInstructionsVisitor.markHandlerStart(instructions, handlerStarts, payloadStarts, -2))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Exception handler target is outside method");
		assertThatThrownBy(() -> ProcessInstructionsVisitor.markHandlerStart(instructions, handlerStarts, payloadStarts, 4))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Exception handler target is outside method");
	}

	@Test
	void rejectBranchIntoInstructionBodyButAllowNop() {
		assertThatThrownBy(() -> ProcessInstructionsVisitor.validateDexBranchTarget(instructions, payloadStarts, 0, 1))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Branch target is not an instruction");
		instructions[1] = new InsnNode(InsnType.NOP, 0);
		assertThatCode(() -> ProcessInstructionsVisitor.validateDexBranchTarget(instructions, payloadStarts, 0, 1))
				.doesNotThrowAnyException();
	}

	@Test
	void rejectHandlerIntoInstructionBodyButAllowNop() {
		boolean[] handlerStarts = new boolean[4];
		assertThatThrownBy(() -> ProcessInstructionsVisitor.markHandlerStart(instructions, handlerStarts, payloadStarts, 1))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Exception handler target is not an instruction");
		instructions[1] = new InsnNode(InsnType.NOP, 0);
		assertThatCode(() -> ProcessInstructionsVisitor.markHandlerStart(instructions, handlerStarts, payloadStarts, 1))
				.doesNotThrowAnyException();
		assertThat(handlerStarts[1]).isTrue();
	}

	@Test
	void stopWhenJumpCannotBeLinked() {
		assertThatThrownBy(() -> ProcessInstructionsVisitor.addJump(instructions, 0, 1))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Failed to set jump");
		assertThatThrownBy(() -> ProcessInstructionsVisitor.addJump(instructions, 0, 4))
				.isInstanceOf(JadxRuntimeException.class)
				.hasMessageContaining("Failed to set jump");
		instructions[1] = new InsnNode(InsnType.NOP, 0);
		assertThatCode(() -> ProcessInstructionsVisitor.addJump(instructions, 0, 1))
				.doesNotThrowAnyException();
	}
}
