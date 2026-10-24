package jadx.tests.integration.others.staticindirectread;

public class IndirectChild extends IndirectParent {
	public static final int initial;

	static {
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (observed != 0 || initial != 17) {
			throw new AssertionError("indirect parent initializer observed changed child field value");
		}
	}
}
