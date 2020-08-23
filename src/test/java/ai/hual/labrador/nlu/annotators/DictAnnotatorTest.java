package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.QueryActUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.annotators.DictAnnotator.DICT_COLLECTION_POLICY_PROP_NAME;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DictAnnotatorTest {

    private static Properties properties;
    private static DictModel dictModel;

    @BeforeEach
    void setup() {
        properties = new Properties();
        dictModel = new DictModel();
    }

    @Test
    void testOverlap() {
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        dictModel = new DictModel(Arrays.asList(
                new Dict("逾期_sc", "逾期"),
                new Dict("逾期+", "逾期")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "逾期";
        List<QueryAct> result = VDA.annotate(new QueryAct(query));

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getSortedSlotAsList().size());
    }

    @Test
    void testLongerAliasHigherThanShorterWord() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "aa"),
                new Dict("B", "bb", "aacd")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "aacd";
        List<QueryAct> result = VDA.annotate(new QueryAct(query));

        assertEquals(3, result.size());
        assertEquals("{{B}}", result.get(0).getPQuery());
        assertEquals("{{A}}cd", result.get(1).getPQuery());
    }

    @Test
    void testAliasOverlapWithOnlyAliasMatchNoWordMatch() {
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "a", "t"),
                new Dict("B", "b", "t")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "xtytz";
        List<QueryAct> result = VDA.annotate(new QueryAct(query));

        // "A" comes first than "B", corresponds to order of dict in dictModel
        assertEquals(9, result.size());
        assertEquals("x{{A}}y{{A}}z", result.get(0).getPQuery());
        assertEquals(2, result.get(0).getSlots().size());
        assertEquals("x{{B}}y{{B}}z", result.get(1).getPQuery());
        assertEquals(2, result.get(1).getSlots().size());
        assertEquals("x{{B}}y{{A}}z", result.get(2).getPQuery());
        assertEquals(2, result.get(2).getSlots().size());
        assertEquals("x{{A}}y{{B}}z", result.get(3).getPQuery());
        assertEquals(2, result.get(3).getSlots().size());
    }

    @Test
    void testAliasOverlapWithWordMatch() {
        properties.put(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "a", "t"),
                new Dict("B", "b", "t"),
                new Dict("T", "t", "P")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "xtytz";
        List<QueryAct> result = VDA.annotate(new QueryAct(query));

        assertEquals(16, result.size());
        assertEquals("x{{T}}y{{T}}z", result.get(0).getPQuery());
        assertEquals(2, result.get(0).getSlots().size());
    }

    @Test
    void testAliasScoreLowerThanWord() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "aa"),
                new Dict("B", "ac", "ab")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "aab";
        List<QueryAct> result = VDA.annotate(new QueryAct(query));

        assertEquals(3, result.size());
        assertTrue(result.get(0).getScore() * QueryActUtils.DEFAULT_ALIAS_DISCOUNT == result.get(1).getScore());
        assertEquals("{{A}}b", result.get(0).getPQuery());
        assertEquals("a{{B}}", result.get(1).getPQuery());
    }

    @Test
    void testOverlapDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba")
        ));
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator ODA = new DictAnnotator(dictModel, properties);
        String text = "aaab";
        List<QueryAct> result = ODA.annotate(new QueryAct(text));
        assertEquals("{{Aa}}b", result.get(0).getPQuery());
    }

    @Test
    void testOverlapDictWithThreshold() {
        List<Dict> dictList = new ArrayList<>(); // A0: a, A2: a, ..., A299: a, AB: ab, C: c
        for (int i = 0; i < QueryActUtils.COMBINATION_THRESHOLD; i++) {
            dictList.add(new Dict("A" + i, "a"));
            dictList.add(new Dict("AB", "ab"));
            dictList.add(new Dict("C", "c"));
        }
        dictModel = new DictModel(dictList);
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator annotator = new DictAnnotator(dictModel, properties);
        String text = "abc";
        List<QueryAct> result = annotator.annotate(new QueryAct(text));
        assertTrue(result.stream().anyMatch(x -> x.getPQuery().equals("{{AB}}{{C}}")));
    }

    @Test
    void testOverlapDictWithKeyInDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba"),
                new Dict("F", "ff"),
                new Dict("NF", "F"),
                new Dict("E", "e"),
                new Dict("NE", "E")
        ));
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cffaaeab";
        String pQuery = "c{{F}}aa{{E}}ab";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("ff", "F", null, 1, 6, 1, 3);
        SlotValue slot1 = new SlotValue("e", "E", null, 8, 13, 5, 6);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot.getKey(), slot1);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{F}}{{Aa}}{{E}}{{Ab}}", result.get(0).getPQuery());
    }

    @Test
    void testOverlapDictWithOneSlotsBeforeAnnotated() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("E", "e")
        ));

        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cffepxlaaattb";
        String pQuery = "c{{F}}e{{PX}}laaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 17, 22, 10, 12);
        SlotValue slot1 = new SlotValue("px", "PX", null, 7, 13, 4, 6);
        SlotValue slot2 = new SlotValue("ff", "F", null, 1, 6, 1, 3);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{F}}{{E}}{{PX}}l{{Aa}}{{T}}b", result.get(0).getPQuery());

        assertEquals(1, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(3, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(7, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(10, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(3, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(6, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(10, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(12, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(1, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(6, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(11, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(18, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(6, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(11, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(29, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testOverlapDictWithTwoSlotsBeforeAnnotated() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("E", "e"),
                new Dict("YY", "y"),
                new Dict("F", "FHH")
        ));

        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cyffepxlaaattb";
        String pQuery = "c{{YY}}{{F}}e{{PX}}laaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 23, 28, 11, 13);
        SlotValue slot1 = new SlotValue("px", "PX", null, 13, 19, 5, 7);
        SlotValue slot2 = new SlotValue("ff", "F", null, 7, 12, 2, 4);
        SlotValue slot3 = new SlotValue("y", "YY", null, 1, 7, 1, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        queryAct.getSlots().put(slot3.getKey(), slot3);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{YY}}{{F}}{{E}}{{PX}}l{{Aa}}{{T}}b", result.get(0).getPQuery());

        assertEquals(2, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(5, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(8, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(11, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(4, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(5, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(7, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(11, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(13, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(7, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(12, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(30, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(12, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(23, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(30, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(35, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testOverlapDictWithOneSlotBeforeAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px")

        ));
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "pxaaab";
        String pQuery = "{{PX}}aaab";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("px", "PX", null, 0, 6, 0, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{PX}}a{{AB}}", result.get(0).getPQuery());
    }

    @Test
    void testOverlapDictWithOneSlotAfterAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px")

        ));
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "aaabpxe";
        String pQuery = "aaab{{PX}}e";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("px", "PX", null, 4, 10, 4, 6);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{Aa}}b{{PX}}e", result.get(0).getPQuery());
    }

    @Test
    void testOverlapDictWithOneSlotAfterAnnotatedPosAndEndWithSlot() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px")

        ));
        properties.setProperty(DICT_COLLECTION_POLICY_PROP_NAME, "overlap");
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "aaabpx";
        String pQuery = "aaab{{PX}}";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("px", "PX", null, 4, 10, 4, 6);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{Aa}}b{{PX}}", result.get(0).getPQuery());
        assertEquals(7, result.get(0).getSlots().get("PX").get(0).getStart());
        assertEquals(13, result.get(0).getSlots().get("PX").get(0).getEnd());
        assertEquals(4, result.get(0).getSlots().get("PX").get(0).getRealStart());
        assertEquals(6, result.get(0).getSlots().get("PX").get(0).getRealEnd());
    }

    @Test
    void testVanillaDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab")
        ));
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "aaatt";
        String pQuery = "aaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 3, 8, 3, 5);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);
        assertEquals("{{Aa}}{{T}}b", result.get(0).getPQuery());
        assertEquals(0, result.get(0).getSlots().get("Aa").get(0).getStart());
        assertEquals(6, result.get(0).getSlots().get("Aa").get(0).getEnd());
        assertEquals(0, result.get(0).getSlots().get("Aa").get(0).getRealStart());
        assertEquals(3, result.get(0).getSlots().get("Aa").get(0).getRealEnd());

    }

    @Test
    void testVanillaDictWithOneSlotAfterAnnotatedPosAndKeyInDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("NT", "T")
        ));
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "aaattb";
        String pQuery = "aaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 3, 8, 3, 5);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{Aa}}{{T}}b", result.get(0).getPQuery());
        assertEquals("{{Aa}}{{T}}b", result.get(0).getPQuery());
        assertEquals(0, result.get(0).getSlots().get("Aa").get(0).getStart());
        assertEquals(6, result.get(0).getSlots().get("Aa").get(0).getEnd());
        assertEquals(0, result.get(0).getSlots().get("Aa").get(0).getRealStart());
        assertEquals(3, result.get(0).getSlots().get("Aa").get(0).getRealEnd());
    }

    @Test
    void testVanillaDictWithOneSlotBeforeAnnotatedPosAndKeyInDict1() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("NT", "T")
        ));
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);
        String query = "ttaaacb";
        String pQuery = "{{T}}aaacb";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 0, 5, 0, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{T}}{{Aa}}cb", result.get(0).getPQuery());
        assertEquals(5, result.get(0).getSlots().get("Aa").get(0).getStart());
        assertEquals(11, result.get(0).getSlots().get("Aa").get(0).getEnd());
        assertEquals(2, result.get(0).getSlots().get("Aa").get(0).getRealStart());
        assertEquals(5, result.get(0).getSlots().get("Aa").get(0).getRealEnd());
    }

    @Test
    void testVanillaDictWithOneSlotsBeforeAndTwoSlotsAfterAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("E", "e")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cffepxlaaattb";
        String pQuery = "c{{F}}e{{PX}}laaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 17, 22, 10, 12);
        SlotValue slot1 = new SlotValue("px", "PX", null, 7, 13, 4, 6);
        SlotValue slot2 = new SlotValue("ff", "F", null, 1, 6, 1, 3);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{F}}{{E}}{{PX}}l{{Aa}}{{T}}b", result.get(0).getPQuery());

        assertEquals(1, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(3, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(7, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(10, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(3, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(6, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(10, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(12, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(1, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(6, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(11, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(18, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(6, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(11, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(29, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testVanillaDictWithOneSlotsBeforeAndTwoSlotsAfterAnnotatedPosAndEndWithSlot() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("E", "e")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cffepxlaaatt";
        String pQuery = "c{{F}}e{{PX}}laaa{{T}}";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 17, 22, 10, 12);
        SlotValue slot1 = new SlotValue("px", "PX", null, 7, 13, 4, 6);
        SlotValue slot2 = new SlotValue("ff", "F", null, 1, 6, 1, 3);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{F}}{{E}}{{PX}}l{{Aa}}{{T}}", result.get(0).getPQuery());

        assertEquals(1, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(3, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(7, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(10, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(3, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(6, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(10, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(12, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(1, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(6, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(11, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(18, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(6, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(11, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(29, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testVanillaDictWithTwoSlotsBeforeAndTowSlotsAfterAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("E", "e"),
                new Dict("YY", "y")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cyffepxlaaattb";
        String pQuery = "c{{YY}}{{F}}e{{PX}}laaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 23, 28, 11, 13);
        SlotValue slot1 = new SlotValue("px", "PX", null, 13, 19, 5, 7);
        SlotValue slot2 = new SlotValue("ff", "F", null, 7, 12, 2, 4);
        SlotValue slot3 = new SlotValue("y", "YY", null, 1, 7, 1, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        queryAct.getSlots().put(slot3.getKey(), slot3);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{YY}}{{F}}{{E}}{{PX}}l{{Aa}}{{T}}b", result.get(0).getPQuery());

        assertEquals(2, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(5, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(8, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(11, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(4, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(5, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(7, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(11, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(13, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(7, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(12, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(30, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(12, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(23, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(30, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(35, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testVanillaDictWithKeyInDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px"),
                new Dict("F", "ff"),
                new Dict("NF", "F"),
                new Dict("E", "e"),
                new Dict("NYY", "YY"),
                new Dict("NT", "T")
        ));

        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "cyffepxlaaattb";
        String pQuery = "c{{YY}}{{F}}e{{PX}}laaa{{T}}b";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("tt", "T", null, 23, 28, 11, 13);
        SlotValue slot1 = new SlotValue("px", "PX", null, 13, 19, 5, 7);
        SlotValue slot2 = new SlotValue("ff", "F", null, 7, 12, 2, 4);
        SlotValue slot3 = new SlotValue("y", "YY", null, 1, 7, 1, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        queryAct.getSlots().put(slot1.getKey(), slot1);
        queryAct.getSlots().put(slot2.getKey(), slot2);
        queryAct.getSlots().put(slot3.getKey(), slot3);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("c{{YY}}{{F}}{{E}}{{PX}}l{{Aa}}{{T}}b", result.get(0).getPQuery());

        assertEquals(2, result.get(0).getSlots().get("F").get(0).realStart);
        assertEquals(4, result.get(0).getSlots().get("E").get(0).realStart);
        assertEquals(5, result.get(0).getSlots().get("PX").get(0).realStart);
        assertEquals(8, result.get(0).getSlots().get("Aa").get(0).realStart);
        assertEquals(11, result.get(0).getSlots().get("T").get(0).realStart);

        assertEquals(4, result.get(0).getSlots().get("F").get(0).realEnd);
        assertEquals(5, result.get(0).getSlots().get("E").get(0).realEnd);
        assertEquals(7, result.get(0).getSlots().get("PX").get(0).realEnd);
        assertEquals(11, result.get(0).getSlots().get("Aa").get(0).realEnd);
        assertEquals(13, result.get(0).getSlots().get("T").get(0).realEnd);

        assertEquals(7, result.get(0).getSlots().get("F").get(0).start);
        assertEquals(12, result.get(0).getSlots().get("E").get(0).start);
        assertEquals(17, result.get(0).getSlots().get("PX").get(0).start);
        assertEquals(24, result.get(0).getSlots().get("Aa").get(0).start);
        assertEquals(30, result.get(0).getSlots().get("T").get(0).start);

        assertEquals(12, result.get(0).getSlots().get("F").get(0).end);
        assertEquals(17, result.get(0).getSlots().get("E").get(0).end);
        assertEquals(23, result.get(0).getSlots().get("PX").get(0).end);
        assertEquals(30, result.get(0).getSlots().get("Aa").get(0).end);
        assertEquals(35, result.get(0).getSlots().get("T").get(0).end);
    }

    @Test
    void testVanillaDictWithOneSlotBeforeAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px")

        ));
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "pxaaab";
        String pQuery = "{{PX}}aaab";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("px", "PX", null, 0, 6, 0, 2);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{PX}}a{{AB}}", result.get(0).getPQuery());
    }

    @Test
    void testVanillaDictWithOneSlotAfterAnnotatedPos() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("AB", "aab"),
                new Dict("PX", "px")

        ));
        DictAnnotator VDA = new DictAnnotator(dictModel, properties);

        String query = "aaabpxe";
        String pQuery = "aaab{{PX}}e";
        QueryAct queryAct = new QueryAct(query);
        queryAct.setPQuery(pQuery);
        SlotValue slot = new SlotValue("px", "PX", null, 4, 10, 4, 6);
        queryAct.getSlots().put(slot.getKey(), slot);
        List<QueryAct> result = VDA.annotate(queryAct);

        assertEquals("{{Aa}}b{{PX}}e", result.get(0).getPQuery());
    }

    @Test
    void testPinyinDict() {
        properties.put("nlu.dictAnnotator.usePinyinRobust", true);
        dictModel = new DictModel(Arrays.asList(
                new Dict("animal", "大鹿"),
                new Dict("region", "大陆"),
                new Dict("song", "大路"),
                new Dict("crap", "大录")
        ));
        DictAnnotator PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "放大陆的电影";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "放{{region}}的电影";

        assertEquals(expPQuery, result.getPQuery());
    }

    @Test
    void testAnnotatorPreferLongerDict() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("人寿保险_产品", "泰康附加e顺人生年金保险（万能型）", "附加e顺人生年金账户,附加e顺人生万能,附加e顺人生万京,e顺人生,附加e顺人生年金,e顺人生年金保险（万能型）,e顺人生年金万能账户,附加e顺人生年金保险（万能型）,附e顺人生,附加e顺,附加e顺人生年金保险,e顺人生万能,附加E顺意外,附加e顺人生年金万能险,附加e顺人生年金（万能型）,附加e顺人生（万能型）,e顺人生年金,稳健理财C款,e顺人生年金万能型,附加e顺人生")
        ));
        properties.setProperty("nlu.dictAnnotator.useGenerateAliasWord", "true");
        DictAnnotator PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "附加e顺人生万可以保多久";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "{{人寿保险_产品}}可以保多久";

        assertEquals(expPQuery, result.getPQuery());
    }


    @Test
    void testGenerateWords() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("人寿保险_产品", "泰康附加e顺人生年金保险（万能型）", "附加e顺人生年金账户,附加e顺人生万能,e顺人生,附加e顺人生年金,e顺人生年金保险（万能型）,e顺人生年金万能账户,附加e顺人生年金保险（万能型）,附e顺人生,附加e顺,附加e顺人生年金保险,e顺人生万能,附加E顺意外,附加e顺人生年金万能险,附加e顺人生年金（万能型）,附加e顺人生（万能型）,e顺人生年金,稳健理财C款,e顺人生年金万能型,附加e顺人生"),
                new Dict("人寿保险_产品", "泰康赢家理财投资连结保险【银行】【2008.0】", "赢家理财投资连,泰康赢家理财保险,泰康赢家理财投资,赢家理财投资连结保险【银行】【2008.0】,泰康赢家理财投连,泰康赢家理财投连险,赢家理财投资连结保险,赢家理财投资连结,泰康赢家理财投资连结,赢家理财投连险,赢家理财投连保险,泰康赢家理财投资连,泰康赢家理财投资连结保险,泰康赢家理财投资连结保险【银行】【2008.0】,赢家理财投资,泰康赢家理财,泰康赢家理财投连保险,赢家理财,赢家理财保险,赢家理财投连"),
                new Dict("人寿保险_产品", "泰康健保通医院住院津贴医疗保险", "健保通医院住院津贴医疗保险,泰康健保通医院住院津贴医疗保险,泰康健保通津贴,健保通医院住院津贴医疗,泰康健保通医院住院津贴,泰康健保通医院,泰康健保通住院津贴,健保通住院津贴,泰康健保通医院住院津贴医疗,健保通医院住院津贴,泰康健保通医院住院,健保通住院津贴医疗,泰康健保通住院津贴医疗,健保通津贴,健保通医院住院,健保通医院住院津贴医疗险,健保通医院,泰康健保通医院住院津贴医疗险,健保通,泰康健保通,泰康赢家理财投资连结保险")
        ));
        properties.setProperty("nlu.dictAnnotator.useGenerateAliasWord", "true");
        DictAnnotator PDA = new DictAnnotator(dictModel, properties);
        String query = "泰康赢家理财投资连结险死了怎么赔付";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(query));
        QueryAct result = resultList.get(0);

        assertEquals("{{人寿保险_产品}}死了怎么赔付", result.getPQuery());

    }

}