package jadx.tests.integration.others.resolvedbase;

public class ExternalForwardChild extends ForwardMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 23) {
			throw new AssertionError("forwarded receiver observed changed field value");
		}
	}
}
