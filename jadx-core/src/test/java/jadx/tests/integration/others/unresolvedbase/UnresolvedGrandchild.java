package jadx.tests.integration.others.unresolvedbase;

public class UnresolvedGrandchild extends UnresolvedMiddle {
	@Override
	protected int readInitial() {
		return initial;
	}

	public void check() {
		if (observed != 0 || initial != 17) {
			throw new AssertionError("super constructor observed changed inherited field initialization order");
		}
	}
}
