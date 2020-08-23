package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;

import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_INTENT_NAME;

public class IntentCondition implements Condition {
    /**
     * Type of condition
     */
    private String type;

    /**
     * Expected intent in String.
     */
    private ContextedString intent;

    @Override
    public void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        assert params.keySet().size() == 1;
        this.type = params.keySet().iterator().next();
        this.intent = params.get(type);
    }

    /**
     * Accept if slot content equals right param.
     *
     * @param context context
     * @return true or false
     */
    @Override
    public boolean accept(Context context) {
        if (context.slotContentByName(SYSTEM_INTENT_NAME) == null)
            return false;
        if (type.equals("eq"))
            return context.slotContentByName(SYSTEM_INTENT_NAME).equals(this.intent.render(context));
        if (type.equals("not_eq"))
            return !context.slotContentByName(SYSTEM_INTENT_NAME).equals(this.intent.render(context));
        else
            return false;
    }
}
