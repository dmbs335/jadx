package jadx.api;

import java.nio.file.Path;

@FunctionalInterface
public interface IOutputFileListener {
	IOutputFileListener NONE = path -> {
	};

	void onFileSaved(Path path) throws Exception;

	default void onFileSaved(Path path, String contentHash, long size) throws Exception {
		onFileSaved(path);
	}

	default boolean useContentMetadata() {
		return false;
	}

	default boolean useWaveCheckpoints() {
		return false;
	}

	default void onOutputCheckpoint() throws Exception {
	}
}
