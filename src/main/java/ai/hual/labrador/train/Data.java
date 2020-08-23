package ai.hual.labrador.train;

import java.util.List;

public class Data {

    private List<Corpus> corpora;

    public Data(List<Corpus> corpora) {
        this.corpora = corpora;
    }

    public List<Corpus> getCorpora() {
        return corpora;
    }

    public void setCorpora(List<Corpus> corpora) {
        this.corpora = corpora;
    }
}
