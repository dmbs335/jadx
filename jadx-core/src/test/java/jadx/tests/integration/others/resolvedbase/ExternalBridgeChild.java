package jadx.tests.integration.others.resolvedbase;

public class ExternalBridgeChild extends BridgeMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 19) {
			throw new AssertionError("private bridge observed changed field value");
		}
	}
}
