package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.List;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RANGE_DROPDOWN;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.TAG_FORM;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_INTENT_NAME;

public class IntentJudgeCondition extends Parameterized implements Condition {
    private static final String EQUAL = "EQUAL";
    private static final String NOT_EQUAL = "NOT_EQUAL";
    /**
     * Type of condition
     */
    @Param(tip = "相等或不等", range = {EQUAL, NOT_EQUAL}, component = RANGE_DROPDOWN)
    private ContextedString type;

    /**
     * Expected intent in String.
     */
    @Param(tip = "指定的意图，可多选", component = TAG_FORM)
    private ContextedString intent;

    /**
     * Accept if slot content equals right param.
     *
     * @param context context
     * @return true or false
     */
    @Override
    public boolean accept(Context context) {
        String strType = type.render(context);
        List<String> strIntent = intent.renderToList(context);
        if (context.slotContentByName(SYSTEM_INTENT_NAME) == null)
            return false;
        if (strType.equals(EQUAL))
            return strIntent.contains(context.slotContentByName(SYSTEM_INTENT_NAME).toString());
        if (strType.equals(NOT_EQUAL))
            return !strIntent.contains(context.slotContentByName(SYSTEM_INTENT_NAME).toString());
        else
            return false;
    }
}
