package jadx.gui.search;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import jadx.gui.jobs.Cancelable;

public interface ISearchMethod {
	int find(String input, String subStr, int start);

	default int find(String input, String subStr, int start, Cancelable cancelable) {
		return find(input, subStr, start);
	}

	static ISearchMethod build(SearchSettings searchSettings) {
		if (searchSettings.isUseRegex()) {
			Pattern pattern = searchSettings.getPattern();
			return RegexSearchMethod.build(pattern);
		}
		if (searchSettings.isIgnoreCase()) {
			return StringUtils::indexOfIgnoreCase;
		}
		return String::indexOf;
	}
}
