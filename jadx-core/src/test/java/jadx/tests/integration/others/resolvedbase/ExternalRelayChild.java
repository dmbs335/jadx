package jadx.tests.integration.others.resolvedbase;

public class ExternalRelayChild extends RelayMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 31) {
			throw new AssertionError("relayed receiver observed changed field value");
		}
	}
}
