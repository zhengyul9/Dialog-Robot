package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.HitsEntity;
import ai.hual.labrador.faq.utils.QidPairRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FAQConfusionPairSearchTest {


    /**
     * 测试数据说明
     * hitConstruct是模拟ES返回结果hitsList的一个三维数组, 该数组中的每个二维数组是一条查询得到的结果,
     * 其中qid是二维数组中的第一个数组的而第一个数,id是第一个数组后面的值,
     * rqaid是二维数组中的第二个数组,它第一个数-1只是用来占位
     * 另外此测试样例只是针对FAQConfusionPairSearch.filterOutCoincidence(Results,coincide=1) 成立的
     */

    private static int MIN_SIZE;

    @BeforeEach
    void setUp() {
        MIN_SIZE = 1;
    }

    @Test
    void pairSearch1() throws IOException {
        int[][][] hitsConstruct = {{{0, 0, 1, 4, 2, 5, 6}, {-1, 0, 0, 3, 0, 3, 3}}, {{1, 1, 0, 2, 3, 5, 7}, {-1, 0, 0, 0, 3, 3, 3}}, {{2, 2, 0, 1, 4, 6, 9}, {-1, 0, 0, 0, 3, 3, 9}}};
        String expectedResults =
                "[ { size: 8\tmapping: {0=[4, 5, 6], 1=[3, 5, 7], 2=[4, 6]}\tatanValueSum: 0.7149274629979671\tparentId: 0->3 }\n" +
                        "]";
        List<HitsEntity> hitsList = HitsConstructTestUtil.hitsConstructTest(hitsConstruct);

        List<QidPairRelation> results = FAQConfusionPairSearch.pairSearch(hitsList, hitsConstruct.length, MIN_SIZE);

        assertEquals(expectedResults, results.toString());
    }

    @Test
    void pairSearch2() throws IOException {
        int[][][] hitsConstruct =
                {{{3, 3, 4, 5, 6, 7, 1, 8, 0, 2, 9}, {-1, 3, 3, 3, 3, 3, 0, 8, 0, 0, 9}}
                        , {{4, 4, 0, 5, 6, 3, 7, 2, 0, 9, 10}, {-1, 3, 0, 3, 3, 3, 3, 0, 0, 9, 9}}, {{5, 5, 4, 3, 9, 6, 7, 1, 0, 10, 8}, {-1, 3, 3, 3, 9, 3, 3, 0, 0, 9, 8}}
                        , {{6, 6, 7, 4, 3, 0, 1, 10, 8, 5, 9}, {-1, 3, 3, 3, 3, 0, 0, 9, 8, 3, 9}}, {{7, 7, 3, 5, 6, 0, 2, 1, 4, 9, 8}, {-1, 3, 3, 3, 3, 0, 0, 0, 3, 9, 8}}};
        String expectedResults =
                "[ { size: 13\tmapping: {3=[1, 0, 2], 4=[0, 2, 0], 5=[1, 0], 6=[0, 1], 7=[0, 2, 1]}\tatanValueSum: 0.5949442946990611\tparentId: 3->0 }\n" +
                        ",  { size: 4\tmapping: {3=[8], 5=[8], 6=[8], 7=[8]}\tatanValueSum: 0.28867904537162414\tparentId: 3->8 }\n" +
                        ",  { size: 8\tmapping: {3=[9], 4=[9, 10], 5=[9, 10], 6=[10, 9], 7=[9]}\tatanValueSum: 0.46151989077748096\tparentId: 3->9 }\n" +
                        "]";
        List<HitsEntity> hitsList = HitsConstructTestUtil.hitsConstructTest(hitsConstruct);
        List<QidPairRelation> results = FAQConfusionPairSearch.pairSearch(hitsList, hitsConstruct.length, MIN_SIZE);
        assertEquals(expectedResults, results.toString());
    }

    @Test
    void pairSearch3() throws IOException {
        int[][][] hitsConstruct = {{{8, 8, 7}, {-1, 8, 3}}};
        String expectedResults =
                "[ { size: 1\tmapping: {8=[7]}\tatanValueSum: 0.6826061944859854\tparentId: 8->3 }\n" +
                        "]";
        List<HitsEntity> hitsList = HitsConstructTestUtil.hitsConstructTest(hitsConstruct);
        List<QidPairRelation> results = FAQConfusionPairSearch.pairSearch(hitsList, hitsConstruct.length, MIN_SIZE);
        assertEquals(expectedResults, results.toString());
    }

    @Test
    void pairSearch4() throws IOException {
        int[][][] hitsConstruct = {{{9, 9, 10, 1, 2}, {-1, 9, 9, 0, 0}}, {{10, 10, 9, 2, 6}, {-1, 9, 9, 0, 3}}};
        String expectedResults =
                "[ { size: 3\tmapping: {9=[1, 2], 10=[2]}\tatanValueSum: 0.6162433801794818\tparentId: 9->0 }\n" +
                        ",  { size: 1\tmapping: {10=[6]}\tatanValueSum: 0.25\tparentId: 9->3 }\n" +
                        "]";
        List<HitsEntity> hitsList = HitsConstructTestUtil.hitsConstructTest(hitsConstruct);
        List<QidPairRelation> results = FAQConfusionPairSearch.pairSearch(hitsList, hitsConstruct.length, MIN_SIZE);
        assertEquals(expectedResults, results.toString());
    }

}