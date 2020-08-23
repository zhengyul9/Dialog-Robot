package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.preprocessors.utils.ZHConverter;
import org.springframework.stereotype.Component;

@Component("traditionalToSimplifiedChinesePreprocessor")
public class TraditionalToSimplifiedChinesePreprocessor implements Preprocessor {
    /**
     * A converter for translating traditional to simplified chinese.
     *
     * @param query The original query or the query preprocessed by other preprocessor
     * @return
     */
    @Override
    public String preprocess(String query) {
        ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
        String simplifiedStr = converter.convert(query);
        return simplifiedStr;
    }
}
