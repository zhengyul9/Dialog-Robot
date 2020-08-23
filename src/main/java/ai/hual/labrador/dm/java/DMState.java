package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.hsm.ConfigUtils;

import java.util.List;
import java.util.stream.Collectors;

public class DMState {

    /**
     * Name of the state.
     */
    private String name;

    /**
     * Sub-states of this state, empty if this is leaf state.
     */
    private List<DMState> subStates;

    /**
     * Transitions of this state.
     */
    private List<DMTransition> transitions;

    /**
     * Executions for leaf state.
     */
    private List<Execution> executions;

    /**
     * Initial state.
     */
    private String initState;

    /**
     * This constructor recursively construct state and its subStates.
     *
     * @param stateConfig        state configuration
     * @param classLoader        class loader
     * @param accessorRepository a repository of accessors providing accessors
     */
    public DMState(DMStateConfig stateConfig, ClassLoader classLoader, AccessorRepository accessorRepository) {
        this.setName(stateConfig.getName());
        this.setInitState(stateConfig.getInitState());
        this.setTransitions(ConfigUtils.transitionsByConfig(
                stateConfig.getTransitions(), classLoader, accessorRepository));
        if (stateConfig.getSubStates() == null) {   // set executions for leaf state only
            this.setExecutions(ConfigUtils.executionsByConfig(stateConfig.getExecutions(), classLoader, accessorRepository));
            this.setSubStates(null);
        } else {
            // recursive
            List<DMState> subStates = stateConfig.getSubStates().stream()
                    .map(subStateConfig -> new DMState(subStateConfig, classLoader, accessorRepository))
                    .collect(Collectors.toList());
            this.setSubStates(subStates);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a subState by name.
     *
     * @param subStateName subState name in String
     * @return the subState
     */
    public DMState subStateByName(String subStateName) {
        return this.subStates.stream()
                .filter(state -> state.getName().equals(subStateName))
                .findAny()
                .orElse(null);
    }

    public List<DMState> getSubStates() {
        return subStates;
    }

    public void setSubStates(List<DMState> subStates) {
        this.subStates = subStates;
    }

    public List<DMTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<DMTransition> transitions) {
        this.transitions = transitions;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }

    public String getInitState() {
        return initState;
    }

    public void setInitState(String initState) {
        this.initState = initState;
    }
}
