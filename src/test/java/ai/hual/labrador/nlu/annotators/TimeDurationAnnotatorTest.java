package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.TimeDurationUtils;
import ai.hual.labrador.utils.TimeType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeDurationAnnotatorTest {

    private static TimeDurationAnnotator TDA;

    @BeforeAll
    private static void setup() {
        TDA = new TimeDurationAnnotator();
    }

    @Test
    void hugeSecondDurationTest() {

        /* output result */
        QueryAct queryAct = new QueryAct("5千秒左右");
        String pQuery = "{{数字}}秒左右";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(5000.0, "数字", "getDigits(str)",
                0, 6, 0, 2);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals("{{时刻段}}左右", result.getPQuery());
        assertEquals("5000 SECOND", result.getSlots().get("时刻段").get(0).matched.toString());
    }

    @Test
    void msNoUnitTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到两分20";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}分{{数字}}";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(2.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(20.0, "数字", "getDigits(str)",
                10, 16, 5, 7);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.SECOND, 140),
                "时刻段", "duration(m,s)", 3, 10, 3, 7);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void hmNoUnitTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到1小时20";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}小时{{数字}}";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(1.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(20.0, "数字", "getDigits(str)",
                11, 17, 6, 8);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.MINUTE, 80),
                "时刻段", "duration(h,m)", 3, 10, 3, 8);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void hmsTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到1小时20分30秒";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}小时{{数字}}分{{数字}}秒";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(1.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(20.0, "数字", "getDigits(str)",
                11, 17, 6, 8);
        SlotValue inputSlot2 = new SlotValue(30.0, "数字", "getDigits(str)",
                18, 24, 9, 11);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.SECOND, 4830),
                "时刻段", "duration(h,m,s)", 3, 10, 3, 12);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void hsTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到两小时20秒";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}小时{{数字}}秒";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(2.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(20.0, "数字", "getDigits(str)",
                11, 17, 6, 8);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.SECOND, 7220),
                "时刻段", "duration(h,s)", 3, 10, 3, 9);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void hmTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到1小时20分";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}小时{{数字}}分";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(1.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(20.0, "数字", "getDigits(str)",
                11, 17, 6, 8);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.MINUTE, 80),
                "时刻段", "duration(h,m)", 3, 10, 3, 9);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void quarterTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "差不多两刻钟吧";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "差不多{{数字}}刻钟吧";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(2.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.QUARTER, 2),
                "时刻段", "duration(q)", 3, 10, 3, 6);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("差不多{{时刻段}}吧");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }

    @Test
    void msTimeDurationAnnotateTest() {

        /* construct inputs */
        String query = "快进到5分30秒";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "快进到{{数字}}分{{数字}}秒";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(5.0, "数字", "getDigits(str)",
                3, 9, 3, 4);
        SlotValue inputSlot1 = new SlotValue(30.0, "数字", "getDigits(str)",
                10, 16, 5, 7);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = TDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("时刻段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new TimeDurationUtils.Duration(TimeType.SECOND, 330),
                "时刻段", "duration(m,s)", 3, 10, 3, 8);
        slotValues.put("时刻段", slot0);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("快进到{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("时刻段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* check matched string */
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                /* check label */
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                /* check start */
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                /* check end */
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                /* check real start */
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                /* check real end */
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd)
        );
    }
}