package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.SLOTS_DROPDOWN;

public class SlotIsNullCondition extends Parameterized implements Condition {

    @Param(tip = "指定槽", component = SLOTS_DROPDOWN)
    private ContextedString slot;

    @Override
    public boolean accept(Context context) {
        return context.getSlots().get(slot.render(context)) == null;
    }
}
