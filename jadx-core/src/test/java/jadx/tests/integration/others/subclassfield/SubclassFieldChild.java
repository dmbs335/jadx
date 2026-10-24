package jadx.tests.integration.others.subclassfield;

public class SubclassFieldChild extends SubclassFieldBase {
	private final int initial;

	public SubclassFieldChild() {
		initial = 9;
	}

	@Override
	int readInitial() {
		return initial;
	}

	public void check() {
		if (observed != 0 || initial != 9) {
			throw new AssertionError("super constructor observed subclass field after initialization");
		}
	}
}
