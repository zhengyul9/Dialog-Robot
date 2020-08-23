package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.engines.LocationNERbyRule;

import java.util.List;

@NER(name = "LocationNER", dependencies = {})
public class LocationNER implements NERModule {
    private LocationNERbyRule engine = new LocationNERbyRule();

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        List<NERResult.Candidate> candidates = engine.locationRecognize(text);
        return new NERResult(text, candidates);
    }

}
