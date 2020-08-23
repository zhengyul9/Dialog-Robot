package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.QueryAct;

import java.util.List;

public interface IntentMatcherRejector {
    /**
     * Tell if should be refused in a matcher by hyps.
     *
     * @param queryActs hyps
     * @return true if should be reject
     */
    boolean reject(List<QueryAct> queryActs);
}
