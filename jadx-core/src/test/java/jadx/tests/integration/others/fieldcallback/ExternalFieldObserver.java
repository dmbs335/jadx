package jadx.tests.integration.others.fieldcallback;

public class ExternalFieldObserver {
	private static int observed;

	public static void observe(InitialReader reader) {
		observed = reader.readInitial();
	}

	public static int getObserved() {
		return observed;
	}
}
