package ai.hual.labrador.train.trainers;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.constants.IntentLabelDefinition;
import ai.hual.labrador.train.Corpus;
import ai.hual.labrador.train.CorpusAnnotation;
import ai.hual.labrador.train.Data;
import ai.hual.labrador.train.TrainTag;
import ai.hual.labrador.train.Trainer;
import ai.hual.labrador.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleGrammarTrainer implements Trainer<GrammarModel> {

    private static final String WITH_DELIMITER_AFTER = "(?<=%1$s)";
    private static final String WITH_DELIMITER_PREV = "(?=%1$s)";

    private static final String STOP_WORDS_FILE = "simple_stop_words.txt";

    public GrammarModel train(Data data) {

        List<Corpus> corpusList = data.getCorpora();

        // prevent repeated Grammar who has same intent and contentRegex
        Table<String, String, Grammar> grammarTable = HashBasedTable.create();

        for (Corpus corpus : corpusList) {
            // collect intents of this corpus who has positive trainTag
            List<String> intentList = corpus.getIntentMap().entrySet().stream()
                    .filter(e -> e.getValue() == TrainTag.POSITIVE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            ListMultimap<String, String> intentParameters = ArrayListMultimap.create();
            String contentRegex = corpusToIntentRegex(corpus, intentParameters);
            // avoid repeating grammar who has same intent AND content
            for (String intent : intentList) {
                intent = extendIntentWithAttachedParameters(intent, intentParameters);
                if (!grammarTable.contains(intent, contentRegex)) {
                    grammarTable.put(intent, contentRegex,
                            new Grammar(GrammarType.INTENT_REGEX, intent, contentRegex));
                } // else pass
            }
        }

        return new GrammarModel(grammarTable.cellSet().stream()
                .map(Table.Cell::getValue)
                .collect(Collectors.toList()));
    }

    private String extendIntentWithAttachedParameters(String intent, ListMultimap<String, String> intentParameters) {
        // add "&" if "?" already exists or add "?"
        String delimiter = intent.contains(IntentLabelDefinition.INTENT_SLOT_SPLIT_TEXT) ?
                IntentLabelDefinition.SLOT_PARAMS_SPLIT_TEXT : IntentLabelDefinition.INTENT_SLOT_SPLIT_TEXT;
        return intent + delimiter + intentParameters.keySet().stream()
                .map(key -> {
                    String paramString = intentParameters.get(key).stream()
                            .collect(Collectors.joining(IntentLabelDefinition.SLOT_PARAM_VALUES_SPLIT_REGEX));
                    return key + IntentLabelDefinition.SLOT_PARAM_NAME_VALUE_SPLIT_REGEX + paramString;
                }).collect(Collectors.joining(IntentLabelDefinition.SLOT_PARAMS_SPLIT_REGEX));
    }

    /**
     * convert an annotated corpus into a regex, and adding annotations from 0 to 0 to extra intentParameters list.
     * e.g.:
     * "abcde" with 0-0:p:q, 0-0:s:t, 0-1:w:x, 3-5:y:z
     * ->
     * "{{w}}bc{{y}}" with intentParameters p=q&s=t
     *
     * @param corpus           The corpus used to generate intent regex
     * @param intentParameters An empty list, to which the extra intent parameters in the annotation will be added,
     *                         which will be attached to the intent.
     * @return a generated regex intent pattern
     */
    private String corpusToIntentRegex(Corpus corpus, ListMultimap<String, String> intentParameters) {
        List<CorpusAnnotation> annotations = corpus.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.sort(Comparator.comparingInt(CorpusAnnotation::getFrom));

        String result = corpus.getContent();
        StringBuilder replacedResult = new StringBuilder();
        int lastReplacedEnd = 0;
        for (CorpusAnnotation annotation : annotations) {
            int start = annotation.getFrom();
            int end = annotation.getTo();
            String unreplacedPart = result.substring(lastReplacedEnd, start);
            replacedResult.append(StringUtils.escapeRegex(unreplacedPart));
            if (start == 0 && end == 0) {
                intentParameters.put(annotation.getType(), annotation.getValue());
                continue;
            }
            String replacement = (start == 0 ? "{{" : ".*{{") + annotation.getType() +
                    (end == result.length() ? "}}" : "}}.*");
            replacedResult.append(replacement);
            lastReplacedEnd = end;
        }
        // append last part
        result = replacedResult + StringUtils.escapeRegex(result.substring(lastReplacedEnd));
        // eliminate stop words
        for (String word : new StringUtils().fetchWords(STOP_WORDS_FILE))
            result = result.replaceAll(word, "");
        // remove repeated wildcard
        result = result.replaceAll("(?:\\.\\*)+", ".*");

        return result;
    }
}
