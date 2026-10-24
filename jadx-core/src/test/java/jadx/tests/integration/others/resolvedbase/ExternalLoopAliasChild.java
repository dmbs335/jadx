package jadx.tests.integration.others.resolvedbase;

public class ExternalLoopAliasChild extends LoopAliasMiddle {
	private int observed;

	@Override
	protected void callback() {
		observed = initial;
	}

	public void check() {
		if (observed != 0 || initial != 59) {
			throw new AssertionError("loop-carried receiver observed changed field value");
		}
	}
}
