package ai.hual.labrador.nlu;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.nlu.matchers.IntentMatcher;
import ai.hual.labrador.nlu.utils.IntentLabelUtils;
import ai.hual.labrador.utils.ScoreUtils;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class HandsUpIntentIdentifier implements IntentIdentifier {

    private static Logger logger = LoggerFactory.getLogger(HandsUpIntentIdentifier.class);

    public static final String MATCHER_PROP_NAME = "nlu.intentMatchers";
    public static final String MATCHER_ALPHA_PROP_NAME = "nlu.intentMatchersAlpha";
    public static final String MATCHER_REFUSE_SCORE_PROP_NAME = "nlu.intentMatchersRefuseScore";

    private static final String MATCHER_PACKAGE = "ai.hual.labrador.nlu.matchers";

    private static final List<String> DEFAULT_INTENT_MATCHERS = Arrays.asList(
            "classifierIntentMatcher",
            "templateIntentMatcher",
            "faqIntentMatcher",
            "chatIntentMatcher");

    public static final List<Double> DEFAULT_INTENT_MATCHERS_ALPHA = ImmutableList.of(1d, 1d, 1d, 1d);

    public static final List<Double> DEFAULT_INTENT_MATCHERS_REFUSE_SCORE = ImmutableList.of(0d, 0d, 0d, 0d);

    private List<IntentMatcher> matchers;
    private List<Double> matchersAlpha; // matcher weight, used to adjust mean of each matcher's score
    private List<Double> matchersRefuseScore; // refuse the matcher if bestAct's score lower than this

    public HandsUpIntentIdentifier(GrammarModel grammarModel, AccessorRepository accessorRepository,
                                   Properties properties) {
        List<String> intentMatchers;    // or say, matchers
        if (properties.containsKey(MATCHER_PROP_NAME)) {
            intentMatchers = Arrays.asList(properties.getProperty(MATCHER_PROP_NAME).split(","));
            logger.debug("Used intentMatchers: {}", intentMatchers);
        } else
            intentMatchers = DEFAULT_INTENT_MATCHERS;
        List<Integer> matcherIndex = intentMatchers.stream()
                .map(DEFAULT_INTENT_MATCHERS::indexOf)
                .collect(Collectors.toList());
        if (properties.containsKey(MATCHER_ALPHA_PROP_NAME)) {
            matchersAlpha = Arrays.stream(properties.getProperty(MATCHER_ALPHA_PROP_NAME).split(","))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
            logger.debug("Used intentMatchersAlpha: {}", matchersAlpha);
        } else
            matchersAlpha = matcherIndex.stream()
                    .map(DEFAULT_INTENT_MATCHERS_ALPHA::get)
                    .collect(Collectors.toList());
        if (properties.containsKey(MATCHER_REFUSE_SCORE_PROP_NAME)) {
            matchersRefuseScore = Arrays.stream(properties.getProperty(MATCHER_REFUSE_SCORE_PROP_NAME).split(","))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
            logger.debug("Used intentMatchersRefuseScore: {}", matchersRefuseScore);
        } else
            matchersRefuseScore = matcherIndex.stream()
                    .map(DEFAULT_INTENT_MATCHERS_REFUSE_SCORE::get)
                    .collect(Collectors.toList());

        assert intentMatchers.size() == matchersAlpha.size() && intentMatchers.size() == matchersRefuseScore.size();

        AnnotationConfigApplicationContext matcherContext = new AnnotationConfigApplicationContext();
        ConfigurableListableBeanFactory factory = matcherContext.getBeanFactory();
        matcherContext.refresh();
        factory.registerSingleton("grammarModel", grammarModel);
        factory.registerSingleton("accessorRepository", accessorRepository);
        factory.registerSingleton("properties", properties);
        matcherContext.scan(MATCHER_PACKAGE);
        // construct matchers
        this.matchers = intentMatchers.stream()
                .map(name -> matcherContext.getBean(name, IntentMatcher.class))
                .collect(Collectors.toList());
    }

    @Override
    public NLUResult identifyIntent(List<QueryAct> queryActs) {
        assert !queryActs.isEmpty();
        Map<String, List<QueryAct>> matcherResultMap = new HashMap<>();

        double bestScore = -1;
        String bestMatcher = null;
        for (int i = 0; i < matchers.size(); i++) {
            IntentMatcher matcher = matchers.get(i);
            double matcherAlpha = matchersAlpha.get(i);
            double refuseScore = matchersRefuseScore.get(i);
            String matcherName = matcher.getClass().getSimpleName();
            List<QueryAct> matcherResult = matcher.matchIntent(queryActs);
            assert !matcherResult.isEmpty();
            QueryAct bestAct = matcherResult.get(0);
            // adjust score average
            matcherResult.forEach(act -> act.setScore(ScoreUtils.shiftScore(act.getScore(), matcherAlpha)));
            // insert extra slot
            IntentLabelUtils.extractExtraSlot(matcherResult);
            // Meet the requirement that when encounter with a matcher result whose bestAct score=1.0, this matcher is chosen.
            // Refuse this matcher result if lower than threshold, but still keep a record of the refused result.
            if (bestAct.getScore() > bestScore && bestAct.getScore() >= refuseScore) {  // use >= to assure 0 won't be filtered in template result
                bestScore = bestAct.getScore();
                bestMatcher = matcherName;
            }
            matcherResultMap.put(matcherName, matcherResult);
        }

        return new NLUResult(bestMatcher, bestScore, matcherResultMap, queryActs);
    }
}
