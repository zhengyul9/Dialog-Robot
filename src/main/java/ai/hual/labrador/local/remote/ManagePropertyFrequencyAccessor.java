package ai.hual.labrador.local.remote;

import ai.hual.labrador.dialog.accessors.PropertyFrequencyAccessor;
import ai.hual.labrador.exceptions.DialogException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ManagePropertyFrequencyAccessor implements PropertyFrequencyAccessor {

    private ManageClient client;

    ManagePropertyFrequencyAccessor(ManageClient client) {
        this.client = client;
    }

    @Override
    public List<String> simQuestions(String entity, String beginDate, String endDate) {
        Response<List<String>> result = client.postJSON("/statistics/{botName}/hotProperty",
                ImmutableMap.of("entity", entity, "beginDate", beginDate, "endDate", endDate),
                new TypeReference<Response<List<String>>>() {
                });
        return result.getMsg();
    }
}
