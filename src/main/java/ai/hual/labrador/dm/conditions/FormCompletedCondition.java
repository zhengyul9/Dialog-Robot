package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.exceptions.DMException;

import static ai.hual.labrador.dm.slotUpdateStrategies.CompleteFormUpdateStrategy.FORM_COMPLETED;

public class FormCompletedCondition extends Parameterized implements Condition {

    @Param
    private ContextedString slot;

    /**
     * Return true if the form defined by slot is completed,
     * which is judged by slot's content.
     *
     * @param context context
     * @return true if slot's content is String defined in <tt>FORM_COMPLETED</tt>
     */
    @Override
    public boolean accept(Context context) {
        String slotName = slot.render(context);
        if (!context.getSlots().keySet().contains(slotName))
            throw new DMException("There's no form slot named: " + slotName);
        Object content = context.slotContentByName(slotName);
        return content != null && content.equals(FORM_COMPLETED);
    }
}
