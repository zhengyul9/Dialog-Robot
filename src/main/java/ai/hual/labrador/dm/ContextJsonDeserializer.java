package ai.hual.labrador.dm;

import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.nlu.QueryAct;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_HYPS_NAME;

public class ContextJsonDeserializer extends StdDeserializer<Context> {
    private static final ObjectMapper mapper = new ObjectMapper();

    public ContextJsonDeserializer() {
        super(Context.class);
    }

    @Override
    public Context deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);

        Context context = new Context();
        JsonNode types = root.get("types");
        JsonNode slots = root.get("slots");
        for (Iterator<String> it = slots.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            JsonNode object = slots.get(key);
            JsonNode type = types.get(key);
            if (key.equals(SYSTEM_HYPS_NAME)) {  // list of queryAct
                List<QueryAct> hyps = new ArrayList<>();
                for (JsonNode queryAct : object)
                    hyps.add(mapper.treeToValue(queryAct, QueryAct.class));
                context.putSlotContent(SYSTEM_HYPS_NAME, hyps);
            } else if (type == null || String.valueOf(type).equals("\"\"")) {
                context.getSlots().put(key, mapper.treeToValue(object, Object.class));
            } else {
                String typeStr = String.valueOf(type).replaceAll("\"", "");
                context.getTypes().put(key, typeStr);
                try {
                    context.getSlots().put(key, mapper.treeToValue(object, Class.forName(typeStr)));
                } catch (ClassNotFoundException e) {
                    throw new DMException("Can not deserialize context's slot with type " + typeStr);
                }
            }
        }
        context.setCurrentState(mapper.treeToValue(root.get("currentState"), CurrentState.class));
        return context;
    }
}
