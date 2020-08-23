package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.QueryAct;

import java.util.List;

public interface CombinationBFS {
    List<QueryAct> combinationBFS(List<QueryAct> queryActs);
}
