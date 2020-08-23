package ai.hual.labrador.nlu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A model that contains a list representing a dict.
 * Created by Dai Wentao on 2017/6/28.
 */
public class DictModel implements Serializable {

    /**
     * The list of dict item
     */
    private List<Dict> dict;

    /**
     * Initialize dict model with empty list
     */
    public DictModel() {
        dict = new ArrayList<>();
    }

    /**
     * Initialize dict model with existing list (as reference)
     */
    public DictModel(List<Dict> dict) {
        this.dict = dict;
    }


    public List<Dict> getDict() {
        return dict;
    }

}
