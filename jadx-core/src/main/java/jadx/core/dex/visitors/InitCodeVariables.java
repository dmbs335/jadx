package jadx.core.dex.visitors;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.instructions.PhiInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.CodeVar;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.visitors.ssa.SSATransform;
import jadx.core.utils.exceptions.JadxException;
import jadx.core.utils.exceptions.JadxRuntimeException;

@JadxVisitor(
		name = "InitCodeVariables",
		desc = "Initialize code variables",
		runAfter = SSATransform.class
)
public class InitCodeVariables extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) throws JadxException {
		initCodeVars(mth);
	}

	public static void rerun(MethodNode mth) {
		List<SSAVar> sVars = mth.getSVars();
		int sVarsCount = sVars.size();
		for (int i = 0; i < sVarsCount; i++) {
			sVars.get(i).resetTypeAndCodeVar();
		}
		initCodeVars(mth);
	}

	private static void initCodeVars(MethodNode mth) {
		RegisterArg thisArg = mth.getThisArg();
		if (thisArg != null) {
			initCodeVar(mth, thisArg);
		}
		List<RegisterArg> argRegs = mth.getArgRegs();
		int argsCount = argRegs.size();
		for (int i = 0; i < argsCount; i++) {
			initCodeVar(mth, argRegs.get(i));
		}
		List<SSAVar> sVars = mth.getSVars();
		int sVarsCount = sVars.size();
		for (int i = 0; i < sVarsCount; i++) {
			initCodeVar(sVars.get(i));
		}
	}

	public static void initCodeVar(MethodNode mth, RegisterArg regArg) {
		SSAVar ssaVar = regArg.getSVar();
		if (ssaVar == null) {
			ssaVar = mth.makeNewSVar(regArg);
		}
		initCodeVar(ssaVar);
	}

	public static void initCodeVar(SSAVar ssaVar) {
		if (ssaVar.isCodeVarSet()) {
			return;
		}
		CodeVar codeVar = new CodeVar();
		RegisterArg assignArg = ssaVar.getAssign();
		if (assignArg.contains(AFlag.THIS)) {
			codeVar.setName(RegisterArg.THIS_ARG_NAME);
			codeVar.setThis(true);
		}
		if (assignArg.contains(AFlag.METHOD_ARGUMENT) || assignArg.contains(AFlag.CUSTOM_DECLARE)) {
			codeVar.setDeclared(true);
		}
		setCodeVar(ssaVar, codeVar);
	}

	private static void setCodeVar(SSAVar ssaVar, CodeVar codeVar) {
		List<PhiInsn> phiList = ssaVar.getPhiList();
		if (!phiList.isEmpty()) {
			Set<SSAVar> vars = new LinkedHashSet<>();
			vars.add(ssaVar);
			collectConnectedVars(phiList, vars);
			setCodeVarType(codeVar, vars);
			for (SSAVar var : vars) {
				if (var.isCodeVarSet()) {
					codeVar.mergeFlagsFrom(var.getCodeVar());
				}
				var.setCodeVar(codeVar);
			}
		} else {
			ssaVar.setCodeVar(codeVar);
		}
	}

	private static void setCodeVarType(CodeVar codeVar, Set<SSAVar> vars) {
		if (vars.size() > 1) {
			ArgType singleType = null;
			List<ArgType> distinctTypes = null;
			for (SSAVar var : vars) {
				ArgType type = var.getImmutableType();
				if (type == null || !type.isTypeKnown()) {
					continue;
				}
				if (singleType == null) {
					singleType = type;
				} else if (!singleType.equals(type)) {
					if (distinctTypes == null) {
						distinctTypes = new ArrayList<>();
						distinctTypes.add(singleType);
					}
					if (!distinctTypes.contains(type)) {
						distinctTypes.add(type);
					}
				}
			}
			if (distinctTypes == null) {
				if (singleType != null) {
					codeVar.setType(singleType);
				}
			} else {
				boolean referenceJoin = true;
				int typesCount = distinctTypes.size();
				for (int i = 0; i < typesCount; i++) {
					if (!distinctTypes.get(i).getPrimitiveType().isObjectOrArray()) {
						referenceJoin = false;
						break;
					}
				}
				if (referenceJoin) {
					// A valid DEX register can join distinct reference types at a phi (for example
					// Guava selects byte[], short[] or int[] for one hash-table Object field).
					// Preserve the common Java assignment type instead of treating the concrete
					// immutable input types as a broken SSA variable.
					codeVar.setType(ArgType.OBJECT);
				} else {
					throw new JadxRuntimeException(
							"Several immutable types in one variable: " + distinctTypes + ", vars: " + vars);
				}
			}
		}
	}

	private static void collectConnectedVars(List<PhiInsn> phiInsnList, Set<SSAVar> vars) {
		if (phiInsnList.isEmpty()) {
			return;
		}
		int phiCount = phiInsnList.size();
		for (int i = 0; i < phiCount; i++) {
			PhiInsn phiInsn = phiInsnList.get(i);
			RegisterArg result = phiInsn.getResult();
			if (result != null) {
				SSAVar resultVar = result.getSVar();
				if (resultVar != null && vars.add(resultVar)) {
					collectConnectedVars(resultVar.getPhiList(), vars);
				}
			}
			int argsCount = phiInsn.getArgsCount();
			for (int argIndex = 0; argIndex < argsCount; argIndex++) {
				InsnArg arg = phiInsn.getArg(argIndex);
				SSAVar sVar = ((RegisterArg) arg).getSVar();
				if (sVar != null && vars.add(sVar)) {
					collectConnectedVars(sVar.getPhiList(), vars);
				}
			}
		}
	}
}
