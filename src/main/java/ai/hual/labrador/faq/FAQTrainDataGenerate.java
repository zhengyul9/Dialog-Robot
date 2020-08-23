package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.FAQTrainData;
import ai.hual.labrador.faq.utils.QuestionSimplify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class FAQTrainDataGenerate {

    private static int seed = 100;
    private final static Logger logger = LoggerFactory.getLogger(FAQTrainDataGenerate.class);

    /**
     * 通过随机相似问题, 构造正例,并控制数量为size×times
     *
     * @param similarityQuestionGroup 一组相似问题
     * @param times                   生成训练数据数量是原来问题数量的倍数
     * @return 正例数据
     */
    public static List<FAQTrainData> generatePositiveData(List<QuestionSimplify> similarityQuestionGroup, Integer times) {
        List<FAQTrainData> positiveData = new ArrayList<>();
        // 没有为空的情况，否则不可能传进来
        // 只有一条数据的处理做的不是很好
        if (similarityQuestionGroup.size() == 1) {
            // 这种情况其实很少，不得已用了这种取巧的方法
            String content = similarityQuestionGroup.get(0).getContent();
            for (int i = 0; i < times; i++) {
                positiveData.add(new FAQTrainData(1, content, content));
            }
            return positiveData;
        }

        for (int i = 0; i < similarityQuestionGroup.size(); i++) {
            for (int j = 0; j < similarityQuestionGroup.size(); j++) {
                if (i < j) {
                    positiveData.add(new FAQTrainData(1, similarityQuestionGroup.get(i).getContent(), similarityQuestionGroup.get(j).getContent()));
                }
            }
        }
        // 因为训练可能会以某个固定的随机顺序，避免一些难以预料的情形
        Random random = new Random(seed);
        Collections.shuffle(positiveData, random);
        int currentSize = positiveData.size();
        int requireSize = similarityQuestionGroup.size() * times;
        // 随机抽取补齐至size×times
        for (int i = 0; i < requireSize - currentSize; i++) {
            positiveData.add(positiveData.get(random.nextInt(currentSize)));
        }
        return positiveData.subList(0, requireSize);
    }

    /**
     * 利用数据库的数据,构造负例
     *
     * @param similarityQuestionGroup 一组相似问题
     * @param allQuestionGroup        QuestionGroup的所有问题组
     * @param qaid                    当前组的qaid
     * @param times                   生成训练数据数量是原来问题数量的倍数
     * @return 负例数据
     */

    public static List<FAQTrainData> generateNegativeData(List<QuestionSimplify> similarityQuestionGroup, List<QuestionSimplify> allQuestionGroup, Integer qaid, Integer times) {
        List<FAQTrainData> negativeData = new ArrayList<>();

        // 随机负例
        int randomRange = allQuestionGroup.size();
        Random random = new Random(seed);
        for (QuestionSimplify question : similarityQuestionGroup) {
            for (int i = 0; i < times; ) {
                int k = random.nextInt(randomRange);
                if (allQuestionGroup.get(k).getQaid() != qaid) {
                    negativeData.add(new FAQTrainData(0, question.getContent(), allQuestionGroup.get(k).getContent()));
                    i++;
                }
            }
        }
        return negativeData;
    }

    /**
     * 一个提供重新生成某个bot的所有训练数据
     *
     * @param allQuestionGroup 某个bot下的所有相似问题组
     * @param times            倍数
     * @return 训练数据
     */
    public static List<FAQTrainData> generateTrainData(List<QuestionSimplify> allQuestionGroup, Integer times) {
        List<FAQTrainData> trainData = new ArrayList<>();
        //logger.debug("相似问题数：{}",allQuestionGroup.size());
        // 提高遍历效率，前提组数远小于问题数 且组数小于2000
        Map<Integer, List<QuestionSimplify>> prepareMapData = new HashMap<>();
        for (QuestionSimplify question : allQuestionGroup) {
            if (prepareMapData.containsKey(question.getQaid())) {
                prepareMapData.get(question.getQaid()).add(question);
            } else {
                List<QuestionSimplify> tempQuestionList = new ArrayList<>();
                tempQuestionList.add(question);
                prepareMapData.put(question.getQaid(), tempQuestionList);
            }
        }
        if (prepareMapData.size() == 1) return trainData;
        //logger.debug("相似问题组数：{}",prepareMapData.size());
        //复杂度是组数的k^2*n 而不是k*n^2
        for (Map.Entry<Integer, List<QuestionSimplify>> similarityQuestionEntry : prepareMapData.entrySet()) {
            //正例
            List<FAQTrainData> positiveData = generatePositiveData(similarityQuestionEntry.getValue(), times);
            trainData.addAll(positiveData);
        }
        //int mount = trainData.size();
        //logger.debug("正例数量：{}",mount);
        for (Map.Entry<Integer, List<QuestionSimplify>> similarityQuestionEntry : prepareMapData.entrySet()) {
            //负例
            trainData.addAll(generateNegativeData(similarityQuestionEntry.getValue(), allQuestionGroup, similarityQuestionEntry.getKey(), times));
        }
        //logger.debug("负例数量：{}",trainData.size() - mount);
        Random random = new Random(seed);
        Collections.shuffle(trainData, random);
        return trainData;
    }

}
