package ai.hual.labrador.local.remote;

/**
 * DAO for virtuoso http
 * Created by Dai Wentao on 2017/6/20.
 */
//@Repository
class VirtuosoHTTPKnowledgeGraphDAO extends HTTPKnowledgeGraphDAO {

    public static final String INFERENCE_PRAGMA = "DEFINE input:inference 'http://www.w3.org/2002/07/owl#'\n";

    VirtuosoHTTPKnowledgeGraphDAO(KnowledgeContext context) {
        super(context);
    }

    @Override
    protected String makeSelectQuery(String queryBody) {
        // return INFERENCE_PRAGMA + super.makeSelectQuery(queryBody);
        return super.makeSelectQuery(queryBody);
    }

}
