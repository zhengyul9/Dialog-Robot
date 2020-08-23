package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static ai.hual.labrador.nlu.preprocessors.Rewriter.SPECIAL_DICT_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PreprocessorRewriterTest {
    private static DictModel dictModel;
    private static DictAnnotator dictAnnotator;
    private static Properties properties;
    private static PreprocessorRewriter preprocessorRewriter;

    @BeforeEach
    void setup() {
        properties = new Properties();
        dictModel = new DictModel();
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));
    }

    @Test
    void testPreprocessStopPuncOfNormalizedWordWorkingBecauseUpperCaseNotDefaultConfig() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "c.d", "ab")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));

        preprocessorRewriter = new PreprocessorRewriter(dictAnnotator, properties);
        assertEquals("cd", preprocessorRewriter.preprocess("ab"));
    }

    @Test
    void testPreprocessStopPuncOfNormalizedWord() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "c.d", "ab")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));

        preprocessorRewriter = new PreprocessorRewriter(dictAnnotator, properties);
        assertEquals("cd", preprocessorRewriter.preprocess("ab"));
    }

    @Test
    void testPreprocessWillReduceDoubleCurlyBraceToSingleOne() {
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));

        preprocessorRewriter = new PreprocessorRewriter(dictAnnotator, properties);
        assertEquals("a{b}", preprocessorRewriter.preprocess("a{{b}}"));
    }
}


