package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.ners.NERModInitiator;
import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResources;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.utils.ScoreUtils;
import com.google.common.collect.ArrayListMultimap;
import com.rits.cloning.Cloner;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("nerAnnotator")
public class NERAnnotator implements Annotator {
    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix", "ambiguous_suffix", "kernel", "limited", "prefix", "suffix", "properties", "standard_entity", "plans", "skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited", "tail_minus_one_and_tail_cut_tail_patterns", "tail_minus_one_and_tail_stop_patterns", "tail_plus_one_patterns", "adjust_segment_patterns");
    private static final String WORDLISTKEY = "wordlist";
    private static final String REGEXLISTKEY = "regexlist";
    private static final Map<String, List<String>> RESOURCES_SPEC = new HashMap<String, List<String>>() {{
        put(WORDLISTKEY, PRODUCTNER_WORDLIST);
        put(REGEXLISTKEY, PRODUCTNER_REGEXLIST);
    }};

    public static final String SPECIAL_DICT_LABEL_REGEX = "__(?<wordlist>.*)__";

    public static final String SUBNERS_PROP_NAME = "nlu.ners";
    private static final String SUBNERS_SPLITTER = ",";
    private static final Cloner cloner = new Cloner();
    private static final String SLOT_VALUE_LABEL_NER_ANNOTATOR = "NERAnnotator";

    private Properties properties;
    private NERModInitiator nerModInitiator;
    private NERResources resources;
    private String subners;

//    public NERAnnotator(Properties properties) {
//        this.properties = properties;
//        this.subners = properties.getProperty(SUBNERS_PROP_NAME,"");
//        if(!subners.equals(""))
//            this.nerModInitiator = new NERModInitiator(this.subners,null);
//        else
//            this.nerModInitiator = null;
//    }

    public NERAnnotator(@Autowired DictModel dictModel, @Autowired GrammarModel grammarModel, @Autowired Properties properties) {
        this.properties = properties;
        this.subners = properties.getProperty(SUBNERS_PROP_NAME, "");
        if (!subners.equals("")) {
            this.resources = genResources(grammarModel, dictModel);
            this.nerModInitiator = new NERModInitiator(this.subners, this.resources);
        } else
            this.nerModInitiator = null;
    }


    /**
     * Given a dict, tell if dict is chosen by its label.
     *
     * @param dict the dict
     * @return true if is chosen
     */
    public static boolean isChosenList(Dict dict) {
        Pattern pattern = Pattern.compile(SPECIAL_DICT_LABEL_REGEX);
        Matcher matcher = pattern.matcher(dict.getLabel());
        return matcher.matches();
    }


    public NERResources genResources(GrammarModel grammarModel, DictModel dictModel) {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        String[] subnames = this.subners.split(SUBNERS_SPLITTER);
        for (String name : subnames) {
            switch (name) {
                case "ProductNER":
                    resources.put(name, new HashMap<>());
                    for (Map.Entry<String, List<String>> entry : RESOURCES_SPEC.entrySet()) {
                        if (entry.getKey().equals(REGEXLISTKEY)) {
                            for (String nameofregexlist : entry.getValue()) {
                                resources.get(name).put(nameofregexlist, new NERResource(nameofregexlist, grammarModel.getGrammars().stream().filter(x -> x.getType() == GrammarType.INTENT_REGEX && x.getLabel().equals(nameofregexlist)).map(x -> Pattern.compile(x.getContent())).collect(Collectors.toList())));
                            }
                        } else if (entry.getKey().equals(WORDLISTKEY)) {
                            for (String nameofwordlist : entry.getValue()) {
                                resources.get(name).put(nameofwordlist, new NERResource(nameofwordlist, dictModel.getDict().stream().filter(x -> isChosenList(x) && x.getLabel().equals("__" + nameofwordlist + "__")).map(x -> x.getWord()).collect(Collectors.toList())));
                            }
                        }
                    }
                    break;
                default:
                    break;

            }
        }

        NERResources nerResources = new NERResources(resources);

        return nerResources;
    }

    @Override
    public List<QueryAct> annotate(QueryAct queryAct) {
        if (this.nerModInitiator == null)
            return null;
        String[] destNERS = properties.getProperty(SUBNERS_PROP_NAME).split(SUBNERS_SPLITTER);
        List<SlotValue> slots = queryAct.getSlots().values().stream()
                .sorted(Comparator.comparing(SlotValue::getRealStart))
                .collect(Collectors.toList());
        String query = queryAct.getQuery();
        List<List<SlotValue>> res = null;
        int left = 0;
        if (slots.size() != 0) {
            for (SlotValue slot : slots) {
                if (left < slot.realStart) {
                    res = search(query, left, slot.realStart, res, destNERS);
                }
                if (res != null) {
                    for (List<SlotValue> r : res) {
                        r.add(new SlotValue(slot));
                    }
                } else {
                    res = new ArrayList<List<SlotValue>>() {{
                        add(new ArrayList<SlotValue>() {{
                            add(new SlotValue(slot));
                        }});
                    }};
                }
                left = slot.realEnd;
            }
        }

        if (left < query.length()) {
            res = search(query, left, query.length(), res, destNERS);
        }

        if (res == null || res.size() == 0) {
            return new ArrayList<QueryAct>() {{
                add(new QueryAct(query, query, ArrayListMultimap.create(), 1d));
            }};
        }
        List<QueryAct> queryActs = new ArrayList<>();
        for (List<SlotValue> r : res) {
            r = r.stream().filter(x -> x.realStart < x.realEnd)
                    .sorted(Comparator.comparing(SlotValue::getRealStart)).collect(Collectors.toList());
            QueryAct q = new QueryAct(query, "", ArrayListMultimap.create(), 1d);
            StringBuilder pQueryBuilder = new StringBuilder(query);

            for (int i = r.size() - 1; i >= 0; --i) { //for loop为一条query中所有的slots组成填槽,一次是一种slot可能性
                SlotValue s = r.get(i);
                pQueryBuilder = pQueryBuilder.replace(s.getRealStart(), s.getRealEnd(), SLOT_PREFIX + s.getKey() + SLOT_SUFFIX);
            }
            // correct start, end and length.
            // calculate score
            // the original slots' start/end only affected by bias from NER annotator
            // start/end of slots by NER annotator will be moved according to every slot before this slot.
            // e. g:
            // before: {{A}}bcd{{E}}, A=a,0-1,0-5, E=e,4-5,8-13
            // after:  {{A}}b{{C}}d{{E}}, A=a,0-1,0-5, C=c,2-3,6-11, E=e,4-5,12-17
            int bias = 0;
            int biasByNER = 0;
            double score = 1d;
            for (SlotValue s : r) {
                if (s.label.equals(SLOT_VALUE_LABEL_NER_ANNOTATOR)) {
                    s.start += bias;
                } else {
                    s.start += biasByNER;
                }
                s.length = SLOT_PREFIX.length() + s.getKey().length() + SLOT_SUFFIX.length();
                s.end = s.start + s.length;
                q.getSlots().put(s.getKey(), s);
                bias += s.length - s.realLength;
                if (s.label.equals(SLOT_VALUE_LABEL_NER_ANNOTATOR)) {
                    biasByNER += s.length - s.realLength;
                }
                score *= ScoreUtils.slotScore(s);
            }
            q.setPQuery(pQueryBuilder.toString());
            q.setScore(score);
            queryActs.add(q);
        }
        return queryActs;
    }

    private List<List<SlotValue>> search(String query, int start, int end, List<List<SlotValue>> cur, String[] destNERS) {
        List<List<SlotValue>> ret = new ArrayList<>();
        List<NERResult> nerResults = nerModInitiator.process(query.substring(start, end));
        List<List<NERResult.Candidate>> tmpResult = new ArrayList<>();
        dfs(nerResults, 0, tmpResult);
        List<List<NERResult>> result = new ArrayList<>();

        for (List<NERResult.Candidate> tmpRes : tmpResult) {
            HashMap<String, NERResult> eleResult = new HashMap<>();
            for (String dest : destNERS) {
                eleResult.put(dest, new NERResult(query.substring(start, end), new ArrayList<>()));
            }
            for (NERResult.Candidate tmp : tmpRes) {
                eleResult.get(tmp.getRecognizer()).getCandidates().add(tmp);
            }
            result.add(new ArrayList<>(eleResult.values()));
        }

        for (List<NERResult> deconflictednerResults : result) {
            List<NERResult.Candidate> allCandidates = deconflictednerResults.stream()
                    .map(NERResult::getCandidates).flatMap(Collection::stream)
                    .sorted(Comparator.comparing(NERResult.Candidate::getRealStart))
                    .collect(Collectors.toList());
            List<SlotValue> slots = new ArrayList<>();
            for (int i = allCandidates.size() - 1; i >= 0; --i) {
                NERResult.Candidate candidate = allCandidates.get(i);
                //System.out.println("Candidates:" + allCandidates.get(i));
                int realStart = candidate.getRealStart();
                int realEnd = candidate.getRealEnd();
                String pos = candidate.getPos();
                //String text = query.substring(realStart,realEnd);
                String text = candidate.getText();
                // write candidates text
                slots.add(new SlotValue(text, pos,
                        SLOT_VALUE_LABEL_NER_ANNOTATOR, start + realStart, start + realEnd, start + realStart, start + realEnd));
                /* // write query text
                slots.add(new SlotValue(query.substring(start, end).substring(realStart, realEnd), pos,
                        "", start + realStart, start + realEnd, start + realStart, start + realEnd));
                */
            }
            if (cur != null) {
                for (List<SlotValue> c : cur) {
                    List<SlotValue> tmp = cloner.deepClone(c);
                    tmp.addAll(slots);
                    ret.add(tmp);
                }
            } else {
                ret.add(slots);
            }
        }
        if (tmpResult.size() == 0)
            return cloner.deepClone(cur);
        return ret;
    }


    private void dfs(List<NERResult> nerResults, int idx, List<List<NERResult.Candidate>> result) {
        if (idx < 0 || idx >= nerResults.size())
            return;
        NERResult nerResult = nerResults.get(idx);

        // side-effect: sorted nerResult.getCandidates() with realStart in ascend
        List<Pair<List<Integer>, Integer>> validResultsStruct = getValidResultsStruct(nerResult);
        if (validResultsStruct == null || validResultsStruct.size() == 0) {
            dfs(nerResults, idx + 1, result);
            return;
        }
//        List<HashSet<String>> conflicts = new ArrayList<>();
//        String text = nerResult.getText();
//        for (int i = 0; i < text.length(); ++i) {
//            conflicts.add(new HashSet<>());
//        }
//
//        for (Integer i = 0; i < nerResult.getCandidates().size(); ++i) {
//            NERResult.Candidate candidate = nerResult.getCandidates().get(i);
//            for (int j = candidate.getRealStart(); j < candidate.getRealEnd(); ++j) {
//                conflicts.get(j).add(i.toString());
//            }
//        }

//        List<String> tmp = new ArrayList<>();
//        for (HashSet<String> conflict : conflicts) {
//            if (conflict.size() > 1) {
//                String SortedConflictStr = String.format("_[%s]_(\\d+_)*[%s]_", String.join("", conflict.stream().sorted(Comparator.comparing(x -> Integer.parseInt(x))).collect(Collectors.toList())), String.join("", ((List<String>) conflict).stream().sorted(Comparator.comparing(x -> Integer.parseInt(x))).collect(Collectors.toList())));
//                if (!tmp.contains(SortedConflictStr))
//                    tmp.add(SortedConflictStr);
//            }
//        }
//        List<Pattern> PatternConflicts = new ArrayList<>();
//        for (String regex : tmp) {
//            PatternConflicts.add(Pattern.compile(regex));
//        }

//        int lCandidates = nerResult.getCandidates().size();
//        List<List<Integer>> AllIdxCombinations = new ArrayList<>();
//        List<Integer> one = new ArrayList<>();
//        for (int r = 1; r < lCandidates + 1; ++r) {
//            this.combinations(nerResult.getCandidates(), 0, r, AllIdxCombinations, one);
//        }

        int lCandidates = nerResult.getCandidates().size();
        List<Set<Integer>> isConflictedWithHistory = new ArrayList<>();
        for (int i = 0; i < lCandidates; ++i) {
            isConflictedWithHistory.add(new HashSet<>());
        }
        boolean isEmpty = true;
        if (result != null && result.size() != 0)
            isEmpty = false;
        if (!isEmpty) {
            for (int i = 0; i < lCandidates; ++i) {
                for (int j = 0; j < result.size(); ++j) {
                    List<NERResult.Candidate> res = result.get(j);
                    for (NERResult.Candidate otherNERCandidate : res) {
                        if (!(nerResult.getCandidates().get(i).getRealStart() >= otherNERCandidate.getRealEnd() ||
                                nerResult.getCandidates().get(i).getRealEnd() <= otherNERCandidate.getRealStart())) {
                            isConflictedWithHistory.get(i).add(j);
                            break;
                        }
                    }
                }
            }
        }
        // deepcopy
        List<List<NERResult.Candidate>> copyResult = cloner.deepClone(result);


        int cursor = 0;
        int lResult = 0;
        if (result != null)
            lResult = result.size();
        while (cursor < lResult) {
            result.remove(0);
            ++cursor;
        }


        for (Pair<List<Integer>, Integer> validResult : validResultsStruct) {
            List<Integer> combinations = validResult.getKey();

            if (!isEmpty) {
                List<List<NERResult.Candidate>> tmpResult = cloner.deepClone(copyResult);
                for (int i = 0; i < tmpResult.size(); ++i) {
                    List<NERResult.Candidate> res = tmpResult.get(i);
                    boolean flag1 = false;
                    for (Integer combine : combinations) {
                        if (isConflictedWithHistory.get(combine).contains(i)) {
                            flag1 = true;
                            break;
                        }
                    }
                    if (!flag1) {
                        for (Integer combine : combinations) {
                            res.add(cloner.deepClone(nerResult.getCandidates().get(combine)));
                        }
                        result.add(res);
                    }
                }
            } else {
                List<NERResult.Candidate> tmp1 = new ArrayList<>();
                for (Integer combine : combinations) {
                    tmp1.add(cloner.deepClone(nerResult.getCandidates().get(combine)));
                }
                result.add(tmp1);
            }
        }
        dfs(nerResults, idx + 1, result);
    }

    public List<Pair<List<Integer>, Integer>> getValidResultsStruct(NERResult nerResult) {
        /**
         *  TEXT: A B C D E
         *  candidates[0]: realStart:0 realEnd:2    ==> content: AB
         *  candidates[1]: realStart:0 realEnd:1    ==> content: A
         *  candidates[2]: realStart:1 realEnd:4    ==> content: BCD
         *  candidates[3]: realStart:1 realEnd:5    ==> content: BCDE
         *  现在要找出所有不冲突的candidate组合，即[[0],[1],[2],[1,2],[3],[1,3]] ==> [[AB],[A],[BCD],[A,BCD],[BCDE],[A,BCDE]]
         *
         *  1. candidates按realStart做升序排列，结果如上面描述的candidates
         *  2. 构造结构 result[-1] = [([],0)] result中每个元素是个二元组，其中第一个元素是无冲突组合的具体元素的index,第二个元素是无冲突组合的最右侧元素的realEnd
         *     其目的是用于判断冲突，因为已经做过升序排列，所以仅用最右侧端点判断冲突即可
         *  3. 递推公式: result[i] = result[i-1] + [(l+candidates[i],candidates[i].realEnd) if end <= candidates[i].realStart for (l,end) in result[i-1] ]
         *
         **/
        nerResult.setCandidates(nerResult.getCandidates().stream().sorted(Comparator.comparing(x -> x.getRealStart())).collect(Collectors.toList()));
        List<Pair<List<Integer>, Integer>> result = new ArrayList<>();
        List<Integer> l0 = new ArrayList<>();
        Pair<List<Integer>, Integer> p0 = new Pair<>(l0, 0);
        result.add(p0);

        for (int i = 0; i < nerResult.getCandidates().size(); ++i) {
            List<Pair<List<Integer>, Integer>> oneTurn = new ArrayList<>();
            for (int j = 0; j < result.size(); ++j) {
                if (result.get(j).getValue() <= nerResult.getCandidates().get(i).getRealStart()) {
                    List<Integer> li = cloner.deepClone(result.get(j).getKey());
                    li.add(i);
                    Pair<List<Integer>, Integer> pi = new Pair<>(li, nerResult.getCandidates().get(i).getRealEnd());
                    oneTurn.add(pi);
                }
            }
            result.addAll(oneTurn);
        }
        result = result.stream().filter(x -> x.getKey().size() != 0).collect(Collectors.toList());
        return result;
    }


}
