package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;

import java.util.List;

@NER(name = "DigitalNER", dependencies = {})
public class DigitalNER implements NERModule {

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        return null;
    }

}
