package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.subners.AgeNER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgeNERbyRule {
    private static final String BELONG_TO_NER_NAME = "AgeNER";
    private static final String RESOURCES_DIR = "Resources_AgeNERbyRule";
    private static final String USED_REGEX_FILE = RESOURCES_DIR + "/" + "age_regex.txt";
    private static final Logger logger = LoggerFactory.getLogger(AgeNER.class);
    List<String> regex = this.getRegexList(USED_REGEX_FILE);
    String patterns = "(" + String.join("|", regex) + ")";

    // regex starts here
    //String allPattern = patterns;
    Pattern p = Pattern.compile(patterns);
    // regex ends here

    //获取词表内容, 并返回一个List<String>
    public List<String> getWordList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) // ignore blank line
                    return;
                if (line.charAt(0) == '#') // ignore
                    return;
                result.add(line);
            });
            br.close();
            logger.debug("getWordList finished in PersonNER: " + result.toString());
            return result;
        } catch (IOException ex) {
            logger.error("getWordList error in PersonNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    // getRegexList has not been used yet
    public List<String> getRegexList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0)
                    return;
                if (line.charAt(0) == '#')
                    return;
                result.add(line);
            });
            br.close();
            return result;
        } catch (IOException ex) {
            logger.error("getRegexList error in PersonNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    // recognize all age from text
        public List<NERResult.Candidate> ageRecognize(String text) {
        Matcher m = p.matcher(text);

        List<NERResult.Candidate> candidates = new ArrayList<>();
        while(m.find()){    // get all spans of matches str
            candidates.add(new NERResult.Candidate(m.start(), m.end(), "年龄", "AgeNER", text.substring(m.start(), m.end()), "", ""));
        }
        return candidates;
    }
}

