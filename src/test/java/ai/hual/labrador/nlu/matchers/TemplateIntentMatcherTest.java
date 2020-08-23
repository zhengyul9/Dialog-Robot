package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.QueryAct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateIntentMatcherTest {
    private static Properties properties;
    private static GrammarModel grammarModel;
    private static TemplateIntentMatcher intentMatcher;

    @BeforeEach
    void setUp() {
        properties = new Properties();
    }

    @Test
    void testStopWords() {
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "intentA", "{{A}}", 1.0f)
        ));
        intentMatcher = new TemplateIntentMatcher(grammarModel, properties);
        // input
        String input1 = "{{A}}的吧?";
        String input2 = "那就{{A}}了";

        // output
        List<QueryAct> resultList1 = intentMatcher.matchIntent(Arrays.asList(new QueryAct(input1)));
        List<QueryAct> resultList2 = intentMatcher.matchIntent(Arrays.asList(new QueryAct(input2)));
        assertEquals(1, resultList1.size());
        assertEquals(1, resultList2.size());
        assertEquals("intentA", resultList1.get(0).getIntent());
        assertEquals("intentA", resultList2.get(0).getIntent());
    }
}