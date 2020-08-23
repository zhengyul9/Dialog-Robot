package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.DirectionalRegionUtils;
import ai.hual.labrador.utils.RegionType;
import ai.hual.labrador.utils.RegionUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhraseAnnotatorTest {

    private static GrammarModel grammarModel;

    private static PhraseAnnotator PA;

    @BeforeEach
    void setUp() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.PHRASE_REGEX, "prefix=不想吃的", "不(想|喜欢)吃{{口味}}.*{{食材}}", 3f),
                new Grammar(GrammarType.PHRASE_REGEX, "prefix=很",
                        "是?真的不(想|喜欢)吃{{不想吃的口味}}.*{{不想吃的食材}}之类的", 3f),
                new Grammar(GrammarType.PHRASE_REGEX, "prefix=prefix_", "{{x}}.*d.*{{y}}", 3f),
                new Grammar(GrammarType.PHRASE_REGEX, "suffix=_suffix", "a{{x}}.*d.*{{y}}", 2f),
                new Grammar(GrammarType.PHRASE_REGEX, "replace=人数", "{{数字}}个?人", 3f),
                new Grammar(GrammarType.PHRASE_REGEX, "replace=episode", "第{{数字}}集", 1f)
        ));

        PA = new PhraseAnnotator(grammarModel, new Properties());
    }

    @Test
    void phraseWithoutPositionMultiConvertTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "明天去北京和上海";
        pQuery = "明天去{{地区}}和{{地区}}";
        slots = ArrayListMultimap.create();
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "北京"),
                "地区", null, 3, 9, 3, 5));
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "上海"),
                "地区", null, 10, 16, 6, 8));
        QueryAct act = new QueryAct(query, pQuery, slots, 1f);

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.PHRASE_REGEX, "replace=到达地#regionToEndRegion(region)",
                        "去{{地区}}和{{地区}}", 1f)
        ));
        PA = new PhraseAnnotator(grammarModel, new Properties());
        res = PA.annotate(act);
        QueryAct result = res.get(0);
        assertEquals("明天去{{到达地}}和{{到达地}}", result.getPQuery());
        assertEquals(2, result.getSlots().get("到达地").size());
        assertTrue(result.getSlots().get("到达地").get(0).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(result.getSlots().get("到达地").get(1).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(0).matched).end != null);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(1).matched).end != null);
    }

    @Test
    void phraseWithoutPositionConvertTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "明天去北京和去上海";
        pQuery = "明天去{{地区}}和去{{地区}}";
        slots = ArrayListMultimap.create();
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "北京"),
                "地区", null, 3, 9, 3, 5));
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "上海"),
                "地区", null, 11, 17, 7, 9));
        QueryAct act = new QueryAct(query, pQuery, slots, 1f);

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.PHRASE_REGEX, "replace=到达地#regionToEndRegion(region)",
                        "去{{地区}}", 1f)
        ));
        PA = new PhraseAnnotator(grammarModel, new Properties());
        res = PA.annotate(act);
        QueryAct result = res.get(0);
        assertEquals("明天去{{到达地}}和去{{到达地}}", result.getPQuery());
        assertEquals(2, result.getSlots().get("到达地").size());
        assertTrue(result.getSlots().get("到达地").get(0).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(result.getSlots().get("到达地").get(1).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(0).matched).end != null);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(1).matched).end != null);
    }

    @Test
    void phraseWithOneConvertTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "明天去北京5个人";
        pQuery = "明天去{{地区}}{{数字}}个人";
        slots = ArrayListMultimap.create();
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "北京"),
                "地区", null, 3, 9, 3, 5));
        slots.put("数字", new SlotValue("5", "数字", "getDigits(str)", 9, 15, 5, 6));
        QueryAct act = new QueryAct(query, pQuery, slots, 1f);

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.PHRASE_REGEX, "replace=到达地@1#regionToEndRegion(region),人数@2",
                        "去{{地区}}.*{{数字}}个人", 1f)
        ));
        PA = new PhraseAnnotator(grammarModel, new Properties());
        res = PA.annotate(act);
        QueryAct result = res.get(0);
        assertEquals("明天去{{到达地}}{{人数}}个人", result.getPQuery());
        assertEquals(1, result.getSlots().get("到达地").size());
        assertEquals(1, result.getSlots().get("人数").size());
        assertTrue(result.getSlots().get("到达地").get(0).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(0).matched).end != null);
        assertEquals(5.0, Double.parseDouble(result.getSlots().get("人数").get(0).matched.toString()));
    }

    @Test
    void phraseWithTwoConvertTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "明天去北京5从上海";
        pQuery = "明天去{{地区}}{{数字}}从{{地区}}";
        slots = ArrayListMultimap.create();
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "北京"),
                "地区", null, 3, 9, 3, 5));
        slots.put("数字", new SlotValue("5", "数字", "getDigits(str)", 9, 15, 5, 6));
        slots.put("地区", new SlotValue(new RegionUtils.Region(RegionType.CITY, "上海"),
                "地区", null, 16, 22, 7, 9));
        QueryAct act = new QueryAct(query, pQuery, slots, 1f);

        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.PHRASE_REGEX, "replace=到达地@1#regionToEndRegion(region),起始地@3#regionToStartRegion(region)",
                        "去{{地区}}.*从{{地区}}", 1f)
        ));
        PA = new PhraseAnnotator(grammarModel, new Properties());
        res = PA.annotate(act);
        QueryAct result = res.get(0);
        assertEquals("明天去{{到达地}}{{数字}}从{{起始地}}", result.getPQuery());
        assertEquals(1, result.getSlots().get("起始地").size());
        assertEquals(1, result.getSlots().get("到达地").size());
        assertTrue(result.getSlots().get("起始地").get(0).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(result.getSlots().get("到达地").get(0).matched instanceof DirectionalRegionUtils.DirectionalRegion);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("到达地").get(0).matched).end != null);
        assertTrue(((DirectionalRegionUtils.DirectionalRegion) result.getSlots().get("起始地").get(0).matched).start != null);
    }

    @Test
    void twoSimilarSlotPhraseAnnotateTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "我想看微微两笑很倾城第一集";
        pQuery = "{{wish}}看微微{{数字}}笑很倾城第{{数字}}集";
        slots = ArrayListMultimap.create();
        slots.put("wish", new SlotValue("我想", "wish", null, 0, 8, 0, 2));
        slots.put("数字", new SlotValue("2", "数字", "getDigits(str)", 11, 17, 5, 6));
        slots.put("数字", new SlotValue("1", "数字", "getDigits(str)", 22, 28, 11, 12));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f);
        res = PA.annotate(act);
        QueryAct result = res.get(0);
        assertEquals("{{wish}}看微微{{数字}}笑很倾城第{{episode}}集", result.getPQuery());

    }

    @Test
    void phraseAnnotateDislikePrefixRecursiveTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "我是真的不想吃甜的还有牛肉之类的";
        pQuery = "我是真的不想吃{{口味}}的还有{{食材}}之类的";
        slots = ArrayListMultimap.create();
        slots.put("口味", new SlotValue("甜", "口味", null, 7, 13, 7, 8));
        slots.put("食材", new SlotValue("牛肉", "食材", null, 16, 22, 11, 13));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f);
        res = PA.annotate(act);
        QueryAct result = res.get(0);

        assertEquals(1, res.size());
        assertEquals("我是真的不想吃{{很不想吃的口味}}的还有{{很不想吃的食材}}之类的", result.getPQuery());
        assertAll("start position",
                () -> assertEquals(7, (result.getSlots().get("很不想吃的口味").get(0).start)),
                () -> assertEquals(21, (result.getSlots().get("很不想吃的食材").get(0).start))
        );
        assertAll("end position",
                () -> assertEquals(18, (result.getSlots().get("很不想吃的口味").get(0).end)),
                () -> assertEquals(32, (result.getSlots().get("很不想吃的食材").get(0).end))
        );
        assertAll("realStart position",
                () -> assertEquals(7, (result.getSlots().get("很不想吃的口味").get(0).realStart)),
                () -> assertEquals(11, (result.getSlots().get("很不想吃的食材").get(0).realStart))
        );
        assertAll("realEnd position",
                () -> assertEquals(8, (result.getSlots().get("很不想吃的口味").get(0).realEnd)),
                () -> assertEquals(13, (result.getSlots().get("很不想吃的食材").get(0).realEnd))
        );
    }

    @Test
    void phraseAnnotatePeopleNumberPrefixTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;

        query = "今天有大概三个人，或者5人";
        pQuery = "今天有大概{{数字}}个人，或者{{数字}}人";
        slots = ArrayListMultimap.create();
        slots.put("数字", new SlotValue(3, "数字", null, 5, 11, 5, 6));
        slots.put("数字", new SlotValue(5, "数字", null, 16, 22, 11, 12));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f);
        res = PA.annotate(act);
        QueryAct result = res.get(0);

        assertEquals(1, res.size());
        assertEquals("今天有大概{{人数}}个人，或者{{人数}}人", result.getPQuery());
    }

    @Test
    void noMatchTest() {
        String query;
        List<QueryAct> res;

        query = "这个句子没有短语";
        QueryAct act = new QueryAct(query);
        res = PA.annotate(act);
        QueryAct result = res.get(0);

        assertEquals(1, res.size());
        assertEquals("这个句子没有短语", result.getPQuery());
        assertEquals(0, result.getSlots().size());
    }

    @Test
    void phraseAnnotatePrefixTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;


        query = "QafSQQdTTfQQ";
        pQuery = "Q{{z}}{{x}}QQd{{y}}{{w}}Q";
        slots = ArrayListMultimap.create();
        slots.put("z", new SlotValue("af", "z", null, 1, 6, 1, 3));
        slots.put("x", new SlotValue("S", "x", null, 6, 11, 3, 4));
        slots.put("y", new SlotValue("TT", "y", null, 14, 19, 7, 9));
        slots.put("w", new SlotValue("fQ", "w", null, 19, 24, 9, 11));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f);
        res = PA.annotate(act);
        QueryAct result = res.get(0);

        assertEquals(1, res.size());
        assertEquals("Q{{z}}{{prefix_x}}QQd{{prefix_y}}{{w}}Q", result.getPQuery());
        assertAll("start position",
                () -> assertEquals(33, (result.getSlots().get("w").get(0).start)),
                () -> assertEquals(1, (result.getSlots().get("z").get(0).start)),
                () -> assertEquals(6, (result.getSlots().get("prefix_x").get(0).start)),
                () -> assertEquals(21, (result.getSlots().get("prefix_y").get(0).start))
        );
        assertAll("end position",
                () -> assertEquals(38, (result.getSlots().get("w").get(0).end)),
                () -> assertEquals(6, (result.getSlots().get("z").get(0).end)),
                () -> assertEquals(18, (result.getSlots().get("prefix_x").get(0).end)),
                () -> assertEquals(33, (result.getSlots().get("prefix_y").get(0).end))
        );
        assertAll("realStart position",
                () -> assertEquals(9, (result.getSlots().get("w").get(0).realStart)),
                () -> assertEquals(1, (result.getSlots().get("z").get(0).realStart)),
                () -> assertEquals(3, (result.getSlots().get("prefix_x").get(0).realStart)),
                () -> assertEquals(7, (result.getSlots().get("prefix_y").get(0).realStart))
        );
        assertAll("realEnd position",
                () -> assertEquals(11, (result.getSlots().get("w").get(0).realEnd)),
                () -> assertEquals(3, (result.getSlots().get("z").get(0).realEnd)),
                () -> assertEquals(4, (result.getSlots().get("prefix_x").get(0).realEnd)),
                () -> assertEquals(9, (result.getSlots().get("prefix_y").get(0).realEnd))
        );
    }

    @Test
    void phraseAnnotateSuffixTest() {
        String query;
        String pQuery;
        ListMultimap<String, SlotValue> slots;
        List<QueryAct> res;


        query = "QaSQQdTTfQQ";
        pQuery = "Qa{{x}}QQd{{y}}QQ";
        slots = ArrayListMultimap.create();
        slots.put("x", new SlotValue("S", "x", null, 2, 7, 2, 3));
        slots.put("y", new SlotValue("TT", "y", null, 10, 15, 6, 8));

        QueryAct act = new QueryAct(query, pQuery, slots, 1f);
        res = PA.annotate(act);
        QueryAct result = res.get(0);

        assertEquals(1, res.size());
        assertEquals("Qa{{x_suffix}}QQd{{y_suffix}}QQ", result.getPQuery());
        assertAll("start position",
                () -> assertEquals(2, (result.getSlots().get("x_suffix").get(0).start)),
                () -> assertEquals(17, (result.getSlots().get("y_suffix").get(0).start))
        );
        assertAll("end position",
                () -> assertEquals(14, (result.getSlots().get("x_suffix").get(0).end)),
                () -> assertEquals(29, (result.getSlots().get("y_suffix").get(0).end))
        );
        assertAll("realStart position",
                () -> assertEquals(2, (result.getSlots().get("x_suffix").get(0).realStart)),
                () -> assertEquals(6, (result.getSlots().get("y_suffix").get(0).realStart))
        );
        assertAll("realEnd position",
                () -> assertEquals(3, (result.getSlots().get("x_suffix").get(0).realEnd)),
                () -> assertEquals(8, (result.getSlots().get("y_suffix").get(0).realEnd))
        );
    }
}