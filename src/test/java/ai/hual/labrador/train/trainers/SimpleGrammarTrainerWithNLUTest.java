package ai.hual.labrador.train.trainers;

import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.NLUImpl;
import ai.hual.labrador.train.Corpus;
import ai.hual.labrador.train.CorpusAnnotation;
import ai.hual.labrador.train.Data;
import ai.hual.labrador.train.TrainTag;
import ai.hual.labrador.train.Trainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleGrammarTrainerWithNLUTest {

    private static SimpleGrammarTrainerWithNLU SGTrainer;
    private static DictModel dictModel;
    private static NLUImpl nlu;
    private static Properties properties;

    @BeforeAll
    static void setup() {
        properties = new Properties();
        properties.put("nlu.intentMatchers", "templateIntentMatcher");
        List<String> annotators = Arrays.asList(
                "dictAnnotator",
                "numAnnotator",
                "dateAnnotator",
                "timeAnnotator",
                "timeDurationAnnotator");
        dictModel = new DictModel(Arrays.asList(
                new Dict("电视节目", "周六夜现场"),
                new Dict("电视节目", "权力的游戏"),
                new Dict("Ab", "ab")
        ));
        nlu = new NLUImpl(dictModel, new GrammarModel(), new AccessorRepositoryImpl(),
                annotators, properties);
        SGTrainer = new SimpleGrammarTrainerWithNLU(nlu);
    }

    @Test
    public void avoidRepeatTrainTest() {

        String content1 = "给我播放个九月十五日的周六夜现场";
        String content2 = "给我播放个上个月的权力的游戏";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("播放", TrainTag.POSITIVE);
        Corpus corpus1 = new Corpus(intentMap, content1, new ArrayList<>());
        Corpus corpus2 = new Corpus(intentMap, content2, new ArrayList<>());
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus1);
        corpora.add(corpus2);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(1, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals("播放", grammarList.get(0).getLabel());
        assertEquals("给我播放个.*{{日期}}.*的.*{{电视节目}}", grammarList.get(0).getContent());
    }

    @Test
    public void trainTest() {

        String content = "给我播放个九月十五日的周六夜现场";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("播放", TrainTag.POSITIVE);
        Corpus corpus = new Corpus(intentMap, content, new ArrayList<>());
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(1, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals("播放", grammarList.get(0).getLabel());
        assertEquals("给我播放个.*{{日期}}.*的.*{{电视节目}}", grammarList.get(0).getContent());
    }

    @Test
    public void contentToIntentRegexTest() {

        String content = "给我播放个九月十五日的周六夜现场";
        String result = SGTrainer.contentToIntentRegex(nlu, content);

        assertEquals("给我播放个.*{{日期}}.*的.*{{电视节目}}", result);
    }

    @Test
    public void testAttachedParameters() {
        Trainer<GrammarModel> trainer = new SimpleGrammarTrainer();
        String content = "到期不还会怎么着";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("后果", TrainTag.POSITIVE);
        Corpus corpus = new Corpus(intentMap, content, Collections.singletonList(
                new CorpusAnnotation(0, 0, "trie_local/dict", "贷后", "借款逾期")
        ));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus);
        Data data = new Data(corpora);

        GrammarModel result = trainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(1, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals("后果?贷后=借款逾期", grammarList.get(0).getLabel());
        assertEquals("到期不还会怎么着", grammarList.get(0).getContent());
    }

    @Test
    public void testAttachedParametersWithParameters() {
        Trainer<GrammarModel> trainer = new SimpleGrammarTrainer();
        String content = "到期不还会怎么着";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("查询?属性=后果", TrainTag.POSITIVE);
        Corpus corpus = new Corpus(intentMap, content, Collections.singletonList(
                new CorpusAnnotation(0, 0, "trie_local/dict", "贷后", "借款逾期")
        ));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus);
        Data data = new Data(corpora);

        GrammarModel result = trainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(1, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals("查询?属性=后果&贷后=借款逾期", grammarList.get(0).getLabel());
        assertEquals("到期不还会怎么着", grammarList.get(0).getContent());
    }
}

