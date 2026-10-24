package jadx.tests.integration.others.resolvedbase;

public class ExternalReturnEscapeChild extends ReturnEscapeMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 41) {
			throw new AssertionError("returned receiver observed changed field value");
		}
	}
}
