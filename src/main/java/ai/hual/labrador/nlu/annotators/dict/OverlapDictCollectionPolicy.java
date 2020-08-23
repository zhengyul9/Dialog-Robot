package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static ai.hual.labrador.utils.QueryActUtils.replaceSuffixDictSlot;

public class OverlapDictCollectionPolicy implements DictCollectionPolicy {
    /**
     * Store all alias in same queryAct, with start and end overlapping.
     *
     * @param dictCollection
     * @param combinationList
     * @param j
     * @param i
     * @param combinationThreshold
     * @return
     */
    @Override
    public List<QueryAct> replaceDictCollection(Collection<Dict> dictCollection, HashMap<Integer, List<QueryAct>> combinationList, int j, int i, String originpQuery, int combinationThreshold) {
        assert !dictCollection.isEmpty();

        // each dict in collection create a new act, with all other dicts as overlap
        QueryAct replacedAct;
        int replaced = 0;
        for (Dict dict : dictCollection) {
            // extract suffix slot of act in list j
            for (QueryAct act : combinationList.get(j)) {
                String pQuery = act.getPQuery();
                // pQuery in act has been changed,retain j,i in new pQuery
                int jUpdatepQuery = j + pQuery.length() - originpQuery.length();
                int iUpdatepQuery = i + pQuery.length() - originpQuery.length();
                replacedAct = replaceSuffixDictSlot(act, dict, jUpdatepQuery, iUpdatepQuery);
                replacedAct = putOverlapDicts(replacedAct, dictCollection, dict);

                combinationList.get(i).add(replacedAct);
                // limit max combination number
                if (replaced++ >= combinationThreshold)
                    return combinationList.get(i);
            }
        }
        return combinationList.get(i);
    }

    /**
     * Put overlap dicts into queryAct.
     *
     * @param replacedAct    the queryAct whose pQuery already replaced by <tt>replaceDict</tt>
     * @param dictCollection dict collection
     * @return queryAct with overlap slots
     */
    private QueryAct putOverlapDicts(QueryAct replacedAct, Collection<Dict> dictCollection, Dict replacedDict) {
        SlotValue replacedSlot = replacedAct.getLastSlot();
        assert replacedSlot != null;
        for (Dict aliasDict : dictCollection) {
            if (aliasDict.equals(replacedDict))
                continue;   // dict used to replace the pQuery should not be added as overlap dict
            if (!replacedDict.getLabel().equals(aliasDict.getLabel()))
                continue; // skip value that not belong to the type of the slot
            String key = aliasDict.getLabel();
            int realStart = replacedSlot.getRealStart();
            int realEnd = replacedSlot.getRealEnd();
            SlotValue aliasSlot = new SlotValue(aliasDict.getWord(), key, "", 0, 0, realStart, realEnd);
            replacedAct.getSlots().put(aliasDict.getLabel(), aliasSlot);
        }
        return replacedAct;
    }
}
