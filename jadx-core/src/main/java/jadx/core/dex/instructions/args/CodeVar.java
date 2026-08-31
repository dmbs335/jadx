package jadx.core.dex.instructions.args;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadx.api.metadata.annotations.VarNode;

public class CodeVar {
	private String name;
	private ArgType type; // before type inference can be null and set only for immutable types
	private List<SSAVar> ssaVars = Collections.emptyList();

	private boolean isFinal;
	private boolean isThis;
	private boolean isDeclared;
	private boolean initAtDeclaration;

	private VarNode cachedVarNode; // set and used at codegen stage

	public static CodeVar fromMthArg(RegisterArg mthArg, boolean linkRegister) {
		CodeVar var = new CodeVar();
		var.setType(mthArg.getInitType());
		var.setName(mthArg.getName());
		var.setThis(mthArg.isThis());
		var.setDeclared(true);
		var.setThis(mthArg.isThis());
		if (linkRegister) {
			var.setSsaVars(Collections.singletonList(new SSAVar(mthArg.getRegNum(), 0, mthArg)));
		}
		return var;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArgType getType() {
		return type;
	}

	public void setType(ArgType type) {
		this.type = type;
	}

	public List<SSAVar> getSsaVars() {
		return ssaVars;
	}

	public void addSsaVar(SSAVar ssaVar) {
		if (ssaVars.isEmpty()) {
			ssaVars = Collections.singletonList(ssaVar);
			return;
		}
		if (ssaVars.contains(ssaVar)) {
			return;
		}
		if (!(ssaVars instanceof ArrayList)) {
			List<SSAVar> list = new ArrayList<>(Math.max(3, ssaVars.size() + 1));
			if (ssaVars.size() == 1) {
				// Avoid addAll's temporary array and iterator for the common compact form.
				list.add(ssaVars.get(0));
			} else {
				list.addAll(ssaVars);
			}
			ssaVars = list;
		}
		ssaVars.add(ssaVar);
	}

	public void setSsaVars(List<SSAVar> ssaVars) {
		this.ssaVars = ssaVars;
	}

	public SSAVar getAnySsaVar() {
		if (ssaVars.isEmpty()) {
			throw new IllegalStateException("CodeVar without SSA variables attached: " + this);
		}
		return ssaVars.get(0);
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean aFinal) {
		isFinal = aFinal;
	}

	public boolean isThis() {
		return isThis;
	}

	public void setThis(boolean aThis) {
		isThis = aThis;
	}

	public boolean isDeclared() {
		return isDeclared;
	}

	public void setDeclared(boolean declared) {
		isDeclared = declared;
	}

	public boolean isInitAtDeclaration() {
		return initAtDeclaration;
	}

	public void setInitAtDeclaration(boolean initAtDeclaration) {
		this.initAtDeclaration = initAtDeclaration;
	}

	public VarNode getCachedVarNode() {
		return cachedVarNode;
	}

	public void setCachedVarNode(VarNode varNode) {
		this.cachedVarNode = varNode;
	}

	/**
	 * Merge flags with OR operator
	 */
	public void mergeFlagsFrom(CodeVar other) {
		if (other.isDeclared()) {
			setDeclared(true);
		}
		if (other.isThis()) {
			setThis(true);
		}
		if (other.isFinal()) {
			setFinal(true);
		}
		if (other.isInitAtDeclaration()) {
			setInitAtDeclaration(true);
		}
	}

	@Override
	public String toString() {
		return (isFinal ? "final " : "") + type + ' ' + name;
	}
}
