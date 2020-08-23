package ai.hual.labrador.local.remote;

import ai.hual.labrador.exceptions.KnowledgeException;
import ai.hual.labrador.kg.KGConst;
import ai.hual.labrador.kg.KnowledgeUtil;
import ai.hual.labrador.kg.SelectResult;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A Jena implementation of HTTP based KnowledgeGraphDAO.
 * See more about SPARQL query over HTTP: https://www.w3.org/TR/sparql11-protocol/
 * Created by Dai Wentao on 2017/5/8.
 */
class HTTPKnowledgeGraphDAO implements KnowledgeGraphDAO {

    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    public static final String PREFIXES = String.join("\n",
            String.format("PREFIX %s:<%s>", KGConst.RDF_PREFIX, KGConst.RDF_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.RDFS_PREFIX, KGConst.RDFS_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.OWL_PREFIX, KGConst.OWL_EXPAND),
            String.format("PREFIX %s:<%s>", KGConst.LBRD_PREFIX, KGConst.LABRADOR_PREFIX),
            String.format("PREFIX %s:<%s>", KGConst.LBRDS_PREFIX, KGConst.LABRADOR_SCHEMA_PREFIX));

    private static final Logger logger = LoggerFactory.getLogger(HTTPKnowledgeGraphDAO.class);

    private final KnowledgeContext context;
    private final CredentialsProvider credsProvider = new BasicCredentialsProvider();
    private final KnowledgeUtil kgUtil = new KnowledgeUtil();

    HTTPKnowledgeGraphDAO(KnowledgeContext context) {
        this.context = context;

        // set default http client with authorization.
        KnowledgeAuth auth = context.getKnowledgeAuth();
        if (auth.isValid()) {
            credsProvider.setCredentials(
                    new AuthScope(auth.getHost(), auth.getPort(), auth.getRealm(), auth.getScheme()),
                    new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword()));
        }
    }

    @Override
    public void create(String graph) {
        String g = context.getGraphPrefix() + graph;
        updateQuery(null, String.format("CREATE GRAPH <%s>", g));
    }

    @Override
    public void uploadGraph(String graph, InputStream input) {
        assert (graph != null);
        try {
            // make graph crud url
            URIBuilder url = new URIBuilder(context.getGraphService());
            url.setParameter("graph", context.getGraphPrefix() + graph);

            // post data to the crud url
            post(url.build(), input, "text/turtle");
        } catch (URISyntaxException | IOException e) {
            logger.warn("Fail executing upload.", e);
            throw new KnowledgeException(e);
        }
    }

    @Override
    public void clearGraph(String graph) {
        String g = context.getGraphPrefix() + graph;
        updateQuery(null, String.format("CLEAR GRAPH <%s>", g));
    }

    @Override
    public void update(String graph, String updateString) {
        String g = context.getGraphPrefix() + graph;
        updateQuery(g, updateString);
    }

    @Override
    public SelectResult select(String graph, String queryString) {
        String g = graph == null ? null : (context.getGraphPrefix() + graph);
        return kgUtil.parseJSONSelectResult(selectQuery(g, queryString));
    }

    @Override
    public List<String> selectOneAsList(String graph, String queryString, String var) {
        String g = graph == null ? null : (context.getGraphPrefix() + graph);
        return kgUtil.parseJSONSelectVarResult(selectQuery(g, queryString), var);
    }

    protected String makeSelectQuery(String queryBody) {
        return PREFIXES + queryBody;
    }

    private String selectQuery(String graph, String queryString, String... args) {
        try {
            URIBuilder url = new URIBuilder(context.getQueryService());
            url.setParameter("format", "application/sparql-results+json");

            List<NameValuePair> params = new ArrayList<>();
            String query = makeSelectQuery(String.format(queryString, (Object[]) args));
            params.add(new BasicNameValuePair("query", query));
            if (graph != null) {
                params.add(new BasicNameValuePair("default-graph-uri", graph));
            }

            return post(url.build(), URLEncodedUtils.format(params, StandardCharsets.UTF_8),
                    "application/x-www-form-urlencoded");
        } catch (URISyntaxException | IOException e) {
            logger.warn("Fail executing query.", e);
            throw new KnowledgeException(e);
        }
    }

    private void updateQuery(String graph, String... updateStrings) {
        try {
            URIBuilder url = new URIBuilder(context.getUpdateService());
            if (graph != null) {
                url.setParameter("using-graph-uri", graph);
            }
            String update = PREFIXES + Joiner.on("\n").join(updateStrings);
            post(url.build(), update, "application/sparql-update");
        } catch (URISyntaxException | IOException e) {
            logger.warn("Fail executing update.", e);
            throw new KnowledgeException(e);
        }
    }

    private String post(URI url, InputStream content, String contentType) throws IOException {
        return post(url, new BufferedHttpEntity(new InputStreamEntity(content)), contentType);
    }

    private String post(URI url, String content, String contentType) throws IOException {
        return post(url, new StringEntity(content, StandardCharsets.UTF_8), contentType);
    }

    private String post(URI url, HttpEntity content, String contentType) throws IOException {
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try {
            HttpPost post = new HttpPost(url);
            post.addHeader(CONTENT_TYPE_HEADER, contentType);
            post.setEntity(content);
            HttpResponse response = client.execute(post);

            // validate status
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (HttpSC.isClientError(statusCode) || HttpSC.isServerError(statusCode)) {
                HttpEntity entity = response.getEntity();
                String contentPayload = entity == null ? null : EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
                throw new KnowledgeException(statusCode + statusLine.getReasonPhrase() + contentPayload);
            }

            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }

}