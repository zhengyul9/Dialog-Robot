package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.dm.java.DialogConfig;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;

import java.util.List;
import java.util.stream.Collectors;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RANGE_DROPDOWN;

public class OverwriteStrategy extends Parameterized implements SlotUpdateStrategy {

    public static final String FROM_QUERY = "query";
    public static final String FROM_CONTEXT = "context";

    public static final String WITH_FIRST_IN_LIST = "first";
    public static final String WITH_LAST_IN_LIST = "last";
    public static final String WITH_ORIGINAL_LIST = "list";

    @Param(tip = "覆盖来源", defaultValue = FROM_QUERY, range = {FROM_QUERY, FROM_CONTEXT}, component = RANGE_DROPDOWN)
    private ContextedString from;

    @Param(tip = "来源的槽名", defaultValue = SLOT_NAME, component = PLAIN_FORM)
    private ContextedString key;

    @Param(tip = "覆盖方式", defaultValue = WITH_FIRST_IN_LIST, range = {WITH_FIRST_IN_LIST, WITH_LAST_IN_LIST, WITH_ORIGINAL_LIST}, component = RANGE_DROPDOWN)
    private ContextedString with;

    @Param(tip = "上轮回复模板为指定值时更新该槽", required = false, component = PLAIN_FORM)
    private ContextedString lastResponseLabel;

    @Override
    public Object update(QueryAct act, Object obj, Context context) {
        String strFrom = from.render(context);
        String strKey = key.render(context);
        String strWith = with.render(context);
        if (lastResponseLabel != null) {
            String strLastResponseLabel = lastResponseLabel.render(context);
            // do not update if last response label doesn't match
            Object lastResponse = context.slotContentByName(DialogConfig.SYSTEM_RESPONSE_NAME);
            if (lastResponse != null && !((ResponseAct) lastResponse).getLabel().contains(strLastResponseLabel)) {
                return obj;
            }
        }

        Object value;
        switch (strFrom) {
            case FROM_QUERY:
            default:
                List<SlotValue> values = act.getSlots().get(strKey);
                value = values.isEmpty() ? null : values.stream().map(SlotValue::getMatched).collect(Collectors.toList());
                break;
            case FROM_CONTEXT:
                value = context.getSlots().get(strKey);
                break;
        }

        // null value
        if (value == null) {
            return obj;
        }
        // not a list
        if (!(value instanceof List)) {
            updateSlotTurn(key.render(context), context);
            return value;
        }

        List list = (List) value;
        // first
        if (strWith.equals(WITH_FIRST_IN_LIST)) {
            if (list.isEmpty())
                return obj;
            else {
                updateSlotTurn(key.render(context), context);
                return list.get(0);
            }
        }
        // last
        if (strWith.equals(WITH_LAST_IN_LIST)) {
            if (list.isEmpty())
                return obj;
            else {
                updateSlotTurn(key.render(context), context);
                return list.get(list.size() - 1);
            }
        }
        // original
        updateSlotTurn(key.render(context), context);
        return value;
    }

}
