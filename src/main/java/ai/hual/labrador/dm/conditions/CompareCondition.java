package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RANGE_DROPDOWN;

public class CompareCondition extends Parameterized implements Condition {

    private Condition comparator;

    /**
     * Slot name in concern.
     */
    @Param(tip = "比较类型", range = {"日期", "数值"}, component = PLAIN_FORM)
    private ContextedString type;

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
        Map<String, ContextedString> params = new HashMap<>();
        params.put("left", left);
        params.put("right", right);
        params.put("operator", operator);
        switch (type.render(context)) {
            case "日期":
                comparator = new CompareDateCondition();
                break;
            case "数值":
                comparator = new CompareDigitCondition();
                break;
            default:
                break;
        }
        if (comparator != null)
            comparator.setUp(params, null);
        return comparator != null && comparator.accept(context);
    }

    /**
     * Tell if the comparison result meet requirement.
     *
     * @param operatorStr   e.g ">", "<", "="
     * @param compareResult e.g 1, -1, 0
     * @return true if operator matched with compareResult
     */
    static boolean operatorSwitch(String operatorStr, int compareResult) {
        switch (operatorStr) {
            case "=":
                return compareResult == 0;
            case ">":
                return compareResult > 0;
            case "<":
                return compareResult < 0;
            default:
                return false;
        }
    }
}
