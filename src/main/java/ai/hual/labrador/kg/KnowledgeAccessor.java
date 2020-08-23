package ai.hual.labrador.kg;


import java.util.List;

/**
 * An accessor that helps dialog system to access KG with only SPARQL query string.
 * Created by Dai Wentao on 2017/6/1.
 * Updated by Dai Wentao on 2017/7/3. Modified as interface.
 */
public interface KnowledgeAccessor {

    /**
     * Execute a select query.
     *
     * @param selectString The select query string.
     * @return select result.
     */
    public SelectResult select(String selectString);

    /**
     * Execute a select query and get a single var from the result.
     *
     * @param selectString The select query string.
     * @param var          The var to be selected.
     * @return A list of var values.
     */
    public List<String> selectOneAsList(String selectString, String var);

    /**
     * Execute a select query and convert to list of object.
     * <Strong>NOTICE</Strong>: clazz should have a constructor who
     * accept only strings and correspond to query's vars order.
     *
     * @param selectString The select query string.
     * @param clazz        The class to be concerted.
     * @return A list of objects of clazz.
     */
    public <T> List<T> selectObjectAsList(String selectString, Class<T> clazz);
}
