package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.nlu.QueryAct;

import java.util.Map;

public class IntentUpdateStrategy implements SlotUpdateStrategy {

    private String slotName;

    @Override
    public void setUp(String slotName, Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        this.slotName = slotName;
    }

    @Override
    public Object update(QueryAct act, Object obj, Context context) {
        String intent = act.getIntent();
        if (intent != null) {
            updateSlotTurn(slotName, context);
            return intent;
        }
        return obj; // if does not have intent, maintain unmodified
    }
}
