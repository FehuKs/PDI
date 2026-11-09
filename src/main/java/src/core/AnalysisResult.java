package src.core;

import java.util.Map;

/** Similarity scores are diagnostic values, not calibrated probabilities. */
public record AnalysisResult(Status status, String summary, Map<String, String> details) {
    public enum Status { IDENTIFIED, INCONCLUSIVE, NOT_FOUND }
    public AnalysisResult { details = Map.copyOf(details); }
}
