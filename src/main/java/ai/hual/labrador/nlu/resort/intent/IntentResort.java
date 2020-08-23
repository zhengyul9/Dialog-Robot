package ai.hual.labrador.nlu.resort.intent;

import java.util.List;

public interface IntentResort {
    List<IntentGroup> resort(List<IntentScores> intentScoresList);
}
