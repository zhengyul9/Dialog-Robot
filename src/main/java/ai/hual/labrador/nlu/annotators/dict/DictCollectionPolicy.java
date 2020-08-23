package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.QueryAct;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public interface DictCollectionPolicy {
    List<QueryAct> replaceDictCollection(Collection<Dict> dictCollection, HashMap<Integer, List<QueryAct>> combinationList,
                                         int j, int i, String oringinpQuery, int combinationThreshold);
}
