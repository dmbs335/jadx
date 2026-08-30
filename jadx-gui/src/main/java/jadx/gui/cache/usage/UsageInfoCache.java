package jadx.gui.cache.usage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.usage.IUsageInfoCache;
import jadx.api.usage.IUsageInfoData;
import jadx.api.usage.impl.InMemoryUsageInfoCache;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.Utils;

public class UsageInfoCache implements IUsageInfoCache {

	private static final Object LOAD_DATA_SYNC = new Object();

	private final Path usageFile;
	private final List<File> inputs;
	private final InMemoryUsageInfoCache memCache = new InMemoryUsageInfoCache();
	private @Nullable PendingWrite pendingWrite;
	private @Nullable Thread writeThread;
	private boolean closed;

	public UsageInfoCache(Path cacheDir, List<File> inputFiles) {
		usageFile = cacheDir.resolve("usage");
		inputs = inputFiles;
	}

	@Override
	public @Nullable IUsageInfoData get(RootNode root) {
		IUsageInfoData memData = memCache.get(root);
		if (memData != null) {
			return memData;
		}
		synchronized (LOAD_DATA_SYNC) {
			IUsageInfoData cachedData = memCache.get(root);
			if (cachedData != null) {
				return cachedData;
			}
			RawUsageData rawUsageData = UsageFileAdapter.load(root, usageFile, inputs);
			if (rawUsageData != null) {
				UsageData data = new UsageData(root, rawUsageData);
				memCache.set(root, data);
				return data;
			}
		}
		return null;
	}

	@Override
	public synchronized void set(RootNode root, IUsageInfoData data) {
		memCache.set(root, data);
		pendingWrite = new PendingWrite(root, data);
	}

	/**
	 * Start disk persistence only after all prepare passes have completed. The
	 * generated usage graph is already installed in memory, so serialization is
	 * not on the critical path for opening the project.
	 */
	public synchronized void persistAsync() {
		if (closed || pendingWrite == null || writeThread != null) {
			return;
		}
		PendingWrite write = pendingWrite;
		pendingWrite = null;
		Thread thread = Utils.simpleThreadFactory("usage-cache-write").newThread(
				() -> UsageFileAdapter.save(write.root, write.data, usageFile, inputs));
		// Keep persistence below foreground work, but don't use MIN_PRIORITY: on a busy
		// desktop Windows can starve that thread for minutes while close/reload waits for it.
		thread.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.NORM_PRIORITY - 1));
		writeThread = thread;
		thread.start();
	}

	@Override
	public void close() {
		Thread thread;
		synchronized (this) {
			closed = true;
			pendingWrite = null;
			thread = writeThread;
		}
		joinWriter(thread);
		memCache.close();
	}

	private static void joinWriter(@Nullable Thread thread) {
		if (thread == null || thread == Thread.currentThread()) {
			return;
		}
		boolean interrupted = false;
		while (thread.isAlive()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	private static final class PendingWrite {
		private final RootNode root;
		private final IUsageInfoData data;

		private PendingWrite(RootNode root, IUsageInfoData data) {
			this.root = root;
			this.data = data;
		}
	}
}
