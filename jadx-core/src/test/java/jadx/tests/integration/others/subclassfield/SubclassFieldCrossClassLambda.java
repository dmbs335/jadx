package jadx.tests.integration.others.subclassfield;

public class SubclassFieldCrossClassLambda extends SubclassFieldMiddle {
	final int initial;

	public SubclassFieldCrossClassLambda() {
		initial = 13;
	}

	@Override
	int readInitial() {
		return SubclassFieldLambdaReader.read(this);
	}

	public void check() {
		if (observed != 0 || initial != 13) {
			throw new AssertionError("cross-class lambda observed field after initialization");
		}
	}
}
