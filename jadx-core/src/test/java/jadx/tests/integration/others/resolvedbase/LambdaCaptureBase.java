package jadx.tests.integration.others.resolvedbase;

public class LambdaCaptureBase {
	public LambdaCaptureBase() {
		Runnable callback = this::callback;
		callback.run();
	}

	protected void callback() {
	}
}
