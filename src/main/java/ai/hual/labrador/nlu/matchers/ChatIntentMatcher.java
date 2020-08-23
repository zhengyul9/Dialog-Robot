package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.matchers.refusers.SlotExistRejector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.constants.SystemIntents.CHAT_INTENT;
import static ai.hual.labrador.nlu.constants.SystemIntents.CHAT_UNKNOWN_INTENT;

@Component("chatIntentMatcher")
public class ChatIntentMatcher implements IntentMatcher {

    public static final String CHAT_ANSWER_SLOT_KEY = "chatAnswer";
    public static final String REFUSE_CHAT_PROP_NAME = "nlu.chatIntentMatcher.reject";
    public static final String REFUSE_CHAT_BY_SLOT = "slotExist";

    private FAQAccessor chatAccessor;
    private IntentMatcherRejector rejector;

    public ChatIntentMatcher(@Autowired AccessorRepository accessorRepository, @Autowired Properties properties) {
        if (accessorRepository.getFAQAccessor() == null)
            throw new NLUException("FaqAccessor not exist");
        chatAccessor = accessorRepository.getChatAccessor();
        if (properties.getProperty(REFUSE_CHAT_PROP_NAME) != null) {
            switch (properties.getProperty(REFUSE_CHAT_PROP_NAME)) {
                case REFUSE_CHAT_BY_SLOT:
                    rejector = new SlotExistRejector();
                    break;
                default:
                    rejector = null;
            }
        }
    }

    @Override
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {
        List<QueryAct> queryActsCopy = queryActs.stream()
                .map(QueryAct::new)
                .collect(Collectors.toList());
        assert !queryActsCopy.isEmpty();
        QueryAct bestAct = queryActsCopy.get(0);
        // try to reject
        if (rejector != null && rejector.reject(queryActs)) {
            bestAct.setScore(0);
            bestAct.setIntent(CHAT_UNKNOWN_INTENT);
            return Arrays.asList(bestAct);
        }
        String query = bestAct.getQuery();
        FaqAnswer faqAnswer = chatAccessor.handleFaqQuery(query);
        if (faqAnswer == null || faqAnswer.getScore() <= 0) {
            bestAct.setScore(0);
            bestAct.setIntent(CHAT_UNKNOWN_INTENT);
        } else {
            double score = faqAnswer.getScore();
            bestAct.getSlots().put(CHAT_ANSWER_SLOT_KEY, new SlotValue(faqAnswer));
            bestAct.setScore(score);
            bestAct.setIntent(CHAT_INTENT);
        }
        return Arrays.asList(bestAct);
    }
}
