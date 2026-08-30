package jadx.api.impl.passes;

import jadx.api.plugins.pass.JadxPass;
import jadx.api.plugins.pass.types.JadxPreparePass;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.utils.exceptions.JadxException;

public class PreparePassWrapper extends AbstractVisitor implements IPassWrapperVisitor {
	private final JadxPreparePass preparePass;

	public PreparePassWrapper(JadxPreparePass preparePass) {
		this.preparePass = preparePass;
	}

	@Override
	public JadxPass getPass() {
		return preparePass;
	}

	@Override
	public void init(RootNode root) throws JadxException {
		try {
			preparePass.init(root);
		} catch (Exception e) {
			root.getErrorsCounter().addGlobalError("Error in prepare pass init: " + this, e);
		}
	}

	@Override
	public String getName() {
		return preparePass.getInfo().getName();
	}
}
