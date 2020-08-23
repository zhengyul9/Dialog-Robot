package ai.hual.labrador.local.remote;

import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.SelectResult;
import ai.hual.labrador.kg.utils.KnowledgeUtils;

import java.util.List;

/**
 * A remote kg accessor access knowledge graph through KnowledgeGraphDAO
 * Created by Dai Wentao on 2017/7/3.
 */
class RemoteKnowledgeAccessor implements KnowledgeAccessor {

    protected KnowledgeGraphDAO kgDAO;
    protected String graph;

    RemoteKnowledgeAccessor(KnowledgeGraphDAO kgDAO, String graph) {
        this.kgDAO = kgDAO;
        this.graph = graph;
    }

    @Override
    public SelectResult select(String selectString) {
        return kgDAO.select(graph, selectString);
    }

    @Override
    public List<String> selectOneAsList(String selectString, String var) {
        return kgDAO.selectOneAsList(graph, selectString, var);
    }

    @Override
    public <T> List<T> selectObjectAsList(String selectString, Class<T> clazz) {
        SelectResult selectResult = select(selectString);
        return KnowledgeUtils.convertToObjects(selectResult, clazz);
    }
}
