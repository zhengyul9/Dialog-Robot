package ai.hual.labrador.nlu.resort.faq;

import ai.hual.labrador.nlg.answer.Answer;

import java.util.ArrayList;
import java.util.List;

public class FaqGroup {
    /**
     * Qaid of this faq group.
     */
    private int qaid;
    /**
     * Standard question of the faq group.
     */
    private String question;
    /**
     * Answer of this faq group.
     */
    private List<Answer> answer;
    /**
     * Category.
     */
    private String category;
    /**
     * Questions matched by query.
     */
    private List<String> simQuestions;
    /**
     * Qid of simQuestions.
     */
    private List<Integer> simQids;
    /**
     * Matching score of simQuestions.
     */
    private List<Double> simScores;
    /**
     * Score of this faq group, which is the max simQuestion score.
     */
    private double score;

    public FaqGroup() {
    }

    FaqGroup(int qaid) {
        this.qaid = qaid;
        this.question = null;
        this.answer = null;
        this.simQuestions = new ArrayList<>();
        this.simQids = new ArrayList<>();
        this.simScores = new ArrayList<>();
        this.score = 0;
    }

    void addQuestion(String q, int qid, double s) {
        simQuestions.add(q);
        simQids.add(qid);
        simScores.add(s);
        if (s > score)
            score = s;
    }

    public int getQaid() {
        return qaid;
    }

    public void setQaid(int qaid) {
        this.qaid = qaid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Answer> getAnswer() {
        return answer;
    }

    public void setAnswer(List<Answer> answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSimQuestions() {
        return simQuestions;
    }

    public void setSimQuestions(List<String> simQuestions) {
        this.simQuestions = simQuestions;
    }

    public List<Double> getSimScores() {
        return simScores;
    }

    public void setSimScores(List<Double> simScores) {
        this.simScores = simScores;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<Integer> getSimQids() {
        return simQids;
    }

    public void setSimQids(List<Integer> simQids) {
        this.simQids = simQids;
    }
}
