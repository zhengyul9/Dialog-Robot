package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.NER;
import ai.hual.labrador.nlu.ners.NERModule;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.engines.TimeNERbyRule;

import java.util.List;

@NER(name = "TimeNER", dependencies = {})
public class TimeNER implements NERModule {
    private TimeNERbyRule engine = new TimeNERbyRule();

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        List<NERResult.Candidate> candidates = engine.timeRecognize(text);
        return new NERResult(text, candidates);
    }

}
/*
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NER(name = "TimeNER", dependencies = {})
public class TimeNER implements NERModule {
    //    private final List<String> DEPS = Arrays.asList("DigitalNER");

    public TimeNER() {
    }

    @Override
    public NERResult recognize(String text, List<NERResult> nerresults) {
        NERResult result = null;
        Pattern pattern = Pattern.compile("[0-9零一二三四五六七八九]+年[0-9零一二三四五六七八九]+月[0-9零一二三四五六七八九]+([日号])");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            List<NERResult.Candidate> candidates = new ArrayList<>();
            String entity = text.substring(matcher.start(), matcher.end());
            String standard = text.substring(matcher.start(), matcher.end());
            NERResult.Candidate candidate = new NERResult.Candidate(matcher.start(), matcher.end(), "time",
                    TimeNER.class.getSimpleName(), entity, standard, "");
            candidates.add(candidate);
            result = new NERResult(text, candidates);
        }
        return result;
    }
}
*/
