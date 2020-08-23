package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StopPunctuationsRewriterTest {

    @Test
    void testPreprocessWithPunc() {
        Preprocessor preprocessor = new StopPunctuationsRewriter();
        assertEquals("你好", preprocessor.preprocess("，。，你。好？，，"));
    }

    @Test
    void testPreprocessWithoutPunc() {
        Preprocessor preprocessor = new StopPunctuationsRewriter();
        assertEquals("你{{好}}", preprocessor.preprocess("你{{好}}"));
    }
}