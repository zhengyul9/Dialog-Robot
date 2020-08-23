package ai.hual.labrador.dm;

import ai.hual.labrador.nlg.ResponseAct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_RESPONSE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextJsonSerializerTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testContextSerialize() throws JsonProcessingException {

        Context context = new Context();
        Map<String, Object> slots = new HashMap<>();

        ResponseAct responseAct = new ResponseAct();
        responseAct.setLabel("问y");
        responseAct.put("x", "x_content");

        slots.put(SYSTEM_RESPONSE_NAME, responseAct);
        context.setSlots(slots);

        context.setCurrentState(new CurrentState());

        context.setTypes(new HashMap<>());

        String expected = "{\"slots\":{" +
                "\"sys.lastResponse\":{\"label\":\"问y\",\"slots\":{\"x\":[\"x_content\"]}}" +
                "}," +
                "\"types\":{" +
                "}," +
                "\"currentState\":{\"currentState\":null,\"subStates\":null}}";
        assertEquals(expected, objectMapper.writeValueAsString(context));
    }
}