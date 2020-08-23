package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import ai.hual.labrador.nlu.preprocessors.utils.SlotIndexPair;
import ai.hual.labrador.nlu.trie.Trie;
import ai.hual.labrador.nlu.trie.VanillaTrie;
import ai.hual.labrador.utils.StringUtils;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.skipOverlapSlots;

@Component("normalizeWordRewriter")
public class NormalizeWordRewriter implements Preprocessor {

    private static Logger logger = LoggerFactory.getLogger(NormalizeWordRewriter.class);

    public static final String CUSTOM_DICT_FILE = "custom_words.txt";
    private static final String SYNONYM_DICT_FILE = "synonym_dict.txt";
    private Trie synonymWordTrie;
    private DictAnnotator dictAnnotator;
    private JiebaSegmenter segmenter;
    private WordDictionary customDict = WordDictionary.getInstance();

    public NormalizeWordRewriter(@Autowired @Qualifier("dictAnnotator") DictAnnotator dictAnnotator) {
        this.dictAnnotator = dictAnnotator;

        logger.debug("Constructing synonym words trie from file: {}", SYNONYM_DICT_FILE);
        synonymWordTrie = new VanillaTrie();
        List<Dict> dicts = new StringUtils().fetchDicts(SYNONYM_DICT_FILE).getDict();
        logger.debug("{} synonym dict loaded", dicts.size());
        dicts.forEach(d -> {
            logger.debug("synonym dict {{}} loaded", d);
            for (String synonym : d.getAliasesArray())
                synonymWordTrie.insert(synonym, d);
        });

        segmenter = new JiebaSegmenter();
        logger.debug("Load custom dict file", CUSTOM_DICT_FILE);
        Path path = new StringUtils().loadCustomWords(CUSTOM_DICT_FILE);
        customDict.loadUserDict(path);
        File file = new File(path.toUri());
        if (file.exists()) {
            if (!file.delete()) {
                logger.warn("Fail deleting temp file {}", path.toString());
            }
        }
    }

    /**
     * Using jieba tokenizer to divide query and replace those should be rewrite words in the dict.
     * Could keep slot value not changed.
     *
     * @param query The original query or the query preprocessed by other preprocessor
     * @return
     */
    public String preprocess(String query) {
        StringBuilder processed = new StringBuilder();
        List<QueryAct> queryActs = dictAnnotator.annotate(new QueryAct(query));
        QueryAct queryAct = queryActs.get(0);
        List<SlotValue> sortedSlotAsList = queryAct.getSortedSlotAsList();
        String pQuery = queryAct.getPQuery();

        processed = recursionProcess(pQuery, 0, pQuery.length(), sortedSlotAsList, 0, processed);
        return processed.toString();
    }

    /**
     * Using recursion method to process the query.
     * When start with slot start index, append slot value, call recursion after slot end index;
     * else call partialProcess util next slot start index.
     *
     * @param query
     * @param start
     * @param end
     * @param sortedSlotAsList
     * @param slotIndex
     * @param result
     * @return
     */
    public StringBuilder recursionProcess(String query, int start, int end, List<SlotValue> sortedSlotAsList, int slotIndex, StringBuilder result) {
        if (start == end)
            return result;
        if (query.substring(start, end).startsWith(Annotator.SLOT_PREFIX)) {    // start with slot
            SlotIndexPair pair = skipOverlapSlots(sortedSlotAsList, slotIndex);
            slotIndex = pair.getSlotIndex();
            result.append(pair.getSlotValue().getMatched());
            while (start < end && !query.substring(start, start + Annotator.SLOT_SUFFIX.length()).equals(Annotator.SLOT_SUFFIX))
                start++;
            return recursionProcess(query, start + Annotator.SLOT_SUFFIX.length(), end, sortedSlotAsList, slotIndex + 1, result);
        } else {
            int temp = start;
            while (temp < end - 1 && !query.substring(temp, temp + Annotator.SLOT_SUFFIX.length()).equals(Annotator.SLOT_PREFIX))
                temp++;
            if (temp == end - 1) temp = end;
            result.append(partialProcess(query.substring(start, temp)));
            return recursionProcess(query, temp, end, sortedSlotAsList, slotIndex, result);
        }
    }

    /**
     * Using jieba tokenizer to get divided words first, then replace those words in dict.
     *
     * @param text input string
     * @return text after rewriting
     */
    private String partialProcess(String text) {
        List<SegToken> segTokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        for (int i = 0; i < segTokens.size(); i++) {
            SegToken token = segTokens.get(i);
            Object object = synonymWordTrie.search(token.word);
            if (object != null) {
                List<Dict> dictList = (List<Dict>) object;

                String replaceWord;
                // very special case, see unit test for explanation
                if (token.word.equals("怎么")) {
                    if (i + 1 < segTokens.size() && segTokens.get(i + 1).word.startsWith("不"))
                        replaceWord = "为什么";
                    else
                        replaceWord = dictList.get(0).getWord();
                } else
                    replaceWord = dictList.get(0).getWord();

                text = StringUtils.replaceSubstring(text, token.startOffset, token.endOffset, replaceWord);
            }
        }
        return text;
    }
}
