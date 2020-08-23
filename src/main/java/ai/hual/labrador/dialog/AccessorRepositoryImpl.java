package ai.hual.labrador.dialog;

import ai.hual.labrador.dialog.accessors.DatabaseAccessor;
import ai.hual.labrador.dialog.accessors.DictAccessor;
import ai.hual.labrador.dialog.accessors.PropertyFrequencyAccessor;
import ai.hual.labrador.dialog.accessors.RelatedQuestionAccessor;
import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.KnowledgeStatusAccessor;
import ai.hual.labrador.nlg.NLG;
import ai.hual.labrador.nlu.NLU;
import ai.hual.labrador.nlu.matchers.IntentClassifierAccessor;

public class AccessorRepositoryImpl implements AccessorRepository {

    private KnowledgeAccessor knowledgeAccessor;
    private FAQAccessor faqAccessor;
    private FAQAccessor chatAccessor;
    private IntentClassifierAccessor intentClassifierAccessor;
    private RelatedQuestionAccessor relatedQuestionAccessor;
    private KnowledgeStatusAccessor knowledgeStatusAccessor;
    private DatabaseAccessor databaseAccessor;
    private NLU nlu;
    private NLG nlg;
    private DictAccessor dictAccessor;
    private PropertyFrequencyAccessor propertyFrequencyAccessor;

    public AccessorRepositoryImpl withKnowledgeAccessor(KnowledgeAccessor knowledgeAccessor) {
        this.knowledgeAccessor = knowledgeAccessor;
        return this;
    }

    public AccessorRepositoryImpl withFaqAccessor(FAQAccessor faqAccessor) {
        this.faqAccessor = faqAccessor;
        return this;
    }

    public AccessorRepositoryImpl withChatAccessor(FAQAccessor chatAccessor) {
        this.chatAccessor = chatAccessor;
        return this;
    }

    public AccessorRepositoryImpl withIntentAccessor(IntentClassifierAccessor intentClassifierAccessor) {
        this.intentClassifierAccessor = intentClassifierAccessor;
        return this;
    }

    public AccessorRepositoryImpl withRelatedQuestionAccessor(RelatedQuestionAccessor relatedQuestionAccessor) {
        this.relatedQuestionAccessor = relatedQuestionAccessor;
        return this;
    }

    public AccessorRepositoryImpl withKnowledgeStatusAccessor(KnowledgeStatusAccessor knowledgeStatusAccessor) {
        this.knowledgeStatusAccessor = knowledgeStatusAccessor;
        return this;
    }

    public AccessorRepositoryImpl withDictAccessor(DictAccessor dictAccessor) {
        this.dictAccessor = dictAccessor;
        return this;
    }

    public AccessorRepositoryImpl withDatabaseAccessor(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
        return this;
    }

    public AccessorRepositoryImpl withNLU(NLU nlu) {
        this.nlu = nlu;
        return this;
    }

    public AccessorRepositoryImpl withNLG(NLG nlg) {
        this.nlg = nlg;
        return this;
    }

    public AccessorRepositoryImpl withPropertyFrequencyAccessor(PropertyFrequencyAccessor propertyFrequencyAccessor){
        this.propertyFrequencyAccessor = propertyFrequencyAccessor;
        return this;
    }


    @Override
    public KnowledgeAccessor getKnowledgeAccessor() {
        return knowledgeAccessor;
    }

    @Override
    public FAQAccessor getFAQAccessor() {
        return faqAccessor;
    }

    @Override
    public FAQAccessor getChatAccessor() {
        return chatAccessor;
    }

    @Override
    public IntentClassifierAccessor getIntentClassifierAccessor() {
        return intentClassifierAccessor;
    }

    @Override
    public RelatedQuestionAccessor getRelatedQuestionAccessor() {
        return relatedQuestionAccessor;
    }

    @Override
    public KnowledgeStatusAccessor getKnowledgeStatusAccessor() {
        return knowledgeStatusAccessor;
    }

    @Override
    public DatabaseAccessor getDatabaseAccessor() {
        return databaseAccessor;
    }

    @Override
    public NLU getNLU() {
        return nlu;
    }

    @Override
    public NLG getNLG() {
        return nlg;
    }

    @Override
    public DictAccessor getDictAccessor() {
        return dictAccessor;
    }

    @Override
    public PropertyFrequencyAccessor getPropertyFrequencyAccessor(){ return propertyFrequencyAccessor;}
}
