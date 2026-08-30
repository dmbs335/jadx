package jadx.gui.search.providers;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.JavaClass;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.nodes.FieldNode;
import jadx.gui.jobs.Cancelable;
import jadx.gui.search.SearchSettings;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;

public final class FieldSearchProvider extends BaseSearchProvider {

	private int clsNum = 0;
	private int fldNum = 0;

	public FieldSearchProvider(MainWindow mw, SearchSettings searchSettings, List<JavaClass> classes) {
		super(mw, searchSettings, classes);
	}

	@Override
	public @Nullable JNode next(Cancelable cancelable) {
		if (classes.isEmpty()) {
			return null;
		}
		while (true) {
			if (cancelable.isCanceled()) {
				return null;
			}
			JavaClass cls = classes.get(clsNum);
			List<FieldNode> fields = cls.getClassNode().getFields();
			if (fldNum < fields.size()) {
				FieldNode fld = fields.get(fldNum++);
				if (checkField(fld.getFieldInfo(), cancelable)) {
					JNode result = convert(fld);
					if (result != null) {
						return result;
					}
				}
			} else {
				clsNum++;
				fldNum = 0;
				if (clsNum >= classes.size()) {
					return null;
				}
			}
		}
	}

	private boolean checkField(FieldInfo fieldInfo, Cancelable cancelable) {
		return isMatch(fieldInfo.getName(), cancelable)
				|| isMatch(fieldInfo.getAlias(), cancelable)
				|| isMatch(fieldInfo.getFullId(), cancelable);
	}

	@Override
	public int progress() {
		return clsNum;
	}
}
