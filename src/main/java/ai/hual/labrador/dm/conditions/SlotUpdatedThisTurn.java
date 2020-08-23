package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.Map;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.SLOTS_DROPDOWN;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURNS_MAINTAIN_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURN_NAME;

public class SlotUpdatedThisTurn extends Parameterized implements Condition {

    @Param(tip = "指定槽", component = SLOTS_DROPDOWN)
    private ContextedString slot;

    @Override
    public boolean accept(Context context) {
        String slotName = slot.render(context);
        Map<String, Integer> turnMap =
                (Map<String, Integer>) context.slotContentByName(SYSTEM_TURNS_MAINTAIN_NAME);
        if (!turnMap.containsKey(slotName))
            return false;
        else
            return turnMap.get(slotName) == (int) context.slotContentByName(SYSTEM_TURN_NAME);
    }
}
