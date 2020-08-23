package ai.hual.labrador.dm.conditions;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.utils.DateUtils.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareDateConditionTest {

    private static AccessorRepository accessorRepository;

    @BeforeAll
    static void setup() {
        accessorRepository = new AccessorRepositoryImpl();
    }

    @Test
    void testYmdDateConditionWithSysCompare() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("日期", Arrays.asList(new Date(2018, 1, 1)));
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("left", new ContextedString("{{日期}}"));
        params.put("right", new ContextedString("{{sys.date}}"));
        params.put("operator", new ContextedString("<"));

        CompareDateCondition compareDateCondition = new CompareDateCondition();
        compareDateCondition.setUp(params, accessorRepository);
        boolean accepted = compareDateCondition.accept(context);

        assertTrue(accepted);
    }

    @Test
    void testYmdDateConditionCompare() {
        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();
        slots.put("日期", Arrays.asList(new Date(2017, 1, 1)));
        context.setSlots(slots);

        HashMap<String, ContextedString> params = new HashMap<>();
        params.put("left", new ContextedString("{{日期}}"));
        params.put("right", new ContextedString("2016-10-1"));
        params.put("operator", new ContextedString(">"));

        CompareDateCondition compareDateCondition = new CompareDateCondition();
        compareDateCondition.setUp(params, accessorRepository);
        boolean accepted = compareDateCondition.accept(context);

        assertTrue(accepted);
    }

}