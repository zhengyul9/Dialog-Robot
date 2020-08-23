package ai.hual.labrador.utils;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.hual.labrador.utils.StringUtils.groupRegexWildcard;
import static java.lang.Math.pow;

public class ScoreUtils {

    private static final Logger logger = LoggerFactory.getLogger(ScoreUtils.class);

    /**
     * slotScore = exp(BASE, K)
     */
    private static double SLOT_SCORE_BASE = 1.1d;
    private static double SLOT_SCORE_K = 1.5d;

    /**
     * discount for different type of regex
     * shift sigmoid to make result more reasonable(saturate slower)
     */
    private static int PATTERN_SHIFT_SIZE = 2;
    private static int SHIFT_SIZE = 8;

    /**
     * Compute discount score. TODO: adapt to Knowledge graph
     * Punish a pattern by its wildcard and alternatives matched length.
     *
     * @param regex      the regex
     * @param matchedStr matched string`
     * @return score
     */
    public static double patternMatchDiscountScore(String regex, String matchedStr) {
        // preprocess
        regex = groupRegexWildcard(regex);
        ArrayList<String> regexGroupList = getGroupedRegex(regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(matchedStr);

        Pattern slotPattern = Pattern.compile("\\{\\{.*?}}");

        // count wildcard and exact match length
        int regexGroupCount = 0;
        int wildcardLen = 0;  // (.*)
        int alternativeQuantifierLen = 0; // (a|b|c)?
        if (matcher.find()) {
            String currentRegexGroup;
            String currentMatchedGroup;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (regexGroupCount >= regexGroupList.size())
                    break;
                currentRegexGroup = regexGroupList.get(regexGroupCount);
                currentMatchedGroup = matcher.group(i);
                if (currentMatchedGroup == null) {
                    // a lazy method to deal with evil regex, e.g  (微信|我要|(账|帐)号|退出|扫码|重新|)(登录|注册)
                    regexGroupCount += regexGroupCount == regexGroupList.size() - 1 ? 0 : 1;
                    continue;
                }
                int slotLengthBias = 0;
                Matcher slotMatcher = slotPattern.matcher(currentMatchedGroup);
                while (slotMatcher.find())
                    slotLengthBias += slotMatcher.end() - slotMatcher.start() - 1;
                if (currentRegexGroup.equals("(.*?)") || currentRegexGroup.equals("(.*)"))
                    wildcardLen += currentMatchedGroup.length() - slotLengthBias;
                else if (currentRegexGroup.charAt(currentRegexGroup.length() - 1) == '?')
                    alternativeQuantifierLen += currentMatchedGroup.length() - slotLengthBias;
                regexGroupCount += 1;
            }
        } else
            return 0d;  // matchedStr and regex don't actually match

        return sigmoid(SHIFT_SIZE - wildcardLen - alternativeQuantifierLen);
    }

    /**
     * Normalize score range of queryActs into (0, 1).
     *
     * @param queryActs Collection of queryActs
     */
    public static void normalizeQueryActScores(Collection<QueryAct> queryActs) {
        double minScore = Double.MAX_VALUE;
        double maxScore = 0d;
        // find min and max
        for (QueryAct act : queryActs) {
            double score = act.getScore();
            if (score < minScore)
                minScore = score;
            if (score > maxScore)
                maxScore = score;
        }
        // normalize
        for (QueryAct act : queryActs)
            act.setScore((act.getScore() - minScore) / (maxScore - minScore));
    }

    /**
     * An obsolete method to compute score.
     *
     * @param regex      the regex
     * @param matchedStr matched string`
     * @return score
     */
    @Deprecated
    public static double patternMatchScoreScoreBased(String regex, String matchedStr) {
        regex = StringUtils.groupRegexWildcard(regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(matchedStr);
        int wildcardCount = 0;
        int i;
        if (matcher.find()) {
            for (i = 1; i <= matcher.groupCount(); ++i) {
                if (matcher.group(i) == null)
                    break;
                wildcardCount += matcher.group(i).length();
            }
        }

        i = matchedStr.length() - wildcardCount;
        return sigmoid(i - wildcardCount - PATTERN_SHIFT_SIZE);
    }

    /**
     * Get all grouped part in regex.
     *
     * @param regex regex string
     * @return list of grouped part in string
     */
    private static ArrayList<String> getGroupedRegex(String regex) {
        Pattern pattern = Pattern.compile("\\(.*?\\)\\??");
        Matcher matcher = pattern.matcher(regex);
        ArrayList<String> groupList = new ArrayList<>();
        while (matcher.find()) {
            String matched = matcher.group(0);
            if (!matched.contains("?:"))
                groupList.add(matched);
            if (matched.contains("(") && !matched.contains(")") || !matched.contains("(") && matched.contains(")"))
                logger.warn("Malformed regex {}, Contains nested bracket! Score of intentAnnotator will be affected", regex);
        }
        return groupList;
    }

    /**
     * Sigmoid.
     *
     * @param x argument
     * @return sigmoid(n)
     */
    public static double sigmoid(double x) {
        return 1 / (1 + pow(Math.E, (-1 * x)));
    }

    /**
     * 2 * Sigmoid(2 * x) - 1.
     *
     * @param x argument bigger than 0
     * @return [sigmoid(n) - 0.5] * 2
     */
    public static double positiveNormalize(double x) {
        return 2 * sigmoid(2 * x) - 1;
    }

    /**
     * "Sigmoid" to compute slot score.
     *
     * @param slot slotValue
     * @return score in double
     */
    public static double slotScore(SlotValue slot) {
        int realLength = slot.realEnd - slot.realStart;
        if (realLength == 1)
            return 1.02d;
        return pow(SLOT_SCORE_BASE, realLength - SLOT_SCORE_K);
    }

    /**
     * "Sigmoid" to compute slot score.
     *
     * @param slotLength real length of slot
     * @return score in double
     */
    public static double slotScore(int slotLength) {
        if (slotLength == 1)
            return 1.02d;
        return pow(SLOT_SCORE_BASE, slotLength - SLOT_SCORE_K);
    }

    /**
     * f(1, alpha) = 1,
     * f(0, alpha) = 0,
     * f(x, alpha) > x for x in (0, 1)
     * monotonically increasing w.r.t x
     * higher alpha results in bigger derivative close to 0
     *
     * @param originScore
     * @return
     */
    public static double shiftScore(double originScore, double alpha) {
        return Math.pow(originScore, alpha);
    }

    /**
     * 'Sigmoid' to normalize score and confidence.
     *
     * @param x input
     * @return value after normalization
     */
    public static double normalizeScore(Double x) {
        return 2 * (sigmoid(x) - 0.5);
    }
}
