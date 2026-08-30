package jadx.gui.ui.panel;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jadx.gui.logs.IssuesListener;
import jadx.gui.logs.LogCollector;

import static org.assertj.core.api.Assertions.assertThat;

class IssuesPanelLifecycleTest {
	@Test
	void disposeRemovesLogListener() throws Exception {
		IssuesPanel panel = new IssuesPanel(null);
		IssuesListener listener = getIssuesListener(panel);
		LogCollector collector = LogCollector.getInstance();
		assertThat(collector.removeListener(listener)).isTrue();
		collector.registerListener(listener);

		panel.dispose();
		panel.dispose();

		assertThat(collector.removeListener(listener)).isFalse();
	}

	private static IssuesListener getIssuesListener(IssuesPanel panel) throws Exception {
		Field field = IssuesPanel.class.getDeclaredField("issuesListener");
		field.setAccessible(true);
		return (IssuesListener) field.get(panel);
	}
}
