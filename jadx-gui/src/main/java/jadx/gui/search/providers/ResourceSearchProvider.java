package jadx.gui.search.providers;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.api.plugins.utils.CommonFileUtils;
import jadx.api.resources.ResourceContentType;
import jadx.api.utils.CodeUtils;
import jadx.gui.jobs.Cancelable;
import jadx.gui.search.ISearchProvider;
import jadx.gui.search.SearchSettings;
import jadx.gui.treemodel.JNode;
import jadx.gui.treemodel.JResSearchNode;
import jadx.gui.treemodel.JResource;
import jadx.gui.treemodel.JRoot;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.dialog.SearchDialog;
import jadx.gui.utils.NLS;

public class ResourceSearchProvider implements ISearchProvider {
	private static final Logger LOG = LoggerFactory.getLogger(ResourceSearchProvider.class);

	private final SearchSettings searchSettings;
	private final SearchDialog searchDialog;
	private final ResourceFilter resourceFilter;
	private final long sizeLimit;

	/**
	 * Resources queue for process. Using UI nodes to reuse loading cache
	 */
	private final Deque<JResource> resQueue;
	private int pos;
	private @Nullable PreparedResource currentResource;

	private int loadErrors = 0;
	private int skipBySize = 0;

	public ResourceSearchProvider(MainWindow mw, SearchSettings searchSettings, SearchDialog searchDialog) {
		this.searchSettings = searchSettings;
		this.resourceFilter = searchSettings.getResourceFilter();
		this.sizeLimit = sizeLimitBytes(searchSettings.getResSizeLimit());
		this.searchDialog = searchDialog;
		JResource activeResource = searchSettings.getActiveResource();
		if (activeResource != null) {
			this.resQueue = new ArrayDeque<>(Collections.singleton(activeResource));
		} else {
			this.resQueue = initResQueue(mw);
		}
	}

	@Override
	public @Nullable JNode next(Cancelable cancelable) {
		while (true) {
			if (cancelable.isCanceled()) {
				return null;
			}
			PreparedResource resource = currentResource;
			if (resource == null) {
				resource = getNextResFile(cancelable);
				if (resource == null) {
					return null;
				}
				currentResource = resource;
			}
			JNode newResult = search(resource, cancelable);
			if (newResult != null) {
				return newResult;
			}
			pos = 0;
			resQueue.removeLast();
			addChildren(resource.node);
			currentResource = null;
			if (resQueue.isEmpty()) {
				return null;
			}
		}
	}

	private JNode search(PreparedResource resource, Cancelable cancelable) {
		if (cancelable.isCanceled()) {
			return null;
		}
		JResource resNode = resource.node;
		String content = resource.content;
		if (SearchPosition.exhausted(content.length(), pos)) {
			return null;
		}
		String searchString = searchSettings.getSearchString();
		int newPos = searchSettings.getSearchMethod().find(content, searchString, pos, cancelable);
		if (newPos == -1 || cancelable.isCanceled()) {
			return null;
		}
		if (resNode.getContentType() == ResourceContentType.CONTENT_TEXT) {
			int lineStart = 1 + CodeUtils.getNewLinePosBefore(content, newPos);
			int lineEnd = CodeUtils.getNewLinePosAfter(content, newPos);
			int end = lineEnd == -1 ? content.length() : lineEnd;
			String line = content.substring(lineStart, end);
			this.pos = SearchPosition.afterMatch(content.length(), newPos, end);
			return new JResSearchNode(resNode, line.trim(), newPos);
		} else {
			int start = Math.max(0, newPos - 30);
			int end = Math.min(newPos + 50, content.length());
			String line = content.substring(start, end);
			this.pos = SearchPosition.afterMatch(
					content.length(), newPos, newPos + searchString.length() + 1);
			return new JResSearchNode(resNode, line, newPos);
		}
	}

	private @Nullable PreparedResource getNextResFile(Cancelable cancelable) {
		while (true) {
			JResource node = resQueue.peekLast();
			if (node == null || cancelable.isCanceled()) {
				return null;
			}
			if (node.getType() == JResource.JResType.FILE) {
				try {
					if (isAllowedFileType(node) && loadResNode(node)) {
						if (cancelable.isCanceled()) {
							return null;
						}
						String content = node.getCodeInfo().getCodeStr();
						if (isAllowedFileSize(node, content)) {
							return new PreparedResource(node, content);
						}
					}
				} catch (RuntimeException e) {
					LOG.warn("Skip invalidated resource during search: {}", node, e);
					loadErrors++;
					updateProgressInfo();
				}
				resQueue.removeLast();
			} else {
				// dir
				resQueue.removeLast();
				if (loadResNode(node)) {
					addChildren(node);
				}
			}
		}
	}

	private void updateProgressInfo() {
		StringBuilder sb = new StringBuilder();
		if (loadErrors != 0) {
			sb.append("  ").append(NLS.str("search_dialog.resources_load_errors", loadErrors));
		}
		if (skipBySize != 0) {
			sb.append("  ").append(NLS.str("search_dialog.resources_skip_by_size", skipBySize));
		}
		if (sb.length() != 0) {
			sb.append("  ").append(NLS.str("search_dialog.resources_check_logs"));
		}
		searchDialog.updateProgressLabel(sb.toString());
	}

	private boolean loadResNode(JResource node) {
		try {
			node.loadNode();
			return true;
		} catch (Exception e) {
			LOG.error("Error load resource node: {}", node, e);
			loadErrors++;
			updateProgressInfo();
			return false;
		}
	}

	private void addChildren(JResource resNode) {
		resQueue.addAll(resNode.getSubNodes());
	}

	private static Deque<JResource> initResQueue(MainWindow mw) {
		JRoot jRoot = mw.getTreeRoot();
		Deque<JResource> deque = new ArrayDeque<>(jRoot.getChildCount());
		Enumeration<TreeNode> children = jRoot.children();
		while (children.hasMoreElements()) {
			TreeNode node = children.nextElement();
			if (node instanceof JResource) {
				JResource resNode = (JResource) node;
				deque.add(resNode);
			}
		}
		return deque;
	}

	private boolean isAllowedFileType(JResource resNode) {
		if (resNode.getResFile().getType() == ResourceType.ARSC) {
			// don't check the size of generated resource table, it will also skip all subfiles
			return resourceFilter.isAnyFile()
					|| resourceFilter.getContentTypes().contains(ResourceContentType.CONTENT_TEXT)
					|| resourceFilter.getExtSet().contains("xml");
		}
		ResourceFile resFile = resNode.getResFile();
		if (resourceFilter.isAnyFile()) {
			return true;
		}
		ResourceContentType resContentType = resNode.getContentType();
		if (resourceFilter.getContentTypes().contains(resContentType)) {
			return true;
		}
		String fileExt = CommonFileUtils.getFileExtension(resFile.getOriginalName());
		if (fileExt != null && resourceFilter.getExtSet().contains(fileExt)) {
			return true;
		}
		if (resContentType == ResourceContentType.CONTENT_UNKNOWN
				&& resourceFilter.getContentTypes().contains(ResourceContentType.CONTENT_BINARY)) {
			// treat unknown file type as binary
			return true;
		}
		return false;
	}

	private boolean isAllowedFileSize(JResource resNode, String content) {
		if (sizeLimit <= 0 || resNode.getResFile().getType() == ResourceType.ARSC) {
			return true;
		}
		long size = content.length() * 8L;
		if (size > sizeLimit) {
			LOG.info("Resource search skipped because of size limit. Resource '{}' size {} bytes, limit: {}",
					resNode.getName(), size, sizeLimit);
			skipBySize++;
			updateProgressInfo();
			return false;
		}
		return true;
	}

	static long sizeLimitBytes(int sizeLimitMb) {
		return sizeLimitMb * 1024L * 1024L;
	}

	@Override
	public int progress() {
		return 0;
	}

	@Override
	public int total() {
		return 0;
	}

	private static final class PreparedResource {
		private final JResource node;
		private final String content;

		private PreparedResource(JResource node, String content) {
			this.node = node;
			this.content = content;
		}
	}
}
