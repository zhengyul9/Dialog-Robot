package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraditionalToSimplifiedChinesePreprocessorTest {

    @Test
    void preprocess() {
        Preprocessor p = new TraditionalToSimplifiedChinesePreprocessor();
        assertEquals("繁体字测试集", p.preprocess("繁體字測試集"));
    }

    @Test
    void preprocess1() {
        Preprocessor p = new TraditionalToSimplifiedChinesePreprocessor();
        assertEquals("简体繁体测试系统", p.preprocess("簡體繁體測試系統"));
    }

    @Test
    void preprocess2() {
        Preprocessor p = new TraditionalToSimplifiedChinesePreprocessor();
        assertEquals("华来智慧", p.preprocess("華來智慧"));
    }
}