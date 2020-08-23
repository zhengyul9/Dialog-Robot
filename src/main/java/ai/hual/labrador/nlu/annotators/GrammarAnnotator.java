package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.preprocessors.StopWordRewriter;
import ai.hual.labrador.utils.ScoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static ai.hual.labrador.utils.StringUtils.regexToPattern;
import static java.lang.Math.min;

/**
 * An annotator that classify QueryAct, changing its pQuery into intent.
 * Created by Dai Wentao on 2017/7/12. Migrated from old code.
 */
@Component("grammarAnnotator")
public class GrammarAnnotator extends BaseAnnotator {

    public static final int MAX_INTENT_LENGTH = 10;

    private static final Logger logger = LoggerFactory.getLogger(GrammarAnnotator.class);

    private List<LabeledRegex<String>> regexList;
    public static final String GRAMMAR_PREPROCESSOR_PROP = "nlu.grammarAnnotator.preprocessor";
    private Preprocessor preprocessor;
    public static final String STOP_WORD_PREPROCESSOR = "stop_word";

    public GrammarAnnotator(@Autowired GrammarModel grammarModel, @Autowired Properties properties) {
        regexList = grammarModel.getGrammars().stream()
                .filter(x -> x.getType() == GrammarType.INTENT_REGEX)
                .map(x -> new LabeledRegex<>(handleSlottedRegex(x.getContent()), "", x.getLabel(), x.getScore()))
                .collect(Collectors.toList());
        String processorName = properties.getProperty(GRAMMAR_PREPROCESSOR_PROP);
        if (processorName != null) {
            switch (processorName) {
                case STOP_WORD_PREPROCESSOR:
                    preprocessor = new StopWordRewriter();
                    break;
                default:
                    preprocessor = null;
            }
        }
    }

    private QueryAct match(LabeledRegex<String> regex, QueryAct act, int start, int end) {
        String pQuery = act.getPQuery();
        Matcher m = regex.getRegex().matcher(pQuery.substring(0, end));
        if (m.find(start)) {
            // "abc}}def{{ghi" -> "def"
            boolean overlap = false;
            String group = m.group();
            int newStart = m.start();
            int newEnd = m.end();
            int firstPrefix = group.indexOf(SLOT_PREFIX);
            int firstSuffix = group.indexOf(SLOT_SUFFIX);
            if (firstPrefix > firstSuffix || firstPrefix < 0 && firstSuffix >= 0) {
                overlap = true;
                newStart = m.start() + firstSuffix + SLOT_SUFFIX.length();
            }
            int lastPrefix = group.lastIndexOf(SLOT_PREFIX);
            int lastSuffix = group.lastIndexOf(SLOT_SUFFIX);
            if (lastPrefix > lastSuffix || lastSuffix < 0 && lastPrefix >= 0) {
                overlap = true;
                newEnd = m.start() + lastPrefix;
            }
            if (overlap) {
                return match(regex, act, newStart, newEnd);
            }

            // when matched part is contained in a slot
            // which means matched part is a part of a slot name
            // invalid
            for (SlotValue slotValue : act.getSlots().values()) {
                if (slotValue.start < m.start() && slotValue.end > m.end()) {
                    return null;
                }
            }

            // calculate matched length in original query
            int matchLength;
            try {
                matchLength = realLength(act, m.start(), m.end());
            } catch (UnsupportedOperationException e) {
                // matched part in a slot
                // eg. "abc{{def}}ghi" matched "ef"
                return null;
            }

            // get matched regex in string
            String regexStr = regex.getRegex().pattern();
            String patternStr = regexToPattern(regexStr);
            int regexRealStart = realRegexStartPosition(act, m.start());
            int regexRealEnd = regexRealStart + matchLength;
            // compute score
            double patternDiscount;
            patternDiscount = ScoreUtils.patternMatchDiscountScore(regexStr, pQuery.substring(start, end));
            double score = act.getScore() * matchLength * patternDiscount * regex.getScore();
            QueryAct matchedAct = new QueryAct(act.getQuery(), act.getPQuery(), regex.getLabel(), regexStr,
                    m.start(), m.end(), regexRealStart, regexRealEnd, act.getSlots(), score);
            logger.debug("pattern of intent is: {}, pQuery: {}" + "score:" + score +
                    "=" + act.getScore() + "*" + matchLength + "*" + patternDiscount +
                    "*" + regex.getScore(), patternStr, pQuery);

            return matchedAct;
        }
        return null;
    }

    /**
     * When multiple matches of the regex exist, choose the one with max real length.
     * Check this example: GrammarAnnotatorTest#testAnnotateWithMultipleMatchChooseMaxRealLength
     *
     * @param regex regex
     * @param act   queryAct
     * @return the best act
     */
    private QueryAct greedyMatch(LabeledRegex<String> regex, QueryAct act) {
        int start = 0;
        int end = act.getPQuery().length();

        int bestMatchedRealLength = 0;
        QueryAct bestMatchedResult = null;
        while (start < end) {
            QueryAct matched = match(regex, act, start, end);
            if (matched == null)
                break;
            int matchedRealLength = matched.getRegexRealEnd() - matched.getRegexRealStart();
            if (matchedRealLength > bestMatchedRealLength) {
                bestMatchedRealLength = matchedRealLength;
                bestMatchedResult = matched;
                start = matched.getRegexEnd();
            } else
                start = matched.getRegexEnd();
        }
        return bestMatchedResult;
    }

    @Override
    public List<QueryAct> annotate(QueryAct act) {
        logger.debug("####### QueryAct: " + act);
        String pQuery = act.getPQuery();
        // get intent by template using original pQuery
        List<QueryAct> templateResult = regexList.stream()
                .map(regex -> greedyMatch(regex, act))
                .filter(Objects::nonNull).sorted().distinct().collect(Collectors.toList());

        // try after removing stop words in pQuery
        if (preprocessor != null) {
            String processedPQuery = preprocessor.preprocess(pQuery);
            if (!processedPQuery.equals(pQuery)) {
                act.setPQuery(processedPQuery);
                templateResult.addAll(regexList.stream()
                        .map(regex -> greedyMatch(regex, act))
                        .filter(Objects::nonNull).sorted().distinct().collect(Collectors.toList()));
            }
        }

        if (templateResult.isEmpty()) {
            templateResult = new ArrayList<>();
            act.setIntent(SystemIntents.UNKNOWN);
            act.setScore(0);
            templateResult.add(act);
        }
        Collections.sort(templateResult);
        return templateResult;
    }

    /**
     * Get intent by matchers.
     *
     * @param queryActs input queryActs
     * @return queryActs with intent, sorted by score
     */
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {
        List<QueryAct> result = new ArrayList<>();
        // annotate by templates
        result.addAll(queryActs.stream()
                .flatMap(act -> this.annotate(act).stream())
                .distinct()
                .collect(Collectors.toList()));
        Collections.sort(result);
        result = result.subList(0, min(result.size(), MAX_INTENT_LENGTH));
        return result;
    }
}