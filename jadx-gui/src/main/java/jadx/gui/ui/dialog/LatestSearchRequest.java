package jadx.gui.ui.dialog;

import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

/**
 * A single-slot latest-wins handoff for search requests.
 */
final class LatestSearchRequest<T> {
	private final AtomicReference<T> latest = new AtomicReference<>();

	void offer(T request) {
		latest.set(request);
	}

	@Nullable
	T peek() {
		return latest.get();
	}

	boolean claim(T request) {
		return latest.compareAndSet(request, null);
	}

	boolean hasPending() {
		return latest.get() != null;
	}
}
