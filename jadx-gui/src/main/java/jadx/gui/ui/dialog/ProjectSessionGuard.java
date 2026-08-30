package jadx.gui.ui.dialog;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/** Lifecycle token for asynchronous UI work owned by one loaded project generation. */
final class ProjectSessionGuard {
	private final LongSupplier currentGeneration;
	private final long ownerGeneration;
	private final AtomicBoolean closed = new AtomicBoolean();

	ProjectSessionGuard(LongSupplier currentGeneration) {
		this.currentGeneration = currentGeneration;
		this.ownerGeneration = currentGeneration.getAsLong();
	}

	boolean isActive() {
		return !closed.get() && ownsCurrentGeneration();
	}

	boolean ownsCurrentGeneration() {
		return currentGeneration.getAsLong() == ownerGeneration;
	}

	boolean close() {
		return closed.compareAndSet(false, true);
	}

	boolean runIfActive(Runnable callback) {
		if (!isActive()) {
			return false;
		}
		callback.run();
		return true;
	}
}
