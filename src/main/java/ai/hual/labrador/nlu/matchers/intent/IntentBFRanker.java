package ai.hual.labrador.nlu.matchers.intent;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.resort.intent.IntentScores;
import com.google.common.collect.ImmutableMap;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.primitives.Ints.max;

public class IntentBFRanker implements IntentRanker {

    public static final String COS_KEY = "cos";
    public static final String OVERLAP_KEY = "overlap";
    public static final String CHAR_OVERLAP_KEY = "char_overlap";
    public static final String MAX_COMMON_STRING_KEY = "mcs";
    public static final String COS_IMP_KEY = "cos_imp";
    public static final String OVERLAP_IMP_KEY = "overlap_imp";
    public static final String COS_VERY_IMP_KEY = "cos_very_imp";
    public static final String OVERLAP_VERY_IMP_KEY = "overlap_very_imp";
    public static final String EQ_VERY_IMP_KEY = "eq_very_imp";

    private static final String punctuationReg = "[\\\\…~.?!':;,/()&\"“”。，：；．！？）+、\\-＂＇｀｜〃〔〕〈〉《》「」『』．〖〗【】（）［］｛｝／￥]";
    private static final Pattern punctuationPattern = Pattern.compile("[\\\\~.?!':;,/()&\"。，、；：？！…—·ˉˇ¨‘’“”々～‖∶＂＇｀｜〃〔〕〈〉《》「」『』．〖〗【】（）［］｛｝／￥]");
    private static final Pattern lettersNumbersPattern = Pattern.compile("[a-zA-Z0-9]+");
    private static final Pattern noLettersNumbersPattern = Pattern.compile("[^a-zA-Z0-9]+");

    static class Word {

        private String str;
        private boolean important;
        private boolean veryImportant;
        private double length;

        Word(String str, String pos) {
            this.str = str;
            this.important = important(pos);
            this.veryImportant = veryImportant(pos);
            this.length = getChineseLength(str);
        }

        private static boolean important(String pos) {
            if (pos == null || pos.isEmpty())
                return false;
            char ch = pos.toLowerCase().charAt(0);
            return ch == 'v' || ch == 'n' || ch == 't' || ch == 'm' ||
                    pos.equalsIgnoreCase("vn") || pos.equalsIgnoreCase("nx");
        }

        private static boolean veryImportant(String pos) {
            return pos.equals("veryImportant");
        }

        private static double getChineseLength(String str) {
            if (punctuationPattern.matcher(str).matches()) {
                return 0;
            }
            String letters = noLettersNumbersPattern.matcher(str).replaceAll("").trim();
            String noLetters = lettersNumbersPattern.matcher(str).replaceAll("").trim();
            return letters.length() / 2 + noLetters.length();
        }

    }

    // segment
    private Forest segDict = new Forest();
    private List<Dict> dicts;
    private Set<String> veryImportantWords = new HashSet<>();

    public IntentBFRanker(DictModel dictModel) {
        this.dicts = dictModel.getDict().stream().filter(x -> x.getLabel().equals("__重要属性__")).collect(Collectors.toList());
        for (Dict dict : dicts) {
            Library.insertWord(segDict, new Value(dict.getWord(), "veryImportant", "1000"));
            veryImportantWords.add(dict.getWord());
            for (String alias : dict.getAliasesArray()) {
                Library.insertWord(segDict, new Value(alias, "veryImportant", "1000"));
                veryImportantWords.add(alias);
            }
        }
    }

    @Override
    public List<Map<String, List<Double>>> rank(List<List<IntentScores>> intentScoresCompoundList) {
        return intentScoresCompoundList.stream().map(intentScoresList -> {
            if (intentScoresList.isEmpty()) {
                return ImmutableMap.<String, List<Double>>of();
            }
            Map<String, List<Double>> result = new HashMap<>();
            Triple<String, List<Word>, Map<String, Word>> query = processQuery(intentScoresList.get(0).getQuery());
            for (IntentScores intentScores : intentScoresList) {
                Pair<String, List<Word>> question = processQuestion(intentScores.getQuestion());
                for (Map.Entry<String, Double> entry : rank(query, question).entrySet()) {
                    result.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue());
                }
            }
            return result;
        }).collect(Collectors.toList());
    }

    private Triple<String, List<Word>, Map<String, Word>> processQuery(String query) {
        List<Word> queryWords = filterWords(segment(query));
        Map<String, Word> extendedQueryMap = buildExtensionMap(queryWords);
        String nQuery = query.replaceAll(punctuationReg + "| ", "").toLowerCase();
        return Triple.of(nQuery, queryWords, extendedQueryMap);
    }

    private Pair<String, List<Word>> processQuestion(String question) {
        List<Word> questionWords = filterWords(segment(question));
        String nQuestion = question.replaceAll(punctuationReg + "| ", "").toLowerCase();
        return Pair.of(nQuestion, questionWords);
    }

    private Map<String, Double> rank(Triple<String, List<Word>, Map<String, Word>> query, Pair<String, List<Word>> question) {
        // similarities
        double[] sims = calculateCosAndOverlap(question.getRight(), query.getMiddle(), query.getRight());
        double length = max(question.getLeft().length(), query.getLeft().length());
        double mcs = maxCommonStringLength(query.getLeft(), question.getLeft()) / length;
        double olp = characterOverlap(query.getLeft(), question.getLeft()) / length;

        return ImmutableMap.<String, Double>builder()
                .put(COS_KEY, sims[0])
                .put(OVERLAP_KEY, sims[1])
                .put(COS_IMP_KEY, sims[2])
                .put(OVERLAP_IMP_KEY, sims[3])
                .put(COS_VERY_IMP_KEY, sims[4])
                .put(OVERLAP_VERY_IMP_KEY, sims[5])
                .put(EQ_VERY_IMP_KEY, sims[6])
                .put(CHAR_OVERLAP_KEY, olp)
                .put(MAX_COMMON_STRING_KEY, mcs)
                .build();
    }

    /**
     * calculate cos, overlap, cos_imp, overlap_imp
     *
     * @param a       The word set of sentence a.
     * @param b       The word set of sentence b.
     * @param bExtend The extension word to Word map
     * @return A double array, cos, overlap, cos_imp, overlap_imp
     */
    private static double[] calculateCosAndOverlap(List<Word> a, List<Word> b, Map<String, Word> bExtend) {
        double cos = 0;
        double aTotalCos = a.stream().mapToDouble(x -> x.length).map(x -> x * x).sum();
        double bTotalCos = b.stream().mapToDouble(x -> x.length).map(x -> x * x).sum();

        double over = 0;
        double aTotalOver = a.stream().mapToDouble(x -> x.length).sum();
        double bTotalOver = b.stream().mapToDouble(x -> x.length).sum();

        double cosImp = 0;
        double aTotalCosImp = a.stream().filter(x -> x.important).mapToDouble(x -> x.length).map(x -> x * x).sum();
        double bTotalCosImp = b.stream().filter(x -> x.important).mapToDouble(x -> x.length).map(x -> x * x).sum();

        double overImp = 0;
        double aTotalOverImp = a.stream().filter(x -> x.important).mapToDouble(x -> x.length).sum();
        double bTotalOverImp = b.stream().filter(x -> x.important).mapToDouble(x -> x.length).sum();

        double cosVeryImp = 0;
        double aTotalCosVeryImp = a.stream().filter(x -> x.veryImportant).mapToDouble(x -> x.length).map(x -> x * x).sum();
        double bTotalCosVeryImp = b.stream().filter(x -> x.veryImportant).mapToDouble(x -> x.length).map(x -> x * x).sum();

        double overVeryImp = 0;
        double aTotalOverVeryImp = a.stream().filter(x -> x.veryImportant).mapToDouble(x -> x.length).sum();
        double bTotalOverVeryImp = b.stream().filter(x -> x.veryImportant).mapToDouble(x -> x.length).sum();

        Map<Word, Boolean> bMap = b.stream().collect(Collectors.toMap(x -> x, x -> false));
        for (Word ta : a) {
            for (Map.Entry<Word, Boolean> tb : bMap.entrySet()) {
                if (tb.getValue()) {
                    continue;
                }
                if (bExtend.containsKey(ta.str) && Objects.equals(bExtend.get(ta.str).str, tb.getKey().str) ||
                        bExtend.containsKey(tb.getKey().str) && Objects.equals(bExtend.get(tb.getKey().str).str, ta.str) ||
                        ta.str.equals(tb.getKey().str)) {
                    cos += ta.length * tb.getKey().length;
                    over += (ta.length + tb.getKey().length) / 2d;
                    if (ta.important && tb.getKey().important) {
                        cosImp += ta.length * tb.getKey().length;
                        overImp += (ta.length + tb.getKey().length) / 2d;
                    }
                    if (ta.veryImportant && tb.getKey().veryImportant) {
                        cosVeryImp += ta.length * tb.getKey().length;
                        overVeryImp += (ta.length + tb.getKey().length) / 2d;
                    }
                    tb.setValue(true);
                    break;
                }
            }
        }

        double totalCos = Math.sqrt(aTotalCos * bTotalCos);
        double totalOver = (aTotalOver + bTotalOver) / 2;
        double totalCosImp = Math.sqrt(aTotalCosImp * bTotalCosImp);
        double totalOverImp = (aTotalOverImp + bTotalOverImp) / 2;
        double totalCosVeryImp = Math.sqrt(aTotalCosVeryImp * bTotalCosVeryImp);
        double totalOverVeryImp = (aTotalOverVeryImp + bTotalOverVeryImp) / 2;

        return new double[]{
                totalCos > 0.000001 ? (cos / totalCos) : 0, // cos similarity
                totalOver > 0.000001 ? (over / totalOver) : 0, // overlap
                totalCosImp > 0.000001 ? (cosImp / totalCosImp) : 0, // cos similarity important
                totalOverImp > 0.000001 ? (overImp / totalOverImp) : 0, // overlap similarity important
                totalCosVeryImp > 0.000001 ? (cosVeryImp / totalCosVeryImp) : (aTotalCosVeryImp < 0.000001 && bTotalCosVeryImp < 0.000001 ? 1 : 0), // cos similarity very important
                totalOverVeryImp > 0.000001 ? (overVeryImp / totalOverVeryImp) : (aTotalOverVeryImp < 0.000001 && bTotalOverVeryImp < 0.000001 ? 1 : 0), // overlap similarity very important
                Math.abs(overVeryImp - totalOverVeryImp) < 0.000001 ? 1d : 0d,
        };
    }

    private List<Word> segment(String text) {
        // FilterModifWord.modifyResult
        List<Term> terms = ToAnalysis.parse(text, segDict).getTerms();

        List<Word> result = new ArrayList<>();
        int slotStart = -1;
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i).getRealName();
            String nextTerm = i + 1 < terms.size() ? terms.get(i + 1).getRealName() : null;
            if ("{".equals(term) && "{".equals(nextTerm)) {
                // slot starts
                if (slotStart >= 0) {
                    addTermsToWords(slotStart, i, terms, result);
                }
                slotStart = i;
                i++;
            } else if (slotStart >= 0 && "}".equals(term) && "}".equals(nextTerm)) {
                // slot ends
                result.add(new Word(terms.subList(slotStart, i + 2).stream().map(Term::getRealName)
                        .collect(Collectors.joining()), "veryImportant"));
                slotStart = -1;
                i++;
            } else if (slotStart < 0) {
                addTermsToWords(i, i + 1, terms, result);
            }
            // else slotStart >= 0: in slot
        }
        if (slotStart >= 0) {
            // {{ xxx $
            addTermsToWords(slotStart, terms.size(), terms, result);
        }

        return result;
    }

    private void addTermsToWords(int from, int to, List<Term> terms, List<Word> words) {
        for (int i = from; i < to; i++) {
            Term t = terms.get(i);
            words.add(new Word(t.getRealName(), veryImportantWords.contains(t.getRealName()) ? "veryImportant" : t.getNatureStr()));
        }
    }

    private List<Word> filterWords(List<Word> words) {
        return words.stream().filter(w -> !punctuationPattern.matcher(w.str).matches()).collect(Collectors.toList());
    }

    private Map<String, Word> buildExtensionMap(List<Word> words) {
        Map<String, Word> map = new HashMap<>();
        for (Word word : words) {
            map.put(word.str, word);
        }
        for (Dict dict : dicts) {
            Word word = map.get(dict.getWord());
            if (word == null) {
                for (String alias : dict.getAliasesArray()) {
                    word = map.get(alias);
                    if (word != null) {
                        break;
                    }
                }
            }
            if (word != null) {
                map.put(dict.getWord(), word);
                for (String alias : dict.getAliasesArray()) {
                    map.put(alias, word);
                }
            }
        }
        return map;
        // TODO synonym
//        String[] base = new String[words.size()];
//        for (int i = 0; i < base.length; i++)
//            base[i] = words.get(i).str;
//        Map<String, String[]> wikiExpended = SynUtil
//                .getWikiSyn(base, "Chinese");
//        Map<String, Word> qMap = new HashMap<>();
//        for (Word ww : words) {
//            qMap.put(ww.str, ww);
//            String[] ss = CilinSynonymFinder.getSynonymArray(ww.str);
//            if (ss != null) {
//                for (String ss1 : ss)
//                    qMap.put(ss1, ww);
//            }
//            ss = wikiExpended.get(ww.str);
//            if (ss != null) {
//                for (String ss1 : ss)
//                    qMap.put(ss1, ww);
//            }
//        }
//        return qMap;
    }

    private static int maxCommonStringLength(String s1, String s2) {
        int size1 = s1.length();
        int size2 = s2.length();
        int[][] matrix = new int[size1 + 1][size2 + 1];
        for (int i = 0; i <= size1; i++) {
            matrix[i][0] = 0;
        }
        for (int i = 0; i <= size2; i++) {
            matrix[0][i] = 0;
        }
        for (int i = 1; i <= size1; i++) {
            for (int j = 1; j <= size2; j++) {
                int equal = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 1 : 0;
                matrix[i][j] = max(matrix[i][j - 1], matrix[i - 1][j], matrix[i - 1][j - 1] + equal);
            }
        }
        return matrix[size1][size2];
    }

    private static int characterOverlap(String s1, String s2) {
        int match = 0;
        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                if (s1.charAt(i) == s2.charAt(j)) {
                    match++;
                    break;
                }
            }
        }
        return match;
    }

}
