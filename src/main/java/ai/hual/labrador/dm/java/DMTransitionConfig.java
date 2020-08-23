package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.ConditionConfig;
import ai.hual.labrador.dm.ExecutionConfig;

import java.util.List;

/**
 * Transition rule from one state to another.
 */
public class DMTransitionConfig {

    /**
     * Destination state name.
     */
    private String to;

    /**
     * Conditions configuration.
     */
    private List<ConditionConfig> conditions;

    /**
     * Executions configuration.
     */
    private List<ExecutionConfig> executions;

    public DMTransitionConfig() {
    }

    public DMTransitionConfig(String to, List<ConditionConfig> conditions, List<ExecutionConfig> executions) {
        this.to = to;
        this.conditions = conditions;
        this.executions = executions;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<ConditionConfig> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionConfig> conditions) {
        this.conditions = conditions;
    }

    public List<ExecutionConfig> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionConfig> executions) {
        this.executions = executions;
    }
}
