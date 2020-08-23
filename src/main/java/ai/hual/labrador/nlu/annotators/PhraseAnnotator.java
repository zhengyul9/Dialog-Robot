package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.SlotValuePack;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import ai.hual.labrador.utils.DirectionalRegionUtils;
import ai.hual.labrador.utils.RegionUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.hual.labrador.utils.QueryActUtils.slotListToMultimap;
import static ai.hual.labrador.utils.QueryActUtils.sortSlotToList;
import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

/**
 * Implementation of the {@link RegexAnnotator} abstract class.
 * This annotator can <Strong>recursively</Strong> annotate
 * <Strong>phrase</Strong> in queryAct, before witch, the queryAct
 * should already be annotated with other raw annotators, such as
 * {@link NumAnnotator} and {@link DateAnnotator}.
 *
 * <p> Annotated slot's key will be modified in either case, and its
 * matched value might change if a convert function is explicitly marked
 * in phrase regex, and resides in <tt>phraseLabelMap</tt>, while real
 * position will not be modified.
 * pQuery position is adjusted accordingly.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 我是真的不想吃甜的还有牛肉之类的
 *     act.pQuery = 我是真的不想吃{{口味}}的还有{{食材}}之类的
 *     act.slots = { 口味: [甜], 食材: [牛肉] }
 *     -> PhraseAnnotator.annotate(act) ->
 *     act.pQuery == 我是真的不想吃{{不想吃口味}}的还有{{不想吃食材}}之类的
 *     act.slots = { 不想吃口味: [甜], 不想吃食材: [牛肉] }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("phraseAnnotator")
public class PhraseAnnotator extends RegexAnnotator {

    /**
     * the strategy of phrase annotator replacing matched phrase.
     * e.g.:     {{A}}bc{{D}}, A=a, D=d
     * slotName: {{E}}bc{{E}}, E=a, E=d
     * query:    {{E}}, E=abcd
     * pQuery:   {{E}}, E={{A}}bc{{D}}, A=a, D=d
     * preserve: {{A}}bc{{D}}, A=a, D=d, E={{A}}bc{{D}}
     */
    public static final String CONFIG_REPLACE_KEY = "nlu.phraseAnnotator.replace";
    public static final String CONFIG_REPLACE_VALUE_SLOT_NAME = "slotName"; // only change slot name
    public static final String CONFIG_REPLACE_VALUE_QUERY = "query"; // replace and use query.
    public static final String CONFIG_REPLACE_VALUE_PQUERY = "pQuery"; // replace and use pQuery.
    public static final String CONFIG_REPLACE_VALUE_PRESERVE = "preserve"; // preserve the original pQuery, but add slot
    private static final int MAX_RECURSIONS = 5;

    /**
     * When a slot name is replaced with a system slot name maintained by NLU,
     * <t>matched</t> content in SlotValue should be converted to corresponding type.
     */
    private static Map<String, Function<Object, Object>> phraseLabelMap = new HashMap<>();

    static {
        phraseLabelMap.put("dateToStartDate(date)", matched -> DirectionalDateUtils.startDate((DateUtils.Date) matched));
        phraseLabelMap.put("dateToEndDate(date)", matched -> DirectionalDateUtils.endDate((DateUtils.Date) matched));
        phraseLabelMap.put("regionToStartRegion(region)", matched -> DirectionalRegionUtils.from((RegionUtils.Region) matched));
        phraseLabelMap.put("regionToEndRegion(region)", matched -> DirectionalRegionUtils.to((RegionUtils.Region) matched));
    }

    private String replace;

    PhraseAnnotator(@Autowired GrammarModel grammarModel, @Autowired Properties properties) {
        // get regex list
        this.regexList = grammarModel.getGrammars().stream()
                .filter(x -> x.getType() == GrammarType.PHRASE_REGEX)
                .filter(x -> x.getLabel().split("=").length == 2)
                .map(x -> new LabeledRegex<>(handleSlottedRegex(x.getContent()),
                        x.getLabel().split("=")[0], x.getLabel().split("=")[1], x.getScore()))
                .collect(Collectors.toList());
        replace = properties.getProperty(CONFIG_REPLACE_KEY, CONFIG_REPLACE_VALUE_SLOT_NAME);
    }

    /**
     * Annotate the input queryAct recursively, modify pQuery and matched object
     * with respect to slots extracted by regex list.
     *
     * @param queryAct The query act to be annotated.
     * @return List of queryAct, each one is a possible interpretation. Only concern one now.
     */
    @Override
    public List<QueryAct> annotate(QueryAct queryAct) {
        return annotate(queryAct, 0);
    }

    private List<QueryAct> annotate(QueryAct queryAct, int recursions) {
        List<QueryAct> queryActList = new ArrayList<>();    // return result
        // get regex list
        List<LabeledRegex<String>> regexList = getRegex();
        if (regexList.size() == 0) {
            queryActList.add(queryAct);
            return queryActList;
        }

        QueryAct annotatedAct = greedyMatchAnnotate(queryAct);

        // if found match(pQuery changed) recursively annotate until no match found
        if (!annotatedAct.getPQuery().equals(queryAct.getPQuery()) && recursions < MAX_RECURSIONS) {
            return annotate(annotatedAct, recursions + 1);
        } else {
            queryActList.add(annotatedAct);
            return queryActList;
        }
    }

    /**
     * Replace double curly braces with single one, add '^' before to
     * enforce match only at beginning, unless the regex used positive
     * or negative lookbehind.
     *
     * @param regex The original regex that may contain "{{"
     * @return processed regex
     */
    @Override
    protected String handleSlottedRegex(String regex) {

        String[] lookBehindRegex = {"?<=", "?<!"};

        regex = regex.replaceAll("\\{\\{", "\\\\\\{\\\\\\{");
        // if has look-ahead regex, don't need to append "^" at start
        Boolean hasSpecialRegex = false;
        for (String specialRegex : lookBehindRegex) {
            if (regex.contains(specialRegex)) {
                regex = "(" + regex + ")";
                hasSpecialRegex = true;
                break;
            }
        }
        if (!hasSpecialRegex)
            regex = "^(" + regex + ")";

        return regex;
    }

    @Override
    protected boolean replaceWithSlot(QueryAct act, int start, int end, String slotName, String label, String regex) {
        switch (replace) {
            case CONFIG_REPLACE_VALUE_PRESERVE:
                return replaceWithSlotPreservingPQuery(act, start, end, label, slotName, regex);
            case CONFIG_REPLACE_VALUE_PQUERY:
                return replaceWithSlotReplacingPQuery(act, start, end, label, slotName, regex);
            case CONFIG_REPLACE_VALUE_QUERY:
                return super.replaceWithSlot(act, start, end, label, slotName, regex);
            case CONFIG_REPLACE_VALUE_SLOT_NAME:
            default:
                return replaceWithSlotReplacingSlotName(act, start, end, slotName, label, regex);

        }
    }

    /**
     * Replace slots who lies inside [start:end] with {{combinedSlotName}}.
     * For slots located before [start:end] nothing will be modified.
     * For slots inside [start:end], key, pQuery position and matched object will be modified.
     * For slots located after [start:end], only pQuery position will be adjusted.
     * <p>
     * e.g:
     * <pre>
     *     act.pQuery = ab{{c}}de{{c}},    act.slots = { c: [x, y] }
     *     -> replace(act, 1, 8, "prefix_", "prefix") ->
     *     act.pQuery == a{{prefix_c}}e{{prefix_c}},
     *     act.slots == { prefix_c: [x, y] }
     * </pre>
     *
     * @param act      The original query act
     * @param start    The start index of replacement in pQuery
     * @param end      The end index of replacement in pQuery
     * @param slotName The new slot value's slot name
     * @param label    The label of regex
     */
    protected boolean replaceWithSlotReplacingSlotName(QueryAct act, int start, int end, String slotName, String label, String regex) {
        assert start < end;

        // the most complex label may take the form: replace=日期起始@1#dateToStartDate(date),日期结束@3#dateToEndDate(date)
        String[] labelArray = label.split(",");
        Boolean treatAllSlotSame = labelArray.length == 1 && !labelArray[0].contains("@");

        Map<Integer, KeyFuncLabel> posKeyFuncMap = new HashMap<>();
        if (!treatAllSlotSame) {    // every label in labelArray has position mark
            // construct map from position of slot to key-function pair for slot
            for (String keyPosFuncStr : labelArray) {
                String[] keyPosFunc = keyPosFuncStr.split("@");
                String[] posFunc = keyPosFunc[1].split("#");
                KeyFuncLabel keyFuncLabel = new KeyFuncLabel(keyPosFunc[0], posFunc.length == 2 ? posFunc[1] : null);
                posKeyFuncMap.put(Integer.parseInt(posFunc[0]), keyFuncLabel);
            }
        } else {
            String[] keyFuncStr = labelArray[0].split("#");
            posKeyFuncMap.put(0, new KeyFuncLabel(keyFuncStr[0], keyFuncStr.length == 2 ? keyFuncStr[1] : null));
        }


        // make a copy
        QueryAct actCopy = new QueryAct(act);

        // update slots
        ListMultimap<String, SlotValue> slots = actCopy.getSlots();

        // modify pQuery
        String pQuery = actCopy.getPQuery();

        // bias to be added for slots after start
        int bias = 0;

        // counter for slots who reside in [start, end]
        int includedSlotCount = 0;

        // whether replaced. if not, it indicates no slot is in the phrase slot, and the text will be replaced directly
        boolean replaced = false;

        // iterate every slot
        List<SlotValue> sortedValues = sortSlotToList(slots);
        for (SlotValue value : sortedValues) {
            if (value.start < start && start < value.end || value.start < end && end < value.end) {
                // overlapping
                throw new UnsupportedOperationException(
                        "Replacing substring of an existing slot. Check the call to replaceWithSlot");
            }
            if (value.start >= start && value.end <= end) {
                // deal with overlap slot if overlap dict is used, ignore this might just be right
                if (value.start + value.end == 0) {
                    // actually, this slot should be removed, but a unique mark will also do
                    value.start = -1;
                    value.end = -1;
                    continue;
                }
                includedSlotCount++;
                // adjust position for each slot
                value.start += bias;
                value.end += bias;
                // get the right position function pair
                KeyFuncLabel keyFuncLabel = treatAllSlotSame ? posKeyFuncMap.get(0) : posKeyFuncMap.get(includedSlotCount);
                if (keyFuncLabel == null)
                    continue;
                String originKey = value.key;
                // slot inside [start, end], adjust key, start, end
                String newKey = combineKey(slotName, keyFuncLabel.keyLabel, originKey);
                // modify matched object if function is not null
                if (keyFuncLabel.funcLabel != null) {
                    try {
                        value.matched = phraseLabelMap.get(keyFuncLabel.funcLabel).apply(value.matched);
                    } catch (NullPointerException e) {
                        throw new NLUException("Function " + keyFuncLabel.funcLabel + "does not exist in map");
                    }
                }
                // modify pQuery
                String newKeyStr = SLOT_PREFIX + newKey + SLOT_SUFFIX;
                value.key = newKey;
                pQuery = pQuery.substring(0, value.start) + newKeyStr +
                        pQuery.substring(value.end);
                actCopy.setPQuery(pQuery);
                // adjust position for slots who changed key
                int lengthDiff = newKey.length() - originKey.length();
                value.end += lengthDiff;
                value.length += lengthDiff;
                value.label = keyFuncLabel.keyLabel;
                value.regex = regex;
                // adjust bias
                bias += lengthDiff;
                replaced = true;
            } else if (value.start >= end) {
                // slot behind [start:end], adjust pQuery position only
                value.start += bias;
                value.end += bias;
            }
        }

        // no slot replaced in phrase slot. directly replace text with phrase slot
        if (!replaced) {
            return super.replaceWithSlot(act, start, end, label, slotName, regex);
        }

        // keep changes
        act.setPQuery(actCopy.getPQuery());
        act.setSlots(slotListToMultimap(sortedValues));
        act.setScore(actCopy.getScore());
        return true;
    }

    /**
     * Replace act's pQuery[start:end] with {{slotName}}, and put (slotName, value) into act's slots,
     * where value = valueFunction(removedSlots).
     * For slots inside [start:end], the values will be moved to 0-0
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
    protected boolean replaceWithSlotReplacingPQuery(QueryAct act, int start, int end, String slotName, String label, String regex) {
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
                    scoreBias /= slotScore(value.realLength);
                    removedSlots.put(slotKey, value);
                    realLength += value.realLength - value.length;

                    // move slot to 0-0 to avoid conflict slots
                    value.start = 0;
                    value.end = 0;
                    value.length = 0;
                    value.realStart = 0;
                    value.realEnd = 0;
                    value.realLength = 0;
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
        SlotValuePack slotValuePack = new SlotValuePack(matched, matched, orderedSlots, slotName, label,
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
     * Replace act's pQuery[start:end] with {{slotName}}, and put (slotName, value) into act's slots,
     * where value = valueFunction(removedSlots).
     * For slots inside [start:end], the values will be moved to 0-0
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
    protected boolean replaceWithSlotPreservingPQuery(QueryAct act, int start, int end, String slotName, String label, String regex) {
        assert start < end;

        // make a copy
        QueryAct actCopy = new QueryAct(act);

        String matched = actCopy.getPQuery().substring(start, end);

        // update slots
        ListMultimap<String, SlotValue> slots = actCopy.getSlots();

        // Removed slots, between [start:end], and replaced by new slot.
        ListMultimap<String, SlotValue> removedSlots = ArrayListMultimap.create();

        // iterate every slot key and value
        for (String slotKey : slots.keySet()) {
            List<SlotValue> values = slots.get(slotKey);
            for (SlotValue value : values) {
                if (value.start < start && start < value.end || value.start < end && end < value.end) {
                    // overlapping
                    throw new UnsupportedOperationException(
                            "Replacing substring of an existing slot. Check the call to replaceWithSlot");
                }
                if (value.end > start && value.end <= end) {
                    // deal with overlap slot if overlap dict is used, ignore this might just be right
                    removedSlots.put(slotKey, value);
                }
            }
        }

        // transform removed slots
        List<SlotValue> orderedSlots = sortSlotToList(removedSlots);
        SlotValuePack slotValuePack = new SlotValuePack(matched, matched, orderedSlots, slotName, label,
                0, 0, 0, 0);
        SlotValue slotValue = transform(slotValuePack, slots);

        if (slotValue != null) {  // meaningful transformation
            // keep changes
            slotValue.regex = regex;
            act.setPQuery(actCopy.getPQuery());
            act.setSlots(slots);
            act.setScore(actCopy.getScore());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Combine key name in label and key name of slot, which is
     * assigning meaning to the raw slot value.
     *
     * @param label    key of slot
     * @param labelKey key from label
     * @param slotKey  indicate method of combine
     * @return combined key name
     */
    protected String combineKey(String label, String labelKey, String slotKey) {
        if (label.equals("prefix"))
            return labelKey + slotKey;
        if (label.equals("suffix"))
            return slotKey + labelKey;
        if (label.equals("replace"))
            return labelKey;
        else {
            System.out.println("label of phrase regex can not be " + labelKey +
                    ", choose from \"prefix\", \"suffix\", \"replace\"");
            return null;
        }
    }

    /**
     * Structure for key function pair.
     */
    private static class KeyFuncLabel {
        String keyLabel;
        String funcLabel;

        public KeyFuncLabel(String keyLabel, String funcLabel) {
            this.keyLabel = keyLabel;
            this.funcLabel = funcLabel;
        }

        public String getKeyLabel() {
            return keyLabel;
        }

        public void setKeyLabel(String keyLabel) {
            this.keyLabel = keyLabel;
        }

        public String getFuncLabel() {
            return funcLabel;
        }

        public void setFuncLabel(String funcLabel) {
            this.funcLabel = funcLabel;
        }
    }
}

