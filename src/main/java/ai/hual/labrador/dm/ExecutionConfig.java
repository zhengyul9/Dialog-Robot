package ai.hual.labrador.dm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutionConfig {
    /**
     * Class name in string.
     */
    private String execution;

    /**
     * Parameters.
     */
    private List<Param> params;

    public ExecutionConfig(String execution, List<Param> params) {
        this.execution = execution;
        this.params = params;
    }

    public ExecutionConfig() {
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
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

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}
