package ai.hual.labrador.utils;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ai.hual.labrador.nlu.Annotator.SLOT_PREFIX;
import static ai.hual.labrador.nlu.Annotator.SLOT_SUFFIX;
import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

public class QueryActUtils {

    public static final int COMBINATION_THRESHOLD = 300;
    /**
     * make sure after discount, minimum slot score (which is 1.02,
     * see {@link ai.hual.labrador.utils.ScoreUtils#slotScore})
     * still bigger than 1
     */
    public static final double DEFAULT_ALIAS_DISCOUNT = 0.995d;

    /**
     * Put all slot values in multimap into a list, which is sorted by start position
     *
     * @param slots values that are extracted by regex
     * @return list of slot values
     */
    public static List<SlotValue> sortSlotToList(Multimap<String, SlotValue> slots) {

        List<SlotValue> sortedSlotList = new ArrayList<>(slots.values());

        Collections.sort(sortedSlotList);   // sort

        return sortedSlotList;
    }

    /**
     * Put all slots inside list to multimap
     *
     * @param slotList list of slots
     * @return multimap of slotValue
     */
    public static ListMultimap<String, SlotValue> slotListToMultimap(List<SlotValue> slotList) {

        ListMultimap<String, SlotValue> slots = ArrayListMultimap.create();

        slotList.forEach(s -> slots.put(s.getKey(), s));

        return slots;
    }

    /**
     * Replace the suffix dict slot.
     *
     * @param act   input act
     * @param dict  dict
     * @param start start of slot
     * @param end   end of slot
     * @return replaced queryAct
     */
    public static QueryAct replaceSuffixDictSlot(QueryAct act, Dict dict, int start, int end) {

        QueryAct actCopy = new QueryAct(act);
        String pQuery = actCopy.getPQuery();
        String key = dict.getLabel();

        ListMultimap<String, SlotValue> slots = act.getSlots();
        // Original start position of this slot,need to be computed
        int realStart = start;
        // Original end position of this slot,need to be computed
        int realEnd = end;
        // Compute bias before current slot to get original start and end position
        for (String k : slots.keySet()) {
            for (SlotValue slotValue : slots.get(k)) {
                int preBias = slotValue.realLength - slotValue.length;
                if (start >= slotValue.end && slotValue.start + slotValue.end != 0) { // overlap condition need to be neglected
                    realStart += preBias;
                    realEnd += preBias;
                }
            }
        }

        actCopy.setPQuery(replaceSubstring(pQuery, start, end,
                SLOT_PREFIX + key + SLOT_SUFFIX));
        end = start + (SLOT_PREFIX + key + SLOT_SUFFIX).length();
        SlotValue slot = new SlotValue(dict.getWord(), key, "", start, end, realStart, realEnd);
        actCopy.getSlots().put(key, slot);
        // compute score based on slot length
        String matched = act.getQuery().substring(realStart, realEnd);
        double aliasDiscount = 1d;
        if (!matched.equals(dict.getWord()))
            aliasDiscount = DEFAULT_ALIAS_DISCOUNT;
        actCopy.setScore(actCopy.getScore() * aliasDiscount * slotScore(realEnd - realStart));

        slots = actCopy.getSlots();

        // update existed slots behind current slot
        for (String k : slots.keySet()) {
            for (SlotValue slotValue : slots.get(k)) {
                int postBias = slot.length - slot.realLength;
                if (slotValue.realStart >= realEnd && slot.start + slot.end != 0) {
                    slotValue.start += postBias;
                    slotValue.end += postBias;
                }
            }
        }
        return actCopy;
    }
}
