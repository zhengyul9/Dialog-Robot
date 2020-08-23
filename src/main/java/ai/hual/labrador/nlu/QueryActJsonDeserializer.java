package ai.hual.labrador.nlu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Iterator;

/**
 * Deserialize QueryAct
 * Created by Dai Wentao on 2017/6/13.
 */
public class QueryActJsonDeserializer extends StdDeserializer<QueryAct> {

    private static final ObjectMapper mapper = new ObjectMapper();

    public QueryActJsonDeserializer() {
        super(QueryAct.class);
    }

    @Override
    public QueryAct deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);

        QueryAct queryAct = new QueryAct();
        if (root.get("intent").asText().equals("null"))
            queryAct.setIntent(null);
        else
            queryAct.setIntent(root.get("intent").asText());
        if (root.get("regex").asText().equals("null"))
            queryAct.setRegex(null);
        else
            queryAct.setRegex(root.get("regex").asText());
        queryAct.setRegexStart(root.get("regexStart").intValue());
        queryAct.setRegexEnd(root.get("regexEnd").intValue());
        queryAct.setRegexRealStart(root.get("regexRealStart").intValue());
        queryAct.setRegexRealEnd(root.get("regexRealEnd").intValue());
        queryAct.setQuery(root.get("query").asText());
        queryAct.setPQuery(root.get("pQuery").asText());
        queryAct.setScore(root.get("score").doubleValue());

        JsonNode slots = root.get("slots");
        for (Iterator<String> it = slots.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            for (JsonNode slotValue : slots.get(key)) {
                queryAct.getSlots().put(key, mapper.treeToValue(slotValue, SlotValue.class));
            }
        }

        return queryAct;
    }

}
