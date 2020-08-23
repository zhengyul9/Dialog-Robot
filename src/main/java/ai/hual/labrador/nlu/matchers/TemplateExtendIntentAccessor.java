package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.QueryAct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public interface TemplateExtendIntentAccessor {
    List<List<Float>> getDistanceMartix(Properties properties);
    Map<String, Integer> getWordCorpus(Properties properties);
}



