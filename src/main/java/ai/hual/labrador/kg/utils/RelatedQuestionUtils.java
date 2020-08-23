package ai.hual.labrador.kg.utils;

import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.pojo.ComplexEntityAndProperty;
import ai.hual.labrador.kg.pojo.RelatedQuestion;
import ai.hual.labrador.kg.pojo.SimpleEntityAndProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RelatedQuestionUtils {

    public static List<RelatedQuestion> getQuestions(String entityiri, KnowledgeAccessor knowledge) {
        List<RelatedQuestion> relatedQuestionsList = new ArrayList<>();
        List<ComplexEntityAndProperty> ComplexList = KnowledgeQueryUtils.queryComplexCombinations(entityiri, knowledge);
        for (ComplexEntityAndProperty ComplexObject : ComplexList) {
            String bniri = ComplexObject.getBn();
            String bnLabel = ComplexObject.getBnLabel();
            String dpLabel = ComplexObject.getDpLabel();
            String dp = ComplexObject.getDp();
            String cLabel = ComplexObject.getCLabel();
            String cClassLabel = ComplexObject.getcClassLabel();
            String ComplexQuestion;
            if (StringUtils.isNotEmpty(cLabel) && StringUtils.isNotEmpty(cClassLabel)) {
                ComplexQuestion = String.format("当%s是%s时%s的%s", cClassLabel, cLabel, bnLabel, dpLabel);
            } else {
                ComplexQuestion = String.format("%s的%s", bnLabel, dpLabel);
            }
            relatedQuestionsList.add(new RelatedQuestion(bniri, dp, dpLabel, ComplexQuestion));
        }

        List<SimpleEntityAndProperty> SimpleList = KnowledgeQueryUtils.querySimpleCombinations(entityiri, knowledge);
        for (SimpleEntityAndProperty SimpleObject : SimpleList) {
            String instanceiri = SimpleObject.getsIri();
            String sLabel = SimpleObject.getsLabel();
            String property = SimpleObject.getPropertyiri();
            String pLabel = SimpleObject.getpLabel();
            String SimpleQuestion = String.format("%s的%s", sLabel, pLabel);
            relatedQuestionsList.add(new RelatedQuestion(instanceiri, property, pLabel, SimpleQuestion));

        }
        return relatedQuestionsList;
    }
}
