package jadx.core.dex.instructions;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.plugins.input.insns.InsnData;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;

class InvokeCustomNodeTest {
	@Test
	void distinguishesCallInstructionsWithDifferentArguments() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.Lambda");
		MethodInfo lambda = MethodInfo.fromDetails(root, cls, "lambda", List.of(), ArgType.OBJECT);
		MethodInfo target = MethodInfo.fromDetails(root, cls, "target", List.of(ArgType.INT), ArgType.VOID);
		InvokeCustomNode first = makeCustomNode(lambda, target, 1);
		InvokeCustomNode second = makeCustomNode(lambda, target, 2);

		assertThat(first.isSame(second)).isFalse();
		assertThat(first.isSame(makeCustomNode(lambda, target, 1))).isTrue();
	}

	@Test
	void customCallMustNotMatchPlainCallInEitherOrder() {
		RootNode root = new RootNode(new JadxArgs());
		ClassInfo cls = ClassInfo.fromName(root, "test.Lambda");
		MethodInfo lambda = MethodInfo.fromDetails(root, cls, "lambda", List.of(), ArgType.OBJECT);
		MethodInfo target = MethodInfo.fromDetails(root, cls, "target", List.of(ArgType.INT), ArgType.VOID);
		InvokeNode plain = new InvokeNode(lambda, InvokeType.CUSTOM, 0);
		InvokeCustomNode custom = makeCustomNode(lambda, target, 1);

		assertThat(plain.isSame(custom)).isFalse();
		assertThat(custom.isSame(plain)).isFalse();
	}

	private static InvokeCustomNode makeCustomNode(MethodInfo lambda, MethodInfo target, int value) {
		InsnData insn = (InsnData) Proxy.newProxyInstance(InsnData.class.getClassLoader(),
				new Class<?>[] { InsnData.class }, (proxy, method, args) -> {
					if (method.getName().equals("getResultReg")) {
						return -1;
					}
					throw new UnsupportedOperationException(method.getName());
				});
		InvokeCustomNode node = new InvokeCustomNode(lambda, insn, false, false);
		node.setImplMthInfo(target);
		InvokeNode call = new InvokeNode(target, InvokeType.STATIC, 1);
		call.addArg(InsnArg.lit(value, ArgType.INT));
		node.setCallInsn(call);
		return node;
	}
}
