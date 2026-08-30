package jadx.gui.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import jadx.api.utils.tasks.ITaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class LoadTaskTest {
	@Test
	void skippedSupplierDoesNotDeliverEmptyResult() {
		AtomicBoolean consumerCalled = new AtomicBoolean();
		LoadTask<Object> task = new LoadTask<>(Object::new, value -> consumerCalled.set(true));
		InternalTask taskInfo = new InternalTask(1, task);
		taskInfo.setStatus(TaskStatus.COMPLETE);

		task.onFinish(taskInfo);

		assertThat(consumerCalled).isFalse();
	}

	@Test
	void completedSupplierCanDeliverNullResult() {
		AtomicBoolean consumerCalled = new AtomicBoolean();
		LoadTask<Object> task = new LoadTask<>(() -> null, value -> consumerCalled.set(true));
		ITaskExecutor executor = task.scheduleTasks();
		executor.setThreadsCount(1);
		executor.execute();
		executor.awaitTermination();
		InternalTask taskInfo = new InternalTask(1, task);
		taskInfo.setStatus(TaskStatus.COMPLETE);

		task.onFinish(taskInfo);

		assertThat(consumerCalled).isTrue();
	}
}
