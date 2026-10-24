package jadx.tests.integration.others.staticreentrant;

public class Secondary {
	public static int observed;
	public static int value;

	static {
		observed = Primary.readInitial();
		value = 3;
	}
}
