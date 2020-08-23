package ai.hual.labrador.dialog.accessors;

import java.util.List;

public interface RelatedQuestionAccessor {

    List<String> relatedQuestionByFAQ(int faqId);

    List<String> relatedQuestionByKG(String instanceIRI, String propertyIRI);

}
