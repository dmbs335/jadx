package jadx.tests.integration.others.statictriggervariants;

public class WriteSecondary {
	public static int observed;
	public static int value;

	static {
		observed = WritePrimary.readInitial();
	}
}
