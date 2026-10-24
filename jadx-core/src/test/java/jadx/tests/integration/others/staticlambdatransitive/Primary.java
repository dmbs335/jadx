package jadx.tests.integration.others.staticlambdatransitive;

import java.util.function.IntSupplier;

public class Primary {
	public static final int initial;
	public static int value;

	static {
		IntSupplier supplier = Helper::load;
		value = supplier.getAsInt();
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (Secondary.observed != 0 || value != 3 || initial != 17) {
			throw new AssertionError("lambda-triggered class initialization order changed");
		}
	}
}
