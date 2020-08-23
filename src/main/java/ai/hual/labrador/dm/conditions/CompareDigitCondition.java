package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Condition;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;

public class CompareDigitCondition extends Parameterized implements Condition {

    /**
     * Slot name in concern.
     */
    @Param
    private ContextedString left;

    /**
     * Expected content in String.
     */
    @Param
    private ContextedString right;

    /**
     * Operator.
     */
    @Param
    private ContextedString operator;

    @Override
    public boolean accept(Context context) {
        String leftStr = this.left.render(context);
        String rightStr = this.right.render(context);
        String operatorStr = this.operator.render(context);

        if (leftStr == null || rightStr == null || operatorStr == null)
            return false;

        Double leftDigit = Double.parseDouble(leftStr);
        Double rightDigit = Double.parseDouble(rightStr);

        int compareResult = leftDigit.compareTo(rightDigit);

        return CompareCondition.operatorSwitch(operatorStr, compareResult);
    }
}
