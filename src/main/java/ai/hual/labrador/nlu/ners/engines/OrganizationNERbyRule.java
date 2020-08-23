package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.utils.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganizationNERbyRule {
    private static final String BELONG_TO_NER_NAME = "OrganizationNER";
    private static final String RESOURCES_DIR = "/Resources_OrganizationNERbyRule(deprecated)/organization";
    private static final Logger logger = LoggerFactory.getLogger(OrganizationNERbyRule.class);

//    private String organization = banks + "|" + hospitals;
    private String organization = getWordList(RESOURCES_DIR);

    private Pattern p = Pattern.compile(organization);

    //获取词表内容, 并返回一个List<String>
    private String getWordList(String directory) {
        String[] filenames;
        try {
            filenames = ResourceUtil.getResourceListing(this.getClass(), directory);

        } catch (URISyntaxException | IOException e){
            logger.error("GetResourceListing error in OrganizationNERbyRule.");
            throw new NLUException("Could not getResourceListing correctly in OrganizationNERbyRule.", e);
        }
        StringBuilder sb = new StringBuilder("(");

        List<String> wordlist = new ArrayList<>();

        for (String filename : filenames) {
            filename = directory + '/' + filename;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(filename), StandardCharsets.UTF_8))) {
                br.lines().map(String::trim).filter(x->!x.isEmpty()).filter(x->!x.startsWith("#")).forEach(wordlist::add);
            } catch (IOException ex) {
                throw new NLUException("Could not find file " + String.join(", ", filenames), ex);
            }
        }
        sb.append(String.join("|", wordlist)).append(")");
        logger.debug("getWordList finished in OrganizationNERbyRule: " + sb.toString());
        return sb.toString();
    }


    // recognize all organizations from text
    public List<NERResult.Candidate> organizationRecognize(String text) {
        Matcher m = p.matcher(text);

        List<NERResult.Candidate> candidates = new ArrayList<>();
        while (m.find()) {  // get all spans of matches str
            candidates.add(new NERResult.Candidate(m.start(), m.end(), "机构", "OrganizationNER", text.substring(m.start(), m.end()), "", ""));
        }

        return candidates;
    }

}
