package jadx.tests.integration.others.resolvedbase;

public class RelayBase {
	public RelayBase() {
		new Dispatcher().dispatch(this);
	}

	protected void callback() {
	}

	public static final class Dispatcher {
		public final void dispatch(RelayBase receiver) {
			receiver.callback();
		}
	}
}
