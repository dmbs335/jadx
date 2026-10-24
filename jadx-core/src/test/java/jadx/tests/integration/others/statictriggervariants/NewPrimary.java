package jadx.tests.integration.others.statictriggervariants;

public class NewPrimary {
	public static final int initial;
	public static int value;

	static {
		value = new NewSecondary().value;
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (NewSecondary.observed != 0 || value != 3 || initial != 17) {
			throw new AssertionError("object creation initialization order changed");
		}
	}
}
