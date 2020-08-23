package ai.hual.labrador.faq.utils;

public class AverageQuestionScore {
    private int id;                        //　标志某个问题类的id
    private String intent_id;           // intent id 为字符串
    private int question_count = 0;        // 属于该类的问题数
    private double total_score = 0;        // 属于该类的所有问题的score/selfBm25值的总和
    private double average_score = -1;

    public AverageQuestionScore(String intent_id) {
        this.intent_id = intent_id;
    }

    public AverageQuestionScore(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIntent_id() {
        return intent_id;
    }

    public void setIntent_id(String intent_id) {
        this.intent_id = intent_id;
    }

    public int getQuestion_count() {
        return question_count;
    }

    public void setQuestion_count(int question_count) {
        this.question_count = question_count;
    }

    /**
     * 添加一个问题,并累加分数。
     *
     * @param score 添加的问题的分数
     */
    public void addQuestionScore(double score) {
        total_score += score;
        question_count += 1;
    }

    /**
     * 返回ES结果中，属于该类的所有问题的score/selfBm25值的平均值
     *
     * @return score/selfBm25平均值
     */
    public double averageScore() {
        return average_score > 0 ? average_score : (average_score = total_score / question_count);
    }
}
