package ai.hual.labrador.dialog;

import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;

import java.util.List;

/**
 * A dict handler that handle corpus generated dict model and dict data in database.
 * Created by Dai Wentao on 2017/6/28.
 */
public class DictHandler {


    private final List<Dict> dict;

    public DictHandler(List<Dict> dict) {
        this.dict = dict;
    }

    /**
     * Generate model based on a given {@link DictModel}, modifying that base model and return it.
     *
     * @param base The model that dict handler generation bases on
     * @return The modified base model
     */
    public DictModel handleDict(DictModel base) {
        dict.forEach(word -> base.getDict().add(new Dict(word.getLabel(), word.getWord(), word.getAliases())));
        return base;
    }

}
