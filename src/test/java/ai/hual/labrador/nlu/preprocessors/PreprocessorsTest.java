package ai.hual.labrador.nlu.preprocessors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static ai.hual.labrador.nlu.NLUImpl.PREPROCESSOR_PROP_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreprocessorsTest {
    private static Properties properties;
    private static Preprocessors preprocessors;

    @BeforeEach
    void setup() {
        properties = new Properties();
    }

    @Test
    void testPreprocessWithDefaultConfig() {
        preprocessors = new Preprocessors(properties);
        assertEquals("ab", preprocessors.preprocess("a b"));
    }

    @Test
    void testPreprocessWithProp() {
        properties.setProperty(PREPROCESSOR_PROP_NAME, "toUpperCasePreprocessor");
        preprocessors = new Preprocessors(properties);
        assertEquals("A B", preprocessors.preprocess("a b"));
    }
}