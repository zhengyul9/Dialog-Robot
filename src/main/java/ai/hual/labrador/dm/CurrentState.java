package ai.hual.labrador.dm;

import ai.hual.labrador.dm.java.DMStateConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentState {

    /**
     * Current state in this level.
     */
    private String currentState;

    /**
     * All states in the same level with state name as key.
     */
    private Map<String, CurrentState> subStates;

    public CurrentState(String currentState, Map<String, CurrentState> subStates) {
        this.currentState = currentState;
        this.subStates = subStates;
    }

    public CurrentState(CurrentState currentState) {
        this.currentState = currentState.getCurrentState();
        this.subStates = currentState.getSubStates().keySet().stream()
                .collect(Collectors.toMap(k -> k, k -> new CurrentState(currentState.getSubStates().get(k))));
    }

    public CurrentState(String currentState) {
        this.currentState = currentState;
    }

    public CurrentState() {
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Map<String, CurrentState> getSubStates() {
        return subStates;
    }

    public void setSubStates(Map<String, CurrentState> subStates) {
        this.subStates = subStates;
    }

    public void setSubStatesByDMState(List<DMStateConfig> states) {
        this.subStates = states.stream()
                .collect(Collectors.toMap(DMStateConfig::getName, state -> new CurrentState(
                        StringUtils.defaultIfEmpty(state.getInitState(), null))));
    }
}
