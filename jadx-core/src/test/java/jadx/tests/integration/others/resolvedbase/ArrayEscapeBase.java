package jadx.tests.integration.others.resolvedbase;

public class ArrayEscapeBase {
	public ArrayEscapeBase() {
		forward(this);
	}

	private static void forward(ArrayEscapeBase receiver) {
		Object[] slots = new Object[1];
		slots[0] = receiver;
		((ArrayEscapeBase) slots[0]).callback();
	}

	protected void callback() {
	}
}
