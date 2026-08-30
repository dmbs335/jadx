package jadx.gui.search.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchPositionTest {
	@Test
	void zeroWidthLineAndFileEndMatchesAlwaysAdvance() {
		String content = "first\nsecond\n";
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		int position = 0;
		int matches = 0;

		while (!SearchPosition.exhausted(content.length(), position)) {
			Matcher matcher = pattern.matcher(content);
			if (!matcher.find(position)) {
				break;
			}
			int match = matcher.start();
			int next = SearchPosition.afterMatch(content.length(), match, match);
			assertThat(next).isGreaterThan(position);
			position = next;
			matches++;
			assertThat(matches).isLessThanOrEqualTo(content.length() + 1);
		}

		assertThat(matches).isEqualTo(3);
		assertThat(position).isEqualTo(content.length() + 1);
	}

	@Test
	void preferredLineBoundaryCannotKeepSearchAtSameOffset() {
		assertThat(SearchPosition.afterMatch(100, 42, 42)).isEqualTo(43);
		assertThat(SearchPosition.afterMatch(100, 42, 80)).isEqualTo(80);
	}
}
