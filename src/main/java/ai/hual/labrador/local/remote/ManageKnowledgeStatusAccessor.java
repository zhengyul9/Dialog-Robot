package ai.hual.labrador.local.remote;

import ai.hual.labrador.kg.KnowledgeStatus;
import ai.hual.labrador.kg.KnowledgeStatusAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class ManageKnowledgeStatusAccessor implements KnowledgeStatusAccessor {

    private static final String ENABLED = "已启用";
    private static final String DISABLED = "已停用";

    private ManageClient client;

    ManageKnowledgeStatusAccessor(ManageClient client) {
        this.client = client;
    }

    @Override
    public KnowledgeStatus instanceStatus(String instanceIRI) {
        Response<String> result = client.getJSON("/bot/{botName}/knowledge/instance/status",
                ImmutableMap.of("instance_iri", instanceIRI),
                new TypeReference<Response<String>>() {
                });
        return parseKnowledgeStatusFromString(result.getMsg());
    }

    @Override
    public KnowledgeStatus propertyStatus(String instanceIRI, String propertyIRI) {
        Response<String> result = client.getJSON("/bot/{botName}/knowledge/property/status",
                ImmutableMap.of("instance_iri", instanceIRI, "property_iri", propertyIRI),
                new TypeReference<Response<String>>() {
                });
        return parseKnowledgeStatusFromString(result.getMsg());
    }

    private static KnowledgeStatus parseKnowledgeStatusFromString(String str) {
        return DISABLED.equals(str) ? KnowledgeStatus.DISABLED : KnowledgeStatus.ENABLED;
    }

}
