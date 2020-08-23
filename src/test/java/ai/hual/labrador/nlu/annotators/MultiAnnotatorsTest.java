package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.DateType;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import ai.hual.labrador.utils.DirectionalTimeUtils;
import ai.hual.labrador.utils.TimeDurationUtils;
import ai.hual.labrador.utils.TimeType;
import ai.hual.labrador.utils.TimeUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ethan on 17-7-17.
 */
class MultiAnnotatorsTest {


    private static NumAnnotator NA;
    private static DateAnnotator DA;
    private static TimeAnnotator TA;
    private static DateDurationAnnotator DDurA;
    private static TimeDurationAnnotator TDurA;
    private static DirectionalDateAnnotator DDA;
    private static DirectionalTimeAnnotator DTA;

    @BeforeAll
    static void prepareData() {

        NA = new NumAnnotator();
        DA = new DateAnnotator();
        TA = new TimeAnnotator();
        DDurA = new DateDurationAnnotator();
        TDurA = new TimeDurationAnnotator();
        DDA = new DirectionalDateAnnotator();
        DTA = new DirectionalTimeAnnotator();
    }

    @Test
    void multiAnnotatorsZeroPrefixNumTimeDurationTest() {

        //  construct inputs 
        String query = "快进到05分零三十秒的地方";
        QueryAct queryAct = new QueryAct(query);
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(query);
        queryAct.setSlots(inputSlots);

        //  output result 
        List<QueryAct> resultNumList = NA.annotate(queryAct);
        QueryAct resultNum = resultNumList.get(0);
        assertEquals("快进到{{数字}}分{{数字}}秒的地方", resultNum.getPQuery());
        List<QueryAct> resultDurTimeList = TDurA.annotate(resultNum);

        List<SlotValue> resultSlots = resultDurTimeList.get(0).getSlots().get("时刻段");

        assertEquals("快进到{{时刻段}}的地方", resultDurTimeList.get(0).getPQuery());
        assertEquals((new TimeDurationUtils.Duration(TimeType.SECOND, 330)).toString(),
                resultSlots.get(0).matched.toString());
    }

    @Test
    void multiAnnotatorsNumDateDirectionalDateArabicRedundantSlotsTest() {

        //  construct inputs 
        String query = "5个人，从2017年7月20号开始，大概到2017年8月10日为止，回来3个";
        QueryAct queryAct = new QueryAct(query);
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(query);
        queryAct.setSlots(inputSlots);

        //  output result 
        List<QueryAct> resultNumList = NA.annotate(queryAct);
        QueryAct resultNum = resultNumList.get(0);
        assertEquals("{{数字}}个人，从{{数字}}年{{数字}}月{{数字}}号开始，大概到{{数字}}年{{数字}}月{{数字}}日为止，回来{{数字}}个", resultNum.getPQuery());
        List<QueryAct> resultDateList = DA.annotate(resultNum);
        QueryAct resultDate = resultDateList.get(0);
        assertEquals("{{数字}}个人，从{{日期}}开始，大概到{{日期}}为止，回来{{数字}}个", resultDate.getPQuery());
        List<QueryAct> resultDirectionalDateList = DDA.annotate(resultDate);
        QueryAct result = resultDirectionalDateList.get(0);
        assertEquals("{{数字}}个人，{{日期起始}}，大概{{日期结束}}，回来{{数字}}个", result.getPQuery());
        List<SlotValue> startSlots = result.getSlots().get("日期起始");
        List<SlotValue> endSlots = result.getSlots().get("日期结束");
        List<SlotValue> numSlots = result.getSlots().get("数字");

        //  expected result 
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, 2017, 7, 20), null, 0),
                "日期起始", "startDate(date)", 9, 17, 4, 17);
        SlotValue slot1 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, 2017, 8, 10), 0),
                "日期结束", "endDate(date)", 20, 28, 20, 33);
        SlotValue slot2 = new SlotValue(5.0, "数字", "getDigits(str)",
                0, 6, 0, 1);
        SlotValue slot3 = new SlotValue(3.0, "数字", "getDigits(str)",
                31, 37, 36, 37);
        slotValues.put("日期起始", slot0);
        slotValues.put("日期结束", slot1);
        slotValues.put("数字", slot2);
        slotValues.put("数字", slot3);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{数字}}个人，{{日期起始}}，大概{{日期结束}}，回来{{数字}}个");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartSlots = slotValues.get("日期起始");
        List<SlotValue> expEndSlots = slotValues.get("日期结束");
        List<SlotValue> expNumSlots = slotValues.get("数字");

        //  verify 
        assertEquals(expResult.getPQuery(), result.getPQuery());
        //  verify slots has single element 
        assertEquals(expStartSlots.size(), startSlots.size());
        assertEquals(expEndSlots.size(), endSlots.size());
        assertEquals(expNumSlots.size(), numSlots.size());
        //  verify matched string 
        assertEquals(expStartSlots.get(0).matched.toString(), startSlots.get(0).matched.toString());
        assertEquals(expEndSlots.get(0).matched.toString(), endSlots.get(0).matched.toString());
        assertEquals(expNumSlots.get(0).matched.toString(), numSlots.get(0).matched.toString());
        assertEquals(expNumSlots.get(1).matched.toString(), numSlots.get(1).matched.toString());
        //  verify label 
        assertEquals(expStartSlots.get(0).label, startSlots.get(0).label);
        assertEquals(expEndSlots.get(0).label, endSlots.get(0).label);
        assertEquals(expNumSlots.get(0).label, numSlots.get(0).label);
        assertEquals(expNumSlots.get(1).label, numSlots.get(1).label);
        //  verify start 
        assertEquals(expStartSlots.get(0).start, startSlots.get(0).start);
        assertEquals(expEndSlots.get(0).start, endSlots.get(0).start);
        assertEquals(expNumSlots.get(0).start, numSlots.get(0).start);
        assertEquals(expNumSlots.get(1).start, numSlots.get(1).start);
        //  verify end 
        assertEquals(expStartSlots.get(0).end, startSlots.get(0).end);
        assertEquals(expEndSlots.get(0).end, endSlots.get(0).end);
        assertEquals(expNumSlots.get(0).end, numSlots.get(0).end);
        assertEquals(expNumSlots.get(1).end, numSlots.get(1).end);
        //  verify real start 
        assertEquals(expStartSlots.get(0).realStart, startSlots.get(0).realStart);
        assertEquals(expEndSlots.get(0).realStart, endSlots.get(0).realStart);
        assertEquals(expNumSlots.get(0).realStart, numSlots.get(0).realStart);
        assertEquals(expNumSlots.get(1).realStart, numSlots.get(1).realStart);
        //  verify real end 
        assertEquals(expStartSlots.get(0).realEnd, startSlots.get(0).realEnd);
        assertEquals(expEndSlots.get(0).realEnd, endSlots.get(0).realEnd);
        assertEquals(expNumSlots.get(0).realEnd, numSlots.get(0).realEnd);
        assertEquals(expNumSlots.get(1).realEnd, numSlots.get(1).realEnd);
    }

    @Test
    void multiAnnotatorsNumDateDirectionalDateArabicTest() {

        //  construct inputs 
        String query = "从2017年7月20号开始，大概到2017年8月10日为止";
        QueryAct queryAct = new QueryAct(query);
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(query);
        queryAct.setSlots(inputSlots);

        //  output result 
        List<QueryAct> resultNumList = NA.annotate(queryAct);
        QueryAct resultNum = resultNumList.get(0);
        assertEquals("从{{数字}}年{{数字}}月{{数字}}号开始，大概到{{数字}}年{{数字}}月{{数字}}日为止", resultNum.getPQuery());
        List<QueryAct> resultDateList = DA.annotate(resultNum);
        QueryAct resultDate = resultDateList.get(0);
        assertEquals("从{{日期}}开始，大概到{{日期}}为止", resultDate.getPQuery());
        List<QueryAct> resultDirectionalDateList = DDA.annotate(resultDate);
        List<SlotValue> startSlots = resultDirectionalDateList.get(0).getSlots().get("日期起始");
        List<SlotValue> endSlots = resultDirectionalDateList.get(0).getSlots().get("日期结束");

        // expected result
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, 2017, 7, 20), null, 0),
                "日期起始", "startDate(date)", 0, 8, 0, 13);
        SlotValue slot1 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, 2017, 8, 10), 0),
                "日期结束", "endDate(date)", 11, 19, 16, 29);
        slotValues.put("日期起始", slot0);
        slotValues.put("日期结束", slot1);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{日期起始}}，大概{{日期结束}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartSlots = slotValues.get("日期起始");
        List<SlotValue> expEndSlots = slotValues.get("日期结束");

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), resultDirectionalDateList.get(0).getPQuery()),
                //  verify slots has single element 
                () -> assertEquals(expStartSlots.size(), startSlots.size()),
                () -> assertEquals(expEndSlots.size(), endSlots.size()),
                //  verify matched string 
                () -> assertEquals(expStartSlots.get(0).matched.toString(), startSlots.get(0).matched.toString()),
                () -> assertEquals(expEndSlots.get(0).matched.toString(), endSlots.get(0).matched.toString()),
                //  verify label 
                () -> assertEquals(expStartSlots.get(0).label, startSlots.get(0).label),
                () -> assertEquals(expEndSlots.get(0).label, endSlots.get(0).label),
                //  verify start 
                () -> assertEquals(expStartSlots.get(0).start, startSlots.get(0).start),
                () -> assertEquals(expEndSlots.get(0).start, endSlots.get(0).start),
                //  verify end 
                () -> assertEquals(expStartSlots.get(0).end, startSlots.get(0).end),
                () -> assertEquals(expEndSlots.get(0).end, endSlots.get(0).end),
                //  verify real start 
                () -> assertEquals(expStartSlots.get(0).realStart, startSlots.get(0).realStart),
                () -> assertEquals(expEndSlots.get(0).realStart, endSlots.get(0).realStart),
                //  verify real end 
                () -> assertEquals(expStartSlots.get(0).realEnd, startSlots.get(0).realEnd),
                () -> assertEquals(expEndSlots.get(0).realEnd, endSlots.get(0).realEnd)
        );
    }

    @Test
    void multiAnnotatorsNumDateDirectionalDateChineseTest() {

        //  construct inputs 
        String query = "从一九九五年7月20号开始，大概到一七年八月十二日为止";
        QueryAct queryAct = new QueryAct(query);
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(query);
        queryAct.setSlots(inputSlots);

        //  output result 
        List<QueryAct> resultNumList = NA.annotate(queryAct);
        QueryAct resultNum = resultNumList.get(0);
        assertEquals("从{{连续数字}}年{{数字}}月{{数字}}号开始，大概到{{连续数字}}年{{数字}}月{{数字}}日为止", resultNum.getPQuery());
        List<QueryAct> resultDateList = DA.annotate(resultNum);
        QueryAct resultDate = resultDateList.get(0);
        assertEquals("从{{日期}}开始，大概到{{日期}}为止", resultDate.getPQuery());
        List<QueryAct> resultDirectionalDateList = DDA.annotate(resultDate);
        List<SlotValue> startSlots = resultDirectionalDateList.get(0).getSlots().get("日期起始");
        List<SlotValue> endSlots = resultDirectionalDateList.get(0).getSlots().get("日期结束");

        //  expected result 
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, 1995, 7, 20), null, 0),
                "日期起始", "startDate(date)", 0, 8, 0, 13);
        SlotValue slot1 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, 2017, 8, 12), 0),
                "日期结束", "endDate(date)", 11, 19, 16, 27);
        slotValues.put("日期起始", slot0);
        slotValues.put("日期结束", slot1);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{日期起始}}，大概{{日期结束}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartSlots = slotValues.get("日期起始");
        List<SlotValue> expEndSlots = slotValues.get("日期结束");

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), resultDirectionalDateList.get(0).getPQuery()),
                //  verify slots has single element 
                () -> assertEquals(expStartSlots.size(), startSlots.size()),
                () -> assertEquals(expEndSlots.size(), endSlots.size()),
                //  verify matched string 
                () -> assertEquals(expStartSlots.get(0).matched.toString(), startSlots.get(0).matched.toString()),
                () -> assertEquals(expEndSlots.get(0).matched.toString(), endSlots.get(0).matched.toString()),
                //  verify label 
                () -> assertEquals(expStartSlots.get(0).label, startSlots.get(0).label),
                () -> assertEquals(expEndSlots.get(0).label, endSlots.get(0).label),
                //  verify start 
                () -> assertEquals(expStartSlots.get(0).start, startSlots.get(0).start),
                () -> assertEquals(expEndSlots.get(0).start, endSlots.get(0).start),
                //  verify end 
                () -> assertEquals(expStartSlots.get(0).end, startSlots.get(0).end),
                () -> assertEquals(expEndSlots.get(0).end, endSlots.get(0).end),
                //  verify real start 
                () -> assertEquals(expStartSlots.get(0).realStart, startSlots.get(0).realStart),
                () -> assertEquals(expEndSlots.get(0).realStart, endSlots.get(0).realStart),
                //  verify real end 
                () -> assertEquals(expStartSlots.get(0).realEnd, startSlots.get(0).realEnd),
                () -> assertEquals(expEndSlots.get(0).realEnd, endSlots.get(0).realEnd)
        );
    }

    @Test
    void multiAnnotatorsNumDateTimeDurationDirectionTest() {

        //  construct inputs 
        String query = "从一九九五年7月20号夜里3点一刻开始，大概到一七年八月十二日为止，以及9月份之后的两个月，每次3个半小时";
        QueryAct queryAct = new QueryAct(query);
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        queryAct.setPQuery(query);
        queryAct.setSlots(inputSlots);

        //  output result 
        List<QueryAct> resultNumList = NA.annotate(queryAct);
        QueryAct resultNum = resultNumList.get(0);
        assertEquals("从{{连续数字}}年{{数字}}月{{数字}}号夜里{{数字}}点{{数字}}刻开始，大概到{{连续数字}}年{{数字}}月{{数字}}日为止，以及{{数字}}月份之后的{{数字}}个月，每次{{数字}}个半小时",
                resultNum.getPQuery());

        List<QueryAct> resultDateList = DA.annotate(resultNum);
        QueryAct resultDate = resultDateList.get(0);
        assertEquals("从{{日期}}夜里{{数字}}点{{数字}}刻开始，大概到{{日期}}为止，以及{{日期}}之后的{{数字}}个月，每次{{数字}}个半小时",
                resultDate.getPQuery());

        List<QueryAct> resultTimeList = TA.annotate(resultDate);
        QueryAct resultTime = resultTimeList.get(0);
        assertEquals("从{{日期}}{{时刻}}开始，大概到{{日期}}为止，以及{{日期}}之后的{{数字}}个月，每次{{数字}}个半小时",
                resultTime.getPQuery());

        List<QueryAct> resultDateDurList = DDurA.annotate(resultTime);
        QueryAct resultDateDur = resultDateDurList.get(0);
        assertEquals("从{{日期}}{{时刻}}开始，大概到{{日期}}为止，以及{{日期}}之后的{{日期段}}，每次{{数字}}个半小时",
                resultDateDur.getPQuery());

        List<QueryAct> resultTimeDurList = TDurA.annotate(resultDateDur);
        QueryAct resultTimeDur = resultTimeDurList.get(0);
        assertEquals("从{{日期}}{{时刻}}开始，大概到{{日期}}为止，以及{{日期}}之后的{{日期段}}，每次{{时刻段}}",
                resultTimeDur.getPQuery());

        List<QueryAct> resultDirectionalDateList = DDA.annotate(resultTimeDur);
        QueryAct resultDirectionalDate = resultDirectionalDateList.get(0);
        assertEquals("{{日期起始}}{{时刻}}开始，大概{{日期结束}}，以及{{确定日期段}}，每次{{时刻段}}",
                resultDirectionalDate.getPQuery());

        List<QueryAct> resultDirectionalTimeList = DTA.annotate(resultDirectionalDate);
        QueryAct resultDirectionalTime = resultDirectionalTimeList.get(0);
        assertEquals("{{日期起始}}{{时刻起始}}，大概{{日期结束}}，以及{{确定日期段}}，每次{{时刻段}}",
                resultDirectionalTime.getPQuery());

        List<SlotValue> startTimeSlots = resultDirectionalTime.getSlots().get("时刻起始");
        List<SlotValue> endDateSlots = resultDirectionalTime.getSlots().get("日期结束");
        List<SlotValue> startDateSlots = resultDirectionalTime.getSlots().get("日期起始");
        List<SlotValue> definedDateSlots = resultDirectionalTime.getSlots().get("确定日期段");
        List<SlotValue> timeDurSlots = resultDirectionalTime.getSlots().get("时刻段");

        //  expected result 
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, 1995, 7, 20), null, 0),
                "日期起始", "startDate(date)", 0, 8, 0, 11);
        SlotValue slot1 = new SlotValue(new DirectionalTimeUtils.DirectionalTime(
                new TimeUtils.Time(TimeType.QUARTER, 3, 15, 0), null, 0),
                "时刻起始", "startTime(time)", 8, 16, 11, 19);
        SlotValue slot2 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, 2017, 8, 12), 0),
                "日期结束", "endDate(date)", 19, 27, 22, 33);
        SlotValue slot3 = new SlotValue(new DirectionalDateUtils.DirectionalDate(DateType.MONTH,
                new DateUtils.Date(DateType.MONTH, 0, 9, 0), null, 2),
                "确定日期段", "nextDateDuration(date,date_duration)", 30, 39, 36, 45);
        SlotValue slot4 = new SlotValue(new TimeDurationUtils.Duration(TimeType.MINUTE, 210),
                "时刻段", "durationHalf(h)", 42, 49, 48, 53);

        slotValues.put("日期起始", slot0);
        slotValues.put("时刻起始", slot1);
        slotValues.put("日期结束", slot2);
        slotValues.put("确定日期段", slot3);
        slotValues.put("时刻段", slot4);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{日期起始}}{{时刻起始}}，大概{{日期结束}}，以及{{确定日期段}}，每次{{时刻段}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartTimeSlots = slotValues.get("时刻起始");
        List<SlotValue> expEndDateSlots = slotValues.get("日期结束");
        List<SlotValue> expStartDateSlots = slotValues.get("日期起始");
        List<SlotValue> expDefinedDateSlots = slotValues.get("确定日期段");
        List<SlotValue> expTimeDurSlots = slotValues.get("时刻段");

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), resultDirectionalTime.getPQuery()),
                //  verify slots has single element 
                () -> assertEquals(expStartTimeSlots.size(), startTimeSlots.size()),
                () -> assertEquals(expEndDateSlots.size(), endDateSlots.size()),
                () -> assertEquals(expStartDateSlots.size(), startDateSlots.size()),
                () -> assertEquals(expDefinedDateSlots.size(), definedDateSlots.size()),
                () -> assertEquals(expTimeDurSlots.size(), timeDurSlots.size()),
                //  verify matched string 
                () -> assertEquals(expStartTimeSlots.get(0).matched.toString(), startTimeSlots.get(0).matched.toString()),
                () -> assertEquals(expEndDateSlots.get(0).matched.toString(), endDateSlots.get(0).matched.toString()),
                () -> assertEquals(expStartDateSlots.get(0).matched.toString(), startDateSlots.get(0).matched.toString()),
                () -> assertEquals(expDefinedDateSlots.get(0).matched.toString(), definedDateSlots.get(0).matched.toString()),
                () -> assertEquals(expTimeDurSlots.get(0).matched.toString(), timeDurSlots.get(0).matched.toString()),
                //  verify label 
                () -> assertEquals(expStartTimeSlots.get(0).label, startTimeSlots.get(0).label),
                () -> assertEquals(expEndDateSlots.get(0).label, endDateSlots.get(0).label),
                () -> assertEquals(expStartDateSlots.get(0).label, startDateSlots.get(0).label),
                () -> assertEquals(expDefinedDateSlots.get(0).label, definedDateSlots.get(0).label),
                () -> assertEquals(expTimeDurSlots.get(0).label, timeDurSlots.get(0).label),
                //  verify start 
                () -> assertEquals(expStartTimeSlots.get(0).start, startTimeSlots.get(0).start),
                () -> assertEquals(expEndDateSlots.get(0).start, endDateSlots.get(0).start),
                () -> assertEquals(expStartDateSlots.get(0).start, startDateSlots.get(0).start),
                () -> assertEquals(expDefinedDateSlots.get(0).start, definedDateSlots.get(0).start),
                () -> assertEquals(expTimeDurSlots.get(0).start, timeDurSlots.get(0).start),
                //  verify end 
                () -> assertEquals(expStartTimeSlots.get(0).end, startTimeSlots.get(0).end),
                () -> assertEquals(expEndDateSlots.get(0).end, endDateSlots.get(0).end),
                () -> assertEquals(expStartDateSlots.get(0).end, startDateSlots.get(0).end),
                () -> assertEquals(expDefinedDateSlots.get(0).end, definedDateSlots.get(0).end),
                () -> assertEquals(expTimeDurSlots.get(0).end, timeDurSlots.get(0).end),
                //  verify real start 
                () -> assertEquals(expStartTimeSlots.get(0).realStart, startTimeSlots.get(0).realStart),
                () -> assertEquals(expEndDateSlots.get(0).realStart, endDateSlots.get(0).realStart),
                () -> assertEquals(expStartDateSlots.get(0).realStart, startDateSlots.get(0).realStart),
                () -> assertEquals(expDefinedDateSlots.get(0).realStart, definedDateSlots.get(0).realStart),
                () -> assertEquals(expTimeDurSlots.get(0).realStart, timeDurSlots.get(0).realStart),
                //  verify real end 
                () -> assertEquals(expStartTimeSlots.get(0).realEnd, startTimeSlots.get(0).realEnd),
                () -> assertEquals(expEndDateSlots.get(0).realEnd, endDateSlots.get(0).realEnd),
                () -> assertEquals(expStartDateSlots.get(0).realEnd, startDateSlots.get(0).realEnd),
                () -> assertEquals(expDefinedDateSlots.get(0).realEnd, definedDateSlots.get(0).realEnd),
                () -> assertEquals(expTimeDurSlots.get(0).realEnd, timeDurSlots.get(0).realEnd)
        );
    }
}
