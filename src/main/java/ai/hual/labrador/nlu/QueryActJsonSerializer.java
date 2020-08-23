package ai.hual.labrador.nlu;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Serialize QueryAct
 * Created by Dai Wentao on 2017/6/13.
 */
public class QueryActJsonSerializer extends StdSerializer<QueryAct> {

    public QueryActJsonSerializer() {
        super(QueryAct.class);
    }

    @Override
    public void serialize(QueryAct queryAct, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();                                           // {
        gen.writeStringField("intent", queryAct.getIntent());   //   "intent": ...
        gen.writeStringField("regex", queryAct.getRegex());     //   "regex": ...
        gen.writeNumberField("regexStart", queryAct.getRegexStart());     //   "regexStart": ...
        gen.writeNumberField("regexEnd", queryAct.getRegexEnd());       //   "regexEnd": ...
        gen.writeNumberField("regexRealStart", queryAct.getRegexRealStart());     //   "regexRealStart": ...
        gen.writeNumberField("regexRealEnd", queryAct.getRegexRealEnd());       //   "regexRealEnd": ...
        gen.writeStringField("query", queryAct.getQuery());     //   "query": ...
        gen.writeStringField("pQuery", queryAct.getPQuery());   //   "pQuery": ...
        gen.writeObjectFieldStart("slots");                     //   "slots": {
        for (String key : queryAct.getSlots().keySet()) {
            gen.writeArrayFieldStart(key);                                //     key: [
            for (SlotValue value : queryAct.getSlots().get(key)) {
                gen.writeObject(value);                                   //       value,
            }
            gen.writeEndArray();                                          //     ],
        }
        gen.writeEndObject();                                             //   }
        gen.writeNumberField("score", queryAct.getScore());     //   "score": ...
        gen.writeEndObject();                                             // }
    }
}
