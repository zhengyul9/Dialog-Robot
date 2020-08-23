package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.nlg.ResponseAct;

import java.util.Collections;

@Deprecated
public class UnknownExecution extends ResponseExecution {
    @Override
    public ExecutionResult execute(Context context) {
        ResponseExecutionResult result = new ResponseExecutionResult();
        ResponseAct act = new ResponseAct("UNKNOWN");
        result.setResponseAct(act);
        result.setInstructions(Collections.emptyList());
        return result;
    }
}
