package jadx.core.dex.attributes;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.CommentsLevel;
import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.Consts;
import jadx.core.dex.attributes.nodes.JadxCommentsAttr;
import jadx.core.utils.Utils;

public abstract class AttrNode implements IAttributeNode {

	private @Nullable AttributeStorage storage;

	@Override
	public void add(AFlag flag) {
		initStorage().add(flag);
		if (Consts.DEBUG_ATTRIBUTES) {
			addDebugComment("Add flag " + flag + " at " + Utils.currentStackTrace(2));
		}
	}

	@Override
	public void addAttr(IJadxAttribute attr) {
		initStorage().add(attr);
		if (Consts.DEBUG_ATTRIBUTES) {
			addDebugComment("Add attribute " + attr.getClass().getSimpleName()
					+ ": " + attr + " at " + Utils.currentStackTrace(2));
		}
	}

	@Override
	public void addAttrs(List<IJadxAttribute> list) {
		if (list.isEmpty()) {
			return;
		}
		initStorage().add(list);
	}

	@Override
	public <T> void addAttr(IJadxAttrType<AttrList<T>> type, T obj) {
		initStorage().add(type, obj);
		if (Consts.DEBUG_ATTRIBUTES) {
			addDebugComment("Add attribute " + obj + " at " + Utils.currentStackTrace(2));
		}
	}

	public <T> void addAttr(IJadxAttrType<AttrList<T>> type, List<T> list) {
		initStorage().addAttrList(type, list);
	}

	@Override
	public void copyAttributesFrom(AttrNode attrNode) {
		AttributeStorage copyFrom = attrNode.storage;
		if (copyFrom != null && !copyFrom.isEmpty()) {
			initStorage().addAll(copyFrom);
		}
	}

	@Override
	public <T extends IJadxAttribute> void copyAttributeFrom(AttrNode attrNode, AType<T> attrType) {
		IJadxAttribute attr = attrNode.get(attrType);
		if (attr != null) {
			this.addAttr(attr);
		}
	}

	/**
	 * Remove attribute in this node, add copy from other if exists
	 */
	@Override
	public <T extends IJadxAttribute> void rewriteAttributeFrom(AttrNode attrNode, AType<T> attrType) {
		remove(attrType);
		copyAttributeFrom(attrNode, attrType);
	}

	private AttributeStorage initStorage() {
		AttributeStorage store = storage;
		if (store == null) {
			store = new AttributeStorage();
			storage = store;
		}
		return store;
	}

	private void unloadIfEmpty() {
		AttributeStorage store = storage;
		if (store != null && store.isEmpty()) {
			storage = null;
		}
	}

	@Override
	public boolean contains(AFlag flag) {
		AttributeStorage store = storage;
		return store != null && store.contains(flag);
	}

	@Override
	public <T extends IJadxAttribute> boolean contains(IJadxAttrType<T> type) {
		AttributeStorage store = storage;
		return store != null && store.contains(type);
	}

	@Override
	public <T extends IJadxAttribute> T get(IJadxAttrType<T> type) {
		AttributeStorage store = storage;
		return store == null ? null : store.get(type);
	}

	@Override
	public IAnnotation getAnnotation(String cls) {
		AttributeStorage store = storage;
		return store == null ? null : store.getAnnotation(cls);
	}

	@Override
	public <T> List<T> getAll(IJadxAttrType<AttrList<T>> type) {
		AttributeStorage store = storage;
		return store == null ? Collections.emptyList() : store.getAll(type);
	}

	@Override
	public void remove(AFlag flag) {
		AttributeStorage store = storage;
		if (store != null) {
			store.remove(flag);
			unloadIfEmpty();
		}
	}

	@Override
	public <T extends IJadxAttribute> void remove(IJadxAttrType<T> type) {
		AttributeStorage store = storage;
		if (store != null) {
			store.remove(type);
			unloadIfEmpty();
		}
	}

	@Override
	public void removeAttr(IJadxAttribute attr) {
		AttributeStorage store = storage;
		if (store != null) {
			store.remove(attr);
			unloadIfEmpty();
		}
	}

	@Override
	public void clearAttributes() {
		storage = null;
	}

	public void unloadAttributes() {
		AttributeStorage store = storage;
		if (store == null) {
			return;
		}
		store.unloadAttributes();
		store.clearFlags();
		unloadIfEmpty();
	}

	@Override
	public List<String> getAttributesStringsList() {
		AttributeStorage store = storage;
		return store == null ? Collections.emptyList() : store.getAttributeStrings();
	}

	@Override
	public String getAttributesString() {
		AttributeStorage store = storage;
		return store == null ? "" : store.toString();
	}

	@Override
	public boolean isAttrStorageEmpty() {
		AttributeStorage store = storage;
		return store == null || store.isEmpty();
	}

	private void addDebugComment(String msg) {
		JadxCommentsAttr commentsAttr = get(AType.JADX_COMMENTS);
		if (commentsAttr == null) {
			commentsAttr = new JadxCommentsAttr();
			initStorage().add(commentsAttr);
		}
		commentsAttr.add(CommentsLevel.DEBUG, msg);
	}
}
