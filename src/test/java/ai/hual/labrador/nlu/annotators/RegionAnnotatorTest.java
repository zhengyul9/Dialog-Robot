package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionAnnotatorTest {

    private static RegionAnnotator RA;

    @BeforeAll
    static void setup() {
        RA = new RegionAnnotator();
    }

    @Test
    void regionTest() {

        // construct inputs
        String query = "给我来张从北京到上海的票吧";
        QueryAct queryAct = new QueryAct(query);

        // output
        List<QueryAct> resultList = RA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals(4, resultList.size());
        assertEquals("给我来张从{{市}}到{{市}}的票吧", result.getPQuery());
        assertEquals(query, resultList.get(3).getPQuery());
    }

    @Test
    void noRegionTest() {

        // construct inputs
        String query = "这个句子中没有地点";
        QueryAct queryAct = new QueryAct(query);

        // output
        List<QueryAct> resultList = RA.annotate(queryAct);
        QueryAct result = resultList.get(0);

        assertEquals(1, resultList.size());
        assertEquals("这个句子中没有地点", result.getPQuery());
    }
}