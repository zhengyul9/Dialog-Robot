package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.engines.AgeNERbyRule;

import java.util.List;

@NER(name = "AgeNER", dependencies = {})
public class AgeNER implements NERModule {
    private AgeNERbyRule engine = new AgeNERbyRule();

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        List<NERResult.Candidate> candidates = engine.ageRecognize(text);
        return new NERResult(text, candidates);
    }

}
