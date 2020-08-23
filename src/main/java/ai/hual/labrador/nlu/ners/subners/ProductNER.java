package ai.hual.labrador.nlu.ners.subners;

import ai.hual.labrador.nlu.ners.*;
import ai.hual.labrador.nlu.ners.engines.ProductNERbyRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NER(name = "ProductNER", dependencies = {"TimeNER"})
public class ProductNER extends DeconflictedNERModule {

    private static final Logger logger = LoggerFactory.getLogger(ProductNER.class);
    private ProductNERbyRule engine;

    //    private final List<String> DEPS = Arrays.asList("TimeNER","PlanNER");

    public ProductNER(@Qualifier("resources") @Autowired NERResources resources) {
        Map<String, NERResource> resourcesOfProductNER = resources.getResources().get(ProductNER.class.getSimpleName());
        if (resourcesOfProductNER != null) {
            this.engine = new ProductNERbyRule(resourcesOfProductNER);
        }
    }

    @Override
    public NERResult recognizeDeconflicted(String text, List<List<NERResult.Candidate>> deconflictedResult) {
        List<NERResult.Candidate> candidates = new ArrayList<>();
        if (deconflictedResult.size() != 0) {
            for (List<NERResult.Candidate> res : deconflictedResult) {
                List<NERResult.Candidate> cands = recognizer(res, text);
                if (cands != null)
                    candidates.addAll(cands);
            }
        } else {
            List<NERResult.Candidate> cands = recognizer(null, text);
            if (cands != null)
                candidates.addAll(cands);
        }
        return new NERResult(text, candidates);
    }

    private List<NERResult.Candidate> recognizer(List<NERResult.Candidate> dependnerResult, String text) {
        return engine == null ? null : engine.recognizer(dependnerResult, text);
    }

}
