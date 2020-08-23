package ai.hual.labrador.train.trainers;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.train.Corpus;
import ai.hual.labrador.train.CorpusAnnotation;
import ai.hual.labrador.train.Data;
import ai.hual.labrador.train.TrainTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleGrammarTrainerTest {

    private static SimpleGrammarTrainer SGTrainer;

    @BeforeAll
    static void setup() {
        SGTrainer = new SimpleGrammarTrainer();
    }

    @Test
    public void regexPervertPatternTest() {

        String content1 = "ab{{cd}}?.***eftxb{tts(da(x]";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("A", TrainTag.POSITIVE);
        CorpusAnnotation annotation1 = new CorpusAnnotation(14, 16, "trie_local/dict", "xxx"); // ft
        CorpusAnnotation annotation2 = new CorpusAnnotation(19, 22, "trie_local/dict", "yy");  // tts
        Corpus corpus1 = new Corpus(intentMap, content1, new ArrayList<>(Arrays.asList(annotation1, annotation2)));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus1);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();
        assertEquals("ab\\{\\{cd}}\\?\\.\\*\\*\\*e.*{{xxx}}.*xb\\{.*{{yy}}.*\\(da\\(x]",
                grammarList.get(0).getContent());
    }

    @Test
    public void regexInPatternTest() {

        String content1 = "您的好友**\\**逾期欠款已.*全部{{清偿}}";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("逾期信息", TrainTag.POSITIVE);
        CorpusAnnotation annotation1 = new CorpusAnnotation(0, 0, "trie_local/dict",
                "逾期信息_c", "逾期信息");
        Corpus corpus1 = new Corpus(intentMap, content1, new ArrayList<>(Arrays.asList(annotation1)));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus1);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();
        assertEquals("您的好友\\*\\*\\\\\\*\\*逾期欠款已\\.\\*全部\\{\\{清偿}}", grammarList.get(0).getContent());
    }

    @Test
    public void avoidRepeatTrainTest() {

        String content1 = "给我播放个{{日期}}的{{program}}";
        String content2 = "给我播放个{{日期}}的{{tv}}";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("播放", TrainTag.POSITIVE);
        CorpusAnnotation annotation1 = new CorpusAnnotation(5, 11, "trie_local/dict", "日期");
        CorpusAnnotation annotation2 = new CorpusAnnotation(12, 23, "trie_local/dict", "program");
        CorpusAnnotation annotation3 = new CorpusAnnotation(12, 18, "trie_local/dict", "tv");
        Corpus corpus1 = new Corpus(intentMap, content1,
                new ArrayList<>(Arrays.asList(annotation1, annotation2)));
        Corpus corpus2 = new Corpus(intentMap, content2,
                new ArrayList<>(Arrays.asList(annotation1, annotation3)));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus1);
        corpora.add(corpus2);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(2, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(1).getType());
        assertEquals("播放?", grammarList.get(0).getLabel());
        assertEquals("播放?", grammarList.get(1).getLabel());
        assertEquals("播放个.*{{日期}}.*的.*{{program}}", grammarList.get(0).getContent());
        assertEquals("播放个.*{{日期}}.*的.*{{tv}}", grammarList.get(1).getContent());
    }

    @Test
    public void avoidConsecutiveWildcardTest() {

        String content1 = "{{日期}}给我{{actor}}的{{movie}}";
        Map<String, TrainTag> intentMap = new HashMap<>();
        intentMap.put("播放", TrainTag.POSITIVE);
        CorpusAnnotation annotation1 = new CorpusAnnotation(0, 6, "trie_local/dict", "日期");
        CorpusAnnotation annotation2 = new CorpusAnnotation(8, 17, "trie_local/dict", "actor");
        CorpusAnnotation annotation3 = new CorpusAnnotation(18, 27, "trie_local/dict", "movie");
        Corpus corpus1 = new Corpus(intentMap, content1,
                new ArrayList<>(Arrays.asList(annotation1, annotation2, annotation3)));
        List<Corpus> corpora = new ArrayList<>();
        corpora.add(corpus1);
        Data data = new Data(corpora);

        GrammarModel result = SGTrainer.train(data);
        List<Grammar> grammarList = result.getGrammars();

        assertEquals(1, grammarList.size());
        assertEquals(GrammarType.INTENT_REGEX, grammarList.get(0).getType());
        assertEquals("播放?", grammarList.get(0).getLabel());
        assertEquals("{{日期}}.*{{actor}}.*的.*{{movie}}", grammarList.get(0).getContent());
    }
}
