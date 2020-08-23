package ai.hual.labrador.faq.utils;

public class QuestionIntention {    //每一个问题对应一个该类的实例

    private int qid;    //问题本身的id
    private int qaid;    //问题所在的问题组的qaid
    private boolean marked = false;        //问题是否已被标记某个意图
    private String intention_marked;    //已被标记的问题所属的意图
    private DetailedHitsEntity entity = null; //若该问题是ES搜索的结果，在这里存储ES搜索相关信息

    //根据问题的内容本身预测的意图及分数
    private String intention_content_based;    // 负数代表信息不足，预测失败
    private double score_content_based;

    //根据问题所在的问题组预测的意图及分数
    private String intention_group_based;        // 负数代表信息不足，预测失败
    private double score_group_based;

    public QuestionIntention() {
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public int getQaid() {
        return qaid;
    }

    public void setQaid(int qaid) {
        this.qaid = qaid;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getIntention_marked() {
        return intention_marked;
    }

    public void setIntention_marked(String intention_marked) {
        this.intention_marked = intention_marked;
    }

    public String getIntention_content_based() {
        return intention_content_based;
    }

    public void setIntention_content_based(String intention_content_based) {
        this.intention_content_based = intention_content_based;
    }

    public double getScore_content_based() {
        return score_content_based;
    }

    public void setScore_content_based(double score_content_based) {
        this.score_content_based = score_content_based;
    }

    public String getIntention_group_based() {
        return intention_group_based;
    }

    public void setIntention_group_based(String intention_group_based) {
        this.intention_group_based = intention_group_based;
    }

    public double getScore_group_based() {
        return score_group_based;
    }

    public void setScore_group_based(double score_group_based) {
        this.score_group_based = score_group_based;
    }

    public DetailedHitsEntity getEntity() {
        return entity;
    }

    public void setEntity(DetailedHitsEntity entity) {
        this.entity = entity;
    }

    public boolean isStandard() {
        return qaid == qid;
    }
}
