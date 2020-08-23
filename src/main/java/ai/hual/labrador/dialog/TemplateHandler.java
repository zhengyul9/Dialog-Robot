package ai.hual.labrador.dialog;

import ai.hual.labrador.nlg.Template;
import ai.hual.labrador.nlg.TemplateModel;
import ai.hual.labrador.nlu.GrammarModel;

import java.util.List;

/**
 * A template handler that handles {@link TemplateModel}
 * Created by Dai Wentao on 2017/7/5.
 */
public class TemplateHandler {


    private final List<Template> templates;

    public TemplateHandler(List<Template> templates) {
        this.templates = templates;
    }

    /**
     * Generate model based on a given {@link GrammarModel}, modifying that base model and return it.
     *
     * @param base The model that grammars handler generation bases on
     * @return The modified base model
     */
    public TemplateModel handleTemplate(TemplateModel base) {
        base.getTemplates().addAll(templates);
        return base;
    }

}
