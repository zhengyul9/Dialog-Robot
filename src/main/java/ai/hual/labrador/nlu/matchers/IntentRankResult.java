package ai.hual.labrador.nlu.matchers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IntentRankResult {

    @JsonProperty("sim_question")
    private List<String> simQuestion;

    @JsonProperty("intent")
    private String intent;

    @JsonProperty("score")
    private Double score;

    public IntentRankResult() {
    }

    public IntentRankResult(List<String> simQuestion, Integer intentid, String intent, Double score) {
        this.simQuestion = simQuestion;
        this.intent = intent;
        this.score = score;
    }

    public List<String> getSimQuestion() {
        return simQuestion;
    }

    public void setSimQuestion(List<String> simQuestion) {
        this.simQuestion = simQuestion;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
