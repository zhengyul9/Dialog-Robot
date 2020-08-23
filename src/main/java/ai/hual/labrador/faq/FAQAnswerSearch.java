package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.AverageQuestionScore;
import ai.hual.labrador.faq.utils.DetailedHitsEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FAQAnswerSearch {
    private static final int limit = 100; // 最多返回的问题组数

    /**
     * 暂时是copy的questionSearch占个位置,因为answer的形式没有确定，而该方法主要是做了一个加权，意义不是很明显
     * 但是应该找到一个更好的方法去解决问题
     *
     * @return 若干个问题组的列表，列表最大大小由limit决定
     * @param    entities 给定的query通过ES搜索得到的若干个相似问题
     */
    public static List<Integer> searchAnswer(List<DetailedHitsEntity> entities) {
        Map<Integer, AverageQuestionScore> groups = new HashMap<>();
        for (DetailedHitsEntity entity : entities) {
            int qaid = entity.getQaid();
            AverageQuestionScore group;
            if (groups.containsKey(qaid))
                group = groups.get(qaid);
            else {
                group = new AverageQuestionScore(qaid);
                groups.put(qaid, group);
            }
            group.addQuestionScore(entity.questionScore());
        }
        List<Integer> result = new ArrayList<>(groups.keySet());
        result.sort((o1, o2) -> Double.compare(groups.get(o2).averageScore(), groups.get(o1).averageScore()));

        return result.size() > limit ? result.subList(0, limit) : result;
    }
}
