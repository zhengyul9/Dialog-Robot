package ai.hual.labrador.faq.utils;

public class DetectionResult {

    private Integer length;
    private DetectionStatus status;
    private DetectionJudge judgeResult;


    public DetectionResult(Integer length, DetectionStatus status, DetectionJudge judgeResult) {
        this.length = length;
        this.status = status;
        this.judgeResult = judgeResult;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public DetectionStatus getStatus() {
        return status;
    }

    public void setStatus(DetectionStatus status) {
        this.status = status;
    }

    public DetectionJudge getJudgeResult() {
        return judgeResult;
    }

    public void setJudgeResult(DetectionJudge judgeResult) {
        this.judgeResult = judgeResult;
    }

    @Override
    public String toString() {
        return "DetectionResult{" +
                "length=" + length +
                ", status=" + status +
                ", judgeResult=" + judgeResult +
                '}';
    }

}
