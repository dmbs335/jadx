package jadx.tests.integration.others.staticinterfaceread;

public class StaticInterfaceChild implements InitObserver {
	public static final int initial;

	static {
		initial = 17;
	}

	public static int readInitial() {
		return initial;
	}

	public void check() {
		if (InitObserver.OBSERVED != 0 || initial != 17) {
			throw new AssertionError("interface initializer observed changed child field value");
		}
	}
}
