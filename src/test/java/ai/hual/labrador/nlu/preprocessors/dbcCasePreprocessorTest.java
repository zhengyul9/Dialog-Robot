package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class dbcCasePreprocessorTest {

    @Test
    void words() {
        Preprocessor p = new DbcCasePreprocessor();
        assertEquals("this is a test for DBC case! ",
                p.preprocess("ｔｈｉｓ　ｉｓ　ａ　ｔｅｓｔ　ｆｏｒ　ＤＢＣ　ｃａｓｅ！　"));
    }

    @Test
    void nums() {
        Preprocessor p = new DbcCasePreprocessor();
        assertEquals("1234",
                p.preprocess("１２３４"));
    }
}