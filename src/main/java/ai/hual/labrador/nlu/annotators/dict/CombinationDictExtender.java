package ai.hual.labrador.nlu.annotators.dict;

import ai.hual.labrador.nlu.Dict;

import java.util.ArrayList;
import java.util.Properties;

import static ai.hual.labrador.utils.AliasWordUtils.combinations;

public class CombinationDictExtender implements DictExtender {
    private int turnOffNum;
    private static final String TURN_OFF_NUM_PROP = "nlu.dictAnnotator.turnOffNum";
    private static final String DEFAULT_TURN_OFF_NUM = "2";

    public CombinationDictExtender(Properties properties) {
        turnOffNum = Integer.parseInt(properties.getProperty(TURN_OFF_NUM_PROP, DEFAULT_TURN_OFF_NUM));
    }

    @Override
    public ArrayList<Dict> extend(Dict dict) {
        ArrayList<Dict> res = new ArrayList<>();
        String word = dict.getWord();
        int k = turnOffNum;
        int n = word.length();

        for (int i = n - k; i < n; i++) {
            ArrayList<Dict> tempRes = combinations(dict, n, i);
            res.addAll(tempRes);
        }

        return res;
    }
}
