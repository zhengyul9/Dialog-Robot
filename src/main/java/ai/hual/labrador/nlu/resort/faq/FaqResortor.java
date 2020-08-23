package ai.hual.labrador.nlu.resort.faq;

import ai.hual.labrador.nlu.resort.faq.FaqScores.SortItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.resort.ResortUtils.BM25_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.BM25_WEIGHT;
import static ai.hual.labrador.nlu.resort.ResortUtils.EMBED_DIS_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.EMBED_DIS_WEIGHT;
import static ai.hual.labrador.nlu.resort.ResortUtils.LSTM_DIS_KEY;
import static ai.hual.labrador.nlu.resort.ResortUtils.LSTM_DIS_WEIGHT;

public class FaqResortor implements FaqResort {

    private static final int LIMIT = 10;
    private static Map<String, Double> DEFAULT_WEIGHTS = new HashMap<>();

    static {
        DEFAULT_WEIGHTS.put(BM25_KEY, BM25_WEIGHT);
        DEFAULT_WEIGHTS.put(EMBED_DIS_KEY, EMBED_DIS_WEIGHT);
        DEFAULT_WEIGHTS.put(LSTM_DIS_KEY, LSTM_DIS_WEIGHT);
    }

    private Map<String, Double> weights;
    private double totalWeights;

    public FaqResortor() {
        this(DEFAULT_WEIGHTS);
    }

    public FaqResortor(Map<String, Double> weights) {
        this.weights = weights == null || weights.isEmpty() ? DEFAULT_WEIGHTS : weights;
        totalWeights = Math.max(this.weights.values().stream().mapToDouble(x -> x).sum(), Double.MIN_VALUE);
    }

    @Override
    public List<FaqGroup> resort(List<FaqScores> faqScoresList) {

        // vote
//        for (String key : weights.keySet()) {
//            double max = faqScoresList.stream()
//                    .map(FaqScores::getScores)
//                    .map(x -> x.get(key))
//                    .filter(Objects::nonNull)
//                    .mapToDouble(x -> x)
//                    .max().orElse(0);
//            for (FaqScores faqScores : faqScoresList) {
//                faqScores.getScores().put(key, faqScores.getScores().get(key) == max ? 1d : 0d);
//            }
//        }

        // horizontal merge scores
        List<SortItem> items = faqScoresList.stream()
                .map(x -> x.toSortItem(weights, totalWeights))
                .sorted((SortItem a, SortItem b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
        for (int i = LIMIT; i < items.size(); i++)
            items.get(i).setScore(0);
        Map<Integer, FaqGroup> groups = new HashMap<>();
        // vertically merge scores by grouping items with same qaid
        for (SortItem item : items) {
            int key = item.getItem().getQaid();
            if (!groups.containsKey(key)) {
                groups.put(key, new FaqGroup(key));
            }
            FaqGroup group = groups.get(key);
            group.setCategory(item.getItem().getCategory());
            group.addQuestion(item.getItem().getQuestion(), item.getItem().getQid(), item.getScore());
        }
        List<FaqGroup> sortedGroups = new ArrayList<>(groups.values());
        sortedGroups.sort((FaqGroup a, FaqGroup b) -> Double.compare(b.getScore(), a.getScore()));

        return sortedGroups;
    }

}
