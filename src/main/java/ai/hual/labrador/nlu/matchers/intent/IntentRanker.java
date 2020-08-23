package ai.hual.labrador.nlu.matchers.intent;

import ai.hual.labrador.nlu.resort.intent.IntentScores;

import java.util.List;
import java.util.Map;

/**
 * Ranker of intent.
 */
public interface IntentRanker {

    /**
     * Calculate scores of a query matching a list of questions.
     *
     * @param intentScoresCompoundList a list of list of {@link IntentScores} objects. each List&lt;IntentScores&gt;
     *                                 index i is the scores of queries.get(i).
     * @return A list with the same size as queries, containing map with score name (e.g. bm25/embedDis/lstmDis) as key,
     * and list of scores as value in which each item is the score between query and the corresponding item in
     * {@param intentScoresCompoundList}.
     */
    List<Map<String, List<Double>>> rank(List<List<IntentScores>> intentScoresCompoundList);

}

