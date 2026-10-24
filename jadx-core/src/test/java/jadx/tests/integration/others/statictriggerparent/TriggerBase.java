package jadx.tests.integration.others.statictriggerparent;

public class TriggerBase {
	public static int observed;

	static {
		observed = HierarchyChild.readInitial();
	}
}
