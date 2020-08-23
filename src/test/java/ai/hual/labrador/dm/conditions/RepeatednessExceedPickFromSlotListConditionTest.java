package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.utils.DateUtils.Date;
import ai.hual.labrador.utils.LoggerHashMap;
import ai.hual.labrador.utils.TimeUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_STATE_REPEATEDNESS_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepeatednessExceedPickFromSlotListConditionTest {

    private static RepeatednessExceedPickFromSlotListCondition condition;

    @BeforeAll
    static void setUp() {
        condition = new RepeatednessExceedPickFromSlotListCondition();
    }

    @Test
    void testRandomOrdinalUnacceptedCauseOfConflictTypes() {
        Map<String, ContextedString> params = new HashMap<>();
        params.put("state", new ContextedString("leaf_state"));
        params.put("times", new ContextedString("2"));
        params.put("fromSlot", new ContextedString("fromSlot"));
        params.put("toSlot", new ContextedString("toSlot"));
        condition.setUp(params, null);

        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("fromSlot", Arrays.asList(new Date(2017, 1, 1), new Date(2018, 1, 1), new Date(2019, 1, 1)));
        slots.put("toSlot", null);
        Map<String, Integer> repeatedness = new HashMap<>();
        repeatedness.put("leaf_state", 2);
        slots.put(SYSTEM_STATE_REPEATEDNESS_NAME, repeatedness);
        context.setSlots(slots);

        Map<String, String> types = new HashMap<>();
        types.put("fromSlot", Date.class.getName());
        types.put("toSlot", TimeUtils.Time.class.getName());
        context.setTypes(types);

        assertThrows(DMException.class, () -> condition.accept(context),
                "Source slot has type: ai.hual.labrador.utils.DateUtils$Date, but destination slot has type: ai.hual.labrador.utils.TimeUtils$Time");
    }

    @Test
    void testRandomOrdinalUnacceptedCauseOfTimeNotEnough() {
        Map<String, ContextedString> params = new HashMap<>();
        params.put("state", new ContextedString("leaf_state"));
        params.put("times", new ContextedString("2"));
        params.put("fromSlot", new ContextedString("fromSlot"));
        params.put("toSlot", new ContextedString("toSlot"));
        condition.setUp(params, null);

        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("fromSlot", Arrays.asList(new Date(2017, 1, 1), new Date(2018, 1, 1), new Date(2019, 1, 1)));
        slots.put("toSlot", null);
        Map<String, Integer> repeatedness = new HashMap<>();
        repeatedness.put("leaf_state", 1);
        slots.put(SYSTEM_STATE_REPEATEDNESS_NAME, repeatedness);
        context.setSlots(slots);

        Map<String, String> types = new HashMap<>();
        types.put("fromSlot", Date.class.getName());
        context.setTypes(types);

        assertFalse(condition.accept(context));
    }

    @Test
    void testRandomOrdinalAccept() {
        Map<String, ContextedString> params = new HashMap<>();
        params.put("state", new ContextedString("leaf_state"));
        params.put("times", new ContextedString("2"));
        params.put("fromSlot", new ContextedString("fromSlot"));
        params.put("toSlot", new ContextedString("toSlot"));
        condition.setUp(params, null);

        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("fromSlot", Arrays.asList(new Date(2017, 1, 1), new Date(2018, 1, 1), new Date(2019, 1, 1)));
        slots.put("toSlot", null);
        Map<String, Integer> repeatedness = new HashMap<>();
        repeatedness.put("leaf_state", 2);
        slots.put(SYSTEM_STATE_REPEATEDNESS_NAME, repeatedness);
        context.setSlots(slots);

        Map<String, String> types = new HashMap<>();
        types.put("fromSlot", Date.class.getName());
        context.setTypes(types);

        assertTrue(condition.accept(context));
        int year = ((Date) context.slotContentByName("toSlot")).year;
        assertTrue(year >= 2017 && year <= 2019);
    }

    @Test
    void testFixOrdinalAccept() {
        Map<String, ContextedString> params = new HashMap<>();
        params.put("state", new ContextedString("leaf_state"));
        params.put("times", new ContextedString("2"));
        params.put("fromSlot", new ContextedString("fromSlot"));
        params.put("toSlot", new ContextedString("toSlot"));
        params.put("ordinal", new ContextedString("2"));
        condition.setUp(params, null);

        Context context = new Context();
        Map<String, Object> slots = new LoggerHashMap<>();
        slots.put("fromSlot", Arrays.asList(new Date(2017, 1, 1), new Date(2018, 1, 1), new Date(2019, 1, 1)));
        slots.put("toSlot", null);
        Map<String, Integer> repeatedness = new HashMap<>();
        repeatedness.put("leaf_state", 2);
        slots.put(SYSTEM_STATE_REPEATEDNESS_NAME, repeatedness);
        context.setSlots(slots);

        Map<String, String> types = new HashMap<>();
        types.put("fromSlot", Date.class.getName());
        context.setTypes(types);

        assertTrue(condition.accept(context));
        assertEquals(2019, ((Date) context.slotContentByName("toSlot")).year);
    }
}