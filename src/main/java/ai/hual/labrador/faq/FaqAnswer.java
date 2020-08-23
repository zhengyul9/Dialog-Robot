package ai.hual.labrador.faq;

import java.util.List;

public class FaqAnswer {
    /**
     * Income query.
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
     * Standard question of best hit.
     */
    private String standardQuestion;
    /**
     * Answer of standard question.
     */
    private String answer;
    /**
     * Hits.
     */
    private List<FaqRankResult> hits;

    public String getQuery() {
        return query;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String toString() {
        return "query:" + query +
                ", standardQuestion:" + standardQuestion +
                ", answer:" + answer;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getStandardQuestion() {
        return this.standardQuestion;
    }

    public void setStandardQuestion(String standardQuestion) {
        this.standardQuestion = standardQuestion;
    }

    public List<FaqRankResult> getHits() {
        return hits;
    }

    public void setHits(List<FaqRankResult> hits) {
        this.hits = hits;
    }
}
