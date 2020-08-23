package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.utils.ComponentScanUtils;

import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.NLUImpl.DEFAULT_PREPROCESSORS;
import static ai.hual.labrador.nlu.NLUImpl.PREPROCESSOR_PACKAGE;
import static ai.hual.labrador.nlu.NLUImpl.PREPROCESSOR_PROP_NAME;
import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.earlyStopProcess;

public class Preprocessors implements Preprocessor {

    private List<Preprocessor> preprocessors;

    private boolean verbose;

    public Preprocessors(Properties properties) {
        this(properties, false);
    }

    public Preprocessors(Properties properties, boolean verbose) {
        this.verbose = verbose;
        String preprocessors = properties.getProperty(PREPROCESSOR_PROP_NAME);
        if (preprocessors != null) {
            this.preprocessors = ComponentScanUtils.scan(preprocessors, PREPROCESSOR_PACKAGE, Preprocessor.class);
        } else {
            this.preprocessors = ComponentScanUtils.scan(DEFAULT_PREPROCESSORS, PREPROCESSOR_PACKAGE, Preprocessor.class);
        }
    }

    @Override
    public String preprocess(String query) {
        return earlyStopProcess(query, preprocessors, verbose);
    }

}
