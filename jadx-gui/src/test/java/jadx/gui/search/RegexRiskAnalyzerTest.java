package jadx.gui.search;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegexRiskAnalyzerTest {

	@ParameterizedTest
	@CsvSource(
			delimiter = ';', value = {
					"needle$;false",
					"foo.*bar;false",
					"(a+)+;true",
					"(a|aa)+;true",
					"(.*a){2,};true",
					"(a)\\1;true",
					"(?<word>a+)\\k<word>;true",
					"a.*b.*c.*d;true",
					"[+*?]{2};false",
					"\\p{L}+;false",
					"(?:foo|bar);false",
					"(?:foo|bar)+;true"
			}
	)
	void classifiesBacktrackingRisk(String regex, boolean expected) {
		assertThat(RegexSearchSafety.requiresGuard(regex)).isEqualTo(expected);
	}
}
