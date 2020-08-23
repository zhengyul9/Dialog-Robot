package ai.hual.labrador.kg;

public interface UpdatableKnowledgeAccessor extends KnowledgeAccessor {

    void update(String... updateStrings);

    void update(Iterable<String> updateStrings);

}
