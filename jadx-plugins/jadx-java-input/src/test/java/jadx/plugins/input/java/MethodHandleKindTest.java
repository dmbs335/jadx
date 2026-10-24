package jadx.plugins.input.java;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;

import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.IMethodHandle;
import jadx.api.plugins.input.data.MethodHandleType;
import jadx.plugins.input.java.data.JavaClassData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.H_GETFIELD;
import static org.objectweb.asm.Opcodes.H_GETSTATIC;
import static org.objectweb.asm.Opcodes.H_PUTFIELD;
import static org.objectweb.asm.Opcodes.H_PUTSTATIC;
import static org.objectweb.asm.Opcodes.V1_7;

class MethodHandleKindTest {
	@Test
	void fieldReferenceKindsMatchJvmSpecification() throws IOException {
		String owner = "MethodHandleKinds";
		ClassWriter writer = new ClassWriter(0);
		writer.visit(V1_7, ACC_PUBLIC, owner, null, "java/lang/Object", null);
		writer.visitField(ACC_PUBLIC, "instanceField", "I", null, null).visitEnd();
		writer.visitField(ACC_PUBLIC | ACC_STATIC, "staticField", "I", null, null).visitEnd();
		int[] refs = {
				writer.newHandle(H_GETFIELD, owner, "instanceField", "I", false),
				writer.newHandle(H_GETSTATIC, owner, "staticField", "I", false),
				writer.newHandle(H_PUTFIELD, owner, "instanceField", "I", false),
				writer.newHandle(H_PUTSTATIC, owner, "staticField", "I", false)
		};
		MethodHandleType[] expected = {
				MethodHandleType.INSTANCE_GET,
				MethodHandleType.STATIC_GET,
				MethodHandleType.INSTANCE_PUT,
				MethodHandleType.STATIC_PUT
		};
		writer.visitEnd();

		AtomicInteger visited = new AtomicInteger();
		try (ICodeLoader loader = JavaInputPlugin.loadSingleClass(writer.toByteArray(), owner + ".class")) {
			loader.visitClasses(cls -> {
				JavaClassData classData = (JavaClassData) cls;
				for (int i = 0; i < refs.length; i++) {
					IMethodHandle handle = (IMethodHandle) classData.getConstPoolReader().readAsEncodedValue(refs[i]).getValue();
					assertThat(handle.getType()).isEqualTo(expected[i]);
				}
				visited.incrementAndGet();
			});
		}
		assertThat(visited.get()).isEqualTo(1);
	}
}
