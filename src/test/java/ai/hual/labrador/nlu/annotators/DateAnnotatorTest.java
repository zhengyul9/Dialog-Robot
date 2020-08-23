package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import ai.hual.labrador.utils.DateType;
import ai.hual.labrador.utils.DateUtils.Date;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ethan on 17-7-14.
 */
class DateAnnotatorTest {

    private static class testDateAnnotator extends DateAnnotator {

    }

    private static testDateAnnotator DA;

    @BeforeAll
    static void prepareData() {

        DA = new testDateAnnotator();
    }

    @Test
    void nextMonthDayTest() {

        // construct inputs
        String query = "下个月1号";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery("下个月{{数字}}号");
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue slot1 = new SlotValue(1.0, "数字", "getDigits(str)", 3, 9, 3, 4);
        inputSlots.put("数字", slot1);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals(1, ((Date) result.getSlots().get("日期").get(0).matched).day);
    }

    @Test
    void lessThanHundredDateAnnotateTest() {

        // construct inputs
        String query = "92年";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery("{{数字}}年");
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue slot1 = new SlotValue(92.0, "数字", "getDigits(str)", 0, 6, 0, 2);
        inputSlots.put("数字", slot1);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals(1992, ((Date) result.getSlots().get("日期").get(0).matched).year);
    }

    @Test
    void specialContinueNumDateAnnotateTest() {

        // construct inputs
        String query = "一两天";
        QueryAct queryAct = new QueryAct(query);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        assertEquals("一两天", result.getPQuery());
    }

    @Test
    void simpleYearAnnotateTest() {

        // construct inputs
        String query = "16年3月";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{数字}}年{{数字}}月";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(16.0, "数字", "getDigits(str)", 0, 6, 0, 2);
        SlotValue slot2 = new SlotValue(3.0, "数字", "getDigits(str)", 7, 13, 3, 4);
        inputSlots.put("数字", slot1);
        inputSlots.put("数字", slot2);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result0 = resultList.get(0);

        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result0.getPQuery());
        assertEquals((new Date(DateType.DAY, 2016, 3, 0)).toString(),
                result0.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void prev3YearAnnotateTest() {

        // construct inputs
        String query = "大前年十月零五号";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "大前年{{数字}}月{{数字}}号";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(10.0, "数字", "getDigits(str)", 2, 8, 2, 3);
        SlotValue slot2 = new SlotValue(5.0, "数字", "getDigits(str)", 9, 15, 4, 6);
        inputSlots.put("数字", slot1);
        inputSlots.put("数字", slot2);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result0 = resultList.get(0);

        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result0.getPQuery());
        assertEquals((new Date(DateType.DAY, year - 3, 10, 5)).toString(),
                result0.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void next3YearAnnotateTest() {

        // construct inputs
        String query = "大后年十月十五号";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "大后年{{数字}}月{{数字}}号";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(10.0, "数字", "getDigits(str)", 2, 8, 2, 3);
        SlotValue slot2 = new SlotValue(15.0, "数字", "getDigits(str)", 9, 15, 4, 6);
        inputSlots.put("数字", slot1);
        inputSlots.put("数字", slot2);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        int year = LocalDate.now().getYear() + 3;
        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals((new Date(DateType.DAY, year, 10, 15)).toString(),
                result.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void lastYearAnnotateTest() {

        // construct inputs
        String query = "去年十月十五号";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "去年{{数字}}月{{数字}}号";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(10.0, "数字", "getDigits(str)", 2, 8, 2, 3);
        SlotValue slot2 = new SlotValue(15.0, "数字", "getDigits(str)", 9, 15, 4, 6);
        inputSlots.put("数字", slot1);
        inputSlots.put("数字", slot2);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        int year = LocalDate.now().getYear() - 1;
        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals((new Date(DateType.DAY, year, 10, 15)).toString(),
                result.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void normalDateNumDateAnnotateTest() {

        // construct inputs
        String query = "2012年10月23日";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{数字}}年{{数字}}月{{数字}}日";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(2012.0, "数字", "getDigits(str)", 0, 6, 0, 4);
        SlotValue slot2 = new SlotValue(10.0, "数字", "getDigits(str)", 7, 13, 5, 7);
        SlotValue slot3 = new SlotValue(23.0, "数字", "getDigits(str)", 14, 20, 8, 10);
        inputSlots.put("数字", slot1);
        inputSlots.put("数字", slot2);
        inputSlots.put("数字", slot3);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals((new Date(DateType.DAY, 2012, 10, 23)).toString(),
                result.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void zeroPrefixNumDateAnnotateTest() {

        // construct inputs
        String query = "05年";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{数字}}年";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        SlotValue slot1 = new SlotValue(5.0, "数字", "getDigits(str)", 0, 6, 0, 2);
        inputSlots.put("数字", slot1);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals(2, resultList.size());
        assertEquals("{{日期}}", result.getPQuery());
        assertEquals((new Date(DateType.YEAR, 2005, 0, 0)).toString(),
                result.getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void dateAnnotateYmdDateTest2018() {

        // construct inputs
        String query = "在2018年10月12号有个会,今年8月也有个会";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "在{{数字}}年{{数字}}月{{数字}}号有个会,今年{{数字}}月也有个会";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(2018.0, "数字", "getDigits(str)", 1, 7, 1, 5);
        SlotValue inputSlot1 = new SlotValue(10.0, "数字", "getDigits(str)", 8, 14, 6, 8);
        SlotValue inputSlot2 = new SlotValue(12.0, "数字", "getDigits(str)", 15, 21, 9, 11);
        SlotValue inputSlot3 = new SlotValue(8.0, "数字", "getDigits(str)", 28, 34, 18, 19);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期");

        // expected result
        // pQuery: 在{{日期}}有个会,今年{{日期}}也有个会
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new Date(DateType.DAY, 2018, 10, 12), "日期", "date(y,m,d)",
                1, 7, 1, 12);
        LocalDate currentDate = LocalDate.now();
        SlotValue slot1 = new SlotValue(new Date(DateType.MONTH, currentDate.getYear(), 8, 0),
                "日期", "thisYearMDate(m)", 11, 17, 16, 20);
        slotValues.put("日期", slot0);
        slotValues.put("日期", slot1);

        QueryAct expResult = new QueryAct(query);
        assertEquals(4, resultList.size());
        expResult.setPQuery("在{{日期}}有个会,{{日期}}也有个会");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期");

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                // check matched string
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                () -> assertEquals(expSlots.get(1).matched.toString(), slots.get(1).matched.toString()),
                // check label
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                // check start
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                // check end
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                // check real start
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                // check real end
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd)
        );
    }

    @Test
    void dateAnnotateYmdDateTest17() {

        // construct inputs
        String query = "在17年10月12号有个会,今年8月也有个会";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "在{{数字}}年{{数字}}月{{数字}}号有个会,今年{{数字}}月也有个会";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(17.0, "数字", "getDigits(str)", 1, 7, 1, 3);
        SlotValue inputSlot1 = new SlotValue(10.0, "数字", "getDigits(str)", 8, 14, 4, 6);
        SlotValue inputSlot2 = new SlotValue(12.0, "数字", "getDigits(str)", 15, 21, 7, 9);
        SlotValue inputSlot3 = new SlotValue(8.0, "数字", "getDigits(str)", 28, 34, 16, 17);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new Date(DateType.DAY, 2017, 10, 12), "日期", "date(y,m,d)",
                1, 7, 1, 10);
        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        SlotValue slot1 = new SlotValue(new Date(DateType.MONTH, year, 8, 0), "日期", "thisYearMDate(m)",
                11, 17, 14, 18);
        slotValues.put("日期", slot0);
        slotValues.put("日期", slot1);

        QueryAct expResult = new QueryAct(query);
        assertEquals(4, resultList.size());
        expResult.setPQuery("在{{日期}}有个会,{{日期}}也有个会");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期");

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                // check matched string
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                () -> assertEquals(expSlots.get(1).matched.toString(), slots.get(1).matched.toString()),
                // check label
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                // check start
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                // check end
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                // check real start
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                // check real end
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd)
        );
    }

    @Test
    void dateAnnotateYmdDateFailYearTest7() {

        // construct inputs
        String query = "在7年10月12号有个会,今年8月也有个会";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "在{{数字}}年{{数字}}月{{数字}}号有个会,今年{{数字}}月也有个会";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(7.0, "数字", "getDigits(str)", 1, 7, 1, 2);
        SlotValue inputSlot1 = new SlotValue(10.0, "数字", "getDigits(str)", 8, 14, 3, 5);
        SlotValue inputSlot2 = new SlotValue(12.0, "数字", "getDigits(str)", 15, 21, 6, 8);
        SlotValue inputSlot3 = new SlotValue(8.0, "数字", "getDigits(str)", 28, 34, 15, 16);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        LocalDate currentDate = LocalDate.now();
        SlotValue slot0 = new SlotValue(new Date(DateType.DAY, 0, 10, 12), "日期",
                "date(m,d)", 8, 14, 3, 9);
        SlotValue slot1 = new SlotValue(new Date(DateType.MONTH, currentDate.getYear(), 8, 0), "日期",
                "thisYearMDate(m)", 18, 24, 13, 17);
        slotValues.put("日期", slot0);
        slotValues.put("日期", slot1);

        QueryAct expResult = new QueryAct(query);
        assertEquals(4, resultList.size());
        expResult.setPQuery("在{{数字}}年{{日期}}有个会,{{日期}}也有个会");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期");

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                // check matched string
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                () -> assertEquals(expSlots.get(1).matched.toString(), slots.get(1).matched.toString()),
                // check label
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                // check start
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                // check end
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                // check real start
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                // check real end
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd)
        );
    }

    @Test
    void dateAnnotateYMDStringTest() {

        // construct inputs
        String query = "二零二零年五月十六日";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{连续数字}}年{{数字}}月{{数字}}日";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(2020.0, "连续数字", "consecutiveDigits(str)", 0, 8, 0, 4);
        SlotValue inputSlot1 = new SlotValue(5.0, "数字", "getDigits(str)", 9, 15, 5, 6);
        SlotValue inputSlot2 = new SlotValue(16.0, "数字", "getDigits(str)", 16, 22, 7, 9);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new Date(DateType.DAY, 2020, 5, 16), "日期",
                "date(y,m,d)", 0, 6, 0, 10);
        slotValues.put("日期", slot0);

        QueryAct expResult = new QueryAct(query);
        assertEquals(2, resultList.size());
        expResult.setPQuery("{{日期}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期");

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
    void dateAnnotateTestYMDSepWithMinusMark() {

        // construct inputs
        String query = "在18-10-12有个会,今年8月也有个会";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "在{{数字}}-{{数字}}-{{数字}}有个会,今年{{数字}}月也有个会";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(18.0, "数字", "getDigits(str)", 1, 7, 1, 3);
        SlotValue inputSlot1 = new SlotValue(10.0, "数字", "getDigits(str)", 8, 14, 4, 6);
        SlotValue inputSlot2 = new SlotValue(12.0, "数字", "getDigits(str)", 15, 21, 7, 9);
        SlotValue inputSlot3 = new SlotValue(8.0, "数字", "getDigits(str)", 27, 33, 15, 16);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("日期");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new Date(DateType.DAY, 2018, 10, 12), "日期", "date(y,m,d)",
                1, 7, 1, 9);
        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        SlotValue slot1 = new SlotValue(new Date(DateType.MONTH, year, 8, 0), "日期", "thisYearMDate(m)",
                11, 17, 13, 17);
        slotValues.put("日期", slot0);
        slotValues.put("日期", slot1);

        QueryAct expResult = new QueryAct(query);
        assertEquals(4, resultList.size());
        expResult.setPQuery("在{{日期}}有个会,{{日期}}也有个会");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("日期");

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                // check matched string
                () -> assertEquals(expSlots.get(0).matched.toString(), slots.get(0).matched.toString()),
                () -> assertEquals(expSlots.get(1).matched.toString(), slots.get(1).matched.toString()),
                // check label
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                // check start
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                // check end
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                // check real start
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                // check real end
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd)
        );
    }

    @Test
    void dateAnnotateEmptyNoMatchTest() {

        // construct inputs
        String query = "这个句子中不会提出任何槽";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "这个句子中不会提出任何槽";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = DA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();

        QueryAct expResult = new QueryAct(query);
        assertEquals(1, resultList.size());
        expResult.setPQuery("这个句子中不会提出任何槽");
        expResult.setSlots(slotValues);

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                () -> assertEquals(0, result.getSlots().values().size())
        );
    }

    @Test
    void transformTestYDate() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(2017.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("\\{\\{数字}}年", slotList, "日期", "date(y)", 0, 0, 0, 0);

        // output result
        SlotValue result = DA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = new SlotValue(new Date(DateType.YEAR, 2017, 0, 0));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

    @Test
    void transformTestYmdDate() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(2017.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        SlotValue slot1 = new SlotValue(7.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        SlotValue slot2 = new SlotValue(15.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        slotList.add(slot1);
        slotList.add(slot2);
        SlotValuePack valuePack = new SlotValuePack("\\{\\{数字}}年\\{\\{数字}}月\\{\\{数字}}日", slotList,
                "日期", "date(y,m,d)", 0, 0, 0, 0);

        // output result
        SlotValue result = DA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = new SlotValue(new Date(DateType.DAY, 2017, 7, 15));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

    @Test
    void transformTestYearStringDate() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValuePack valuePack0 = new SlotValuePack("九五年", slotList, "日期", "date(yearString)",
                0, 0, 0, 0);
        SlotValuePack valuePack1 = new SlotValuePack("一九九五年", slotList, "日期", "date(yearString)",
                0, 0, 0, 0);
        SlotValuePack valuePack2 = new SlotValuePack("二零零五年", slotList, "日期", "date(yearString)",
                0, 0, 0, 0);
        SlotValuePack valuePack3 = new SlotValuePack("一二年", slotList, "日期", "date(yearString)",
                0, 0, 0, 0);
        SlotValuePack valuePack4 = new SlotValuePack("二年", slotList, "日期", "date(yearString)",
                0, 0, 0, 0);
        List<SlotValue> slotList5 = new ArrayList<>();
        SlotValue slot0 = new SlotValue(7.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        SlotValue slot1 = new SlotValue(15.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList5.add(slot0);
        slotList5.add(slot1);
        SlotValuePack valuePack5 = new SlotValuePack("八二年{{数字}}月{{数字}}日", slotList5, "日期", "date(ymdString,m,d)",
                0, 0, 0, 0);
        List<SlotValue> slotList6 = new ArrayList<>();
        slotList6.add(slot0);
        SlotValuePack valuePack6 = new SlotValuePack("零一年{{数字}}月", slotList6, "日期", "date(ymString,m)",
                0, 0, 0, 0);

        // output result
        SlotValue result0 = DA.transform(valuePack0, ArrayListMultimap.create());
        SlotValue result1 = DA.transform(valuePack1, ArrayListMultimap.create());
        SlotValue result2 = DA.transform(valuePack2, ArrayListMultimap.create());
        SlotValue result3 = DA.transform(valuePack3, ArrayListMultimap.create());
        SlotValue result4 = DA.transform(valuePack4, ArrayListMultimap.create());
        SlotValue result5 = DA.transform(valuePack5, ArrayListMultimap.create());
        SlotValue result6 = DA.transform(valuePack6, ArrayListMultimap.create());

        // expected result
        SlotValue expResult0 = new SlotValue(new Date(DateType.YEAR, 1995, 0, 0));
        SlotValue expResult1 = new SlotValue(new Date(DateType.YEAR, 1995, 0, 0));
        SlotValue expResult2 = new SlotValue(new Date(DateType.YEAR, 2005, 0, 0));
        SlotValue expResult3 = new SlotValue(new Date(DateType.YEAR, 2012, 0, 0));
        SlotValue expResult4 = null;
        SlotValue expResult5 = new SlotValue(new Date(DateType.DAY, 1982, 7, 15));
        SlotValue expResult6 = new SlotValue(new Date(DateType.MONTH, 2001, 7, 0));

        // verify
        assertAll("All date correct",
                () -> assertEquals(expResult0.matched.toString(), result0.matched.toString()),
                () -> assertEquals(expResult1.matched.toString(), result1.matched.toString()),
                () -> assertEquals(expResult2.matched.toString(), result2.matched.toString()),
                () -> assertEquals(expResult3.matched.toString(), result3.matched.toString()),
                () -> assertEquals(expResult4, result4),
                () -> assertEquals(expResult5.matched.toString(), result5.matched.toString()),
                () -> assertEquals(expResult6.matched.toString(), result6.matched.toString())
        );
    }

    @Test
    void transformTestPrevWeekDay() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        int weekday = 5;
        SlotValue slot0 = new SlotValue((double) weekday, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("上周\\{\\{数字}}", slotList, "日期", "prevWeekday(wd)",
                0, 0, 0, 0);

        // output result
        SlotValue result = DA.transform(valuePack, ArrayListMultimap.create());
        Date resultDate = (Date) result.matched;

        // expected result
        LocalDate currentDate = LocalDate.now();
        int minusDays = currentDate.getDayOfWeek().getValue() + (7 - weekday);
        LocalDate expectDate = currentDate.minusDays(minusDays);
        SlotValue expResult = new SlotValue(new Date(DateType.DAY, expectDate.getYear(),
                expectDate.getMonthValue(), expectDate.getDayOfMonth()));

        // verify
        assertAll("All date correct",
                () -> assertEquals(expResult.matched.toString(), result.matched.toString()),
                () -> assertEquals(5, resultDate.weekday)
        );
    }

    @Test
    void transformTestThisYearMonth() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(8.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("今年{{数字}}月", slotList, "日期", "thisYearMDate(m)",
                0, 0, 0, 0);

        // output result
        SlotValue result = DA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        LocalDate localDate = LocalDate.now();
        SlotValue expResult = new SlotValue(new Date(DateType.MONTH, localDate.getYear(), 8, 0));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

}