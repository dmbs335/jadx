package jadx.gui.search;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import jadx.gui.jobs.Cancelable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegexSearchMethodTest {
	@Test
	void preservesJavaRegexSemantics() {
		ISearchMethod method = RegexSearchMethod.build(Pattern.compile("(?<=foo)bar"));

		assertThat(method.find("xxfoobarxx", "", 0)).isEqualTo(5);
	}

	@Test
	void canceledMatchStopsBeforeEnteringRegexEngine() {
		RegexSearchMethod method = new RegexSearchMethod(Pattern.compile(".*"));
		Cancelable canceled = new Cancelable() {
			@Override
			public boolean isCanceled() {
				return true;
			}

			@Override
			public void cancel() {
			}
		};

		assertThat(method.find("content", "", 0, canceled)).isEqualTo(-1);
	}

	@Test
	@Timeout(2)
	void cancellationIsObservedDuringBacktracking() {
		RegexSearchMethod method = new RegexSearchMethod(Pattern.compile("^(a+)+$"));
		AtomicInteger checks = new AtomicInteger();
		Cancelable canceledDuringMatch = new Cancelable() {
			@Override
			public boolean isCanceled() {
				return checks.incrementAndGet() > 1;
			}

			@Override
			public void cancel() {
			}
		};

		assertThat(method.find("a".repeat(20_000) + '!', "", 0, canceledDuringMatch)).isEqualTo(-1);
		assertThat(checks).hasValueGreaterThan(1);
	}

	@Test
	@Timeout(2)
	void catastrophicBacktrackingIsBoundedWithoutLeakingWorkerThread() {
		RegexSearchMethod method = new RegexSearchMethod(
				Pattern.compile("^(a+)+$"), Duration.ofMillis(5));
		String input = "a".repeat(20_000) + '!';

		assertThatThrownBy(() -> method.find(input, "", 0))
				.isInstanceOf(RegexSearchTimeoutException.class);
	}
}
