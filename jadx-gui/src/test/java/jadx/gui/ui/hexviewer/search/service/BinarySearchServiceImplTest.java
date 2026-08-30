package jadx.gui.ui.hexviewer.search.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;
import org.exbin.bined.highlight.swing.SearchMatch;
import org.junit.jupiter.api.Test;

import jadx.gui.ui.hexviewer.search.SearchCondition;
import jadx.gui.ui.hexviewer.search.SearchParameters;

import static org.assertj.core.api.Assertions.assertThat;

class BinarySearchServiceImplTest {
	@Test
	void backwardSearchVisitsAllEarlierPositions() {
		SearchParameters parameters = binarySearch(new byte[] { 1, 2 });
		parameters.setSearchDirection(SearchParameters.SearchDirection.BACKWARD);
		parameters.setStartPosition(4);

		List<SearchMatch> matches = BinarySearchServiceImpl.findBinaryMatches(
				parameters,
				new ByteArrayEditableData(new byte[] { 1, 2, 0, 1, 2, 1 }),
				() -> false);

		assertThat(matches).extracting(SearchMatch::getPosition).containsExactly(0L, 3L);
	}

	@Test
	void canceledSearchStopsBeforeScanningWholeInput() {
		SearchParameters parameters = binarySearch(new byte[] { 9 });
		parameters.setStartPosition(0);
		AtomicInteger checks = new AtomicInteger();

		List<SearchMatch> matches = BinarySearchServiceImpl.findBinaryMatches(
				parameters,
				new ByteArrayEditableData(new byte[1_000_000]),
				() -> checks.incrementAndGet() > 10);

		assertThat(matches).isNull();
		assertThat(checks).hasValue(11);
	}

	private static SearchParameters binarySearch(byte[] searchData) {
		SearchParameters parameters = new SearchParameters();
		parameters.getCondition().setSearchMode(SearchCondition.SearchMode.BINARY);
		parameters.getCondition().setBinaryData(new ByteArrayEditableData(searchData));
		return parameters;
	}
}
