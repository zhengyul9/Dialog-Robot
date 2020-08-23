package ai.hual.labrador.kg;

public interface KnowledgeStatusAccessor {

    KnowledgeStatus instanceStatus(String instanceIRI);

    KnowledgeStatus propertyStatus(String instanceIRI, String propertyIRI);

}
