package ai.hual.labrador.nlu.ners;

import com.rits.cloning.Cloner;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class DeconflictedNERModule implements NERModule {

    private static final Logger logger = LoggerFactory.getLogger(DeconflictedNERModule.class);
    private static final Cloner cloner = new Cloner();

    public abstract NERResult recognizeDeconflicted(String text, List<List<NERResult.Candidate>> deconflictedCandidates);

    @Override
    public final NERResult recognize(String text, List<NERResult> nerResults) {
        if (nerResults != null && nerResults.size() != 0) {
            boolean isMatchText = true;
            for (NERResult nerresult : nerResults) {
                if (!text.equals(nerresult.getQuery())) {
                    isMatchText = false;
                    break;
                }
            }
            if (!isMatchText) {
                logger.debug("The texts of NERResults don't match");
                return null;
            }
        }
        // get independ nerresult
        List<List<NERResult.Candidate>> result = new ArrayList<>();
        if (nerResults != null)
            dfs(nerResults, 0, result);

        return recognizeDeconflicted(text, result);
    }

    private void dfs(List<NERResult> nerResults, int idx, List<List<NERResult.Candidate>> result) {
        if (idx < 0 || idx >= nerResults.size())
            return;
        NERResult nerResult = nerResults.get(idx);

        // side-effect: sorted nerResult.getCandidates() with realStart in ascend
        List<Pair<List<Integer>, Integer>> validResultsStruct = getValidResultsStruct(nerResult);
        if(validResultsStruct == null || validResultsStruct.size() == 0){
            dfs(nerResults, idx + 1, result);
            return ;
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
        for(int i=0;i<lCandidates;++i){
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

    private List<Pair<List<Integer>, Integer>> getValidResultsStruct(NERResult nerResult) {
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
        result = result.stream().filter(x->x.getKey().size()!=0).collect(Collectors.toList());
        return result;
    }

}
