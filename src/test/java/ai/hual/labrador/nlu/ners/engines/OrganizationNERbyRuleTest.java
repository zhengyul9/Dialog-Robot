package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import org.junit.Test;
import org.mockito.internal.matchers.Null;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrganizationNERbyRuleTest {

    // To load necessory resources from files.
    private Map<String, Map<String, NERResource>> loadResource(List<String> testFiles) {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> organizationnerResource = new HashMap<>();

        for (String wordlist : testFiles) {
            try {
                InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_OrganizationNERbyRule/" + wordlist + ".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                List<String> result = new ArrayList<>();
                br.lines().forEach(line -> {
                    line = line.trim();
                    if (line.length() == 0)  // ignore empty line
                        return;
                    result.add(line);
                });
                br.close();
                organizationnerResource.put(wordlist, new NERResource(wordlist, result));
            } catch (IOException | NullPointerException e) {
                System.out.println("File not found.");
            }
        }
        resources.put("OrganizationNER", organizationnerResource);
        return resources;
    }


    @Test
    public void testOrganizationNER() {
        String text = "可以去工行办理吗?";
        String text2 = "天坛医院可以报销吗?";

        OrganizationNERbyRule o = new OrganizationNERbyRule();
        List<NERResult.Candidate> results = o.organizationRecognize(text);
        List<NERResult.Candidate> results2 = o.organizationRecognize(text2);


        for (NERResult.Candidate candidate : results) {
            System.out.println("candidate: " + candidate.toString());
        }

        for (NERResult.Candidate candidate : results2) {
            System.out.println("candidate: " + candidate.toString());
        }

        String validation = "[{realEnd=5, pos=机构, realStart=3, recognizer=OrganizationNER, text=工行, entity=, segments=}]";
        assertEquals(validation,results.toString());

        String validation2 = "[{realEnd=4, pos=机构, realStart=0, recognizer=OrganizationNER, text=天坛医院, entity=, segments=}]";
        assertEquals(validation2, results2.toString());
    }
}
