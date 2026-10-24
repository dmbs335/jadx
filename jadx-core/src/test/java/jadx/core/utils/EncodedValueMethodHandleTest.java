package jadx.core.utils;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.plugins.input.data.IMethodRef;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.impl.MethodRefHandle;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncodedValueMethodHandleTest {
	@Test
	void booleanBootstrapArgumentsPreserveValue() {
		RootNode root = new RootNode(new JadxArgs());
		InsnArg trueArg = EncodedValueUtils.convertToInsnArg(root, new EncodedValue(EncodedType.ENCODED_BOOLEAN, true));
		InsnArg falseArg = EncodedValueUtils.convertToInsnArg(root, new EncodedValue(EncodedType.ENCODED_BOOLEAN, false));
		assertThat(((LiteralArg) trueArg).getLiteral()).isEqualTo(1);
		assertThat(((LiteralArg) falseArg).getLiteral()).isEqualTo(0);
	}

	@Test
	void constructorHandleUsesTwoArgumentLookup() {
		RootNode root = new RootNode(new JadxArgs());
		IMethodRef methodRef = methodRef("<init>");
		EncodedValue value = new EncodedValue(EncodedType.ENCODED_METHOD_HANDLE,
				new MethodRefHandle(MethodHandleType.INVOKE_CONSTRUCTOR, methodRef));
		InsnArg arg = EncodedValueUtils.convertToInsnArg(root, value);
		assertThat(arg).isInstanceOf(InsnWrapArg.class);
		InvokeNode invoke = (InvokeNode) ((InsnWrapArg) arg).getWrapInsn();
		assertThat(invoke.getCallMth().getName()).isEqualTo("findConstructor");
		assertThat(invoke.getCallMth().getArgumentsTypes()).hasSize(2);
		assertThat(invoke.getArgsCount()).isEqualTo(3); // receiver plus Class and MethodType
	}

	@Test
	void directHandleUsesSpecialLookupWithCallerClass() {
		RootNode root = new RootNode(new JadxArgs());
		EncodedValue value = new EncodedValue(EncodedType.ENCODED_METHOD_HANDLE,
				new MethodRefHandle(MethodHandleType.INVOKE_DIRECT, methodRef("run")));
		assertThatThrownBy(() -> EncodedValueUtils.convertToInsnArg(root, value))
				.hasMessageContaining("Missing caller class");
		InsnArg arg = EncodedValueUtils.convertToInsnArg(root, value, ArgType.object("Lsample/Caller;"));
		InvokeNode invoke = (InvokeNode) ((InsnWrapArg) arg).getWrapInsn();
		assertThat(invoke.getCallMth().getName()).isEqualTo("findSpecial");
		assertThat(invoke.getCallMth().getArgumentsTypes()).hasSize(4);
		assertThat(invoke.getArgsCount()).isEqualTo(5);
	}

	private static IMethodRef methodRef(String name) {
		IMethodRef methodRef = new IMethodRef() {
			@Override
			public int getUniqId() {
				return 0;
			}

			@Override
			public void load() {
			}

			@Override
			public String getParentClassType() {
				return "Lsample/Owner;";
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getReturnType() {
				return "V";
			}

			@Override
			public java.util.List<String> getArgTypes() {
				return Collections.singletonList("I");
			}
		};
		return methodRef;
	}
}
