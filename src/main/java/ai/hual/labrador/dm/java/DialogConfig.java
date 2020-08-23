package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.CurrentState;
import ai.hual.labrador.dm.ExecutionConfig;
import ai.hual.labrador.dm.slotUpdateStrategies.ChatAnswerUpdateStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.FAQAnswerUpdateStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.InitHashMapUpdateStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.IntentUpdateStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.OverwriteStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.QueryUpdateStrategy;
import ai.hual.labrador.dm.slotUpdateStrategies.TurnUpdateStrategy;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.NLUResult;
import ai.hual.labrador.utils.LoggerHashMap;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Dialogue configurations, generated from Jason.
 */
public class DialogConfig {
    /**
     * Name for system maintained slot of nluResult.
     */
    public static final String SYSTEM_NLU_RESULT_NAME = "sys.nluResult";
    public static final String SYSTEM_NLU_RESULT_STRATEGY = OverwriteStrategy.class.getName();
    public static final String SYSTEM_NLU_RESULT_TYPE = NLUResult.class.getName();

    /**
     * Name for system maintained slot of turnParams.
     */
    public static final String SYSTEM_TURN_PARAMS_NAME = "sys.turnParams";
    public static final String SYSTEM_TURN_PARAMS_STRATEGY = OverwriteStrategy.class.getName();

    /**
     * Name for system maintained slot of faq.
     */
    public static final String SYSTEM_FAQ_NAME = "sys.faqAnswer";
    public static final String SYSTEM_FAQ_STRATEGY = FAQAnswerUpdateStrategy.class.getName();
    public static final String SYSTEM_FAQ_TYPE = FaqAnswer.class.getName();

    /**
     * Name for system maintained slot of faq.
     */
    public static final String SYSTEM_CHAT_NAME = SYSTEM_FAQ_NAME;
    public static final String SYSTEM_CHAT_STRATEGY = ChatAnswerUpdateStrategy.class.getName();
    public static final String SYSTEM_CHAT_TYPE = FaqAnswer.class.getName();

    /**
     * Name for system maintained slot of intent.
     */
    public static final String SYSTEM_INTENT_NAME = "sys.intent";
    public static final String SYSTEM_INTENT_STRATEGY = IntentUpdateStrategy.class.getName();


    /**
     * Name for system maintained slot of query.
     */
    public static final String SYSTEM_QUERY_NAME = "sys.query";
    private static final String SYSTEM_QUERY_STRATEGY = QueryUpdateStrategy.class.getName();


    /**
     * Name for system maintained slot of turn.
     */
    public static final String SYSTEM_TURN_NAME = "sys.turn";
    private static final String SYSTEM_TURN_STRATEGY = TurnUpdateStrategy.class.getName();
    private static final String SYSTEM_TURN_TYPE = Integer.class.getName();

    /**
     * Name for system maintained slot of obtained queryAct hyps.
     */
    public static final String SYSTEM_HYPS_NAME = "sys.hyps";
    private static final String SYSTEM_HYPS_STRATEGY = OverwriteStrategy.class.getName();

    /**
     * Name for system maintained slot of all slots update turn.
     */
    public static final String SYSTEM_TURNS_MAINTAIN_NAME = "sys.turns";
    private static final String SYSTEM_TURNS_MAINTAIN_STRATEGY = InitHashMapUpdateStrategy.class.getName();
    private static final String SYSTEM_TURNS_MAINTAIN_TYPE = HashMap.class.getName();

    /**
     * Name for system maintained slot to count repeatedness time of a leaf state.
     */
    public static final String SYSTEM_STATE_REPEATEDNESS_NAME = "sys.repeatedness";
    private static final String SYSTEM_STATE_REPEATEDNESS_STRATEGY = InitHashMapUpdateStrategy.class.getName();
    private static final String SYSTEM_STATE_REPEATEDNESS_TYPE = HashMap.class.getName();

    /**
     * Name for system maintained slot of last response.
     */
    public static final String SYSTEM_RESPONSE_NAME = "sys.lastResponse";
    private static final String SYSTEM_RESPONSE_STRATEGY = OverwriteStrategy.class.getName();
    private static final String SYSTEM_RESPONSE_TYPE = ResponseAct.class.getName();

    /**
     * Name for system initialized slot of bot configuration
     */
    public static final String SYSTEM_BOT_CONFIGURATION_NAME = "sys.botConfiguration";
    public static final String BOT_CONFIGURATION_BOT_NAME_KEY = "botName";
    public static final String BOT_CONFIGURATION_MODEL_ID_KEY = "modelId";

    public static final ImmutableList<DMSlotConfig> SYSTEM_SLOTS_CONFIG = ImmutableList.of(
            new DMSlotConfig(SYSTEM_NLU_RESULT_NAME, SYSTEM_NLU_RESULT_TYPE, SYSTEM_NLU_RESULT_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_TURN_PARAMS_NAME, SYSTEM_NLU_RESULT_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_TURNS_MAINTAIN_NAME, SYSTEM_TURNS_MAINTAIN_TYPE, SYSTEM_TURNS_MAINTAIN_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_TURN_NAME, SYSTEM_TURN_TYPE, SYSTEM_TURN_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_HYPS_NAME, SYSTEM_HYPS_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_QUERY_NAME, SYSTEM_QUERY_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_INTENT_NAME, SYSTEM_INTENT_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_RESPONSE_NAME, SYSTEM_RESPONSE_TYPE, SYSTEM_RESPONSE_STRATEGY, Collections.emptyList()),
            new DMSlotConfig(SYSTEM_STATE_REPEATEDNESS_NAME, SYSTEM_STATE_REPEATEDNESS_TYPE, SYSTEM_STATE_REPEATEDNESS_STRATEGY, Collections.emptyList()));

    /**
     * Slots configuration of DM.
     */
    private List<DMSlotConfig> slots;

    /**
     * States configuration of DM.
     */
    private List<DMStateConfig> states;

    /**
     * Initial state.
     */
    private String initState;

    /**
     * Execution class for dealing with unknown intent.
     */
    private ExecutionConfig unknownExecution;

    /**
     * FAQ execution, use default if absent.
     */
    private ExecutionConfig faqExecution;


    public DialogConfig(List<DMSlotConfig> slots, List<DMStateConfig> states, String initState,
                        ExecutionConfig unknownExecution, ExecutionConfig faqExecution) {
        this.slots = slots;
        this.states = states;
        this.initState = initState;
        this.unknownExecution = unknownExecution;
        this.faqExecution = faqExecution;
    }

    public DialogConfig() {
    }

    /**
     * Add system maintained slot config.
     */
    void addSystemSlots() {
        slots.addAll(0, SYSTEM_SLOTS_CONFIG);
    }

    /**
     * Construct CurrentState from dialogConfig.
     *
     * @return currentState
     */
    Context generateContext(Properties botConfiguration) {
        Context context = new Context();

        // set states
        CurrentState currentState = this.generateCurrentState();
        context.setCurrentState(currentState);
        // set slots
        Map<String, Object> slotsMap = new LoggerHashMap<>();
        for (DMSlotConfig slot : this.getSlots())
            slotsMap.put(slot.getName(), null);
        context.setSlots(slotsMap);
        // set slots type
        context.setTypes(slots.stream()
                .filter(s -> s.getType() != null)
                .collect(HashMap::new, (m, s) -> m.put(s.getName(), s.getType()), HashMap::putAll));
        generateSystemInitialSlots(context.getSlots(), botConfiguration);
        return context;
    }

    private void generateSystemInitialSlots(Map<String, Object> slots, Properties botConfiguration) {
        slots.put(SYSTEM_BOT_CONFIGURATION_NAME, botConfiguration);
    }

    /**
     * Construct CurrentState from dialogConfig.
     *
     * @return currentState
     */
    CurrentState generateCurrentState() {
        // initialize the outer most level of states
        String initState = this.getInitState();
        CurrentState currentState = new CurrentState(initState);
        List<DMStateConfig> states = this.getStates();
        currentState.setSubStatesByDMState(states);
        // recursively construct currentState
        for (DMStateConfig state : states) {
            String stateName = state.getName();
            recursiveCreateCurrentState(currentState.getSubStates().get(stateName), state.getSubStates());
        }

        return currentState;
    }

    /**
     * Recursively construct CurrentState.
     *
     * @param currentState currentState whose subStates need to be constructed
     * @param states       states who are at the same level as currentState
     */
    private static void recursiveCreateCurrentState(CurrentState currentState, List<DMStateConfig> states) {
        if (states == null || states.size() == 0) {
            currentState.setSubStates(new HashMap<>());
            return;
        }
        currentState.setSubStatesByDMState(states);
        for (DMStateConfig state : states) {
            String stateName = state.getName();
            recursiveCreateCurrentState(currentState.getSubStates().get(stateName), state.getSubStates());
        }
    }

    /**
     * Get outer most level states by name. The only usage is in initialization phase.
     *
     * @param stateName state name in String
     * @return the state
     */
    public DMStateConfig getState(String stateName) {
        return this.states.stream()
                .filter(state -> state.getName().equals(stateName))
                .findAny()
                .orElse(null);
    }

    public List<DMSlotConfig> getSlots() {
        return slots;
    }

    /**
     * Get slot by name.
     *
     * @param slotName state name in String
     * @return the state
     */
    public DMSlotConfig getSlot(String slotName) {
        return this.slots.stream()
                .filter(slot -> slot.getName().equals(slotName))
                .findAny()
                .orElse(null);
    }

    public void setSlots(List<DMSlotConfig> slots) {
        this.slots = slots;
    }

    public List<DMStateConfig> getStates() {
        return states;
    }

    public void setStates(List<DMStateConfig> states) {
        this.states = states;
    }

    public String getInitState() {
        return initState;
    }

    public void setInitState(String initState) {
        this.initState = initState;
    }

    public ExecutionConfig getUnknownExecution() {
        return unknownExecution;
    }

    public void setUnknownExecution(ExecutionConfig unknownExecution) {
        this.unknownExecution = unknownExecution;
    }

    public ExecutionConfig getFaqExecution() {
        return faqExecution;
    }

    public void setFaqExecution(ExecutionConfig faqExecution) {
        this.faqExecution = faqExecution;
    }
}
