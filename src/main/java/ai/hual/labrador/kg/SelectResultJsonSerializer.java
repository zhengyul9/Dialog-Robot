package ai.hual.labrador.kg;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Serialize SelectResult
 * Created by Dai Wentao on 2017/6/13.
 */
public class SelectResultJsonSerializer extends StdSerializer<SelectResult> {

    public SelectResultJsonSerializer() {
        super(SelectResult.class);
    }

    @Override
    public void serialize(SelectResult result, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();                                     // {
        gen.writeObjectFieldStart("head");                //   "head": {
        gen.writeObjectField("vars", result.getVars());   //     "vars": ...
        gen.writeObjectField("link", result.getLink());   //     "link": ...
        gen.writeEndObject();                                       //   }
        gen.writeObjectFieldStart("results");             //   "results": {
        gen.writeArrayFieldStart("bindings");             //     "bindings": [
        for (Binding b : result.getBindings()) {
            gen.writeStartObject();                                 //     {
            for (String var : result.getVars()) {
                String value = b.value(var);
                if (value != null) {
                    gen.writeStringField(var, value);               //       var: value
                }
            }
            gen.writeEndObject();                                   //     }
        }
        gen.writeEndArray();                                        //     ]
        gen.writeEndObject();                                       //   }
        gen.writeEndObject();                                       // ]
    }
}
