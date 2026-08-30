package jadx.gui.search;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Selects patterns that need an interruptible input wrapper. This is deliberately
 * conservative: false positives only add a guard to advanced regex searches,
 * while ordinary literal and single-quantifier searches retain the JDK String path.
 */
final class RegexRiskAnalyzer {

	private RegexRiskAnalyzer() {
	}

	static boolean requiresGuard(String regex) {
		Deque<GroupState> groups = new ArrayDeque<>();
		boolean escaped = false;
		boolean inCharClass = false;
		boolean riskyClosedGroup = false;
		int unboundedQuantifiers = 0;
		for (int i = 0; i < regex.length(); i++) {
			char ch = regex.charAt(i);
			if (escaped) {
				if (!inCharClass && (Character.isDigit(ch) || ch == 'k')) {
					return true; // numbered or named backreference
				}
				escaped = false;
				riskyClosedGroup = false;
				continue;
			}
			if (ch == '\\') {
				escaped = true;
				continue;
			}
			if (ch == '[' && !inCharClass) {
				inCharClass = true;
				riskyClosedGroup = false;
				continue;
			}
			if (ch == ']' && inCharClass) {
				inCharClass = false;
				continue;
			}
			if (inCharClass) {
				continue;
			}
			if (ch == '(') {
				groups.push(new GroupState());
				riskyClosedGroup = false;
				continue;
			}
			if (ch == ')') {
				GroupState group = groups.poll();
				riskyClosedGroup = group != null && (group.containsQuantifier || group.containsAlternation);
				continue;
			}
			if (ch == '|') {
				GroupState group = groups.peek();
				if (group != null) {
					group.containsAlternation = true;
				}
				riskyClosedGroup = false;
				continue;
			}
			if (isQuantifierStart(regex, i, ch)) {
				if (riskyClosedGroup) {
					return true;
				}
				for (GroupState group : groups) {
					group.containsQuantifier = true;
				}
				if (isUnboundedQuantifier(regex, i, ch) && ++unboundedQuantifiers >= 3) {
					return true;
				}
				riskyClosedGroup = false;
				continue;
			}
			riskyClosedGroup = false;
		}
		return false;
	}

	private static boolean isQuantifierStart(String regex, int index, char ch) {
		if (ch == '*' || ch == '+' || ch == '?') {
			if (ch == '?' && index > 0 && regex.charAt(index - 1) == '(') {
				return false; // non-capturing group, lookaround, named group or inline flags
			}
			// Lazy/possessive modifiers belong to the preceding quantifier.
			return index == 0 || (regex.charAt(index - 1) != '*' && regex.charAt(index - 1) != '+'
					&& regex.charAt(index - 1) != '?' && regex.charAt(index - 1) != '}');
		}
		return ch == '{' && findQuantifierEnd(regex, index) != -1;
	}

	private static boolean isUnboundedQuantifier(String regex, int index, char ch) {
		if (ch == '*' || ch == '+') {
			return true;
		}
		if (ch != '{') {
			return false;
		}
		int end = findQuantifierEnd(regex, index);
		if (end == -1) {
			return false;
		}
		String range = regex.substring(index + 1, end);
		return range.endsWith(",");
	}

	private static int findQuantifierEnd(String regex, int start) {
		int end = regex.indexOf('}', start + 1);
		if (end == -1 || end == start + 1) {
			return -1;
		}
		boolean commaSeen = false;
		for (int i = start + 1; i < end; i++) {
			char ch = regex.charAt(i);
			if (ch == ',') {
				if (commaSeen) {
					return -1;
				}
				commaSeen = true;
			} else if (!Character.isDigit(ch)) {
				return -1;
			}
		}
		return end;
	}

	private static final class GroupState {
		private boolean containsQuantifier;
		private boolean containsAlternation;
	}
}
