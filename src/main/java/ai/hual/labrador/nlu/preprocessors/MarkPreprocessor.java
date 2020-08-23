package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.faq.utils.IntegerPair;
import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.trie.Emit;
import ai.hual.labrador.nlu.trie.Trie;
import ai.hual.labrador.nlu.trie.VanillaTrie;
import ai.hual.labrador.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("markPreprocessor")
public class MarkPreprocessor implements Preprocessor {

    private static final String STOP_WORDS_FILE = "mark_stop_words.txt";
    private Trie stopWordTrie;

    private Logger logger = LoggerFactory.getLogger(StopWordRewriter.class);

    public MarkPreprocessor() {
        logger.debug("Constructing stop words trie from file: {}", STOP_WORDS_FILE);
        stopWordTrie = new VanillaTrie();
        new StringUtils().fetchWords(STOP_WORDS_FILE).forEach(w -> stopWordTrie.insert(w, null));
    }

    /**
     * Remove all stop words.
     *
     * @param query The original query or the query preprocessed by other preprocessor
     * @return processed query
     */
    @Override
    public String preprocess(String query) {
        String processed = query;

        // get position of all slots
        List<IntegerPair> slotsPosition = new ArrayList<>();
        int prefixLen = Annotator.SLOT_PREFIX.length();
        int suffixLen = Annotator.SLOT_SUFFIX.length();
        int i = 0;
        while (i < query.length() - (prefixLen + suffixLen)) {
            if (query.substring(i, i + 2).equals(Annotator.SLOT_PREFIX)) {
                int start = i;
                i += prefixLen;
                while (i < query.length() - suffixLen && !query.substring(i, i + suffixLen).equals(Annotator.SLOT_SUFFIX))
                    i++;
                i += suffixLen;
                slotsPosition.add(new IntegerPair(start, i));
            } else
                i++;
        }

        List<Emit> emits = stopWordTrie.parse(processed);
        int bias = 0;
        for (Emit emit : emits) {
            boolean insideSlot = false;
            for (IntegerPair slotPosition : slotsPosition) {
                if (emit.getStart() >= slotPosition.getFormer() && emit.getEnd() <= slotPosition.getLatter()) {
                    insideSlot = true;
                    break;
                }
            }
            if (!insideSlot) {  // pass stop words inside slot
                processed = processed.substring(0, emit.getStart() + bias) +
                        processed.substring(emit.getEnd() + bias, processed.length());
                bias -= emit.getEnd() - emit.getStart();
            }
        }
        return processed;
    }
}
