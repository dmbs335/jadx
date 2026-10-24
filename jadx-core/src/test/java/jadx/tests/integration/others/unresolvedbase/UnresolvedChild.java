package jadx.tests.integration.others.unresolvedbase;

public class UnresolvedChild extends UnresolvedBase {
	private final int initial;

	public UnresolvedChild() {
		initial = 17;
	}

	@Override
	protected int readInitial() {
		return initial;
	}

	public void check() {
		if (observed != 0 || initial != 17) {
			throw new AssertionError("super constructor observed changed field initialization order");
		}
	}
}
