package ai.hual.labrador.faq;

import ai.hual.labrador.faq.utils.DetectionJudge;
import ai.hual.labrador.faq.utils.DetectionResult;
import ai.hual.labrador.faq.utils.DetectionStatus;
import org.apache.commons.lang3.StringUtils;


public class FAQDetectionMethod {

    public static DetectionResult extendQuestionMountDetection(Integer size) {
        Integer extendQuestionLower = 5;
        Integer extendQuestionUpper = 30;
        DetectionJudge currentJudge = DetectionJudge.APPROPRIATE;
        DetectionStatus currentStaus = DetectionStatus.NOTBAD;
        if (size == null) {
            currentJudge = DetectionJudge.LITTLE;
            currentStaus = DetectionStatus.EMERGENT;
        } else if (size < extendQuestionLower - 1) {
            currentJudge = DetectionJudge.LITTLE;
            currentStaus = DetectionStatus.EMERGENT;
        } else if (size > extendQuestionUpper - 1) {
            currentJudge = DetectionJudge.MUCH;
            currentStaus = DetectionStatus.EMERGENT;
        }
        return new DetectionResult(size, currentStaus, currentJudge);
    }

    public static DetectionResult questionLengthDetection(String content) {
        int length = countChineseAndEnglistStrInput(content);
        int upper = 30, lower = 4;
        DetectionJudge currentJudge = DetectionJudge.APPROPRIATE;
        DetectionStatus currentStaus = DetectionStatus.NOTBAD;
        if (length < lower) {
            currentJudge = DetectionJudge.SHORT;
            currentStaus = DetectionStatus.EMERGENT;
        } else if (length > upper) {
            currentJudge = DetectionJudge.LONG;
            currentStaus = DetectionStatus.EMERGENT;
        }
        return new DetectionResult(length, currentStaus, currentJudge);
    }

    public static DetectionResult answerLengthDetection(String content) {
        content = replaceStripHtml(content);
        int length = countChineseAndEnglistStrInput(content);
        int upper = 100;
        DetectionJudge currentJudge = DetectionJudge.APPROPRIATE;
        DetectionStatus currentStaus = DetectionStatus.NOTBAD;
        if (length > upper) {
            currentJudge = DetectionJudge.LONG;
            currentStaus = DetectionStatus.EMERGENT;
        }
        return new DetectionResult(length, currentStaus, currentJudge);
    }

    /**
     * 统计字符串的实际长度,类似单词的多个字符算一个,多个数字例如电话号则算一个,多个连续中文标点也只算一个.
     *
     * @param content 输入内容
     * @return 字符实际长度
     */
    public static int countChineseAndEnglistStrInput(String content) {
        if (!StringUtils.isNotEmpty(content)) return 0;
        int count = 0;
        boolean hasEnglish = false;
        boolean hasNumber = false;
        for (int i = 0; i < content.length(); i++) {
            //获取此字符的UniCodeBlock
            char currentChar = content.charAt(i);
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(currentChar);
            //  GENERAL_PUNCTUATION 判断中文的“号
            //  CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号
            //  HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号
            if (ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                    || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
                if (i < content.length() - 1 && currentChar == content.charAt(i + 1))
                    continue;
            }

            if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                    || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                    || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {

                if (hasEnglish) {
                    count += 1;
                    hasEnglish = false;
                }
                count += 1;
            }
            if ((currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= 'a' && currentChar <= 'z')) {
                hasEnglish = true;
            } else {// 空格,数字和特殊字符,英文字符都不算
                if (hasEnglish) {
                    count += 1;
                    hasEnglish = false;
                }
            }
            if ((isNum(currentChar))) {
                hasNumber = true;
            } else if (currentChar == '.' && i > 0 && i < content.length() - 1
                    && isNum(content.charAt(i - 1)) && isNum(content.charAt(i + 1))) {
                //小数,主要是英文和数字也有可能混合,逻辑有点复杂了
                hasNumber = true;
            } else {// 空格,特殊字符,英文字符都不算
                if (hasNumber) {
                    count += 1;
                    hasNumber = false;
                }
            }
        }
        if (hasEnglish) {
            count += 1;
        }
        if (hasNumber) {
            count += 1;
        }
        return count;
    }

    public static String replaceStripHtml(String content) {
        if (!StringUtils.isNotEmpty(content)) return "";
        // <p>段落替换为换行
        content = content.replaceAll("<p .*?>", "\r\n");
        // <br><br/>替换为换行
        content = content.replaceAll("<br\\s*/?>", "\r\n");
        // 去掉其它的<>之间的东西
        content = content.replaceAll("\\<.*?>", "");
        // 去掉空格
        content = content.replaceAll(" ", "");
        return content;
    }

    private static boolean isNum(char a) {
        return a >= '0' && a <= '9';
    }

    public static void main(String[] args) {
        String content = "。。。。。。。你好啊年轻人,真实很少见你这样愚蠢的额人类了";
        System.out.println(content);
        System.out.println(content.length());
        System.out.println(countChineseAndEnglistStrInput(content));
        assert (22 == countChineseAndEnglistStrInput(content));
        content = "中英文混合输入hello world";
        System.out.println(content);
        System.out.println(content.length());
        System.out.println(countChineseAndEnglistStrInput(content));
        assert (9 == countChineseAndEnglistStrInput(content));
        String cont2 = null;
        System.out.println(countChineseAndEnglistStrInput(cont2));
    }
}
