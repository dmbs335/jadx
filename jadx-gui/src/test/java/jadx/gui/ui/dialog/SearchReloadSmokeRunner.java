package jadx.gui.ui.dialog;

import java.awt.EventQueue;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import jadx.gui.cache.code.CodeCacheMode;
import jadx.gui.settings.JadxSettings;
import jadx.gui.settings.JadxSettingsData;
import jadx.gui.ui.MainWindow;

/** End-to-end AWT/decompiler smoke for search cancellation followed by project reload. */
public final class SearchReloadSmokeRunner {
	private static final long LOAD_TIMEOUT_SECONDS = 180;
	private static final long SEARCH_TIMEOUT_MS = 60_000;

	private SearchReloadSmokeRunner() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new IllegalArgumentException("Usage: SearchReloadSmokeRunner <input.apk> <report.json> [rounds]");
		}
		Path input = Paths.get(args[0]).toAbsolutePath().normalize();
		Path report = Paths.get(args[1]).toAbsolutePath().normalize();
		int rounds = args.length > 2 ? Integer.parseInt(args[2]) : 3;
		if (!Files.isRegularFile(input)) throw new IllegalArgumentException("Input not found: " + input);

		Set<Long> baselineThreads = threadIds();
		List<String> uncaught = Collections.synchronizedList(new ArrayList<>());
		Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		MainWindow window = null;
		List<RoundResult> results = new ArrayList<>();
		long initialLoadMs = 0;
		try {
			JadxSettings settings = settings();
			window = onEdt(() -> new MainWindow(settings));
			Thread.setDefaultUncaughtExceptionHandler((thread, error) -> uncaught.add(
					thread.getName() + ": " + error.getClass().getName() + ": " + error.getMessage()));

			long loadStart = System.nanoTime();
			awaitInitialLoad(window, input);
			initialLoadMs = elapsedMs(loadStart);

			for (int round = 1; round <= rounds; round++) {
				results.add(runRound(window, round));
			}
		} finally {
			if (window != null) {
				MainWindow closing = window;
				onEdt(() -> {
					closing.dispose();
					return null;
				});
			}
			Thread.setDefaultUncaughtExceptionHandler(previousHandler);
		}

		List<String> leakedThreads = awaitTaskThreadsGone(baselineThreads, 5_000);
		writeReport(report, input, rounds, initialLoadMs, results, uncaught, leakedThreads);
		System.out.println("SEARCH_RELOAD_SMOKE\trounds=" + rounds
				+ "\tinitialLoadMs=" + initialLoadMs
				+ "\tuncaught=" + uncaught.size()
				+ "\tleakedTaskThreads=" + leakedThreads.size()
				+ "\treport=" + report);
		for (RoundResult result : results) {
			System.out.println("ROUND\t" + result.round
					+ "\tcancelSearchMs=" + result.cancelSearchMs
					+ "\treloadMs=" + result.reloadMs
					+ "\tnewSearchMs=" + result.newSearchMs
					+ "\tnewResults=" + result.newResults);
		}
		if (!uncaught.isEmpty() || !leakedThreads.isEmpty()) {
			throw new IllegalStateException("Search/reload smoke failed; see " + report);
		}
	}

	private static RoundResult runRound(MainWindow window, int round) throws Exception {
		SearchDialog oldDialog = onEdt(() -> SearchDialog.createHiddenForSmoke(
				window, SearchDialog.SearchPreset.TEXT));
		long searchStart = System.nanoTime();
		onEdt(() -> {
			oldDialog.submitForSmoke("if");
			return null;
		});
		if (!oldDialog.awaitSmokeTaskStarted(SEARCH_TIMEOUT_MS)) {
			throw new IllegalStateException("Round " + round + " search did not start");
		}
		Thread.sleep(25);
		onEdt(() -> {
			oldDialog.cancelForSmoke();
			return null;
		});
		if (!oldDialog.awaitSmokeTaskFinished(SEARCH_TIMEOUT_MS)) {
			throw new IllegalStateException("Round " + round + " canceled search did not finish");
		}
		long cancelSearchMs = elapsedMs(searchStart);

		long reloadStart = System.nanoTime();
		awaitReload(window);
		long reloadMs = elapsedMs(reloadStart);

		SearchDialog newDialog = onEdt(() -> SearchDialog.createHiddenForSmoke(
				window, SearchDialog.SearchPreset.CLASS));
		long newSearchStart = System.nanoTime();
		onEdt(() -> {
			newDialog.submitForSmoke("MainActivity");
			return null;
		});
		if (!newDialog.awaitSmokeTaskStarted(SEARCH_TIMEOUT_MS)
				|| !newDialog.awaitSmokeTaskFinished(SEARCH_TIMEOUT_MS)) {
			throw new IllegalStateException("Round " + round + " new-project search did not finish");
		}
		int newResults = onEdt(newDialog::getSmokeResultCount);
		long newSearchMs = elapsedMs(newSearchStart);
		onEdt(() -> {
			newDialog.dispose();
			return null;
		});
		return new RoundResult(round, cancelSearchMs, reloadMs, newSearchMs, newResults);
	}

	private static JadxSettings settings() {
		JadxSettingsData data = new JadxSettingsData();
		data.setCheckForUpdates(false);
		data.setAutoStartJobs(false);
		data.setUseAutoSearch(false);
		data.setSearchResultsPerPage(20);
		data.setThreadsCount(4);
		data.setCodeCacheMode(CodeCacheMode.MEMORY);
		JadxSettings settings = new JadxSettings(null);
		settings.loadSettingsData(data);
		return settings;
	}

	private static void awaitInitialLoad(MainWindow window, Path input) throws Exception {
		CountDownLatch loaded = new CountDownLatch(1);
		onEdt(() -> {
			window.addLoadListener(state -> {
				if (state) loaded.countDown();
				return state;
			});
			window.open(input);
			return null;
		});
		if (!loaded.await(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			throw new IllegalStateException("Initial project load timed out");
		}
	}

	private static void awaitReload(MainWindow window) throws Exception {
		CountDownLatch reloaded = new CountDownLatch(1);
		boolean[] sawUnload = {false};
		onEdt(() -> {
			window.addLoadListener(state -> {
				if (!state) {
					sawUnload[0] = true;
				} else if (sawUnload[0]) {
					reloaded.countDown();
					return true;
				}
				return false;
			});
			window.reopen();
			return null;
		});
		if (!reloaded.await(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			throw new IllegalStateException("Project reload timed out");
		}
	}

	private static <T> T onEdt(Callable<T> callable) throws Exception {
		if (EventQueue.isDispatchThread()) return callable.call();
		FutureTask<T> task = new FutureTask<>(callable);
		EventQueue.invokeAndWait(task);
		return task.get();
	}

	private static long elapsedMs(long start) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
	}

	private static Set<Long> threadIds() {
		Set<Long> result = new LinkedHashSet<>();
		for (Thread thread : Thread.getAllStackTraces().keySet()) result.add(thread.getId());
		return result;
	}

	private static List<String> awaitTaskThreadsGone(Set<Long> baseline, long timeoutMs)
			throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
		List<String> leaked;
		do {
			leaked = taskThreads(baseline);
			if (leaked.isEmpty()) return leaked;
			Thread.sleep(50);
		} while (System.nanoTime() < deadline);
		return leaked;
	}

	private static List<String> taskThreads(Set<Long> baseline) {
		List<String> result = new ArrayList<>();
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			String name = thread.getName();
			if (thread.isAlive() && !baseline.contains(thread.getId())
					&& (name.equals("bg") || name.startsWith("bg-")
							|| name.equals("search") || name.startsWith("search-"))) {
				result.add(name + "[" + thread.getState() + "]");
			}
		}
		Collections.sort(result);
		return result;
	}

	private static void writeReport(Path output, Path input, int rounds, long initialLoadMs,
			List<RoundResult> results, List<String> uncaught, List<String> leaked) throws Exception {
		if (output.getParent() != null) Files.createDirectories(output.getParent());
		StringBuilder json = new StringBuilder("{\n  \"schema\": \"rel-1d-search-reload-smoke-v1\",")
				.append("\n  \"input\": ").append(q(input.toString())).append(',')
				.append("\n  \"rounds\": ").append(rounds).append(',')
				.append("\n  \"initialLoadMs\": ").append(initialLoadMs).append(',')
				.append("\n  \"uncaughtExceptions\": ").append(strings(uncaught)).append(',')
				.append("\n  \"leakedTaskThreads\": ").append(strings(leaked)).append(',')
				.append("\n  \"results\": [");
		for (int index = 0; index < results.size(); index++) {
			if (index > 0) json.append(',');
			RoundResult result = results.get(index);
			json.append("\n    {\"round\": ").append(result.round)
					.append(", \"cancelSearchMs\": ").append(result.cancelSearchMs)
					.append(", \"reloadMs\": ").append(result.reloadMs)
					.append(", \"newSearchMs\": ").append(result.newSearchMs)
					.append(", \"newResults\": ").append(result.newResults).append('}');
		}
		json.append("\n  ]\n}\n");
		Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
	}

	private static String strings(List<String> values) {
		StringBuilder json = new StringBuilder("[");
		for (int index = 0; index < values.size(); index++) {
			if (index > 0) json.append(',');
			json.append(q(values.get(index)));
		}
		return json.append(']').toString();
	}

	private static String q(String value) {
		return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"")
				.replace("\n", "\\n").replace("\r", "\\r") + '"';
	}

	private static final class RoundResult {
		private final int round;
		private final long cancelSearchMs;
		private final long reloadMs;
		private final long newSearchMs;
		private final int newResults;

		private RoundResult(int round, long cancelSearchMs, long reloadMs,
				long newSearchMs, int newResults) {
			this.round = round;
			this.cancelSearchMs = cancelSearchMs;
			this.reloadMs = reloadMs;
			this.newSearchMs = newSearchMs;
			this.newResults = newResults;
		}
	}
}
