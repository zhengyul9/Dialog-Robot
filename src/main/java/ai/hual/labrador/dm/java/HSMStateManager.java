package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.CurrentState;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.exceptions.DMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_STATE_REPEATEDNESS_NAME;

public class HSMStateManager {

    private static final Logger logger = LoggerFactory.getLogger(HSMStateManager.class);

    private final DialogImpl dialog;

    public HSMStateManager(DialogImpl dialog) {
        this.dialog = dialog;
    }

    /**
     * Recursively step into next state until encounter with a leaf state.
     *
     * @param context      context
     * @param currentState current state
     * @return score of transition
     */
    @SuppressWarnings("unchecked")
    public TransitionResult step(Context context, DMState state, List<DMState> sameLevelStates, CurrentState currentState,
                                 int level, Set<String> jumpedStates, Float score) {

        assert state != null;

        String lastStateName = state.getName();
        Map<String, CurrentState> currentSubStates = currentState.getSubStates();
        List<DMState> subStates = state.getSubStates();
        jumpedStates.add(state.getName());
        for (DMTransition transition : state.getTransitions()) {
            logger.debug("Processing transition from {} to {}", lastStateName, transition.getTo());
            if (jumpedStates.contains(transition.getTo())) { // should not jump to an jumped state
                logger.debug("{} already transited to, ignoring", transition.getTo());
                continue;
            }
            // jump to a same level state if all conditions of this transition are met
            if (transition.acceptAllConditions(context)) {
                // execute
                if (transition.getExecutions() != null) {
                    for (Execution execution : transition.getExecutions())
                        // TODO: ignore return value for now
                        execution.execute(context);
                }
                // reset repeatedness count
                HashMap<String, Integer> repeatednessMap =
                        (HashMap<String, Integer>) context.slotContentByName(SYSTEM_STATE_REPEATEDNESS_NAME);
                if (repeatednessMap.containsKey(state.getName()))
                    repeatednessMap.put(state.getName(), 0);

                String nextStateName = transition.getTo();
                state = sameLevelStates.stream()
                        .filter(s -> s.getName().equals(nextStateName))
                        .findFirst()
                        .orElse(null);
                if (jumpedStates.size() == 1) // only discount once for each level, initialized with size 1
                    score *= level * 1f / HSMDM.MAX_DEPTH * 1f;
                jumpedStates.add(transition.getTo());
                currentState.setCurrentState(nextStateName);
                // set pack
                logger.debug("{} to same level {}", lastStateName, nextStateName);
                return step(context, state, sameLevelStates, currentState, level, jumpedStates, score);
            }
        }
        // same level transition can not be realized, go to next level
        if (subStates != null) { // might not be a leaf state, step further
            if (subStates.size() == 0) {  // leaf
                if (state.getExecutions() == null || state.getExecutions().isEmpty())
                    throw new DMException("请为非叶子状态\"" + state.getName() + "\"设置有执行的子状态");
                return new TransitionResult(state, score);
            }
            currentState = currentSubStates.get(lastStateName);
            sameLevelStates = state.getSubStates();
            if (currentState.getCurrentState() == null)
                throw new DMException("请为\"" + lastStateName + "\"状态的子状态定义初始状态");
            state = state.subStateByName(currentState.getCurrentState());
            // set pack
            logger.debug("{} to deeper level {}", lastStateName, state.getName());
            // step from new state
            return step(context, state, sameLevelStates, currentState, level + 1, new HashSet<>(), score);
        }
        return new TransitionResult(state, score);  // leaf state
    }
}
