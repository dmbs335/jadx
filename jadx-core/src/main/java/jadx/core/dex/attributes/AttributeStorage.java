package jadx.core.dex.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jadx.api.plugins.input.data.annotations.IAnnotation;
import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.api.plugins.input.data.attributes.JadxAttrType;
import jadx.api.plugins.input.data.attributes.types.AnnotationsAttr;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;

/**
 * Storage for different attribute types:<br>
 * 1. Flags - boolean attribute (set or not)<br>
 * 2. Attributes - class instance ({@link IJadxAttribute}) associated with an attribute type
 * ({@link IJadxAttrType})<br>
 */
public class AttributeStorage {
	private static final AFlag[] FLAGS = AFlag.values();

	public static AttributeStorage fromList(List<IJadxAttribute> list) {
		AttributeStorage storage = new AttributeStorage();
		storage.add(list);
		return storage;
	}

	static {
		int flagsCount = FLAGS.length;
		if (flagsCount > Long.SIZE) {
			throw new JadxRuntimeException("Try to reduce flags count to 64 for use one long bit set, now " + flagsCount);
		}
	}

	private long flags;
	/**
	 * Tagged attribute storage: {@code null}, a single {@link IJadxAttribute}, or an
	 * {@link AttributeMap}. A node can't be in the single and map states at once,
	 * so sharing one slot avoids an otherwise unused reference in every node.
	 */
	private volatile Object attributes;

	public void add(AFlag flag) {
		flags |= flagMask(flag);
	}

	public void add(IJadxAttribute attr) {
		synchronized (this) {
			putAttribute(attr);
			markJadxError(attr.getAttrType());
		}
	}

	public void add(List<IJadxAttribute> list) {
		synchronized (this) {
			for (IJadxAttribute attr : list) {
				putAttribute(attr);
				markJadxError(attr.getAttrType());
			}
		}
	}

	public <T> void add(IJadxAttrType<AttrList<T>> type, T obj) {
		AttrList<T> list = get(type);
		if (list != null) {
			list.getList().add(obj);
		} else {
			add(new AttrList<>(type, ListUtils.mutableListOf(obj)));
		}
	}

	public <T> void addAttrList(IJadxAttrType<AttrList<T>> type, List<T> attrList) {
		AttrList<T> list = get(type);
		if (list != null) {
			list.getList().addAll(attrList);
		} else {
			add(new AttrList<>(type, attrList));
		}
	}

	public void addAll(AttributeStorage otherList) {
		flags |= otherList.flags;
		Object otherAttributes = otherList.attributes;
		if (otherAttributes != null) {
			synchronized (this) {
				if (otherAttributes instanceof IJadxAttribute) {
					putAttribute((IJadxAttribute) otherAttributes);
				} else {
					AttributeMap map = attributesMap(otherAttributes);
					for (int i = 0; i < map.size(); i++) {
						putAttribute(map.valueAt(i));
					}
				}
			}
		}
	}

	public boolean contains(AFlag flag) {
		return (flags & flagMask(flag)) != 0;
	}

	public <T extends IJadxAttribute> boolean contains(IJadxAttrType<T> type) {
		Object stored = attributes;
		if (stored instanceof IJadxAttribute) {
			return ((IJadxAttribute) stored).getAttrType() == type;
		}
		return stored != null && attributesMap(stored).get(type) != null;
	}

	@SuppressWarnings("unchecked")
	public <T extends IJadxAttribute> T get(IJadxAttrType<T> type) {
		Object stored = attributes;
		if (stored instanceof IJadxAttribute) {
			IJadxAttribute attr = (IJadxAttribute) stored;
			return attr.getAttrType() == type ? (T) attr : null;
		}
		return stored == null ? null : (T) attributesMap(stored).get(type);
	}

	public IAnnotation getAnnotation(String cls) {
		AnnotationsAttr aList = get(JadxAttrType.ANNOTATION_LIST);
		return aList == null ? null : aList.get(cls);
	}

	public <T> List<T> getAll(IJadxAttrType<AttrList<T>> type) {
		AttrList<T> attrList = get(type);
		if (attrList == null) {
			return Collections.emptyList();
		}
		return attrList.getReadOnlyList();
	}

	public void remove(AFlag flag) {
		flags &= ~flagMask(flag);
	}

	public void clearFlags() {
		flags = 0;
	}

	public <T extends IJadxAttribute> void remove(IJadxAttrType<T> type) {
		synchronized (this) {
			Object stored = attributes;
			if (stored instanceof IJadxAttribute) {
				IJadxAttribute attr = (IJadxAttribute) stored;
				if (attr.getAttrType() == type) {
					attributes = null;
				}
			} else if (stored != null) {
				AttributeMap map = attributesMap(stored);
				map.remove(type);
				collapseAttributes(map);
			}
			if (type == AType.JADX_ERROR) {
				remove(AFlag.JADX_ERROR);
			}
		}
	}

	public void remove(IJadxAttribute attr) {
		synchronized (this) {
			Object stored = attributes;
			if (stored instanceof IJadxAttribute) {
				IJadxAttribute single = (IJadxAttribute) stored;
				if (single == attr) {
					attributes = null;
				}
			} else if (stored != null) {
				AttributeMap map = attributesMap(stored);
				IJadxAttrType<? extends IJadxAttribute> type = attr.getAttrType();
				IJadxAttribute storedAttr = map.get(type);
				if (storedAttr == attr) {
					map.remove(type);
					collapseAttributes(map);
				}
			}
			if (attr.getAttrType() == AType.JADX_ERROR && get(AType.JADX_ERROR) == null) {
				remove(AFlag.JADX_ERROR);
			}
		}
	}

	private void putAttribute(IJadxAttribute attr) {
		Object stored = attributes;
		if (stored instanceof IJadxAttribute) {
			IJadxAttribute single = (IJadxAttribute) stored;
			if (single.getAttrType() == attr.getAttrType()) {
				attributes = attr;
				return;
			}
			attributes = new AttributeMap(single, attr);
			return;
		}
		if (stored == null) {
			attributes = attr;
		} else {
			attributesMap(stored).put(attr.getAttrType(), attr);
		}
	}

	private void markJadxError(IJadxAttrType<?> type) {
		if (type == AType.JADX_ERROR) {
			add(AFlag.JADX_ERROR);
		}
	}

	private void collapseAttributes(AttributeMap map) {
		if (map.isEmpty()) {
			attributes = null;
		} else if (map.size() == 1) {
			attributes = map.valueAt(0);
		}
	}

	public void unloadAttributes() {
		synchronized (this) {
			Object stored = attributes;
			if (stored instanceof IJadxAttribute) {
				IJadxAttribute attr = (IJadxAttribute) stored;
				if (!attr.keepLoaded()) {
					attributes = null;
				}
			} else if (stored != null) {
				AttributeMap map = attributesMap(stored);
				map.removeNotLoaded();
				collapseAttributes(map);
			}
			remove(AFlag.JADX_ERROR);
		}
	}

	public List<String> getAttributeStrings() {
		Object stored = attributes;
		int attributesCount = stored == null ? 0 : stored instanceof IJadxAttribute ? 1 : attributesMap(stored).size();
		int size = Long.bitCount(flags) + attributesCount;
		if (size == 0) {
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<>(size);
		for (AFlag flag : FLAGS) {
			if (contains(flag)) {
				list.add(flag.toString());
			}
		}
		if (stored instanceof IJadxAttribute) {
			IJadxAttribute attr = (IJadxAttribute) stored;
			list.add(attr.toAttrString());
		} else if (stored != null) {
			AttributeMap map = attributesMap(stored);
			for (int i = 0; i < map.size(); i++) {
				list.add(map.valueAt(i).toAttrString());
			}
		}
		return list;
	}

	public boolean isEmpty() {
		return flags == 0 && attributes == null;
	}

	private static AttributeMap attributesMap(Object stored) {
		return (AttributeMap) stored;
	}

	private static final class AttributeMap {
		private Object[] entries;
		private int size;

		private AttributeMap(IJadxAttribute first, IJadxAttribute second) {
			entries = new Object[] { first.getAttrType(), first, second.getAttrType(), second };
			size = 2;
		}

		private int size() {
			return size;
		}

		private boolean isEmpty() {
			return size == 0;
		}

		private IJadxAttribute get(IJadxAttrType<?> type) {
			for (int i = 0; i < size; i++) {
				int offset = i * 2;
				if (entries[offset] == type) {
					return (IJadxAttribute) entries[offset + 1];
				}
			}
			return null;
		}

		private IJadxAttribute valueAt(int index) {
			return (IJadxAttribute) entries[index * 2 + 1];
		}

		private void put(IJadxAttrType<?> type, IJadxAttribute attr) {
			for (int i = 0; i < size; i++) {
				int offset = i * 2;
				if (entries[offset] == type) {
					entries[offset + 1] = attr;
					return;
				}
			}
			int offset = size * 2;
			if (offset == entries.length) {
				entries = Arrays.copyOf(entries, entries.length * 2);
			}
			entries[offset] = type;
			entries[offset + 1] = attr;
			size++;
		}

		private void remove(IJadxAttrType<?> type) {
			for (int i = 0; i < size; i++) {
				int offset = i * 2;
				if (entries[offset] == type) {
					removeAt(i);
					return;
				}
			}
		}

		private void removeNotLoaded() {
			for (int i = size - 1; i >= 0; i--) {
				if (!valueAt(i).keepLoaded()) {
					removeAt(i);
				}
			}
		}

		private void removeAt(int index) {
			int offset = index * 2;
			int lastOffset = (size - 1) * 2;
			if (offset != lastOffset) {
				entries[offset] = entries[lastOffset];
				entries[offset + 1] = entries[lastOffset + 1];
			}
			entries[lastOffset] = null;
			entries[lastOffset + 1] = null;
			size--;
		}
	}

	private static long flagMask(AFlag flag) {
		return 1L << flag.ordinal();
	}

	@Override
	public String toString() {
		List<String> list = getAttributeStrings();
		if (list.isEmpty()) {
			return "";
		}
		list.sort(String::compareTo);
		return "A[" + Utils.listToString(list) + ']';
	}
}
