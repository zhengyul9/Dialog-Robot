package ai.hual.labrador.faq.utils;

public class QuestionSimplify {
    private String content;

    private int qaid;

    public QuestionSimplify() {

    }

    public QuestionSimplify(String content, int qaid) {
        this.content = content;
        this.qaid = qaid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getQaid() {
        return qaid;
    }

    public void setQaid(int qaid) {
        this.qaid = qaid;
    }

    @Override
    public String toString() {
        return "QuestionSimplify{" +
                "content='" + content + '\'' +
                ", qaid=" + qaid +
                '}';
    }

}
