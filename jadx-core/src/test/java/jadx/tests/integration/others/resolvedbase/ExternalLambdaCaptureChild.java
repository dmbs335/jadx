package jadx.tests.integration.others.resolvedbase;

public class ExternalLambdaCaptureChild extends LambdaCaptureMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 53) {
			throw new AssertionError("lambda-captured receiver observed changed field value");
		}
	}
}
