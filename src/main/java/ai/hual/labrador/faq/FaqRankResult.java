package ai.hual.labrador.faq;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FaqRankResult {

    @JsonProperty("sim_question")
    private List<String> simQuestion;

    @JsonProperty("question")
    private String question;

    @JsonProperty("qaid")
    private Integer qaid;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("category")
    private String category;

    @JsonProperty("score")
    private Double score;

    public FaqRankResult() {
    }

    public FaqRankResult(int qaid, String question, String answer, String category, double score, List<String> simQuestion) {
        this.qaid = qaid;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.score = score;
        this.simQuestion = simQuestion;
    }

    public List<String> getSimQuestion() {
        return simQuestion;
    }

    public void setSimQuestion(List<String> simQuestion) {
        this.simQuestion = simQuestion;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getQaid() {
        return qaid;
    }

    public void setQaid(Integer qaid) {
        this.qaid = qaid;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}