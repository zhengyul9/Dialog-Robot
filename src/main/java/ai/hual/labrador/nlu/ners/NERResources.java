package ai.hual.labrador.nlu.ners;

import java.util.Map;

public class NERResources {
    private Map<String,Map<String,NERResource>> resources;
    public NERResources(Map<String,Map<String,NERResource>> resources)
    {
        this.resources = resources;
    }

    public Map<String, Map<String, NERResource>> getResources() {
        return resources;
    }

    public void setResources(Map<String, Map<String, NERResource>> resources) {
        this.resources = resources;
    }
}
