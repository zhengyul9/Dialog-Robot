package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import ai.hual.labrador.utils.QueryActUtils;
import ai.hual.labrador.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.hual.labrador.utils.QueryActUtils.sortSlotToList;
import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static ai.hual.labrador.utils.StringUtils.getBinaries;
import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

/**
 * Regular expression based implementation of the <tt>BaseAnnotator</tt> interface.
 * This implementation will use <tt>findMatch</tt> to find all matched regex
 * pattern, and then use <tt>replaceWithSlot</tt> to substitute original slots
 * with transformed slot using <tt>transform</tt>. The whole process is finished
 * in <tt>annotate</tt>.
 * <p>
 * <p>To implement a specific annotator, the <tt>labelMap</tt> must be constructed
 * as a one-to-one mapping from the label of regex pattern to actual lambda expression
 * which performs the transformation.
 * <p>
 * <p>For the annotators who implements this class, if the regex patterns are read
 * from a regex file, then it can use <tt>fetchRegexFile</tt> in its constructor.
 * For those who get regex patterns from database, a new method should be in
 * the class itself.
 * <p>
 * <p>
 * This is an example of regexAnnotator's usage:
 * <pre>
 *     regexList = { [regex: b{{c}}d, key: "slotName", label: "function(c)"] }
 *     labelMap = { "function(c)": g( valuePack{ matched: b{{c}}d, slotValues: [c: x]} ) }
 *     (where g is a lambda expression applied to {@link SlotValuePack}, assume the
 *     result is gLambdaResult)
 *
 *     act.pQuery = ab{{c}}de,    act.slots = { c: [x] }
 *     -> annotate(act) ->
 *     act.pQuery == a{{slotName}}e,
 *     act.slots == { slotName: [gLambdaResult] }
 * </pre>
 *
 * @author Yuqi
 * @see BaseAnnotator
 * @since 1.8
 */
public abstract class RegexAnnotator extends BaseAnnotator {

    private static Logger logger = LoggerFactory.getLogger(RegexAnnotator.class);

    /**
     * List of labeled regex.
     */
    protected List<LabeledRegex<String>> regexList;

    /**
     * Map from label string to lambada function.
     */
    protected Map<String, Function<SlotValuePack, Object>> labelMap = new HashMap<>();


    /**
     * Get regex defined by specific annotator.
     *
     * @return List of labeled regex.
     */
    protected List<LabeledRegex<String>> getRegex() {

        return this.regexList;
    }


    /**
     * Fetch regex from file resides in resource.
     *
     * @param file name of file in string, not path, but the name in config file
     * @return list of labeled regex
     */
    protected List<LabeledRegex<String>> fetchRegexFile(String file) {

        // Initialize configurations
        new Config();
        String KEY_REGEX_FILE = Config.get(file);

        List<LabeledRegex<String>> fetchedRegexList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Config.getLoader().getResourceAsStream(KEY_REGEX_FILE), StandardCharsets.UTF_8))) {
            br.lines().forEach(line -> {
                if (line.equals("") || line.charAt(0) == '#')
                    return;
                String[] split1 = line.split(" ");   // separated by tab
                assert (split1.length == 2);
                String regex = split1[0].trim();  // get regex string
                String keyLabel = split1[1].trim();  // get key=label string
                String[] split2 = keyLabel.split("=");
                assert (split2.length == 2);
                String key = split2[0];
                String label = split2[1];
                if (regex.equals("")) {
                    logger.warn("Has null regex in file");
                }
                regex = StringUtils.patternSlotPrefixToRegex(regex);
                regex = "^(" + regex + ")";
                LabeledRegex<String> labeledRegex = new LabeledRegex<>(regex, key, label);

                fetchedRegexList.add(labeledRegex);
            });
        } catch (IOException e) {
            throw new NLUException("Error reading regex file.", e);
        }
        return fetchedRegexList;
    }

    /**
     * Match longest regex at beginning of string subQuery.
     *
     * @param subQuery  Query that expected to be matched from beginning.
     * @param regexList List of regex.
     * @param pQueryPos Start position of original pQuery, used to determine start and end.
     * @return An un-transformed slot value contain enough info to get corresponding slot values.
     */
    protected SlotValue findMatch(String subQuery, List<LabeledRegex<String>> regexList, int pQueryPos) {

        SlotValue slotValue = null;

        /* values needed to tracked to create slotValue object */
        String matched = "";
        String label = "";
        String key = "";
        String regex = "";
        int start = 0;
        int end = 0;    // useful to decide next pQueryPos
        Boolean found = false;

        for (LabeledRegex<String> labeledRegex : regexList) {

            Pattern pattern = labeledRegex.getRegex();
            Matcher matcher = pattern.matcher(subQuery);

            // find regex matched subQuery with no unpaired "{{" or "}}" in matched string.
            String newMatched = null;
            while (matcher.find()) {   // if pattern found on head, has one match at most
                // new matched string's length > old one, means new one can cover old one, replace by new
                String group = matcher.group();
                if (org.apache.commons.lang3.StringUtils.countMatches(group, SLOT_PREFIX) ==
                        org.apache.commons.lang3.StringUtils.countMatches(group, SLOT_SUFFIX)) {
                    newMatched = group;
                    break;
                }
                int lastSlotPrefix = subQuery.lastIndexOf(SLOT_PREFIX);
                if (lastSlotPrefix <= 0) {
                    break;
                }
                subQuery = subQuery.substring(0, lastSlotPrefix);
                matcher = pattern.matcher(subQuery);
            }
            if (newMatched != null && newMatched.length() > matched.length()) {
                found = true;
                // update fields
                matched = newMatched;
                // in case the regex is positive lookahead
                start = pQueryPos + matcher.start();
                key = labeledRegex.getKey();
                label = labeledRegex.getLabel();
                end = start + key.length() + SLOT_PREFIX.length() + SLOT_SUFFIX.length();
                regex = pattern.pattern();
            }
        }

        if (found) {
            slotValue = new SlotValue(matched, key, label, start, end, 0, 0, regex);
        }

        return slotValue;
    }

    /**
     * Apply a lambda function to value pack, transform raw info
     * in slots and pQuery into meaningful slot.
     * <p>
     * <p>
     * e.g:
     * <pre>
     *     slotValuePack.slots = {数字: [9, 1]}
     *     slotValuePack.pQuery = 二零一七年{{数字}}月{{数字}}日
     *     -> slot = transform(slotValuePack) ->
     *     slot.value = DateUtils.Date(2017, 9, 1)
     *     slot.key = 日期
     * </pre>
     *
     * @param slotValuePack a pack of slotValues to be converted to objective object
     * @param slots         slots of queryAct
     * @return transformed slot value if succeed, else null
     */
    protected SlotValue transform(SlotValuePack slotValuePack, ListMultimap<String, SlotValue> slots) {

        String label = slotValuePack.label;
        Function<SlotValuePack, Object> function = labelMap.get(label);

        Object transformed;
        if (function == null) { // print error message
            transformed = slotValuePack.matchedQuery;
        } else {
            transformed = function.apply(slotValuePack);
        }
        if (transformed != null) {  // if transform succeed

            String key = slotValuePack.key;

            SlotValue newSlotValue = new SlotValue(transformed, key, slotValuePack.label,
                    slotValuePack.start, slotValuePack.end, slotValuePack.realStart, slotValuePack.realEnd);
            slots.put(key, newSlotValue);   // update slots

            return newSlotValue;

        } else {    // if transform failed
            return null;
        }

    }

    /**
     * Replace act's pQuery[start:end] with {{slotName}}, and put (slotName, value) into act's slots,
     * where value = valueFunction(removedSlots).
     * For slots inside [start:end], the values will be removed and kept in removedSlots.
     * For slots located after [start:end], their start and end will be updated.
     * <p>
     * <p>
     * e.g:
     * <pre>
     *     act.pQuery = ab{{c}}de{{c}},    act.slots = { c: [x, y] }
     *     -> replace(act, 1, 8, "f", g) ->
     *     act.pQuery == a{{f}}e{{c}},    act.slots == { c: [y], f: [g({ c: [x] })] }
     * </pre>
     *
     * @param act      The original query act
     * @param start    The start index of replacement in pQuery
     * @param end      The end index of replacement in pQuery
     * @param slotName The new slot value's slot name
     * @param label    The label of regex
     * @return replace succeeded or not
     */
    protected boolean replaceWithSlot(QueryAct act, int start, int end, String slotName, String label, String regex) {

        assert start < end;

        // make a copy
        QueryAct actCopy = new QueryAct(act);

        String matched = actCopy.getPQuery().substring(start, end);
        // replace pQuery
        String replacement = SLOT_PREFIX + slotName + SLOT_SUFFIX;
        actCopy.setPQuery(replaceSubstring(actCopy.getPQuery(), start, end, replacement));

        // update slots
        ListMultimap<String, SlotValue> slots = actCopy.getSlots();

        // bias to be added for slots located after this slot.
        int bias = replacement.length() - (end - start);
        double scoreBias = 1d;

        // calculating start index in original query
        int realStart = start;
        // calculating length in original query
        int realLength = end - start;

        // Removed slots, between [start:end], and replaced by new slot.
        ListMultimap<String, SlotValue> removedSlots = ArrayListMultimap.create();

        // iterate every slot key and value
        Iterator<String> keyIter = slots.keySet().iterator();
        while (keyIter.hasNext()) {
            String slotKey = keyIter.next();
            List<SlotValue> values = slots.get(slotKey);
            for (int i = 0; i < values.size(); i++) {
                SlotValue value = values.get(i);
                if (value.start < start && start < value.end || value.start < end && end < value.end) {
                    // overlapping
                    throw new UnsupportedOperationException(
                            "Replacing substring of an existing slot. Check the call to replaceWithSlot");
                }
                if (value.end <= start) {
                    // slot before [start:end]. calculate bias for realStart
                    // bias already shifted by the pQuery slot, no need to shift again by overlap slots
                    if (value.start + value.end == 0)
                        continue;
                    realStart += value.realLength - value.length;
                } else if (value.end <= end) {
                    // deal with overlap slot if overlap dict is used, ignore this might just be right
                    if (value.start + value.end == 0) {
                        // actually, this slot should be removed, but a unique mark will also do
                        value.start = -1;
                        value.end = -1;
                        continue;
                    }
                    // new slot covered slot. remove slot and inherit its length diff.
                    if (values.size() == 1) {   // when this value is the only one left for this key
                        scoreBias /= slotScore(values.get(0).realLength);
                        removedSlots.put(slotKey, values.get(0));
                        keyIter.remove();   // has to remove by keySet's own iterator, ConcurrentModificationException
                    } else {
                        scoreBias /= slotScore(values.get(i).realLength);
                        removedSlots.put(slotKey, values.remove(i));
                    }
                    realLength += value.realLength - value.length;
                    i--;
                } else {
                    // slot after [start:end], add bias according to the diff of original interval and
                    // replacement
                    value.start += bias;
                    value.end += bias;
                }
            }
        }

        int replacementEnd = start + replacement.length();
        int realEnd = realStart + realLength;
        // transform removed slots
        List<SlotValue> orderedSlots = sortSlotToList(removedSlots);
        String matchedQuery = act.getQuery().substring(realStart, realEnd);
        SlotValuePack slotValuePack = new SlotValuePack(matched, matchedQuery, orderedSlots, slotName, label,
                start, replacementEnd, realStart, realEnd);
        SlotValue slotValue = transform(slotValuePack, slots);

        if (slotValue != null) {  // meaningful transformation
            // keep changes
            slotValue.regex = regex;
            act.setPQuery(actCopy.getPQuery());
            act.setSlots(slots);
            act.setScore(actCopy.getScore() * scoreBias * slotScore(realLength));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Filter out matchedValue where regex failed to perform.
     * Default behavior is pass, check {@link NumAnnotator} for a serious
     * implementation.
     *
     * @param queryAct     the queryAct
     * @param matchedValue matched value as slot
     * @return pass or not
     */
    public boolean matchedValueFilter(QueryAct queryAct, SlotValue matchedValue) {
        return true;
    }

    /**
     * Greedy match with BFS combinations.
     *
     * @param queryAct
     * @return
     */
    public QueryAct annotateByCombination(QueryAct queryAct, String combination) {

        QueryAct queryActCopy = new QueryAct(queryAct);
        // if all slots are turned off, return input queryAct directly
        if (combination.matches("[0]+"))
            return queryActCopy;
        int currentSlot = 0;
        int pQueryPos = 0;
        // traverse pQuery
        boolean found = false;
        while (pQueryPos < queryActCopy.getPQuery().length()
                && currentSlot < combination.length()) {

            String subQuery = queryActCopy.getPQuery().substring(pQueryPos);
            SlotValue matchedValue = findMatch(subQuery, regexList, pQueryPos);
            if (matchedValue != null) {
                // get slot window
                int start = matchedValue.start;
                int end = start + ((String) matchedValue.matched).length();
                boolean passedFilter = matchedValueFilter(queryActCopy, matchedValue);
                // move on if this matched value can't pass filter
                if (!passedFilter) {
                    pQueryPos++;
                    continue;
                }
                // jump to next slot if this slot is turned off
                if (combination.charAt(currentSlot) == '0') {
                    currentSlot++;
                    pQueryPos = end;
                    continue;
                }
                // tell if the replacement succeeded
                boolean replaced = replaceWithSlot(queryActCopy, start, end, matchedValue.key, matchedValue.label, matchedValue.regex);
                if (replaced) {
                    found = true;
                    currentSlot++;
                }
                // TODO subQuery using replaced
                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            } else {    // could not find any match

                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            }
        }
        if (found)
            return queryActCopy;
        else
            return queryAct;
    }

    /**
     * An optimization for choosing next pQuery position.
     *
     * @param queryAct  the queryAct
     * @param pQueryPos previous pQueryPos
     * @param subQuery  the pQuery part in concern
     * @return new position
     */
    private int getPQueryPos(QueryAct queryAct, int pQueryPos, String subQuery) {
        // surpass already annotated slots
        if (0 < subQuery.length() - 1 &&
                subQuery.substring(0, 2).equals("{{")) {
            int k = 2;
            while (!subQuery.substring(k, k + 2).equals("}}"))
                k++;
            return pQueryPos + k + 2;
        }
        return pQueryPos + 1;
    }

    /**
     * Match greedily. Traverse all regex in regexList, while truncating
     * head of pQueryInput after one round, to assure the part being matched
     * is always the leading characters of String. Inclusion of regex are
     * resolved by keeping only the ones who are not being included by others.
     * And record the number of slots being annotated in this annotator.
     *
     * @param queryAct           input queryAct
     * @param annotatedSlotCount number of slots annotated in this annotator
     * @return queryAct if pattern found, else null
     */
    public QueryAct greedyMatchAnnotate(QueryAct queryAct, int[] annotatedSlotCount) {

        annotatedSlotCount[0] = 0;
        QueryAct queryActCopy = new QueryAct(queryAct);
        int pQueryPos = 0;
        // traverse pQuery
        boolean found = false;
        while (pQueryPos < queryActCopy.getPQuery().length()) {
            String subQuery = queryActCopy.getPQuery().substring(pQueryPos);
            SlotValue matchedValue = findMatch(subQuery, regexList, pQueryPos);
            if (matchedValue != null) {
                if (!matchedValueFilter(queryActCopy, matchedValue)) {
                    pQueryPos++;
                    continue;
                }
                // get slot window, then replace slot
                int start = matchedValue.start;
                int end = start + ((String) matchedValue.matched).length();
                boolean replaced = replaceWithSlot(queryActCopy, start, end, matchedValue.key,
                        matchedValue.label, matchedValue.regex);
                if (replaced) { // replace succeeded
                    found = true;
                    annotatedSlotCount[0]++;
                    pQueryPos = start + matchedValue.key.length() +
                            SLOT_SUFFIX.length() + SLOT_PREFIX.length();
                    continue;
                }
                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            } else {    // could not find any match
                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            }
        }
        if (found)
            return queryActCopy;
        else
            return queryAct;
    }

    /**
     * Match greedily. Traverse all regex in regexList, while truncating
     * head of pQueryInput after one round, to assure the part being matched
     * is always the leading characters of String. Inclusion of regex are
     * resolved by keeping only the ones who are not being included by others.
     *
     * @param queryAct input queryAct
     * @return queryAct if pattern found, else null
     */
    public QueryAct greedyMatchAnnotate(QueryAct queryAct) {

        QueryAct queryActCopy = new QueryAct(queryAct);
        int pQueryPos = 0;
        // traverse pQuery
        boolean found = false;
        while (pQueryPos < queryActCopy.getPQuery().length()) {

            String subQuery = queryActCopy.getPQuery().substring(pQueryPos);
            SlotValue matchedValue = findMatch(subQuery, regexList, pQueryPos);

            if (matchedValue != null) {
                if (!matchedValueFilter(queryActCopy, matchedValue)) {
                    pQueryPos++;
                    continue;
                }
                found = true;
                // get slot window, then replace slot
                int start = matchedValue.start;
                int end = start + ((String) matchedValue.matched).length();
                replaceWithSlot(queryActCopy, start, end, matchedValue.key, matchedValue.label, matchedValue.regex);
                subQuery = queryActCopy.getPQuery().substring(pQueryPos);
                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            } else {    // could not find any match
                pQueryPos = getPQueryPos(queryActCopy, pQueryPos, subQuery);
            }
        }
        if (found)
            return queryActCopy;
        else
            return queryAct;
    }

    /**
     * Annotate the input queryAct, modify pQuery with respect to slots
     * extracted by regex list.
     *
     * @param queryAct The query act to be annotated.
     * @return List of all slots combination.
     */
    public List<QueryAct> annotate(QueryAct queryAct) {

        PriorityQueue<QueryAct> queryActQueue = new PriorityQueue<>();
        List<QueryAct> queryActList = new ArrayList<>();    // return result
        // get regex list
        List<LabeledRegex<String>> regexList = getRegex();
        if (regexList.size() == 0) {    // no regex
            queryActList.add(queryAct);
            return queryActList;
        }
        // match and replace slots
        int[] annotatedSlotCount = {0};
        QueryAct annotatedAct = greedyMatchAnnotate(queryAct, annotatedSlotCount);
        int slotCount = annotatedSlotCount[0];
        if (!annotatedAct.getPQuery().equals(queryAct.getPQuery()) && slotCount != 0) {
            queryActQueue.add(annotatedAct);
            List<String> combinationList = getBinaries(slotCount);
            // remove last combination, where all slots are switched on
            combinationList.remove(combinationList.size() - 1);
            // generate queryActs for each combination
            for (String combination : combinationList) {
                if (queryActQueue.size() >= QueryActUtils.COMBINATION_THRESHOLD)
                    break;
                QueryAct act = annotateByCombination(queryAct, combination);
                queryActQueue.add(act);
            }
            while (!queryActQueue.isEmpty())
                queryActList.add(queryActQueue.poll());
        } else {    // no slot found, return input queryAct
            queryActList.add(queryAct);
        }
        return queryActList;
    }

    /**
     * Annotate the input queryAct, modify pQuery with respect to slots
     * extracted by regex list.
     *
     * @param queryAct The query act to be annotated.
     * @return List of all slots combination.
     */
    @Deprecated
    public List<QueryAct> annotateMax(QueryAct queryAct) {

        List<QueryAct> queryActList = new ArrayList<>();    // return result
        // get regex list
        List<LabeledRegex<String>> regexList = getRegex();
        if (regexList.size() == 0) {
            queryActList.add(queryAct);
            return queryActList;
        }
        // match and replace slots
        QueryAct annotatedAct = greedyMatchAnnotate(queryAct);
        queryActList.add(annotatedAct);
        return queryActList;
    }
}
