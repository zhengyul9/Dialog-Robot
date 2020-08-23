package ai.hual.labrador.faq;

import ai.hual.labrador.nlu.resort.faq.FaqScores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple Ranker of faq. compare only one query with one questions.
 * P is the result type of pre-processed query.
 * Q is the result type of pre-processed question
 */
public abstract class SimpleRanker<P, Q> implements Ranker {

    /**
     * Calculate scores of a query matching a question.
     *
     * @param query    the query string
     * @param question the question string
     * @return A map with score name (e.g. bm25/embedDis/lstmDis) as key, and a score as value in which
     */
    protected abstract Map<String, Double> rank(P query, Q question);

    /**
     * pre-process query
     *
     * @param query the query string
     * @return the pre-processed text
     */
    protected abstract P processQuery(String query);

    /**
     * pre-process question
     *
     * @param question the query string
     * @return the pre-processed text
     */
    protected abstract Q processQuestion(String question);

    @Override
    public Map<String, List<Double>> rank(String rawQuery, List<FaqScores> faqScoresList) {
        Map<String, List<Double>> result = new HashMap<>();
        P query = processQuery(rawQuery);
        for (FaqScores faqScores : faqScoresList) {
            Q question = processQuestion(faqScores.getQuestion());
            for (Map.Entry<String, Double> entry : rank(query, question).entrySet()) {
                result.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue());
            }
        }
        return result;
    }

}
