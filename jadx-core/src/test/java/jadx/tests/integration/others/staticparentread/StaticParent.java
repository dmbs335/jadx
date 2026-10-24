package jadx.tests.integration.others.staticparentread;

public class StaticParent {
	public static int observed;

	static {
		observed = StaticChild.readInitial();
	}
}
