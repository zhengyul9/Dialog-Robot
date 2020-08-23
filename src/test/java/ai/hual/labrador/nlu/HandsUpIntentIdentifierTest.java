package ai.hual.labrador.nlu;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.local.remote.RemoteChatAccessor;
import ai.hual.labrador.local.remote.RemoteFAQAccessor;
import ai.hual.labrador.local.remote.RemoteIntentClassifierAccessor;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.matchers.ChatIntentMatcher;
import ai.hual.labrador.nlu.matchers.ClassifierIntentMatcher;
import ai.hual.labrador.nlu.matchers.FaqIntentMatcher;
import ai.hual.labrador.nlu.matchers.IntentClassifierResult;
import ai.hual.labrador.nlu.matchers.IntentScorePair;
import ai.hual.labrador.nlu.matchers.TemplateIntentMatcher;
import ai.hual.labrador.utils.ScoreUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static ai.hual.labrador.nlu.matchers.TemplateIntentMatcher.TEMPLATE_SCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandsUpIntentIdentifierTest {

    private static AccessorRepository accessorRepository;
    private static RemoteIntentClassifierAccessor classifierAccessor;
    private static RemoteFAQAccessor faqAccessor;
    private static RemoteChatAccessor chatAccessor;  // only for mock test
    private static GrammarModel grammarModel;
    private static Properties properties;

    private static HandsUpIntentIdentifier intentIdentifier;

    @BeforeEach
    void setUp() {
        grammarModel = new GrammarModel();
        properties = new Properties();
    }

    @Test
    void testIdentifyIntentWithAllMatchersTemplateDominant() {
        // accessor confidence
        float intentConfidence = 0.9f;
        String confidentIntent = "classifierIntent";
        float faqScore = 1f;
        float faqConfidence = 1f;
        float chatScore = 1f;
        float chatConfidence = 1f;

        String query = "abb";
        grammarModel.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, "TemplateIntent1", "{{A}}bb", 1f));
        grammarModel.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, "TemplateIntent2", "{{A}}b", 0.0001f));

        // mock classifierAccessor
        classifierAccessor = mock(RemoteIntentClassifierAccessor.class);
        List<IntentScorePair> pairs = Arrays.asList(new IntentScorePair(confidentIntent, intentConfidence));
        IntentClassifierResult classifierResult = new IntentClassifierResult("{{A}}bb", pairs, intentConfidence);
        when(classifierAccessor.handleIntentClassification(any())).thenReturn(classifierResult);

        // mock faqAccessor
        faqAccessor = mock(RemoteFAQAccessor.class);
        chatAccessor = mock(RemoteChatAccessor.class);   // only for mock test
        FaqAnswer faqAnswer = new FaqAnswer();
        faqAnswer.setScore(faqScore);
        faqAnswer.setConfidence(faqConfidence);
        faqAnswer.setQuery(query);
        FaqAnswer chatAnswer = new FaqAnswer();
        chatAnswer.setScore(chatScore);
        chatAnswer.setConfidence(chatConfidence);
        chatAnswer.setQuery(query);
        when(faqAccessor.handleFaqQuery(anyString())).thenReturn(faqAnswer);
        when(chatAccessor.handleFaqQuery(anyString())).thenReturn(chatAnswer);

        accessorRepository = new AccessorRepositoryImpl()
                .withIntentAccessor(classifierAccessor)
                .withFaqAccessor(faqAccessor)
                .withChatAccessor(chatAccessor);

        intentIdentifier = new HandsUpIntentIdentifier(grammarModel, accessorRepository, properties);

        List<QueryAct> inputActs = new ArrayList<>();

        ListMultimap<String, SlotValue> slots1 = ArrayListMultimap.create();
        slots1.put("A", new SlotValue("a"));
        inputActs.add(new QueryAct("abb", "{{A}}bb", slots1, 0.1));

        ListMultimap<String, SlotValue> slots2 = ArrayListMultimap.create();
        inputActs.add(new QueryAct("abb", "aab", slots2, 0.1));

        NLUResult result = intentIdentifier.identifyIntent(inputActs);

        assertEquals(TemplateIntentMatcher.class.getSimpleName(), result.getChosenMatcher());
        Map<String, List<QueryAct>> resultMap = result.getMatcherResultMap();
        assertEquals(4, resultMap.keySet().size());
        // template matcher
        assertEquals(3, resultMap.get(result.getChosenMatcher()).size());
        assertTrue(resultMap.get(result.getChosenMatcher()).get(0).getScore() == TEMPLATE_SCORE);
        assertTrue(resultMap.get(result.getChosenMatcher()).get(1).getScore() == TEMPLATE_SCORE);
        // classifier matcher
        String classifierMatcherName = ClassifierIntentMatcher.class.getSimpleName();
        double classifierAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(0);
        assertEquals(1, resultMap.get(classifierMatcherName).size());
        assertEquals(confidentIntent, resultMap.get(classifierMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(intentConfidence, classifierAlpha),
                resultMap.get(classifierMatcherName).get(0).getScore());
        // faq matcher
        String faqMatcherName = FaqIntentMatcher.class.getSimpleName();
        double faqAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(2);
        assertEquals(1, resultMap.get(faqMatcherName).size());
        assertEquals(SystemIntents.FAQ_INTENT, resultMap.get(faqMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(faqConfidence, faqAlpha), resultMap.get(faqMatcherName).get(0).getScore());
        // chat matcher
        String chatMatcherName = ChatIntentMatcher.class.getSimpleName();
        double chatAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(3);
        assertEquals(1, resultMap.get(chatMatcherName).size());
        assertEquals(SystemIntents.CHAT_INTENT, resultMap.get(chatMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(chatConfidence, chatAlpha), resultMap.get(chatMatcherName).get(0).getScore());
    }

    @Test
    void testIdentifyIntentWithAllMatchersClassifierDominant() {
        // accessor confidence
        float intentConfidence = 1f;
        String confidentIntent = "classifierIntent";
        float faqScore = 0.9f;
        float faqConfidence = 0.9f;
        float chatScore = 0.7f;
        float chatConfidence = 0.7f;

        String query = "abb";

        // mock classifierAccessor
        classifierAccessor = mock(RemoteIntentClassifierAccessor.class);
        List<IntentScorePair> pairs = Arrays.asList(new IntentScorePair(confidentIntent, intentConfidence));
        IntentClassifierResult classifierResult = new IntentClassifierResult("{{A}}bb", pairs, intentConfidence);
        when(classifierAccessor.handleIntentClassification(any())).thenReturn(classifierResult);

        // mock faqAccessor
        faqAccessor = mock(RemoteFAQAccessor.class);
        chatAccessor = mock(RemoteChatAccessor.class);   // only for mock test
        FaqAnswer faqAnswer = new FaqAnswer();
        faqAnswer.setScore(faqScore);
        faqAnswer.setConfidence(faqConfidence);
        faqAnswer.setQuery(query);
        FaqAnswer chatAnswer = new FaqAnswer();
        chatAnswer.setScore(chatScore);
        chatAnswer.setConfidence(chatConfidence);
        chatAnswer.setQuery(query);
        when(faqAccessor.handleFaqQuery(anyString())).thenReturn(faqAnswer);
        when(chatAccessor.handleFaqQuery(anyString())).thenReturn(chatAnswer);

        accessorRepository = new AccessorRepositoryImpl()
                .withIntentAccessor(classifierAccessor)
                .withFaqAccessor(faqAccessor)
                .withChatAccessor(chatAccessor);

        intentIdentifier = new HandsUpIntentIdentifier(grammarModel, accessorRepository, properties);

        List<QueryAct> inputActs = new ArrayList<>();

        ListMultimap<String, SlotValue> slots1 = ArrayListMultimap.create();
        slots1.put("A", new SlotValue("a"));
        inputActs.add(new QueryAct("abb", "{{A}}bb", slots1, 0.1));

        ListMultimap<String, SlotValue> slots2 = ArrayListMultimap.create();
        inputActs.add(new QueryAct("abb", "aab", slots2, 0.1));

        NLUResult result = intentIdentifier.identifyIntent(inputActs);

        assertEquals(ClassifierIntentMatcher.class.getSimpleName(), result.getChosenMatcher());
        Map<String, List<QueryAct>> resultMap = result.getMatcherResultMap();
        assertEquals(4, resultMap.keySet().size());
        // template matcher
        String templateMatcherName = TemplateIntentMatcher.class.getSimpleName();
        assertEquals(2, resultMap.get(templateMatcherName).size());
        assertEquals(0, resultMap.get(templateMatcherName).get(0).getScore());
        assertEquals(0, resultMap.get(templateMatcherName).get(1).getScore());
        // classifier matcher
        String classifierMatcherName = ClassifierIntentMatcher.class.getSimpleName();
        double classifierAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(1);
        assertEquals(1, resultMap.get(classifierMatcherName).size());
        assertEquals(confidentIntent, resultMap.get(classifierMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(intentConfidence, classifierAlpha),
                resultMap.get(classifierMatcherName).get(0).getScore());
        // faq matcher
        String faqMatcherName = FaqIntentMatcher.class.getSimpleName();
        double faqAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(2);
        assertEquals(1, resultMap.get(faqMatcherName).size());
        assertEquals(SystemIntents.FAQ_INTENT, resultMap.get(faqMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(faqConfidence, faqAlpha), resultMap.get(faqMatcherName).get(0).getScore());
        // chat matcher
        String chatMatcherName = ChatIntentMatcher.class.getSimpleName();
        double chatAlpha = HandsUpIntentIdentifier.DEFAULT_INTENT_MATCHERS_ALPHA.get(3);
        assertEquals(1, resultMap.get(chatMatcherName).size());
        assertEquals(SystemIntents.CHAT_INTENT, resultMap.get(chatMatcherName).get(0).getIntent());
        assertEquals(ScoreUtils.shiftScore(chatConfidence, chatAlpha), resultMap.get(chatMatcherName).get(0).getScore());
    }
}