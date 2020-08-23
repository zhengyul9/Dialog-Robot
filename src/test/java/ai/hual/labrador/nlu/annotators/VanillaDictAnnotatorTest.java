package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test {@link ai.hual.labrador.nlu.annotators.DictAnnotator}
 * Created by Dai Wentao on 2017/7/12.
 */
class VanillaDictAnnotatorTest {

    private static DictModel dictModel;
    private static Properties properties = new Properties();

    @BeforeAll
    static void setUp() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("wildAb", "a?b"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba"),
                new Dict("长", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                new Dict("长", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
                new Dict("歌手", "周杰伦", "周董,杰伦"),
                new Dict("演员", "周杰伦", "周董,杰伦"),
                new Dict("导演", "周杰伦", "周董,杰伦"),
                new Dict("歌手", "林俊杰", "JJ"),
                new Dict("Dota主播", "林俊杰", "JJ")
        ));
        properties.setProperty("nlu.dictAnnotator.usePinyinRobust", "false");
    }

    @Test
    void testMultiWildCardWithInterception() {
        // wildAb2 and wildAb1 has higher priority than wildAb3 at the third char,
        // so it won't be extracted
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb3", "a??b?"),
                new Dict("wildAb2", "a?b?"),
                new Dict("wildAb1", "a?b")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "acbbt";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals(3, res.size());
        assertEquals("{{wildAb2}}t", res.get(0).getPQuery());
        assertEquals("{{wildAb1}}bt", res.get(1).getPQuery());
        assertEquals(str1, res.get(2).getPQuery());
    }

    @Test
    void testWildCardWithInterception() {
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb2", "a?b?"),
                new Dict("wildAb1", "a?b")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "ppacbt";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals("pp{{wildAb2}}", res.get(0).getPQuery());
        assertEquals("pp{{wildAb1}}t", res.get(1).getPQuery());
    }

    @Test
    void testWildCardWithMoreSpecific() {
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb", "a?b"),
                new Dict("Ac", "acb")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "ppacb";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals(2, res.size());
        assertEquals("pp{{Ac}}", res.get(0).getPQuery());
    }

    @Test
    void testWildCard() {
        List<QueryAct> res;

        DictAnnotator annotator = new DictAnnotator(dictModel, properties);

        String str1 = "ppacb";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals("pp{{wildAb}}", res.get(0).getPQuery());
    }

    @Test
    void testInterception1() {
        List<QueryAct> res;

        DictAnnotator annotator = new DictAnnotator(dictModel, properties);

        String str = "aaab";
        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{Aa}}b", res.get(0).getPQuery());
    }

    @Test
    void testInterception2() {
        List<QueryAct> res;

        DictAnnotator annotator = new DictAnnotator(dictModel, properties);

        String str = "aaabba";
        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{Aa}}{{Bb}}", res.get(0).getPQuery());
    }

    @Test
    void testMediumInterception() {
        String str;
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);
        str = "abbaaxbbba";

        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{Ab}}{{Aa}}xb{{Bb}}", res.get(0).getPQuery());
    }

    /**
     * Test the long case, where the retain rule of {@link ai.hual.labrador.nlu.annotators.DictAnnotator}
     * might edge out the queryAct with the potential of obtaining better slot combination.
     * While in this case, the {@value ai.hual.labrador.utils.QueryActUtils#COMBINATION_THRESHOLD} is just
     * big enough to include the potential "best" queryAct.
     */
    @Test
    void testHugeInterception() {
        String str;
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("AA", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);
        // if more "b" tailing in this string, the COMBINATION_THRESHOLD will not be big enough to
        // keep the queryAct who has the potential to obtain max score
        str = "abbaaxbbbaaaxbbbbbbbbbbb";

        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{Ab}}{{Aa}}x{{Bb}}b{{AA}}x{{Bb}}{{Bb}}{{Bb}}{{Bb}}{{Bb}}b", res.get(0).getPQuery());
    }

    @Test
    void testAnnotateAmbiguousWordsLeftSlotHasHigherPriority() {
        String str;
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("AA", "aaa"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);
        str = "bbbaaa";

        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{Bb}}b{{AA}}", res.get(0).getPQuery());
    }

    @Test
    void testAnnotateAmbiguousWordsSecondPositionHasBetterCombination() {
        String str;
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Bb", "bb"),
                new Dict("Ba", "bba")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);
        str = "bbbaaa";

        res = annotator.annotate(new QueryAct(str));
        assertEquals("b{{Ba}}{{Aa}}", res.get(0).getPQuery());
    }

    @Test
    void testAnnotateAmbiguousWordsFullyMatch() {
        String str;
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("AA", "aaa"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba"),
                new Dict("BB", "bbb")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);
        str = "bbbaaa";

        res = annotator.annotate(new QueryAct(str));
        assertEquals("{{BB}}{{AA}}", res.get(0).getPQuery());
    }

    @Test
    void testAnnotate() {
        String str;
        List<QueryAct> res;
        ListMultimap<String, SlotValue> slots;

        DictAnnotator annotator = new DictAnnotator(dictModel, properties);

        str = "xaay";
        res = annotator.annotate(new QueryAct(str));
        assertEquals(2, res.size());
        slots = res.get(0).getSlots();
        assertEquals("x{{Aa}}y", res.get(0).getPQuery());
        assertEquals(1, slots.get("Aa").size());
        assertEquals("aa", slots.get("Aa").get(0).matched);
        assertEquals(1, slots.get("Aa").get(0).start);
        assertEquals(7, slots.get("Aa").get(0).end);
        assertEquals(1, slots.get("Aa").get(0).realStart);
        assertEquals(3, slots.get("Aa").get(0).realEnd);


        str = "aaab";
        res = annotator.annotate(new QueryAct(str));
        assertEquals(7, res.size());
        ListMultimap<String, SlotValue> slots0 = res.get(0).getSlots();
        ListMultimap<String, SlotValue> slots1 = res.get(2).getSlots();
        ListMultimap<String, SlotValue> slots2 = res.get(1).getSlots();
        ListMultimap<String, SlotValue> slots3 = res.get(3).getSlots();
        // first slots combination
        assertEquals("{{Aa}}b", res.get(0).getPQuery());
        assertEquals(1, slots0.get("Aa").size());
        assertEquals("aaa", slots0.get("Aa").get(0).matched);
        assertEquals(0, slots0.get("Aa").get(0).start);
        assertEquals(6, slots0.get("Aa").get(0).end);
        assertEquals(0, slots0.get("Aa").get(0).realStart);
        assertEquals(3, slots0.get("Aa").get(0).realEnd);
        // second slots combination
        assertEquals("{{Aa}}{{Ab}}", res.get(2).getPQuery());
        assertEquals(1, slots1.get("Aa").size());
        assertEquals(1, slots1.get("Ab").size());
        assertEquals("aa", slots1.get("Aa").get(0).matched);
        assertEquals(0, slots1.get("Aa").get(0).start);
        assertEquals(6, slots1.get("Aa").get(0).end);
        assertEquals(0, slots1.get("Aa").get(0).realStart);
        assertEquals(2, slots1.get("Aa").get(0).realEnd);

        assertEquals("ab", slots1.get("Ab").get(0).matched);
        assertEquals(6, slots1.get("Ab").get(0).start);
        assertEquals(12, slots1.get("Ab").get(0).end);
        assertEquals(2, slots1.get("Ab").get(0).realStart);
        assertEquals(4, slots1.get("Ab").get(0).realEnd);
        // third slots combination
        assertEquals("a{{Ab}}", res.get(1).getPQuery());
        assertEquals(1, slots2.get("Ab").size());
        assertEquals("aab", slots2.get("Ab").get(0).matched);
        assertEquals(1, slots2.get("Ab").get(0).start);
        assertEquals(7, slots2.get("Ab").get(0).end);
        assertEquals(1, slots2.get("Ab").get(0).realStart);
        assertEquals(4, slots2.get("Ab").get(0).realEnd);
        // fourth slots combination
        // Pretty hard to keep the order of pQuery after BFS in reasonable order, which
        // is to ensure left-priority of slot, e.g {{Aa}}ab, a{{Aa}}b, aa{{Ab}}.
        // This scenario happens because queryActs before BFS contains a{{Aa}}b, so, this
        // act is enqueued with higher priority than the other two.
        assertEquals("a{{Aa}}b", res.get(3).getPQuery());
        assertEquals(1, slots3.get("Aa").size());
        assertEquals("aa", slots3.get("Aa").get(0).matched);
        assertEquals(1, slots3.get("Aa").get(0).start);
        assertEquals(7, slots3.get("Aa").get(0).end);
        assertEquals(1, slots3.get("Aa").get(0).realStart);
        assertEquals(3, slots3.get("Aa").get(0).realEnd);
    }

    @Test
    void testCrazilyLongAnnotate() {
        String str;
        List<QueryAct> res;
        ListMultimap<String, SlotValue> slots;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("AA", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba"),
                new Dict("长", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                new Dict("长", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        str = "abbaaxbbbaaaxbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
        res = annotator.annotate(new QueryAct(str));
        slots = res.get(0).getSlots();
        assertEquals("{{Ab}}{{Aa}}x{{Bb}}b{{AA}}x{{长}}b", res.get(0).getPQuery());
        assertEquals(1, slots.get("Ab").size());
        assertEquals("abb", slots.get("Ab").get(0).matched);
        assertEquals(0, slots.get("Ab").get(0).start);
        assertEquals(6, slots.get("Ab").get(0).end);
        assertEquals(0, slots.get("Ab").get(0).realStart);
        assertEquals(3, slots.get("Ab").get(0).realEnd);

        assertEquals(1, slots.get("Aa").size());
        assertEquals("aa", slots.get("Aa").get(0).matched);
        assertEquals(6, slots.get("Aa").get(0).start);
        assertEquals(12, slots.get("Aa").get(0).end);
        assertEquals(3, slots.get("Aa").get(0).realStart);
        assertEquals(5, slots.get("Aa").get(0).realEnd);

        assertEquals(1, slots.get("Bb").size());
        assertEquals("bb", slots.get("Bb").get(0).matched);
        assertEquals(13, slots.get("Bb").get(0).start);
        assertEquals(19, slots.get("Bb").get(0).end);
        assertEquals(6, slots.get("Bb").get(0).realStart);
        assertEquals(8, slots.get("Bb").get(0).realEnd);

        assertEquals(1, slots.get("AA").size());
        assertEquals("aaa", slots.get("AA").get(0).matched);
        assertEquals(20, slots.get("AA").get(0).start);
        assertEquals(26, slots.get("AA").get(0).end);
        assertEquals(9, slots.get("AA").get(0).realStart);
        assertEquals(12, slots.get("AA").get(0).realEnd);

        assertEquals(1, slots.get("长").size());
        assertEquals("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", slots.get("长").get(0).matched);
        assertEquals(27, slots.get("长").get(0).start);
        assertEquals(32, slots.get("长").get(0).end);
        assertEquals(13, slots.get("长").get(0).realStart);
        assertEquals(63, slots.get("长").get(0).realEnd);
    }

    @Test
    void testAnnotateMultipleLabel() {
        String str;
        List<QueryAct> res;

        DictAnnotator annotator = new DictAnnotator(dictModel, properties);

        str = "我想听林俊杰和周董的歌";
        res = annotator.annotate(new QueryAct(str));
        assertEquals(12, res.size());
        for (QueryAct act : res) {
            switch (act.getPQuery()) {
                case "我想听{{歌手}}和{{歌手}}的歌":
                    assertIterableEquals(Arrays.asList("林俊杰", "周杰伦"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听{{歌手}}和{{导演}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
                    break;
                case "我想听{{歌手}}和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听{{Dota主播}}和{{歌手}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听{{Dota主播}}和{{导演}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
                    break;
                case "我想听{{Dota主播}}和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听{{Dota主播}}和周董的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    break;
                case "我想听{{歌手}}和周董的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听林俊杰{{Dota主播}}和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听林俊杰和{{导演}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
                    break;
                case "我想听林俊杰和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听林俊杰和{{歌手}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听林俊杰和周董的歌":
                    assertTrue(act.getSlots().isEmpty());
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
    }

}