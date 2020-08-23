package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.nlg.ResponseAct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.RESPONSE_DROPDOWN;

public class ResponseExecution implements Execution {

    public static final String KEY_INTENT = "intent";

    @Param(tip = "回复模板意图", component = RESPONSE_DROPDOWN)
    private ContextedString intent;

    private Map<String, ContextedString> map;

    @Override
    public void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        this.map = new HashMap<>(params);
        intent = this.map.remove(KEY_INTENT);
    }

    @Override
    public ExecutionResult execute(Context context) {
        ResponseExecutionResult result = new ResponseExecutionResult();
        ResponseAct act = new ResponseAct(intent.render(context));
        map.forEach((k, v) -> act.put(k, v.render(context)));
        result.setResponseAct(act);
        result.setInstructions(Collections.emptyList());
        return result;
    }

}
