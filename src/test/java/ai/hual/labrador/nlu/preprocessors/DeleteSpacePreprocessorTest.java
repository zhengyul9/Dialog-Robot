package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteSpacePreprocessorTest {

    @Test
    void preprocessFull() {
        Preprocessor test = new DeleteSpacePreprocessor();
        assertEquals("全角空格测试", test.preprocess("全角　空格　测试"));
    }

    @Test
    void preprocessHalf() {
        Preprocessor test = new DeleteSpacePreprocessor();
        assertEquals("半角空格测试", test.preprocess("半角 空格 测试"));
    }
}