package kotlinx.coroutines;

public interface Job {
	boolean isCancelled();

	boolean isCompleted();
}
