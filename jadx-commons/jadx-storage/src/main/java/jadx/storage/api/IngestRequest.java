package jadx.storage.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IngestRequest {
	private final String applicationName;
	private final List<Path> inputFiles;
	private final Path outputDirectory;
	private final String analysisKey;
	private final MaterializationMode materializationMode;
	private final ContentIndexMode indexMode;

	public IngestRequest(String applicationName, List<Path> inputFiles, Path outputDirectory,
			String analysisKey, MaterializationMode materializationMode) {
		this(applicationName, inputFiles, outputDirectory, analysisKey, materializationMode, ContentIndexMode.SECURITY);
	}

	public IngestRequest(String applicationName, List<Path> inputFiles, Path outputDirectory,
			String analysisKey, MaterializationMode materializationMode, ContentIndexMode indexMode) {
		this.applicationName = Objects.requireNonNull(applicationName);
		this.inputFiles = Collections.unmodifiableList(new ArrayList<>(inputFiles));
		this.outputDirectory = Objects.requireNonNull(outputDirectory);
		this.analysisKey = Objects.requireNonNull(analysisKey);
		this.materializationMode = Objects.requireNonNull(materializationMode);
		this.indexMode = Objects.requireNonNull(indexMode);
		if (inputFiles.isEmpty()) {
			throw new IllegalArgumentException("At least one input file is required");
		}
	}

	public String getApplicationName() {
		return applicationName;
	}

	public List<Path> getInputFiles() {
		return inputFiles;
	}

	public Path getOutputDirectory() {
		return outputDirectory;
	}

	public String getAnalysisKey() {
		return analysisKey;
	}

	public MaterializationMode getMaterializationMode() {
		return materializationMode;
	}

	public ContentIndexMode getIndexMode() {
		return indexMode;
	}
}
