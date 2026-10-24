package jadx.tests.integration.others.resolvedbase;

public class LoopAliasBase {
	public LoopAliasBase() {
		forward(this);
	}

	private static void forward(LoopAliasBase receiver) {
		LoopAliasBase alias = null;
		for (int i = 0; i < 2; i++) {
			if (alias != null) {
				alias.callback();
			}
			alias = receiver;
		}
	}

	protected void callback() {
	}
}
