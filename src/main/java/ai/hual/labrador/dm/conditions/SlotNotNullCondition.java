package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.Optional;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.SLOTS_DROPDOWN;

public class SlotNotNullCondition extends Parameterized implements Condition {

    @Param(tip = "指定槽", component = SLOTS_DROPDOWN)
    private ContextedString slot;

    @Override
    public boolean accept(Context context) {
        // not null and not empty list
        return Optional.ofNullable(context.getSlots().get(slot.render(context))).map(c -> !c.toString().equals("[]")).orElse(false);
    }
}
