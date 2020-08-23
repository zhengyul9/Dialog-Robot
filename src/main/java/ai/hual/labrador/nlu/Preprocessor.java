package ai.hual.labrador.nlu;

/**
 * Preprocessor for NLU
 * Created by Dai Wentao on 2017/7/13.
 */
public interface Preprocessor {

    /**
     * preprocess a query
     *
     * @param query The original query or the query preprocessed by other preprocessor
     * @return processed query.
     */
    String preprocess(String query);

}
