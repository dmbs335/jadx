package jadx.tests.integration.others.resolvedbase;

public class ExternalFilledArrayChild extends FilledArrayMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 47) {
			throw new AssertionError("filled-array receiver observed changed field value");
		}
	}
}
