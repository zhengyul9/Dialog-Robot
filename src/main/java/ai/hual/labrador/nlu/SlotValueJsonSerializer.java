package ai.hual.labrador.nlu;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SlotValueJsonSerializer extends StdSerializer<SlotValue> {

    public SlotValueJsonSerializer() {
        super(SlotValue.class);
    }

    @Override
    public void serialize(SlotValue slotValue, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // try to obtain matched object's class name from map
        gen.writeStartObject();                                           // {

        gen.writeObjectField("matched", slotValue.matched);
        gen.writeStringField("type", slotValue.matched.getClass().getName());   // used to deserialize to original object type

        gen.writeStringField("label", slotValue.label);
        gen.writeStringField("key", slotValue.key);
        gen.writeNumberField("realStart", slotValue.realStart);
        gen.writeNumberField("realEnd", slotValue.realEnd);
        gen.writeNumberField("realLength", slotValue.realLength);
        gen.writeStringField("regex", slotValue.regex);

        gen.writeEndObject();
    }
}
