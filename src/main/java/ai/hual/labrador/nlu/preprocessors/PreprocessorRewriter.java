package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.annotators.DictAnnotator;

import java.util.Properties;

public class PreprocessorRewriter implements Preprocessor {

    private Preprocessors preprocessors;

    private Rewriter rewriter;

    public PreprocessorRewriter(DictAnnotator dictAnnotator, Properties properties) {
        preprocessors = new Preprocessors(properties);
        rewriter = new Rewriter(dictAnnotator, properties);
    }

    @Override
    public String preprocess(String query) {
        return this.rewriter.preprocess(preprocessors.preprocess(query));
    }
}
