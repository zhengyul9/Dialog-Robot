package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.constants.SystemIntents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("classifierIntentMatcher")
public class ClassifierIntentMatcher implements IntentMatcher {

    public static final String INTENT_ANSWER_SLOT_KEY = "intentAnswer";

    private IntentClassifierAccessor intentClassifierAccessor;

    public ClassifierIntentMatcher(@Autowired AccessorRepository accessorRepository) {
        if (accessorRepository.getIntentClassifierAccessor() == null)
            throw new NLUException("IntentClassifierAccessor not exist");
        intentClassifierAccessor = accessorRepository.getIntentClassifierAccessor();
    }

    @Override
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {
        List<QueryAct> queryActsCopy = queryActs.stream().map(QueryAct::new).collect(Collectors.toList());
        IntentClassifierResult classifierResult = intentClassifierAccessor.handleIntentClassification(queryActsCopy);
        if (classifierResult != null && classifierResult.getIntents() != null && !classifierResult.getIntents().isEmpty()) {
            IntentScorePair bestHit = classifierResult.getIntents().get(0);
            QueryAct correspondAct = queryActsCopy.stream()
                    .filter(act -> act.getPQuery().equals(classifierResult.getQuery()))
                    .findFirst()
                    .orElse(null);
            assert correspondAct != null;
            if (bestHit.getIntent() == null || classifierResult.getConfidence() == 0) {
                correspondAct.setIntent(SystemIntents.CLASSIFIER_UNKNOWN_INTENT);
                correspondAct.setScore(0);
            } else {
                correspondAct.setIntent(bestHit.getIntent());
                correspondAct.setScore(classifierResult.getConfidence());
            }
            correspondAct.getSlots().put(INTENT_ANSWER_SLOT_KEY, new SlotValue(classifierResult));
            return Collections.singletonList(correspondAct);
        } else {
            queryActsCopy.forEach(act -> {
                if (act.getIntent() == null) {
                    act.setIntent(SystemIntents.UNKNOWN);
                    act.setScore(0);
                }
            });
            queryActsCopy.get(0).getSlots().put(INTENT_ANSWER_SLOT_KEY, null);
            return queryActsCopy;
        }
    }
}