package ai.hual.labrador.faq;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FaqRankResponse {

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("hits")
    private List<FaqRankResult> hits;

    @JsonProperty("status")
    private int status;

    public FaqRankResponse() {
    }

    public FaqRankResponse(double confidence, List<FaqRankResult> hits, int status) {
        this.confidence = confidence;
        this.hits = hits;
        this.status = status;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<FaqRankResult> getHits() {
        return hits;
    }

    public void setHits(List<FaqRankResult> hits) {
        this.hits = hits;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}