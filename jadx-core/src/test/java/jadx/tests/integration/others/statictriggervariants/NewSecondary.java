package jadx.tests.integration.others.statictriggervariants;

public class NewSecondary {
	public static int observed;
	public int value = 3;

	static {
		observed = NewPrimary.readInitial();
	}
}
