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

class SlotNotNullConditionTest {

    private static AccessorRepository accessorRepository;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
    }

    @Test
    void slotNotNullTest() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("日期", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("slot", new ContextedString("日期"));

        SlotNotNullCondition slotNotNullCondition = new SlotNotNullCondition();
        slotNotNullCondition.setUp(params, accessorRepository);
        boolean accepted = slotNotNullCondition.accept(context);

        assertFalse(accepted);
    }

}