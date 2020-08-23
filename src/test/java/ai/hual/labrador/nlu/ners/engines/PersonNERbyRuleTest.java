package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonNERbyRuleTest {
    private static final List<String> PERSONNER_WORDLIST = Arrays.asList("total_person");
    private static final List<String> PERSONNER_REGEXLIST = Arrays.asList("person_property_regex");
    private static final Logger logger = LoggerFactory.getLogger(PersonNERbyRuleTest.class);

    private Map<String, Map<String, NERResource>> loadResource() {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> personner_resources = new HashMap<>();

        List<List<String>> total = Arrays.asList(PERSONNER_WORDLIST, PERSONNER_REGEXLIST);
        for (int i = 0; i < total.size(); ++i) {
            List<String> list = total.get(i);
            for (String wordlist : list) {
                try {
                    InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_PersonNERbyRule/" + wordlist + ".txt");
                    //System.out.println(wordlist);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    List<String> result = new ArrayList<>();
                    br.lines().forEach(line -> {
                        line = line.trim();
                        if (line.length() == 0)  // ignore empty line
                            return;
                        if (line.charAt(0) == '#') // ignore comment line
                            return;
                        result.add(line);
                    });
                    //System.out.println(result);
                    br.close();
                    if (i == 0)
                        personner_resources.put(wordlist, new NERResource(wordlist, result));
                    if (i == 1) {
                        List<Pattern> patterns = result.stream().map(x -> Pattern.compile(x)).collect(Collectors.toList());
                        personner_resources.put(wordlist, new NERResource(wordlist, patterns));
                    }
                } catch (IOException | NullPointerException e) {
                    logger.error("loadResource error in PersonNERbyRuleTest");
                    throw new NLUException("loadResource error in PersonNERbyRuleTest");
                }
            }
        }

        resources.put("PersonNER", personner_resources);
        return resources;
    }

    @Test
    public void testPersonRecognize() {
        String text = "三十多岁的男人和男孩4岁";

        PersonNERbyRule p = new PersonNERbyRule();

        List<NERResult.Candidate> result = p.personRecognize(text);

        for (NERResult.Candidate candidate : result) {
            System.out.println("candidate: " + candidate.toString());
        }

        String validation = "[{realEnd=7, pos=人物, realStart=0, recognizer=PersonNER, text=三十多岁的男人, entity=, segments=}, {realEnd=12, pos=人物, realStart=8, recognizer=PersonNER, text=男孩4岁, entity=, segments=}]";
        assertEquals(validation,result.toString());

    }
}
