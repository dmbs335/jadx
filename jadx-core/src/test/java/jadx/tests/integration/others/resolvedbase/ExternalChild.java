package jadx.tests.integration.others.resolvedbase;

public class ExternalChild extends ResolvedMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 17) {
			throw new AssertionError("superclass callback observed changed field value");
		}
	}
}
