package ai.hual.labrador.utils;

import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.NLUImpl;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.annotators.dict.VanillaCombinationBFS;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryActUtilsTest {

    private static DictModel dictModel;
    private static NLUImpl nlu;
    private static Properties properties;

    @BeforeAll
    static void setup() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("Aa", "aa"),
                new Dict("Aa", "aaa"),
                new Dict("Ab", "ab"),
                new Dict("Ab", "abb"),
                new Dict("Ab", "aab"),
                new Dict("Bb", "bb"),
                new Dict("Bb", "bba"),
                new Dict("长", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                new Dict("长", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
                new Dict("歌手", "周杰伦", "周董,杰伦"),
                new Dict("演员", "周杰伦", "周董,杰伦"),
                new Dict("导演", "周杰伦", "周董,杰伦"),
                new Dict("歌手", "林俊杰", "JJ"),
                new Dict("Dota主播", "林俊杰", "JJ"),
                new Dict("傅", "傅"),
                new Dict("道", "道"),
                new Dict("人名", "阿什"),
                new Dict("movie", "爆裂鼓手"),
                new Dict("侦探", "柯南")
        ));

        properties = new Properties();
        properties.put("nlu.intentMatchers", "templateIntentMatcher");
    }

    @Test
    void testCombinationBFS() {
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator");

        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        /* construct inputs */
        String query1 = "在2018年10月12号有个会,今年8月也有个会";
        QueryAct queryAct1 = new QueryAct(query1);
        String pQuery1 = "在{{数字}}年{{数字}}月{{数字}}号有个会,今年{{数字}}月也有个会";
        ListMultimap<String, SlotValue> inputSlots1 = ArrayListMultimap.create();
        SlotValue inputSlot00 = new SlotValue(2018, "数字", "getDigits(str)", 1, 7, 1, 5);
        SlotValue inputSlot01 = new SlotValue(10, "数字", "getDigits(str)", 8, 14, 6, 8);
        SlotValue inputSlot2 = new SlotValue(12, "数字", "getDigits(str)", 15, 21, 9, 11);
        SlotValue inputSlot3 = new SlotValue(8, "数字", "getDigits(str)", 28, 34, 18, 19);
        inputSlots1.put("数字", inputSlot00);
        inputSlots1.put("数字", inputSlot01);
        inputSlots1.put("数字", inputSlot2);
        inputSlots1.put("数字", inputSlot3);
        queryAct1.setPQuery(pQuery1);
        queryAct1.setSlots(inputSlots1);
        queryAct1.setScore(slotScore(4) + 1 + 1 + 1);

        String query = "二零二零年五月十六日";
        QueryAct queryAct2 = new QueryAct(query);
        String pQuery = "二零二零年{{数字}}月{{数字}}日";
        ListMultimap<String, SlotValue> inputSlots = ArrayListMultimap.create();
        SlotValue inputSlot0 = new SlotValue(5, "数字", "getDigits(str)", 5, 11, 5, 6);
        SlotValue inputSlot1 = new SlotValue(16, "数字", "getDigits(str)", 12, 18, 7, 9);
        inputSlots.put("数字", inputSlot0);
        inputSlots.put("数字", inputSlot1);
        queryAct2.setPQuery(pQuery);
        queryAct2.setSlots(inputSlots);
        queryAct2.setScore(1 + 1);

        List<QueryAct> actList1 = new ArrayList<>(Arrays.asList(queryAct1));
        List<QueryAct> result1 = new VanillaCombinationBFS().combinationBFS(actList1);
        List<QueryAct> actList2 = new ArrayList<>(Arrays.asList(queryAct2));
        List<QueryAct> result2 = new VanillaCombinationBFS().combinationBFS(actList2);

        assertEquals(Math.pow(2, 4), result1.size());
        assertEquals(Math.pow(2, 2), result2.size());
    }

}