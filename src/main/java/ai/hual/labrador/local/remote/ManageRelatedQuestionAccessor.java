package ai.hual.labrador.local.remote;

import ai.hual.labrador.dialog.accessors.RelatedQuestionAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
class ManageRelatedQuestionAccessor implements RelatedQuestionAccessor {

    private ManageClient client;

    ManageRelatedQuestionAccessor(ManageClient client) {
        this.client = client;
    }

    @Override
    public List<String> relatedQuestionByFAQ(int faqId) {
        Response<List<String>> result = client.getJSON("/bot/{botName}/faq/relate/question",
                ImmutableMap.of("faq_id", String.valueOf(faqId)), new TypeReference<Response<List<String>>>() {
                });
        return result.getMsg();
    }

    @Override
    public List<String> relatedQuestionByKG(String instanceIRI, String propertyIRI) {
        Map<String, String> params = new HashMap<>();
        if (instanceIRI != null) {
            params.put("instance_iri", instanceIRI);
        }
        if (propertyIRI != null) {
            params.put("property_iri", propertyIRI);
        }

        Response<List<String>> result = client.getJSON("/bot/{botName}/knowledge/relate/question", params,
                new TypeReference<Response<List<String>>>() {
                });
        return result.getMsg();
    }
}
