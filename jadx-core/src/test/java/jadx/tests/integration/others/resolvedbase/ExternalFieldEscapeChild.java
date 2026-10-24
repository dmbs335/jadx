package jadx.tests.integration.others.resolvedbase;

public class ExternalFieldEscapeChild extends FieldEscapeMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 43) {
			throw new AssertionError("field-escaped receiver observed changed field value");
		}
	}
}
