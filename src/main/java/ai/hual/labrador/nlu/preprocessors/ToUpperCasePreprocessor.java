package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.springframework.stereotype.Component;

/**
 * Convert english letters to upper case.
 * Created by Dai Wentao on 2018/8/18.
 */
@Component("toUpperCasePreprocessor")
public class ToUpperCasePreprocessor implements Preprocessor {

    @Override
    public String preprocess(String query) {
        return query.toUpperCase();
    }

}
