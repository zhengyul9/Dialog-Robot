package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

public class SetSlotValueExecution extends Parameterized implements Execution {

    @Param(tip = "要赋值的槽名")
    private ContextedString slot;

    @Param(tip = "要赋的值")
    private ContextedString value;

    @Override
    public ExecutionResult execute(Context context) {
        String slotName = slot.render(context);
        String valueStr = value.render(context);
        if (valueStr.equals("null"))
            context.putSlotContent(slotName, null);
        else
            context.putSlotContent(slotName, valueStr);
        return null;
    }
}
