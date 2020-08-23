package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.DetailedHitsEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FAQSimilarQAGroupFinder {

    private static final double threshold = 0.8;    //得分阈值．当某个问题的得分超过该阈值时，才将其所在QAGroup添加到结果列表中返回．
    private static final int maxSize = 2;        //结果列表的最大长度．

    /**
     * 给定一个query，返回与该query相似的若干个问题组．
     *
     * @param entities 该query通过ES搜索得到的若干个相似问题
     * @return 相似问题组列表，最大大小由常量maxSize指定．
     */
    public static List<Integer> generateSimilarGroups(List<DetailedHitsEntity> entities) {
        entities.sort((o1, o2) -> Double.compare(o2.questionScore(), o1.questionScore()));

        List<Integer> result = new ArrayList<>();
        HashSet<Integer> addedQaid = new HashSet<>();   // avoid adding repeated qaid

        for (DetailedHitsEntity entity : entities) {
            if (result.size() >= maxSize || entity.questionScore() < threshold)
                break;
            else {
                if (!addedQaid.contains(entity.getQaid())) {
                    result.add(entity.getQaid());
                    addedQaid.add(entity.getQaid());
                }
            }
        }
        return result;
    }

}
