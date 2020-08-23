package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.DetailedHitsEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FAQQuestionSearchTest {

    private static List<DetailedHitsEntity> dummyHits;

    @Before
    public void setUp() throws Exception {
        dummyHits = new ArrayList<>();
        dummyHits.add(new DetailedHitsEntity(1, 0.8, 0.9)); // 0
        dummyHits.add(new DetailedHitsEntity(1, 0.1, 0.9)); // 1
        dummyHits.add(new DetailedHitsEntity(1, 0.85, 0.9)); // 2
        dummyHits.add(new DetailedHitsEntity(2, 1.8, 2.0)); // 3
        dummyHits.add(new DetailedHitsEntity(3, 4.1, 5.0)); // 4
        dummyHits.add(new DetailedHitsEntity(4, 4.6, 5.0)); // 5
        dummyHits.add(new DetailedHitsEntity(5, 3.8, 5.0)); // 6
        dummyHits.add(new DetailedHitsEntity(6, 4.999, 5.0)); // 7
    }

    @Test
    public void testSearchQuestion() {
        List<Integer> result = FAQQuestionSearch.searchQuestion(dummyHits);
        assertEquals(6, result.size());
        assertEquals(Integer.valueOf(6), result.get(0));
        assertEquals(Integer.valueOf(4), result.get(1));
        assertEquals(Integer.valueOf(2), result.get(2));
        assertEquals(Integer.valueOf(3), result.get(3));
        assertEquals(Integer.valueOf(5), result.get(4));
        assertEquals(Integer.valueOf(1), result.get(5));
    }

}
