package ai.hual.labrador.nlu.resort;


import ai.hual.labrador.nlu.matchers.IntentClassifierResult;
import ai.hual.labrador.nlu.resort.intent.IntentScores;

import java.util.List;

public interface IntentResort {
    IntentClassifierResult resort(List<IntentScores> intentScoresList);
}
