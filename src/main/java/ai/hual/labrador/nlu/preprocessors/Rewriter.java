package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.NLUImpl;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import ai.hual.labrador.utils.ComponentScanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.earlyStopProcess;

@Component("rewriter")
public class Rewriter implements Preprocessor {

    public static final String SPECIAL_DICT_LABEL = "__其它核心词__";

    public static final String PROP_NLU_REWRITERS = "nlu.rewriters";
    public static final String DEFAULT_REWRITERS = "stopPunctuationsRewriter," +
            "normalizeWordRewriter," +
            "stopPunctuationsRewriter," +
            "stopWordRewriter," +
            "chatWordRewriter";

    private List<Preprocessor> rewriters;

    /**
     * Constructor of Preprocessor.
     *
     * <Strong>NOTICE:</Strong> {@link StopPunctuationsRewriter} appeared
     * twice in the rewrite flow to deal with the case where normalized word might contain punctuations, see
     * unit test RewriterTest#testPreprocessStopPuncOfNormalizedWord.
     *
     * <Strong>NOTICE:</Strong> The parameter <t>dictAnnotator</t> should include dict whose label is
     * {@value SPECIAL_DICT_LABEL}, so, those words are implicitly normalized in normalizeWordRewriter.
     * These 2 rewriters use dictAnnotator to prevent business words being substituted unintently.
     *
     * @param dictAnnotator dict annotator
     * @param properties    might be useful later
     */
    public Rewriter(DictAnnotator dictAnnotator, Properties properties) {
        String rewriters = properties.getProperty(PROP_NLU_REWRITERS, DEFAULT_REWRITERS);
        this.rewriters = ComponentScanUtils
                .withBean("dictAnnotator", dictAnnotator)
                .scan(rewriters, NLUImpl.PREPROCESSOR_PACKAGE, Preprocessor.class);
    }

    @Override
    public String preprocess(String query) {
        return earlyStopProcess(query, this.rewriters);
    }

}
