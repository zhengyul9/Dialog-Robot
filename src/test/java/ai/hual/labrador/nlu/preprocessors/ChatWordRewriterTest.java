package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static ai.hual.labrador.nlu.preprocessors.Rewriter.SPECIAL_DICT_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatWordRewriterTest {

    private static DictModel dictModel;
    private static DictAnnotator dictAnnotator;
    private static Properties properties;

    @BeforeEach
    void setup() {
        properties = new Properties();
        dictModel = new DictModel();
        dictModel = new DictModel(Arrays.asList(
                new Dict("基金", "000681"), new Dict("基金属性", "净值"), new Dict("基金属性", "风险")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));
    }

    @Test
    void test1() {
        Preprocessor preprocessor = new ChatWordRewriter(dictAnnotator);
        assertEquals("我想看000681风险和净值啊好不好", preprocessor.preprocess("你好我想看000681风险你好和净值啊谢谢好不好"));
    }

    @Test
    void test2() {
        Preprocessor preprocessor = new ChatWordRewriter(dictAnnotator);
        assertEquals("我想看000681风险和净值啊", preprocessor.preprocess("卧槽你好我想看000681风险你好和净值啊"));
    }

    @Test
    void testPreprocessStandardDictWord() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "原则", "规则")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);
        /*
            DictAnnotator put standard word of dict into matched, so when
            substitute {{slot}} by its content, it implicitly convert to
            standard word.
         */
        assertEquals("为什么不原则", preprocessor.preprocess("怎么不规则"));
    }

    @Test
    void testPreprocessContinuousSlots() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "aa"),
                new Dict("B", "b"),
                new Dict("C", "c")
        ));
        dictAnnotator = new DictAnnotator(dictModel, properties, true, Arrays.asList(SPECIAL_DICT_LABEL));
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);

        assertEquals("aabc", preprocessor.preprocess("aabc"));
    }
}