package ai.hual.labrador.train.utils;

import java.util.List;

public class CorpusIntentData {
    private String corpusContent;
    private List<String> intents;

    public CorpusIntentData() {
    }


    public CorpusIntentData(String corpusContent, List<String> intents) {
        this.corpusContent = corpusContent;
        this.intents = intents;
    }

    public String getCorpusContent() {
        return corpusContent;
    }

    public void setCorpusContent(String corpusContent) {
        this.corpusContent = corpusContent;
    }

    public List<String> getIntents() {
        return intents;
    }

    public void setIntents(List<String> intents) {
        this.intents = intents;
    }

    @Override
    public String toString() {
        return "CorpusIntentData{" +
                "corpusContent='" + corpusContent + '\'' +
                ", intents=" + intents +
                '}';
    }
}
