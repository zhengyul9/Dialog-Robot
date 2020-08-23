package ai.hual.labrador.nlu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Result of nlu.
 */
public class NLUResult {
    /**
     * Name of the  {@link ai.hual.labrador.nlu.matchers.IntentMatcher} who identified
     * the intent with highest score.
     */
    private String chosenMatcher;
    /**
     * Score of the chosen matcher.
     */
    private double chosenScore;
    /**
     * Key is name of {@link ai.hual.labrador.nlu.matchers.IntentMatcher},
     * value is the matcher's result.
     */
    private Map<String, List<QueryAct>> matcherResultMap;

    /**
     * QueryActs before go through intentIdentifier, usually they are the result of
     * {@link ai.hual.labrador.nlu.annotators.PhraseAnnotator}
     */
    private List<QueryAct> queryActsBeforeIntent;

    public NLUResult() {
    }

    public NLUResult(String chosenMatcher, double chosenScore, Map<String, List<QueryAct>> matcherResultMap, List<QueryAct> queryActs) {
        this.chosenMatcher = chosenMatcher;
        this.chosenScore = chosenScore;
        this.matcherResultMap = matcherResultMap;
        this.queryActsBeforeIntent = queryActs;
    }

    public Map<String, List<QueryAct>> getMatcherResultMap() {
        return matcherResultMap;
    }

    public void setMatcherResultMap(Map<String, List<QueryAct>> matcherResultMap) {
        this.matcherResultMap = matcherResultMap;
    }

    public String getChosenMatcher() {
        return chosenMatcher;
    }

    public void setChosenMatcher(String chosenMatcher) {
        this.chosenMatcher = chosenMatcher;
    }

    public List<QueryAct> retrieveHyps() {
        if (this.chosenMatcher == null) // all matcher refused
            return new ArrayList<>();
        return matcherResultMap.get(this.chosenMatcher);
    }

    public String toString() {
        String result = "";
        result += "Chosen matcher: " + chosenMatcher + ", ";
        result += matcherResultMap;
        return result;
    }

    public List<QueryAct> getQueryActsBeforeIntent() {
        return queryActsBeforeIntent;
    }

    public void setQueryActsBeforeIntent(List<QueryAct> queryActsBeforeIntent) {
        this.queryActsBeforeIntent = queryActsBeforeIntent;
    }

    public double getChosenScore() {
        return chosenScore;
    }

    public void setChosenScore(double chosenScore) {
        this.chosenScore = chosenScore;
    }
}
