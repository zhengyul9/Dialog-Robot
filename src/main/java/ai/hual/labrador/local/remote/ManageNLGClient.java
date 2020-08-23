package ai.hual.labrador.local.remote;

import ai.hual.labrador.nlg.NLG;
import ai.hual.labrador.nlg.ResponseAct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class ManageNLGClient implements NLG {

    private ManageClient client;

    ManageNLGClient(ManageClient client) {
        this.client = client;
    }

    @Override
    public String generate(ResponseAct act) {
        Response<String> result = client.postJSON("/bot/{botName}/simulator/nlg", act, new TypeReference<Response<String>>() {
        });
        return result.getMsg();
    }
}
