package ai.hual.labrador.dialog.accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RelatedQuestionResponse {

    @JsonProperty("status")
    private int status;

    @JsonProperty("errno")
    private int errno;

    @JsonProperty("msg")
    private List<String> relatedQuestions;

    public RelatedQuestionResponse() {
    }

    public RelatedQuestionResponse(int status, int errno, List<String> relatedQuestions) {
        this.status = status;
        this.errno = errno;
        this.relatedQuestions = relatedQuestions;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public List<String> getRelatedQuestions() {
        return relatedQuestions;
    }

    public void setRelatedQuestions(List<String> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }
}
