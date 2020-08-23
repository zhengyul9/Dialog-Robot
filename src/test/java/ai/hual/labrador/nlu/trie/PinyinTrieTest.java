package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Dict;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinyinTrieTest {

    private static PinyinTrie trie;

    @BeforeAll
    static void setup() {
        trie = new PinyinTrie();

        Dict dict1 = new Dict("菜品", "鱼香肉丝");
        Dict dict2 = new Dict("食材", "鱼肉");
        Dict dict3 = new Dict("保险", "四位一体康乃馨保险产品");
        Dict dict4 = new Dict("菜品", "芋香肉丝");
        Dict dict5 = new Dict("食材", "鱼香");
        Dict dict6 = new Dict("保险", "鑫瑞人生");
        Dict dict7 = new Dict("保险", "鑫享人生");
        Dict dict8 = new Dict("保险", "鑫瑞");
        Dict dict9 = new Dict("保险", "鑫享");
        Dict dict10 = new Dict("菜品", "驴香肉丝");
        Dict dict11 = new Dict("职位", "侍卫");
        Dict dict12 = new Dict("bullshit", "视位一体康莱馨");

        trie.insert(dict1.getWord(), dict1);
        trie.insert(dict2.getWord(), dict2);
        trie.insert(dict3.getWord().substring(0, 7), dict3);
        trie.insert(dict4.getWord(), dict4);
        trie.insert(dict5.getWord(), dict5);
        trie.insert(dict6.getWord(), dict6);
        trie.insert(dict7.getWord(), dict7);
        trie.insert(dict8.getWord(), dict8);
        trie.insert(dict9.getWord(), dict9);
        trie.insert(dict10.getWord(), dict10);
        trie.insert(dict11.getWord(), dict11);
        trie.insert(dict12.getWord(), dict12);
    }

    @Test
    void testParse() {
        String word1 = "变形金刚4绝迹重生";
        Dict dict1 = new Dict("movie", word1);
        String word2 = "大陆";
        Dict dict2 = new Dict("region", word2);
        String word3 = "大路";
        Dict dict3 = new Dict("song", word3);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);

        List<Emit> result = trie.parse("看个大鹿版的变型金刚4绝技重生");

        assertEquals(2, result.size());
    }

    @Test
    void longSentenceParseTest() {

        // output
        String text = "来一份鱼香漏诗,驴漏,还有事位一体康奶馨保险,馨睿人生保险怎么样,那新想呢";
        List<Emit> emits = trie.parse(text);

        assertTrue(!trie.getRoot().isLeaf);
        assertTrue(!trie.getRoot().getChildren().get("yu2").isLeaf);
        // expected output
        Emit expEmit0 = new Emit(new Dict("菜品", "鱼香肉丝"), 3, 7);
        Emit expEmit1 = new Emit(new Dict("食材", "鱼肉"), 8, 10);
        Emit expEmit2 = new Emit(new Dict("保险", "四位一体康乃馨保险产品"), 13, 20);
        Emit expEmit3 = new Emit(new Dict("保险", "鑫瑞人生"), 23, 27);
        Emit expEmit4 = new Emit(new Dict("保险", "鑫享"), 34, 36);

        assertAll("emit to String",
                () -> assertEquals(expEmit0.toString().replaceAll("score.*", ""),
                        emits.get(0).toString().replaceAll("(score.*|[\\[\\]])", "")),
                () -> assertEquals(expEmit1.toString().replaceAll("score.*", ""),
                        emits.get(1).toString().replaceAll("(score.*|[\\[\\]])", "")),
                () -> assertEquals(expEmit2.toString().replaceAll("score.*", ""),
                        emits.get(2).toString().replaceAll("(score.*|[\\[\\]])", "")),
                () -> assertEquals(expEmit3.toString().replaceAll("score.*", ""),
                        emits.get(3).toString().replaceAll("(score.*|[\\[\\]])", "")),
                () -> assertEquals(expEmit4.toString().replaceAll("score.*", ""),
                        emits.get(4).toString().replaceAll("(score.*|[\\[\\]])", ""))
        );
    }

    @Test
    void testParseSingleSameChar() {
        String word1 = "跳";
        Dict dict1 = new Dict("movie", word1);
        trie.insert(word1, dict1);

        List<Emit> result = trie.parse("看跳跳虎");

        assertEquals(2, result.size());
    }

    @Test
    void testTooLongPinyinRobustSearch() {
        String word1 = "变形金刚4绝迹重生";
        Dict dict1 = new Dict("movie", word1);
        String word2 = "大陆";
        Dict dict2 = new Dict("region", word2);
        String word3 = "大路";
        Dict dict3 = new Dict("song", word3);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);

        Dict result1 = (Dict) trie.search("看大鹿啊");
        Dict result2 = (Dict) trie.search("看大鹿");
        Dict result3 = (Dict) trie.search("大鹿啊");
        Dict result4 = (Dict) trie.search("变形金刚42绝迹重生");

        assertEquals(null, result1);
        assertEquals(null, result2);
        assertEquals(null, result3);
        assertEquals(null, result4);
    }

    @Test
    void testTooShortPinyinRobustSearch() {
        String word1 = "变形金刚4绝迹重生";
        Dict dict1 = new Dict("movie", word1);
        String word2 = "大陆";
        Dict dict2 = new Dict("region", word2);
        String word3 = "大路";
        Dict dict3 = new Dict("song", word3);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);

        Dict result1 = (Dict) trie.search("鹿");
        Dict result2 = (Dict) trie.search("大");
        Dict result3 = (Dict) trie.search("变形金刚绝迹重生");
        Dict result4 = (Dict) trie.search("变形金刚4绝迹重");

        assertEquals(null, result1);
        assertEquals(null, result2);
        assertEquals(null, result3);
        assertEquals(null, result4);
    }

    @Test
    void testSuccessPinyinRobustSearch() {
        String word1 = "变形金刚4绝迹重生";
        Dict dict1 = new Dict("movie", word1);
        String word2 = "大陆";
        Dict dict2 = new Dict("region", word2);
        String word3 = "大路";
        Dict dict3 = new Dict("song", word3);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);

        Dict result1 = ((Collection<Dict>) trie.search("大鹿")).iterator().next();
        Dict result2 = ((Collection<Dict>) trie.search("大陆")).iterator().next();
        Dict result3 = ((Collection<Dict>) trie.search("变型金刚4决继重生")).iterator().next();
        Dict result4 = ((Collection<Dict>) trie.search("变形金刚4绝迹重生")).iterator().next();

        assertEquals((new Dict("song", "大路")).toString(), result1.toString());
        assertEquals((new Dict("region", "大陆")).toString(), result2.toString());
        assertEquals((new Dict("movie", "变形金刚4绝迹重生")).toString(), result3.toString());
        assertEquals((new Dict("movie", "变形金刚4绝迹重生")).toString(), result4.toString());
    }

    @Test
    void testInsert() {
        String word1 = "变形金刚4绝迹重生";
        Dict dict1 = new Dict("movie", word1);
        String word2 = "大陆";
        Dict dict2 = new Dict("region", word2);
        String word3 = "大路";
        Dict dict3 = new Dict("song", word3);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);

//        traverseTrie(trie.root);
    }

    @Test
    void testInsert2() {
        String word1 = "鱼香肉丝";
        String word2 = "芋香肉丝";
        String word3 = "四位一体康乃馨";
        String word4 = "鱼香馒头";
        Dict dict1 = new Dict("dish", word1);
        Dict dict2 = new Dict("dish", word2);
        Dict dict3 = new Dict("dish", word3);
        Dict dict4 = new Dict("dish", word4);
        trie.insert(word1, dict1);
        trie.insert(word2, dict2);
        trie.insert(word3, dict3);
        trie.insert(word4, dict4);

//        traverseTrie(trie.root);
    }

//    private void traverseTrie(TrieNode<ListMultimap<String, Dict>> root) {
//        root.getChildren().values().forEach(this::traverseTrie);
//    }

}