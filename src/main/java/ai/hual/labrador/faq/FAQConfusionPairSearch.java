package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.HitsEntity;
import ai.hual.labrador.faq.utils.IntegerPair;
import ai.hual.labrador.faq.utils.QidPairRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanlei on 2018/2/27.
 */
public class FAQConfusionPairSearch {
    private static final int confusionMax = 50;
    private static double[] integer2function = new double[customFunction(confusionMax) + 1];
    // 如何让threshold自适应呢
    private static final double classThreshold2 = 0.33;
    private static final double classThreshold1 = 0.18;

    static {
        // 为了减轻计算量,提前计算前50个值,相当于cache的作用
        for (int i = 1; i < customFunction(confusionMax) + 1; i++) {
            integer2function[i] = Math.log(i);
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(FAQConfusionPairSearch.class);

    /**
     * 通过遍历一组问题ES返回的hitsList,hitsList是用一个标准问题及其相似问题搜索到的问题(包括其它标准问题和相似问题),
     * 其中包括qaid和rqaid映射关系,以及qid和多个id映射关系(命名在代码中有解释)
     * 其中qaid是一组问题的标识,也是标准问题的id,rqaid即return qaid,是es返回结果混淆组qaid,
     * qid是question id即当前查找问题的id,id是ES返回结果的某个问题的id
     * <p>
     * 暂时minSize等于1,没有起到它的作用，因为它的设定取决于线上的效果
     *
     * @param hitsList  通过es查找获得的多组HitsEntity,其中第一组总是标准问题组
     * @param minSize   返回结果PairRelateNode的size最小值,应为正整数
     * @param groupSize group的size
     * @return 一个复杂点的组合
     */
    public static List<QidPairRelation> pairSearch(List<HitsEntity> hitsList, Integer groupSize, Integer minSize) {
        Map<IntegerPair, QidPairRelation> mapResults = new HashMap<>();
        int correctNum = 0;
        Integer qaid = hitsList.get(0).getQid();    // 第一组的qid总是标准问题id
        int limit = Math.min(groupSize * 2, confusionMax);
        // 按照qaid->rqaid组与组的映射关系存储结果
        for (HitsEntity aHitsList : hitsList) {
            Integer qid = aHitsList.getQid();   // qaid的相似问题的id
            List<IntegerPair> hits = aHitsList.getHits();
            for (int j = 0; j < Math.min(limit, hits.size()); j++) {
                IntegerPair hit = hits.get(j);
                Integer rqaid = hit.getLatter();    // qaid或其相似问题匹配到的问题的标准问题id
                Integer id = hit.getFormer();   // rqaid的相似问题
                if (qaid.equals(rqaid)) {   // 如果相似问题属于标准问题qaid,则不算混淆
                    correctNum++;
                } else {
                    // 在mapResults中添加标准问题组之间的映射关系
                    IntegerPair tempPair = new IntegerPair(qaid, rqaid);
                    if (mapResults.containsKey(tempPair)) {
                        QidPairRelation relateNodes = mapResults.get(tempPair);
                        relateNodes.setSize(relateNodes.getSize() + 1);
                    } else {
                        QidPairRelation relateNodes = new QidPairRelation();
                        relateNodes.setSize(1);
                        mapResults.put(tempPair, relateNodes);
                    }
                    // 在对应的Node下添加qid与多个id的映射关系,即相似问题之间的关系
                    QidPairRelation relateNodes = mapResults.get(tempPair);
                    if (relateNodes.getMapping() != null) {
                        Map<Integer, List<Integer>> mapping = relateNodes.getMapping();
                        if (mapping.containsKey(qid)) {
                            mapping.get(qid).add(id);
                        } else {
                            List<Integer> list = new ArrayList<>();
                            list.add(id);
                            mapping.put(qid, list);
                        }
                        relateNodes.setMapping(mapping);
                    } else {
                        Map<Integer, List<Integer>> mapping = new HashMap<>();
                        List<Integer> list = new ArrayList<>();
                        list.add(id);
                        mapping.put(qid, list);
                        relateNodes.setMapping(mapping);
                    }
                }
            }
        }
        //logger.debug("正确数: {}",correctNum);
        // 筛掉混淆数量(相似问题之间)小于minSize的标准问题组,合理的减少返回结果数量
        if (minSize > 1)
            mapResults.entrySet().removeIf(entry -> entry.getValue().getSize() < minSize);

        // 转成List并计算arctanValueSum
        List<QidPairRelation> listResults = new ArrayList<>();
        for (Map.Entry<IntegerPair, QidPairRelation> entry : mapResults.entrySet()) {
            QidPairRelation entryValues = entry.getValue();
            entryValues.setParentId(entry.getKey());
            double scores = 0;
            for (Map.Entry<Integer, List<Integer>> singleMapping : entryValues.getMapping().entrySet()) {
                // 严谨的处理下微可能的越界
                scores += integer2function[customFunction(singleMapping.getValue().size())]
                        / integer2function[customFunction(limit)];// there is some effect on limit
            }
            entryValues.setScore(scores / groupSize);
            Integer grade = getGrade(entryValues.getScore());
            if (grade > -1) {
                entryValues.setGrade(grade);
                listResults.add(entryValues);
            }

        }
        //logger.debug("结果： \n{}",listResults);
        return listResults;
    }

    private static Integer customFunction(int number) {
        return number * 2 + 1;
    }

    /**
     * 根据arctanValueSum 划分等级
     *
     * @param arctanValueSum 0到1的值
     * @return 1, 0,-1
     */
    private static Integer getGrade(double arctanValueSum) {
        if (arctanValueSum >= classThreshold1) {
            if (arctanValueSum >= classThreshold2) return 1;
            return 0;
        }
        return -1;
    }

    /**
     * 计算健康度,初始值不能有为零的值
     *
     * @param grade2     当前grade为2的数目
     * @param grade1     当前grade为1的数目
     * @param initGrade2 初始grade为2的数目
     * @param initGrade1 初始grade为1的数目
     * @return 0-100
     */
    public static double computeOverallHealthValue(int grade2, int grade1, int initGrade2, int initGrade1) {
        double overallHealth = (double) grade2 / initGrade2 * (1 - classThreshold2) +
                (double) grade1 / initGrade1 * (classThreshold2 - classThreshold1);
        if (overallHealth > 1.0) return 0.0;
        if (overallHealth < 0.0) return 100D;
        return (1 - overallHealth) * 100;
    }

    /**
     * 健康度初始,视为数据噪音,暂时忽视了该值的动态变化
     *
     * @return 0-100
     */
    public static Double initOverallHealth() {
        return classThreshold1 * 100;
    }

}