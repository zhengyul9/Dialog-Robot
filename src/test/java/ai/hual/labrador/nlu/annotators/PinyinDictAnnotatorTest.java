package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinyinDictAnnotatorTest {

    private static DictModel dictModel;
    private static DictAnnotator PDA;
    private static Properties properties = new Properties();

    @BeforeAll
    static void setup() {
        properties.setProperty("nlu.dictAnnotator.usePinyinRobust", "true");
    }

    @Test
    void testMultiWildCardWithInterception() {
        // wildAb2 and wildAb1 has higher priority than wildAb3 at the third char,
        // so it won't be extracted
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb3", "安??静?"),
                new Dict("wildAb2", "安?静?"),
                new Dict("wildAb1", "安?静")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "胺c境境t";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals(3, res.size());
        assertEquals("{{wildAb2}}t", res.get(0).getPQuery());
        assertEquals("{{wildAb1}}境t", res.get(1).getPQuery());
        assertEquals(str1, res.get(2).getPQuery());
    }

    @Test
    void testWildCardWithInterception() {
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb2", "安?静?"),
                new Dict("wildAb1", "安?静")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "pp胺c境t";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals("pp{{wildAb2}}", res.get(0).getPQuery());
        assertEquals("pp{{wildAb1}}t", res.get(1).getPQuery());
    }

    @Test
    void testWildCardWithMoreSpecific() {
        List<QueryAct> res;

        DictModel model = new DictModel(Arrays.asList(
                new Dict("wildAb", "安?静"),
                new Dict("Ac", "安c静")
        ));
        DictAnnotator annotator = new DictAnnotator(model, properties);

        String str1 = "pp胺c境";
        res = annotator.annotate(new QueryAct(str1));
        assertEquals("pp{{Ac}}", res.get(0).getPQuery());
    }

    @Test
    void conflictPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "遗迹"),
                new Dict("tv", "权力的游戏")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "播放圈里的游戏第一季";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        assertEquals(4, resultList.size());
        assertEquals("播放{{tv}}第{{movie}}", resultList.get(0).getPQuery());
    }

    @Test
    void interceptedPinyinDictAnnotateTest2() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("category", "电视剧"),
                new Dict("APP", "想看电视"),
                new Dict("song", "想"),
                new Dict("wish", "我想"),
                new Dict("action", "我想看电视剧")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "我想看电视剧";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        assertEquals(8, resultList.size());
        assertEquals("{{action}}", resultList.get(0).getPQuery());
    }

    @Test
    void interceptedPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("song", "打开爱"),
                new Dict("APP", "爱奇艺")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "打开爱其艺";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        // hopefully, there is only a regex: "打开.*{{APP}}", and no regex: "{{song}}.*其艺"
        assertEquals("{{song}}其艺", resultList.get(0).getPQuery());
        assertEquals("打开{{APP}}", resultList.get(1).getPQuery());
    }

    @Test
    void symbolPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "1%")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "我想看《1%》";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));

        assertEquals("我想看《{{movie}}》", resultList.get(0).getPQuery());
    }

    @Test
    void crazyWordPinyinDictAnnotateTest() {

        // input
        String input = "*^_* *% ** ";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));

        assertEquals("*^_* *% ** ", resultList.get(0).getPQuery());
    }

    @Test
    void coveredDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("station", "一台"),
                new Dict("station", "中央一台")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "放中央一台";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "放{{station}}";

        assertEquals(expPQuery, result.getPQuery());
    }

    @Test
    void samePinyinWordsPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("animal", "大鹿"),
                new Dict("region", "大陆"),
                new Dict("song", "大路"),
                new Dict("crap", "大录")
        ));
        PDA = new DictAnnotator(dictModel, properties);
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
    void multipleLabelPinyinDictAnnotateTest() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("歌手", "周杰伦", "周董,杰伦"),
                new Dict("演员", "周杰伦", "周董,杰伦"),
                new Dict("导演", "周杰伦", "周董,杰伦"),
                new Dict("歌手", "林俊杰", "JJ"),
                new Dict("Dota主播", "林俊杰", "JJ")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        String str;
        List<QueryAct> res;

        str = "我想听临俊杰和周懂的歌";
        res = PDA.annotate(new QueryAct(str));
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
                case "我想听{{Dota主播}}和周懂的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    break;
                case "我想听{{歌手}}和周懂的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听临俊杰{{Dota主播}}和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("林俊杰"), act.getSlotAsStringList("Dota主播"));
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听临俊杰和{{导演}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("导演"));
                    break;
                case "我想听临俊杰和{{演员}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("演员"));
                    break;
                case "我想听临俊杰和{{歌手}}的歌":
                    assertIterableEquals(Collections.singletonList("周杰伦"), act.getSlotAsStringList("歌手"));
                    break;
                case "我想听临俊杰和周懂的歌":
                    assertTrue(act.getSlots().isEmpty());
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
    }

    @Test
    void WordWithEnglishCharPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("station", "ATV")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "看ATV";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "看{{station}}";

        assertEquals(expPQuery, result.getPQuery());
    }

    @Test
    void WordWithNumberPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("movie", "变形金刚4绝迹重生")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "看变形金刚4绝迹重生";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "看{{movie}}";

        assertEquals(expPQuery, result.getPQuery());

    }

    @Test
    void SingleWordPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("word1", "晋")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "快进1分钟";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "快进1分钟";

        assertEquals(expPQuery, result.getPQuery());

    }

    @Test
    void toneRobustPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("菜品", "芋香")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "鱼香";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "{{菜品}}";

        assertEquals(expPQuery, result.getPQuery());
        assertAll("verify matched",
                () -> assertEquals("芋香", result.getSlots().get("菜品").get(0).matched.toString())
        );
    }

    @Test
    void oneDishPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("菜品", "鱼香肉丝"),
                new Dict("食材", "鱼肉"),
                new Dict("保险", "四位一体康乃馨"),
                new Dict("菜品", "芋香肉丝"),
                new Dict("食材", "鱼香"),
                new Dict("保险", "鑫瑞人生"),
                new Dict("保险", "鑫享人生"),
                new Dict("保险", "鑫瑞"),
                new Dict("保险", "鑫享"),
                new Dict("菜品", "驴香肉丝"),
                new Dict("职位", "侍卫")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "鱼香漏诗";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "{{菜品}}";

        assertEquals(expPQuery, result.getPQuery());
        assertAll("verify matched",
                () -> assertEquals("鱼香肉丝", result.getSlots().get("菜品").get(0).matched.toString())
        );
    }

    @Test
    void singleWordPinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("菜品", "驴香肉丝"),
                new Dict("食材", "鱼肉")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "驴漏";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "{{食材}}";
        assertEquals(expPQuery, result.getPQuery());
    }

    @Test
    void PinyinDictAnnotateTest() {

        dictModel = new DictModel(Arrays.asList(
                new Dict("菜品", "鱼香肉丝"),
                new Dict("食材", "鱼肉"),
                new Dict("保险", "四位一体康乃馨"),
                new Dict("菜品", "芋香肉丝"),
                new Dict("食材", "鱼香"),
                new Dict("保险", "鑫瑞人生"),
                new Dict("保险", "鑫享人生"),
                new Dict("保险", "鑫瑞"),
                new Dict("保险", "鑫享"),
                new Dict("菜品", "驴香肉丝"),
                new Dict("职位", "侍卫")
        ));
        PDA = new DictAnnotator(dictModel, properties);
        // input
        String input = "来一份鱼香漏诗,驴漏,还有事位一体康奶馨保险,馨睿人生保险怎么样,那新想呢";

        // output
        List<QueryAct> resultList = PDA.annotate(new QueryAct(input));
        QueryAct result = resultList.get(0);

        // expected output
        String expPQuery = "来一份{{菜品}},{{食材}},还有{{保险}}保险,{{保险}}保险怎么样,那{{保险}}呢";

        SlotValue expSlot1 = new SlotValue(new Dict("菜品", "鱼香肉丝"),
                "菜品", null, 3, 9, 3, 7);
        SlotValue expSlot2 = new SlotValue(new Dict("食材", "鱼肉"),
                "食材", null, 10, 16, 8, 10);
        SlotValue expSlot3 = new SlotValue(new Dict("保险", "四位一体康乃馨"),
                "保险", null, 19, 25, 13, 20);
        SlotValue expSlot4 = new SlotValue(new Dict("保险", "鑫瑞人生"),
                "保险", null, 28, 34, 23, 27);
        SlotValue expSlot5 = new SlotValue(new Dict("保险", "鑫享"),
                "保险", null, 41, 47, 34, 36);


        assertEquals(expPQuery, result.getPQuery());
        assertAll("verify matched",
                () -> assertEquals("鱼香肉丝", result.getSlots().get("菜品").get(0).matched.toString()),
                () -> assertEquals("鱼肉", result.getSlots().get("食材").get(0).matched.toString()),
                () -> assertEquals("四位一体康乃馨", result.getSlots().get("保险").get(0).matched.toString()),
                () -> assertEquals("鑫瑞人生", result.getSlots().get("保险").get(1).matched.toString()),
                () -> assertEquals("鑫享", result.getSlots().get("保险").get(2).matched.toString())
        );
        assertAll("verify start",
                () -> assertEquals(expSlot1.start, result.getSlots().get("菜品").get(0).start),
                () -> assertEquals(expSlot2.start, result.getSlots().get("食材").get(0).start),
                () -> assertEquals(expSlot3.start, result.getSlots().get("保险").get(0).start),
                () -> assertEquals(expSlot4.start, result.getSlots().get("保险").get(1).start),
                () -> assertEquals(expSlot5.start, result.getSlots().get("保险").get(2).start)
        );
        assertAll("verify end",
                () -> assertEquals(expSlot1.end, result.getSlots().get("菜品").get(0).end),
                () -> assertEquals(expSlot2.end, result.getSlots().get("食材").get(0).end),
                () -> assertEquals(expSlot3.end, result.getSlots().get("保险").get(0).end),
                () -> assertEquals(expSlot4.end, result.getSlots().get("保险").get(1).end),
                () -> assertEquals(expSlot5.end, result.getSlots().get("保险").get(2).end)
        );
        assertAll("verify realStart",
                () -> assertEquals(expSlot1.realStart, result.getSlots().get("菜品").get(0).realStart),
                () -> assertEquals(expSlot2.realStart, result.getSlots().get("食材").get(0).realStart),
                () -> assertEquals(expSlot3.realStart, result.getSlots().get("保险").get(0).realStart),
                () -> assertEquals(expSlot4.realStart, result.getSlots().get("保险").get(1).realStart),
                () -> assertEquals(expSlot5.realStart, result.getSlots().get("保险").get(2).realStart)
        );
        assertAll("verify realEnd",
                () -> assertEquals(expSlot1.realEnd, result.getSlots().get("菜品").get(0).realEnd),
                () -> assertEquals(expSlot2.realEnd, result.getSlots().get("食材").get(0).realEnd),
                () -> assertEquals(expSlot3.realEnd, result.getSlots().get("保险").get(0).realEnd),
                () -> assertEquals(expSlot4.realEnd, result.getSlots().get("保险").get(1).realEnd),
                () -> assertEquals(expSlot5.realEnd, result.getSlots().get("保险").get(2).realEnd)
        );
    }
}