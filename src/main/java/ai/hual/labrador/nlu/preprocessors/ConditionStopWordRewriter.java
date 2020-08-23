package ai.hual.labrador.nlu.preprocessors;

import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component("conditionStopWordRewriter")
public class ConditionStopWordRewriter implements Preprocessor {

    private static final String CONDITION_STOP_WORD_PATTERN_FILE = "condition_stop_word_pattern.txt";
    private static final String REPLACEMENT = "";

    private List<Pattern> conditonStopWordPattern = new StringUtils().fetchWords(CONDITION_STOP_WORD_PATTERN_FILE)
            .stream().map(String::trim).map(Pattern::compile).collect(Collectors.toList());

    @Override
    public String preprocess(String query) {
        for (Pattern p : conditonStopWordPattern) {
            Matcher m = p.matcher(query);
            query = m.replaceAll(REPLACEMENT);
        }
        return query;
    }
}
