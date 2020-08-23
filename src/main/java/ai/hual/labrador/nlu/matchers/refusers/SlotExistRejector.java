package ai.hual.labrador.nlu.matchers.refusers;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.matchers.IntentMatcherRejector;

import java.util.List;

public class SlotExistRejector implements IntentMatcherRejector {

    /**
     * Refuse if queryAct has slot.
     *
     * @param queryActs hyps
     * @return
     */
    @Override
    public boolean reject(List<QueryAct> queryActs) {
        assert !queryActs.isEmpty();
        QueryAct bestAct = queryActs.get(0);
        return !bestAct.getSlots().isEmpty();
    }
}
