package ai.hual.labrador.faq.utils;

import java.util.List;

public class HitsEntity {
    private Integer qid;
    private List<IntegerPair> hits;

    public HitsEntity(Integer qid, List<IntegerPair> hits) {
        this.qid = qid;
        this.hits = hits;
    }

    public Integer getQid() {
        return qid;
    }

    public void setQid(Integer qid) {
        this.qid = qid;
    }

    public List<IntegerPair> getHits() {
        return hits;
    }

    public void setHits(List<IntegerPair> hits) {
        this.hits = hits;
    }
}
