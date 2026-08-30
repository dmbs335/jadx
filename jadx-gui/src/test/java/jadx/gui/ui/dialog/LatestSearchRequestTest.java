package jadx.gui.ui.dialog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LatestSearchRequestTest {

	@Test
	void keepsOnlyLatestRequestWhileWorkerIsFinishing() {
		LatestSearchRequest<Object> pending = new LatestSearchRequest<>();
		Object first = new Object();
		pending.offer(first);
		Object latest = null;
		for (int i = 0; i < 10_000; i++) {
			latest = new Object();
			pending.offer(latest);
		}

		assertThat(pending.peek()).isSameAs(latest);
		assertThat(pending.claim(first)).isFalse();
		assertThat(pending.claim(latest)).isTrue();
		assertThat(pending.hasPending()).isFalse();
	}

	@Test
	void failedClaimDoesNotDropNewerRequest() {
		LatestSearchRequest<Object> pending = new LatestSearchRequest<>();
		Object stale = new Object();
		Object latest = new Object();
		pending.offer(latest);

		assertThat(pending.claim(stale)).isFalse();
		assertThat(pending.peek()).isSameAs(latest);
	}
}
