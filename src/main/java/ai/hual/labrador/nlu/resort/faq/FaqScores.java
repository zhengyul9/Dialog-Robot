package ai.hual.labrador.nlu.resort.faq;

import java.util.Map;

public class FaqScores {

    private int qaid;   // qaid
    private int qid;
    private String question;    // question content
    private String category;    // question category
    private Map<String, Double> scores; // including bm25, selfBm25, embedDis, lstm

    public static class SortItem {
        private FaqScores item;
        private double score;

        public SortItem(FaqScores item, double score) {
            this.item = item;
            this.score = score;
        }

        public FaqScores getItem() {
            return item;
        }

        public void setItem(FaqScores item) {
            this.item = item;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public FaqScores(int qaid, String question, Map<String, Double> scores) {
        this.qaid = qaid;
        this.question = question;
        this.category = null;
        this.scores = scores;
    }

    public FaqScores(int qaid, String question, String category, Map<String, Double> scores) {
        this.qaid = qaid;
        this.question = question;
        this.category = category;
        this.scores = scores;
    }

    public FaqScores(int qaid, int qid, String question, String category, Map<String, Double> scores) {
        this.qaid = qaid;
        this.qid = qid;
        this.question = question;
        this.category = category;
        this.scores = scores;
    }

    public SortItem toSortItem(Map<String, Double> weights, double totalWeights) {
        return new SortItem(this, mergeScores(scores, weights, totalWeights));
    }

    /**
     * Horizontally merge scores.
     *
     * @param scores       scores
     * @param totalWeights
     * @return merged scores
     */
    private double mergeScores(Map<String, Double> scores, Map<String, Double> weights, double totalWeights) {
        double score = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            score += scores.get(entry.getKey()) != null ? scores.get(entry.getKey()) * entry.getValue() : 0;
        }
        return score / totalWeights;
    }

    public int getQaid() {
        return qaid;
    }

    public void setQaid(int qaid) {
        this.qaid = qaid;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }
}
