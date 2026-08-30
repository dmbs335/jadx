package jadx.gui.jobs;

import java.io.File;

import javax.swing.JOptionPane;

import jadx.api.ICodeCache;
import jadx.api.JadxArgs;
import jadx.api.utils.tasks.ITaskExecutor;
import jadx.core.export.ExportGradleType;
import jadx.gui.JadxWrapper;
import jadx.gui.cache.code.CodeCacheMode;
import jadx.gui.cache.code.FixedCodeCache;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.export.ExportProjectProperties;
import jadx.gui.utils.NLS;

public class ExportTask extends CancelableBackgroundTask {

	private final MainWindow mainWindow;
	private final JadxWrapper wrapper;
	private final File saveDir;
	private final boolean skipSources;
	private final boolean skipResources;
	private final ExportGradleType exportGradleType;

	private int timeLimit;
	private ICodeCache uiCodeCache;
	private ExportArgsState initialArgs;
	private boolean argsRestored;

	public ExportTask(MainWindow mainWindow, JadxWrapper wrapper, ExportProjectProperties properties) {
		this.mainWindow = mainWindow;
		this.wrapper = wrapper;
		this.saveDir = new File(properties.getExportPath());
		this.exportGradleType = properties.isAsGradleMode() ? properties.getExportGradleType() : null;
		this.skipSources = !properties.isAsGradleMode() && properties.isSkipSources();
		this.skipResources = !properties.isAsGradleMode() && properties.isSkipResources();
	}

	@Override
	public String getTitle() {
		return NLS.str("msg.saving_sources");
	}

	@Override
	public ITaskExecutor scheduleTasks() {
		JadxArgs args = wrapper.getArgs();
		initialArgs = ExportArgsState.capture(args);
		try {
			wrapCodeCache();
			args.setRootDir(saveDir);
			args.setExportGradleType(exportGradleType);
			args.setSkipSources(skipSources);
			args.setSkipResources(skipResources);
			ITaskExecutor saveTasks = wrapper.getDecompiler().getSaveTaskExecutor();
			this.timeLimit = DecompileTask.calcDecompileTimeLimit(saveTasks.getTasksCount());
			return saveTasks;
		} catch (RuntimeException | Error e) {
			restoreArgs();
			throw e;
		}
	}

	private void wrapCodeCache() {
		uiCodeCache = wrapper.getArgs().getCodeCache();
		if (mainWindow.getSettings().getCodeCacheMode() != CodeCacheMode.DISK) {
			// do not save newly decompiled code in cache to not increase memory usage
			// TODO: maybe make memory limited cache?
			wrapper.getArgs().setCodeCache(new FixedCodeCache(uiCodeCache));
		}
	}

	@Override
	public void onDone(ITaskInfo taskInfo) {
		restoreArgs();
	}

	private synchronized void restoreArgs() {
		if (argsRestored || initialArgs == null) {
			return;
		}
		JadxArgs args = wrapper.getArgs();
		initialArgs.restore(args);
		if (uiCodeCache != null) {
			args.setCodeCache(uiCodeCache);
		}
		argsRestored = true;
	}

	@Override
	public void onFinish(ITaskInfo taskInfo) {
		if (taskInfo.getJobsSkipped() == 0) {
			return;
		}
		String reason = getIncompleteReason(taskInfo.getStatus());
		if (reason != null) {
			JOptionPane.showMessageDialog(mainWindow,
					NLS.str("message.saveIncomplete", reason, taskInfo.getJobsSkipped()),
					NLS.str("message.errorTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private String getIncompleteReason(TaskStatus status) {
		switch (status) {
			case CANCEL_BY_USER:
				return NLS.str("message.userCancelTask");

			case CANCEL_BY_TIMEOUT:
				return NLS.str("message.taskTimeout", timeLimit());

			case CANCEL_BY_MEMORY:
				mainWindow.showHeapUsageBar();
				return NLS.str("message.memoryLow");

			case ERROR:
				return NLS.str("message.taskError");
		}
		return null;
	}

	@Override
	public int timeLimit() {
		return timeLimit;
	}

	@Override
	public boolean canBeCanceled() {
		return true;
	}

	@Override
	public boolean checkMemoryUsage() {
		return true;
	}

	static final class ExportArgsState {
		private final File outDir;
		private final File outDirSrc;
		private final File outDirRes;
		private final boolean skipSources;
		private final boolean skipResources;
		private final ExportGradleType exportGradleType;

		private ExportArgsState(JadxArgs args) {
			this.outDir = args.getOutDir();
			this.outDirSrc = args.getOutDirSrc();
			this.outDirRes = args.getOutDirRes();
			this.skipSources = args.isSkipSources();
			this.skipResources = args.isSkipResources();
			this.exportGradleType = args.getExportGradleType();
		}

		static ExportArgsState capture(JadxArgs args) {
			return new ExportArgsState(args);
		}

		void restore(JadxArgs args) {
			args.setOutDir(outDir);
			args.setOutDirSrc(outDirSrc);
			args.setOutDirRes(outDirRes);
			args.setSkipSources(skipSources);
			args.setSkipResources(skipResources);
			args.setExportGradleType(exportGradleType);
		}
	}
}
