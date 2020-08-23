package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizeWordRewriterTest {
    private static DictModel dictModel;
    private static DictAnnotator dictAnnotator;

    @BeforeEach
    void setup() {
        dictModel = new DictModel();
        dictAnnotator = new DictAnnotator(dictModel, new Properties());
    }

    @Test
    void preprocess() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("基金", "000681"),
                new Dict("基金属性", "净值"),
                new Dict("基金风险属性", "风险"),
                new Dict("A", "劳做")
        ));
        dictAnnotator = new DictAnnotator(dictModel, new Properties());

        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);
        assertEquals("如何查000681的净值，如何查000681的风险", preprocessor.preprocess("怎么查000681的净值，怎样查000681的风险"));
        assertEquals("劳做啥", preprocessor.preprocess("劳做啥"));
    }

    @Test
    void testPreprocessExploitTokenizer() {
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);
        dictAnnotator = new DictAnnotator(dictModel, new Properties());

        assertEquals("如何", preprocessor.preprocess("怎么"));
        // "怎么办" is treated as a whole word by tokenizer
        assertEquals("怎么办", preprocessor.preprocess("怎么办"));
    }

    @Test
    void testPreprocessVerySpecialCase() {
        /*
            "怎么不" should not be replaced by "如何不"(synonym should be "为何不"),
            but add a dict as (synonym	为什么不	怎么不,为啥不,为何不)
            won't work, because the tokenizer will split "怎么不" as ["怎么", "不"],
            which means "怎么" should not always be replaced to "如何" in different context.
        */
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);

        assertEquals("为什么不", preprocessor.preprocess("怎么不"));
        assertEquals("为什么不能", preprocessor.preprocess("怎么不能"));
    }

    @Test
    void testPreprocessTime() {
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);

        long start = System.currentTimeMillis();
        preprocessor.preprocess("怎么不");
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.println(timeElapsed);
    }

    @Test
    void testPreprocessStandardDictWord() {
        dictModel = new DictModel(Arrays.asList(
                new Dict("A", "原则", "规则")
        ));
        dictAnnotator = new DictAnnotator(dictModel, new Properties());
        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);
        /*
            DictAnnotator put standard word of dict into matched, so when
            substitute {{slot}} by its content, it implicitly convert to
            standard word.
         */
        assertEquals("为什么不原则", preprocessor.preprocess("怎么不规则"));
    }

//    @Test
//    // unpassed
//    void testPreprocess() {
//        dictModel = new DictModel(Arrays.asList(
//        ));
//        dictAnnotator = new DictAnnotator(dictModel, new Properties());
//
//        Preprocessor preprocessor = new NormalizeWordRewriter(dictAnnotator);
//        assertEquals("如果{{人寿保险_产品}}试试手的{{对象}}去世了可以{{人寿保险_产品}}吗", preprocessor.preprocess("如果{{人寿保险_产品}}的{{对象}}去世了可以{{人寿保险_产品}}吗"));
//    }
}