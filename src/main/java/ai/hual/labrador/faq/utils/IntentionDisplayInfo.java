package ai.hual.labrador.faq.utils;

/**
 * 给定了意图以后，每条问题与知识点预测结果相关的信息
 */
public class IntentionDisplayInfo {
    private int qid;                            // 问题的id
    private double score_group_based = 0;        // 若问题根据所在问题组预测所得意图为给定意图，则该字段储存预测得分
    private double score_content_based = 0;        // 若问题根据问题内容预测所得意图为给定意图，则该字段储存预测得分
    private DetailedHitsEntity entity = null;    // 若问题为根据query进行ES搜索的结果，则该字段储存对应的entity

    public IntentionDisplayInfo(int qid) {
        this.qid = qid;
    }

    public int getQid() {
        return qid;
    }

    private static final double[] weights = {0.236, 0.146, 0.618};    //　score_content_based, score_group_based, 搜索得分，三者加权平均的权重

    public double getScore() {
        return score_content_based * weights[0] + score_group_based * weights[1]
                + (entity == null ? 0 : entity.questionScore() * weights[2]);
    }

    public double getScore_group_based() {
        return score_group_based;
    }

    public void setScore_group_based(double score_group_based) {
        this.score_group_based = score_group_based;
    }

    public double getScore_content_based() {
        return score_content_based;
    }

    public void setScore_content_based(double score_content_based) {
        this.score_content_based = score_content_based;
    }

    public DetailedHitsEntity getEntity() {
        return entity;
    }

    public void setEntity(DetailedHitsEntity entity) {
        this.entity = entity;
    }


}
