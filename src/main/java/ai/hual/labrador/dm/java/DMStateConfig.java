package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.ExecutionConfig;

import java.util.List;

public class DMStateConfig {

    /**
     * Name of the state.
     */
    private String name;

    /**
     * Sub-states configuration of this state, empty if this is leaf state.
     */
    private List<DMStateConfig> subStates;

    /**
     * Transitions configuration of this state.
     */
    private List<DMTransitionConfig> transitions;

    /**
     * Executions configuration for leaf state.
     */
    private List<ExecutionConfig> executions;

    /**
     * Initial state.
     */
    private String initState;

    /**
     * Constructor for non-leaf node.
     *
     * @param name        name of this state
     * @param subStates   sub states
     * @param transitions transitions
     * @param initState   initial state
     */
    public DMStateConfig(String name, List<DMStateConfig> subStates, List<DMTransitionConfig> transitions, String initState) {
        this(name, subStates, transitions, null, initState);
    }

    /**
     * Constructor for leaf node.
     *
     * @param name        name of this state
     * @param subStates   sub states
     * @param transitions transitions
     * @param executions  executions
     * @param initState   initial state
     */
    public DMStateConfig(String name, List<DMStateConfig> subStates, List<DMTransitionConfig> transitions,
                         List<ExecutionConfig> executions, String initState) {
        this.name = name;
        this.subStates = subStates;
        this.transitions = transitions;
        this.executions = executions;
        this.initState = initState;
    }

    /**
     * Get a subState by name.
     *
     * @param subStateName subState name in String
     * @return the subState
     */
    public DMStateConfig getSubState(String subStateName) {
        return this.subStates.stream()
                .filter(state -> state.getName().equals(subStateName))
                .findAny()
                .orElse(null);
    }

    public DMStateConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DMStateConfig> getSubStates() {
        return subStates;
    }

    public void setSubStates(List<DMStateConfig> subStates) {
        this.subStates = subStates;
    }

    public List<DMTransitionConfig> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<DMTransitionConfig> transitions) {
        this.transitions = transitions;
    }

    public String getInitState() {
        return initState;
    }

    public void setInitState(String initState) {
        this.initState = initState;
    }

    public List<ExecutionConfig> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionConfig> executions) {
        this.executions = executions;
    }
}
