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

public class TimeNERbyRuleTest {

    // To load necessory resources from files.
    private Map<String, Map<String, NERResource>> loadResource(List<String> testFiles) {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> timeResource = new HashMap<>();

        for (String wordlist : testFiles) {
            try {
                InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_TimeNERbyRule/" + wordlist + ".txt");
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
                timeResource.put(wordlist, new NERResource(wordlist, result));
            } catch (IOException | NullPointerException e) {
                System.out.println("File not found.");
            }
        }
        resources.put("TimeNER", timeResource);
        return resources;
    }


    @Test
    public void testTimeRecognize() {
        String text1 = "一年3万交十年追加20万的利息多少";
        //String text2 = "哺乳假两个小时 ";

        TimeNERbyRule o = new TimeNERbyRule();
        List<NERResult.Candidate> result1 = o.timeRecognize(text1);
        //List<NERResult.Candidate> result2 = o.TimeRecognize(text2);

        for (NERResult.Candidate candidate : result1) {
            System.out.println("candidate1: " + candidate.toString());
        }

        //for (NERResult.Candidate candidate : result2) {
        //    System.out.println("candidate2: " + candidate.toString());
        //}

        String validation1 = "[{realEnd=2, pos=时间段, realStart=0, recognizer=TimeNER, text=0001-00-00, entity=一年, segments=}, {realEnd=7, pos=时间段, realStart=5, recognizer=TimeNER, text=0010-00-00, entity=十年, segments=}]";
        assertEquals(validation1, result1.toString());

        //String validation2 = "[{realEnd=4, pos=时间, realStart=0, recognizer=TimeNER, text=多长时间, entity=, segments=}," +
        //        " {realEnd=10, pos=时间段, realStart=7, recognizer=TimeNER, text=一个月, entity=, segments=}]"; //"[{realEnd=4, pos=机构, realStart=0, recognizer=OrganizationNER, text=天坛医院, entity=, segments=}]";
        //assertEquals(validation2, result2.toString());

    }

}

