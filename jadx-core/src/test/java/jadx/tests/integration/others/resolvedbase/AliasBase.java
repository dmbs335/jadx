package jadx.tests.integration.others.resolvedbase;

public class AliasBase {
	public AliasBase() {
		forward(this);
	}

	private static void forward(AliasBase receiver) {
		AliasBase alias = receiver;
		alias.callback();
	}

	protected void callback() {
	}
}
