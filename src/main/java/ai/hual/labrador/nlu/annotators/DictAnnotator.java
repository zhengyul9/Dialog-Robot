package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.annotators.dict.CombinationBFS;
import ai.hual.labrador.nlu.annotators.dict.CombinationDictExtender;
import ai.hual.labrador.nlu.annotators.dict.DictCollectionPolicy;
import ai.hual.labrador.nlu.annotators.dict.DictExtender;
import ai.hual.labrador.nlu.annotators.dict.OverlapCombinationBFS;
import ai.hual.labrador.nlu.annotators.dict.OverlapDictCollectionPolicy;
import ai.hual.labrador.nlu.annotators.dict.VanillaCombinationBFS;
import ai.hual.labrador.nlu.annotators.dict.VanillaDictCollectionPolicy;
import ai.hual.labrador.nlu.preprocessors.Preprocessors;
import ai.hual.labrador.nlu.trie.PinyinTrie;
import ai.hual.labrador.nlu.trie.Trie;
import ai.hual.labrador.nlu.trie.VanillaTrie;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ai.hual.labrador.utils.QueryActUtils.COMBINATION_THRESHOLD;

@Component("dictAnnotator")
public class DictAnnotator extends BaseAnnotator {

    public static final String DICT_COLLECTION_POLICY_PROP_NAME = "nlu.dictAnnotator.collectionPolicy";

    /**
     * Label patter
     * n of specific dict.
     */
    public static final String SPECIAL_DICT_LABEL_REGEX = "__.*__";

    private static Logger logger = LoggerFactory.getLogger(DictAnnotator.class);

    private Trie dictTrie;

    private DictCollectionPolicy dictCollectionPolicy;

    private CombinationBFS combinationBFS;

    @Autowired
    public DictAnnotator(DictModel dictModel, Properties properties) {
        this(dictModel, properties, true, new ArrayList<>());
    }

    /**
     * A configurable dict constructor, can choose specific dict type.
     * Normal dict refers to the base dict set, which includes dict generated from KG.
     * Other types of dict will have the pattern specified by {@value SPECIAL_DICT_LABEL_REGEX},
     * which should not be extracted as slot in NLU, and the pQuery with not be affected also.
     * So that when writing intent regex, the slots needed to be taken into consideration is
     * under control.
     *
     * @param dictModel     dictModel with all types of dict
     * @param properties    properties
     * @param useNormalDict use normal dict or not
     * @param dictTypes     other types of dict in need
     */
    public DictAnnotator(DictModel dictModel, Properties properties, boolean useNormalDict, List<String> dictTypes) {
        // preprocess words of dict, consistent with preprocess of query
        Preprocessors preprocessors = new Preprocessors(properties, false);
        boolean pinyinRobust = Boolean.parseBoolean(properties.getProperty(
                "nlu.dictAnnotator.usePinyinRobust", "false"));
        logger.debug("pinyin robust trigger: {}", pinyinRobust);
        if (pinyinRobust)
            this.dictTrie = new PinyinTrie();
        else
            this.dictTrie = new VanillaTrie();

        // keep the chosen types of dict only
        DictModel chosenDictModel = new DictModel(dictModel.getDict().stream()
                .filter(dict -> useNormalDict && isNormalDict(dict) || dictTypes.contains(dict.getLabel()))
                .collect(Collectors.toList()));

        logger.debug("Constructing {} from dictModel...", dictTrie.getClass().getName());

        // TODO rewrite this code
        boolean generateAliasWord = Boolean.parseBoolean(properties.getProperty(
                "nlu.dictAnnotator.useGenerateAliasWord", "false"));
        if (generateAliasWord) {
            DictExtender dictExtender = new CombinationDictExtender(properties);
            extendDict(dictTrie, dictModel, preprocessors, dictExtender);
        } else {
            for (Dict d : chosenDictModel.getDict()) {
                String preprocessed = preprocessors.preprocess(d.getWord());
                dictTrie.insert(preprocessed, d);
                for (String alias : d.getAliasesArray()) {
                    if (!alias.equals(d.getWord())) {   // prevent alias same as word
                        preprocessed = preprocessors.preprocess(alias);
                        dictTrie.insert(preprocessed, d);
                    }
                }
            }
        }


        String dictCollectionPolicyName = properties.getProperty(DICT_COLLECTION_POLICY_PROP_NAME, "vanilla");
        switch (dictCollectionPolicyName) {
            case "vanilla":
                dictCollectionPolicy = new VanillaDictCollectionPolicy();
                combinationBFS = new VanillaCombinationBFS();
                break;
            case "overlap":
                dictCollectionPolicy = new OverlapDictCollectionPolicy();
                combinationBFS = new OverlapCombinationBFS();
                break;
            default:
                dictCollectionPolicy = new VanillaDictCollectionPolicy();
                combinationBFS = new VanillaCombinationBFS();
        }
        logger.debug("Using dict collect policy: {}", dictCollectionPolicy.getClass().getSimpleName());
        logger.debug("Using combinationBFS method: {}", combinationBFS.getClass().getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<QueryAct> annotate(QueryAct queryAct) {
        String pQuery = queryAct.getPQuery();
        int length = pQuery.length();
        HashMap<Integer, List<QueryAct>> combinationList = new HashMap<>();
        QueryAct initAct = new QueryAct(queryAct);
        initAct.setScore(1f);
        combinationList.put(0, new ArrayList<>(Collections.singleton(initAct)));

        // DP
        int iPrev = 0; // last step in dp
        int jJump = 0; // skip existed slot in pQuery

        for (int i = 1; i < length + 1; i++) {
            combinationList.put(i, new ArrayList<>());
            int lastWordStartPos = 0;
            if (pQuery.charAt(i - 1) == '{' && pQuery.charAt(i) == '{') {
                for (int k = i + 2; k < length; k++) { // find position end with skip slot
                    if (pQuery.charAt(k - 1) == '}' && pQuery.charAt(k) == '}') {
                        jJump = k + 1;
                        for (int m = i; m <= jJump; m++)
                            combinationList.put(m, combinationList.get(i - 1));// skip slot and put pre List<QueryAct> to combinationList
                        i = jJump + 1;
                        if (!(i < length + 1 && pQuery.charAt(i - 1) == '{' && pQuery.charAt(i) == '{')) break;
                    }
                }
                if (jJump >= length) break;
                combinationList.put(i, new ArrayList<>());
            }

            for (int j = jJump; j < i; j++) {
                String suffixWord = pQuery.substring(j, i);
                Object searchedObject = dictTrie.search(suffixWord);
                if (searchedObject != null) {
                    Collection<Dict> dictCollection = (Collection<Dict>) searchedObject;
                    lastWordStartPos = j;
                    dictCollectionPolicy.replaceDictCollection(dictCollection, combinationList,
                            j, i, pQuery, COMBINATION_THRESHOLD);
                }
            }
            List<QueryAct> prev = combinationList.get(iPrev);
            iPrev = i;
            if (prev.get(0).getSlots().size() != 0) {
                combinationList.get(i).addAll(0, retainActs(prev, lastWordStartPos));
            }
            if (combinationList.get(i).size() == 0) {
                QueryAct emptyAct = new QueryAct(queryAct);
                emptyAct.setScore(1f);
                combinationList.get(i).add(emptyAct);
            }
            combinationList.put(i, limitSort(combinationList.get(i), COMBINATION_THRESHOLD));
        }

        Collections.sort(combinationList.get(length));
        return combinationBFS.combinationBFS(combinationList.get(length));
    }

    /**
     * trim list to a given limit number, preserving top items.
     * if list.size() <= limit, return the list directly.
     * if list.size() > limit, the list is sorted and the sublist is returned.
     *
     * @param list  the list
     * @param limit the size limit
     */
    private static <T extends Comparable> List<T> limitSort(@Nonnull List<T> list, int limit) {
        if (list.size() <= limit) {
            return list;
        }
        list.sort(null);
        return list.subList(0, Math.min(list.size(), limit));
    }

    /**
     * Given a dict, tell if dict is normal by its label.
     *
     * @param dict the dict
     * @return true if is normal
     */
    public static boolean isNormalDict(Dict dict) {
        Pattern pattern = Pattern.compile(SPECIAL_DICT_LABEL_REGEX);
        Matcher matcher = pattern.matcher(dict.getLabel());
        return !matcher.matches();
    }

    /**
     * Choose the acts in previous round(i-1) to be retained, tell by position.
     *
     * @param prevActs         acts from previous round
     * @param lastWordStartPos start position of last word in this round
     * @return acts retained
     */
    private List<QueryAct> retainActs(List<QueryAct> prevActs, int lastWordStartPos) {
        return prevActs.stream()
                .filter(act -> Collections.max(act.getSlots().values()).getRealEnd() > lastWordStartPos)
                .collect(Collectors.toList());
    }

    private void extendDict(Trie trie, DictModel dictModel, Preprocessors preprocessors, DictExtender dictExtender) {
        ListMultimap<String, Dict> originDictMap = ArrayListMultimap.create();
        ListMultimap<String, Dict> genDictMap = ArrayListMultimap.create();

        // k:需要从单词中去掉的字符个数;minlen:当该词的长度小于等于该长度时,则不产生新单词
        int minlen = 5;

        for (Dict d : dictModel.getDict()) {
            String preprocessed = preprocessors.preprocess(d.getWord());
            String standWord = preprocessed;
            Boolean isMultiLabeledWord = false;
            logger.debug("Preprocessed standard word of \"{}\" is \"{}\"", d.getWord(), preprocessed);

            if (originDictMap.containsKey(preprocessed)) isMultiLabeledWord = true;
            originDictMap.put(preprocessed, d);
            if (preprocessed.length() > minlen) {
                Dict newDict = new Dict(d);
                newDict.setWord(preprocessed);
                ArrayList<Dict> standAliasDicts = dictExtender.extend(newDict);
                for (Dict dict : standAliasDicts) {
                    String word = dict.getWord();
                    dict.setWord(standWord + " " + preprocessed); //word表示哪个词生成了该词
                    // 标准词的生成:若标准词的生成词为另一个标准词,则该生成词不要;若标准词的生成词为另一个标准词的同义词,则该生成词不要;
                    // 若标准词的生成词为另一标准词的生成词或另一标准词的同义词的生成词,则该生成词不要
                    // 注意:在此语义下,word相同,但label不同的两个标准词视为同一标准词,而不是另一标准词
                    if (!originDictMap.containsKey(word)) {
                        if (!genDictMap.containsKey(word) || isMultiLabeledWord) {
                            genDictMap.put(word, dict);
                        } else for (Dict labelDict : genDictMap.get(word)) labelDict.setLabel("neg");
                    }
                }
            }

            for (String alias : d.getAliasesArray()) {
                // 标准词的同义词的生成:若标准词的同义词的生成词为另一个标准词,则该生成词不要;若标准词的同义词的生成词为另一个标准词的同义词,则该生成词不要;
                // 若标准词的同义词的生成词为另一标准词的生成词,则该生成词不要;若标准词的同义词的生成词为另一标准词的同义词的生成词,当两个标准词的同义词相等时,则要,否则不要
                // 若标准词的同义词的生成词为该标准词另一同义词的生成词,则该词要
                // 注意:在此语义下,word相同,但label不同的两个标准词视为同一标准词,而不是另一标准词
                if (!alias.equals(d.getWord()) && !alias.equals("")) {
                    preprocessed = preprocessors.preprocess(alias);
                    logger.debug("Preprocessed alias of \"{}\" is \"{}\"", alias, preprocessed);
                    originDictMap.put(preprocessed, d);
                    if (preprocessed.length() > minlen) {
                        Dict newDict = new Dict(d);
                        newDict.setWord(preprocessed);
                        ArrayList<Dict> AliasAliasDicts = dictExtender.extend(newDict);
                        for (Dict dict : AliasAliasDicts) {
                            String word = dict.getWord();
                            dict.setWord(standWord + " " + preprocessed);
                            if (!originDictMap.containsKey(word)) {
                                if (!genDictMap.containsKey(word)) genDictMap.put(word, dict);
                                else {
                                    boolean insertable = false;
                                    for (Dict labelDict : genDictMap.get(word)) {
                                        if (labelDict.getWord().split(" ")[0].equals(standWord) || labelDict.getWord().split(" ")[1].equals(preprocessed))
                                            insertable = true;
                                    }
                                    if (insertable) genDictMap.put(word, dict);
                                    else for (Dict labelDict : genDictMap.get(word)) labelDict.setLabel("neg");
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Dict> entry : originDictMap.entries()) dictTrie.insert(entry.getKey(), entry.getValue());
        for (Map.Entry<String, Dict> entry : genDictMap.entries()) {
            Dict dict = entry.getValue();
            if (dict.getLabel() == null || !dict.getLabel().equals("neg")) dictTrie.insert(entry.getKey(), dict);
        }
    }
}
