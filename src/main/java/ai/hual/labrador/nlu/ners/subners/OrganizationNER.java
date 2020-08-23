package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.engines.OrganizationNERbyRule;

import java.util.List;

@NER(name = "OrganizationNER", dependencies = {})
public class OrganizationNER implements NERModule {
    private OrganizationNERbyRule engine = new OrganizationNERbyRule();

    @Override
    public NERResult recognize(String text, List<NERResult> nerResults) {
        List<NERResult.Candidate> candidates = engine.organizationRecognize(text);

        return new NERResult(text, candidates);
    }
}
