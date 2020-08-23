package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.constants.SystemIntents.FAQ_INTENT;
import static ai.hual.labrador.nlu.constants.SystemIntents.FAQ_UNKNOWN_INTENT;

@Component("faqIntentMatcher")
public class FaqIntentMatcher implements IntentMatcher {

    public static final String FAQ_ANSWER_SLOT_KEY = "faqAnswer";

    private FAQAccessor faqAccessor;

    public FaqIntentMatcher(@Autowired AccessorRepository accessorRepository, @Autowired Properties properties) {
        if (accessorRepository.getFAQAccessor() == null)
            throw new NLUException("FaqAccessor not exist");
        faqAccessor = accessorRepository.getFAQAccessor();
    }

    @Override
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {
        List<QueryAct> queryActsCopy = queryActs.stream()
                .map(QueryAct::new)
                .collect(Collectors.toList());
        assert !queryActsCopy.isEmpty();
        QueryAct bestAct = queryActsCopy.get(0);
        String query = bestAct.getQuery();
        FaqAnswer faqAnswer = faqAccessor.handleFaqQuery(query);
        if (faqAnswer == null || faqAnswer.getScore() <= 0) {
            bestAct.setScore(0);
            bestAct.setIntent(FAQ_UNKNOWN_INTENT);
        } else {
            double score = faqAnswer.getScore();
            bestAct.getSlots().put(FAQ_ANSWER_SLOT_KEY, new SlotValue(faqAnswer));
            bestAct.setScore(score);
            bestAct.setIntent(FAQ_INTENT);
        }
        return Collections.singletonList(bestAct);
    }
}
