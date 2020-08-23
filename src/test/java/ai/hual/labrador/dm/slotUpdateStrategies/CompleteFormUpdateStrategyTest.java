package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.nlu.QueryAct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.utils.loadFormEntriesJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
class CompleteFormUpdateStrategyTest {

    private static AccessorRepository accessorRepository;
    private static String formEntriesJson;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
        formEntriesJson = loadFormEntriesJson();
    }

    @Test
    void testGet() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("x", "10");
        slots.put("y", 10);
        context.setSlots(slots);

        assertEquals("10", context.get("x"));
        assertEquals(Integer.valueOf(10), context.<Integer>get("y"));
    }

    @Test
    void completedTest() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("x", "x_content");
        slots.put("y", "y_content");
        slots.put("z", "z_content");
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("form", new ContextedString(formEntriesJson));

        QueryAct act = new QueryAct("input");

        CompleteFormUpdateStrategy completeFormUpdateStrategy = new CompleteFormUpdateStrategy();
        completeFormUpdateStrategy.setUp("formA", params, accessorRepository);
        String updated = (String) completeFormUpdateStrategy.update(act, null, context);

        assertEquals(CompleteFormUpdateStrategy.FORM_COMPLETED, updated);
    }

    @Test
    void twoFilledTest() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("x", "x_content");
        slots.put("y", "y_content");
        slots.put("z", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("form", new ContextedString(formEntriesJson));

        QueryAct act = new QueryAct("input");

        CompleteFormUpdateStrategy completeFormUpdateStrategy = new CompleteFormUpdateStrategy();
        completeFormUpdateStrategy.setUp("formA", params, accessorRepository);
        HashMap<String, ContextedString> updated =
                (HashMap<String, ContextedString>) completeFormUpdateStrategy.update(act, null, context);

        assertEquals(4, updated.keySet().size());
        assertEquals("问z", updated.get("intent").getStr());
        assertEquals("{{x}}", updated.get("x").getStr());
        assertEquals("{{y}}", updated.get("y").getStr());
        assertEquals("z", updated.get("entrySlotName").getStr());
    }

    @Test
    void noneFilledTest() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("x", null);
        slots.put("y", null);
        slots.put("z", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("form", new ContextedString(formEntriesJson));

        QueryAct act = new QueryAct("input");

        CompleteFormUpdateStrategy completeFormUpdateStrategy = new CompleteFormUpdateStrategy();
        completeFormUpdateStrategy.setUp("formA", params, accessorRepository);
        HashMap<String, ContextedString> updated =
                (HashMap<String, ContextedString>) completeFormUpdateStrategy.update(act, null, context);

        assertEquals(2, updated.keySet().size());
        assertEquals("问x", updated.get("intent").getStr());
        assertEquals("x", updated.get("entrySlotName").getStr());
    }
}