package jadx.plugins.input.java;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import jadx.api.plugins.input.ICodeLoader;
import jadx.api.plugins.input.data.ICodeReader;
import jadx.api.plugins.input.insns.Opcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

class HighConstantPoolInvokeTest {
	@Test
	void invokeIndexIsUnsigned() throws IOException {
		ClassWriter writer = new ClassWriter(0);
		writer.visit(V1_5, ACC_PUBLIC, "HighConstantPoolInvoke", null, "java/lang/Object", null);
		for (int i = 0; i < 32768; i++) {
			writer.newUTF8("padding" + i);
		}
		MethodVisitor target = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "target", "()V", null, null);
		target.visitCode();
		target.visitInsn(RETURN);
		target.visitMaxs(0, 0);
		target.visitEnd();

		MethodVisitor caller = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "caller", "()V", null, null);
		caller.visitCode();
		caller.visitMethodInsn(INVOKESTATIC, "HighConstantPoolInvoke", "target", "()V", false);
		caller.visitInsn(RETURN);
		caller.visitMaxs(0, 0);
		caller.visitEnd();
		writer.visitEnd();

		AtomicInteger invokes = new AtomicInteger();
		try (ICodeLoader loader = JavaInputPlugin.loadSingleClass(writer.toByteArray(), "HighConstantPoolInvoke.class")) {
			loader.visitClasses(cls -> cls.visitFieldsAndMethods(field -> {
			}, method -> {
				if (method.getMethodRef().getName().equals("caller")) {
					ICodeReader code = method.getCodeReader();
					assertThat(code).isNotNull();
					code.visitInstructions(insn -> {
						if (insn.getOpcode() == Opcode.INVOKE_STATIC) {
							insn.decode();
							assertThat(insn.getIndex()).isGreaterThan(32767);
							assertThat(insn.getIndexAsMethod().getName()).isEqualTo("target");
							invokes.incrementAndGet();
						}
					});
				}
			}));
		}
		assertThat(invokes.get()).isEqualTo(1);
	}
}
