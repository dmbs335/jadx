package jadx.tests.integration.others.resolvedbase;

public class ReturnEscapeBase {
	public ReturnEscapeBase() {
		identity(this).callback();
	}

	private static ReturnEscapeBase identity(ReturnEscapeBase receiver) {
		return receiver;
	}

	protected void callback() {
	}
}
