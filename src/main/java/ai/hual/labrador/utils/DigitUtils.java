package ai.hual.labrador.utils;

import ai.hual.labrador.nlu.SlotValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.pow;

public class DigitUtils {

    private static Logger logger = LoggerFactory.getLogger(DigitUtils.class);

    /**
     * %digits = ("０", 0, "0", 0, "零", 0, "〇", 0,
     * "１", 1, "1", 1, "一", 1, "壹", 1,
     * "２", 2, "2", 2, "二", 2, "貳", 2, "贰", 2, "兩", 2, "两", 2,
     * "３", 3, "3", 3, "三", 3, "參", 3, "叄", 3, "叁", 3,
     * "４", 4, "4", 4, "四", 4, "肆", 4,
     * "５", 5, "5", 5, "五", 5, "伍", 5,
     * "６", 6, "6", 6, "六", 6, "陸", 6, "陆", 6,
     * "７", 7, "7", 7, "七", 7, "柒", 7,
     * "８", 8, "8", 8, "八", 8, "捌", 8,
     * "９", 9, "9", 9, "九", 9, "玖", 9);
     */
    private static final Map<String, Integer> digits = new HashMap<String, Integer>() {{
        put("０", 0);
        put("0", 0);
        put("零", 0);
        put("〇", 0);
        put("１", 1);
        put("1", 1);
        put("一", 1);
        put("壹", 1);
        put("２", 2);
        put("2", 2);
        put("二", 2);
        put("貳", 2);
        put("贰", 2);
        put("兩", 2);
        put("两", 2);
        put("３", 3);
        put("3", 3);
        put("三", 3);
        put("參", 3);
        put("叄", 3);
        put("叁", 3);
        put("４", 4);
        put("4", 4);
        put("四", 4);
        put("肆", 4);
        put("５", 5);
        put("5", 5);
        put("五", 5);
        put("伍", 5);
        put("６", 6);
        put("6", 6);
        put("六", 6);
        put("陸", 6);
        put("陆", 6);
        put("７", 7);
        put("7", 7);
        put("七", 7);
        put("柒", 7);
        put("８", 8);
        put("8", 8);
        put("八", 8);
        put("捌", 8);
        put("９", 9);
        put("9", 9);
        put("九", 9);
        put("玖", 9);
    }};

    public static double getDigits(String input) {
        String p = "(.+?)(?:分之|/)(.+)";
        Pattern r = Pattern.compile(p);
        Matcher matcher = r.matcher(input);
        if (matcher.find()) {    // fraction number
            String numerator;
            String denominator;
            if (input.contains("分之")) {
                numerator = matcher.group(2);
                denominator = matcher.group(1);
            } else {    // contains '/'
                numerator = matcher.group(1);
                denominator = matcher.group(2);
            }
            return chineseToEnglishFull(numerator) / chineseToEnglishFull(denominator);
        } else  // not fraction
            return chineseToEnglishFull(input);
    }

    public static double chineseToEnglishFull(String s) {
        int cNumLength;
        int negative = 0;
        boolean afterDecimal = false;
        double power = 0;
        double total = 0, levelTotal = 0;

        s = s.replaceAll("万亿", "兆");
        s = s.replaceAll("萬億", "兆");
        s = s.replaceAll("亿万", "兆");
        s = s.replaceAll("億萬", "兆");
        s = s.replaceAll("個", "");
        s = s.replaceAll("个", "");
        s = s.replaceAll("廿", "二十");
        s = s.replaceAll("卄", "二十");
        s = s.replaceAll("卅", "三十");
        s = s.replaceAll("卌", "四十");

        cNumLength = s.length();
        for (int i = 0; i < cNumLength; i++) {
            char cChar = s.charAt(i);
            if (i == 0 && (cChar == '负' || cChar == '負' || cChar == '-')) {
                negative = 1;
            } else if (i == 0 && (cChar == '第' || cChar == '+')) { //ordinal
                // Do nothing, handled elsewhere
            } else if (cChar == '點' || cChar == '点' || cChar == '.' || cChar == '．' || cChar == '。') {
                afterDecimal = true;
                power = -1;
            } else if (cChar == '兆') {
                power = 12;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * pow(10, power);
                levelTotal = 0;
                power -= 4;
            } else if (cChar == '億' || cChar == '亿') {
                power = 8;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * pow(10, power);
                levelTotal = 0;
                power -= 4;
            } else if (cChar == '万' || cChar == '萬') {
                power = 4;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * pow(10, power);
                levelTotal = 0;
                power -= 4;
            } else if (cChar == '千' || cChar == '仟') {
                levelTotal += 1000;
            } else if (cChar == '百' || cChar == '佰') {
                levelTotal += 100;
            } else if (cChar == '十' || cChar == '拾') {
                levelTotal += 10;
            } else if (cChar == '零' || cChar == '0' ||
                    cChar == '〇' || cChar == '０') {
                power = 0;
            } else if (digits.containsKey(String.valueOf(cChar))) {
                int digitVal = digits.get(String.valueOf(cChar));
                if (afterDecimal) {
                    levelTotal += digitVal * pow(10, power);
                    power--;
                    while (i + 1 < cNumLength && digits.containsKey(String.valueOf(s.charAt(i + 1)))) {
                        levelTotal += digits.get(String.valueOf(s.charAt(i + 1))) * pow(10, power);
                        power--;
                        i++;
                    }
                } else if (i + 1 < cNumLength) {
                    char nextChar = s.charAt(i + 1);
                    if (nextChar == '十' || nextChar == '拾') {
                        levelTotal += digitVal * 10;
                        i++;
                    } else if (nextChar == '百' || nextChar == '佰') {
                        levelTotal += digitVal * 100;
                        i++;
                    } else if (nextChar == '千' || nextChar == '仟') {
                        levelTotal += digitVal * 1000;
                        i++;
                    } else if (digits.containsKey(String.valueOf(nextChar))) {
                        levelTotal *= 10;
                        levelTotal += digitVal;

                        while (i + 1 < cNumLength && digits.containsKey(String.valueOf(s.charAt(i + 1)))) {
                            levelTotal *= 10;
                            levelTotal += digits.get(String.valueOf(s.charAt(i + 1)));
                            i++;
                        }
                    } else {
                        levelTotal += digitVal;
                    }
                } else {
                    if (i + 1 == cNumLength) {
                        if (i > 0) {
                            char preChar = s.charAt(i - 1);
                            if (preChar == '兆') {
                                levelTotal += digitVal * pow(10, 11);
                            } else if (preChar == '億' || preChar == '亿') {
                                levelTotal += digitVal * pow(10, 7);
                            } else if (preChar == '万' || preChar == '萬') {
                                levelTotal += digitVal * pow(10, 3);
                            } else if (preChar == '千' || preChar == '仟') {
                                levelTotal += digitVal * 100;
                            } else if (preChar == '百' || preChar == '佰') {
                                levelTotal += digitVal * 10;
                            } else {
                                levelTotal += digitVal;
                            }
                        } else if (i == 0)
                            return digitVal;
                    }
                }
            } else {
                logger.warn("Seems to be an error in number {}.", s);
                return 0;
            }
        }
        total += levelTotal;
        if (negative == 1)
            total = -total;
        return total;
    }

    /**
     * Combine consecutive chinese digits using 10 base.
     *
     * @param input the input string
     * @return the combined value
     */
    public static Double consecutiveDigits(String input) {
        int inputLength = input.length();
        double num = 0;
        for (int i = inputLength - 1; i >= 0; i--)
            num += digits.get(input.substring(i, i + 1)) * pow(10, inputLength - i - 1);
        return num;
    }

    /**
     * Combine consecutive digits, which can be mixed up with chinese and arabic using 10 base.
     *
     * @param slots list of number slots
     * @return the combined value
     */
    public static Double combineDigits(List<SlotValue> slots) {
        Collections.reverse(slots);
        double num = 0;
        int i = 0;
        for (SlotValue slot : slots) {
            if ((double) slot.matched > 9) // if number > 9 it's not a chinese char, e.g 10+20 -> {{数字}}{{数字}}
                return null;
            num += (double) slot.matched * pow(10, i++);
            if (slot.realLength == 2)    // e.g 零1
                i++;
        }
        return num;
    }

    /**
     * Combine fractional digits, which can be mixed up with chinese and arabic using 10 base.
     *
     * @param input the input string
     * @param slots list of number slots
     * @return the combined value
     */
    public static Double getFractionalDigits(String input, List<SlotValue> slots) {
        Collections.sort(slots);
        Double denominator;
        if (input.contains("分之")) {
            denominator = (Double) slots.get(0).matched;
        } else {    // contains '/'
            denominator = (Double) slots.get(1).matched;
        }
        if (denominator == 0.0)
            return null;
        else
            return getDigits(input);
    }

    /**
     * Combine decimal digits, which can be mixed up with chinese and arabic using 10 base.
     *
     * @param slots list of number slots
     * @return the combined value
     */
    public static Double getDecimalDigits(String input, List<SlotValue> slots) {
        assert slots.size() == 2;
        Collections.sort(slots);
        Double decimalPart;
        boolean secondConsecutive = slots.get(1).key.equals("连续数字");
        if (slots.get(0).key.equals(slots.get(1).key) || secondConsecutive) {    // all {{数字}}
            decimalPart = (Double) slots.get(1).matched;
            if (!secondConsecutive && decimalPart > 10.0)   // 两点三十六, 2点五十
                return null;
            if (decimalPart % 1 == 0) { // the decimal part has no decimal part, ...
                return getDigits(input);
            } else
                return null;    // decimalPart of a decimal should not be decimal
        } else  // not {{数字}}点{{数字}} and not {{数字}}点{{连续数字}}
            return null;
    }

    /**
     * Get percentage value.
     *
     * @param slots list of number slots
     * @return computed value
     */
    public static Object getPercentDigit(List<SlotValue> slots) {
        return (double) slots.get(0).matched / 100;
    }

    public static double getVagueDigits(String numberString) {
        int length = numberString.length();
        if (length != 2)
            return 0;
        return getDigits(numberString.substring(1));
    }

    /**
     * Transform chinese year number expression to year in Arabic.
     * E.g. 九五 -> 1995, 一七 -> 2017
     *
     * @param yearString chinese expression
     * @return year in int
     */
    public static int getYearDigits(String yearString) {

        int year = 0;
        int strLength = yearString.length();
        if (strLength == 2) {

            char firstChar = yearString.charAt(0);
            char secondChar = yearString.charAt(1);
            if (digits.get(String.valueOf(firstChar)) != null)
                year += 10 * digits.get(String.valueOf(firstChar));
            else return 0;    // false expression
            if (digits.get(String.valueOf(secondChar)) != null)
                year += digits.get(String.valueOf(secondChar));
            else return 0; // false expression

            if (year > 30)
                return 1900 + year;    // 八二
            else
                return 2000 + year;    // 一七
        } else if (strLength == 4) {    // 一九五三， 二零零三

            for (int i = 0; i < strLength; i++) {

                year *= 10;
                char currentChar = yearString.charAt(i);
                if (digits.get(String.valueOf(currentChar)) != null)
                    year += digits.get(String.valueOf(currentChar));
                else
                    return 0;    // false expression
            }
            return year;
        } else
            return 0;    // false expression
    }

    public static void main(String[] args) {

        String s = "10亿两千三百卄一";
        String s1 = "三点14一伍92陆53589793238462643";
        String s2 = "1千两百3十六万5千1百二";
        String ss2 = "两百3十六万5千1百二";
        String s3 = "1千两百3十六万5千1百二十。五5七陆";
        String s4 = "1千两百30六万5千100二";

        BigDecimal bigDecimal1 = new BigDecimal(chineseToEnglishFull(s));
        BigDecimal bigDecimal2 = new BigDecimal(chineseToEnglishFull(s1));
        BigDecimal bigDecimal3 = new BigDecimal(chineseToEnglishFull(s2));
        BigDecimal bigDecimal4 = new BigDecimal(chineseToEnglishFull(s3));
        BigDecimal bigDecimal5 = new BigDecimal(chineseToEnglishFull(s4));

        System.out.println(chineseToEnglishFull(s));
        System.out.println(chineseToEnglishFull(s1));
        System.out.println(chineseToEnglishFull(ss2));
        System.out.println(bigDecimal1.toString());
        System.out.println(bigDecimal2.toString());
        System.out.println(bigDecimal3.toString());
        System.out.println(bigDecimal4.toString());
        System.out.println(bigDecimal5.toString());

        System.out.println(getDigits("三分之2"));
        System.out.println(getDigits("2/五十"));
        System.out.println(getDigits("2点五零"));
        System.out.println(getDigits("20.五三六"));
    }
}