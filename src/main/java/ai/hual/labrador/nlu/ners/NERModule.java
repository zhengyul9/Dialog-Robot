package ai.hual.labrador.nlu.ners;

import java.util.List;

public interface NERModule {

    NERResult recognize(String text, List<NERResult> nerResults);

}
