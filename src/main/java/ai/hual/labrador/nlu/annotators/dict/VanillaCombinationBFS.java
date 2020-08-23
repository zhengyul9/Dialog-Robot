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

public class VanillaCombinationBFS implements CombinationBFS {

    /**
     * Get all combinations by switching each slot, using priority queue to maintain
     * decreasing order(by queryAct's score).
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
            int slotsCount = queryAct.getSlots().values().size();
            for (int j = 0; j < slotsCount; j++) {
                // loop over slots to switch, then enqueue
                QueryAct switchedAct = removeSlot(queryAct, j);
                if (enqueuedActs.add(switchedAct))
                    actQueue.add(switchedAct);
            }
        }
        return result;
    }

    /**
     * Remove a slot by index.
     *
     * @param act              input queryAct
     * @param removedSlotIndex index of the slot to be removed
     * @return new queryAct
     */
    public static QueryAct removeSlot(QueryAct act, int removedSlotIndex) {

        QueryAct queryAct = new QueryAct(act);
        List<SlotValue> orderedSlots = sortSlotToList(queryAct.getSlots());
        SlotValue removedSlot = orderedSlots.get(removedSlotIndex);

        // replace pQuery by its original substring in query
        String replacement = queryAct.getQuery().substring(removedSlot.realStart, removedSlot.realEnd);
        queryAct.setPQuery(replaceSubstring(queryAct.getPQuery(),
                removedSlot.start, removedSlot.end, replacement));

        // bias to be added for slots located after this slot.
        int bias = replacement.length() - (removedSlot.end - removedSlot.start);

        // iterate over slots after removed slot to adjust start and end
        int slotsCount = orderedSlots.size();
        for (int i = removedSlotIndex + 1; i < slotsCount; i++) {
            orderedSlots.get(i).start += bias;
            orderedSlots.get(i).end += bias;
        }

        // replace slots
        SlotValue removed = orderedSlots.remove(removedSlotIndex);
        queryAct.setScore(queryAct.getScore() / slotScore(removed));
        ListMultimap<String, SlotValue> newSlots = ArrayListMultimap.create();
        orderedSlots.forEach(slot -> newSlots.put(slot.getKey(), slot));
        queryAct.setSlots(newSlots);

        return queryAct;
    }
}
