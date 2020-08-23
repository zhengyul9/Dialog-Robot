package ai.hual.labrador.kg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserialize SelectResult
 * Created by Dai Wentao on 2017/6/13.
 */
public class SelectResultJsonDeserializer extends StdDeserializer<SelectResult> {

    public SelectResultJsonDeserializer() {
        super(SelectResult.class);
    }

    @Override
    public SelectResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = jp.getCodec().readTree(jp);

        // head vars
        List<String> vars = new ArrayList<>();
        JsonNode varsNode = rootNode.get("head").get("vars");
        if (varsNode != null) {
            varsNode.forEach(node -> vars.add(node.asText()));
        }

        // head link
        List<String> link = new ArrayList<>();
        JsonNode linkNode = rootNode.get("head").get("link");
        if (linkNode != null) {
            linkNode.forEach(node -> link.add(node.asText()));
        }

        List<Binding> bindings = new ArrayList<>();
        rootNode.get("results").get("bindings").forEach(binding -> {
            Map<String, RDFTerm> map = new HashMap<>();
            binding.fields().forEachRemaining(entry -> {
                JsonNode var = entry.getValue();
                RDFTerm rdfTerm = new RDFTerm();
                rdfTerm.setValue(var.get("value").asText());
                rdfTerm.setType(RDFTermType.parse(var.get("type").asText()));
                if (var.has("lang")) {
                    rdfTerm.setLang(var.get("lang").asText());
                }
                if (var.has("datatype")) {
                    rdfTerm.setDataType(var.get("datatype").asText());
                }
                map.put(entry.getKey(), rdfTerm);
            });
            bindings.add(new Binding(map));
        });
        return new SelectResult(vars, link, bindings);
    }
}
