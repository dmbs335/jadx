package jadx.tests.integration.others.unresolvedbase;

public class NestedPrivateFieldOwner {
	public static class Middle extends UnresolvedBase {
		private final int initial;

		public Middle() {
			initial = 17;
		}
	}

	public static class Grandchild extends Middle {
		@Override
		protected int readInitial() {
			return ((Middle) this).initial;
		}
	}

	public void check() {
		Grandchild child = new Grandchild();
		if (child.getObserved() != 0 || ((Middle) child).initial != 17) {
			throw new AssertionError("nested subclass observed changed private field initialization order");
		}
	}
}
