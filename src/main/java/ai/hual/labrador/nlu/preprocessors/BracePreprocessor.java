package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.springframework.stereotype.Component;

/**
 * Replacing multiple successive braces into single brace.
 * Created by Dai Wentao on 2017/7/13.
 */
@Component("bracePreprocessor")
public class BracePreprocessor implements Preprocessor {

    @Override
    public String preprocess(String query) {
        return query.replaceAll("\\{(\\{)+", "\\{")
                .replaceAll("}(})+", "\\}");
    }

}
