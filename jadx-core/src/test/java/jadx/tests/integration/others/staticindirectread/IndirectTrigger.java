package jadx.tests.integration.others.staticindirectread;

public class IndirectTrigger {
	public static final int VALUE;

	static {
		VALUE = IndirectChild.readInitial();
	}
}
