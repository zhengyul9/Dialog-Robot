package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.HashMap;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_STATE_REPEATEDNESS_NAME;

public class StopRepeatednessCondition extends Parameterized implements Condition {

    @Param
    ContextedString state;

    @Param
    ContextedString times;

    @Override
    @SuppressWarnings("unchecked")
    public boolean accept(Context context) {
        String stateName = state.render(context);
        HashMap<String, Integer> repeatednessMap =
                (HashMap<String, Integer>) context.slotContentByName(SYSTEM_STATE_REPEATEDNESS_NAME);
        assert repeatednessMap != null;
        return repeatednessMap.containsKey(stateName) && repeatednessMap.get(stateName) >= Integer.parseInt(times.getStr());
    }
}
