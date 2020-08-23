package ai.hual.labrador.nlu.matchers;


import java.util.ArrayList;
import java.util.List;

public class IntentClassifierResult {
    /**
     * best matching pQuery
     */
    private String query;
    /**
     * Score of the best hit.
     */
    private double score;
    /**
     * Best group score / second best group score.
     */
    private double confidence;
    /**
     * Hits.
     */
    private List<IntentRankResult> hits;
    /**
     * Matched intents. (only have one pair in list now)
     */
    // TODO: this might not be a list anymore
    private List<IntentScorePair> intents;

    public IntentClassifierResult() {
    }

    public IntentClassifierResult(IntentClassifierResult classifierResult) {
        this.query = classifierResult.getQuery();
        this.intents = new ArrayList<>();
        classifierResult.getIntents().forEach(p -> this.intents.add(new IntentScorePair(p)));
        this.confidence = classifierResult.getConfidence();
    }

    public IntentClassifierResult(String query, List<IntentScorePair> intents, float confidence) {
        this.query = query;
        this.intents = intents;
        this.confidence = confidence;
    }

    public IntentScorePair findIntentScorePair(String intent) {
        return intents.stream()
                .filter(i -> i.getIntent().equals(intent))
                .findFirst()
                .orElse(null);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<IntentScorePair> getIntents() {
        return intents;
    }

    public void setIntents(List<IntentScorePair> intents) {
        this.intents = intents;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        if (intents == null)
            return "No result by calling API";
        s.append("confidence=").append(confidence).append(",");
        for (IntentScorePair intent : intents) {
            s.append(intent.toString()).append(",");
        }
        return s.toString();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<IntentRankResult> getHits() {
        return hits;
    }

    public void setHits(List<IntentRankResult> hits) {
        this.hits = hits;
    }
}
