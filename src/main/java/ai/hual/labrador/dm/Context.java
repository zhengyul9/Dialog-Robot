package ai.hual.labrador.dm;

import ai.hual.labrador.dm.java.DMSlot;
import ai.hual.labrador.dm.java.DMSlotConfig;
import ai.hual.labrador.dm.java.DialogImpl;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.utils.LoggerHashMap;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_HYPS_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_SLOTS_CONFIG;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURNS_MAINTAIN_NAME;

@JsonSerialize(using = ContextJsonSerializer.class)
@JsonDeserialize(using = ContextJsonDeserializer.class)
public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    /**
     * Slots in concern.
     */
    private Map<String, Object> slots;

    /**
     * Slots type in java class string.
     */
    private Map<String, String> types;

    /**
     * Current state.
     */
    private CurrentState currentState;

    public Context() {
        slots = new LoggerHashMap<>();
        types = new HashMap<>();
        currentState = new CurrentState();
    }

    public Context(Context context) {
        this.setSlots(new LoggerHashMap<>(context.getSlots()));
        this.setTypes(new HashMap<>(context.getTypes()));
        this.setCurrentState(new CurrentState(context.getCurrentState()));
    }

    /**
     * Set the object of slot by name.
     *
     * @param slotName slot name
     * @param object   the content of slot
     */
    public void putSlotContent(String slotName, Object object) {
        LoggerHashMap<String, Object> loggerMap = (LoggerHashMap<String, Object>) slots;
        loggerMap.putWithLog(slotName, object);
    }

    /**
     * Get the object in slot by name.
     *
     * @param slotName slot name
     * @return object in slot
     */
    public Object slotContentByName(String slotName) {
        return slots.get(slotName);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String slotName) {
        return (T) slots.get(slotName);
    }

    /**
     * Get type of slot by name.
     *
     * @param slotName slot name
     * @return type of slot
     */
    public String slotTypeByName(String slotName) {
        return types.get(slotName);
    }

    /**
     * Update slots according to given DA possibilities.
     *
     * @param input  Original input
     * @param act    queryAct  from NLU
     * @param dialog DialogImpl object contain updateStrategy implementations
     */
    @SuppressWarnings("unchecked")
    public void updateSlots(String input, QueryAct act, DialogImpl dialog) {
        // apply updateStrategy for all slots in context
        for (String slotName : dialog.getSlots().keySet()) { // loop over slots name
            // avoid updating FAQ answer for each hyp, cause it's updated by calling API for now
            if (slotName.equals(SYSTEM_HYPS_NAME))
                continue;
            Map<String, Integer> turnsMap = (Map<String, Integer>) this.slotContentByName(SYSTEM_TURNS_MAINTAIN_NAME);
            if (turnsMap != null && turnsMap.get(slotName) != null)
                logger.debug("Updating slot {}, last updated turn is {}", slotName, turnsMap.get(slotName));
            else
                logger.debug("Updating slot {}", slotName);

            DMSlot dmSlot = dialog.getSlot(slotName);
            if (dmSlot == null || dmSlot.getUpdateStrategy() == null) {
                logger.debug("Skip slot {}, null update strategy", slotName);
                continue;
            }
            // update slot in context
            Object updatedObject = dmSlot.getUpdateStrategy().update(act, slots.get(slotName), this);
            slots.put(slotName, updatedObject);
            if (turnsMap != null && turnsMap.get(slotName) != null)
                logger.debug("Update slot {} with value {} and new turn {}",
                        slotName, updatedObject, turnsMap.get(slotName));
            else
                logger.debug("Update slot {} with value {}", slotName, updatedObject);
        }
    }

    @SuppressWarnings("unchecked")
    public void updateSystemSlots(QueryAct act, DialogImpl dialog) {
        Set<String> systemSlots = SYSTEM_SLOTS_CONFIG.stream().map(DMSlotConfig::getName).collect(Collectors.toSet());
        for (String slotName : dialog.getSlots().keySet()) { // loop over slots name
            // avoid updating FAQ answer for each hyp
            if (!systemSlots.contains(slotName) || slotName.equals(SYSTEM_HYPS_NAME))
                continue;
            Map<String, Integer> turnsMap = (Map<String, Integer>) this.slotContentByName(SYSTEM_TURNS_MAINTAIN_NAME);
            if (turnsMap != null && turnsMap.get(slotName) != null)
                logger.debug("Updating slot {}, last updated turn is {}", slotName, turnsMap.get(slotName));
            else
                logger.debug("Updating slot {}", slotName);

            DMSlot dmSlot = dialog.getSlot(slotName);
            if (dmSlot == null || dmSlot.getUpdateStrategy() == null) {
                logger.debug("Skip slot {}, null update strategy", slotName);
                continue;
            }
            // update slot in context
            Object updatedObject = dmSlot.getUpdateStrategy().update(act, slots.get(slotName), this);
            slots.put(slotName, updatedObject);
            if (turnsMap != null && turnsMap.get(slotName) != null)
                logger.debug("Update slot {} with value {} and new turn {}",
                        slotName, updatedObject, turnsMap.get(slotName));
            else
                logger.debug("Update slot {} with value {}", slotName, updatedObject);
        }
    }

    /**
     * Get current leaf state.
     *
     * @return leaf state name
     */
    public String currentLeafState() {
        return currentLeafStateRecurs(currentState.getCurrentState(), currentState.getSubStates());
    }

    /**
     * Recursively go deeper and get current leaf state.
     *
     * @param currentState current state
     * @param subStates    same level states
     * @return current leaf state
     */
    private String currentLeafStateRecurs(String currentState, Map<String, CurrentState> subStates) {
        // return current state name if is a leaf state
        CurrentState state = subStates.get(currentState);
        if (state.getCurrentState() == null)
            return currentState;
        return currentLeafStateRecurs(state.getCurrentState(), state.getSubStates());
    }

    /**
     * Tell if current state inside a specific state.
     *
     * @param stateName specific state name
     * @return true if currently resides in the state
     */
    public boolean insideState(String stateName) {
        CurrentState currentStateCopy = new CurrentState(currentState);
        while (currentStateCopy.getSubStates() != null && !currentStateCopy.getSubStates().isEmpty()) {
            if (currentStateCopy.getCurrentState().equals(stateName))
                return true;
            currentStateCopy = currentStateCopy.getSubStates().get(currentStateCopy.getCurrentState());
        }
        return false;
    }

    public Map<String, Object> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, Object> slots) {
        this.slots = slots;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CurrentState currentState) {
        this.currentState = currentState;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }
}
