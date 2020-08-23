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

public interface AccessorRepository {

    KnowledgeAccessor getKnowledgeAccessor();

    FAQAccessor getFAQAccessor();

    FAQAccessor getChatAccessor();

    IntentClassifierAccessor getIntentClassifierAccessor();

    RelatedQuestionAccessor getRelatedQuestionAccessor();

    KnowledgeStatusAccessor getKnowledgeStatusAccessor();

    DatabaseAccessor getDatabaseAccessor();

    NLU getNLU();

    NLG getNLG();

    DictAccessor getDictAccessor();

    PropertyFrequencyAccessor getPropertyFrequencyAccessor();

}
