package jadx.tests.integration.others.staticparentread;

public class StaticChild extends StaticParent {
	public static final int initial;

	static {
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (observed != 0 || initial != 17) {
			throw new AssertionError("parent class initializer observed changed child field value");
		}
	}
}
