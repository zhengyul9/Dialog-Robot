package ai.hual.labrador.dialog;

import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.local.ByteArrayKnowledgeAccessor;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.GrammarModel;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeHandlerTest {

    private static final String TEST_GRAPH_TTL = "/kg/knowledgeHandlerTest.ttl";

    private static KnowledgeHandler knowledgeHandler;

    @BeforeAll
    static void setUp() throws IOException {
        KnowledgeAccessor kgAccessor = new ByteArrayKnowledgeAccessor(ByteStreams.toByteArray(
                KnowledgeHandlerTest.class.getResourceAsStream(TEST_GRAPH_TTL)));
        knowledgeHandler = new KnowledgeHandler(kgAccessor);
    }

    @Test
    void testHandleDictWithCounterpartInBaseModelAndDoNotAdd() {
        DictModel dictModel = new DictModel();
        Dict manuallyAddedDict = new Dict("实体类1", "实体1", "a1,a2");
        dictModel.getDict().add(manuallyAddedDict);
        knowledgeHandler.handleDict(dictModel);

        List<Dict> dicts = dictModel.getDict().stream()
                .filter(d -> d.getLabel().equals(manuallyAddedDict.getLabel()) && d.getWord().equals(manuallyAddedDict.getWord()))
                .collect(Collectors.toList());
        assertEquals(1, dicts.size());
        assertEquals(manuallyAddedDict.getAliases(), dicts.get(0).getAliases());
    }

    @Test
    void testHandleDict() {
        DictModel dictModel = new DictModel();
        knowledgeHandler.handleDict(dictModel);
        Map<String, String> wordToLabel = dictModel.getDict().stream().collect(Collectors.toMap(
                Dict::getWord, Dict::getLabel));
        assertEquals("class", wordToLabel.get("实体类1"));
        assertEquals("class", wordToLabel.get("实体子类1"));
        assertEquals("class", wordToLabel.get("条件实体类1"));
        assertEquals("class", wordToLabel.get("条件BN类1"));
        assertEquals("class", wordToLabel.get("条件BN子类1"));
        assertEquals("class", wordToLabel.get("YBN类1"));
        assertEquals("class", wordToLabel.get("YBN子类1"));
        assertEquals("实体类1", wordToLabel.get("实体1"));
        assertEquals("实体类1", wordToLabel.get("实体2"));
        assertEquals("实体类1", wordToLabel.get("实体3"));
        assertEquals("条件BN子类1", wordToLabel.get("条件BN1"));
        assertEquals("条件BN子类1", wordToLabel.get("条件BN2"));
        assertEquals("条件实体类1", wordToLabel.get("条件实体1"));
        assertEquals("条件实体类1", wordToLabel.get("条件实体2"));
        assertEquals("YBN类1", wordToLabel.get("YBN1"));
    }

    @Test
    void testHandleGrammar() {
        GrammarModel grammarModel = new GrammarModel();
        knowledgeHandler.handleGrammar(grammarModel);
        assertTrue(grammarModel.getGrammars().stream()
                .anyMatch(x -> Pattern.compile(x.getContent()).matcher("{{实体类1}}的数值属性1").find()));
    }

}
