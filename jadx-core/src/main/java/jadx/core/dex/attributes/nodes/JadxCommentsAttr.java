package jadx.core.dex.attributes.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.api.CommentsLevel;
import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.IAttributeNode;
import jadx.core.utils.Utils;

public class JadxCommentsAttr implements IJadxAttribute {

	public static void add(IAttributeNode node, CommentsLevel level, String comment) {
		initFor(node).add(level, comment);
	}

	private static JadxCommentsAttr initFor(IAttributeNode node) {
		JadxCommentsAttr currentAttr = node.get(AType.JADX_COMMENTS);
		if (currentAttr != null) {
			return currentAttr;
		}
		JadxCommentsAttr newAttr = new JadxCommentsAttr();
		node.addAttr(newAttr);
		return newAttr;
	}

	private final Map<CommentsLevel, Set<String>> comments = new EnumMap<>(CommentsLevel.class);

	public void add(CommentsLevel level, String comment) {
		Set<String> levelComments = comments.get(level);
		if (levelComments == null) {
			levelComments = new HashSet<>();
			comments.put(level, levelComments);
		}
		levelComments.add(comment);
	}

	public List<String> formatAndFilter(CommentsLevel level) {
		if (level == CommentsLevel.NONE || level == CommentsLevel.USER_ONLY) {
			return Collections.emptyList();
		}
		int resultSize = 0;
		for (Map.Entry<CommentsLevel, Set<String>> entry : comments.entrySet()) {
			if (entry.getKey().filter(level)) {
				resultSize += entry.getValue().size();
			}
		}
		if (resultSize == 0) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(resultSize);
		for (Map.Entry<CommentsLevel, Set<String>> entry : comments.entrySet()) {
			CommentsLevel commentLevel = entry.getKey();
			if (!commentLevel.filter(level)) {
				continue;
			}
			String prefix = "JADX " + commentLevel.name() + ": ";
			for (String comment : entry.getValue()) {
				result.add(prefix + comment);
			}
		}
		Collections.sort(result);
		return result;
	}

	public Map<CommentsLevel, Set<String>> getComments() {
		return comments;
	}

	@Override
	public IJadxAttrType<JadxCommentsAttr> getAttrType() {
		return AType.JADX_COMMENTS;
	}

	@Override
	public String toString() {
		return "JadxCommentsAttr{\n "
				+ Utils.listToString(comments.entrySet(), "\n ",
						e -> e.getKey() + ": \n -> " + Utils.listToString(e.getValue(), "\n -> "))
				+ '}';
	}
}
