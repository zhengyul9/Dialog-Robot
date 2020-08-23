package ai.hual.labrador.faq;

public interface FAQAccessor {

    FaqAnswer handleFaqQuery(String question);
}
