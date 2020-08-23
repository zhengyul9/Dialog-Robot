package ai.hual.labrador.nlg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseActJsonSerializerTest {

    @BeforeAll
    static void setup() {

    }

    @Test
    void testSerialize() throws JsonProcessingException {
        ResponseAct responseAct = new ResponseAct();
        responseAct.setLabel("intent");
        responseAct.getSlots().put("a", "x");
        responseAct.getSlots().put("a", "y");
        responseAct.getSlots().put("b", "x");
        responseAct.getSlots().put("c", ImmutableMap.of("s", 1, "t", 2.0));

        String expected = "{\"label\":\"intent\",\"slots\":{\"a\":[\"x\",\"y\"],\"b\":[\"x\"],\"c\":[{\"s\":1,\"t\":2.0}]}}";
        assertEquals(expected, new ObjectMapper().writeValueAsString(responseAct));
    }

}
