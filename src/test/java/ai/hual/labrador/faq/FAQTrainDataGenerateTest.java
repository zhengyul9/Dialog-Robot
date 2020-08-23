package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.FAQTrainData;
import ai.hual.labrador.faq.utils.QuestionSimplify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FAQTrainDataGenerateTest {
    private List<QuestionSimplify> simplifyList;
    private static int times = 3;

    @BeforeEach
    void setUp() throws Exception {
        simplifyList = new ArrayList<>();
        simplifyList.add(new QuestionSimplify("c11", 11));

        simplifyList.add(new QuestionSimplify("c21", 22));
        simplifyList.add(new QuestionSimplify("c22", 22));

        simplifyList.add(new QuestionSimplify("c31", 33));
        simplifyList.add(new QuestionSimplify("c32", 33));
        simplifyList.add(new QuestionSimplify("c33", 33));
        simplifyList.add(new QuestionSimplify("c34", 33));
        simplifyList.add(new QuestionSimplify("c35", 33));
    }

    @Test
    void generatePositiveData1() {

        List<QuestionSimplify> positiveTest1 = new ArrayList<>();
        positiveTest1.add(simplifyList.get(0));
        List<FAQTrainData> positiveData = FAQTrainDataGenerate.generatePositiveData(positiveTest1, times);
        assertEquals(positiveData.size(), 3);
    }

    @Test
    void generatePositiveData2() {

        List<QuestionSimplify> positiveTest2 = new ArrayList<>();
        positiveTest2.add(simplifyList.get(1));
        positiveTest2.add(simplifyList.get(2));
        List<FAQTrainData> positiveData = FAQTrainDataGenerate.generatePositiveData(positiveTest2, times);
        assertEquals(positiveData.size(), 6);
        assert (!positiveData.get(0).getQuery().equals(positiveData.get(0).getTarget()));
    }

    @Test
    void generatePositiveData3() {

        List<QuestionSimplify> positiveTest3 = new ArrayList<>();
        positiveTest3.add(simplifyList.get(3));
        positiveTest3.add(simplifyList.get(4));
        positiveTest3.add(simplifyList.get(5));
        positiveTest3.add(simplifyList.get(6));
        positiveTest3.add(simplifyList.get(7));

        List<FAQTrainData> positiveData = FAQTrainDataGenerate.generatePositiveData(positiveTest3, times);
        assertEquals(positiveData.size(), 15);
    }

    @Test
    void generateTrainData() {
        List<FAQTrainData> trainData = FAQTrainDataGenerate.generateTrainData(simplifyList, times);
        assertEquals(trainData.size(), 48);
    }

    @Test
    void generateNegativeData() {
        // 负例运行时间比较长主要是因为不同组的数量太少,真实数据不会出现这种现象
        List<QuestionSimplify> negativeTest = new ArrayList<>();
        negativeTest.add(simplifyList.get(1));
        negativeTest.add(simplifyList.get(2));

        List<FAQTrainData> negativeData = FAQTrainDataGenerate.generateNegativeData(negativeTest, simplifyList, 22, times);
        assertEquals(negativeData.size(), 6);
    }

}