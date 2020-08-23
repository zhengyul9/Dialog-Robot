package ai.hual.labrador.train;

import java.util.List;
import java.util.Map;

public class Corpus {

    /**
     * The intent label of this corpus
     */
    private Map<String, TrainTag> intentMap;

    /**
     * The content of this corpus
     */
    private String content;

    /**
     * The annotation added to this corpus
     */
    private List<CorpusAnnotation> annotations;

    public Corpus(Map<String, TrainTag> intentMap, String content, List<CorpusAnnotation> annotations) {
        this.intentMap = intentMap;
        this.content = content;
        this.annotations = annotations;
    }

    public Map<String, TrainTag> getIntentMap() {
        return intentMap;
    }

    public void setIntentMap(Map<String, TrainTag> intentMap) {
        this.intentMap = intentMap;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<CorpusAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<CorpusAnnotation> annotations) {
        this.annotations = annotations;
    }

}
