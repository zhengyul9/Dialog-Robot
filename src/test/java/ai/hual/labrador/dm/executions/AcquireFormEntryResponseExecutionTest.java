package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.dm.slotUpdateStrategies.CompleteFormUpdateStrategy;
import ai.hual.labrador.nlu.QueryAct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.utils.loadFormEntriesJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
class AcquireFormEntryResponseExecutionTest {

    private static AccessorRepository accessorRepository;
    private static String formEntriesJson;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
        formEntriesJson = loadFormEntriesJson();
    }

    @Test
    void formNotCompletedReturnResponseTest() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("form_A", null);
        slots.put("x", "x_content");
        slots.put("y", "y_content");
        slots.put("z", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("form", new ContextedString(formEntriesJson));

        QueryAct act = new QueryAct("input");

        CompleteFormUpdateStrategy completeFormUpdateStrategy = new CompleteFormUpdateStrategy();
        completeFormUpdateStrategy.setUp("form_A", params, accessorRepository);
        Object updated = completeFormUpdateStrategy.update(act, null, context);
        slots.put("form_A", updated);

        HashMap<String, ContextedString> executionParams = new HashMap<>();
        executionParams.put("formSlot", new ContextedString("form_A"));

        AcquireFormEntryResponseExecution execution = new AcquireFormEntryResponseExecution();
        execution.setUp(executionParams, accessorRepository);

        ResponseExecutionResult result = execution.execute(context);
        assertEquals("问z", result.getResponseAct().getLabel());
    }
}