package ai.hual.labrador.nlu.resort.intent;

import ai.hual.labrador.nlu.resort.intent.IntentScores.SortItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.resort.ResortUtils.BM25_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.BM25_WEIGHT;
import static ai.hual.labrador.nlu.resort.ResortUtils.EMBED_DIS_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.EMBED_DIS_WEIGHT;
import static ai.hual.labrador.nlu.resort.ResortUtils.INTENT_BETA;
import static ai.hual.labrador.nlu.resort.ResortUtils.LSTM_DIS_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.LSTM_DIS_WEIGHT;

public class IntentResortor implements IntentResort {

    private static final int LIMIT = 10;
    private static Map<String, Double> DEFAULT_WEIGHTS = new HashMap<>();

    static {
        DEFAULT_WEIGHTS.put(BM25_KEY, BM25_WEIGHT);
        DEFAULT_WEIGHTS.put(EMBED_DIS_KEY, EMBED_DIS_WEIGHT);
        DEFAULT_WEIGHTS.put(LSTM_DIS_KEY, LSTM_DIS_WEIGHT);
    }

    private Map<String, Double> weights;

    private double beta;

    public IntentResortor() {
        this.weights = DEFAULT_WEIGHTS;
        this.beta = INTENT_BETA;
    }

    public IntentResortor(Map<String, Double> weights) {
        this.weights = weights == null || weights.isEmpty() ? DEFAULT_WEIGHTS : weights;
        this.beta = INTENT_BETA;
    }

    public IntentResortor(Map<String, Double> weights, double beta) {
        this.weights = weights == null || weights.isEmpty() ? DEFAULT_WEIGHTS : weights;
        this.beta = beta;
    }

    @Override
    public List<IntentGroup> resort(List<IntentScores> intentScoresList) {
        // horizontally merge scores
        List<SortItem> items = intentScoresList.stream()
                .map(x -> x.toSortItem(weights))
                .collect(Collectors.toList());
        items.sort((SortItem a, SortItem b) -> Double.compare(b.score, a.score));
        items = items.subList(0, Math.min(LIMIT, items.size()));
        Map<String, IntentGroup> groups = new HashMap<>();
        double modifier = 1;
        // vertically merge scores by grouping items with same intent
        for (SortItem item : items) {
            // identical intents across query will not be grouped, only those of same query will be grouped
            if (!groups.containsKey(item.item.intent)) {
                groups.put(item.item.intent, new IntentGroup(item.item.intent, item.item.query));
            }
            groups.get(item.item.intent).addScore(item.score * modifier);
            groups.get(item.item.intent).getSimQuestion().add(item.item.getQuestion());
            modifier *= this.beta; // 权重不一致
        }
        List<IntentGroup> sortedGroups = new ArrayList<>(groups.values());
        sortedGroups.sort((IntentGroup a, IntentGroup b) -> Double.compare(b.getScore(), a.getScore()));

        return sortedGroups;
    }

}
