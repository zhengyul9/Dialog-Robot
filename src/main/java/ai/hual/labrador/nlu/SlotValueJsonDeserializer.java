package ai.hual.labrador.nlu;

import ai.hual.labrador.exceptions.NLUException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class SlotValueJsonDeserializer extends StdDeserializer<SlotValue> {

    private static final ObjectMapper mapper = new ObjectMapper();

    public SlotValueJsonDeserializer() {
        super(SlotValue.class);
    }

    @Override
    public SlotValue deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);

        SlotValue slotValue = new SlotValue();

        // deserialize matched object by its class type, if exists
        String matchedClassName = root.get("type") == null ? null : root.get("type").asText();
        if (matchedClassName == null)
            slotValue.matched = mapper.readValue(root.get("matched").toString(), Object.class);
        else {  // cast to the right class
            try {
                slotValue.matched = mapper.readValue(root.get("matched").toString(), Class.forName(matchedClassName));
            } catch (ClassNotFoundException e) {
                throw new NLUException("Deserialize SlotValue failed");
            }
        }

        slotValue.label = root.get("label").asText();
        slotValue.key = root.get("key").asText();
        slotValue.realStart = root.get("realStart").asInt();
        slotValue.realEnd = root.get("realEnd").asInt();
        slotValue.realLength = root.get("realLength").asInt();

        return slotValue;
    }
}
