package jadx.core.codegen;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.impl.SimpleCodeInfo;
import jadx.core.codegen.json.JsonCodeGen;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.ProcessAnonymous;
import jadx.core.utils.RegionUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class CodeGen {

	public static ICodeInfo generate(ClassNode cls) {
		if (cls.contains(AFlag.DONT_GENERATE)) {
			return ICodeInfo.EMPTY;
		}
		JadxArgs args = cls.root().getArgs();
		switch (args.getOutputFormat()) {
			case JAVA:
				return generateJavaCode(cls, args);

			case JSON:
				return generateJson(cls);

			default:
				throw new JadxRuntimeException("Unknown output format");
		}
	}

	private static ICodeInfo generateJavaCode(ClassNode cls, JadxArgs args) {
		prepareAnonymousClasses(cls);
		ClassGen clsGen = new ClassGen(cls, args);
		return wrapCodeGen(cls, clsGen::makeClass);
	}

	private static void prepareAnonymousClasses(ClassNode cls) {
		Set<ClassNode> classes = new HashSet<>();
		classes.add(cls);
		cls.getInnerAndInlinedClassesRecursive(classes);
		if (classes.stream().noneMatch(currentCls -> currentCls.contains(AType.ANONYMOUS_CLASS))) {
			return;
		}
		Set<InsnNode> seenInsns = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<ClassNode> duplicatedAnonymousClasses = new HashSet<>();
		for (ClassNode currentCls : classes) {
			for (MethodNode mth : currentCls.getMethods()) {
				if (mth.getRegion() == null
						|| mth.getBasicBlocks() == null
						|| mth.getBasicBlocks().stream().noneMatch(block -> block.contains(AFlag.DUPLICATED))) {
					continue;
				}
				RegionUtils.visitBlocks(mth, mth.getRegion(),
						block -> block.getInstructions()
								.forEach(insn -> insn.visitInsns((Consumer<InsnNode>) innerInsn -> collectDuplicatedAnonymousClass(
										cls, innerInsn, seenInsns, duplicatedAnonymousClasses))));
			}
		}
		duplicatedAnonymousClasses.forEach(ProcessAnonymous::convertToInner);
	}

	private static void collectDuplicatedAnonymousClass(
			ClassNode topCls,
			InsnNode insn,
			Set<InsnNode> seenInsns,
			Set<ClassNode> duplicatedAnonymousClasses) {
		if (seenInsns.add(insn) || insn.getType() != InsnType.CONSTRUCTOR) {
			return;
		}
		ConstructorInsn constructorInsn = (ConstructorInsn) insn;
		if (!constructorInsn.isNewInstance()) {
			return;
		}
		ClassNode constructorCls = topCls.root().resolveClass(constructorInsn.getClassType());
		if (constructorCls != null && constructorCls.contains(AType.ANONYMOUS_CLASS)) {
			duplicatedAnonymousClasses.add(constructorCls);
		}
	}

	private static ICodeInfo generateJson(ClassNode cls) {
		JsonCodeGen codeGen = new JsonCodeGen(cls);
		String clsJson = wrapCodeGen(cls, codeGen::process);
		return new SimpleCodeInfo(clsJson);
	}

	private static <R> R wrapCodeGen(ClassNode cls, Callable<R> codeGenFunc) {
		try {
			return codeGenFunc.call();
		} catch (Exception e) {
			if (cls.contains(AFlag.RESTART_CODEGEN)) {
				cls.remove(AFlag.RESTART_CODEGEN);
				try {
					return codeGenFunc.call();
				} catch (Exception ex) {
					throw new JadxRuntimeException("Code generation error after restart", ex);
				}
			} else {
				throw new JadxRuntimeException("Code generation error", e);
			}
		}
	}

	private CodeGen() {
	}
}
