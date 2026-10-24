package jadx.core.dex.instructions.invokedynamic;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.api.plugins.input.data.IMethodProto;
import jadx.api.plugins.input.data.annotations.EncodedType;
import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.insns.InsnData;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.nodes.InsnNode;

import static org.assertj.core.api.Assertions.assertThat;

class CustomStringConcatTest {
	@Test
	void nullBootstrapConstantIsAnExplicitError() {
		InsnNode result = CustomStringConcat.buildStringConcat(noArgInsn(), false,
				values("\u0002", EncodedValue.NULL));
		assertError(result, "Null string concat bootstrap constant");
	}

	@Test
	void missingOrUnusedConstantsAreExplicitErrors() {
		assertError(CustomStringConcat.buildStringConcat(noArgInsn(), false, values("\u0002")),
				"excess constant tags");
		assertError(CustomStringConcat.buildStringConcat(noArgInsn(), false,
				values("", new EncodedValue(EncodedType.ENCODED_STRING, "unused"))),
				"recipe does not match");
	}

	private static void assertError(InsnNode result, String message) {
		assertThat(result.getType()).isEqualTo(InsnType.NOP);
		assertThat(result.contains(AType.JADX_ERROR)).isTrue();
		assertThat(result.get(AType.JADX_ERROR).getList().get(0).getCause()).hasMessageContaining(message);
	}

	private static List<EncodedValue> values(String recipe, EncodedValue... constants) {
		IMethodProto proto = new IMethodProto() {
			@Override
			public String getReturnType() {
				return "Ljava/lang/String;";
			}

			@Override
			public List<String> getArgTypes() {
				return Collections.emptyList();
			}
		};
		List<EncodedValue> values = new ArrayList<>();
		values.add(EncodedValue.NULL); // bootstrap method handle is not read by buildStringConcat
		values.add(new EncodedValue(EncodedType.ENCODED_STRING, "makeConcatWithConstants"));
		values.add(new EncodedValue(EncodedType.ENCODED_METHOD_TYPE, proto));
		values.add(new EncodedValue(EncodedType.ENCODED_STRING, recipe));
		Collections.addAll(values, constants);
		return values;
	}

	private static InsnData noArgInsn() {
		return (InsnData) Proxy.newProxyInstance(InsnData.class.getClassLoader(), new Class<?>[] { InsnData.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
						case "getRegsCount":
							return 0;
						case "getResultReg":
							return -1;
						default:
							throw new UnsupportedOperationException(method.getName());
					}
				});
	}
}
