package jadx.tests.integration.others.resolvedbase;

public class ExternalAliasChild extends AliasMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 29) {
			throw new AssertionError("aliased receiver observed changed field value");
		}
	}
}
