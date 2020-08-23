package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.pinyin.PinyinImpl;
import ai.hual.labrador.nlu.pinyin.PinyinScoreTuple;
import ai.hual.labrador.nlu.pinyin.PinyinTokenizer;
import ai.hual.labrador.utils.Tokenizer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PinyinTrie implements Trie {

    private static double TRIM_SCORE = 1;
    private static double DEPTH_BONUS = 1.5;
    private static double TONE_DISCOUNT = 0.7;
    private static double WILDCARD_DISCOUNT = 0.9;

    private static Logger logger = LoggerFactory.getLogger(PinyinTrie.class);

    public PinyinTrieNode root;
    public Boolean VERBOSE = false;

    private PinyinImpl pinyin;
    private Tokenizer tokenizer;
    private ListMultimap<String, PinyinScoreTuple> pinyinMap;

    public PinyinTrie() {

        root = new PinyinTrieNode(null);
        root.setContent(ArrayListMultimap.create());
        pinyin = new PinyinImpl();
        tokenizer = new PinyinTokenizer();
        pinyinMap = PinyinImpl.getPinyinRobustMap("pinyin_prob_file");
    }

    public PinyinTrieNode getRoot() {
        return this.root;
    }

    @Override
    public List<Emit> parse(String text) {

        List<Emit> emits = new ArrayList<>();

        if (this.pinyin == null)
            this.pinyin = new PinyinImpl();

        String pinyinString = pinyin.getPinyin(text);
        // convert text to a list of pinyin
        List<String> pinyinList = tokenizer.tokenize(pinyinString);
        int length = pinyinList.size();

        int textPos = 0;
        PinyinTrieNode crawl = this.root;

        while (textPos < length) {

            List<String> subPinyinList = IntStream.range(textPos, length)
                    .mapToObj(pinyinList::get)
                    .collect(Collectors.toList());

            Emit maxEmit = new Emit();
            parsePinyinRobustDFS(crawl, subPinyinList, pinyinMap, 1f, maxEmit);

            if (maxEmit.getLevel() != 0) { // has a meaningful emit
                int realStart = maxEmit.getStart() + textPos;
                int realEnd = maxEmit.getEnd() + textPos;
                maxEmit.setStart(realStart);
                maxEmit.setEnd(realEnd);
                textPos += realEnd - realStart;

                String textPart = text.substring(realStart, realEnd);
                // deal with words with same pinyin
                if (chooseEmitChineseWord(textPart, maxEmit))
                    emits.add(maxEmit);
            } else {    // no match, move on
                textPos++;
            }
        }

        return emits;
    }

    @Override
    public void insert(String word, Object content) {

        if (!(content instanceof Dict)) {
            logger.debug("has to take Dict as content");
            return;
        }
        Dict dict = (Dict) content;

        String pinyinString = pinyin.getPinyin(word);
        List<String> wordPinyin = tokenizer.tokenize(pinyinString);

        if (VERBOSE)
            logger.debug(pinyinString);

        // Find length of the given wordPinyin
        int length = wordPinyin.size();
        PinyinTrieNode crawl = this.root;

        // Traverse through all characters of given wordPinyin
        for (int level = 0; level < length; level++) {
            HashMap<Object, PinyinTrieNode> children = crawl.getChildren();
            Object token = wordPinyin.get(level);

            // If there is already a children for current character of given wordPinyin
            if (children.containsKey(token)) {
                crawl = children.get(token);
            } else { // create a children
                PinyinTrieNode newNode = new PinyinTrieNode(token);
                newNode.setLevel(crawl.getLevel() + 1); // parent's level + 1
                children.put(token, newNode);
                crawl = newNode;
            }
        }

        // Set isLeaf true for last character
        // use add to make sure words with same pinyin is added
        crawl.putContent(word, dict);
        crawl.setAsLeaf();
    }

    /**
     * Search by word's pinyin, return a collection of dict,
     * who has same key and different value.
     *
     * @param word word(in string) of interest
     * @return collection of dict
     */
    @Override
    public Object search(String word) {
        if (this.pinyin == null)
            this.pinyin = new PinyinImpl();

        Tokenizer tokenizer = new PinyinTokenizer();
        String pinyinString = pinyin.getPinyin(word);

        if (VERBOSE)
            logger.debug(pinyinString);
        List<String> inputPinyin = tokenizer.tokenize(pinyinString);

        int length = inputPinyin.size();
        PinyinTrieNode crawl = this.root;

        Emit maxEmit = new Emit();
        parsePinyinRobustDFS(crawl, inputPinyin, pinyinMap, 1f, maxEmit);
        if (maxEmit.getEnd() - maxEmit.getStart() == length) {
            if (chooseEmitChineseWord(word, maxEmit))
                return maxEmit.getContent();
            else
                return null;
        } else
            return null;
    }

    /**
     * Choose the best Chinese word who has same pinyin if exist.
     * Modified maxEmit has Dict as content.
     *
     * @param textPart word in input text
     * @param maxEmit  emit of max score
     * @return if a best Chinese word can be chosen
     */
    protected boolean chooseEmitChineseWord(String textPart, Emit maxEmit) {

        PinyinTrieNode maxNode = (PinyinTrieNode) maxEmit.getContent();
        ListMultimap<String, Dict> contentMap = maxNode.getContent();

        if (contentMap.containsKey(textPart)) {   // obtain the one who matches in Chinese char
            maxEmit.setContent(contentMap.get(textPart));
            return true;
        } else {   // select most alike one, if exist
            Collection<String> keySet = contentMap.keySet();
            String bestKey = null;
            int minMissMatch = 10;
            for (Object k : keySet) {
                int missMatchCount = 0;
                String key = (String) k;
                key = key.replaceAll("\\s+", "");
                assert textPart.length() == key.length();
                for (int i = 0; i < key.length(); i++) {
                    if (textPart.charAt(i) != key.charAt(i))
                        missMatchCount++;
                }
                if (missMatchCount < minMissMatch) {
                    minMissMatch = missMatchCount;
                    bestKey = key;
                }
            }
            // for single chinese char, no mismatch is allowed
            if (!(textPart.length() == 1 && minMissMatch > 0)) {
                maxEmit.setContent(contentMap.get(bestKey));
                return true;
            }
            return false;
        }
    }

    /**
     * DFS along Trie's path from <tt>trieNode</tt>,
     * where each node represents a pinyin token.
     * For every token in <tt>pinyinList</tt>, traverse
     * all path start with its pinyin robust synonym.
     *
     * @param trieNode   start node
     * @param pinyinList list of pinyin token to be traversed
     * @param pinyinMap  map from pinyin to its robust synonym
     * @param score      accumulated score so far
     * @param maxEmit    emit with the maximum score so far
     */
    public void parsePinyinRobustDFS(PinyinTrieNode trieNode, List<String> pinyinList,
                                     ListMultimap<String, PinyinScoreTuple> pinyinMap,
                                     double score, Emit maxEmit) {

        int level = trieNode.getLevel();    // same as pinyinPos
        if (level >= pinyinList.size())
            return;

        HashMap<Object, PinyinTrieNode> children = trieNode.getChildren();
        String token = (String) pinyinList.get(level);

        // split pinyin and its tone
        String tokenNoTone;
        String tone;
        if (token.length() == 0) {
            tokenNoTone = "";
            tone = "";
        } else {
            tokenNoTone = token.substring(0, token.length() - 1);
            tone = String.valueOf(token.charAt(token.length() - 1));
        }

        // add pinyin if not already in map
        if (pinyinMap.get(tokenNoTone).size() == 0)
            pinyinMap.put(tokenNoTone, new PinyinScoreTuple(tokenNoTone, 1f));

        Boolean found = false;
        // loop over all robust pinyin in map
        for (PinyinScoreTuple pinyinScoreTuple : pinyinMap.get(tokenNoTone)) {
            double toneDiscount;
            // loop over all tone
            for (int t = 1; t <= 5; t++) {
                String currentTone = Integer.toString(t);
                String robustPinyin;
                if (token.length() > 1)
                    robustPinyin = pinyinScoreTuple.getPinyin() + currentTone;  // append tone
                else {
                    robustPinyin = pinyinScoreTuple.getPinyin() + tone; // re-append
                    t = 6;  // no need to loop tone
                }
                if (!currentTone.equals(tone))
                    toneDiscount = TONE_DISCOUNT;
                else
                    toneDiscount = 1f;
                // move to next node and update score
                if (children.containsKey(robustPinyin)) {   // have a child of robust pinyin
                    found = true;
                    trieNode = children.get(robustPinyin);
                    // compute score
                    double scoreDiscount = pinyinScoreTuple.getScore();
                    double newScore = score;
                    newScore *= scoreDiscount;
                    newScore *= toneDiscount;
                    newScore *= DEPTH_BONUS;
                    // trim branch if score lower than threshold
                    // or lower than max (judge when at same level)
                    if (newScore < TRIM_SCORE ||
                            trieNode.getLevel() > maxEmit.getLevel() && newScore < maxEmit.getScore())
                        return;
                    // update maximum emit found so far
                    if (trieNode.isLeaf() && newScore >= maxEmit.getScore()) {
                        maxEmit.setContent(trieNode);
                        maxEmit.setEnd(trieNode.getLevel());
                        maxEmit.setLevel(trieNode.getLevel());
                        maxEmit.setScore(newScore);
                    }
                    // recursively call DFS
                    parsePinyinRobustDFS(trieNode, pinyinList, pinyinMap, newScore, maxEmit);
                }
            }
        }
        if (!found && children.containsKey("?")) {
            trieNode = children.get("?");
            // compute score
            double newScore = score;
            newScore *= WILDCARD_DISCOUNT;
            newScore *= DEPTH_BONUS;
            // trim branch if score lower than threshold
            // or lower than max (judge when at same level)
            if (newScore < TRIM_SCORE ||
                    trieNode.getLevel() > maxEmit.getLevel() && newScore < maxEmit.getScore())
                return;
            // update maximum emit found so far
            if (trieNode.isLeaf() && newScore >= maxEmit.getScore()) {
                maxEmit.setContent(trieNode);
                maxEmit.setEnd(trieNode.getLevel());
                maxEmit.setLevel(trieNode.getLevel());
                maxEmit.setScore(newScore);
            }
            // recursively call DFS
            parsePinyinRobustDFS(trieNode, pinyinList, pinyinMap, newScore, maxEmit);
        }
        // can not move on in trie, return
    }
}
