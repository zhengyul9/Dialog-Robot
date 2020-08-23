package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import static ai.hual.labrador.utils.QueryActUtils.COMBINATION_THRESHOLD;
import static ai.hual.labrador.utils.QueryActUtils.sortSlotToList;
import static ai.hual.labrador.utils.ScoreUtils.slotScore;
import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

public class OverlapCombinationBFS implements CombinationBFS {
    /**
     * Get all combinations by switching each slot, using priority queue to maintain
     * decreasing order(by queryAct's score). With overlap turned on.
     *
     * @param queryActs list of queryAct
     * @return list of queryAct with all combinations
     */
    @Override
    public List<QueryAct> combinationBFS(List<QueryAct> queryActs) {

        List<QueryAct> result = new ArrayList<>();
        Set<QueryAct> enqueuedActs = new HashSet<>();

        // initialize queue
        PriorityQueue<QueryAct> actQueue = new PriorityQueue<>();
        actQueue.addAll(queryActs);
        enqueuedActs.addAll(queryActs);

        // BFS
        while (!actQueue.isEmpty() && result.size() < COMBINATION_THRESHOLD) {
            QueryAct queryAct = actQueue.poll();
            result.add(queryAct);
            int slotsCount = 0;
            for (SlotValue slotValue : queryAct.getSlots().values())
                if (!(slotValue.getStart() + slotValue.getEnd() == 0))  // both 0, indicate that this is a overlap alias
                    slotsCount += 1;
            for (int j = 0; j < slotsCount; j++) {
                // loop over slots to switch, then enqueue
                QueryAct switchedAct = removeOverlapSlot(queryAct, j);
                if (enqueuedActs.add(switchedAct))
                    actQueue.add(switchedAct);
            }
        }
        return result;
    }

    private static QueryAct removeOverlapSlot(QueryAct act, int removedSlotIndex) {
        QueryAct queryAct = new QueryAct(act);
        List<SlotValue> orderedSlots = sortSlotToList(queryAct.getSlots());

        // find the right start index, consider overlapping
        int naturalNextSlot = 0;    // with overlap index
        int naturalRemovedSlotIndex;
        int realNextSlot = 0;   // no overlap index
        SlotValue removedSlotHeader = orderedSlots.get(naturalNextSlot);
        while (realNextSlot < removedSlotIndex) {
            SlotValue nextSlot = orderedSlots.get(naturalNextSlot + 1);
            if (removedSlotHeader.realStart != nextSlot.realStart) {
                naturalNextSlot++;
                realNextSlot++;
                removedSlotHeader = nextSlot;
            } else
                naturalNextSlot++;
        }
        naturalRemovedSlotIndex = naturalNextSlot;
        int realStart = removedSlotHeader.getRealStart();
        int realEnd = removedSlotHeader.getRealEnd();
        assert removedSlotHeader.getRealStart() == realStart;
        assert removedSlotHeader.getRealEnd() == realEnd;

        // find the slot who replaced pQuery by pQuery position not both 0
        while (removedSlotHeader.start + removedSlotHeader.end == 0 &&
                removedSlotHeader.realStart == realStart && removedSlotHeader.realEnd == realEnd) {    // alias slot
            // if this loop out of bound, replace with slot has problem
            // cause if correct, must be a non alias slot
            naturalNextSlot++;
            removedSlotHeader = orderedSlots.get(naturalNextSlot);
        }

        SlotValue removedSlot = removedSlotHeader;  // slot replaced pQuery

        // replace pQuery by its original substring in queryAct.Query
        String replacement = queryAct.getQuery().substring(removedSlot.realStart, removedSlot.realEnd);
        queryAct.setPQuery(replaceSubstring(queryAct.getPQuery(),
                removedSlot.start, removedSlot.end, replacement));

        // remove overlaps
        orderedSlots.removeIf(slotValue -> slotValue.getRealStart() == realStart && slotValue.realEnd == realEnd
                && slotValue.start + slotValue.end == 0);

        // bias to be added for slots located after this slot.
        int bias = replacement.length() - (removedSlot.end - removedSlot.start);

        // iterate over slots after removed slot to adjust start and end
        int slotsCount = orderedSlots.size();
        for (int i = naturalRemovedSlotIndex + 1; i < slotsCount; i++) {
            SlotValue slot = orderedSlots.get(i);
            if (slot.start + slot.end != 0) {
                slot.start += bias;
                slot.end += bias;
            }
        }

        // replace slots
        SlotValue removed = orderedSlots.remove(naturalRemovedSlotIndex);
        queryAct.setScore(queryAct.getScore() / slotScore(removed));
        ListMultimap<String, SlotValue> newSlots = ArrayListMultimap.create();
        orderedSlots.forEach(slot -> newSlots.put(slot.getKey(), slot));
        queryAct.setSlots(newSlots);

        return queryAct;
    }
}
