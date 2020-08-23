package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DateUtils.Date;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RANGE_DROPDOWN;

public class CompareDateCondition extends Parameterized implements Condition {

    /**
     * Slot name in concern.
     */
    @Param(tip = "左边的值", component = PLAIN_FORM)
    private ContextedString left;

    /**
     * Expected content in String.
     */
    @Param(tip = "右边的值", component = PLAIN_FORM)
    private ContextedString right;

    /**
     * Operator.
     */
    @Param(tip = "比较符", range = {"=", ">", "<"}, component = RANGE_DROPDOWN)
    private ContextedString operator;

    @Override
    public boolean accept(Context context) {
        String leftStr = this.left.render(context);
        String rightStr = this.right.render(context);
        String operatorStr = this.operator.render(context);

        Date leftDate = DateUtils.strToDate(leftStr);
        Date rightDate = DateUtils.strToDate(rightStr);
        if (leftDate == null || rightDate == null)
            return false;
        int compareResult = leftDate.compareTo(rightDate);

        return CompareCondition.operatorSwitch(operatorStr, compareResult);
    }
}
