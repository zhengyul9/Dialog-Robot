package ai.hual.labrador.dm;

import ai.hual.labrador.exceptions.DMException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Strings;

import java.io.IOException;

public class ContextJsonSerializer extends StdSerializer<Context> {
    public ContextJsonSerializer() {
        super(Context.class);
    }

    @Override
    public void serialize(Context context, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();                                           // {

        gen.writeObjectFieldStart("slots");                     // slots: {
        for (String key : context.getSlots().keySet()) {
            String typeStr = context.slotTypeByName(key);
            Object obj = context.slotContentByName(key);
            if (Strings.isNullOrEmpty(typeStr) || typeStr.replaceAll("\\s", "").equals("") || obj == null)
                gen.writeObjectField(key, obj);
            else {
                try {
                    gen.writeObjectField(key, (Class.forName(typeStr).cast(obj)));
                } catch (ClassNotFoundException e) {
                    throw new DMException("Can not serialize context's slot with type " + typeStr + ", with slot's content: " +
                            obj);
                }
            }
        }
        gen.writeEndObject();

        gen.writeObjectField("types", context.getTypes());
        gen.writeObjectField("currentState", context.getCurrentState());
        gen.writeEndObject();
    }
}
