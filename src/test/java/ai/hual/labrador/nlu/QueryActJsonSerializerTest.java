package ai.hual.labrador.nlu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryActJsonSerializerTest {

    @BeforeAll
    static void setup() {

    }

    @Test
    void testSerialize() throws JsonProcessingException {
        QueryAct queryAct = new QueryAct();
        queryAct.setIntent("intent");
        queryAct.setQuery("query");
        queryAct.setPQuery("pQuery");
        queryAct.setScore(1f);
        queryAct.getSlots().put("a", new SlotValue("x"));
        queryAct.getSlots().put("a", new SlotValue("y"));
        queryAct.getSlots().put("b", new SlotValue("x"));
        queryAct.getSlots().put("c", new SlotValue(ImmutableMap.of("s", 1, "t", 2.0)));

        String expected = "{\"intent\":\"intent\",\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"query\",\"pQuery\":\"pQuery\",\"slots\":{\"a\":[{\"matched\":\"x\",\"type\":\"java.lang.String\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0,\"regex\":null},{\"matched\":\"y\",\"type\":\"java.lang.String\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0,\"regex\":null}],\"b\":[{\"matched\":\"x\",\"type\":\"java.lang.String\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0,\"regex\":null}],\"c\":[{\"matched\":{\"s\":1,\"t\":2.0},\"type\":\"com.google.common.collect.RegularImmutableMap\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0,\"regex\":null}]},\"score\":1.0}";
        assertEquals(expected, new ObjectMapper().writeValueAsString(queryAct));
    }

}
