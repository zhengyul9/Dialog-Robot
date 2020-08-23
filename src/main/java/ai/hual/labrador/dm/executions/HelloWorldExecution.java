package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;

import java.util.Map;

public class HelloWorldExecution implements Execution {

    private Map<String, ContextedString> params;

    @Override
    public void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        this.params = params;
    }

    @Override
    public ExecutionResult execute(Context context) {
        System.out.println(this.params.get("say"));
        return null;
    }
}
