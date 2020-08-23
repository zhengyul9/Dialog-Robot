package ai.hual.labrador.nlu.preprocessors.utils;

import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.SlotValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PreprocessUtils {

    private static Logger logger = LoggerFactory.getLogger(PreprocessUtils.class);

    public static String earlyStopProcess(String query, List<Preprocessor> preprocessors) {
        return earlyStopProcess(query, preprocessors, false);
    }

    /**
     * Process, stop before empty.
     *
     * @param query input query
     * @return processed query, will not be empty
     */
    public static String earlyStopProcess(String query, List<Preprocessor> preprocessors, boolean verbose) {
        for (Preprocessor preprocessor : preprocessors) {
            String prev = query;
            query = preprocessor.preprocess(query);
            if (verbose)
                logger.debug("After {}, processed query is {}", preprocessor.getClass().getSimpleName(), query);
            if (query.isEmpty()) {
                query = prev;
                break;
            }
        }
        return query;
    }

    /**
     * Skip overlapped slots whose pQuery start and pQuery end are all 0. So that
     * only the slot used to replace pQuery is substituted, and index can update correctly.
     *
     * @param sortedSlots
     * @param slotIndex
     * @return
     */
    public static SlotIndexPair skipOverlapSlots(List<SlotValue> sortedSlots, int slotIndex) {
        assert slotIndex < sortedSlots.size();
        SlotValue slot = sortedSlots.get(slotIndex);
        while (slot.getStart() == 0 && slot.getEnd() == 0)   // skip overlapped slot
            slot = sortedSlots.get(++slotIndex);
        return new SlotIndexPair(slot, slotIndex);
    }
}
