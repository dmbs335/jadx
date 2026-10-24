package jadx.tests.integration.others.unresolvedbase;

public class UnresolvedBase {
	protected final int observed;

	public UnresolvedBase() {
		observed = readInitial();
	}

	protected int readInitial() {
		return -1;
	}
}
