package jadx.tests.integration.others.fieldcallback;

public class CallbackChild implements InitialReader {
	private final int initial;

	public CallbackChild() {
		ExternalFieldObserver.observe(this);
		initial = 17;
	}

	@Override
	public int readInitial() {
		return initial;
	}

	public void check() {
		if (ExternalFieldObserver.getObserved() != 0 || initial != 17) {
			throw new AssertionError("external callback observed changed field initialization order");
		}
	}
}
