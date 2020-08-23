package ai.hual.labrador.nlu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryActJsonDeserializerTest {

    @BeforeAll
    static void setup() {

    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeserialize() throws IOException {
        String json = "{\"intent\":\"intent\",\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"query\",\"pQuery\":\"pQuery\",\"slots\":{\"a\":[{\"matched\":\"x\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0},{\"matched\":\"y\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0}],\"b\":[{\"matched\":\"x\",\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0}],\"c\":[{\"matched\":{\"s\":1,\"t\":2.0},\"label\":\"\",\"key\":\"\",\"realStart\":0,\"realEnd\":0,\"realLength\":0}]},\"score\":1.0}";

        QueryAct queryAct = new ObjectMapper().readValue(json, QueryAct.class);
        assertEquals("intent", queryAct.getIntent());
        assertEquals("query", queryAct.getQuery());
        assertEquals("pQuery", queryAct.getPQuery());
        assertEquals(1f, queryAct.getScore());

        assertEquals("x", queryAct.getSlots().get("a").get(0).getMatched());
        assertEquals("y", queryAct.getSlots().get("a").get(1).getMatched());
        assertEquals("x", queryAct.getSlots().get("b").get(0).getMatched());
        assertEquals(1, ((Map) queryAct.getSlots().get("c").get(0).getMatched()).get("s"));
        assertEquals(2.0, ((Map) queryAct.getSlots().get("c").get(0).getMatched()).get("t"));
    }

}
