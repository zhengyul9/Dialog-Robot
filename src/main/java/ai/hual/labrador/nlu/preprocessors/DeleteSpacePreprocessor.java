package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.springframework.stereotype.Component;

@Component("deleteSpacePreprocessor")
public class DeleteSpacePreprocessor implements Preprocessor {
    @Override
    public String preprocess(String query) {
        query = query.trim();
        query = query.replaceAll("\\s", "");
        query = query.replaceAll("\u00A0", "");  // no break space, /uA0
        query = query.replaceAll("　", "");  // chinese space
        return query;
    }
}
