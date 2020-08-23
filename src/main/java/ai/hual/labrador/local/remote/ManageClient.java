package ai.hual.labrador.local.remote;

import ai.hual.labrador.exceptions.DialogException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ManageClient {

    private static final Logger logger = LoggerFactory.getLogger(ManageClient.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private String manageAPIAddress;
    private String token;
    private CloseableHttpClient httpClient;

    private StrSubstitutor botNameSubstitutor;

    public ManageClient(String manageAPIAddress, String botName, String username, String password) {
        this.manageAPIAddress = manageAPIAddress;
        this.httpClient = HttpClients.createDefault();

        botNameSubstitutor = new StrSubstitutor(ImmutableMap.of("botName", botName), "{", "}");
        login(username, password);
    }

    private void login(String username, String password) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            Response<String> response = postJSON("/user/login",
                    ImmutableMap.of("name", username, "password", password),
                    new TypeReference<Response<String>>() {
                    });
            token = response.getMsg();
        }
    }

    public String getJSON(String uri, Map<String, String> params) {
        return getJSON(uri, params, new TypeReference<String>() {
        });
    }

    public <T> T getJSON(String uri, Map<String, String> params, Class<T> clazz) {
        return getJSON(uri, params, new TypeReference<T>() {
        });
    }

    public <T> T getJSON(String uri, Map<String, String> params, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(get(uri, params), typeRef);
        } catch (JsonProcessingException e) {
            throw new DialogException(String.format("Error with JSON processing %s", uri), e);
        } catch (IOException e) {
            throw new DialogException(String.format("Error calling %s", uri), e);
        }
    }

    public String get(String uri, Map<String, String> params) {
        return http(uri, params, new HttpGet());
    }

    public String postJSON(String uri, Object body) {
        return postJSON(uri, body, new TypeReference<String>() {
        });
    }

    public <T> T postJSON(String uri, Object body, Class<T> clazz) {
        return postJSON(uri, body, new TypeReference<T>() {
        });
    }

    public <T> T postJSON(String uri, Object body, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(post(uri, mapper.writeValueAsString(body), ContentType.APPLICATION_JSON), typeRef);
        } catch (JsonProcessingException e) {
            throw new DialogException(String.format("Error with JSON processing %s", uri), e);
        } catch (IOException e) {
            throw new DialogException(String.format("Error calling %s", uri), e);
        }
    }

    public String post(String uri, String body, ContentType contentType) {
        HttpPost postMethod = new HttpPost();
        postMethod.setEntity(new StringEntity(body, contentType));
        return http(uri, null, postMethod);
    }

    private String http(String uri, Map<String, String> uriParams, HttpRequestBase request) {
        uri = manageAPIAddress + botNameSubstitutor.replace(uri);
        logger.debug("Calling remote manage API {}", uri);
        try {
            URIBuilder builder = new URIBuilder(uri);
            if (uriParams != null) {
                uriParams.forEach(builder::setParameter);
            }
            request.setURI(builder.build());
            if (token != null) {
                request.setHeader("Authorization", token);
            }
        } catch (URISyntaxException e) {
            throw new DialogException(String.format("API address %s", uri), e);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new DialogException(response.getStatusLine().toString());
            }
            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DialogException("Error calling remote manage API", e);
        }
    }

}
