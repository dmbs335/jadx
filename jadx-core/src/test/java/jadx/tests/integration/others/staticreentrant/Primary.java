package jadx.tests.integration.others.staticreentrant;

public class Primary {
	public static final int initial;
	public static int value;

	static {
		value = Secondary.value;
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (Secondary.observed != 0 || value != 3 || initial != 17) {
			throw new AssertionError("class initialization order changed");
		}
	}
}
