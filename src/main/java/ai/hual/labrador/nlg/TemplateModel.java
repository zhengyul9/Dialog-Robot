package ai.hual.labrador.nlg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of template
 * Created by Dai Wentao on 2017/7/5.
 */
public class TemplateModel implements Serializable {

    private List<Template> templates;

    public TemplateModel() {
        templates = new ArrayList<>();
    }

    public TemplateModel(List<Template> templates) {
        this.templates = templates;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }
}
