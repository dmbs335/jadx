package jadx.tests.integration.others.staticindirectread;

public class IndirectParent {
	public static int observed;

	static {
		observed = IndirectTrigger.VALUE;
	}
}
