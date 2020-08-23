package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.annotators.DictAnnotator;
import ai.hual.labrador.nlu.preprocessors.utils.SlotIndexPair;
import ai.hual.labrador.nlu.trie.Emit;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.preprocessors.NormalizeWordRewriter.CUSTOM_DICT_FILE;
import static ai.hual.labrador.nlu.preprocessors.utils.PreprocessUtils.skipOverlapSlots;

@Component("chatWordRewriter")
public class ChatWordRewriter implements Preprocessor {

    private static final String CHAT_WORDS_FILE = "chat_words.txt";
    private static final Logger logger = LoggerFactory.getLogger(ChatWordRewriter.class);
    private Trie chatWordTrie;
    private DictAnnotator dictAnnotator;

    private JiebaSegmenter segmenter;
    private WordDictionary customDict = WordDictionary.getInstance();

    public ChatWordRewriter(@Autowired @Qualifier("dictAnnotator") DictAnnotator dictAnnotator) {
        this.dictAnnotator = dictAnnotator;
        logger.debug("Constructing stop words trie from file: {}", CHAT_WORDS_FILE);
        chatWordTrie = new VanillaTrie();
        List<String> stopWords = new StringUtils().fetchWords(CHAT_WORDS_FILE);
        stopWords.forEach(w -> chatWordTrie.insert(w, null));
        logger.debug("{} chat stop words loaded", stopWords.size());
        segmenter = new JiebaSegmenter();

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
     * Remove all chat_words.txt words after jieba tokenizer processed.
     * Could keep slot value not changed.
     *
     * @param query The original query or the query preprocessed by other preprocessor
     * @return processed query
     */
    @Override
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
     * 另可参见 NormlizeWordIntentRewriter
     *
     * @param query            pquery
     * @param sortedSlotAsList 多个槽
     * @return
     */

    public StringBuilder orderProcess(String query, List<SlotValue> sortedSlotAsList) {
        List<String> textList = new ArrayList<>();
        int lastLoc = 0;
        //存在一个槽有多个匹配，去掉多余的匹配
        List<SlotValue> clearSlotAsList = sortedSlotAsList.stream().filter(x -> x.getStart() != 0 || x.getEnd() != 0).collect(Collectors.toList());
        for (int i = 0; i < clearSlotAsList.size(); i++) {
            SlotValue slot = clearSlotAsList.get(i);
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
     * using recursion method to process the query.
     * when start with slot start index, append slot value, call recursion after slot end index;
     * else call partialProcess util next slot start index.
     *
     * @param pQuery
     * @param start
     * @param end
     * @param sortedSlotAsList
     * @param slotIndex
     * @param result
     * @return
     */
    public StringBuilder recursionProcess(String query, String pQuery, int start, int end, List<SlotValue> sortedSlotAsList, int slotIndex, StringBuilder result) {
        if (start == end)
            return result;
        if (pQuery.substring(start, end).startsWith(Annotator.SLOT_PREFIX)) {
            SlotIndexPair pair = skipOverlapSlots(sortedSlotAsList, slotIndex);
            slotIndex = pair.getSlotIndex();
            result.append(query.substring(pair.getSlotValue().getRealStart(), pair.getSlotValue().getRealEnd()));
            while (start < end && !pQuery.substring(start, start + Annotator.SLOT_SUFFIX.length()).equals(Annotator.SLOT_SUFFIX))
                start++;
            return recursionProcess(query, pQuery, start + Annotator.SLOT_SUFFIX.length(), end, sortedSlotAsList, slotIndex + 1, result);
        } else {
            int temp = start;
            while (temp < end - 1 && !pQuery.substring(temp, temp + Annotator.SLOT_SUFFIX.length()).equals(Annotator.SLOT_PREFIX))
                temp++;
            if (temp == end - 1) temp = end;
            result.append(partialProcess(pQuery.substring(start, temp)));
            return recursionProcess(query, pQuery, temp, end, sortedSlotAsList, slotIndex, result);
        }
    }

    /**
     * using jieba tokenizer to get divided words first, then filter those chat words in dict.
     *
     * @param text
     * @return
     */
    private String partialProcess(String text) {
        List<SegToken> segTokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        StringBuilder result = new StringBuilder();
        for (SegToken token : segTokens) {
            List<Emit> emits = chatWordTrie.parse(token.word);
            if (emits.isEmpty()) result.append(token.word);
        }
        return result.toString();
    }
}
