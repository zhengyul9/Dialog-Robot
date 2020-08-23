package ai.hual.labrador.nlg;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Serialize ResponseAct
 * Created by Dai Wentao on 2017/6/13.
 */
public class ResponseActJsonSerializer extends StdSerializer<ResponseAct> {

    public ResponseActJsonSerializer() {
        super(ResponseAct.class);
    }

    @Override
    public void serialize(ResponseAct act, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();                                        // {
        gen.writeStringField("label", act.getLabel());       //   "label": ...
        gen.writeObjectFieldStart("slots");                  //   "slots": {
        for (String key : act.getSlots().keySet()) {
            gen.writeArrayFieldStart(key);                             //     key: [
            for (Object value : act.getSlots().get(key)) {
                gen.writeObject(value);                                //       value,
            }
            gen.writeEndArray();                                       //     ],
        }
        gen.writeEndObject();                                          //   }
        gen.writeEndObject();                                          // }
    }
}
