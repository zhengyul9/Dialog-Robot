package ai.hual.labrador.train;


import ai.hual.labrador.exceptions.GenerateTrainDataException;
import ai.hual.labrador.train.utils.CorpusIntentData;
import ai.hual.labrador.train.utils.CorpusTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CorpusTrainDataGenerate {
    private static int seed = 100;
    private final static Logger logger = LoggerFactory.getLogger(CorpusTrainDataGenerate.class);

    /**
     * 通过随机语料, 构造正例,并控制数量为size×times
     *
     * @param similarityCorpusGroup 一组包含相同意图的组
     * @param times                 生成训练数据数量是原来语料数量的倍数
     * @return 正例数据
     */
    public static List<CorpusTrainData> generatePositiveData(List<CorpusIntentData> similarityCorpusGroup, Integer times) {
        List<CorpusTrainData> positiveData = new ArrayList<>();
        // 没有为空的情况，否则不可能传进来
        // 只有一条数据的处理做的不是很好
        if (similarityCorpusGroup.size() == 1) {
            // 这种情况其实很少，不得已用了这种取巧的方法
            String content = similarityCorpusGroup.get(0).getCorpusContent();
            for (int i = 0; i < times; i++) {
                positiveData.add(new CorpusTrainData(1, content, content));
            }
            return positiveData;
        }

        for (int i = 0; i < similarityCorpusGroup.size(); i++) {
            for (int j = 0; j < similarityCorpusGroup.size(); j++) {
                if (i < j) {
                    positiveData.add(new CorpusTrainData(1, similarityCorpusGroup.get(i).getCorpusContent(), similarityCorpusGroup.get(j).getCorpusContent()));
                }
            }
        }
        // 因为训练可能会以某个固定的随机顺序，避免一些难以预料的情形
        Random random = new Random(seed);
        Collections.shuffle(positiveData, random);
        int currentSize = positiveData.size();
        int requireSize = similarityCorpusGroup.size() * times;
        // 随机抽取补齐至size×times
        for (int i = 0; i < requireSize - currentSize; i++) {
            positiveData.add(positiveData.get(random.nextInt(currentSize)));
        }
        return positiveData.subList(0, requireSize);
    }

    /**
     * 通过随机然后判断是否不包含当前相同意图,构造负例
     *
     * @param similarityCorpusGroup 一组包含相同意图的组
     * @param allCorpusGroup        CorpusGroup的所有语料
     * @param times                 生成训练数据数量是原来语料数量的倍数
     * @return 负例数据
     */

    public static List<CorpusTrainData> generateNegativeData(List<CorpusIntentData> similarityCorpusGroup, List<CorpusIntentData> allCorpusGroup, Integer times) {
        List<CorpusTrainData> negativeData = new ArrayList<>();
        // 随机负例
        int randomRange = allCorpusGroup.size();
        Random random = new Random(seed);
        for (CorpusIntentData corpus : similarityCorpusGroup) {
            for (int i = 0; i < times; ) {
                int k = random.nextInt(randomRange);
                boolean notSameIntent = true;
                // 有相同意图的语料不应当构造成负例
                for (String intent : corpus.getIntents()) {
                    if (allCorpusGroup.get(k).getIntents().contains(intent)) {
                        notSameIntent = false;
                    }
                }
                if (notSameIntent) {
                    negativeData.add(new CorpusTrainData(0, corpus.getCorpusContent(), allCorpusGroup.get(k).getCorpusContent()));
                    i++;
                }
            }
        }
        return negativeData;
    }

    /**
     * 一个提供重新生成某个bot的所有语料训练数据
     *
     * @param allCorpusGroup 某个bot下的所有语料
     * @param times          倍数
     * @return 语料训练数据
     */
    public static List<CorpusTrainData> generateCorpusTrainData(List<CorpusIntentData> allCorpusGroup, Integer times) {
        List<CorpusTrainData> corpusTrainData = new ArrayList<>();
        //logger.debug("语料数：{}",allCorpusGroup.size());
        // 根据intent分组
        Map<String, List<CorpusIntentData>> prepareMapData = new HashMap<>();
        for (CorpusIntentData Corpus : allCorpusGroup) {
            for (String intent : Corpus.getIntents())
                if (prepareMapData.containsKey(intent)) {
                    prepareMapData.get(intent).add(Corpus);
                } else {
                    List<CorpusIntentData> tempCorpusList = new ArrayList<>();
                    tempCorpusList.add(Corpus);
                    prepareMapData.put(intent, tempCorpusList);
                }
        }
        if (prepareMapData.size() == 1) return corpusTrainData;
        //logger.debug("同意图组数：{}", prepareMapData.size());
        //复杂度是组数的k^2*n 而不是k*n^2
        for (Map.Entry<String, List<CorpusIntentData>> similarityCorpusEntry : prepareMapData.entrySet()) {
            //正例
            List<CorpusTrainData> positiveData = generatePositiveData(similarityCorpusEntry.getValue(), times);
            corpusTrainData.addAll(positiveData);
        }
        //int mount = corpusTrainData.size();
        //logger.debug("正例数量：{}", mount);
        // 一个不够明智的检验
        for (Map.Entry<String, List<CorpusIntentData>> similarityCorpusEntry : prepareMapData.entrySet()) {
            if (similarityCorpusEntry.getValue().size() == allCorpusGroup.size()) {
                throw new GenerateTrainDataException("每条语料有一个相同的意图，无法构造负例！");
            }
        }
        for (Map.Entry<String, List<CorpusIntentData>> similarityCorpusEntry : prepareMapData.entrySet()) {
            //负例
            corpusTrainData.addAll(generateNegativeData(similarityCorpusEntry.getValue(), allCorpusGroup, times));
        }
        //logger.debug("负例数量：{}", corpusTrainData.size() - mount);
        Random random = new Random(seed);
        Collections.shuffle(corpusTrainData, random);
        return corpusTrainData;
    }
}
