package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.utils.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareConditionTest {

    private static AccessorRepository accessorRepository;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
    }

    @Test
    void testDigitConditionWithNull() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("数字", null);
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("type", new ContextedString("数值"));
        params.put("left", new ContextedString("{{数字}}"));
        params.put("right", new ContextedString("5"));
        params.put("operator", new ContextedString("<"));

        CompareCondition compareCondition = new CompareCondition();
        compareCondition.setUp(params, accessorRepository);
        boolean accepted = compareCondition.accept(context);

        assertFalse(accepted);
    }

    @Test
    void testDateConditionWithSysCompare() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("日期", Arrays.asList(new DateUtils.Date(2018, 1, 1)));
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("type", new ContextedString("日期"));
        params.put("left", new ContextedString("{{日期}}"));
        params.put("right", new ContextedString("{{sys.date}}"));
        params.put("operator", new ContextedString("<"));

        CompareCondition compareCondition = new CompareCondition();
        compareCondition.setUp(params, accessorRepository);
        boolean accepted = compareCondition.accept(context);

        assertTrue(accepted);
    }

}