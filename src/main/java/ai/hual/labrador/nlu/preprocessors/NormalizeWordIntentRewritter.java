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
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.skipOverlapSlots;

@Component("normalizeCommonIntentWordPreprocessor")
public class NormalizeWordIntentRewritter implements Preprocessor {
    private static Logger logger = LoggerFactory.getLogger(NormalizeWordRewriter.class);

    public static final String CUSTOM_DICT_FILE = "custom_words.txt";
    private static final String SYNONYM_DICT_FILE = "synonym_dict.txt";
    private Trie synonymWordTrie;
    private DictAnnotator dictAnnotator;
    private JiebaSegmenter segmenter;
    private WordDictionary customDict = WordDictionary.getInstance();

    public NormalizeWordIntentRewritter(DictAnnotator dictAnnotator) {
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
        List<QueryAct> queryActs = dictAnnotator.annotate(new QueryAct(query));
        QueryAct queryAct = queryActs.get(0);
        List<SlotValue> sortedSlotAsList = queryAct.getSortedSlotAsList();
        String pQuery = queryAct.getPQuery();

        StringBuilder processed = orderProcess(pQuery, sortedSlotAsList);
        return processed.toString();
    }

    /**
     * 使用按照槽的个数完成处理, 避开intent可能会出现{{{{}}}}和class
     *
     * @param query            pquery
     * @param sortedSlotAsList 多个槽
     * @return
     */

    public StringBuilder orderProcess(String query, List<SlotValue> sortedSlotAsList) {
        List<String> textList = new ArrayList<>();
        int lastLoc = 0;
        //存在一个槽有多个匹配，去掉多余的匹配
        List<SlotValue> clearSlotAsList = sortedSlotAsList.stream().filter(x -> x.getStart() != 0 && x.getEnd() != 0).collect(Collectors.toList());
        for (SlotValue slot : clearSlotAsList) {
            if (slot.getStart() > lastLoc) textList.add(query.substring(lastLoc, slot.getStart())); //保证首位是非槽的文本
            else textList.add("");
            lastLoc = slot.getEnd();
        }
        if (lastLoc < query.length()) textList.add(query.substring(lastLoc, query.length()));//保证末尾是非槽的文本
        else textList.add("");

        // process
        List<String> processText = new ArrayList<>();
        for (String text : textList) {
            if (text.isEmpty()) processText.add("");
            else processText.add(partialProcess(text));
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < processText.size() + clearSlotAsList.size(); i++) {
            if (i % 2 == 0) result.append(processText.get(i / 2));
            else result.append(clearSlotAsList.get(i / 2).matched);
        }

        return result;
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
        if (start == end)//终止条件
            return result;
        if (query.substring(start, end).startsWith(Annotator.SLOT_PREFIX)) {    // start with slot 发现槽填进去，并移动start到suffix
            SlotIndexPair pair = skipOverlapSlots(sortedSlotAsList, slotIndex);// 槽的位置
            slotIndex = pair.getSlotIndex();
            while (query.substring(start + Annotator.SLOT_SUFFIX.length(), end).startsWith(Annotator.SLOT_PREFIX)) { // 利用不会判断}},{{和}}并不对, 实际应该把每段预先保存的
                result.append(Annotator.SLOT_PREFIX);
                start += 2;
            }
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
