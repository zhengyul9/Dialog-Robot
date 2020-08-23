package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.Dict;

import java.util.ArrayList;

public interface DictExtender {
    ArrayList<Dict> extend(Dict dict);
}
