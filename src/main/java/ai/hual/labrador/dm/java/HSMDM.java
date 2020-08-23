package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.CurrentState;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMResult;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.Instruction;
import ai.hual.labrador.dm.ResponseExecutionResult;
import ai.hual.labrador.dm.executions.DefaultFAQExecution;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.NLUResult;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.matchers.ChatIntentMatcher;
import ai.hual.labrador.nlu.matchers.FaqIntentMatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_FAQ_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_HYPS_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_INTENT_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_NLU_RESULT_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_RESPONSE_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_STATE_REPEATEDNESS_NAME;
import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURN_PARAMS_NAME;
import static ai.hual.labrador.dm.java.DialogImpl.USE_DEFAULT_UNKNOWN_EXECUTION_WHEN_ABSENT_PROP_NAME;
import static ai.hual.labrador.nlu.matchers.ChatIntentMatcher.CHAT_ANSWER_SLOT_KEY;
import static ai.hual.labrador.nlu.matchers.FaqIntentMatcher.FAQ_ANSWER_SLOT_KEY;
import static ai.hual.labrador.nlu.matchers.IntentMatcher.MATCHER_SCORE_UPPER_BOUND;
import static java.lang.Math.min;

public class HSMDM implements DM {

    private static final Logger logger = LoggerFactory.getLogger(HSMDM.class);

    private static final double TRANSITION_SCORE_SWITCH = 1f; // opened
    static final int MAX_DEPTH = 10;   // assumption: max depth will not exceed 10
    private static final int KEEPED_HYPS_SIZE = 5;

    private final ObjectMapper mapper = new ObjectMapper();

    private final AccessorRepository accessorRepository;
    private final DialogImpl dialog;    // logic of whole dialogue
    private final HSMStateManager stateManager;
    private final HSMPolicyManager policyManager;
    private Context initContext;

    public HSMDM(DialogConfig dialogConfig, ClassLoader classLoader, AccessorRepository accessorRepository, Properties properties) {
        this.accessorRepository = accessorRepository;
        // add system maintained slot config
        dialogConfig.addSystemSlots();
        this.initContext = dialogConfig.generateContext(properties);
        this.dialog = new DialogImpl(dialogConfig, classLoader, accessorRepository, properties);

        stateManager = new HSMStateManager(dialog);
        policyManager = new HSMPolicyManager();
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, String strState) {
        return process(input, nluResult, null, strState);
    }

    @Override
    public DMResult process(String input, NLUResult nluResult, @Nullable String turnParams,                            String strState) {
        Context context;
        // parse JSON string context into Context object
        try {
            // initialize context if strState is meaningless
            // TODO: consider the scenario where strState contains only some slots, no states
            if (!(strState == null || strState.equals("{}") || strState.equals("")))
                context = mapper.readValue(strState, Context.class);
            else
                context = new Context(this.initContext);

            String faqMatcherName = FaqIntentMatcher.class.getSimpleName();
            String chatMatcherName = ChatIntentMatcher.class.getSimpleName();
            // TODO: remove this hack, faqAnswerUpdateStrategy can refresh this slot
            context.putSlotContent(SYSTEM_FAQ_NAME, null);
            context.putSlotContent(SYSTEM_NLU_RESULT_NAME, nluResult);
            context.putSlotContent(SYSTEM_TURN_PARAMS_NAME, turnParams);

            List<QueryAct> hyps = nluResult.retrieveHyps();
            if (hyps.isEmpty()) {   // all matcher refused
                List<QueryAct> actsBeforeIntent = nluResult.getQueryActsBeforeIntent();
                assert !actsBeforeIntent.isEmpty();
                QueryAct unknownAct = new QueryAct(actsBeforeIntent.get(0));
                unknownAct.setScore(0d);
                // put first couple of acts into system maintained hyps slot (sys.hyps)
                context.putSlotContent(SYSTEM_HYPS_NAME, new ArrayList());
                return giveUnknownResult(input, context, unknownAct);
            }

            // put first couple of acts into system maintained hyps slot (sys.hyps)
            context.putSlotContent(SYSTEM_HYPS_NAME, hyps.subList(0, min(hyps.size(), KEEPED_HYPS_SIZE)));

            String chosenMatcher = nluResult.getChosenMatcher();
            if (chosenMatcher.equals(faqMatcherName) || chosenMatcher.equals(chatMatcherName)) {
                String answerSlotName;
                if (chosenMatcher.equals(faqMatcherName))
                    answerSlotName = FAQ_ANSWER_SLOT_KEY;
                else
                    answerSlotName = CHAT_ANSWER_SLOT_KEY;
                if (hyps.get(0).getSlots().get(answerSlotName).isEmpty())
                    return giveUnknownResult(input, context, hyps.get(0));
                FaqAnswer faqAnswer = (FaqAnswer) hyps.get(0).getSlots().get(answerSlotName).get(0).matched;
                context.putSlotContent(SYSTEM_FAQ_NAME, faqAnswer);
                return giveFaqResult(faqAnswer, context, hyps.get(0));
            } else  // intent classifier or template result
                return giveIntentResult(hyps, input, context);
        } catch (IOException e) {
            throw new DMException("Error converting context JSON: \n" + strState, e);
        }
    }

    @SuppressWarnings("unchecked")
    private DMResult giveIntentResult(List<QueryAct> hyps, String input, Context context) {
        Double bestActScore = null;
        double bestContextScore = 0d;
        Context bestContext = new Context(context);
        DMState leafState = null;
        // Go on in act only when next act score is identical as previous one.
        // However, if act score reach upper bound, try only the first one,
        // because in this case, order is maintained implicitly
        for (QueryAct act : hyps) {
            if (bestActScore == null)   // first act must be processed
                bestActScore = act.getScore();
            else if (bestActScore >= MATCHER_SCORE_UPPER_BOUND || act.getScore() < bestActScore)
                break;
            logger.debug("Processing query in DM: {}", act.getPQuery());
            Context contextCopy = new Context(context);
            // invoke update method of state manager
            contextCopy.updateSlots(input, act, dialog);
            // step into state recursively
            CurrentState initCurrentState = contextCopy.getCurrentState();
            if (initCurrentState.getCurrentState() == null)
                throw new DMException("请为最外层定义初始状态");
            DMState initState = dialog.getState(initCurrentState.getCurrentState());
            List<DMState> initSameLevelStates = dialog.getStates();
            TransitionResult transitionResult = stateManager.step(contextCopy, initState, initSameLevelStates,
                    initCurrentState, 1, new HashSet<>(), 1f);
            Double score = TRANSITION_SCORE_SWITCH * transitionResult.getScore();
            if (score > bestContextScore) {
                logger.debug("Best score updated to {}", score);
                bestContextScore = score;
                bestContext = contextCopy;
                bestContext.setCurrentState(initCurrentState);
                leafState = transitionResult.getLeafState();
            }
        }

        assert leafState != null;
        // accumulate count of leaf state repeatedness
        HashMap<String, Integer> repeatednessMap =
                (HashMap<String, Integer>) bestContext.slotContentByName(SYSTEM_STATE_REPEATEDNESS_NAME);
        if (repeatednessMap.containsKey(leafState.getName()))
            repeatednessMap.put(leafState.getName(), repeatednessMap.get(leafState.getName()) + 1);
        else
            repeatednessMap.put(leafState.getName(), 1);
        List<Execution> executions;
        if (SystemIntents.UNKNOWN.equals(bestContext.slotContentByName(SYSTEM_INTENT_NAME))) {
            if (dialog.getUnknownExecution() != null) {
                logger.debug("Executing unknownExecution");
                executions = Collections.singletonList(dialog.getUnknownExecution());
            } else {
                logger.debug("意图为sys.unknown，但未指定UnknownExecution，也不允许使用默认UnknownExecution，作为普通意图继续执行" +
                        "可以将{}参数设置为true来使用默认执行，或在DM管理界面中设置无意图执行", USE_DEFAULT_UNKNOWN_EXECUTION_WHEN_ABSENT_PROP_NAME);
                executions = leafState.getExecutions();
            }
        } else {
            logger.debug("Processing executions in state {}", leafState.getName());
            executions = leafState.getExecutions();
        }

        // invoke process method of policy manager
        Instructions instructions = new Instructions();
        ResponseAct answerAct = policyManager.process(bestContext, executions, instructions);

        bestContext.putSlotContent(SYSTEM_RESPONSE_NAME, answerAct);
        try {
            return new DMResult(mapper.writeValueAsString(bestContext), answerAct, instructions.getInstructions());
        } catch (JsonProcessingException e) {
            throw new DMException("Error converting context JSON when return Intent result: \n" + context, e);
        }
    }

    private DMResult giveFaqResult(FaqAnswer faqAnswer, Context context, QueryAct queryAct) {
        context.updateSlots(queryAct.getQuery(), queryAct, dialog);
        Instructions instructions = new Instructions();
        ResponseAct responseAct;
        if (Strings.isNullOrEmpty(faqAnswer.getAnswer())) { // no answer
            instructions.getInstructions().add(new Instruction("msginfo_faq_na"));
            responseAct = new ResponseAct(SystemIntents.UNKNOWN);
        } else { // has answer, use faqExecution to get response
            ResponseExecutionResult faqExecutionResult;
            if (dialog.getFaqExecution() == null) {
                DefaultFAQExecution faqExecution = new DefaultFAQExecution();
                faqExecutionResult = (ResponseExecutionResult) faqExecution.execute(context);
            } else
                faqExecutionResult = (ResponseExecutionResult) dialog.getFaqExecution().execute(context);
            responseAct = faqExecutionResult.getResponseAct();
            instructions.getInstructions().addAll(faqExecutionResult.getInstructions());
        }
        try {
            return new DMResult(mapper.writeValueAsString(context), responseAct, instructions.getInstructions());
        } catch (JsonProcessingException e) {
            throw new DMException("Error converting context JSON when return FAQ result: \n" + context, e);
        }
    }

    private DMResult giveUnknownResult(String input, Context context, QueryAct queryAct) {
        QueryAct unknownAct = new QueryAct(queryAct);
        unknownAct.setIntent(SystemIntents.UNKNOWN);
        List<QueryAct> hyps = Collections.singletonList(unknownAct);
        return giveIntentResult(hyps, input, context);
    }

    public AccessorRepository getAccessorRepository() {
        return accessorRepository;
    }

    @Override
    public void close() {
    }

}
