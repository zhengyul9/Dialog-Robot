package ai.hual.labrador.nlg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Iterator;

/**
 * Deserialize ResponseAct
 * Created by Dai Wentao on 2017/6/13.
 */
public class ResponseActJsonDeserializer extends StdDeserializer<ResponseAct> {

    private static final ObjectMapper mapper = new ObjectMapper();

    public ResponseActJsonDeserializer() {
        super(ResponseAct.class);
    }

    @Override
    public ResponseAct deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);

        ResponseAct responseAct = new ResponseAct();
        responseAct.setLabel(root.get("label").asText());

        JsonNode slots = root.get("slots");
        for (Iterator<String> it = slots.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            for (JsonNode value : slots.get(key)) {
                responseAct.getSlots().put(key, mapper.treeToValue(value, Object.class));
            }
        }

        return responseAct;
    }

}
