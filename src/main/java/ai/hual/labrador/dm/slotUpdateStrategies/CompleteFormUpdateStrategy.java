package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.dm.hsm.Param;
import ai.hual.labrador.dm.hsm.Parameterized;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.nlu.QueryAct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.FILL_FORM;
import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.TAG_FORM;

public class CompleteFormUpdateStrategy extends Parameterized implements SlotUpdateStrategy {

    public static final String INTENT_KEY_NAME = "intent";

    public static final String FORM_COMPLETED = "COMPLETED";

    public static final String FORM_ENTRY_SLOT_NAME = "entrySlotName";

    @Param(tip = "需要填满的表，每个表项是已有的变量", component = FILL_FORM)
    private ContextedString form;

    @Param(tip = "当这些槽已被填上时，才开始更新该槽", required = false, component = TAG_FORM)
    private ContextedString slotsRequired;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object update(QueryAct act, Object original, Context context) {

        if (original != null && original.toString().equals(FORM_COMPLETED))
            return original;

        String formJson = form.getStr();
        List<FormEntry> formEntryList;
        try {
            formEntryList = mapper.readValue(formJson, new TypeReference<List<FormEntry>>() {
            });
        } catch (IOException e) {
            throw new DMException("Form json can not be converted to list of formEntry");
        }

        // won't update before dependent slots all filled, this make sure this slot
        // only updates when necessary
        List<String> slotsRequiredList = Optional.ofNullable(slotsRequired)
                .map(x -> x.renderToList(context)).orElse(Collections.emptyList());
        if (!slotsRequiredList.isEmpty()) {
            if (original == null) {
                for (String slotRequired : slotsRequiredList) {
                    if (context.slotContentByName(slotRequired) == null)
                        return null;
                }
            }
        }
        // traverse slots to be filled, set slot content to corresponding template if not filled
        for (FormEntry entry : formEntryList) {
            if (context.slotContentByName(entry.getSlotName()) == null) {
                Map<String, ContextedString> responseExecutionParams = new HashMap<>();
                responseExecutionParams.put(FORM_ENTRY_SLOT_NAME, new ContextedString(entry.getSlotName()));
                responseExecutionParams.put(INTENT_KEY_NAME, new ContextedString(String.join(",", entry.getTemplateIntents())));
                for (ai.hual.labrador.dm.Param param : entry.getTemplateParams())
                    responseExecutionParams.put(param.getKey(), new ContextedString(param.getValue()));
                return responseExecutionParams;
            }
        }

        // all slots filled, return completed mark
        return FORM_COMPLETED;
    }
}
