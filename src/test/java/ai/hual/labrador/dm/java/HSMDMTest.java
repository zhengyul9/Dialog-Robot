package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.CurrentState;
import ai.hual.labrador.dm.DMResult;
import ai.hual.labrador.dm.Execution;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.local.remote.RemoteFAQAccessor;
import ai.hual.labrador.local.remote.RemoteIntentClassifierAccessor;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.NLUImpl;
import ai.hual.labrador.nlu.NLUResult;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.matchers.FaqIntentMatcher;
import ai.hual.labrador.nlu.matchers.TemplateIntentMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static ai.hual.labrador.dm.utils.getClassLoaderFromJar;
import static ai.hual.labrador.dm.utils.getClassLoaderFromZip;
import static ai.hual.labrador.dm.utils.loadDialogConfigFromJson;
import static ai.hual.labrador.dm.utils.loadDialogConfigFromZip;
import static ai.hual.labrador.nlu.matchers.IntentMatcher.MATCHER_SCORE_UPPER_BOUND;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HSMDMTest {
    private static GrammarModel grammarModel;
    private static NLUImpl nlu;
    private static Properties properties;
    private static AccessorRepository accessorRepository;
    private static KnowledgeAccessor knowledgeAccessor;
    private static RemoteFAQAccessor faqAccessor;
    private static RemoteIntentClassifierAccessor intentAccessor;

    @BeforeEach
    void setup() {
        properties = new Properties();
        properties.put("nlu.intentMatchers", "templateIntentMatcher");
        faqAccessor = mock(RemoteFAQAccessor.class);
        intentAccessor = mock(RemoteIntentClassifierAccessor.class);
        knowledgeAccessor = mock(KnowledgeAccessor.class);
        FaqAnswer faqAnswer = new FaqAnswer();
        when(faqAccessor.handleFaqQuery(anyString())).thenReturn(faqAnswer);
        accessorRepository = new AccessorRepositoryImpl()
                .withFaqAccessor(faqAccessor)
                .withIntentAccessor(intentAccessor);
    }

    @Test
    void testFaqMatcherExecution() {
        FaqAnswer faqAnswer = new FaqAnswer();
        faqAnswer.setQuery("query");
        faqAnswer.setAnswer("answer");

        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);

        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();
        slots.put(FaqIntentMatcher.FAQ_ANSWER_SLOT_KEY, new SlotValue(faqAnswer));
        QueryAct act = new QueryAct("query", "pQuery", SystemIntents.FAQ_INTENT, slots, 0.8);
        List<QueryAct> acts = Arrays.asList(act);

        String matcherName = FaqIntentMatcher.class.getSimpleName();
        Map<String, List<QueryAct>> matcherResults = new HashMap<>();
        matcherResults.put(matcherName, acts);
        NLUResult nluResult = new NLUResult(matcherName, 0.8, matcherResults, acts);

        DMResult result = dm.process("test", nluResult, "");
        // default faqExecution result
        assertEquals(SystemIntents.FAQ_INTENT, result.getAct().getLabel());
        assertEquals("answer", result.getAct().getSlots().get(SystemIntents.FAQ_SLOT_RESULT).get(0));

    }

    @Test
    void testUnknownIntentExecution() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);

        List<QueryAct> hyps = Collections.singletonList(new QueryAct("test"));
        hyps.get(0).setIntent(SystemIntents.UNKNOWN);
        String matcherName = TemplateIntentMatcher.class.getSimpleName();
        Map<String, List<QueryAct>> matcherResults = new HashMap<>();
        matcherResults.put(matcherName, hyps);
        NLUResult nluResult = new NLUResult(matcherName, 0, matcherResults, new ArrayList<>());
        DMResult result = dm.process("test", nluResult, "");

        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testProcessDiffScoreHypsStopAtFirst() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);

        String input = "明天的机票多少钱，没有我就退票了";
        NLUResult nluResult = new NLUResult();
        nluResult.setChosenMatcher(TemplateIntentMatcher.class.getSimpleName());
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();
        List<QueryAct> resultList = Arrays.asList(
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问价格",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND - 0.1),
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "退票",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND - 0.2)
        );
        matcherResultMap.put(TemplateIntentMatcher.class.getSimpleName(), resultList);
        nluResult.setMatcherResultMap(matcherResultMap);

        DMResult result = dm.process(input, nluResult, "");
        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(context);
        assertEquals("咨询", context.getCurrentState().getCurrentState());
        assertEquals("问价格", context.getCurrentState().getSubStates().get("咨询").getCurrentState());
    }

    @Test
    void testProcessDiffScoreHypsStopAtFirstReachUpperBound() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);

        String input = "明天的机票多少钱，没有我就退票了";
        NLUResult nluResult = new NLUResult();
        nluResult.setChosenMatcher(TemplateIntentMatcher.class.getSimpleName());
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();
        List<QueryAct> resultList = Arrays.asList(
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问价格",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND),
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "退票",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND - 0.1)
        );
        matcherResultMap.put(TemplateIntentMatcher.class.getSimpleName(), resultList);
        nluResult.setMatcherResultMap(matcherResultMap);

        DMResult result = dm.process(input, nluResult, "");
        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(context);
        assertEquals("咨询", context.getCurrentState().getCurrentState());
        assertEquals("问价格", context.getCurrentState().getSubStates().get("咨询").getCurrentState());
    }

    @Test
    void testProcessSameScoreHypsStopAtFirstBecauseReachUpperBound() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);
        // nluResult
        String input = "明天的机票多少钱，没有我就退票了";
        NLUResult nluResult = new NLUResult();
        nluResult.setChosenMatcher(TemplateIntentMatcher.class.getSimpleName());
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();
        List<QueryAct> resultList = Arrays.asList(
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问价格",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND),
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问日期",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND)
        );
        matcherResultMap.put(TemplateIntentMatcher.class.getSimpleName(), resultList);
        nluResult.setMatcherResultMap(matcherResultMap);
        DMResult result = dm.process(input, nluResult, "");

        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(context);
        assertEquals("咨询", context.getCurrentState().getCurrentState());
        assertEquals("问价格", context.getCurrentState().getSubStates().get("咨询").getCurrentState());
    }

    @Test
    void testProcessSameScoreHypsTryNextActWithoutReachingUpperBound() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);
        // nluResult
        String input = "明天的机票多少钱，没有我就退票了";
        NLUResult nluResult = new NLUResult();
        nluResult.setChosenMatcher(TemplateIntentMatcher.class.getSimpleName());
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();
        List<QueryAct> resultList = Arrays.asList(
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问价格",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND - 0.1),
                new QueryAct(input, "{{日期}}的机票多少钱，没有我就退票了", "问日期",
                        ArrayListMultimap.create(), MATCHER_SCORE_UPPER_BOUND - 0.1)
        );
        matcherResultMap.put(TemplateIntentMatcher.class.getSimpleName(), resultList);
        nluResult.setMatcherResultMap(matcherResultMap);
        DMResult result = dm.process(input, nluResult, "");

        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(context);
        assertEquals("咨询", context.getCurrentState().getCurrentState());
        assertEquals("问日期", context.getCurrentState().getSubStates().get("咨询").getCurrentState());
    }

    /**
     * Debug message should be:
     * [main] DEBUG ai.hual.labrador.dm.java.HSMStateManager - 咨询 → 退票
     * [main] DEBUG ai.hual.labrador.dm.java.HSMStateManager - 退票 ↓ 问日期
     */
    @Test
    void testTransitionAcceptedThenExecutionExecuted() {

        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();  // add system maintained slot config
        ClassLoader classLoader = getClassLoaderFromJar();
        DialogImpl dialog = new DialogImpl(dialogConfig, classLoader, accessorRepository);
        Context context = dialogConfig.generateContext(new Properties());
        HSMStateManager stateManager = new HSMStateManager(dialog);

        // nlu result
        String input = "我想退票";
        List<QueryAct> hyps = Arrays.asList(
                new QueryAct(input, input, "退票", ArrayListMultimap.create(), 1.0)
        );

        // update state
        context.updateSlots(input, hyps.get(0), dialog);

        // step into state recursively
        CurrentState initCurrentState = context.getCurrentState();
        String initStateName = initCurrentState.getCurrentState();
        DMState initState = dialog.getState(initStateName);
        List<DMState> initSameLevelStates = dialog.getStates();
        TransitionResult transitionResult = stateManager.step(context, initState, initSameLevelStates,
                initCurrentState, 1, new HashSet<>(), 1f);
        assertEquals("问日期", transitionResult.getLeafState().getName());
        assertEquals(1f / HSMDM.MAX_DEPTH, (float) transitionResult.getScore());
        // transition from 咨询 to 订票 has a execution who puts "Executed" into slot "a"
        assertEquals("Executed", context.slotContentByName("a"));
    }

    /**
     * Debug message should be:
     * [main] DEBUG ai.hual.labrador.dm.java.HSMStateManager - 咨询 ↓ 问日期
     */
    @Test
    void noStateTransitionAcceptedTest() {

        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        DialogImpl dialog = new DialogImpl(dialogConfig, classLoader, accessorRepository);
        Context context = dialogConfig.generateContext(new Properties());
        HSMStateManager stateManager = new HSMStateManager(dialog);

        // nlu
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "phraseAnnotator");
        properties.put("nlu.dictAnnotator.usePinyinRobust", "true");
        grammarModel = new GrammarModel(Arrays.asList(
                new Grammar(GrammarType.INTENT_REGEX, "退订", // the mergers is not correct, can't jump
                        "我(想|要|就想)退票", 1.0f)
        ));
        nlu = new NLUImpl(new DictModel(), grammarModel, new AccessorRepositoryImpl(),
                annotators, properties);
        String input = "我想退票";
        List<QueryAct> hyps = nlu.understand(input).retrieveHyps();

        // update state
        context.updateSlots(input, hyps.get(0), dialog);

        // step into state recursively
        CurrentState initCurrentState = context.getCurrentState();
        String initStateName = initCurrentState.getCurrentState();
        DMState initState = dialog.getState(initStateName);
        List<DMState> initSameLevelStates = dialog.getStates();
        TransitionResult transitionResult = stateManager.step(context, initState, initSameLevelStates,
                initCurrentState, 1, new HashSet<>(), 1f);
        assertEquals("问日期", transitionResult.getLeafState().getName());
        assertEquals(1f, (float) transitionResult.getScore());
    }

    @Test
    void deserializeContextFromStrTest() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromJar();
        HSMDM dm = new HSMDM(dialogConfig, classLoader, accessorRepository, properties);

        QueryAct act = new QueryAct("我想退票");
        act.setIntent("退票");
        List<QueryAct> hyps = Collections.singletonList(act);
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();
        matcherResultMap.put("TemplateIntentMatcher", hyps);
        NLUResult nluResult = new NLUResult("TemplateIntentMatcher", 1, matcherResultMap, new ArrayList<>());
        DMResult result = dm.process("退票", nluResult, "");

        ObjectMapper objectMapper = new ObjectMapper();
        String stateStr = result.getState();
        Context context = null;
        try {
            context = objectMapper.readValue(stateStr, Context.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(context);
        assertEquals("退票", context.getCurrentState().getCurrentState());
        assertEquals("问日期", context.getCurrentState().getSubStates().get("退票").getCurrentState());
    }

    @Test
    void getDialogImplFromDialogConfigTest() {
        DialogConfig dialogConfig = loadDialogConfigFromJson();
        dialogConfig.addSystemSlots();
        ClassLoader classLoader = getClassLoaderFromZip();
        DialogImpl dialog = new DialogImpl(dialogConfig, classLoader, accessorRepository);
        assertTrue(dialog.getClass().toString().contains("ai.hual.labrador.dm.java.DialogImpl"));
    }

    @Test
    void testCurrentLeafState() {
        new Config();
        ObjectMapper mapper = new ObjectMapper();
        String contextJsonPath = Config.get("local_context_json");
        Context context = null;
        try {
            String contextStr = IOUtils.toString(Config.getLoader().getResourceAsStream(contextJsonPath), "UTF-8");
            context = mapper.readValue(contextStr, Context.class);
        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }
        assertNotNull(context);
        assertEquals("问日期", context.currentLeafState());
    }

    @Test
    void deserializeContextTest() {
        new Config();
        ObjectMapper mapper = new ObjectMapper();
        String contextJsonPath = Config.get("local_context_json");
        Context context = null;
        try {
            String contextStr = IOUtils.toString(Config.getLoader().getResourceAsStream(contextJsonPath), "UTF-8");
            context = mapper.readValue(contextStr, Context.class);
        } catch (NoClassDefFoundError | IOException e) {
            System.out.println(e);
        }
        assertNotNull(context);
        assertTrue(context.getClass().toString().contains("ai.hual.labrador.dm.Context"));
    }

    @Test
    void contextDeserializeFromDialogConfigTest() {
        DialogConfig dialogConfig = loadDialogConfigFromZip();
        Context context = dialogConfig.generateContext(new Properties());
        assertTrue(context.getClass().toString().contains("ai.hual.labrador.dm.Context"));
    }

    @Test
    void currentStateDeserializeTest() {
        DialogConfig dialogConfig = loadDialogConfigFromZip();
        CurrentState currentState = dialogConfig.generateCurrentState();
        assertTrue(currentState.getClass().toString().contains("ai.hual.labrador.dm.CurrentState"));
    }

    /**
     * PeopleCountStrategy is in jar.
     */
    @Test
    void loadPeopleCountStrategyClassFromLocalClassLoaderTest() {

        String ALGO_DM_PATH = "ai.hual.labrador.";
        URLClassLoader loader = getClassLoaderFromZip();
        String strategyClassName = "dm.slotUpdateStrategy.PeopleCountStrategy";
        SlotUpdateStrategy slotUpdateStrategy = null;
        for (int i = 0; i < 2; i++) {
            try {
                slotUpdateStrategy = (SlotUpdateStrategy) Class.forName(strategyClassName, true, loader).newInstance();
                break;
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                strategyClassName = ALGO_DM_PATH + strategyClassName;
                e.printStackTrace();
            }
        }
        assertNotNull(slotUpdateStrategy);
    }

    /**
     * This execution is in local.
     */
    @Test
    void loadHelloWorldExecutionClassFromZipJarTest() {

        ClassLoader loader = getClassLoaderFromZip();

        String executionClassName = "ai.hual.labrador.dm.executions.HelloWorldExecution";
        Execution execution = null;
        try {
            execution = (Execution) Class.forName(executionClassName, true, loader).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        assertNotNull(execution);
        Map<String, ContextedString> params = new HashMap<>();
        params.put("say", new ContextedString("It's working correctly!"));
        execution.setUp(params, accessorRepository);
        execution.execute(null);
    }

    @Test
    void deserializeDialogConfigFromZipJsonTest() {
        DialogConfig dialogConfig = loadDialogConfigFromZip();
        assertTrue(dialogConfig.toString().contains("ai.hual.labrador.dm.java.DialogConfig@"));
    }

    /**
     * Recursively construct CurrentState.
     *
     * @param currentState currentState whose subStates need to be constructed
     * @param states       states who are at the same level as currentState
     */
    private void recursiveCreateCurrentState(CurrentState currentState, List<DMStateConfig> states) {
        if (states == null || states.size() == 0)
            return;
        currentState.setSubStatesByDMState(states);
        for (DMStateConfig state : states) {
            String stateName = state.getName();
            recursiveCreateCurrentState(currentState.getSubStates().get(stateName), state.getSubStates());
        }
    }

    @Test
    void deserializeListToObjectFieldTest() {
        String json = "{\"slots\": [\"a\", \"b\"]}";
        ObjectMapper objectMapper = new ObjectMapper();
        SlotList slotList = null;
        try {
            slotList = objectMapper.readValue(json, SlotList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(slotList);
        ArrayList<String> slots = (ArrayList<String>) slotList.getSlots();
        assertEquals(2, slots.size());
        assertEquals("a", slots.get(0));
        assertEquals("b", slots.get(1));
    }
}
