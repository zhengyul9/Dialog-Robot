package ai.hual.labrador.kg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * A SPARQL query result.
 * See https://www.w3.org/TR/sparql11-results-json/
 * https://www.w3.org/TR/rdf-sparql-XMLres/
 * Created by Dai Wentao on 2017/5/8.
 */
@JsonSerialize(using = SelectResultJsonSerializer.class)
@JsonDeserialize(using = SelectResultJsonDeserializer.class)
public class SelectResult {
    private List<String> vars;
    private List<String> link;
    private List<Binding> bindings;

    public SelectResult() {

    }

    public SelectResult(List<String> vars, List<String> link, List<Binding> bindings) {
        this.vars = vars;
        this.link = link;
        this.bindings = bindings;
    }

    public List<String> getVars() {
        return vars;
    }

    public void setVars(List<String> vars) {
        this.vars = vars;
    }

    public List<String> getLink() {
        return link;
    }

    public void setLink(List<String> link) {
        this.link = link;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }
}
