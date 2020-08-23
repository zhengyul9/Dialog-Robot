package ai.hual.labrador.dm;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.nlu.QueryAct;

import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURNS_MAINTAIN_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURN_NAME;

public interface SlotUpdateStrategy {

    String SLOT_NAME = "{{slot.name}}";

    /**
     * Set up the strategy using slotName and context.
     *
     * @param slotName           name of the slot
     * @param params             params needed
     * @param accessorRepository a repository of accessors providing accessors
     */
    void setUp(String slotName, Map<String, ContextedString> params, AccessorRepository accessorRepository);

    /**
     * Update slot by QueryAct from hyps and original slot object.
     *
     * @param act      the QueryAct
     * @param original original object in context of this slot
     * @param context  context
     * @return The new value of the slot
     */
    Object update(QueryAct act, Object original, Context context);

    /**
     * Set the slot's update turn to current turn in <tt>SYSTEM_TURN_NAME</tt>,
     * and put into system maintained slot <tt>SYSTEM_TURNS_MAINTAIN_NAME</tt>.
     *
     * @param slotName name of the slot
     * @param context  context
     */
    @SuppressWarnings("unchecked")
    default void updateSlotTurn(String slotName, Context context) {
        HashMap<String, Integer> turnsMap =
                (HashMap<String, Integer>) context.slotContentByName(SYSTEM_TURNS_MAINTAIN_NAME);
        turnsMap.put(slotName, (Integer) context.slotContentByName(SYSTEM_TURN_NAME));
    }

}
