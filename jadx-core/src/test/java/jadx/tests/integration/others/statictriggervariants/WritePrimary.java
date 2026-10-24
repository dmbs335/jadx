package jadx.tests.integration.others.statictriggervariants;

public class WritePrimary {
	public static final int initial;

	static {
		WriteSecondary.value = 3;
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (WriteSecondary.observed != 0 || WriteSecondary.value != 3 || initial != 17) {
			throw new AssertionError("static field write initialization order changed");
		}
	}
}
