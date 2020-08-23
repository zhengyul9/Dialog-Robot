package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test {@link BracePreprocessor}
 * Created by Dai Wentao on 2017/7/13.
 */
public class BracePreprocessorTest {

    @Test
    void testPreprocess() {
        Preprocessor p = new BracePreprocessor();
        assertEquals("a{bb}cdd{e}", p.preprocess("a{{bb}}cdd{{e}"));
    }

}
