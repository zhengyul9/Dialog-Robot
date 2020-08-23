package ai.hual.labrador.dialog.accessors;

import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.annotators.DictAnnotator;

import java.util.List;

public interface DictAccessor {

    DictModel getDictModel();

    DictAnnotator constructDictAnnotator(boolean useNormalDict, List<String> dictTypes);

}
