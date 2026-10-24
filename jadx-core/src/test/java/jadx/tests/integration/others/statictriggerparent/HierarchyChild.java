package jadx.tests.integration.others.statictriggerparent;

public class HierarchyChild extends HierarchyParent {
	public static final int initial;

	static {
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (TriggerBase.observed != 0 || value != 3 || initial != 17) {
			throw new AssertionError("triggered superclass initializer observed changed child field value");
		}
	}
}
