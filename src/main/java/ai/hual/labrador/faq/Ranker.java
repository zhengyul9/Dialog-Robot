package ai.hual.labrador.faq;

import ai.hual.labrador.nlu.resort.faq.FaqScores;

import java.util.List;
import java.util.Map;

/**
 * Ranker of faq.
 */
public interface Ranker {

    /**
     * Calculate scores of a query matching a list of questions.
     *
     * @param query         the query string
     * @param faqScoresList a list of of {@link FaqScores} objects.
     * @return A map with score name (e.g. bm25/embedDis/lstmDis) as key, and list of scores as value in which
     * each item is the score between query and the corresponding item in {@param faqScoresList}.
     */
    Map<String, List<Double>> rank(String query, List<FaqScores> faqScoresList);

}
