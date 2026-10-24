package jadx.tests.integration.others.statictriggerparent;

public class HierarchyParent {
	public static int value;

	static {
		value = TriggerBranch.value;
	}
}
