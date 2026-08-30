package jadx.gui.ui.hexviewer.search;

import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchParametersTest {
	@Test
	void copyDoesNotShareMutableCondition() {
		SearchParameters source = new SearchParameters();
		source.getCondition().setSearchText("first");

		SearchParameters copy = new SearchParameters(source);
		source.getCondition().setSearchText("second");

		assertThat(copy.getCondition().getSearchText()).isEqualTo("first");
	}

	@Test
	void copyDoesNotShareMutableBinaryData() {
		SearchParameters source = new SearchParameters();
		source.getCondition().setSearchMode(SearchCondition.SearchMode.BINARY);
		ByteArrayEditableData sourceData = new ByteArrayEditableData(new byte[] { 1, 2 });
		source.getCondition().setBinaryData(sourceData);

		SearchParameters copy = new SearchParameters(source);
		sourceData.clear();

		assertThat(copy.getCondition().getBinaryData().getDataSize()).isEqualTo(2);
	}
}
