package jadx.tests.functional;

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
import jadx.core.dex.visitors.InitCodeVariables;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiverPhiCodeVarTest {
	@Test
	void preservesReceiverForInternalPhiCopies() {
		ArgType ownerType = ArgType.object("sample.Owner");
		RegisterArg receiverAssign = InsnArg.reg(0, ownerType);
		receiverAssign.add(AFlag.THIS);
		SSAVar receiver = new SSAVar(0, 0, receiverAssign);

		RegisterArg copyAssign = InsnArg.reg(0, ownerType);
		SSAVar copy = new SSAVar(0, 1, copyAssign);
		RegisterArg receiverUse = InsnArg.reg(0, ownerType);
		receiver.use(receiverUse);
		InsnNode copyInsn = new InsnNode(InsnType.MOVE, 1);
		copyInsn.addArg(receiverUse);
		copyInsn.setResult(copyAssign);

		PhiInsn phi = new PhiInsn(0, 2);
		SSAVar joined = new SSAVar(0, 2, phi.getResult());
		RegisterArg receiverInput = InsnArg.reg(0, ownerType);
		phi.bindArg(receiverInput, new BlockNode(0, 0, 0));
		receiver.use(receiverInput);
		receiver.addUsedInPhi(phi);
		RegisterArg copyInput = InsnArg.reg(0, ownerType);
		phi.bindArg(copyInput, new BlockNode(1, 1, 1));
		copy.use(copyInput);
		copy.addUsedInPhi(phi);

		InitCodeVariables.initCodeVar(receiver);
		assertThat(receiver.getCodeVar()).isSameAs(copy.getCodeVar()).isSameAs(joined.getCodeVar());
		assertThat(joined.getCodeVar().isThis()).isTrue();
		assertThat(joined.getCodeVar().isInitFromThis()).isFalse();
	}

	@Test
	void keepsReceiverSeparateFromJoinedLocal() {
		ArgType ownerType = ArgType.object("sample.Owner");
		RegisterArg receiverAssign = InsnArg.reg(0, ownerType);
		receiverAssign.add(AFlag.THIS);
		SSAVar receiver = new SSAVar(0, 0, receiverAssign);

		RegisterArg otherAssign = InsnArg.reg(0, ownerType);
		SSAVar other = new SSAVar(0, 1, otherAssign);
		RegisterArg externalAssign = InsnArg.reg(1, ownerType);
		SSAVar external = new SSAVar(1, 0, externalAssign);
		RegisterArg externalUse = InsnArg.reg(1, ownerType);
		external.use(externalUse);
		InsnNode externalMove = new InsnNode(InsnType.MOVE, 1);
		externalMove.addArg(externalUse);
		externalMove.setResult(otherAssign);
		PhiInsn phi = new PhiInsn(0, 2);
		SSAVar joined = new SSAVar(0, 2, phi.getResult());
		RegisterArg receiverInput = InsnArg.reg(0, ownerType);
		phi.bindArg(receiverInput, new BlockNode(0, 0, 0));
		receiver.use(receiverInput);
		receiver.addUsedInPhi(phi);
		RegisterArg otherInput = InsnArg.reg(0, ownerType);
		phi.bindArg(otherInput, new BlockNode(1, 1, 1));
		other.use(otherInput);
		other.addUsedInPhi(phi);

		InitCodeVariables.initCodeVar(receiver);
		CodeVar receiverCodeVar = receiver.getCodeVar();
		CodeVar joinedCodeVar = joined.getCodeVar();
		assertThat(receiverCodeVar.isThis()).isTrue();
		assertThat(joinedCodeVar).isSameAs(other.getCodeVar()).isNotSameAs(receiverCodeVar);
		assertThat(joinedCodeVar.isThis()).isFalse();
		assertThat(joinedCodeVar.isInitFromThis()).isTrue();
	}
}
