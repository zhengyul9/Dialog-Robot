package ai.hual.labrador.kg;

import java.util.Map;
import java.util.Optional;

/**
 * A binding of SPARQL query result
 * Created by Dai Wentao on 2017/6/1.
 */
public class Binding {

    private final Map<String, RDFTerm> map;

    public Binding(Map<String, RDFTerm> map) {
        this.map = map;
    }

    public String value(String varName) {
        return Optional.ofNullable(map.get(varName)).map(RDFTerm::getValue).orElse(null);
    }

}
