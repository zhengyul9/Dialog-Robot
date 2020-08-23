package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.ExecutionResult;
import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.dm.ResponseExecutionResult;

import java.util.Collections;
import java.util.Map;

public class InstructionExecution implements Execution {

    public static final String KEY_TYPE = "type";

    private Map<String, ContextedString> map;

    @Override
    public void setUp(Map<String, ContextedString> map, AccessorRepository accessorRepository) {
        this.map = map;
    }

    @Override
    public ExecutionResult execute(Context context) {
        ResponseExecutionResult result = new ResponseExecutionResult();
        Instruction instruction = new Instruction(map.get(KEY_TYPE).render(context));
        map.keySet().stream()
                .filter(x -> !KEY_TYPE.equals(x))
                .forEach(x -> instruction.addParam(x, map.get(x).render(context)));
        result.setInstructions(Collections.singletonList(instruction));
        return result;
    }

}
