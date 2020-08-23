package ai.hual.labrador.dialog;

import ai.hual.labrador.dm.DMModel;
import ai.hual.labrador.nlg.TemplateModel;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.GrammarModel;

/**
 * The whole dialog model that can be directly used by simulator and product service to handle dialog.
 * Created by Dai Wentao on 2017/6/28.
 */
public class DialogModel {

    private DictModel dictModel;
    private GrammarModel grammarModel;
    private DMModel dmModel;
    private TemplateModel templateModel;

    public DialogModel() {
    }

    public DialogModel(DictModel dictModel, GrammarModel grammarModel, DMModel dmModel, TemplateModel templateModel) {
        this.dictModel = dictModel;
        this.grammarModel = grammarModel;
        this.dmModel = dmModel;
        this.templateModel = templateModel;
    }

    public DictModel getDictModel() {
        return dictModel;
    }

    public void setDictModel(DictModel dictModel) {
        this.dictModel = dictModel;
    }

    public GrammarModel getGrammarModel() {
        return grammarModel;
    }

    public void setGrammarModel(GrammarModel grammarModel) {
        this.grammarModel = grammarModel;
    }

    public void setDMModel(DMModel dmModel) {
        this.dmModel = dmModel;
    }

    public DMModel getDMModel() {
        return dmModel;
    }

    public TemplateModel getTemplateModel() {
        return templateModel;
    }

    public void setTemplateModel(TemplateModel templateModel) {
        this.templateModel = templateModel;
    }

}
