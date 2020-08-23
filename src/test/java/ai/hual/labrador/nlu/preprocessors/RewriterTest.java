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

public class RewriterTest {
    private static DictModel dictModel;
    private static DictAnnotator dictAnnotator;
    private static Properties properties;
    private static Rewriter rewriter;

    @BeforeEach
    void setup() {
        properties = new Properties();
        dictModel = new DictModel();
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));
    }

    @Test
    void testPreprocessStopPuncOfNormalizedWord() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "c.d", "ab")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));

        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("cd", rewriter.preprocess("ab"));
    }

    @Test
    void testSingleStopWordOnly() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("啊", rewriter.preprocess("啊"));
    }

    @Test
    void testSinglePunctuationOnly() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("？", rewriter.preprocess("？"));
    }

    @Test
    void testStopWordsOnly() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("啊呀嘛", rewriter.preprocess("啊呀嘛"));
    }

    @Test
    void testPunctuationsOnly() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("。？,", rewriter.preprocess("。？,"));
    }

    @Test
    void testStopBeforeStopWords() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("啊呀嘛", rewriter.preprocess("啊。呀?嘛"));
    }

    @Test
    void testNormalizeCommonWord() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("如何办卡", rewriter.preprocess("怎么办卡"));
    }

    @Test
    void testPreprocessWillNotDeleteCurlyBrace() {
        rewriter = new Rewriter(dictAnnotator, properties);
        assertEquals("如何{办卡}", rewriter.preprocess("怎么{办卡}"));
    }

}

