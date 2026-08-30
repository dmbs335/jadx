package jadx.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.Consts;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.IAttributeNode;
import jadx.core.dex.attributes.nodes.JadxError;
import jadx.core.dex.nodes.IDexNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.exceptions.JadxOverflowException;

public class ErrorsCounter {
	private static final Logger LOG = LoggerFactory.getLogger(ErrorsCounter.class);
	private static final boolean PRINT_MTH_SIZE = Consts.DEBUG;
	private static final int ANALYSIS_LOSS_SAMPLE_LIMIT = 16;

	private final Set<IAttributeNode> errorNodes = new HashSet<>();
	private int errorsCount;
	private final List<String> globalErrors = new ArrayList<>();
	private final Map<String, AnalysisLossGroup> analysisLosses = new LinkedHashMap<>();
	private final Map<String, AnalysisLossGroup> analysisExclusions = new LinkedHashMap<>();
	private final Set<IAttributeNode> warnNodes = new HashSet<>();
	private int warnsCount;

	public static <N extends IDexNode & IAttributeNode> String error(N node, String warnMsg, Throwable th) {
		return node.root().getErrorsCounter().addError(node, warnMsg, th);
	}

	public static <N extends IDexNode & IAttributeNode> void warning(N node, String warnMsg) {
		node.root().getErrorsCounter().addWarning(node, warnMsg);
	}

	public static String formatMsg(IDexNode node, String msg) {
		return msg + " in " + node.typeName() + ": " + node + ", file: " + node.getInputFileName();
	}

	private synchronized <N extends IDexNode & IAttributeNode> String addError(N node, String error, @Nullable Throwable e) {
		errorNodes.add(node);
		errorsCount++;

		String msg = formatMsg(node, error);
		if (PRINT_MTH_SIZE && node instanceof MethodNode) {
			String mthSize = "[" + ((MethodNode) node).getInsnsCount() + "] ";
			msg = mthSize + msg;
			error = mthSize + error;
		}
		if (e == null) {
			LOG.error(msg);
		} else if (e instanceof StackOverflowError) {
			LOG.error("{}, error: StackOverflowError", msg);
		} else if (e instanceof JadxOverflowException) {
			// don't print full stack trace
			String details = e.getMessage();
			e = new JadxOverflowException(details);
			if (details == null || details.isEmpty()) {
				LOG.error("{}", msg);
			} else {
				LOG.error("{}, details: {}", msg, details);
			}
		} else {
			LOG.error(msg, e);
		}
		node.addAttr(AType.JADX_ERROR, new JadxError(error, e));
		return msg;
	}

	public synchronized void addGlobalError(String error, @Nullable Throwable e) {
		errorsCount++;
		String message = e == null || e.getMessage() == null || e.getMessage().isEmpty()
				? error
				: error + ": " + e.getMessage();
		globalErrors.add(message);
		if (e == null) {
			LOG.error("{}", error);
		} else {
			LOG.error(error, e);
		}
	}

	public synchronized void addAnalysisLoss(String category, String subject, @Nullable Throwable e) {
		AnalysisLossGroup group = analysisLosses.get(category);
		if (group == null) {
			group = new AnalysisLossGroup();
			analysisLosses.put(category, group);
			errorsCount++;
			if (e == null) {
				LOG.error("Analysis loss [{}]: {}", category, subject);
			} else {
				LOG.error("Analysis loss [{}]: {}", category, subject, e);
			}
		}
		group.add(subject, e);
	}

	/**
	 * Record an explicitly audited input which was excluded from analysis. Unlike an analysis loss,
	 * this remains visible in reports but does not increment the error counter or fail completeness.
	 */
	public synchronized void addAnalysisExclusion(String category, String subject, @Nullable Throwable e) {
		AnalysisLossGroup group = analysisExclusions.get(category);
		if (group == null) {
			group = new AnalysisLossGroup();
			analysisExclusions.put(category, group);
			if (e == null) {
				LOG.warn("Audited analysis exclusion [{}]: {}", category, subject);
			} else {
				LOG.warn("Audited analysis exclusion [{}]: {}", category, subject, e);
			}
		}
		group.add(subject, e);
	}

	private synchronized <N extends IDexNode & IAttributeNode> void addWarning(N node, String warn) {
		warnNodes.add(node);
		warnsCount++;
		LOG.warn(formatMsg(node, warn));
	}

	public void printReport() {
		if (getErrorCount() > 0) {
			LOG.error("{} errors occurred in following nodes:", getErrorCount());
			List<String> errors = new ArrayList<>(errorNodes.size());
			for (IAttributeNode node : errorNodes) {
				String nodeName = node.getClass().getSimpleName().replace("Node", "");
				errors.add(nodeName + ": " + node);
			}
			Collections.sort(errors);
			for (String err : errors) {
				LOG.error("  {}", err);
			}
			for (String err : getGlobalErrors()) {
				LOG.error("  Global: {}", err);
			}
			for (Map.Entry<String, Integer> loss : getAnalysisLossCounts().entrySet()) {
				LOG.error("  Analysis loss [{}]: {} affected, samples: {}", loss.getKey(),
						loss.getValue(), getAnalysisLossSamples().get(loss.getKey()));
			}
		}
		for (Map.Entry<String, Integer> exclusion : getAnalysisExclusionCounts().entrySet()) {
			LOG.warn("  Audited analysis exclusion [{}]: {} affected, samples: {}", exclusion.getKey(),
					exclusion.getValue(), getAnalysisExclusionSamples().get(exclusion.getKey()));
		}
		if (getWarnsCount() > 0) {
			LOG.warn("{} warnings in {} nodes", getWarnsCount(), warnNodes.size());
		}
	}

	public int getErrorCount() {
		return errorsCount;
	}

	public int getWarnsCount() {
		return warnsCount;
	}

	public Set<IAttributeNode> getErrorNodes() {
		return errorNodes;
	}

	public synchronized List<String> getGlobalErrors() {
		return Collections.unmodifiableList(new ArrayList<>(globalErrors));
	}

	public synchronized Map<String, Integer> getAnalysisLossCounts() {
		Map<String, Integer> result = new LinkedHashMap<>();
		analysisLosses.forEach((category, group) -> result.put(category, group.count));
		return Collections.unmodifiableMap(result);
	}

	public synchronized Map<String, List<String>> getAnalysisLossSamples() {
		Map<String, List<String>> result = new LinkedHashMap<>();
		analysisLosses.forEach((category, group) -> result.put(category, List.copyOf(group.samples)));
		return Collections.unmodifiableMap(result);
	}

	public synchronized Map<String, Integer> getAnalysisExclusionCounts() {
		Map<String, Integer> result = new LinkedHashMap<>();
		analysisExclusions.forEach((category, group) -> result.put(category, group.count));
		return Collections.unmodifiableMap(result);
	}

	public synchronized Map<String, List<String>> getAnalysisExclusionSamples() {
		Map<String, List<String>> result = new LinkedHashMap<>();
		analysisExclusions.forEach((category, group) -> result.put(category, List.copyOf(group.samples)));
		return Collections.unmodifiableMap(result);
	}

	public Set<IAttributeNode> getWarnNodes() {
		return warnNodes;
	}

	private static final class AnalysisLossGroup {
		private int count;
		private final Set<String> subjects = new HashSet<>();
		private final Set<String> samples = new LinkedHashSet<>();

		private void add(String subject, @Nullable Throwable error) {
			if (!subjects.add(subject)) {
				return;
			}
			count++;
			if (samples.size() >= ANALYSIS_LOSS_SAMPLE_LIMIT) {
				return;
			}
			String message = subject;
			if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
				message += ": " + error.getMessage();
			}
			samples.add(message);
		}
	}
}
