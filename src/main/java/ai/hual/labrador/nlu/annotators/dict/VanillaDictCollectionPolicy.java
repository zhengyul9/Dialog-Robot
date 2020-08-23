package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.QueryAct;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static ai.hual.labrador.utils.QueryActUtils.replaceSuffixDictSlot;

public class VanillaDictCollectionPolicy implements DictCollectionPolicy {
    @Override
    public List<QueryAct> replaceDictCollection(Collection<Dict> dictCollection, HashMap<Integer, List<QueryAct>> combinationList, int j, int i, String oringinPQuery, int combinationThreshold) {
        // split each label of dict into different queryAct
        for (Dict dict : dictCollection) {
            // extract suffix slot of act in list j
            for (QueryAct act : combinationList.get(j)) {
                String pQuery = act.getPQuery();
                // pQuery in act has been changed,retain j,i in new pQuery
                int jUpdatepQuery = j + pQuery.length() - oringinPQuery.length();
                int iUpdatepQuery = i + pQuery.length() - oringinPQuery.length();
                QueryAct replacedAct = replaceSuffixDictSlot(act, dict, jUpdatepQuery, iUpdatepQuery);
                combinationList.get(i).add(replacedAct);
                // limit max combination number
                if (combinationList.get(i).size() >= combinationThreshold)
                    return combinationList.get(i);
            }
        }
        return combinationList.get(i);
    }
}
