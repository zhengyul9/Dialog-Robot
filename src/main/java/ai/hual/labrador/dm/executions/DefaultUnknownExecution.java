package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.constants.SystemIntents;

import java.util.ArrayList;

public class DefaultUnknownExecution extends ResponseExecution {

    @Override
    public ExecutionResult execute(Context context) {
        ResponseExecutionResult result = new ResponseExecutionResult();
        ResponseAct act = new ResponseAct(SystemIntents.UNKNOWN);
        result.setResponseAct(act);
        result.setInstructions(new ArrayList<>());
        result.getInstructions().add(new Instruction("msginfo_kb_na"));
        return result;
    }
}
