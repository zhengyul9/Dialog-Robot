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

public class LocationNERbyRuleTest {

    // To load necessory resources from files.
    private Map<String, Map<String, NERResource>> loadResource(List<String> testFiles) {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> LocationResource = new HashMap<>();

        for (String wordlist : testFiles) {
            try {
                InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_LocationNERbyRule/" + wordlist + ".txt");
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
                LocationResource.put(wordlist, new NERResource(wordlist, result));
            } catch (IOException | NullPointerException e) {
                System.out.println("File not found.");
            }
        }
        resources.put("LocationNER", LocationResource);
        return resources;
    }


    @Test
    public void testLocationRecognize() {
        //String text1 = "北京市西城区月坛街道复兴门职场打卡时间?";
        String text1 = "那就在我公司一层xx餐厅或麦当劳见怎么样？";
        //String text2 = "北京市昌平职场打卡方式有哪种";

        LocationNERbyRule o = new LocationNERbyRule();
        List<NERResult.Candidate> result1 = o.locationRecognize(text1);
        //List<NERResult.Candidate> result2 = o.locationRecognize(text2);


        for (NERResult.Candidate candidate : result1) {
            System.out.println("candidate1: " + candidate.toString());
        }

        //for (NERResult.Candidate candidate : result2) {
        //   System.out.println("candidate2: " + candidate.toString());
        //}

        //String validation1 = ""; //"[{realEnd=5, pos=机构, realStart=3, recognizer=OrganizationNER, text=工行, entity=, segments=}]";
        //assertEquals(validation1,result1.toString());

        //String validation2 = ""; //"[{realEnd=4, pos=机构, realStart=0, recognizer=OrganizationNER, text=天坛医院, entity=, segments=}]";
        //assertEquals(validation2, result2.toString());

    }
}

