package ai.hual.labrador.nlg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseActJsonDeserializerTest {

    @BeforeAll
    static void setup() {

    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeserialize() throws IOException {
        String json = "{\"label\":\"intent\",\"slots\":{\"a\":[\"x\",\"y\"],\"b\":[\"x\"],\"c\":[{\"s\":1,\"t\":2.0}]}}";

        ResponseAct responseAct = new ObjectMapper().readValue(json, ResponseAct.class);
        assertEquals("intent", responseAct.getLabel());

        assertEquals("x", responseAct.getSlots().get("a").get(0));
        assertEquals("y", responseAct.getSlots().get("a").get(1));
        assertEquals("x", responseAct.getSlots().get("b").get(0));
        assertEquals(1, ((Map) responseAct.getSlots().get("c").get(0)).get("s"));
        assertEquals(2.0, ((Map) responseAct.getSlots().get("c").get(0)).get("t"));
    }

}
