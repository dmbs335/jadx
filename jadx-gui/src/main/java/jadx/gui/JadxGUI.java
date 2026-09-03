package jadx.gui;

import java.awt.Desktop;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.cli.JadxCLIArgs;
import jadx.cli.config.JadxConfigAdapter;
import jadx.commons.app.JadxSystemInfo;
import jadx.core.Jadx;
import jadx.core.utils.JadxBuildInfo;
import jadx.core.utils.files.FileUtils;
import jadx.gui.logs.LogCollector;
import jadx.gui.settings.GuiConfigLocale;
import jadx.gui.settings.JadxSettings;
import jadx.gui.settings.JadxSettingsData;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.LafManager;

public class JadxGUI {
	private static final Logger LOG = LoggerFactory.getLogger(JadxGUI.class);
	private static final String STARTUP_ERROR_LOG = "jadx-gui-startup-error.log";

	public static void main(String[] args) {
		try {
			GuiConfigLocale.load();
			JadxConfigAdapter<JadxSettingsData> configAdapter = JadxSettings.buildConfigAdapter();
			JadxSettingsData settingsData = JadxCLIArgs.processArgs(args, new JadxSettingsData(), configAdapter);
			if (settingsData == null) {
				return;
			}
			JadxSettings settings = new JadxSettings(configAdapter);
			settings.loadSettingsData(settingsData);
			GuiConfigLocale.checkConfig(settingsData);

			LogCollector.register();
			printSystemInfo();
			SwingUtilities.invokeLater(() -> initMainWindow(settings));
		} catch (Throwable e) {
			reportStartupFailure(e);
			System.exit(1);
		}
	}

	private static void initMainWindow(JadxSettings settings) {
		try {
			LafManager.init(settings);
			settings.getFontSettings().updateDefaultFont();
			MainWindow mw = new MainWindow(settings);
			registerOpenFileHandler(mw);
			mw.init();
		} catch (Throwable e) {
			reportStartupFailure(e);
			System.exit(1);
		}
	}

	private static void reportStartupFailure(Throwable error) {
		LOG.error("Failed to start jadx-gui", error);
		Path logPath = Path.of(System.getProperty("java.io.tmpdir"), STARTUP_ERROR_LOG);
		try {
			StringWriter stackTrace = new StringWriter();
			error.printStackTrace(new PrintWriter(stackTrace));
			Files.writeString(
					logPath,
					stackTrace.toString(),
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Throwable writeError) {
			LOG.error("Failed to write startup error log", writeError);
		}
		try {
			JOptionPane.showMessageDialog(
					null,
					"jadx-gui could not start.\n\n" + error + "\n\nDiagnostic log: " + logPath,
					"jadx startup error",
					JOptionPane.ERROR_MESSAGE);
		} catch (Throwable dialogError) {
			LOG.error("Failed to show startup error dialog", dialogError);
		}
	}

	private static void registerOpenFileHandler(MainWindow mw) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
					desktop.setOpenFileHandler(e -> mw.open(FileUtils.toPaths(e.getFiles())));
				}
			}
		} catch (Throwable e) {
			LOG.error("Failed to register open file handler", e);
		}
	}

	private static void printSystemInfo() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Starting jadx-gui: version: {}, bundle: {}. JVM: {} {}. OS: {}, version: {}, arch: {}",
					Jadx.getVersion(), JadxBuildInfo.getJadxBundleType(),
					JadxSystemInfo.JAVA_VM, JadxSystemInfo.JAVA_VER,
					JadxSystemInfo.OS_NAME, JadxSystemInfo.OS_VERSION, JadxSystemInfo.OS_ARCH);
		}
	}
}
