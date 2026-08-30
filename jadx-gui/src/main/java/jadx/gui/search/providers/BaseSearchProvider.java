package jadx.gui.search.providers;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.core.dex.nodes.ICodeNode;
import jadx.gui.jobs.Cancelable;
import jadx.gui.search.ISearchMethod;
import jadx.gui.search.ISearchProvider;
import jadx.gui.search.SearchSettings;
import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.JNodeCache;

public abstract class BaseSearchProvider implements ISearchProvider {

	private final JNodeCache nodeCache;
	protected final ISearchMethod searchMth;
	protected final String searchStr;
	protected final List<JavaClass> classes;
	protected final SearchSettings searchSettings;

	public BaseSearchProvider(MainWindow mw, SearchSettings searchSettings, List<JavaClass> classes) {
		this.nodeCache = mw.getCacheObject().getNodeCache();
		this.searchMth = searchSettings.getSearchMethod();
		this.searchStr = searchSettings.getSearchString();
		if (searchSettings.getSearchPackage() != null) {
			this.classes = classes
					.stream()
					.filter(c -> c.getJavaPackage().isDescendantOf(searchSettings.getSearchPackage()))
					.collect(Collectors.toList());
		} else {
			this.classes = classes;
		}
		this.searchSettings = searchSettings;
	}

	protected boolean isMatch(String str, Cancelable cancelable) {
		return searchMth.find(str, searchStr, 0, cancelable) != -1;
	}

	protected @Nullable JNode convert(JavaNode node) {
		return nodeCache.makeFrom(node);
	}

	protected JClass convert(JavaClass cls) {
		return nodeCache.makeFrom(cls);
	}

	protected @Nullable JNode convert(ICodeNode codeNode) {
		return nodeCache.makeFrom(codeNode);
	}

	@Override
	public int total() {
		return classes.size();
	}
}
