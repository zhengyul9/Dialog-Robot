package ai.hual.labrador.nlu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A grammar model that contains a list of grammars which can be serialized to and deserialized from a byte array.
 * Created by Dai Wentao on 2017/6/28.
 */
public class GrammarModel implements Serializable {

    /**
     * The list of grammars in the model
     */
    private List<Grammar> grammars;

    /**
     * Initialize grammar model with empty list
     */
    public GrammarModel() {
        grammars = new ArrayList<>();
    }

    /**
     * Initialize grammar model with existing list (as reference)
     */
    public GrammarModel(List<Grammar> grammars) {
        this.grammars = grammars;
    }

    public List<Grammar> getGrammars() {
        return grammars;
    }

}
