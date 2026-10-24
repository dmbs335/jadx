package jadx.tests.integration.others.resolvedbase;

public class ForwardBase {
	public ForwardBase() {
		forward(7L, this);
	}

	private static void forward(long unused, ForwardBase receiver) {
		receiver.callback();
	}

	protected void callback() {
	}
}
