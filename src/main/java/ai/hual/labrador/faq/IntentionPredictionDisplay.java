package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.IntentionDisplayInfo;

import java.util.List;
import java.util.stream.Collectors;

public class IntentionPredictionDisplay {

    /**
     * 给定意图，将若干条待标记的问题进行排序，以更好地显示给用户。
     *
     * @param questions 　待排序的问题列表
     * @return 排序后的问题列表
     */
    public static List<Integer> sortCandidateQuestions(List<IntentionDisplayInfo> questions) {
        // 目前排序方法是简单地将三种得分全部加起来．有待讨论
        questions.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
        return questions.stream()
                .map(IntentionDisplayInfo::getQid)
                .collect(Collectors.toList());
    }

}
