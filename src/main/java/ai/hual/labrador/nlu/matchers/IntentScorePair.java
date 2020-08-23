package ai.hual.labrador.nlu.matchers;

public class IntentScorePair {
    private String intent;
    private double score;

    public IntentScorePair() {
    }

    public IntentScorePair(IntentScorePair pair) {
        this.intent = pair.getIntent();
        this.score = pair.getScore();
    }

    public IntentScorePair(String intent, double score) {
        this.intent = intent;
        this.score = score;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString() {
        return intent + "=" + score;
    }
}

