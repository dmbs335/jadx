package jadx.gui.ui.hexviewer.search;

/*
 * Copyright (C) ExBin Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;

import jadx.gui.ui.hexviewer.HexSearchBar;
import jadx.gui.ui.hexviewer.search.service.BinarySearchService;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

/**
 * Binary search.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinarySearch {

	private static final int DEFAULT_DELAY = 500;

	private volatile InvokeSearchThread invokeSearchThread;
	private volatile SearchThread searchThread;
	private volatile long searchGeneration;

	private SearchOperation currentSearchOperation = SearchOperation.FIND;
	private SearchParameters.SearchDirection currentSearchDirection = SearchParameters.SearchDirection.FORWARD;
	private final SearchParameters currentSearchParameters = new SearchParameters();
	private BinarySearchService.FoundMatches foundMatches = new BinarySearchService.FoundMatches();

	private BinarySearchService binarySearchService;
	private final BinarySearchService.SearchStatusListener searchStatusListener;
	private HexSearchBar binarySearchPanel;

	public BinarySearch(HexSearchBar binarySearchPanel) {
		this.binarySearchPanel = binarySearchPanel;

		searchStatusListener = new BinarySearchService.SearchStatusListener() {
			@Override
			public void setStatus(BinarySearchService.FoundMatches foundMatches, SearchParameters.MatchMode matchMode) {
				BinarySearch.this.foundMatches = foundMatches;
				switch (foundMatches.getMatchesCount()) {
					case 0:
						binarySearchPanel.setInfoLabel(NLS.str("search.match_not_found"));
						break;
					case 1:
						binarySearchPanel.setInfoLabel(
								matchMode == SearchParameters.MatchMode.MULTIPLE
										? NLS.str("search.single_match")
										: NLS.str("search.match_found"));
						break;
					default:
						binarySearchPanel.setInfoLabel(String.format(NLS.str("search.match_of"),
								foundMatches.getMatchPosition() + 1, foundMatches.getMatchesCount()));
						break;
				}
				updateMatchStatus();
			}

			@Override
			public void clearStatus() {
				binarySearchPanel.setInfoLabel("");
				BinarySearch.this.foundMatches = new BinarySearchService.FoundMatches();
				updateMatchStatus();
			}

			private void updateMatchStatus() {
				int matchesCount = foundMatches.getMatchesCount();
				int matchPosition = foundMatches.getMatchPosition();
				binarySearchPanel.updateMatchCount(matchesCount > 0,
						matchesCount > 1 && matchPosition > 0,
						matchPosition < matchesCount - 1);
			}
		};
		binarySearchPanel.setControl(new HexSearchBar.Control() {
			@Override
			public void prevMatch() {
				foundMatches.prev();
				binarySearchService.setMatchPosition(foundMatches.getMatchPosition());
				searchStatusListener.setStatus(foundMatches, binarySearchService.getLastSearchParameters().getMatchMode());
			}

			@Override
			public void nextMatch() {
				foundMatches.next();
				binarySearchService.setMatchPosition(foundMatches.getMatchPosition());
				searchStatusListener.setStatus(foundMatches, binarySearchService.getLastSearchParameters().getMatchMode());
			}

			@Override
			public void performEscape() {
				cancelSearch();
				close();
				clearSearch();
			}

			@Override
			public void performFind() {
				invokeSearch(SearchOperation.FIND);
			}

			@Override
			public void notifySearchChanged() {
				if (currentSearchOperation == SearchOperation.FIND) {
					invokeSearch(SearchOperation.FIND);
				}
			}

			@Override
			public void notifySearchChanging() {
				if (currentSearchOperation != SearchOperation.FIND) {
					return;
				}

				SearchCondition condition = currentSearchParameters.getCondition();
				SearchCondition updatedSearchCondition = binarySearchPanel.getSearchParameters().getCondition();

				switch (updatedSearchCondition.getSearchMode()) {
					case TEXT: {
						String searchText = updatedSearchCondition.getSearchText();
						if (searchText.isEmpty()) {
							condition.setSearchText(searchText);
							clearSearch();
							return;
						}

						if (searchText.equals(condition.getSearchText())) {
							return;
						}

						condition.setSearchText(searchText);
						break;
					}
					case BINARY: {
						EditableBinaryData searchData = (EditableBinaryData) updatedSearchCondition.getBinaryData();
						if (searchData == null || searchData.isEmpty()) {
							condition.setBinaryData(null);
							clearSearch();
							return;
						}

						if (searchData.equals(condition.getBinaryData())) {
							return;
						}

						ByteArrayEditableData data = new ByteArrayEditableData();
						data.insert(0, searchData);
						condition.setBinaryData(data);
						break;
					}
				}
				BinarySearch.this.invokeSearch(SearchOperation.FIND, DEFAULT_DELAY);
			}

			@Override
			public SearchParameters.SearchDirection getSearchDirection() {
				return currentSearchDirection;
			}

			@Override
			public void close() {
				cancelSearch();
				clearSearch();
			}
		});
	}

	public void setBinarySearchService(BinarySearchService binarySearchService) {
		this.binarySearchService = binarySearchService;
	}

	public void setTargetComponent(HexSearchBar targetComponent) {
		binarySearchPanel = targetComponent;
	}

	public BinarySearchService.SearchStatusListener getSearchStatusListener() {
		return searchStatusListener;
	}

	private void invokeSearch(SearchOperation searchOperation) {
		invokeSearch(searchOperation, binarySearchPanel.getSearchParameters(), 0);
	}

	private void invokeSearch(SearchOperation searchOperation, final int delay) {
		invokeSearch(searchOperation, binarySearchPanel.getSearchParameters(), delay);
	}

	private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters) {
		invokeSearch(searchOperation, searchParameters, 0);
	}

	private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters, final int delay) {
		SearchParameters searchSnapshot = new SearchParameters(searchParameters);
		synchronized (this) {
			searchGeneration++;
			interruptSearchThreads();
			currentSearchOperation = searchOperation;
			currentSearchParameters.setFromParameters(searchSnapshot);
			invokeSearchThread = new InvokeSearchThread(searchGeneration, searchOperation, searchSnapshot, delay);
			invokeSearchThread.start();
		}
	}

	public synchronized void cancelSearch() {
		searchGeneration++;
		interruptSearchThreads();
		invokeSearchThread = null;
		searchThread = null;
	}

	private void interruptSearchThreads() {
		InvokeSearchThread invokeThread = invokeSearchThread;
		if (invokeThread != null) {
			invokeThread.interrupt();
		}
		SearchThread worker = searchThread;
		if (worker != null) {
			worker.interrupt();
		}
	}

	public void clearSearch() {
		SearchCondition condition = currentSearchParameters.getCondition();
		condition.clear();
		binarySearchPanel.clearSearch();
		binarySearchService.clearMatches();
		searchStatusListener.clearStatus();
	}

	public HexSearchBar getPanel() {
		return binarySearchPanel;
	}

	public void dataChanged() {
		binarySearchService.clearMatches();
		invokeSearch(currentSearchOperation, DEFAULT_DELAY);
	}

	private class InvokeSearchThread extends Thread {

		private final long generation;
		private final SearchOperation operation;
		private final SearchParameters parameters;
		private final int delay;

		public InvokeSearchThread(long generation, SearchOperation operation, SearchParameters parameters, int delay) {
			super("InvokeSearchThread");
			this.generation = generation;
			this.operation = operation;
			this.parameters = parameters;
			this.delay = delay;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(delay);
				startSearch(this, generation, operation, parameters);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private synchronized void startSearch(InvokeSearchThread source, long generation,
			SearchOperation operation, SearchParameters parameters) {
		if (generation != searchGeneration || invokeSearchThread != source) {
			return;
		}
		SearchThread worker = searchThread;
		if (worker != null) {
			worker.interrupt();
		}
		invokeSearchThread = null;
		searchThread = new SearchThread(generation, operation, parameters);
		searchThread.start();
	}

	private class SearchThread extends Thread {
		private final long generation;
		private final SearchOperation operation;
		private final SearchParameters parameters;

		public SearchThread(long generation, SearchOperation operation, SearchParameters parameters) {
			super("SearchThread");
			this.generation = generation;
			this.operation = operation;
			this.parameters = parameters;
		}

		@Override
		public void run() {
			try {
				BinarySearchService.SearchStatusListener guardedListener = guardedListener(generation);
				switch (operation) {
					case FIND:
						binarySearchService.performFind(parameters, guardedListener);
						break;
					case FIND_AGAIN:
						binarySearchService.performFindAgain(guardedListener);
						break;
					default:
						throw new UnsupportedOperationException("Not supported yet.");
				}
			} finally {
				searchComplete(this);
			}
		}
	}

	private BinarySearchService.SearchStatusListener guardedListener(long generation) {
		return new BinarySearchService.SearchStatusListener() {
			@Override
			public void setStatus(BinarySearchService.FoundMatches matches, SearchParameters.MatchMode matchMode) {
				UiUtils.uiRun(() -> {
					if (!isCanceled()) {
						searchStatusListener.setStatus(matches, matchMode);
					}
				});
			}

			@Override
			public void clearStatus() {
				UiUtils.uiRun(() -> {
					if (!isCanceled()) {
						searchStatusListener.clearStatus();
					}
				});
			}

			@Override
			public boolean isCanceled() {
				return generation != searchGeneration || Thread.currentThread().isInterrupted();
			}
		};
	}

	private synchronized void searchComplete(SearchThread worker) {
		if (searchThread == worker) {
			searchThread = null;
		}
	}

	private enum SearchOperation {
		FIND,
		FIND_AGAIN
	}
}
