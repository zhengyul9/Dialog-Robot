package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.subners.PersonNER;
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

public class PersonNERbyRule {
    private static final String BELONG_TO_NER_NAME = "PersonNER";
    private static final String RESOURCES_DIR = "Resources_PersonNERbyRule(deprecated)";
    private static final String USED_PROPERTY_REGEX_FILE = RESOURCES_DIR + "/" + "person_property_regex.txt";
    private static final String USED_TOTAL_PERSON_FILE = RESOURCES_DIR + "/" + "total_person.txt";
    private static final Logger logger = LoggerFactory.getLogger(PersonNER.class);

    List<String> allPerson = this.getWordList(USED_TOTAL_PERSON_FILE);
    //List<String> regex = this.getRegexList(USED_PROPERTY_REGEX_FILE);
    String person = "(" + String.join("|", allPerson) + ")";
    //        System.out.println("person: " + person);
    String pattern1 = "(" + "(?<age1>\\d+|\\D+)(岁|周岁)(的)?" + person + ")";
    String pattern2 = "(" + person + "(?<age2>\\d+|\\D+)(岁|周岁)" + ")";
    String pattern3 = person;
    String allPattern = pattern1 + "|" + pattern2 + "|" + pattern3;

    Pattern p = Pattern.compile(allPattern);

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

    // recognize all person from text
    public List<NERResult.Candidate> personRecognize(String text) {

        Matcher m = p.matcher(text);

//        Map<String, List<NERResult.Candidate>> result = null;
        List<NERResult.Candidate> candidates = new ArrayList<>();
        while (m.find()) {  // get all spans of matches str
//            System.out.println("String matched by m: " + m.group());
            //System.out.println("groupCount: " + m.groupCount());
//            System.out.println("m.start & m.end: " + m.start() + m.end());
            //Pair<Integer, Integer> pair = new Pair<>(m.start(), m.end());
//
//            if (Pattern.matches(pattern1, m.group(0))) {
//                System.out.println("String matched by pattern1: " + m.group("age1"));
//
//            } else if (Pattern.matches(pattern2, m.group(0))) {
//                System.out.println("String matched by pattern2: " + m.group("age2"));
//
//            } else if (Pattern.matches(pattern3, m.group(0))) {
//                System.out.println("String matched by pattern3: " + m.group(0));
//
//            } else {
//                System.out.println("match error");
//            }

            candidates.add(new NERResult.Candidate(m.start(), m.end(), "人物", "PersonNER", text.substring(m.start(), m.end()), "", ""));

        }

//        for (NERResult.Candidate eachPerson : candidates) {
//            System.out.println(eachPerson.toString());
//        }
//        result.put(text, candidates);
//        return result;
        return candidates;
    }
}
