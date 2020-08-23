package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.engines.PersonNERbyRule;

import java.util.List;

@NER(name = "PersonNER", dependencies = {})
public class PersonNER implements NERModule {
    private PersonNERbyRule engine = new PersonNERbyRule();

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        List<NERResult.Candidate> candidates = engine.personRecognize(text);

        return new NERResult(text, candidates);
    }

}


