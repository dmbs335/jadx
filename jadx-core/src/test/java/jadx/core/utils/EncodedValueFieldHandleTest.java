package jadx.core.utils;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.plugins.input.data.IFieldRef;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.impl.FieldRefHandle;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.nodes.RootNode;

import static org.assertj.core.api.Assertions.assertThat;

class EncodedValueFieldHandleTest {
	@Test
	void fieldHandlesUseMatchingLookupMethods() {
		RootNode root = new RootNode(new JadxArgs());
		IFieldRef field = new IFieldRef() {
			@Override
			public String getParentClassType() {
				return "Lsample/Owner;";
			}

			@Override
			public String getName() {
				return "value";
			}

			@Override
			public String getType() {
				return "I";
			}
		};
		assertLookup(root, field, MethodHandleType.INSTANCE_GET, "findGetter");
		assertLookup(root, field, MethodHandleType.STATIC_GET, "findStaticGetter");
		assertLookup(root, field, MethodHandleType.INSTANCE_PUT, "findSetter");
		assertLookup(root, field, MethodHandleType.STATIC_PUT, "findStaticSetter");
	}

	private static void assertLookup(RootNode root, IFieldRef field, MethodHandleType type, String methodName) {
		EncodedValue value = new EncodedValue(EncodedType.ENCODED_METHOD_HANDLE, new FieldRefHandle(type, field));
		InsnArg arg = EncodedValueUtils.convertToInsnArg(root, value);
		assertThat(arg).isInstanceOf(InsnWrapArg.class);
		assertThat(((InsnWrapArg) arg).getWrapInsn()).isInstanceOf(InvokeNode.class);
		InvokeNode invoke = (InvokeNode) ((InsnWrapArg) arg).getWrapInsn();
		assertThat(invoke.getCallMth().getName()).isEqualTo(methodName);
		assertThat(invoke.getArgsCount()).isEqualTo(4);
	}
}
