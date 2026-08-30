package jadx.tests.integration.others.cfg;

public class CfgTarget {
	private final CfgDependency dependency = new CfgDependency();

	public int call(int value) {
		return dependency.increment(value);
	}
}
