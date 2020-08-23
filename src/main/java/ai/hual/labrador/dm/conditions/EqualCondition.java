package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.Objects;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;

public class EqualCondition extends Parameterized implements Condition {

    @Param(tip = "左边的值", component = PLAIN_FORM)
    private ContextedString left;

    @Param(tip = "右边的值", component = PLAIN_FORM)
    private ContextedString right;

    @Override
    public boolean accept(Context context) {
        return Objects.equals(left.render(context), right.render(context));
    }

}
