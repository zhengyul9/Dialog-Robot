package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.exceptions.DMException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_STATE_REPEATEDNESS_NAME;

public class RepeatednessExceedPickFromSlotListCondition extends Parameterized implements Condition {

    @Param
    private ContextedString state;

    @Param
    private ContextedString times;

    @Param
    private ContextedString fromSlot;

    @Param
    private ContextedString toSlot;

    @Param(required = false)
    private ContextedString as;

    @Param(required = false)
    private ContextedString ordinal;

    @Override
    @SuppressWarnings("unchecked")
    public boolean accept(Context context) {

        String stateName = state.render(context);
        HashMap<String, Integer> repeatednessMap =
                (HashMap<String, Integer>) context.slotContentByName(SYSTEM_STATE_REPEATEDNESS_NAME);
        assert repeatednessMap != null;
        if (!repeatednessMap.containsKey(stateName) || repeatednessMap.get(stateName) < Integer.parseInt(times.getStr()))
            return false;

        String strFromSlot = fromSlot.render(context);
        String strToSlot = toSlot.render(context);

        List<Object> fromSlotContent = (List<Object>) context.slotContentByName(strFromSlot);
        int intOrdinal;
        if (ordinal == null) {
            // pick randomly
            Random random = new Random();
            intOrdinal = random.ints(0, fromSlotContent.size()).findFirst().orElse(0);
        } else
            intOrdinal = Integer.parseInt(ordinal.render(context));

        if (fromSlotContent == null)
            throw new DMException(strFromSlot + " slot has no content");
        String destinationType = context.slotTypeByName(strToSlot);
        String sourceType = context.slotTypeByName(strFromSlot);
        if (sourceType != null && destinationType != null && !destinationType.equals(sourceType))
            throw new DMException("Source slot has type: " + sourceType + ", but destination slot has type: " + destinationType);
        // choose cast type
        String castType;
        if (sourceType != null)
            castType = sourceType;
        else if (destinationType != null)
            castType = destinationType;
        else castType = Object.class.getName();
        try {
            if (as != null && as.getStr().equals("list"))
                context.putSlotContent(strToSlot, Collections.singletonList(Class.forName(castType).cast(fromSlotContent.get(intOrdinal))));
            else
                context.putSlotContent(strToSlot, Class.forName(castType).cast(fromSlotContent.get(intOrdinal)));
        } catch (ClassNotFoundException e) {
            throw new DMException("Can't cast the " + intOrdinal + "th element from " + strFromSlot
                    + "to slot " + strToSlot);
        }
        return true;
    }
}
