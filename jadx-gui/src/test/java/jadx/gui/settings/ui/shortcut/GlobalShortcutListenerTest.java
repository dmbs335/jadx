package jadx.gui.settings.ui.shortcut;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import jadx.gui.settings.JadxSettings;
import jadx.gui.settings.JadxSettingsData;
import jadx.gui.ui.action.ActionModel;
import jadx.gui.utils.shortcut.ShortcutsController;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalShortcutListenerTest {
	@Test
	void shortcutEditorRemovesItsGlobalMouseListener() throws Exception {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		int before = toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length;
		AtomicReference<ShortcutEdit> editRef = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> editRef.set(new ShortcutEdit(ActionModel.OPEN, null, null)));
		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK)).hasSize(before + 1);

		SwingUtilities.invokeAndWait(editRef.get()::dispose);

		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK)).hasSize(before);
	}

	@Test
	void shortcutControllerRegistrationIsIdempotentAndDisposable() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		int before = toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length;
		ShortcutsController controller = new ShortcutsController(new JadxSettings(null));

		controller.registerMouseEventListener(null);
		controller.registerMouseEventListener(null);
		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK)).hasSize(before + 1);

		controller.dispose();
		controller.dispose();
		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK)).hasSize(before);
	}

	@Test
	void shortcutSettingsGroupDisposesAllEditors() throws Exception {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		int before = toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length;
		JadxSettings settings = new JadxSettings(null);
		settings.loadSettingsData(new JadxSettingsData());
		ShortcutsSettingsGroup group = new ShortcutsSettingsGroup(null, settings);

		SwingUtilities.invokeAndWait(group::getSubGroups);
		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length).isGreaterThan(before + 10);

		SwingUtilities.invokeAndWait(() -> group.close(false));
		assertThat(toolkit.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK)).hasSize(before);
	}
}
