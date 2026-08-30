package jadx.gui.ui.menu;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.action.ActionModel;
import jadx.gui.ui.action.JadxGuiAction;
import jadx.gui.utils.shortcut.ShortcutsController;

import static org.assertj.core.api.Assertions.assertThat;

class JadxMenuLifecycleTest {
	@Test
	void menuShortcutDoesNotUseAStaticActionMap() {
		ShortcutsController shortcuts = new ShortcutsController(new JadxSettings(null));
		JadxMenu menu = new JadxMenu("File", shortcuts);
		AtomicInteger calls = new AtomicInteger();
		JadxGuiAction action = new JadxGuiAction(ActionModel.OPEN, calls::incrementAndGet);

		menu.add(action);
		action.setShortcut(ActionModel.OPEN.getDefaultShortcut());

		assertThat(action.getShortcutComponent()).isSameAs(menu);
		assertThat(menu.getActionMap().get(ActionModel.OPEN.name())).isNull();
		assertThat(action.getValue(JadxGuiAction.ACCELERATOR_KEY)).isNotNull();
		action.performAction();
		assertThat(calls).hasValue(1);
	}

	@Test
	void disposeUninstallsMenuUiAndActions() {
		ShortcutsController shortcuts = new ShortcutsController(new JadxSettings(null));
		JadxMenu menu = new JadxMenu("File", shortcuts);
		JMenuItem item = menu.add(new JadxGuiAction(ActionModel.OPEN, () -> {
		}));
		JadxMenuBar menuBar = new JadxMenuBar();
		menuBar.add(menu);

		menuBar.dispose();

		assertThat(menuBar.getMenuCount()).isZero();
		assertThat(menuBar.getUI()).isNull();
		assertThat(menu.getUI()).isNull();
		assertThat(item.getAction()).isNull();
	}
}
