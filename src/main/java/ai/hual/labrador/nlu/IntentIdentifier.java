package ai.hual.labrador.nlu;

import java.util.List;

public interface IntentIdentifier {
    NLUResult identifyIntent(List<QueryAct> queryActs);
}
