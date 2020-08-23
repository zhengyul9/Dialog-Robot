package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.earlyStopProcess;

public class IntentRewriter implements Preprocessor {

    private List<Preprocessor> rewriters;

    public IntentRewriter(Properties properties) {
        Preprocessor stopWordRewriter = new StopWordRewriter();
        Preprocessor stopPunctuationsRewriter = new StopPunctuationsRewriter();
        this.rewriters = Arrays.asList(stopPunctuationsRewriter, stopPunctuationsRewriter, stopWordRewriter);
    }

    @Override
    public String preprocess(String query) {
        return earlyStopProcess(query, this.rewriters);
    }
}
