package ai.hual.labrador.local.remote;

import ai.hual.labrador.exceptions.DialogException;
import ai.hual.labrador.nlu.NLU;
import ai.hual.labrador.nlu.NLUResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 */
class ManageNLUClient implements NLU {

    private ManageClient client;

    ManageNLUClient(ManageClient client) {
        this.client = client;
    }

    @Override
    public NLUResult understand(String input) {
        Response<NLUResult> result = client.postJSON("/bot/{botName}/simulator/nlu", input, new TypeReference<Response<NLUResult>>() {
        });
        return result.getMsg();
    }

}
