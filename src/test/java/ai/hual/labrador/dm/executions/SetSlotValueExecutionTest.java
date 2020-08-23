package ai.hual.labrador.dm.executions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.utils.LoggerHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetSlotValueExecutionTest {

    private static AccessorRepository accessorRepository;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
    }

    @Test
    void clearSlotValueTest() {
        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("意图", "订票");
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("意图"));
        params.put("value", new ContextedString("null"));

        SetSlotValueExecution setSlotValueExecution = new SetSlotValueExecution();
        setSlotValueExecution.setUp(params, accessorRepository);
        setSlotValueExecution.execute(context);

        assertEquals(null, context.slotContentByName("意图"));
    }

    @Test
    void setSlotValueAsStringTest() {
        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("意图", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("意图"));
        params.put("value", new ContextedString("订车票"));

        SetSlotValueExecution setSlotValueExecution = new SetSlotValueExecution();
        setSlotValueExecution.setUp(params, accessorRepository);
        setSlotValueExecution.execute(context);

        assertEquals("订车票", context.slotContentByName("意图"));
    }
}