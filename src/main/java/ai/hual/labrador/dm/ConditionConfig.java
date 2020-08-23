package ai.hual.labrador.dm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionConfig {
    /**
     * Class name in string.
     */
    private String condition;

    /**
     * Parameters.
     */
    private List<Param> params;

    /**
     * Reverser
     */
    private boolean reverser;

    public ConditionConfig(String condition, List<Param> params, boolean reverser) {
        this.condition = condition;
        this.params = params;
        this.reverser = reverser;
    }

    public ConditionConfig(String condition, List<Param> params) {
        this.condition = condition;
        this.params = params;
    }

    public ConditionConfig() {

    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Get params as map.
     *
     * @return map
     */
    public Map<String, ContextedString> paramsAsMap() {
        return params.stream()
                .collect(Collectors.toMap(Param::getKey, p -> new ContextedString(p.getValue())));
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public boolean isReverser() {
        return reverser;
    }

    public void setReverser(boolean reverser) {
        this.reverser = reverser;
    }
}
