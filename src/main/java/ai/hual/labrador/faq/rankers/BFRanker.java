package ai.hual.labrador.faq.rankers;

import ai.hual.labrador.faq.SimpleRanker;
import com.google.common.collect.ImmutableMap;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.primitives.Ints.max;

public class BFRanker extends SimpleRanker<Triple<String, List<BFRanker.Word>, Map<String, BFRanker.Word>>, Pair<String, List<BFRanker.Word>>> {

    public static final String COS_KEY = "cos";
    public static final String OVERLAP_KEY = "overlap";
    public static final String CHAR_OVERLAP_KEY = "char_overlap";
    public static final String MAX_COMMON_STRING_KEY = "mcs";
    public static final String COS_IMP_KEY = "cos_imp";
    public static final String OVERLAP_IMP_KEY = "overlap_imp";

    private static final String punctuationReg = "[\\\\…~.?!':;,/()&\"“”。，：；．！？）+、\\-＂＇｀｜〃〔〕〈〉《》「」『』．〖〗【】（）［］｛｝／￥]";
    private static final Pattern punctuationPattern = Pattern.compile("[\\\\~.?!':;,/()&\"。，、；：？！…—·ˉˇ¨‘’“”々～‖∶＂＇｀｜〃〔〕〈〉《》「」『』．〖〗【】（）［］｛｝／￥]");
    private static final Pattern lettersNumbersPattern = Pattern.compile("[a-zA-Z0-9]+");
    private static final Pattern noLettersNumbersPattern = Pattern.compile("[^a-zA-Z0-9]+");

    static class Word {

        private String str;
        private boolean important;
        private double length;

        Word(String str, String pos) {
            this.str = str;
            this.important = important(pos);
            this.length = getChineseLength(str);
        }

        private static boolean important(String pos) {
            if (pos == null || pos.isEmpty())
                return false;
            char ch = pos.toLowerCase().charAt(0);
            return ch == 'v' || ch == 'n' || ch == 't' || ch == 'm' ||
                    pos.equalsIgnoreCase("vn") || pos.equalsIgnoreCase("nx");
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

    @Override
    protected Triple<String, List<Word>, Map<String, Word>> processQuery(String query) {
        List<Word> queryWords = filterWords(segment(query));
        Map<String, Word> extendedQueryMap = buildExtensionMap(queryWords);
        String nQuery = query.replaceAll(punctuationReg + "| ", "").toLowerCase();
        return Triple.of(nQuery, queryWords, extendedQueryMap);
    }

    @Override
    protected Pair<String, List<Word>> processQuestion(String question) {
        List<Word> questionWords = filterWords(segment(question));
        String nQuestion = question.replaceAll(punctuationReg + "| ", "").toLowerCase();
        return Pair.of(nQuestion, questionWords);
    }

    @Override
    public Map<String, Double> rank(Triple<String, List<Word>, Map<String, Word>> query, Pair<String, List<Word>> question) {
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
                    tb.setValue(true);
                    break;
                }
            }
        }

        double totalCos = Math.sqrt(aTotalCos * bTotalCos);
        double totalOver = (aTotalOver + bTotalOver) / 2;
        double totalCosImp = Math.sqrt(aTotalCosImp * bTotalCosImp);
        double totalOverImp = (aTotalOverImp + bTotalOverImp) / 2;

        return new double[]{
                totalCos > 0.000001 ? (cos / totalCos) : 0, // cos similarity
                totalOver > 0.000001 ? (over / totalOver) : 0, // overlap
                totalCosImp > 0.000001 ? (cosImp / totalCosImp) : 0, // cos similarity important
                totalOverImp > 0.000001 ? (overImp / totalOverImp) : 0, // overlap similarity important
        };
    }

    private List<Word> segment(String text) {
        Result result = ToAnalysis.parse(text);
        return result.getTerms().stream()
                .map(term -> new Word(term.getRealName(), term.getNatureStr()))
                .collect(Collectors.toList());
    }

    private List<Word> filterWords(List<Word> words) {
        return words.stream().filter(w -> !punctuationPattern.matcher(w.str).matches()).collect(Collectors.toList());
    }

    private Map<String, Word> buildExtensionMap(List<Word> words) {
        Map<String, Word> map = new HashMap<>();
        for (Word word : words) {
            map.put(word.str, word);
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
