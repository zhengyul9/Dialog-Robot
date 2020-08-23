package ai.hual.labrador.train.utils;

public class CorpusTrainData {
    private int tag = 0;
    private String target;
    private String query;

    public CorpusTrainData() {
    }

    public CorpusTrainData(int tag, String target, String query) {
        this.tag = tag;
        this.target = target;
        this.query = query;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "CorpusTrainData{" +
                "tag=" + tag +
                ", target='" + target + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}
