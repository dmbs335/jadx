package jadx.tests.integration.others.resolvedbase;

public class ExternalArrayEscapeChild extends ArrayEscapeMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 37) {
			throw new AssertionError("array-escaped receiver observed changed field value");
		}
	}
}
