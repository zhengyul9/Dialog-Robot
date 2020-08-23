package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.utils.IntentLabelUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.annotators.GrammarAnnotator.GRAMMAR_PREPROCESSOR_PROP;
import static ai.hual.labrador.nlu.annotators.GrammarAnnotator.STOP_WORD_PREPROCESSOR;
import static ai.hual.labrador.utils.ScoreUtils.patternMatchDiscountScore;
import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test {@link ai.hual.labrador.nlu.annotators.GrammarAnnotator}
 * Created by Dai Wentao on 2017/7/12.
 */
class GrammarAnnotatorTest {

    private static Properties properties;
    private static GrammarModel grammarModel;
    private static GrammarAnnotator IA;

    @BeforeEach
    void setUp() {
        properties = new Properties();
    }

    /**
     * When there is a regex "{{A}}", and  there are two acts before grammar annotator:
     * act1: b{{A}}c{{A}}t  act2: bac{{A}}t
     * if act1 matched first encountered regex pattern, which is slotA (slotA1.realLength = 1)
     * while act2 matched the only slotA (slotA2.realLength = 2),
     * then, in the end, act2 may have a higher overall score due to the bigger real matched length.
     * In order to eliminate this unintended behavior, regex should match the pattern with max
     * real length if multiple pattern exist.
     * In this test, the right move is to match the second slot A.
     */
    @Test
    void testAnnotateWithMultipleMatchChooseMaxRealLength() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);

        // input
        String query = "bacaataaas";
        String pQuery = "b{{A}}c{{A}}t{{A}}s";
        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();
        slots.put("A", new SlotValue("a", "A", null, 1, 6, 1, 2));
        slots.put("A", new SlotValue("aa", "A", null, 7, 12, 3, 5));
        slots.put("A", new SlotValue("aaa", "A", null, 13, 18, 6, 9));

        QueryAct act = new QueryAct(query, pQuery, slots, 1);

        List<QueryAct> resultList = IA.annotate(act);
        assertEquals(1, resultList.size());
        QueryAct result = resultList.get(0);
        assertEquals("intentA", result.getIntent());
        assertEquals(6, result.getRegexRealStart());
        assertEquals(9, result.getRegexRealEnd());
        assertEquals(13, result.getRegexStart());
        assertEquals(18, result.getRegexEnd());
    }

    @Test
    void testAnnotateWithMultipleMatchWithSameLength() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);

        // input
        String query = "baacaat";
        String pQuery = "b{{A}}c{{A}}t";
        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();
        slots.put("A", new SlotValue("aa", "A", null, 1, 6, 1, 3));
        slots.put("A", new SlotValue("aa", "A", null, 7, 12, 4, 6));

        QueryAct act = new QueryAct(query, pQuery, slots, 1);

        List<QueryAct> resultList = IA.annotate(act);
        assertEquals(1, resultList.size());
        QueryAct result = resultList.get(0);
        assertEquals("intentA", result.getIntent());
        assertEquals(1, result.getRegexRealStart());
        assertEquals(3, result.getRegexRealEnd());
        assertEquals(1, result.getRegexStart());
        assertEquals(6, result.getRegexEnd());
    }

    @Test
    void testAnnotateWithMultipleMatchWithSameLengthAndDiffLength() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);

        // input
        String query = "baacaataaas";
        String pQuery = "b{{A}}c{{A}}t{{A}}s";
        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();
        slots.put("A", new SlotValue("aa", "A", null, 1, 6, 1, 3));
        slots.put("A", new SlotValue("aa", "A", null, 7, 12, 4, 6));
        slots.put("A", new SlotValue("aaa", "A", null, 13, 18, 7, 10));

        QueryAct act = new QueryAct(query, pQuery, slots, 1);

        List<QueryAct> resultList = IA.annotate(act);
        assertEquals(1, resultList.size());
        QueryAct result = resultList.get(0);
        assertEquals("intentA", result.getIntent());
        assertEquals(7, result.getRegexRealStart());
        assertEquals(10, result.getRegexRealEnd());
        assertEquals(13, result.getRegexStart());
        assertEquals(18, result.getRegexEnd());
    }

    @Test
    void testAnnotateBothWithStopWordsAndWithout() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}的吧", 1.0f),
                new Grammar(GrammarType.INTENT_REGEX, "intentARemove", "{{A}}", 1.0f)
        ));
        properties.setProperty(GRAMMAR_PREPROCESSOR_PROP, STOP_WORD_PREPROCESSOR);
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "{{A}}的吧";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        assertEquals(3, resultList.size());
        assertEquals("intentA", resultList.get(0).getIntent());
        assertEquals("intentARemove", resultList.get(1).getIntent());
        assertEquals("intentARemove", resultList.get(1).getIntent());
    }

    @Test
    void testDoNotRemoveStopWordsInsideSlot() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA1", "{{A吧}}", 1.0f),
                new Grammar(GrammarType.INTENT_REGEX, "intentA2", "{{请问A}}", 1.0f)
        ));
        properties.setProperty(GRAMMAR_PREPROCESSOR_PROP, STOP_WORD_PREPROCESSOR);
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input1 = "{{A吧}}吧";
        String input2 = "请问{{请问A}}了";

        // output
        List<QueryAct> resultList1 = IA.annotate(new QueryAct(input1));
        List<QueryAct> resultList2 = IA.annotate(new QueryAct(input2));
        assertEquals(2, resultList1.size());
        assertEquals(2, resultList2.size());
        assertEquals("intentA1", resultList1.get(0).getIntent());
        assertEquals("intentA1", resultList1.get(1).getIntent());
        assertEquals("intentA2", resultList2.get(0).getIntent());
        assertEquals("intentA2", resultList2.get(1).getIntent());
    }

    @Test
    void testStopWords() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}", 1.0f)
        ));
        properties.setProperty(GRAMMAR_PREPROCESSOR_PROP, STOP_WORD_PREPROCESSOR);
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input1 = "{{A}}嘛?";
        String input2 = "请问{{A}}啊";

        // output
        List<QueryAct> resultList1 = IA.annotate(new QueryAct(input1));
        List<QueryAct> resultList2 = IA.annotate(new QueryAct(input2));
        assertEquals(2, resultList1.size());
        assertEquals(2, resultList2.size());
        assertEquals("intentA", resultList1.get(0).getIntent());
        assertEquals("intentA", resultList1.get(1).getIntent());
        assertEquals("intentA", resultList1.get(0).getIntent());
        assertEquals("intentA", resultList2.get(1).getIntent());
    }

    @Test
    void testAnnotateWithSlotRightTemplateScore() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "基金搜索", "基金.*介绍", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input1 = "{{基金对象}}基金给我介绍介绍";
        String input2 = "货币型基金给我介绍介绍";

        // output
        List<QueryAct> resultList1 = IA.annotate(new QueryAct(input1));
        List<QueryAct> resultList2 = IA.annotate(new QueryAct(input2));
        assertEquals(1, resultList1.size());
        assertEquals(1, resultList2.size());
        assertEquals("基金搜索", resultList1.get(0).getIntent());
        assertEquals("基金搜索", resultList2.get(0).getIntent());
        assertTrue(resultList1.get(0).getScore() == resultList2.get(0).getScore());
    }


    @Test
    void tempTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "query", "{{人寿保险}}", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "{{人寿保险}}";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        assertEquals(1, resultList.size());
        assertEquals("query", resultList.get(0).getIntent());
        assertEquals("\\{\\{人寿保险}}", resultList.get(0).getRegex());
        assertEquals(0, resultList.get(0).getRegexStart());
        assertEquals(8, resultList.get(0).getRegexEnd());
        assertEquals(0, resultList.get(0).getRegexRealStart());
        assertEquals(8, resultList.get(0).getRegexRealEnd());
    }

    @Test
    void matchFirstEncounteredRegexTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "借款_c", "{{借款_c}}", 1.0f),
                new Grammar(GrammarType.INTENT_REGEX, "逾期_sc", "{{逾期_sc}}", 1.0f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "{{日期}}{{日期结束}}{{借款_c}}{{逾期_c}}的后果";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        assertEquals(1, resultList.size());
        assertEquals("借款_c", resultList.get(0).getIntent());
    }

    @Test
    void containPervertRegexTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "bullshit?",
                        "ab\\{\\{cd}}\\?\\.\\*\\*\\*e.*{{xxx}}.*xb\\{.*{{yy}}.*\\(da\\(x]", 1f)));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "ab{{cd}}?.***e{{xxx}}xb{{{yy}}(da(x]";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "bullshit?";

        assertEquals(expPQuery, result.getIntent());
    }

    @Test
    void containRegexTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "逾期信息?逾期信息_c=逾期信息",
                        "您的好友\\*\\*\\\\\\*\\*逾期欠款已\\.\\*全部\\{\\{清偿}}", 1f)));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "您的好友**\\**逾期欠款已.*全部{{清偿}}";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        IntentLabelUtils.extractExtraSlot(resultList);
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "逾期信息";

        assertEquals(expPQuery, result.getIntent());
    }

    @Test
    void intentRegexSecondTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/QUERY?area=日本&category=电视剧", "日剧", 10f),
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/QUERY", "(我要|我想|给我|找点|想)?(看|来|介绍|播放|查找)?({{actor}}|{{area}}|{{language}}|{{tag}}|{{type}}|{{award}}|{{sub_award}})", 0.2f),
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/QUERY", "{{type}}", 0.1f)));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "{{type}}类日剧";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        IntentLabelUtils.extractExtraSlot(resultList);
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "VIDEO/QUERY";

        assertEquals(expPQuery, result.getIntent());

    }

    @Test
    void intentRegexTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/RECOMMEND", "(我要|我想|给我|找点|想)(看|来|介绍|播放|查找)({{category}})?({{cartoon}}|{{children}}|{{movie}}|{{education}}|{{doc}}|{{variety}}|{{tv}}|{{sport}})", 1f),
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/RECOMMEND", "(我要|我想|给我|找点|想)(看|来|介绍|播放|查找)({{cartoon}}|{{children}}|{{movie}}|{{education}}|{{doc}}|{{variety}}|{{tv}}|{{sport}})({{category}})?", 1.1f),
                new Grammar(GrammarType.INTENT_REGEX, "VIDEO/RECOMMEND", "(我要|我想|给我|找点|想)?(看|来|介绍|播放|查找)?({{cartoon}}|{{children}}|{{movie}}|{{education}}|{{doc}}|{{variety}}|{{tv}}|{{sport}})({{category}})?", 1.2f)));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "我想看{{variety}}";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "VIDEO/RECOMMEND";

        assertEquals(expPQuery, result.getIntent());

    }

    @Test
    void WordWithNumberIntentAnnotateTest() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "watch_movie", "^看{{movie}}$", 3f)
        ));
        IA = new GrammarAnnotator(grammarModel, properties);
        // input
        String input = "看{{movie}}";

        // output
        List<QueryAct> resultList = IA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "watch_movie";

        assertEquals(expPQuery, result.getIntent());
        assertEquals("^看\\{\\{movie}}$", result.getRegex());
        assertEquals(0, result.getRegexStart());
        assertEquals(10, result.getRegexEnd());
        assertEquals(0, result.getRegexRealStart());
        assertEquals(10, result.getRegexRealEnd());
    }

    @Test
    void testAnnotate() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "fff", "a.*{{x}}.*d.*{{y}}f", 3f),
                new Grammar(GrammarType.INTENT_REGEX, "ggg?hh=ii", "a.*{{x}}.*d.*{{y}}f", 2f)));
        IA = new GrammarAnnotator(grammarModel, properties);
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        GrammarAnnotator IA = new GrammarAnnotator(grammarModel, properties);

        query = "QaSQQdTTfQQ";
        pQuery = "Qa{{x}}QQd{{y}}fQQ";
        slots = ArrayListMultimap.create();
        slots.put("x", new SlotValue("S", "x", null, 2, 7, 2, 3));
        slots.put("y", new SlotValue("TT", "y", null, 10, 15, 6, 8));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f * slotScore(2));
        res = IA.annotate(act);
        IntentLabelUtils.extractExtraSlot(res);

        assertEquals(2, res.size());
        assertEquals("fff", res.get(0).getIntent());
        int matchLength = query.length() - 3;
        double slotScore1 = 1 * slotScore(2);
        double patternMatchScore1 = patternMatchDiscountScore("a.*\\{\\{x}}.*d.*\\{\\{y}}f", pQuery);
        assertEquals(slotScore1 * matchLength * patternMatchScore1 * 3f, res.get(0).getScore());
        assertEquals("ggg", res.get(1).getIntent());
        double slotScore2 = 1 * slotScore(2);
        double patternMatchScore2 = patternMatchDiscountScore("a.*\\{\\{x}}.*d.*\\{\\{y}}f", pQuery);
        assertEquals(slotScore2 * matchLength * patternMatchScore2 * 2f, res.get(1).getScore());
        assertEquals(1, res.get(1).getSlots().get("hh").size());
        assertEquals("ii", res.get(1).getSlots().get("hh").get(0).getMatched());

        query = "QaSQQdTTfQQ";
        pQuery = "Q{{a}}{{x}}QQd{{y}}fQQ";
        slots = ArrayListMultimap.create();
        slots.put("a", new SlotValue("a", "a", null, 1, 6, 1, 2));
        slots.put("x", new SlotValue("sss", "x", null, 6, 11, 2, 3));
        slots.put("y", new SlotValue("ttt", "y", null, 14, 19, 6, 8));

        res = IA.annotate(new QueryAct(query, pQuery, slots, 1f));
        assertEquals(1, res.size());
    }

}