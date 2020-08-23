package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.nlg.ResponseAct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.SLOTS_DROPDOWN;
import static ai.hual.labrador.dm.slotUpdateStrategies.CompleteFormUpdateStrategy.FORM_ENTRY_SLOT_NAME;
import static ai.hual.labrador.dm.slotUpdateStrategies.CompleteFormUpdateStrategy.INTENT_KEY_NAME;

public class AcquireFormEntryResponseExecution extends Parameterized implements Execution {

    private Logger logger = LoggerFactory.getLogger(AcquireFormEntryResponseExecution.class);

    @Param(tip = "表所在的槽名", component = SLOTS_DROPDOWN)
    private ContextedString formSlot;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseExecutionResult execute(Context context) {
        HashMap<String, ContextedString> responseExecutionParams =
                (HashMap<String, ContextedString>) context.slotContentByName(formSlot.getStr());

        ContextedString intent = responseExecutionParams.remove(INTENT_KEY_NAME);
        // not used here, but might be useful if want to know the slot name to be filled
        ContextedString entrySlotName = responseExecutionParams.remove(FORM_ENTRY_SLOT_NAME);
        logger.debug("Expecting slot " + entrySlotName.getStr() + "to be filled, ask with intent: " + intent.getStr());

        ResponseExecutionResult result = new ResponseExecutionResult();
        ResponseAct act = new ResponseAct(intent.render(context));
        responseExecutionParams.forEach((k, v) -> act.put(k, v.render(context)));
        result.setResponseAct(act);
        result.setInstructions(Collections.emptyList());
        return result;
    }
}
