package jadx.tests.integration.others.subclassfield;

import java.util.function.IntSupplier;

public class SubclassFieldGrandchild extends SubclassFieldMiddle {
	private final int initial;

	public SubclassFieldGrandchild() {
		initial = 11;
	}

	@Override
	int readInitial() {
		IntSupplier reader = () -> initial;
		return reader.getAsInt();
	}

	public void check() {
		if (observed != 0 || initial != 11) {
			throw new AssertionError("grandparent constructor observed subclass field after initialization");
		}
	}
}
