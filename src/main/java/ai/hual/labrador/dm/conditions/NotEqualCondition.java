package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Context;

public class NotEqualCondition extends EqualCondition {

    /**
     * Accept if slot content not equals right param.
     *
     * @param context context
     * @return true or false
     */
    @Override
    public boolean accept(Context context) {
        return !super.accept(context);
    }

}
