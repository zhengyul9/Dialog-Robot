package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.executions.DefaultFAQExecution;
import ai.hual.labrador.dm.executions.DefaultUnknownExecution;
import ai.hual.labrador.dm.hsm.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DialogImpl {

    static final String USE_DEFAULT_FAQ_EXECUTION_WHEN_ABSENT_PROP_NAME = "dm.useDefaultFaqExecutionWhenAbsent";
    static final String USE_DEFAULT_UNKNOWN_EXECUTION_WHEN_ABSENT_PROP_NAME = "dm.useDefaultUnknownExecutionWhenAbsent";

    private static final Logger logger = LoggerFactory.getLogger(DialogImpl.class);

    /**
     * Slots of DM.
     */
    private Map<String, DMSlot> slots = new LinkedHashMap<>();

    /**
     * States of DM.
     */
    private List<DMState> states = new ArrayList<>();

    /**
     * Initial state.
     */
    private String initState;

    /**
     * Execution for dealing with unknown intent.
     */
    private Execution unknownExecution;

    /**
     * Execution for FAQ.
     */
    private Execution faqExecution;

    /**
     * Construct DialogImpl from DialogConfig.
     *
     * @param dialogConfig       dialogConfig
     * @param classLoader        classLoader containing user customed implementation of
     *                           {@link ai.hual.labrador.dm.SlotUpdateStrategy},
     *                           {@link ai.hual.labrador.dm.Condition} and
     *                           {@link ai.hual.labrador.dm.Execution}
     * @param accessorRepository a repository of accessors providing accessors
     */
    public DialogImpl(DialogConfig dialogConfig, ClassLoader classLoader, AccessorRepository accessorRepository, Properties properties) {
        for (DMSlotConfig config : dialogConfig.getSlots()) {
            DMSlot slot = new DMSlot(config, classLoader, accessorRepository);
            slots.put(slot.getName(), slot);
        }
        for (DMStateConfig config : dialogConfig.getStates()) {
            states.add(new DMState(config, classLoader, accessorRepository));
        }
        initState = dialogConfig.getInitState();
        // unknownExecution
        boolean useDefaultUnknownExecutionWhenAbsent = Boolean.parseBoolean(properties.getProperty(
                USE_DEFAULT_UNKNOWN_EXECUTION_WHEN_ABSENT_PROP_NAME, "true"));
        if (dialogConfig.getUnknownExecution() != null) {
            unknownExecution = ConfigUtils.executionByConfig(
                    dialogConfig.getUnknownExecution(), classLoader, accessorRepository);
            logger.debug("使用自定义无意图执行:{}", unknownExecution.getClass().getName());

        } else if (useDefaultUnknownExecutionWhenAbsent) {
            unknownExecution = new DefaultUnknownExecution();
            logger.debug("使用默认无意图执行:{}", unknownExecution.getClass().getName());
        } else
            logger.debug("未指定自定义无意图执行，也不允许使用默认执行，无意图时将不做特殊处理");
        // faqExecution
        boolean useDefaultFaqExecutionWhenAbsent = Boolean.parseBoolean(properties.getProperty(
                USE_DEFAULT_FAQ_EXECUTION_WHEN_ABSENT_PROP_NAME, "true"));
        if (dialogConfig.getFaqExecution() != null) {
            faqExecution = ConfigUtils.executionByConfig(
                    dialogConfig.getFaqExecution(), classLoader, accessorRepository);
            logger.debug("使用自定义FAQ执行:{}", faqExecution.getClass().getName());
        } else if (useDefaultFaqExecutionWhenAbsent) {
            faqExecution = new DefaultFAQExecution();
            logger.debug("使用默认FAQ执行:{}", faqExecution.getClass().getName());
        } else
            logger.debug("未指定自定义FAQ执行，也不允许使用默认执行，即使FAQ答案更优时也会按意图结果走DM流程");
    }

    public DialogImpl(Map<String, DMSlot> slots, List<DMState> states, String initState, Execution unknownExecution,
                      Execution faqExecution) {
        this.slots = slots;
        this.states = states;
        this.initState = initState;
        this.faqExecution = faqExecution;
        this.unknownExecution = unknownExecution;
    }

    /**
     * Construct DialogImpl from DialogConfig.
     *
     * @param dialogConfig       dialogConfig
     * @param classLoader        classLoader containing user customed implementation of
     *                           {@link ai.hual.labrador.dm.SlotUpdateStrategy},
     *                           {@link ai.hual.labrador.dm.Condition} and
     *                           {@link ai.hual.labrador.dm.Execution}
     * @param accessorRepository a repository of accessors providing accessors
     */
    public DialogImpl(DialogConfig dialogConfig, ClassLoader classLoader, AccessorRepository accessorRepository) {
        for (DMSlotConfig config : dialogConfig.getSlots()) {
            DMSlot slot = new DMSlot(config, classLoader, accessorRepository);
            slots.put(slot.getName(), slot);
        }
        for (DMStateConfig config : dialogConfig.getStates()) {
            states.add(new DMState(config, classLoader, accessorRepository));
        }
        initState = dialogConfig.getInitState();
        if (dialogConfig.getUnknownExecution() != null) {
            unknownExecution = ConfigUtils.executionByConfig(
                    dialogConfig.getUnknownExecution(), classLoader, accessorRepository);
        }
        if (dialogConfig.getFaqExecution() != null) {
            faqExecution = ConfigUtils.executionByConfig(
                    dialogConfig.getFaqExecution(), classLoader, accessorRepository);
        }
    }

    /**
     * Get slot by name.
     *
     * @param slotName state name in String
     * @return the state
     */
    public DMSlot getSlot(String slotName) {
        return slots.get(slotName);
    }

    /**
     * Get outer most level states by name. The only usage is in initialization phase.
     *
     * @param stateName state name in String
     * @return the state
     */
    public DMState getState(String stateName) {
        return this.states.stream()
                .filter(state -> state.getName().equals(stateName))
                .findAny()
                .orElse(null);
    }


    public Map<String, DMSlot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, DMSlot> slots) {
        this.slots = slots;
    }

    public List<DMState> getStates() {
        return states;
    }

    public void setStates(List<DMState> states) {
        this.states = states;
    }

    public String getInitState() {
        return initState;
    }

    public void setInitState(String initState) {
        this.initState = initState;
    }

    public Execution getUnknownExecution() {
        return unknownExecution;
    }

    public void setUnknownExecution(Execution unknownExecution) {
        this.unknownExecution = unknownExecution;
    }

    public Execution getFaqExecution() {
        return faqExecution;
    }

    public void setFaqExecution(Execution faqExecution) {
        this.faqExecution = faqExecution;
    }
}
