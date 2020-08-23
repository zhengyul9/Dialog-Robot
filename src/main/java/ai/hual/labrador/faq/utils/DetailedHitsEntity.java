package ai.hual.labrador.faq.utils;


/**
 * 每个实例代表ES搜索得到的一个相似问题
 */
public class DetailedHitsEntity {
    private int qaid;                //　该问题所属的问题组的qaid
    private double score;            //　该问题的ES搜索得分
    private double selfBm25;        //　该问题的selfBm25分数
    private double question_score = -1; // 该问题的得分

    public DetailedHitsEntity(int qaid, double score, double selfBm25) {
        this.qaid = qaid;
        this.score = score;
        this.selfBm25 = selfBm25;
    }

    public double questionScore() {
        return question_score > 0 ? question_score : (question_score = score / selfBm25);
    }

    public int getQaid() {
        return qaid;
    }

    public void setQaid(int qaid) {
        this.qaid = qaid;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getSelfBm25() {
        return selfBm25;
    }

    public void setSelfBm25(double selfBm25) {
        this.selfBm25 = selfBm25;
    }
}
