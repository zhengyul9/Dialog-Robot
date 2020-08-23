package ai.hual.labrador.nlu.resort.faq;

import java.util.List;

public interface FaqResort {
    List<FaqGroup> resort(List<FaqScores> faqScoresList);
}
