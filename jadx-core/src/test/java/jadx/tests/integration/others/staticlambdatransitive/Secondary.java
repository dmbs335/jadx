package jadx.tests.integration.others.staticlambdatransitive;

public class Secondary {
	public static int observed;
	public static int value;

	static {
		observed = Primary.readInitial();
		value = 3;
	}
}
