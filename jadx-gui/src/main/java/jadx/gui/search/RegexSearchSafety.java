package jadx.gui.search;

public final class RegexSearchSafety {

	private RegexSearchSafety() {
	}

	public static boolean requiresGuard(String regex) {
		return RegexRiskAnalyzer.requiresGuard(regex);
	}
}
