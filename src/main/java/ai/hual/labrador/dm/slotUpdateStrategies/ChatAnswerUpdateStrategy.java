package ai.hual.labrador.dm.slotUpdateStrategies;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.Context;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.nlu.QueryAct;

import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_QUERY_NAME;

public class ChatAnswerUpdateStrategy implements SlotUpdateStrategy {
    private FAQAccessor chatAccessor;

    @Override
    public void setUp(String s, Map<String, ContextedString> map, AccessorRepository accessorRepository) {
        this.chatAccessor = accessorRepository.getChatAccessor();
    }

    @Override
    public Object update(QueryAct queryAct, Object o, Context context) {
        String query = (String) context.getSlots().get(SYSTEM_QUERY_NAME);
        FaqAnswer answer = chatAccessor.handleFaqQuery(query);
        if (answer != null) {
            return answer;
        }
        return null;    // clear the slot if no faqAnswer this turn, refresh
    }
}
