package ai.hual.labrador.train.trainers;

import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.NLU;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.train.Corpus;
import ai.hual.labrador.train.Data;
import ai.hual.labrador.train.TrainTag;
import ai.hual.labrador.train.Trainer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SimpleGrammarTrainerWithNLU implements Trainer<GrammarModel> {

    private static final String WITH_DELIMITER_AFTER = "(?<=%1$s)";
    private static final String WITH_DELIMITER_PREV = "(?=%1$s)";
    private NLU nlu;

    public SimpleGrammarTrainerWithNLU(NLU nlu) {
        this.nlu = nlu;
    }

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
            String content = corpus.getContent();
            String contentRegex = contentToIntentRegex(this.nlu, content);
            // avoid repeating grammar who has same intent AND content
            for (String intent : intentList) {
                if (!grammarTable.contains(intent, contentRegex)) {
                    grammarTable.put(intent, contentRegex,
                            new Grammar(GrammarType.INTENT_REGEX, intent, contentRegex));
                } // else pass
            }
        }

        return new GrammarModel(
                grammarTable.cellSet().stream()
                        .map(Table.Cell::getValue)
                        .collect(Collectors.toList()));
    }

    protected String contentToIntentRegex(NLU nlu, String content) {
        // NLU
        List<QueryAct> resultList = nlu.understand(content).retrieveHyps();
        QueryAct result = resultList.get(0);
        String pQuery = result.getPQuery();

        // append ".*" to "}}"
        StringJoiner joinerLeft = new StringJoiner(".*");
        String[] splitLeft = pQuery.split(String.format(WITH_DELIMITER_AFTER, "}}"));
        Arrays.stream(splitLeft).forEach(joinerLeft::add);
        String joinLeft = joinerLeft.toString();

        // add ".*" before "{{"
        StringJoiner joinerRight = new StringJoiner(".*");
        String[] splitRight = joinLeft.split(String.format(WITH_DELIMITER_PREV, "\\{\\{"));
        Arrays.stream(splitRight).forEach(joinerRight::add);

        return joinerRight.toString();
    }

}
