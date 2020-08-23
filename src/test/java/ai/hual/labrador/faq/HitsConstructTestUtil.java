package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.HitsEntity;
import ai.hual.labrador.faq.utils.IntegerPair;

import java.util.ArrayList;
import java.util.List;

public class HitsConstructTestUtil {
    public static List<HitsEntity> hitsConstructTest(int[][][] hitConstruct) {
        List<HitsEntity> hitsList = new ArrayList<>();
        for (int i = 0; i < hitConstruct.length; i++) {
            Integer qid = hitConstruct[i][0][0];
            List<IntegerPair> tempIntegerPair = new ArrayList<>();
            for (int j = 1; j < hitConstruct[i][0].length; j++) {
                int id = hitConstruct[i][0][j];
                int qaid = hitConstruct[i][1][j];
                tempIntegerPair.add(new IntegerPair(id, qaid));
            }
            hitsList.add(new HitsEntity(qid, tempIntegerPair));
        }
        return hitsList;
    }
}
