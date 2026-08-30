package jadx.core.dex.attributes.nodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.PinnedAttribute;
import jadx.core.dex.attributes.AType;

public class MethodThrowsAttr extends PinnedAttribute {
	private Set<String> list;

	public MethodThrowsAttr() {
	}

	public MethodThrowsAttr(Set<String> list) {
		if (!list.isEmpty()) {
			this.list = list;
		}
	}

	public Set<String> getList() {
		Set<String> throwsList = list;
		return throwsList == null ? Collections.emptySet() : throwsList;
	}

	public int size() {
		Set<String> throwsList = list;
		return throwsList == null ? 0 : throwsList.size();
	}

	public boolean isEmpty() {
		Set<String> throwsList = list;
		return throwsList == null || throwsList.isEmpty();
	}

	public boolean add(String type) {
		Set<String> throwsList = list;
		if (throwsList == null) {
			throwsList = new HashSet<>();
			list = throwsList;
		}
		return throwsList.add(type);
	}

	@Override
	public IJadxAttrType<MethodThrowsAttr> getAttrType() {
		return AType.METHOD_THROWS;
	}

	@Override
	public String toString() {
		return "THROWS:" + list;
	}

}
