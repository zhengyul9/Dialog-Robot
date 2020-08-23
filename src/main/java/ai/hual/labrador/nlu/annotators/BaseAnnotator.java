package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.RegexUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static ai.hual.labrador.utils.QueryActUtils.sortSlotToList;
import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

/**
 * A annotator base that helps annotators to do slot transformations.
 * Created by Dai Wentao on 2017/7/12.
 */
public abstract class BaseAnnotator implements Annotator {

    /**
     * Replacing slots in regex. Replacing "{{" with "\{\{", so that
     * java Pattern can handle slot matching.
     *
     * @param regex The original regex that may contain "{{" and "}}"
     * @return The new regex with "\{\{" and "\}\}"
     */
    protected String handleSlottedRegex(String regex) {
        return RegexUtils.handelSlottedRegex(regex);
    }

    /**
     * Replace act's pQuery[start:end] with {{slotName}}, and put (slotName, value) into act's slots,
     * where value = valueFunction(removedSlots).
     * For slots inside [start:end], the values will be removed and kept in removedSlots.
     * For slots located after [start:end], their start and end will be updated.
     * <p>
     * e.g:
     * <pre>
     *     act.pQuery = ab{{c}}de{{c}},    act.slots = { c: [x, y] }
     *     -> replace(act, 1, 8, "f", g) ->
     *     act.pQuery == a{{f}}e{{c}},    act.slots == { c: [y], f: [g({ c: [x] })] }
     * </pre>
     *
     * @param act           The original query act
     * @param start         The start index of replacement in pQuery
     * @param end           The end index of replacement in pQuery
     * @param slotName      The new slot value's slot name
     * @param valueFunction The label that takes removedSlots and returns new value
     */
    protected void replaceWithSlot(QueryAct act, int start, int end, String slotName,
                                   Function<List<SlotValue>, Object> valueFunction) {
        assert start < end;

        // replace pQuery
        String replacement = SLOT_PREFIX + slotName + SLOT_SUFFIX;
        act.setPQuery(replaceSubstring(act.getPQuery(), start, end, replacement));

        // update slots
        ListMultimap<String, SlotValue> slots = act.getSlots();

        // bias to be added for slots located after this slot.
        int bias = replacement.length() - (end - start);

        // calculating start index in original query
        int realStart = start;
        // calculating length in original query
        int realLength = end - start;

        // Removed slots, between [start:end], and replaced by new slot.
        ListMultimap<String, SlotValue> removedSlots = ArrayListMultimap.create();

        // iterate every slot key and value
        for (String slotKey : slots.keySet()) {
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
                    realStart += value.realLength - value.length;
                } else if (value.end <= end) {
                    // new slot covered slot. remove slot and inherit its length diff.
                    removedSlots.put(slotKey, values.remove(i));
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

        // add new slot
        int replacementEnd = start + replacement.length();
        int realEnd = realStart + realLength;
        List<SlotValue> orderedSlots = sortSlotToList(removedSlots);
        SlotValue newValue = new SlotValue(valueFunction.apply(orderedSlots),
                slotName, null, start, replacementEnd, realStart, realEnd);
        slots.put(slotName, newValue);
        slots.get(slotName).sort(Comparator.comparingInt(x -> x.start));
    }

    /**
     * Do the same thing as {@link #replaceWithSlot(QueryAct, int, int, String, Function)}
     * except that the new slot value is a given one.
     *
     * @param act      The original query act
     * @param start    The start index of replacement in pQuery
     * @param end      The end index of replacement in pQuery
     * @param slotName The new slot value's slot name
     * @param value    The value of the new slot
     */
    protected void replaceWithSlot(QueryAct act, int start, int end, String slotName, Object value) {
        replaceWithSlot(act, start, end, slotName, removedSlots -> value);
    }

    /**
     * Calculate length in original query given a start and end index on pQuery.
     * e.g
     * <pre>
     *     act.query = "abcdef"
     *     act.pQuery, act.slots = "a{{x}}d{{y}}f", {x: ["bc"], y: ["e"]}
     *     -> realLength(act, 1, 12) == 4
     * </pre>
     *
     * @param act   The query act for the calculation
     * @param start The start index on pQuery
     * @param end   The end index on pQuery
     * @return The real length of query's substring correlated to pQuery[start:end]
     */
    public static int realLength(QueryAct act, int start, int end) {
        assert start < end;
        int realLength = end - start;

        // iterate every value
        for (SlotValue slot : act.getSlots().values()) {
            if (slot.start < start && start < slot.end || slot.start < end && end < slot.end) {
                // overlapping
                throw new UnsupportedOperationException(
                        "Length calculation overlap with an existing slot. Check the call to realLength");
            }
            if (!(slot.start == 0 && slot.end == 0) && start <= slot.start && slot.end <= end)    // skip overlap slot
                realLength += slot.realLength - slot.length;
        }
        return realLength;
    }

    /**
     * Calculate real position of regex matched.
     *
     * @param act                 the queryAct
     * @param pQueryStartPosition regex start position with respect to pQuery
     * @return regex start position with respect to query
     */
    public static int realRegexStartPosition(QueryAct act, int pQueryStartPosition) {
        int realPosition = pQueryStartPosition;
        // iterate every value
        for (SlotValue slot : act.getSlots().values()) {
            if (slot.getStart() + slot.getEnd() == 0)
                continue;
            if (slot.start < pQueryStartPosition && pQueryStartPosition < slot.end) {
                // overlapping
                throw new UnsupportedOperationException(
                        "Length calculation overlap with an existing slot. Check the call to realRegexStartPosition");
            }
            if (slot.end <= pQueryStartPosition) {
                realPosition += slot.realLength - slot.length;
            }
        }
        return realPosition;
    }
}
