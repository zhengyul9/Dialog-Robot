package ai.hual.labrador.nlu;

import java.util.List;

/**
 * Annotator in NLU, which annotate a {@link QueryAct} and produce a list
 * of {@link QueryAct}. Each {@link QueryAct} in the returned list is a
 * possible rewrite of the given query act.
 * Created by Dai Wentao on 2017/6/26.
 */
public interface Annotator {

    String SLOT_PREFIX = "{{";
    String SLOT_SUFFIX = "}}";

    /**
     * Annotate a {@link QueryAct}, rewriting its {@link QueryAct#pQuery},
     * modifying its slots and calculating new score.
     *
     * @param queryAct The query act to be annotated.
     * @return A list of {@link QueryAct}, in which each {@link QueryAct} is a
     * possible rewrite of the given query act.
     */
    List<QueryAct> annotate(QueryAct queryAct);

}
