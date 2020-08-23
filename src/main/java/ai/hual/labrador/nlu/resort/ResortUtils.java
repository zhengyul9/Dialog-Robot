package ai.hual.labrador.nlu.resort;

import ai.hual.labrador.utils.StringUtils;

import java.util.List;

public class ResortUtils {
    public static final double FAQ_BETA = 0.9;
    public static final double INTENT_BETA = 0.9;

    public static final String BM25_KEY = "bm25";
    public static final String SELF_BM25_KEY = "selfBm25";
    public static final String EMBED_DIS_KEY = "embedDis";
    public static final String LSTM_DIS_KEY = "lstmDis";
    public static final String QUERY_SCORE_KEY = "queryScore";

    public static final double BM25_WEIGHT = 1.0;
    public static final double EMBED_DIS_WEIGHT = 0.0;
    public static final double LSTM_DIS_WEIGHT = 0.0;

    private static final String STOP_CHAR_FILE = "stop_char.txt";

    private static List<String> stopChars = new StringUtils().fetchWords(STOP_CHAR_FILE);

    public static boolean exactMatch(String query, String question) {
        return removeStopChar(query).equals(removeStopChar(question));
    }

    private static String removeStopChar(String query) {
        for (String c : stopChars)
            query = query.replaceAll("\\\\" + c, "");
        return query;
    }
}
