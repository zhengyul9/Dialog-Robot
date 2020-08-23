package ai.hual.labrador.nlu.resort.intent;

import java.util.ArrayList;
import java.util.List;

public class IntentGroup {
    private String intent;
    private String query;
    private double score;
    private List<String> simQuestion;

    public IntentGroup() {
    }

    public IntentGroup(String intent, String query) {
        this.intent = intent;
        this.query = query;
        this.score = 0;
        this.simQuestion = new ArrayList<>();
    }

    public IntentGroup(String intent, double score) {
        this.intent = intent;
        this.score = score;
    }

    public void addScore(double score) {
        this.score += score;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<String> getSimQuestion() {
        return simQuestion;
    }

    public void setSimQuestion(List<String> simQuestion) {
        this.simQuestion = simQuestion;
    }
}
