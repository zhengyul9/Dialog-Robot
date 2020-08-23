package ai.hual.labrador.nlu.resort.intent;

import java.util.Map;

import static ai.hual.labrador.nlu.resort.ResortUtils.BM25_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.SELF_BM25_KEY;

public class IntentScores {
    private static int BASE_BM25 = 20;

    String intent;  // intent
    String query;    // input query, a.k.a pQuery
    String question;    // question content
    Map<String, Double> scores; // including queryScore, bm25, selfBm25, embedDis

    static class SortItem {
        IntentScores item;
        double score;

        public SortItem(IntentScores item, double score) {
            this.item = item;
            this.score = score;
        }
    }

    public IntentScores(String intent, String query, String question, Map<String, Double> scores) {
        this.intent = intent;
        this.query = query;
        this.question = question;
        this.scores = scores;
    }

    public SortItem toSortItem(Map<String, Double> weights) {
        return new SortItem(this, mergeScores(scores, weights));
    }

    /**
     * Horizontally merge scores.
     *
     * @param scores scores
     * @return merged scores
     */
    private double mergeScores(Map<String, Double> scores, Map<String, Double> weights) {
        scores.put(BM25_KEY, scores.get(BM25_KEY) / Math.max(Math.max(scores.get(SELF_BM25_KEY) != null ? scores.get(SELF_BM25_KEY) : 1000,
                BASE_BM25), scores.get(BM25_KEY)));  // compute real bm25
        double score = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            score += scores.get(entry.getKey()) != null ? scores.get(entry.getKey()) * entry.getValue() : 0;
        }
        return score;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }
}
