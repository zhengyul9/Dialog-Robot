package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dm.Param;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This class defines json structure for param value in
 * {@link CompleteFormUpdateStrategy}
 */
public class FormEntry {

    @JsonProperty("slot_name")
    private String slotName;

    @JsonProperty("template_intent")
    private String templateIntent;

    @JsonProperty("template_params")
    private List<Param> templateParams;

    public FormEntry() {
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getTemplateIntents() {
        return templateIntent;
    }

    public void setTemplateIntents(String templateIntent) {
        this.templateIntent = templateIntent;
    }

    public List<Param> getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(List<Param> templateParams) {
        this.templateParams = templateParams;
    }
}
