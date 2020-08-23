package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.annotators.GrammarAnnotator;
import ai.hual.labrador.nlu.constants.SystemIntents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static ai.hual.labrador.dialog.KnowledgeHandler.QUERY_DATATYPE_SCORE;
import static ai.hual.labrador.utils.StringUtils.isWholeSlot;

@Component("templateIntentMatcher")
public class TemplateIntentMatcher implements IntentMatcher {

    public static final int TEMPLATE_SCORE = MATCHER_SCORE_UPPER_BOUND;

    /**
     * Just an assumption.
     * 1.1^100 = 13780.61233982238
     */
    private static final double MAX_SLOT_CUMULATIVE_PRODUCT = 15000d;

    /**
     * With above assumption, and the assumption that handwriting grammar will
     * not have tiny score (anywhere close to {@value ai.hual.labrador.dialog.KnowledgeHandler#QUERY_DATATYPE_SCORE}),
     * then, this value will be a valid lower bound for handwriting templates,
     * and o.t.h a upper bound for kg templates
     */
    private static final double HANDWRITING_LOWER_BOUND = QUERY_DATATYPE_SCORE * MAX_SLOT_CUMULATIVE_PRODUCT;

    private GrammarAnnotator grammarAnnotator;

    public TemplateIntentMatcher(@Autowired GrammarModel grammarModel, @Autowired Properties properties) {
        grammarAnnotator = new GrammarAnnotator(grammarModel, properties);
    }

    @Override
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {
        List<QueryAct> queryActsCopy = queryActs.stream()
                .map(QueryAct::new)
                .collect(Collectors.toList());
        List<QueryAct> result = grammarAnnotator.matchIntent(queryActsCopy);
        result.forEach(act -> {
            // Set to 1 in these scenario, if multiple acts are set to 1, order remained.
            // Priority relationship with other matcher, whose score can reach 1, should be set by order of matchers
            // in bot config.
            if (act.getScore() > HANDWRITING_LOWER_BOUND ||
                    !act.getIntent().equals(SystemIntents.UNKNOWN) && isWholeSlot(act.getPQuery()))
                act.setScore(TEMPLATE_SCORE);
        });
        return result;
    }
}
