package ai.hual.labrador.nlu.utils;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.constants.IntentLabelDefinition;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntentLabelUtils {

    /**
     * Add parsed param into slots
     *
     * @param slots        slots reference.
     * @param paramsString param string in format "a=b,c&d=e,f"
     */
    public static void parseParamToSlots(ListMultimap<String, SlotValue> slots, String paramsString) {
        // split with "&"
        for (String paramString : paramsString.split(IntentLabelDefinition.SLOT_PARAMS_SPLIT_REGEX)) {
            // split with "="
            String[] paramSplit = paramString.split(IntentLabelDefinition.SLOT_PARAM_NAME_VALUE_SPLIT_REGEX);
            String paramKey = paramSplit[0];
            if (paramSplit.length < 2)
                throw new NLUException("Param string: \"" + paramsString + "\" not legal, no value after \"" + paramKey + "=\"");
            // split with ","
            for (String paramValue : paramSplit[1].split(IntentLabelDefinition.SLOT_PARAM_VALUES_SPLIT_REGEX)) {
                SlotValue slotValue = new SlotValue(paramValue);
                slotValue.key = paramKey;
                slots.put(paramKey, slotValue);
            }
        }
    }

    /**
     * build an intent label with param built from slots
     *
     * @param intent the intent
     * @param slots  slots reference.
     * @return intent label with params "intent?s1=v11,v12&s2=v21,v22"
     */
    public static String buildParamLabel(String intent, ListMultimap<String, String> slots) {
        return buildParamLabel(intent, slots, x -> x);
    }

    /**
     * build an intent label with param built from slots
     *
     * @param intent      the intent
     * @param slots       slots reference.
     * @param valueMapper A mapper that maps slots' value to string
     * @return intent label with params "intent?s1=v11,v12&s2=v21,v22"
     */
    public static <T> String buildParamLabel(String intent, ListMultimap<String, T> slots,
                                             Function<T, String> valueMapper) {
        StringBuilder builder = new StringBuilder(intent);
        if (!slots.isEmpty()) {
            builder.append(IntentLabelDefinition.INTENT_SLOT_SPLIT_TEXT);
            builder.append(slots.keySet().stream()
                    .map(key -> {
                        String paramString = slots.get(key).stream()
                                .map(valueMapper)
                                .collect(Collectors.joining(IntentLabelDefinition.SLOT_PARAM_VALUES_SPLIT_REGEX));
                        return key + IntentLabelDefinition.SLOT_PARAM_NAME_VALUE_SPLIT_REGEX + paramString;
                    })
                    .collect(Collectors.joining(IntentLabelDefinition.SLOT_PARAMS_SPLIT_REGEX)));
        }
        return builder.toString();
    }

    /**
     * Extract extra slots embodied in intent string.
     *
     * @param queryActs input queryActs
     */
    public static void extractExtraSlot(List<QueryAct> queryActs) {
        for (QueryAct queryAct : queryActs) {
            // split with "?"
            String[] intentSplit = queryAct.getIntent().split(IntentLabelDefinition.INTENT_SLOT_SPLIT_REGEX);
            if (intentSplit.length > 1) {
                queryAct.setIntent(intentSplit[0]);
                IntentLabelUtils.parseParamToSlots(queryAct.getSlots(), intentSplit[1]);
            }
        }
    }
}
