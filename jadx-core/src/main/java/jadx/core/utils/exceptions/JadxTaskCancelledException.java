package jadx.core.utils.exceptions;

/**
 * Cooperative task cancellation signal. This exception must not be converted into a decompilation
 * warning or cached as generated code.
 */
public final class JadxTaskCancelledException extends JadxRuntimeException {
	private static final long serialVersionUID = 1L;

	public JadxTaskCancelledException() {
		super("JADX task cancelled");
	}
}
