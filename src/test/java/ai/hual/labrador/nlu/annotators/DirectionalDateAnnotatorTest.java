package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.DateDurationUtils;
import ai.hual.labrador.utils.DateType;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ethan on 17-7-24
 */
class DirectionalDateAnnotatorTest {


    static DirectionalDateAnnotator DDA;

    @BeforeAll
    static void prepareData() {

        DDA = new DirectionalDateAnnotator();
    }

    @Test
    void directionalDateAnnotateTwoKeyTest() {

        /* construct inputs */
        String query = "从2017年7月20号起始，大概到2017年8月10日为止";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "从{{日期}}起始，大概到{{日期}}为止";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(new DateUtils.Date(2017, 7, 20), "日期", "date(y,m,d)",
                1, 7, 1, 11);
        SlotValue inputSlot1 = new SlotValue(new DateUtils.Date(2017, 8, 10), "日期", "date(y,m,d)",
                13, 19, 17, 27);
        inputSlots.put("日期", inputSlot0);
        inputSlots.put("日期", inputSlot1);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = DDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> startSlots = result.getSlots().get("日期起始");
        List<SlotValue> endSlots = result.getSlots().get("日期结束");

        /* expected result */
        /* pQuery: {{日期起始}},大概{{日期结束}} */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot0 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(2017, 7, 20), null, 0),
                "日期起始", "startDate(date)", 0, 8, 0, 13);
        SlotValue slot1 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(2017, 8, 10), 0),
                "日期结束", "endDate(date)", 11, 19, 16, 29);
        slotValues.put("日期起始", slot0);
        slotValues.put("日期结束", slot1);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{日期起始}}，大概{{日期结束}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartSlots = slotValues.get("日期起始");
        List<SlotValue> expEndSlots = slotValues.get("日期结束");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* verify slots has single element */
                () -> assertEquals(expStartSlots.size(), startSlots.size()),
                () -> assertEquals(expEndSlots.size(), endSlots.size()),
                /* verify matched string */
                () -> assertEquals(expStartSlots.get(0).matched.toString(), startSlots.get(0).matched.toString()),
                () -> assertEquals(expEndSlots.get(0).matched.toString(), endSlots.get(0).matched.toString()),
                /* verify label */
                () -> assertEquals(expStartSlots.get(0).label, startSlots.get(0).label),
                () -> assertEquals(expEndSlots.get(0).label, endSlots.get(0).label),
                /* verify start */
                () -> assertEquals(expStartSlots.get(0).start, startSlots.get(0).start),
                () -> assertEquals(expEndSlots.get(0).start, endSlots.get(0).start),
                /* verify end */
                () -> assertEquals(expStartSlots.get(0).end, startSlots.get(0).end),
                () -> assertEquals(expEndSlots.get(0).end, endSlots.get(0).end),
                /* verify real start */
                () -> assertEquals(expStartSlots.get(0).realStart, startSlots.get(0).realStart),
                () -> assertEquals(expEndSlots.get(0).realStart, endSlots.get(0).realStart),
                /* verify real end */
                () -> assertEquals(expStartSlots.get(0).realEnd, startSlots.get(0).realEnd),
                () -> assertEquals(expEndSlots.get(0).realEnd, endSlots.get(0).realEnd)
        );
    }

    @Test
    void directionalDateAnnotateThreeKeyFourSlotTest() {

        /* construct inputs */
        String query = "估计下两周会通知，从7月20号起始，大概到8月10日为止，也可能持续到15号";
        QueryAct queryAct = new QueryAct(query);
        String pQuery = "估计下{{日期段}}会通知，从{{日期}}起始，大概到{{日期}}为止，也可能持续到{{日期}}";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        LocalDate currentDate = LocalDate.now();
        SlotValue inputSlot0 = new SlotValue(
                new DateDurationUtils.Duration(DateType.WEEK, 2),
                "日期段", "duration(w)", 3, 10, 3, 5);
        SlotValue inputSlot1 = new SlotValue(new DateUtils.Date(DateType.DAY, currentDate.getYear(), 7, 20),
                "日期", "date(m,d)", 15, 21, 10, 15);
        SlotValue inputSlot2 = new SlotValue(new DateUtils.Date(DateType.DAY, currentDate.getYear(), 8, 10),
                "日期", "date(m,d)", 27, 33, 21, 26);
        SlotValue inputSlot3 = new SlotValue(new DateUtils.Date(DateType.DAY, currentDate.getYear(), 8, 10),
                "日期", "date(d)", 42, 48, 35, 38);
        inputSlots.put("日期段", inputSlot0);
        inputSlots.put("日期", inputSlot1);
        inputSlots.put("日期", inputSlot2);
        inputSlots.put("日期", inputSlot3);
        queryAct.setPQuery(pQuery);
        queryAct.setSlots(inputSlots);

        /* output result */
        List<QueryAct> resultList = DDA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> startSlots = result.getSlots().get("日期起始");
        List<SlotValue> endSlots = result.getSlots().get("日期结束");
        List<SlotValue> definedSlots = result.getSlots().get("确定日期段");

        /* expected result */
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int week = currentDate.get(weekFields.weekOfWeekBasedYear());
        DateUtils.Date date0 = new DateUtils.Date(DateType.WEEK, currentDate.getYear(), 0, 0);
        date0.week = week;
        DirectionalDateUtils.DirectionalDate expDefinedDateDur =
                new DirectionalDateUtils.DirectionalDate(DateType.WEEK, date0, null, 2);
        SlotValue slot0 = new SlotValue(expDefinedDateDur,
                "确定日期段", "nextDateDuration(date_duration)", 2, 11, 2, 5);
        SlotValue slot1 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, currentDate.getYear(), 7, 20), null, 0),
                "日期起始", "startDate(date)", 15, 23, 9, 17);
        SlotValue slot2 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, currentDate.getYear(), 8, 10), 0),
                "日期结束", "endDate(date)", 26, 34, 20, 28);
        SlotValue slot3 = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                null, new DateUtils.Date(DateType.DAY, currentDate.getYear(), 8, 15), 0),
                "日期结束", "endDate(date)", 40, 48, 34, 38);
        slotValues.put("确定日期段", slot0);
        slotValues.put("日期起始", slot1);
        slotValues.put("日期结束", slot2);
        slotValues.put("日期结束", slot3);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("估计{{确定日期段}}会通知，{{日期起始}}，大概{{日期结束}}，也可能持续{{日期结束}}");
        expResult.setSlots(slotValues);
        List<SlotValue> expStartSlots = slotValues.get("日期起始");
        List<SlotValue> expEndSlots = slotValues.get("日期结束");
        List<SlotValue> expDefinedSlots = slotValues.get("确定日期段");

        /* verify */
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                /* verify slots has single element */
                () -> assertEquals(expStartSlots.size(), startSlots.size()),
                () -> assertEquals(expEndSlots.size(), endSlots.size()),
                () -> assertEquals(expDefinedSlots.size(), definedSlots.size()),
                /* verify matched string */
                () -> assertEquals(expStartSlots.get(0).matched.toString(), startSlots.get(0).matched.toString()),
                () -> assertEquals(expEndSlots.get(0).matched.toString(), endSlots.get(0).matched.toString()),
                () -> assertEquals(expDefinedSlots.get(0).matched.toString(), definedSlots.get(0).matched.toString()),
                /* verify label */
                () -> assertEquals(expStartSlots.get(0).label, startSlots.get(0).label),
                () -> assertEquals(expEndSlots.get(0).label, endSlots.get(0).label),
                () -> assertEquals(expDefinedSlots.get(0).label, definedSlots.get(0).label),
                /* verify start */
                () -> assertEquals(expStartSlots.get(0).start, startSlots.get(0).start),
                () -> assertEquals(expEndSlots.get(0).start, endSlots.get(0).start),
                () -> assertEquals(expDefinedSlots.get(0).start, definedSlots.get(0).start),
                /* verify end */
                () -> assertEquals(expStartSlots.get(0).end, startSlots.get(0).end),
                () -> assertEquals(expEndSlots.get(0).end, endSlots.get(0).end),
                () -> assertEquals(expDefinedSlots.get(0).end, definedSlots.get(0).end),
                /* verify real start */
                () -> assertEquals(expStartSlots.get(0).realStart, startSlots.get(0).realStart),
                () -> assertEquals(expEndSlots.get(0).realStart, endSlots.get(0).realStart),
                () -> assertEquals(expDefinedSlots.get(0).realStart, definedSlots.get(0).realStart),
                /* verify real end */
                () -> assertEquals(expStartSlots.get(0).realEnd, startSlots.get(0).realEnd),
                () -> assertEquals(expEndSlots.get(0).realEnd, endSlots.get(0).realEnd),
                () -> assertEquals(expDefinedSlots.get(0).realEnd, definedSlots.get(0).realEnd)
        );
    }
}