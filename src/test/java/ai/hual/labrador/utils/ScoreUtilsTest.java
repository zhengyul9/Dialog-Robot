package ai.hual.labrador.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreUtilsTest {

    @BeforeAll
    static void setup() {
    }

    @Test
    void testPositiveNormalizePrecision() {
        assertTrue(ScoreUtils.positiveNormalize(30d) > ScoreUtils.positiveNormalize(15d));
        // double still does not have enough precision
        assertTrue(ScoreUtils.positiveNormalize(50d) == ScoreUtils.positiveNormalize(30d));
        ListMultimap<String, String> map = ArrayListMultimap.create();
        System.out.println(map.get("a"));
        System.out.println(ScoreUtils.shiftScore(0.6, 1));
    }

    @Test
    void test1() {
        String matchedStr = "{{slot1}}abc{{slot2}}def{{slot3}}";
        String regex1 = "\\{\\{slot1}}.*\\{\\{slot3}}";
        String regex2 = "\\{\\{slot1}}.*\\{\\{slot2}}";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        assertTrue(score1 < score2);   // score1 > score2 when consider real length
    }

    @Test
    void test2() {
        String matchedStr = "{{slot1}}absa{{slot2}}";
        String regex1 = "\\{\\{slot1}}(ab|c)?(t|sa)?\\{\\{slot2}}";
        String regex2 = "\\{\\{slot1}}.*\\{\\{slot2}}";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        assertTrue(score1 == score2);
    }

    @Test
    void test3() {
        String matchedStr = "{{slot1}}absa{{slot2}}";
        String regex1 = "\\{\\{slot1}}(ab|c)?(t|sa)\\{\\{slot2}}";
        String regex2 = "\\{\\{slot1}}.*\\{\\{slot2}}";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        assertTrue(score1 > score2);
    }

    @Test
    void test4() {
        String matchedStr = "{{slot1}}{{slot2}}{{slot3}}";
        String regex1 = "\\{\\{slot1}}\\{\\{slot2}}\\{\\{slot3}}";
        String regex2 = "\\{\\{slot1}}(a|b|c)?(d|e|f)?(g|h|i)?.*";  // malformed
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        assertTrue(score1 > score2);
    }

    @Test
    void test5() {
        String matchedStr = "{{slot1}}abc{{slot1}}{{slot2}}";
        String regex1 = "\\{\\{slot1}}\\{\\{slot2}}";
        String regex2 = "\\{\\{slot1}}.*\\{\\{slot2}}";
        String regex3 = "\\{\\{slot1}}.*\\{\\{slot1}}\\{\\{slot2}}";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        double score3 = ScoreUtils.patternMatchDiscountScore(regex3, matchedStr);
        assertTrue(score3 > score2);
        assertTrue(score2 < score1);    // score2 > score1 when consider real length
    }

    @Test
    void test6() {
        String matchedStr = "{{slot1}}ab{{slot2}}";
        String regex1 = "\\{\\{slot1}}.*\\{\\{slot2}}";
        String regex2 = "\\{\\{slot1}}(ab|st)\\{\\{slot2}}";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        assertTrue(score2 > score1);
        String regex3 = "\\{\\{slot1}}(ab)\\{\\{slot2}}";
        String regex4 = "\\{\\{slot1}}(ab)?\\{\\{slot2}}";
        double score3 = ScoreUtils.patternMatchDiscountScore(regex3, matchedStr);
        double score4 = ScoreUtils.patternMatchDiscountScore(regex4, matchedStr);
        assertTrue(score2 == score3);
        assertTrue(score3 > score4);
    }

    @Test
    void test7() {
        String matchedStr = "ab{{slot1}}cd";
        String regex1 = "ab(\\{\\{slot1}})cd";
        String regex2 = "ab(\\{\\{slot1}})?cd";
        String regex3 = "ab\\{\\{slot1}}cd";
        String regex4 = "ab(\\{\\{slot1}}|\\{\\{slot2}})?cd";
        double score1 = ScoreUtils.patternMatchDiscountScore(regex1, matchedStr);
        double score2 = ScoreUtils.patternMatchDiscountScore(regex2, matchedStr);
        double score3 = ScoreUtils.patternMatchDiscountScore(regex3, matchedStr);
        double score4 = ScoreUtils.patternMatchDiscountScore(regex4, matchedStr);
        assertTrue(score1 > score2);
        assertTrue(score1 == score3);
        assertTrue(score2 == score4);
    }

    @Test
    void testSlotRegex() {
        int slotCount = 0;
        String matchedPart = "{{asd}}sdf{{dsf}}d{{a}}";
        Pattern slotPattern = Pattern.compile("\\{\\{.*?}}");
        Matcher slotMatcher = slotPattern.matcher(matchedPart);
        while (slotMatcher.find())
            slotCount++;
        assertEquals(3, slotCount);
    }

    @Test
    void testComplexEvilPatternScore() {
        String regex = "(退出|不(想)?看了)";
        String matchedStr = "退出登录";
        double score = ScoreUtils.patternMatchDiscountScore(regex, matchedStr);
    }

    @Test
    void testComplexErrorPatternScore() {
        String regex = "(微信|我要|(账|帐)号|退出|扫码|重新|)(登录|注册)";
        String matchedStr = "我要注册声纹";
        double score = ScoreUtils.patternMatchDiscountScore(regex, matchedStr);
    }

    @Test
    void testRegex() {
        String regex = "(?:我要|我想|给我|找点|想)?(.*|看|来|介绍|播放|.*|查找|.*)?({{cartoon}}|.*|{{children}}|{{movie}}|{{education}}|{{doc}}|{{variety}}|{{tv}}|{{sport}})({{category}})?";
        String matchedStr = "播放{{movie}}";
        double score = ScoreUtils.patternMatchDiscountScore(regex, matchedStr);
    }

    @Test
    void testComplexPatternScore() {
        String regex = "(我要|我想|给我|找点|想)?(看|来|介绍|播放|查找)?({{cartoon}}|{{children}}|{{movie}}|{{education}}|{{doc}}|{{variety}}|{{tv}}|{{sport}})({{category}})?";
        String matchedStr = "播放{{movie}}";
        double score = ScoreUtils.patternMatchDiscountScore(regex, matchedStr);
    }

    @Test
    void testPatternScore() {
        String regex = ".*a\\{\\{BC}}.*d(e|t|ab)?";
        String matchedStr = "tyza{{BC}}dsfde";
        double score = ScoreUtils.patternMatchDiscountScore(regex, matchedStr);
    }
}