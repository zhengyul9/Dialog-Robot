package ai.hual.labrador.local;

import ai.hual.labrador.kg.KGConst;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.KnowledgeUtil;
import ai.hual.labrador.kg.SelectResult;
import ai.hual.labrador.kg.utils.KnowledgeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A knowledge accessor that read model from byte array
 * Created by Dai Wentao on 2017/7/3.
 */
public class ByteArrayKnowledgeAccessor implements KnowledgeAccessor {

    public static final String PREFIXES = String.join("\n",
            String.format("PREFIX %s:<%s>", KGConst.RDF_PREFIX, KGConst.RDF_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.RDFS_PREFIX, KGConst.RDFS_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.OWL_PREFIX, KGConst.OWL_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.LBRD_PREFIX, KGConst.LABRADOR_PREFIX),
            String.format("PREFIX %s:<%s>", KGConst.LBRDS_PREFIX, KGConst.LABRADOR_SCHEMA_PREFIX));

    private final KnowledgeUtil kgUnit = new KnowledgeUtil();

    private final OntModel model;


    public ByteArrayKnowledgeAccessor(byte[] bytes) {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        model.read(in, null, Lang.TTL.getName());
    }

    @Override
    public SelectResult select(String queryString) {
        return kgUnit.parseJSONSelectResult(query(queryString));
    }

    @Override
    public List<String> selectOneAsList(String queryString, String var) {
        return kgUnit.parseJSONSelectVarResult(query(queryString), var);
    }

    @Override
    public <T> List<T> selectObjectAsList(String selectString, Class<T> clazz) {
        SelectResult selectResult = select(selectString);
        return KnowledgeUtils.convertToObjects(selectResult, clazz);
    }

    private String query(String queryString) {
        Query query = QueryFactory.create(PREFIXES + queryString);
        try (QueryExecution exec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = exec.execSelect();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

}
