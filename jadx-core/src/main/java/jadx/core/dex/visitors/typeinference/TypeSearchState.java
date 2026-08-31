package jadx.core.dex.visitors.typeinference;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class TypeSearchState {

	private final Map<SSAVar, TypeSearchVarInfo> varInfoMap;
	private final List<TypeSearchVarInfo> allVars;

	public TypeSearchState(MethodNode mth) {
		List<SSAVar> vars = mth.getSVars();
		this.varInfoMap = new IdentityHashMap<>(vars.size());
		this.allVars = new ArrayList<>(vars.size());
		for (SSAVar var : vars) {
			TypeSearchVarInfo varInfo = new TypeSearchVarInfo(var);
			varInfoMap.put(var, varInfo);
			allVars.add(varInfo);
		}
	}

	@NotNull
	public TypeSearchVarInfo getVarInfo(SSAVar var) {
		TypeSearchVarInfo varInfo = this.varInfoMap.get(var);
		if (varInfo == null) {
			throw new JadxRuntimeException("TypeSearchVarInfo not found in map for var: " + var);
		}
		return varInfo;
	}

	public ArgType getArgType(InsnArg arg) {
		if (arg.isRegister()) {
			RegisterArg reg = (RegisterArg) arg;
			return getVarInfo(reg.getSVar()).getCurrentType();
		}
		return arg.getType();
	}

	Iterable<TypeSearchVarInfo> getAllVars() {
		return allVars;
	}

	public List<TypeSearchVarInfo> getUnresolvedVars() {
		List<TypeSearchVarInfo> result = new ArrayList<>(allVars.size());
		for (TypeSearchVarInfo varInfo : allVars) {
			if (!varInfo.isTypeResolved()) {
				result.add(varInfo);
			}
		}
		return result;
	}

}
