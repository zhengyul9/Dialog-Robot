package ai.hual.labrador.local.remote;

/**
 * Context for knowledge graph.
 * Maintains prefixes and schema about labrador.
 * Created by Dai Wentao on 2017/5/10.
 */
class KnowledgeContext {


    private String graphPrefix;

    private String queryService;
    private String updateService;
    private String graphService;

    private KnowledgeAuth knowledgeAuth;

    KnowledgeContext(String graphPrefix, String queryService, String updateService, String graphService,
                     KnowledgeAuth knowledgeAuth) {
        this.graphPrefix = graphPrefix;

        this.queryService = queryService;
        this.updateService = updateService;
        this.graphService = graphService;

        this.knowledgeAuth = knowledgeAuth;
    }

    public String getGraphPrefix() {
        return graphPrefix;
    }

    public String getQueryService() {
        return queryService;
    }

    public String getUpdateService() {
        return updateService;
    }

    public String getGraphService() {
        return graphService;
    }

    public KnowledgeAuth getKnowledgeAuth() {
        return knowledgeAuth;
    }

}
