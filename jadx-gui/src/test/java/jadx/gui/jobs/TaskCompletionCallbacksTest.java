package jadx.gui.jobs;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskCompletionCallbacksTest {
	@Test
	void loadTaskDoesNotPublishMissingDataAfterCancellation() {
		AtomicBoolean uiTaskCalled = new AtomicBoolean();
		LoadTask<String> task = new LoadTask<>(() -> "data", data -> uiTaskCalled.set(true));

		task.onFinish(taskInfo(TaskStatus.CANCEL_BY_USER));

		assertThat(uiTaskCalled).isFalse();
	}

	@Test
	void runnableDecoratorRunsOnlyAfterSuccessfulTask() {
		AtomicBoolean extraCalled = new AtomicBoolean();
		TaskWithExtraOnFinish task = new TaskWithExtraOnFinish(
				new SimpleTask("test", () -> {
				}),
				() -> extraCalled.set(true));

		task.onFinish(taskInfo(TaskStatus.ERROR));
		assertThat(extraCalled).isFalse();

		task.onFinish(taskInfo(TaskStatus.COMPLETE));
		assertThat(extraCalled).isTrue();
	}

	@Test
	void statusDecoratorStillReceivesFailureForCleanup() {
		AtomicReference<TaskStatus> receivedStatus = new AtomicReference<>();
		TaskWithExtraOnFinish task = new TaskWithExtraOnFinish(
				new SimpleTask("test", () -> {
				}),
				receivedStatus::set);

		task.onFinish(taskInfo(TaskStatus.CANCEL_BY_MEMORY));

		assertThat(receivedStatus).hasValue(TaskStatus.CANCEL_BY_MEMORY);
	}

	private static ITaskInfo taskInfo(TaskStatus status) {
		return new ITaskInfo() {
			@Override
			public TaskStatus getStatus() {
				return status;
			}

			@Override
			public long getJobsCount() {
				return 0;
			}

			@Override
			public long getJobsComplete() {
				return 0;
			}

			@Override
			public long getJobsSkipped() {
				return 0;
			}

			@Override
			public long getTime() {
				return 0;
			}
		};
	}
}
