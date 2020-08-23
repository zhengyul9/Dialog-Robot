package ai.hual.labrador.local.remote;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMResult;
import ai.hual.labrador.dm.java.DialogConfig;
import ai.hual.labrador.dm.java.HSMDM;
import ai.hual.labrador.nlu.NLUResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Properties;

class ManageDMClient implements DM {

    private DM dm;

    ManageDMClient(ClassLoader classLoader, ManageClient client, AccessorRepository accessorRepository, Properties properties) {
        Response<DialogConfig> result = client.getJSON("/bot/{botName}/dm/hsm/config", null, new TypeReference<Response<DialogConfig>>() {
        });
        dm = new HSMDM(result.getMsg(), classLoader, accessorRepository, properties);
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, String strState) {
        return dm.process(input, nluResult, strState);
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, String turnParams, String strState) {
        return dm.process(input, nluResult, turnParams, strState);
    }

    @Override
    public void close() {

    }

}
