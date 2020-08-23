package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import ai.hual.labrador.utils.DateDurationUtils;
import ai.hual.labrador.utils.DateType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateDurationAnnotatorTest {

    private static DateDurationAnnotator DDA;

    @BeforeAll
    static void setup() {
        DDA = new DateDurationAnnotator();
    }

    @Test
    void testWeekDurationAnnotate() {
        // construct inputs
        String query = "大概3周吧";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "大概{{数字}}周吧";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(3.0, "数字", "getDigits(str)",
                2, 8, 2, 3);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期段");

        assertEquals(1, slots.size());
        DateDurationUtils.Duration duration = (DateDurationUtils.Duration) slots.get(0).matched;
        assertEquals(3, duration.length);
        assertEquals(DateType.WEEK, duration.type);
    }

    @Test
    void dateDurationAnnotateSeasonTest() {

        // construct inputs
        String query = "大概3个季度吧";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "大概{{数字}}个季度吧";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(3.0, "数字", "getDigits(str)",
                2, 8, 2, 3);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期段");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DateDurationUtils.Duration(DateType.SEASON, 3),
                "日期段", "duration(s)", 2, 9, 2, 6);
        slotValues.put("日期段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("大概{{日期段}}吧");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期段");

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                // check matched string
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                // check label
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                // check start
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                // check end
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                // check real start
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                // check real end
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void transformThreeAndHalfMonthTest() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(3.0, "数字", "getDigits(str)",
                0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("这{{数字}}个半月", slotList, "日期段", "durationHalf(m)",
                0, 0, 0, 0);

        // output result
        SlotValue result = DDA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = new SlotValue(new DateDurationUtils.Duration(DateType.DAY, 105));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }
}