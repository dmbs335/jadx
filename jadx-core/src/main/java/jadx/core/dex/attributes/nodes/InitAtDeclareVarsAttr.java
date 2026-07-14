package jadx.core.dex.attributes.nodes;

import java.util.BitSet;

import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;

public class InitAtDeclareVarsAttr implements IJadxAttribute {
	private final BitSet regs = new BitSet();

	public void add(int regNum) {
		regs.set(regNum);
	}

	public boolean contains(int regNum) {
		return regs.get(regNum);
	}

	@Override
	public AType<InitAtDeclareVarsAttr> getAttrType() {
		return AType.INIT_AT_DECLARE_VARS;
	}
}
