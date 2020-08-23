package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.QueryAct;

import java.util.List;

public interface IntentClassifierAccessor {

    IntentClassifierResult handleIntentClassification(List<QueryAct> queryActs);
}
