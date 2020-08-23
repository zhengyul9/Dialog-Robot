package ai.hual.labrador.utils;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.DictModelSerDeser;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility to manipulate strings
 * Created by Dai Wentao on 2017/7/12.
 */
public class StringUtils {

    private static Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static final List<String> regexStr = ImmutableList.of("\\{", "\\.", "\\*", "\\[", "\\+", "\\(", "\\)",
            "\\$", "\\<", "\\?", "\\=");

    public Path loadCustomWords(String fileName) {
        File tempWordFile;
        try {
            byte[] txtBytes = IOUtils.toByteArray(new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            logger.debug("custom word file has {} bytes", txtBytes.length);
            tempWordFile = File.createTempFile("labrador-nlu-custom-word-", ".txt");
            tempWordFile.deleteOnExit();
            Files.write(txtBytes, tempWordFile);

            URL url = tempWordFile.toURI().toURL();
            return Paths.get(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            throw new NLUException("Load custom words from " + fileName + " failed");
        }
    }

    /**
     * Read in dict from file.
     *
     * @param file file containing dict in each line
     * @return list of dict
     */
    public DictModel fetchDicts(String file) {
        DictModelSerDeser dictModelSerDeser = new DictModelSerDeser();
        try {
            byte[] content = IOUtils.toByteArray(new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            return dictModelSerDeser.deserialize(content);
        } catch (IOException ex) {
            throw new NLUException("Error fetching synom words file");
        }
    }

    /**
     * Read in stop words to list as file.
     *
     * @param file file containing words in each line
     * @return list of stop words in string
     */
    public List<String> fetchWords(String file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8))) {
            return br.lines()
                    .filter(line -> !Strings.isNullOrEmpty(line) && line.charAt(0) != '#')
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new NLUException("Error fetching words file");
        }
    }

    /**
     * Replace str[start:end] with replacement
     *
     * @param str         The original string
     * @param start       The start index of replaced interval
     * @param end         The end index of replaced interval
     * @param replacement The string to be placed in the replaced part
     * @return The new string
     */
    public static String replaceSubstring(String str, int start, int end, String replacement) {
        return str.substring(0, start) + replacement + str.substring(end);
    }

    /**
     * Get all combinations of item number.
     *
     * @param bits number of items
     * @return list of String of permutations
     */
    public static List<String> getBinaries(int bits) {
        assert bits > 0;

        List<String> binaries = new ArrayList<>();
        if (bits > 15) {
            bits = 15;
        }

        int max = 1 << bits;
        for (int i = 0; i < max; i++) {
            String binary = Integer.toBinaryString(i);
            while (binary.length() != bits)
                binary = String.format("0%s", binary);
            binaries.add(binary);
        }
        return binaries;
    }

    /**
     * See if the pQuery string is a whole slot. e.g {{xxx}}
     *
     * @param pQuery the pQuery
     * @return true if is whole slot
     */
    public static boolean isWholeSlot(String pQuery) {
        boolean isWholeSlot = false;
        int pQueryLen = pQuery.length();
        if (pQueryLen > Annotator.SLOT_PREFIX.length() + Annotator.SLOT_SUFFIX.length()) {
            if (pQuery.substring(0, 2).equals(Annotator.SLOT_PREFIX) &&
                    pQuery.substring(pQueryLen - Annotator.SLOT_SUFFIX.length(), pQueryLen).equals(Annotator.SLOT_SUFFIX) &&
                    !pQuery.substring(2, pQueryLen - Annotator.SLOT_SUFFIX.length()).contains(Annotator.SLOT_PREFIX)) {
                isWholeSlot = true;
            }
        }
        return isWholeSlot;
    }

    /**
     * Transform SLOT_PREFIX by valid regex expression.
     *
     * @param pattern pattern
     * @return transformed pattern
     */
    public static String patternSlotPrefixToRegex(String pattern) {
        String replace = "\\\\\\{\\\\\\{";
        return pattern.replaceAll("\\{\\{", replace);  // first param is regex, second is not
    }

    /**
     * Reverse of <t>patternSlotPrefixToRegex</t>
     *
     * @param regex regex
     * @return transformed regex
     */
    public static String regexToPattern(String regex) {
        String replace = "{{";
        return regex.replaceAll("\\\\\\{\\\\\\{", replace);  // first param is regex, second is not
    }

    /**
     * Group wildcard {@code .*} with {@code ()}, so it can be extracted conveniently.
     * NOTICE: original group in regex should be convert to non-capturing
     * group using {@code ?:}.
     *
     * @param regex input regex string
     * @return grouped regex string
     */
    public static String groupRegexWildcard(String regex) {
        Pattern pattern = Pattern.compile("\\(.*?\\)\\??");
        Matcher matcher = pattern.matcher(regex);

        int bias = 0;
        while (matcher.find()) {
            int start = matcher.start() + bias;
            int end = matcher.end() + bias;
            String matched = regex.substring(start, end);
            if (matched.contains("|") && matched.contains(".*")) {
                int originLength = matched.length();
                matched = matched.replaceAll("\\|?\\.\\*\\??", ""); // |.*?
                matched = matched.replaceAll("\\.\\*\\??\\|?", ""); // .*?|
                regex = replaceSubstring(regex, start, end, matched);
                int lengthDiff = matched.length() - originLength;
                bias += lengthDiff;
            }
        }
        regex = patternSlotPrefixToRegex(regex);
        return regex.replaceAll("(\\.\\*\\??)", "(.*)");
    }

    /**
     * Escape all regex characters.
     *
     * @param input input string who might contain regex char
     * @return escaped regex
     */
    public static String escapeRegex(String input) {
        input = input.replaceAll("\\\\", "\\\\\\\\");
        for (String str : StringUtils.regexStr) {
            input = input.replaceAll(str, "\\" + str);
        }
        return input;
    }

    public static String replaceRegexWithWildCard(String input) {
        input = input.replaceAll("\\\\", ".*");
        for (String str : StringUtils.regexStr) {
            input = input.replaceAll(str, ".*");
        }
        return input;
    }

}
