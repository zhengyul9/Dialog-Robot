package ai.hual.labrador.local.remote;

import ai.hual.labrador.dialog.accessors.DictAccessor;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.DictModelSerDeser;
import ai.hual.labrador.nlu.annotators.DictAnnotator;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class ManageDictAccessor implements DictAccessor {

    private ManageClient client;

    private DictModel cachedDictModel;

    ManageDictAccessor(ManageClient client) {
        this.client = client;
    }

    private DictModel fetchDictModel() {
        if (cachedDictModel == null) {
            cachedDictModel = new DictModelSerDeser().deserialize(
                    client.get("/bot/{botName}/dicts/export", null).getBytes(StandardCharsets.UTF_8));
        }
        return cachedDictModel;
    }

    @Override
    public DictModel getDictModel() {
        return fetchDictModel();
    }

    @Override
    public DictAnnotator constructDictAnnotator(boolean useNormalDict, List<String> dictTypes) {
        return new DictAnnotator(fetchDictModel(), new Properties(), useNormalDict, dictTypes);
    }

}
