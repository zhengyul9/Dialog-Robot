package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgeNERbyRuleTest {

    // To load necessory resources from files.
    private Map<String, Map<String, NERResource>> loadResource(List<String> testFiles) {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> ageResource = new HashMap<>();

        for (String wordlist : testFiles) {
            try {
                InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_AgeNERbyRule/" + wordlist + ".txt");
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
                br.close();
                ageResource.put(wordlist, new NERResource(wordlist, result));
            } catch (IOException | NullPointerException e) {
                System.out.println("File not found.");
            }
        }
        resources.put("AgeNER", ageResource);
        return resources;
    }


    @Test
    public void testAgeRecognize() {
        String text1 = "3周岁的女孩";
        //String text2 = "哺乳假两个小时 ";

        AgeNERbyRule o = new AgeNERbyRule();
        List<NERResult.Candidate> result1 = o.ageRecognize(text1);
        //List<NERResult.Candidate> result2 = o.TimeRecognize(text2);


        for (NERResult.Candidate candidate : result1) {
            System.out.println("candidate1: " + candidate.toString());
        }

        //for (NERResult.Candidate candidate : result2) {
        //    System.out.println("candidate2: " + candidate.toString());
        //}

        String validation1 = "[{realEnd=3, pos=年龄, realStart=0, recognizer=AgeNER, text=3周岁, entity=, segments=}]"; //"[{realEnd=5, pos=机构, realStart=3, recognizer=OrganizationNER, text=工行, entity=, segments=}]";
        assertEquals(validation1,result1.toString());

        //String validation2 = "[{realEnd=4, pos=时间, realStart=0, recognizer=TimeNER, text=多长时间, entity=, segments=}," +
        //        " {realEnd=10, pos=时间段, realStart=7, recognizer=TimeNER, text=一个月, entity=, segments=}]"; //"[{realEnd=4, pos=机构, realStart=0, recognizer=OrganizationNER, text=天坛医院, entity=, segments=}]";
        //assertEquals(validation2, result2.toString());

    }
}

