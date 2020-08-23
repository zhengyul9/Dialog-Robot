package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.DetailedHitsEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FAQSimilarQAGroupFinderTest {

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
    public void eachGroupNeedOnlyOneHighScoreHit() {
        List<DetailedHitsEntity> entities = new ArrayList<>();
        entities.add(dummyHits.get(0));
        entities.add(dummyHits.get(2));
        entities.add(dummyHits.get(4));
        List<Integer> re = FAQSimilarQAGroupFinder.generateSimilarGroups(entities);
        assertEquals(2, re.size());
        assertEquals(Integer.valueOf(1), re.get(0));
        assertEquals(Integer.valueOf(3), re.get(1));
    }

    @Test
    public void generateSimilarGroups1() {
        List<DetailedHitsEntity> entities = new ArrayList<>();
        entities.add(dummyHits.get(0));
        entities.add(dummyHits.get(1));
        entities.add(dummyHits.get(2));
        entities.add(dummyHits.get(3));
        entities.add(dummyHits.get(4));
        entities.add(dummyHits.get(5));
        entities.add(dummyHits.get(6));
        List<Integer> re = FAQSimilarQAGroupFinder.generateSimilarGroups(entities);
        assertEquals(2, re.size());
        assertEquals(Integer.valueOf(1), re.get(0));
        assertEquals(Integer.valueOf(4), re.get(1));
    }

    @Test
    public void generateSimilarGroups2() {
        List<DetailedHitsEntity> entities = new ArrayList<>();
        entities.add(dummyHits.get(0));
        entities.add(dummyHits.get(1));
        entities.add(dummyHits.get(2));
        entities.add(dummyHits.get(6));
        List<Integer> re = FAQSimilarQAGroupFinder.generateSimilarGroups(entities);
        assertEquals(1, re.size());
        assertEquals(Integer.valueOf(1), re.get(0));
    }

    @Test
    public void generateSimilarGroups3() {
        List<DetailedHitsEntity> entities = new ArrayList<>();
        entities.add(dummyHits.get(1));
        entities.add(dummyHits.get(6));
        List<Integer> re = FAQSimilarQAGroupFinder.generateSimilarGroups(entities);
        assertEquals(0, re.size());
    }

    @Test
    public void generateSimilarGroups4() {
        List<DetailedHitsEntity> entities = new ArrayList<>();
        entities.add(dummyHits.get(0));
        entities.add(dummyHits.get(1));
        entities.add(dummyHits.get(2));
        entities.add(dummyHits.get(3));
        entities.add(dummyHits.get(4));
        entities.add(dummyHits.get(5));
        entities.add(dummyHits.get(6));
        entities.add(dummyHits.get(7));
        List<Integer> re = FAQSimilarQAGroupFinder.generateSimilarGroups(entities);
        assertEquals(2, re.size());
        assertEquals(Integer.valueOf(6), re.get(0));
        assertEquals(Integer.valueOf(1), re.get(1));
    }

}
