package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotUpdatedThisTurnTest {

    private static AccessorRepository accessorRepository;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
    }

    @Test
    void testAccept() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("sys.turn", 2);
        Map<String, Integer> turns = new HashMap<>();
        turns.put("数字", 2);
        slots.put("sys.turns", turns);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("数字"));

        SlotUpdatedThisTurn condition = new SlotUpdatedThisTurn();
        condition.setUp(params, accessorRepository);
        boolean accepted = condition.accept(context);

        assertTrue(accepted);
    }

    @Test
    void testPreviousTurnUnaccepted() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("sys.turn", 2);
        Map<String, Integer> turns = new HashMap<>();
        turns.put("数字", 1);
        slots.put("sys.turns", turns);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("数字"));

        SlotUpdatedThisTurn condition = new SlotUpdatedThisTurn();
        condition.setUp(params, accessorRepository);
        boolean accepted = condition.accept(context);

        assertFalse(accepted);
    }

    @Test
    void testNoTurnUnaccepted() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("sys.turn", 2);
        Map<String, Integer> turns = new HashMap<>();
        slots.put("sys.turns", turns);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("数字"));

        SlotUpdatedThisTurn condition = new SlotUpdatedThisTurn();
        condition.setUp(params, accessorRepository);
        boolean accepted = condition.accept(context);

        assertFalse(accepted);
    }
}