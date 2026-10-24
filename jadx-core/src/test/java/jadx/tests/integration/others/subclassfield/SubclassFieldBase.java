package jadx.tests.integration.others.subclassfield;

public class SubclassFieldBase {
	final int observed;

	public SubclassFieldBase() {
		observed = readInitial();
	}

	int readInitial() {
		return -1;
	}
}
