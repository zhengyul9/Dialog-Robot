package ai.hual.labrador.faq;

import ai.hual.labrador.nlg.answer.AnswerPostprocessor;
import ai.hual.labrador.nlg.answer.ChannelRandomAnswerPostprocessor;
import com.google.common.base.Strings;

public class ChanneledFAQAccessor implements FAQAccessor {

    private FAQAccessor faqAccessor;
    private AnswerPostprocessor postprocessor;

    public ChanneledFAQAccessor(FAQAccessor faqAccessor, String channel) {
        this.faqAccessor = faqAccessor;
        this.postprocessor = new ChannelRandomAnswerPostprocessor(channel);
    }

    @Override
    public FaqAnswer handleFaqQuery(String question) {
        FaqAnswer faqAnswer = faqAccessor.handleFaqQuery(question);
        if (faqAnswer == null)
            return null;
        faqAnswer.setAnswer(postprocessor.process(faqAnswer.getAnswer()));
        if (Strings.isNullOrEmpty(faqAnswer.getAnswer())) {
            return null;
        }
        if (faqAnswer.getHits() != null) {
            faqAnswer.getHits().forEach(x -> x.setAnswer(postprocessor.process(x.getAnswer())));
        }
        return faqAnswer;
    }

}