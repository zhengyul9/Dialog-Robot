package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ethan on 17-7-11.
 */
class NumAnnotatorTest {

    private static NumAnnotator NA;

    @BeforeAll
    static void prepareData() {
        NA = new NumAnnotator();
    }

    @Test
    void testAnnotateNumWithOverlapDictSlots() {

        //  construct inputs
        String query = "看一看晨星评级在三星以上的";
        String pQuery = "看一看晨星{{星级}}以上的";
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot1 = new SlotValue("四星", "星级", "", 5, 11, 5, 12);
        SlotValue slot2 = new SlotValue("五星", "星级", "", 0, 0, 5, 12);
        SlotValue slot3 = new SlotValue("四星", "星级", "", 0, 0, 5, 12);
        slotValues.put("星级", slot1);
        slotValues.put("星级", slot2);
        slotValues.put("星级", slot3);
        QueryAct queryAct = new QueryAct(query, pQuery, slotValues, 1.05f);

        //  output result
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("数字");
    }

    @Test
    void testAnnotateMarkPercentNumber() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("20%"));
        assertEquals(1, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}", resultList.get(0).getPQuery());
        assertEquals(0.2, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void testAnnotateChinesePercentNumber() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("百分之20"));
        assertEquals(1, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}", resultList.get(0).getPQuery());
        assertEquals(0.2, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void testAnnotatePercentNumber() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("20/100"));
        assertEquals(1, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}", resultList.get(0).getPQuery());
        assertEquals(0.2, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void slotKeyContainNumberTest() {
        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();
        slots.put("类型三", new SlotValue("实体三", "类型三", "", 0, 7, 0, 3));
        QueryAct queryAct = new QueryAct("实体三", "{{类型三}}", null, slots, 1d);
        List<QueryAct> result = NA.annotate(queryAct);
        assertEquals(1, result.size());
        assertEquals(queryAct.getPQuery(), result.get(0).getPQuery());
    }

    @Test
    void mixedDecimalExpressionWithoutConsecutiveExtractedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("20.五十三"));  // this is an expression for time
        assertEquals(2, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}.{{数字}}", resultList.get(0).getPQuery());
    }

    @Test
    void mixedDecimalExpressionWithConsecutiveExtractedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("20.五三六"));  // this is an expression for time
        assertEquals(1, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}", resultList.get(0).getPQuery());
        assertEquals(20.536, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void mixedDecimalExpressionExtractedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("2点五零"));  // this is an expression for time
        assertEquals(1, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}", resultList.get(0).getPQuery());
        assertEquals(2.5, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void mixedTimeExpressionWithoutConsecutiveNotExtractTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("3点五十六"));  // this is an expression for time
        assertEquals(2, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}点{{数字}}", resultList.get(0).getPQuery());
    }

    @Test
    void mixedTimeExpressionNotExtractTest1() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("2点五分"));  // this is an expression for time
        assertEquals(2, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}点{{数字}}分", resultList.get(0).getPQuery());
    }

    @Test
    void chineseTimeExpressionNotExtractTest1() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("两点五分"));  // this is an expression for time
        assertEquals(2, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}点{{数字}}分", resultList.get(0).getPQuery());
    }

    @Test
    void chineseTimeExpressionNotExtractTest2() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("两点五十"));  // this is an expression for time
        assertEquals(2, resultList.get(0).getSlots().get("数字").size());
        assertEquals("{{数字}}点{{数字}}", resultList.get(0).getPQuery());
    }

    @Test
    void fractionWithCharMixedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("2/五十"));
        assertEquals(0.04, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void fractionWithMarkMixedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("两百五分之5"));
        assertEquals(0.02, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void fractionWithMarkArabicTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("3/2"));
        assertEquals(1.5, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void fractionWithCharArabicTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("2分之6"));
        assertEquals(3.0, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void fractionWithCharChineseTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("三/二"));
        assertEquals(1.5, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void fractionWithMarkChineseTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("二分之三"));
        assertEquals(1.5, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void consecutiveChineseDigitsTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("五十三四十"));
        assertEquals("{{数字}}{{连续数字}}{{数字}}", resultList.get(0).getPQuery());
        List<SlotValue> slots = resultList.get(0).getSortedSlotAsList();
        assertEquals(3, slots.size());
        assertEquals(50.0, slots.get(0).matched);
        assertEquals(34.0, slots.get(1).matched);
        assertEquals(10.0, slots.get(2).matched);
    }

    @Test
    void consecutiveChineseDigitsWithZeroTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("01零一2"));
        assertEquals(1012.0, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void consecutiveAllChineseDigitsTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("一二一"));
        assertEquals(121.0, resultList.get(0).getSlots().get("连续数字").get(0).matched);
    }

    @Test
    void consecutiveAllChineseDigitsWithZeroTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("一零二四"));
        assertEquals(1024.0, resultList.get(0).getSlots().get("连续数字").get(0).matched);
    }

    @Test
    void consecutiveMixedDigitsWithZeroTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("01零一2"));
        assertEquals(1012.0, resultList.get(0).getSlots().get("数字").get(0).matched);
    }

    @Test
    void digitPlusChinesePervertedTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("1千两百3十六万5千1百二"));
        QueryAct result = resultList.get(0);
        assertEquals(12365120.0, result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void digitTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("不会是一种疾病保6万 100种疾病累计600万吧"));
        List<SlotValue> slots = resultList.get(0).getSlots().get("数字");
        assertEquals(4, slots.size());
        assertEquals(1.0, slots.get(0).matched);
        assertEquals(100.0, slots.get(1).matched);
        assertEquals(60000.0, slots.get(2).matched);
        assertEquals(6000000.0, slots.get(3).matched);
    }

    @Test
    void timeNumNotExtractedAsDecimalTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("下午一点二十给我播放一点电影"));
        Collections.sort(resultList);
        assertEquals("下午{{数字}}点{{数字}}给我播放{{数字}}点电影", resultList.get(0).getPQuery());
        assertEquals("下午一点二十给我播放一点电影", resultList.get(resultList.size() - 1).getPQuery());
    }

    @Test
    void decimalChineseTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("一一集"));
        List<QueryAct> resultList2 = NA.annotate(new QueryAct("一二一集"));
        Collections.sort(resultList);
        Collections.sort(resultList2);
        assertEquals("{{连续数字}}集", resultList.get(0).getPQuery());
        assertEquals("{{连续数字}}集", resultList2.get(0).getPQuery());
        assertEquals(11.0, (double) resultList.get(0).getSlots().get("连续数字").get(0).matched);
        assertEquals(121.0, (double) resultList2.get(0).getSlots().get("连续数字").get(0).matched);
    }

    @Test
    void mixedComplexQuantTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("三亿5百万20"));
        resultList = resultList.stream()
                .flatMap(act -> NA.annotate(act).stream())
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(resultList);
        QueryAct result = resultList.get(0);
        assertEquals(3.0500002e8, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void quantifierArabicTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("10个亿"));
        resultList = resultList.stream()
                .flatMap(act -> NA.annotate(act).stream()).distinct().collect(Collectors.toList());
        Collections.sort(resultList);
        QueryAct result = resultList.get(0);
        assertEquals(1e9f, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void quantifierChineseTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("两个亿"));
        resultList = resultList.stream()
                .flatMap(act -> NA.annotate(act).stream()).distinct().collect(Collectors.toList());
        Collections.sort(resultList);
        QueryAct result = resultList.get(0);
        assertEquals(2e8, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void mixedComplexTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("9万6千零二十五"));
        QueryAct result = resultList.get(0);
        assertEquals(96025.0, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void digitMixWithChineseTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("9万"));
        QueryAct result = resultList.get(0);
        assertEquals(90000.0, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void specialContinuedDigitWithPlusTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("10+20"));
        QueryAct result = resultList.get(0);
        assertEquals("{{数字}}+{{数字}}", result.getPQuery());
    }

    @Test
    void specialContinuedDigitTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("一两"));
        QueryAct result = resultList.get(0);
        assertEquals("consecutiveDigits(str)", result.getSlots().get("连续数字").get(0).label);
        assertEquals(12.0, result.getSlots().get("连续数字").get(0).matched);
        assertEquals("{{连续数字}}", result.getPQuery());
    }

    @Test
    void withDigitChineseOutputZeroForNowTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("一点五"));
        QueryAct result = resultList.get(0);
        assertEquals(1.5, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void withDigitOutputZeroForNowTest() {
        List<QueryAct> resultList = NA.annotate(new QueryAct("1.5"));
        QueryAct result = resultList.get(0);
        assertEquals(1.5, (double) result.getSlots().get("数字").get(0).matched);
    }

    @Test
    void zeroPrefixNumAnnotatorTest() {
        //  construct inputs 
        String query = "零五分02秒，零二十年";
        QueryAct queryAct = new QueryAct(query);

        //  output result 
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSortedSlotAsList();

        //  expected result 
        ListMultimap<String, SlotValue> expSlotValues = ArrayListMultimap.create();

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{数字}}分{{数字}}秒，{{数字}}年");
        SlotValue slot1 = new SlotValue(5.0, "数字", "getDigits(str)", 0, 6, 0, 2);
        SlotValue slot2 = new SlotValue(2.0, "数字", "getDigits(str)", 7, 13, 3, 5);
        SlotValue slot3 = new SlotValue(20.0, "数字", "getDigits(str)", 15, 21, 7, 10);
        expSlotValues.put("数字", slot1);
        expSlotValues.put("数字", slot2);
        expSlotValues.put("数字", slot3);
        expResult.setSlots(expSlotValues);
        List<SlotValue> expSlots = expResult.getSortedSlotAsList();

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                () -> assertEquals(expSlots.get(0).toString(), slots.get(0).toString()),
                () -> assertEquals(expSlots.get(1).toString(), slots.get(1).toString()),
                () -> assertEquals(expSlots.get(2).toString(), slots.get(2).toString())
        );
    }

    @Test
    void consecutiveNumIgnoredTest() {

        //  construct inputs 
        String query = "七八个月，大概一两分钟吧";
        QueryAct queryAct = new QueryAct(query);

        //  output result 
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSortedSlotAsList();

        //  expected result 
        ListMultimap<String, SlotValue> expSlotValues = ArrayListMultimap.create();

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{连续数字}}个月，大概{{连续数字}}分钟吧");
        expResult.setSlots(expSlotValues);

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery())
        );
        assertEquals(78.0, slots.get(0).matched);
        assertEquals("consecutiveDigits(str)", slots.get(0).label);
        assertEquals(12.0, slots.get(1).matched);
        assertEquals("consecutiveDigits(str)", slots.get(1).label);
    }

    @Test
    void numAnnotateTest1() {

        //  construct inputs 
        String query = "定个上午十点和下午二十三点的闹钟";
        QueryAct queryAct = new QueryAct(query);

        //  output result 
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("数字");

        //  expected result 
        //  pQuery: 定个上午{{数字}}点和下午{{数字}}点的闹钟 
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot1 = new SlotValue(10.0, "数字", "getDigits(str)", 4, 10, 4, 5);
        SlotValue slot2 = new SlotValue(23.0, "数字", "getDigits(str)", 14, 20, 9, 12);
        slotValues.put("数字", slot1);
        slotValues.put("数字", slot2);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("定个上午{{数字}}点和下午{{数字}}点的闹钟");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("数字");

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                //  check matched string 
                () -> assertEquals(expSlots.get(0).matched, slots.get(0).matched),
                () -> assertEquals(expSlots.get(1).matched, slots.get(1).matched),
                //  check label 
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                //  check start 
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                //  check end 
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                //  check real start 
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                //  check real end 
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd)
        );
    }

    @Test
    void numAnnotatorTest2() {

        //  construct inputs 
        String query = "提醒我2017年八月份大概十二日左右有会议";
        QueryAct queryAct = new QueryAct(query);

        //  output result 
        //  pQuery: 提醒我{{数字}}年{{数字}}月份大概{{数字}}日左右有会议 
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        List<SlotValue> slots = result.getSlots().get("数字");

        //  expected result 
        ListMultimap<String, SlotValue> slotValues = ArrayListMultimap.create();
        SlotValue slot1 = new SlotValue(2017.0, "数字", "getDigits(str)", 3, 9, 3, 7);
        SlotValue slot2 = new SlotValue(8.0, "数字", "getDigits(str)", 10, 16, 8, 9);
        SlotValue slot3 = new SlotValue(12.0, "数字", "getDigits(str)", 20, 26, 13, 15);
        slotValues.put("数字", slot1);
        slotValues.put("数字", slot2);
        slotValues.put("数字", slot3);

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("提醒我{{数字}}年{{数字}}月份大概{{数字}}日左右有会议");
        expResult.setSlots(slotValues);
        List<SlotValue> expSlots = slotValues.get("数字");

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery()),
                //  check matched string 
                () -> assertEquals(expSlots.get(0).matched, slots.get(0).matched),
                () -> assertEquals(expSlots.get(1).matched, slots.get(1).matched),
                () -> assertEquals(expSlots.get(2).matched, slots.get(2).matched),
                //  check label 
                () -> assertEquals(expSlots.get(0).label, slots.get(0).label),
                () -> assertEquals(expSlots.get(1).label, slots.get(1).label),
                () -> assertEquals(expSlots.get(2).label, slots.get(2).label),
                //  check start 
                () -> assertEquals(expSlots.get(0).start, slots.get(0).start),
                () -> assertEquals(expSlots.get(1).start, slots.get(1).start),
                () -> assertEquals(expSlots.get(2).start, slots.get(2).start),
                //  check end 
                () -> assertEquals(expSlots.get(0).end, slots.get(0).end),
                () -> assertEquals(expSlots.get(1).end, slots.get(1).end),
                () -> assertEquals(expSlots.get(2).end, slots.get(2).end),
                //  check real start 
                () -> assertEquals(expSlots.get(0).realStart, slots.get(0).realStart),
                () -> assertEquals(expSlots.get(1).realStart, slots.get(1).realStart),
                () -> assertEquals(expSlots.get(2).realStart, slots.get(2).realStart),
                //  check real end 
                () -> assertEquals(expSlots.get(0).realEnd, slots.get(0).realEnd),
                () -> assertEquals(expSlots.get(1).realEnd, slots.get(1).realEnd),
                () -> assertEquals(expSlots.get(2).realEnd, slots.get(2).realEnd)
        );
    }

    @Test
    void numAnnotatorFailChineseYearExpressionTest() {

        //  construct inputs 
        String query = "一七年和一九八五年";
        QueryAct queryAct = new QueryAct(query);

        //  output result 
        List<QueryAct> resultList = NA.annotate(queryAct);
        QueryAct result = resultList.get(0);
        ListMultimap<String, SlotValue> slots = result.getSlots();

        //  expected result 
        ListMultimap<String, SlotValue> expSlotValues = ArrayListMultimap.create();

        QueryAct expResult = new QueryAct(query);
        expResult.setPQuery("{{连续数字}}年和{{连续数字}}年");
        expResult.setSlots(expSlotValues);

        //  verify 
        assertAll("Assert result match",
                () -> assertEquals(expResult.getPQuery(), result.getPQuery())
        );
        assertEquals(17.0, slots.get("连续数字").get(0).matched);
        assertEquals("consecutiveDigits(str)", slots.get("连续数字").get(0).label);
        assertEquals(1985.0, slots.get("连续数字").get(1).matched);
        assertEquals("consecutiveDigits(str)", slots.get("连续数字").get(1).label);
    }

    @Test
    void transformTest() {

        //  construct inputs 
        SlotValuePack valuePack = new SlotValuePack("二十三", new ArrayList<>(),
                "数字", "getDigits(str)", 0, 0, 0, 0);

        //  output result 
        SlotValue result = NA.transform(valuePack, ArrayListMultimap.create());

        //  expected result 
        SlotValue expResult = new SlotValue(23.0);

        //  verify 
        assertEquals(expResult.matched, result.matched);
    }

}