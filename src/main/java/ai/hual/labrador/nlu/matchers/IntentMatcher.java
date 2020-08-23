package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.QueryAct;

import java.util.List;

public interface IntentMatcher {
    int MATCHER_SCORE_UPPER_BOUND = 1;

    List<QueryAct> matchIntent(List<QueryAct> queryActs);
}
