package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.AverageQuestionScore;
import ai.hual.labrador.faq.utils.QuestionIntention;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class IntentionPredictor {

    /**
     * 给定一个问题，从问题内容和问题所属问题组两个方面对其可能的意图进行预测．
     *
     * @param target          　待预测意图的问题
     * @param ES_result       　待预测问题通过ES搜索所得的相似问题
     * @param group_questions 　待预测问题所属问题组的所有问题
     */

    public static QuestionIntention predictIntention(QuestionIntention target,
                                                     List<QuestionIntention> ES_result,
                                                     List<QuestionIntention> group_questions) {
        predictByContent(target, ES_result);
        predictByGroup(target, group_questions);
        if (target.getIntention_content_based() != null &&
                target.getIntention_group_based() != null &&
                !(target.getIntention_content_based().equals(target.getIntention_group_based()))) { // 如果预测得出不同意图，则将意图得分降低
            double diff = Math.abs(target.getScore_content_based() - target.getScore_group_based());
            target.setScore_content_based(target.getScore_content_based() * diff);
            target.setScore_group_based(target.getScore_group_based() * diff);
        }
        return target;
    }

    private static final double threshold = 0.8; // 相似问题中，分数超过该阈值的问题才能够对预测产生影响

    /**
     * 从target的ES搜索的结果预测意图。
     *
     * @param target    需要预测意图的问题
     * @param ES_result ES搜索结果
     */
    private static void predictByContent(QuestionIntention target, List<QuestionIntention> ES_result) {
        // 筛选出question_score大于threshold且已被标记的问题
        List<QuestionIntention> qualified_questions = ES_result.stream()
                .filter(QuestionIntention::isMarked)
                .filter(e -> e.getEntity().questionScore() > threshold)
                .collect(Collectors.toList());
        if (qualified_questions.size() == 0) {
            target.setIntention_content_based(null);    // 信息不足，预测失败
            return;
        }
        // 在筛选出的问题当中，提取出若干个意图，比较每个意图的所有问题的score/selfBm25的平均值，取分数最大者为预测结果
        Map<String, AverageQuestionScore> intentions = new HashMap<>();
        for (QuestionIntention question : qualified_questions) {
            String intent_id = question.getIntention_marked();
            AverageQuestionScore intention;
            if (intentions.containsKey(intent_id)) {
                intention = intentions.get(intent_id);
            } else {
                intention = new AverageQuestionScore(intent_id);
                intentions.put(intent_id, intention);
            }
            intention.addQuestionScore(question.getEntity().questionScore());
        }
        List<AverageQuestionScore> sorted = new ArrayList<>(intentions.values());
        sorted.sort((o1, o2) -> Double.compare(o2.averageScore(), o1.averageScore()));
        target.setIntention_content_based(sorted.get(0).getIntent_id());
        target.setScore_content_based(sorted.get(0).averageScore());
    }

    private static final double factor = 0.1; // unmarked的问题数乘这个factor，一起计入group_based计分公式的分母中

    /**
     * 从target所属的问题组的其它问题预测意图。
     *
     * @param target          需要预测意图的问题
     * @param group_questions 与target同组的其它问题
     */
    private static void predictByGroup(QuestionIntention target, List<QuestionIntention> group_questions) {
        List<QuestionIntention> qualified_questions = group_questions.stream()
                .filter(QuestionIntention::isMarked)
                .collect(Collectors.toList());
        if (qualified_questions.size() == 0) {
            target.setIntention_group_based(null);    // 信息不足，预测失败
            return;
        }
        int unmarked = group_questions.size() - qualified_questions.size();    // 组内未标记意图的问题
        Map<String, Intention> intentions = new HashMap<>();
        for (QuestionIntention question : qualified_questions) {
            String intent_id = question.getIntention_marked();
            Intention intention;
            if (intentions.containsKey(intent_id)) {
                intention = intentions.get(intent_id);
            } else {
                intention = new Intention(intent_id);
                intentions.put(intent_id, intention);
            }
            intention.increment();
            // 标准问题的意图做特殊标记
            if (question.isStandard()) {
                intention.setStandard(true);
            }
        }
        List<Intention> sorted = new ArrayList<>(intentions.values());
        double divisor = 0;
        divisor += ((factor * unmarked) * (factor * unmarked));
        for (Intention intention : sorted) {
            divisor += (intention.getCount() * intention.getCount());
        }
        for (Intention intention : sorted) {
            intention.calculateScore(divisor);
        }
        sorted.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
        target.setIntention_group_based(sorted.get(0).getId());
        target.setScore_group_based(sorted.get(0).getScore());
    }

    /**
     * 用于根据问题组预测意图
     */
    private static class Intention {
        private String id;                        // 意图的id
        private int count = 0;                // 属于该意图的问题数
        private double score = -1;            // 该意图的得分
        private boolean isStandard = false; // 问题组的标准问题是否属于该意图

        Intention(String id) {
            this.id = id;
        }

        String getId() {
            return id;
        }

        int getCount() {
            return count;
        }

        void increment() {
            count++;
        }

        void setStandard(boolean standard) {
            isStandard = standard;
        }

        /**
         * 计算意图得分．
         *
         * @param divisor 　计分公式分母
         */
        void calculateScore(double divisor) {
            score = count * count / divisor;
            // 该问题组的标准问题如果也被标记为该意图，则将意图的基础得分与1取平均，获得更高的新得分．
            if (isStandard) {
                score = (1 + score) / 2;
            }
        }

        double getScore() {
            return score;
        }
    }

}
