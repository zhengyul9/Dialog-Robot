package ai.hual.labrador.nlu;

import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dialog.GrammarHandler;
import ai.hual.labrador.nlu.annotators.GrammarAnnotator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.NLUImpl.PREPROCESSOR_PROP_NAME;
import static ai.hual.labrador.nlu.annotators.DictAnnotator.DICT_COLLECTION_POLICY_PROP_NAME;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class NLUImplTest {

    private static DictModel dictModel;
    private static GrammarModel grammarModel;
    private static NLUImpl nlu;
    private static Properties properties;

    @BeforeEach
    void setup() {
        properties = new Properties();
        properties.setProperty(PREPROCESSOR_PROP_NAME, "bracePreprocessor");
        properties.put("nlu.intentMatchers", "templateIntentMatcher");
        grammarModel = new GrammarModel();
        dictModel = new DictModel();
    }

    @Test
    void testRealRegexPosition() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "a")
        ));

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intent",
                        "ct", 1.0f)
        ));
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "abbct";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals(2, resultList.size());
        assertEquals("{{A}}bbct", resultList.get(0).getPQuery());
        assertEquals(7, resultList.get(0).getRegexStart());
        assertEquals(9, resultList.get(0).getRegexEnd());
        assertEquals(3, resultList.get(0).getRegexRealStart());
        assertEquals(5, resultList.get(0).getRegexRealEnd());
    }

    @Test
    void testDistinctAnnotator() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "a"),
                new Dict("A", "a")
        ));

        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "a";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals(2, resultList.size());
        assertEquals("{{A}}", resultList.get(0).getPQuery());
        assertEquals("a", resultList.get(1).getPQuery());
    }

    @Test
    void testAnnotateDateTimeOut() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("日期1", "2016年10月20日"),
                new Dict("日期2", "2016年12月12日"),
                new Dict("日期1+2", "2016年10月20日到2016年12月12日")
        ));

        grammarModel = new GrammarModel();
        GrammarHandler grammarHandler = new GrammarHandler(new ArrayList<>());
        grammarModel = grammarHandler.handleGrammar(grammarModel);

        properties.put("nlu.intentMatchers", "");
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                NLUImpl.DEFAULT_ANNOTATORS, properties);
        // input
        String input = "2016年10月20日到2016年12月12日";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        QueryAct result = resultList.get(0);

        assertTrue(resultList.size() == NLUImpl.MAX_LENGTH);
        assertEquals("{{日期1+2}}", result.getPQuery());
    }

    @Test
    void testUnderstandWithOverlapDictMinusOneExist() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("日期", "f", "今天"),
                new Dict("日期", "u", "今天"),
                new Dict("日期", "今天", "c,k")
        ));

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "确认日期",
                        "是{{日期}}", 1.0f)
        ));

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "是今天嘛";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        QueryAct result = resultList.get(0);

        assertEquals("确认日期", result.getIntent());
        assertEquals("是{{日期}}嘛", result.getPQuery());
        assertEquals(3, result.getSlots().size());
    }

    @Test
    void testUnderstandWithOverlapDict() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "a", "t"),
                new Dict("B", "b", "t"),
                new Dict("T", "t", "P")
        ));

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "Intent",
                        "y{{T}}z", 1.0f),
                new Grammar(GrammarType.PHRASE_REGEX,
                        "replace=REPLACE", "{{T}}f", 1f)
        ));

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "xtytztf";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        QueryAct result = resultList.get(0);

        assertEquals("Intent", result.getIntent());
        assertEquals("x{{T}}y{{T}}z{{REPLACE}}f", result.getPQuery());
        assertEquals(3, result.getSlots().size());
    }

    @Test
    void dateDurationConflictWithAgeTest() {

        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                NLUImpl.DEFAULT_ANNOTATORS, properties);
        // input
        String input = "60周岁以上";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        QueryAct result = resultList.get(0);
        assertEquals("{{数字}}周岁以上", result.getPQuery());
    }

    @Test
    void regionAnnotateTest() {

        List<String> annotators = new ArrayList<>();

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        properties.put("nlu.annotators", "numAnnotator,timeAnnotator,dateAnnotator,regionAnnotator,directionalRegionAnnotator");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "2017-01-01";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("2017-01-01", resultList.get(0).getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void dateAnnotateTest() {

        List<String> annotators = new ArrayList<>();

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        properties.put("nlu.annotators", "dictAnnotator,numAnnotator,dateAnnotator");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "2017-01-01";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("2017-01-01", resultList.get(0).getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void getAnnotatorsFromPropertyTest() {

        List<String> annotators = new ArrayList<>();

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        properties.put("nlu.annotators", "dictAnnotator,numAnnotator");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "9个亿";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("9.0E8", resultList.get(0).getSlots().get("数字").get(0).matched.toString());
    }

    @Test
    void mixedNumberTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "9个亿";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("9.0E8", resultList.get(0).getSlots().get("数字").get(0).matched.toString());
    }

    @Test
    void positiveLookBehindPhraseRegexWithIntentTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "小鬼当家")
        ));

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "WATCH/MOVIE",
                        "我(想|要|就想)看{{movie}}{{season}}", 1.0f),
                new Grammar(GrammarType.PHRASE_REGEX,
                        "replace=season", "(?<=^{{movie}}){{数字}}", 1f)
        ));

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想看小鬼当家3";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("WATCH/MOVIE", resultList.get(0).getIntent());
        assertEquals(0, resultList.get(0).getRegexStart());
        assertEquals(22, resultList.get(0).getRegexEnd());
        assertEquals(0, resultList.get(0).getRegexRealStart());
        assertEquals(8, resultList.get(0).getRegexRealEnd());
    }

    @Test
    void positiveLookBehindPhraseRegexTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "小鬼当家")
        ));

        Grammar episodeGrammar = new Grammar(GrammarType.PHRASE_REGEX,
                "replace=season", "(?<=^{{movie}}){{数字}}", 1f);
        List<Grammar> grammarList = Collections.singletonList(episodeGrammar);
        grammarModel = new GrammarModel(grammarList);
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想看小鬼当家3";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("我想看{{movie}}{{season}}", resultList.get(0).getPQuery());
    }

    @Test
    void positiveLookAheadPhraseRegexTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "小鬼当家")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        Grammar episodeGrammar = new Grammar(GrammarType.PHRASE_REGEX,
                "replace=movie_episode", "{{movie}}(?={{数字}})", 1f);
        List<Grammar> grammarList = Collections.singletonList(episodeGrammar);
        grammarModel = new GrammarModel(grammarList);
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想看小鬼当家3";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("我想看{{movie_episode}}{{数字}}", resultList.get(0).getPQuery());
    }

    @Test
    void wangMovieMultiPropertiesMaxLengthTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("director", "王力宏", "王力宏"),
                new Dict("actor", "王力宏", "王力宏"),
                new Dict("singer", "王力宏", "王力宏"),
                new Dict("actor", "王力"),
                new Dict("song", "龙的传人"),
                new Dict("song", "人")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        grammarModel = new GrammarModel();
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想听龙的传人王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏王力宏";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals(GrammarAnnotator.MAX_INTENT_LENGTH, resultList.size());
    }

    @Test
    void liuMovieMultiPropertiesTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("director", "刘德华"),
                new Dict("actor", "刘德华"),
                new Dict("singer", "刘德华")
        ));
        grammarModel = new GrammarModel();
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "刘德华的电影";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
    }

    @Test
    void specialNumCharTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );

        grammarModel = new GrammarModel();
        nlu = new NLUImpl(new DictModel(), grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "20+20袋加五十三四十";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("{{数字}}+{{数字}}袋加{{数字}}{{连续数字}}{{数字}}", resultList.get(0).getPQuery());
    }

    @Test
    void phraseTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator",
                "phraseAnnotator"
        );
        dictModel = new DictModel(Arrays.asList(
                new Dict("song", "想"),
                new Dict("wish", "我想")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        Grammar episodeGrammar = new Grammar(GrammarType.PHRASE_REGEX,
                "replace=episode", "第{{数字}}集", 1f);
        List<Grammar> grammarList = Collections.singletonList(episodeGrammar);
        grammarModel = new GrammarModel(grammarList);
        nlu = new NLUImpl(dictModel, grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想看微微一笑很倾城第一集";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("{{wish}}看微微{{数字}}笑很倾城第{{episode}}集", resultList.get(0).getPQuery());
    }

    @Test
    void yearChineseMonthDayTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "2012年五月";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("{{日期}}", resultList.get(0).getPQuery());
    }

    @Test
    void yearMonthDayTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "2012年10月11日";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();

        assertEquals("{{日期}}", resultList.get(0).getPQuery());
    }

    @Test
    void holyTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "dateDurationAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "这两天可能要开个持续5天的会，明后天下午5点的时候讨论，大概一两个小时吧";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("{{日期段}}可能要开个持续{{日期段}}的会，明{{日期}}{{时刻}}的时候讨论，大概{{连续数字}}个小时吧",
                resultList.get(0).getPQuery());
    }

    @Test
    void dateAndTimeAnnotateTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "前天下午5点的时候";


        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        String date = LocalDate.now().minusDays(2).toString();
        assertEquals(date, resultList.get(0).getSlots().get("日期").get(0).matched.toString());
        assertEquals("17:00:00", resultList.get(0).getSlots().get("时刻").get(0).matched.toString());
    }

    @Test
    void prev3YearDateAnnotateTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "大前年十月零五号";


        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        String year = Integer.toString(LocalDate.now().getYear() - 3);
        assertEquals(year + "-10-05", resultList.get(0).getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void next3YearDateAnnotateTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "大后年十月十五号";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        String year = Integer.toString(LocalDate.now().getYear() + 3);
        assertEquals(year + "-10-15", resultList.get(0).getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void lastYearDateAnnotateTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeDurationAnnotator");

        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(new DictModel(), new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "去年十月十五号";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        String year = Integer.toString(LocalDate.now().getYear() - 1);
        assertEquals(year + "-10-15", resultList.get(0).getSlots().get("日期").get(0).matched.toString());
    }

    @Test
    void interceptedPinyinWithTimeDictAnnotateTest2() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "timeDurationAnnotator");

        dictModel = new DictModel(Arrays.asList(
                new Dict("道", "道"),
                new Dict("song", "20秒")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "快进到1分20秒";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
    }

    @Test
    public void testAnnotatedCombinationWithDP() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");

        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba")
        ));
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        String query = "aaab";
        List<QueryAct> result = nlu.understand(query).retrieveHyps();
        assertEquals("{{Aa}}b", result.get(0).getPQuery());
    }

    @Test
    void interceptedPinyinDictAnnotateTest2() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");

        dictModel = new DictModel(Arrays.asList(
                new Dict("category", "电视剧"),
                new Dict("APP", "想看电视"),
                new Dict("action", "我想看电视剧"),
                new Dict("song", "想"),
                new Dict("wish", "我想")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        // input
        String input = "我想看电视剧";

        // output
        List<QueryAct> resultList = nlu.understand(input).retrieveHyps();
        assertEquals("{{action}}", resultList.get(0).getPQuery());
    }

    @Test
    public void specialStringWithSpaceTest() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");
        dictModel = new DictModel();
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        String query = "*** *** %^ _ ";
        List<QueryAct> result = nlu.understand(query).retrieveHyps();

        assertEquals("*** *** %^ _ ", result.get(0).getPQuery());
    }

    @Test
    public void pinyinDictTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");

        dictModel = new DictModel(Arrays.asList(
                new Dict("侦探", "柯南")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "true");
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);

        /* construct inputs */
        String query1 = "柯难破了3个案件";
        List<QueryAct> result = nlu.understand(query1).retrieveHyps();

        assertEquals("{{侦探}}破了{{数字}}个案件", result.get(0).getPQuery());
    }

    @Test
    public void vanillaDictTest() {

        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");
        dictModel = new DictModel(Arrays.asList(
                new Dict("侦探", "柯南")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");

        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);

        /* construct inputs */
        String query1 = "柯南破了3个案件";

        List<QueryAct> result = nlu.understand(query1).retrieveHyps();

        assertEquals("{{侦探}}破了{{数字}}个案件", result.get(0).getPQuery());
    }

    @Test
    void multipleLabelAnnotateTest() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");
        dictModel = new DictModel(Arrays.asList(
                new Dict("歌手", "周杰伦", "周董,杰伦"),
                new Dict("演员", "周杰伦", "周董,杰伦"),
                new Dict("导演", "周杰伦", "周董,杰伦"),
                new Dict("歌手", "林俊杰", "JJ"),
                new Dict("Dota主播", "林俊杰", "JJ")
        ));
        properties.put("nlu.dictAnnotator.usePinyinRobust", "false");

        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        String str;
        List<QueryAct> res;

        str = "我想听临俊杰和周懂的歌";
        res = nlu.understand(str).retrieveHyps();
        for (QueryAct act : res) {
            if (act.getPQuery().equals("我想听{{歌手}}和{{歌手}}的歌")) {
                assertIterableEquals(Arrays.asList("林俊杰", "周杰伦"), act.getSlotAsStringList("歌手"));
            } else if (act.getPQuery().equals("我想听{{歌手}}和{{导演}}的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
            } else if (act.getPQuery().equals("我想听{{歌手}}和{{演员}}的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
            } else if (act.getPQuery().equals("我想听{{Dota主播}}和{{歌手}}的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("歌手"));
            } else if (act.getPQuery().equals("我想听{{Dota主播}}和{{导演}}的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
            } else if (act.getPQuery().equals("我想听{{Dota主播}}和{{演员}}的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
            } else if (act.getPQuery().equals("我想听{{Dota主播}}和周懂的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
            } else if (act.getPQuery().equals("我想听{{歌手}}和周懂的歌")) {
                assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
            } else if (act.getPQuery().equals("我想听临俊杰和{{演员}}的歌")) {
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
            } else if (act.getPQuery().equals("我想听临俊杰和{{导演}}的歌")) {
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
            } else if (act.getPQuery().equals("我想听临俊杰和{{歌手}}的歌")) {
                assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("歌手"));
            } else if (act.getPQuery().equals("我想听临俊杰和周懂的歌")) {
                assertEquals(0, act.getSlots().values().size());
            } else {
                assertTrue(false);
            }
        }
    }
}
