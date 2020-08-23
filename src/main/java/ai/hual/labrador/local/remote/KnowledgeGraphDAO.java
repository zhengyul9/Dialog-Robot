package ai.hual.labrador.local.remote;


import ai.hual.labrador.kg.SelectResult;

import java.io.InputStream;
import java.util.List;

/**
 * A DAO for knowledge graph.
 * All the graphs are typed as Labrador Graph (&lt;prefix&gt;/schema/graph).
 * Created by Dai Wentao on 2017/5/7.
 */
interface KnowledgeGraphDAO {

    /**
     * Create a graph
     *
     * @param graph The name of the graph, without prefix.
     */
    void create(String graph);

    /**
     * Load data into an existing graph.
     *
     * @param graph The name of the graph, without prefix.
     * @param input The input stream of the RDF file to be uploaded.
     */
    void uploadGraph(String graph, InputStream input);

    /**
     * Clear graph.
     *
     * @param graph The name of the graph, without prefix.
     */
    void clearGraph(String graph);

    /**
     * Execute a SPARQL Update.
     *
     * @param graph        The name of the graph.
     * @param updateString Update query string.
     */
    void update(String graph, String updateString);

    /**
     * Execute a SPARQL select query.
     *
     * @param graph       The name of the graph.
     * @param queryString Select query string.
     * @return The result of select.
     */
    SelectResult select(String graph, String queryString);

    /**
     * Execute a SPARQL select query and select a specific variable in the query.
     *
     * @param graph       The name of the graph.
     * @param queryString Select query string.
     * @param var         The specific variable to be selected.
     * @return The results of var as a  list
     */
    List<String> selectOneAsList(String graph, String queryString, String var);

}
