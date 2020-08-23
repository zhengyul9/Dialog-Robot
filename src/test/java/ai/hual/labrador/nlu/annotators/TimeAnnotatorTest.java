package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import ai.hual.labrador.utils.TimeUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ethan on 17-7-17.
 */
class TimeAnnotatorTest {

    /**
     * "Mock" fetchRegex() method, let it use regexList here.
     */
    private static class testTimeAnnotator extends TimeAnnotator {

    }

    private static testTimeAnnotator TA;

    @BeforeAll
    static void prepareData() {

        TA = new testTimeAnnotator();
    }

    @Test
    void hugeSecondElapseTest() {

        // output result
        QueryAct queryAct = new QueryAct("5千秒之后");
        String pQuery = "{{数字}}秒之后";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(5000.0, "数字", "getDigits(str)",
                0, 6, 0, 2);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        List<QueryAct> resultList = TA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals("{{时刻}}", result.getPQuery());
    }

    @Test
    void timeAnnotateRegexHasNoBraceTest1() {

        // output result
        List<QueryAct> resultList = TA.annotate(new QueryAct("在这半分之前"));
        QueryAct result = resultList.get(0);

        assertEquals("在这{{时刻}}", result.getPQuery());
    }

    @Test
    void timeAnnotateRegexHasNoBraceTest2() {

        // output result
        List<QueryAct> resultList = TA.annotate(new QueryAct("想听此刻"));
        QueryAct result = resultList.get(0);

        assertEquals("想听{{时刻}}", result.getPQuery());
    }

    @Test
    void timeAnnotateEmptyNoMatchTest() {

        // construct inputs
        String query = "这个句子中不会提出任何槽";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "这个句子中不会提出任何槽";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> resultList = TA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("这个句子中不会提出任何槽");
        expResult.setSlots(slotValues);

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                () -> assertEquals(0, result.getSlots().values().size())
        );
    }

    @Test
    void timeAnnotateFailNegativeHourTest() {

        // construct inputs
        String query = "-3点的机票";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{数字}}点的机票";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(-3.0, "数字", "getDigits(str)",
                0, 6, 0, 2);
        inputSlots.put("数字", inputSlot0);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> result = TA.annotate(queryAct);

        // expected result
        List<QueryAct> expResult = new ArrayList<>();
        expResult.add(queryAct);

        // verify
        assertAll("Assert result match",
                () -> assertEquals(expResult.size(), result.size()),
                () -> assertEquals(expResult.get(0), result.get(0))
        );
    }

    @Test
    void timeAnnotateFailFirstInTwoTest() {

        // construct inputs
        String query = "26时的机票和5点30分两秒的火车";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "{{数字}}时的机票和{{数字}}点{{数字}}分{{数字}}秒的火车";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(26.0, "数字", "getDigits(str)", 0, 6, 0, 2);
        SlotValue inputSlot1 = new SlotValue(5.0, "数字", "getDigits(str)", 11, 17, 7, 8);
        SlotValue inputSlot2 = new SlotValue(30.0, "数字", "getDigits(str)", 18, 24, 9, 11);
        SlotValue inputSlot3 = new SlotValue(2.0, "数字", "getDigits(str)", 25, 31, 12, 13);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> result = TA.annotate(queryAct);
        QueryAct resultQA = result.get(0);
        SlotValue resultSV = resultQA.getSlots().get("时刻").get(0);

        // expected result
        String expPQuery = "{{数字}}时的机票和{{时刻}}的火车";
        SlotValue expSV = new SlotValue(new TimeUtils.Time(5, 30, 2), "时刻", "time(h,m,s)",
                11, 17, 7, 14);

        // verify
        assertAll("Assert result match",
                () -> assertEquals(1, resultQA.getSlots().get("时刻").size()),

                // verify new slot
                () -> assertEquals(expPQuery, resultQA.getPQuery()),
                () -> assertEquals(expSV.matched.toString(), resultSV.matched.toString()),
                () -> assertEquals(expSV.label, resultSV.label),
                () -> assertEquals(expSV.start, resultSV.start),
                () -> assertEquals(expSV.end, resultSV.end),
                () -> assertEquals(expSV.realStart, resultSV.realStart),
                () -> assertEquals(expSV.realEnd, resultSV.realEnd),

                // verify remained slots
                () -> assertEquals(1, resultQA.getSlots().get("数字").size()),
                () -> assertEquals(inputSlot0.toString(), resultQA.getSlots().get("数字").get(0).toString())

        );
    }

    @Test
    void timeAnnotateFailSecondInTwoTest() {

        // construct inputs
        String query = "夜里3点10分的火车,夜间5点6刻的飞机";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "夜里{{数字}}点{{数字}}分的火车,夜间{{数字}}点{{数字}}刻的飞机";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(3.0, "数字", "getDigits(str)", 2, 8, 2, 3);
        SlotValue inputSlot1 = new SlotValue(10.0, "数字", "getDigits(str)", 9, 15, 4, 6);
        SlotValue inputSlot2 = new SlotValue(5.0, "数字", "getDigits(str)", 22, 28, 13, 14);
        SlotValue inputSlot3 = new SlotValue(6.0, "数字", "getDigits(str)", 29, 35, 15, 16);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        inputSlots.put("数字", inputSlot2);
        inputSlots.put("数字", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        // output result
        List<QueryAct> result = TA.annotate(queryAct);
        QueryAct resultQA = result.get(0);
        SlotValue resultSV = resultQA.getSlots().get("时刻").get(0);

        // expected result
        String expPQuery = "{{时刻}}的火车,夜间{{数字}}点{{数字}}刻的飞机";
        SlotValue expSV = new SlotValue(new TimeUtils.Time(3, 10, 0), "时刻", "timeNight(h,m)",
                0, 6, 0, 7);
        inputSlot2.start = 12;
        inputSlot2.end = 18;
        inputSlot3.start = 19;
        inputSlot3.end = 25;

        // verify
        assertAll("Assert result match",
                () -> assertEquals(1, resultQA.getSlots().get("时刻").size()),

                // check new slot
                () -> assertEquals(expPQuery, resultQA.getPQuery()),
                () -> assertEquals(expSV.matched.toString(), resultSV.matched.toString()),
                () -> assertEquals(expSV.label, resultSV.label),
                () -> assertEquals(expSV.start, resultSV.start),
                () -> assertEquals(expSV.end, resultSV.end),
                () -> assertEquals(expSV.realStart, resultSV.realStart),
                () -> assertEquals(expSV.realEnd, resultSV.realEnd),

                // check remained slots
                () -> assertEquals(2, resultQA.getSlots().get("数字").size()),
                () -> assertEquals(inputSlot2.toString(), resultQA.getSlots().get("数字").get(0).toString()),
                () -> assertEquals(inputSlot3.toString(), resultQA.getSlots().get("数字").get(1).toString())
        );
    }

    @Test
    void transformPmHourTest() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(2.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("\\{\\{数字}}p.m", slotList, "时刻", "timePm(h)", 0, 0, 0, 0);

        // output result
        SlotValue result = TA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = new SlotValue(new TimeUtils.Time(14, 0, 0));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

    @Test
    void transformHqNightTest() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(11.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        SlotValue slot1 = new SlotValue(2.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        slotList.add(slot1);
        SlotValuePack valuePack = new SlotValuePack("夜里\\{\\{数字}}时\\{\\{数字}}刻", slotList,
                "时刻", "timeNight(h,q)", 0, 0, 0, 0);

        // output result
        SlotValue result = TA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = new SlotValue(new TimeUtils.Time(23, 30, 0));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

    @Test
    void transformNextHourTest() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValuePack valuePack = new SlotValuePack("下个钟头", slotList,
                "时刻", "nextHour(1)", 0, 0, 0, 0);

        // output result
        SlotValue result = TA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        LocalTime currentTime = LocalTime.now();
        SlotValue expResult = new SlotValue(new TimeUtils.Time(currentTime.plusHours(1).getHour(), 0, 0));

        // verify
        assertEquals(expResult.matched.toString(), result.matched.toString());
    }

    @Test
    void transformFailTest() {
        /*
            transform should fail and return null here
         */

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValue slot0 = new SlotValue(26.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList.add(slot0);
        SlotValuePack valuePack = new SlotValuePack("\\{\\{数字}}p.m", slotList, "时刻", "timePm(h)", 0, 0, 0, 0);

        // output result
        SlotValue result = TA.transform(valuePack, ArrayListMultimap.create());

        // expected result
        SlotValue expResult = null;

        // verify
        assertEquals(expResult, result);
    }

    @Test
    void transformTimesTest() {

        // construct inputs
        List<SlotValue> slotList = new ArrayList<>();
        SlotValuePack valuePack0 = new SlotValuePack("现在", slotList, "时刻", "thisTime()",
                0, 0, 0, 0);
        List<SlotValue> slotList1 = new ArrayList<>();
        SlotValue slot0 = new SlotValue(2.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList1.add(slot0);
        SlotValuePack valuePack1 = new SlotValuePack("{{数字}}个半钟头之前", slotList1, "时刻", "beforeHourHalf(h)",
                0, 0, 0, 0);
        SlotValuePack valuePack2 = new SlotValuePack("{{数字}}刻钟后", slotList1, "时刻", "afterQuarter(q)",
                0, 0, 0, 0);
        SlotValuePack valuePack3 = new SlotValuePack("过半天", slotList, "时刻", "afterDayHalf()",
                0, 0, 0, 0);
        SlotValuePack valuePack4 = new SlotValuePack("{{数字}}分半以后", slotList1, "时刻", "afterMinHalf(m)",
                0, 0, 0, 0);
        List<SlotValue> slotList5 = new ArrayList<>();
        SlotValue slot1 = new SlotValue(7.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        SlotValue slot2 = new SlotValue(15.0, "数字", "getDigits(str)", 0, 0, 0, 0);
        slotList5.add(slot0);
        slotList5.add(slot1);
        slotList5.add(slot2);
        SlotValuePack valuePack5 = new SlotValuePack("{{数字}}:{{数字}}:{{数字}}pm", slotList5, "时刻", "timePm(h,m,s)",
                0, 0, 0, 0);
        SlotValuePack valuePack6 = new SlotValuePack("半分钟前", slotList, "时刻", "beforeMinHalf()",
                0, 0, 0, 0);

        // output result
        SlotValue result0 = TA.transform(valuePack0, ArrayListMultimap.create());
        SlotValue result1 = TA.transform(valuePack1, ArrayListMultimap.create());
        SlotValue result2 = TA.transform(valuePack2, ArrayListMultimap.create());
        SlotValue result3 = TA.transform(valuePack3, ArrayListMultimap.create());
        SlotValue result4 = TA.transform(valuePack4, ArrayListMultimap.create());
        SlotValue result5 = TA.transform(valuePack5, ArrayListMultimap.create());
        SlotValue result6 = TA.transform(valuePack6, ArrayListMultimap.create());

        // expected result
        LocalTime currentTime;
        currentTime = LocalTime.now();
        SlotValue expResult0 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
        currentTime = LocalTime.now().minusMinutes(2 * 60 + 30);
        SlotValue expResult1 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
        currentTime = LocalTime.now().plusMinutes(2 * 15);
        SlotValue expResult2 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
        currentTime = LocalTime.now().plusHours(24 / 2);
        SlotValue expResult3 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
        currentTime = LocalTime.now().plusSeconds(2 * 60 + 30);
        SlotValue expResult4 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
        SlotValue expResult5 = new SlotValue(new TimeUtils.Time(14, 7, 15));
        currentTime = LocalTime.now().minusSeconds(30);
        SlotValue expResult6 = new SlotValue(new TimeUtils.Time(
                currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));

        // verify
        assertAll("All date correct",
                () -> assertEquals(expResult0.matched.toString(), result0.matched.toString()),
                () -> assertEquals(expResult1.matched.toString(), result1.matched.toString()),
                () -> assertEquals(expResult2.matched.toString(), result2.matched.toString()),
                () -> assertEquals(expResult3.matched.toString(), result3.matched.toString()),
                () -> assertEquals(expResult4.matched.toString(), result4.matched.toString()),
                () -> assertEquals(expResult5.matched.toString(), result5.matched.toString()),
                () -> assertEquals(expResult6.matched.toString(), result6.matched.toString())
        );
    }
}