package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import jadx.gui.utils.UiUtils;
import jadx.storage.api.SearchResult;
import jadx.storage.impl.SqliteContentStore;

public final class ContentStoreSearchDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTextField storeField = new JTextField(45);
	private final JTextField queryField = new JTextField(30);
	private final JButton searchButton = new JButton("Search");
	private final DefaultTableModel tableModel = new DefaultTableModel(
			new Object[] { "App ID", "Application", "Path", "Object hash", "Match" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	public ContentStoreSearchDialog(Window owner) {
		super(owner, "Content Store Search", ModalityType.MODELESS);
		String configuredStore = System.getProperty("jadx.content.store");
		if (configuredStore != null) {
			storeField.setText(configuredStore);
		}
		initUi();
	}

	private void initUi() {
		JPanel storePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		storePanel.add(new JLabel("Store:"));
		storePanel.add(storeField);
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(event -> chooseStore());
		storePanel.add(browseButton);

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		searchPanel.add(new JLabel("Query:"));
		searchPanel.add(queryField);
		searchButton.addActionListener(event -> search());
		queryField.addActionListener(event -> search());
		searchPanel.add(searchButton);

		JPanel controls = new JPanel(new BorderLayout());
		controls.add(storePanel, BorderLayout.NORTH);
		controls.add(searchPanel, BorderLayout.SOUTH);

		JTable table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(1).setPreferredWidth(160);
		table.getColumnModel().getColumn(2).setPreferredWidth(360);
		table.getColumnModel().getColumn(3).setPreferredWidth(460);
		table.getColumnModel().getColumn(4).setPreferredWidth(500);

		add(controls, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		setPreferredSize(new Dimension(1100, 650));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		UiUtils.setWindowIcons(this);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private void chooseStore() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (!storeField.getText().trim().isEmpty()) {
			chooser.setCurrentDirectory(Path.of(storeField.getText().trim()).toFile());
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			storeField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void search() {
		String storePath = storeField.getText().trim();
		String query = queryField.getText().trim();
		if (storePath.isEmpty() || query.isEmpty()) {
			return;
		}
		searchButton.setEnabled(false);
		new SwingWorker<List<SearchResult>, Void>() {
			@Override
			protected List<SearchResult> doInBackground() throws Exception {
				try (SqliteContentStore store = SqliteContentStore.open(Path.of(storePath))) {
					return store.search(query, 500);
				}
			}

			@Override
			protected void done() {
				searchButton.setEnabled(true);
				try {
					showResults(get());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ContentStoreSearchDialog.this,
							"Content-store search failed: " + rootMessage(e),
							"Search error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	private void showResults(List<SearchResult> results) {
		tableModel.setRowCount(0);
		for (SearchResult result : results) {
			tableModel.addRow(new Object[] {
					result.getApplicationId(), result.getApplicationName(), result.getPath(),
					result.getObjectHash(), result.getSnippet()
			});
		}
		setTitle("Content Store Search - " + results.size() + " result(s)");
	}

	private static String rootMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null) {
			current = current.getCause();
		}
		String message = current.getMessage();
		return message == null ? current.getClass().getSimpleName() : message;
	}
}
