package jadx.gui.ui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class JadxMenuBar extends JMenuBar {
	public void reloadShortcuts() {
		for (int i = 0; i < getMenuCount(); i++) {
			JMenu menu = getMenu(i);
			if (menu instanceof JadxMenu) {
				((JadxMenu) menu).reloadShortcuts();
			}
		}
	}

	public void dispose() {
		for (int i = 0; i < getMenuCount(); i++) {
			disposeMenu(getMenu(i));
		}
		removeAll();
		setUI(null);
	}

	private static void disposeMenu(JMenu menu) {
		if (menu == null) {
			return;
		}
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			if (item instanceof JMenu) {
				disposeMenu((JMenu) item);
			} else if (item != null) {
				item.setAccelerator(null);
				item.setAction(null);
			}
		}
		menu.removeAll();
		menu.setUI(null);
	}
}
