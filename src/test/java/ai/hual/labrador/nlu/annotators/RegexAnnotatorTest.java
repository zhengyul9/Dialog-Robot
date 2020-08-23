package ai.hual.labrador.nlu.annotators;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexAnnotatorTest {

    @Test
    public void fetchRegexFileTest() {

        RegexAnnotator RA = new RegexAnnotator() {
            @Override
            protected List<LabeledRegex<String>> getRegex() {
                return super.getRegex();
            }
        };

        List<LabeledRegex<String>> regexList = RA.fetchRegexFile("date_regex_file");
        assertTrue(regexList.size() != 0);
    }

}