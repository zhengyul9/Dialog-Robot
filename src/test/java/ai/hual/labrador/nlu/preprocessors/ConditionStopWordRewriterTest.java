package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ConditionStopWordRewriterTest {
    @Test
    void testPreprocess() {
        Preprocessor preprocessor = new ConditionStopWordRewriter();

        assertEquals("保险都不给钱", preprocessor.preprocess("保险都不给钱么"));
        assertEquals("有什么保险不给钱", preprocessor.preprocess("有什么保险不给钱"));
        assertEquals("不给钱的保险有甚么", preprocessor.preprocess("不给钱的保险有甚么"));
    }
}
