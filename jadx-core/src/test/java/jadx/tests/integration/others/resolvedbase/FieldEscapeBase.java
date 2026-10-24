package jadx.tests.integration.others.resolvedbase;

public class FieldEscapeBase {
	public FieldEscapeBase() {
		forward(this);
	}

	private static void forward(FieldEscapeBase receiver) {
		Holder holder = new Holder();
		holder.value = receiver;
		holder.value.callback();
	}

	protected void callback() {
	}

	private static final class Holder {
		private FieldEscapeBase value;
	}
}
