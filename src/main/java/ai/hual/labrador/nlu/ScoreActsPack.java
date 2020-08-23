package ai.hual.labrador.nlu;

import java.util.List;

/**
 * Resulting score and the corresponding queryActs with intent, identified by
 * {@link ai.hual.labrador.nlu.matchers.IntentMatcher}.
 */
public class ScoreActsPack {
    /**
     * queryActs with intent
     */
    List<QueryAct> queryActs;
    /**
     * normalized score of this matcher
     */
    double score;

    public ScoreActsPack() {
    }

    public ScoreActsPack(List<QueryAct> queryActs, double score) {
        this.queryActs = queryActs;
        this.score = score;
    }

    public List<QueryAct> getQueryActs() {
        return queryActs;
    }

    public void setQueryActs(List<QueryAct> queryActs) {
        this.queryActs = queryActs;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
