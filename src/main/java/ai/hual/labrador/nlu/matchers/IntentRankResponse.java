package ai.hual.labrador.nlu.matchers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IntentRankResponse {
    @JsonProperty("confidence")
    private float confidence;

    @JsonProperty("hits")
    private List<IntentRankResult> hits;

    @JsonProperty("status")
    private int status;

    public IntentRankResponse() {
    }

    public IntentRankResponse(float confidence, List<IntentRankResult> hits, int status) {
        this.confidence = confidence;
        this.hits = hits;
        this.status = status;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public List<IntentRankResult> getHits() {
        return hits;
    }

    public void setHits(List<IntentRankResult> hits) {
        this.hits = hits;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

