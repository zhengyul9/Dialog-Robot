package ai.hual.labrador.train;

import ai.hual.labrador.exceptions.GenerateTrainDataException;
import ai.hual.labrador.train.utils.CorpusIntentData;
import ai.hual.labrador.train.utils.CorpusTrainData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorpusTrainDataGenerateTest {

    private List<CorpusIntentData> simplifyList;
    private static int times = 3;

    @BeforeEach
    void setUp() throws Exception {
        simplifyList = new ArrayList<>();
        List<String> intent1 = new ArrayList<>();
        intent1.add("11");
        simplifyList.add(new CorpusIntentData("c11", intent1));
        List<String> intent2 = new ArrayList<>();
        intent2.add("22");
        simplifyList.add(new CorpusIntentData("c21", intent2));
        simplifyList.add(new CorpusIntentData("c22", intent2));
        List<String> intent3 = new ArrayList<>();
        intent3.add("33");
        simplifyList.add(new CorpusIntentData("c31", intent3));
        simplifyList.add(new CorpusIntentData("c32", intent3));
        simplifyList.add(new CorpusIntentData("c33", intent3));
        simplifyList.add(new CorpusIntentData("c34", intent3));
        simplifyList.add(new CorpusIntentData("c35", intent3));
        List<String> intent4 = new ArrayList<>();
        intent4.add("44");
        intent4.add("33");
        simplifyList.add(new CorpusIntentData("c41", intent4));
        simplifyList.add(new CorpusIntentData("c42", intent4));
        simplifyList.add(new CorpusIntentData("c43", intent4));
        simplifyList.add(new CorpusIntentData("c44", intent4));
        simplifyList.add(new CorpusIntentData("c45", intent4));
    }

    @Test
    void generatePositiveData1() {

        List<CorpusIntentData> positiveTest1 = new ArrayList<>();
        positiveTest1.add(simplifyList.get(0));
        List<CorpusTrainData> positiveData = CorpusTrainDataGenerate.generatePositiveData(positiveTest1, times);
        assertEquals(positiveData.size(), 3);
        assertEquals(positiveData.get(0).getQuery(), positiveData.get(0).getTarget());
    }

    @Test
    void generatePositiveData2() {

        List<CorpusIntentData> positiveTest2 = new ArrayList<>();
        positiveTest2.add(simplifyList.get(1));
        positiveTest2.add(simplifyList.get(2));
        List<CorpusTrainData> positiveData = CorpusTrainDataGenerate.generatePositiveData(positiveTest2, times);
        assertEquals(positiveData.size(), 6);
        assert (!positiveData.get(0).getQuery().equals(positiveData.get(0).getTarget()));
    }

    @Test
    void generatePositiveData3() {

        List<CorpusIntentData> positiveTest3 = new ArrayList<>();
        positiveTest3.add(simplifyList.get(3));
        positiveTest3.add(simplifyList.get(4));
        positiveTest3.add(simplifyList.get(5));
        positiveTest3.add(simplifyList.get(6));
        positiveTest3.add(simplifyList.get(7));

        List<CorpusTrainData> positiveData = CorpusTrainDataGenerate.generatePositiveData(positiveTest3, times);
        assertEquals(positiveData.size(), 15);
    }

    @Test
    void generateTrainData() {
        List<CorpusTrainData> trainData = CorpusTrainDataGenerate.generateCorpusTrainData(simplifyList, times);
        // 因为多个意图的原因,这里数量增加的不是语料数量的times
        // (13+5)*3*2
        assertEquals(trainData.size(), 108);
    }

    @Test
    void generateNegativeData() {
        List<CorpusIntentData> negativeTest = new ArrayList<>();
        negativeTest.add(simplifyList.get(1));
        negativeTest.add(simplifyList.get(2));

        List<CorpusTrainData> negativeData = CorpusTrainDataGenerate.generateNegativeData(negativeTest, simplifyList, times);
        assertEquals(negativeData.size(), 6);
    }

    @Test
    void generateNegativeData2() {
        // 负例运行时间比较长主要是因为不同组的数量太少,真实数据不会出现这种现象
        List<CorpusIntentData> negativeTest2 = new ArrayList<>();
        negativeTest2.add(simplifyList.get(8));
        negativeTest2.add(simplifyList.get(9));
        negativeTest2.add(simplifyList.get(10));
        negativeTest2.add(simplifyList.get(11));
        negativeTest2.add(simplifyList.get(12));

        List<CorpusTrainData> negativeData = CorpusTrainDataGenerate.generateNegativeData(negativeTest2, simplifyList, times);
        List<String> corpusContents = new ArrayList<>();
        corpusContents.add("c11");
        corpusContents.add("c21");
        corpusContents.add("c22");
        for (CorpusTrainData negativeCorpus : negativeData) {
            assert (corpusContents.contains(negativeCorpus.getQuery()));
        }
        assertEquals(negativeData.size(), 15);
    }

    @Test
    void generateNegativeData3() {
        List<CorpusIntentData> negativeTest3 = new ArrayList<>();
        negativeTest3.add(simplifyList.get(8));
        negativeTest3.add(simplifyList.get(9));
        negativeTest3.add(simplifyList.get(10));
        negativeTest3.add(simplifyList.get(11));
        negativeTest3.add(simplifyList.get(12));
        negativeTest3.add(simplifyList.get(3));
        negativeTest3.add(simplifyList.get(4));
        negativeTest3.add(simplifyList.get(5));
        negativeTest3.add(simplifyList.get(6));
        negativeTest3.add(simplifyList.get(7));
        try {
            List<CorpusTrainData> negativeData = CorpusTrainDataGenerate.generateCorpusTrainData(negativeTest3, times);
        } catch (GenerateTrainDataException e) {
            System.out.println("无法构造负例！");
        }
    }

}