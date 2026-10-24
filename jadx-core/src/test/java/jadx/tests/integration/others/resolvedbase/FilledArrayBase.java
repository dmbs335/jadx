package jadx.tests.integration.others.resolvedbase;

public class FilledArrayBase {
	public FilledArrayBase() {
		forward(this);
	}

	private static void forward(FilledArrayBase receiver) {
		Object[] slots = { receiver };
		((FilledArrayBase) slots[0]).callback();
	}

	protected void callback() {
	}
}
