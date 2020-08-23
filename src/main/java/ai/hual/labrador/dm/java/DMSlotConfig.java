package ai.hual.labrador.dm.java;


import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Slot configuration of DM.
 */
public class DMSlotConfig {

    /**
     * Slot name.
     */
    private String name;
    /**
     * Update strategy is the name of Java class.
     */
    private String updateStrategy;
    /**
     * Parameters needed for slotUpdateStrategy.
     */
    private List<Param> params;

    /**
     * Data type of this slot.
     */
    private String type;

    public DMSlotConfig(String name, String updateStrategy, List<Param> params) {
        this.name = name;
        this.updateStrategy = updateStrategy;
        this.params = params;
    }

    public DMSlotConfig(String name, String type, String updateStrategy, List<Param> params) {
        this.name = name;
        this.updateStrategy = updateStrategy;
        this.params = params;
        this.type = type;
    }

    public DMSlotConfig() {
    }

    /**
     * Get parameters as map.
     *
     * @return params map
     */
    public Map<String, ContextedString> paramsAsMap() {
        return params.stream()
                .collect(Collectors.toMap(Param::getKey, p -> new ContextedString(p.getValue())));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(String updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
