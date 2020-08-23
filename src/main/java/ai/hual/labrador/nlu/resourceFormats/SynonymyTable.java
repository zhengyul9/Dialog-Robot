package ai.hual.labrador.nlu.resourceFormats;


import javafx.util.Pair;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SynonymyTable {

    public HashMap<String, String> synonymyDictSet = new HashMap<String, String>();
    public HashMap<String, String> synonymyDictPro = new HashMap<String, String>();

    public SynonymyTable(){}

    public SynonymyTable(Forest forest,Pair<HashMap<String,String>,HashMap<String,String>> synonymyDict){
//        将同义词典添加在分词字典中
        List<String> keyWordList=new ArrayList<>(synonymyDict.getValue().keySet());
        for(int i=0;i<synonymyDict.getValue().size();i++){
            Library.insertWord( forest, new Value(keyWordList.get(i), synonymyDict.getValue().get(keyWordList.get(i)), String.valueOf(1000)));
        }
        this.synonymyDictSet=synonymyDict.getKey();
        this.synonymyDictPro=synonymyDict.getValue();
    }
}






