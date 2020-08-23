package ai.hual.labrador.nlu.matchers;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.annotators.NERAnnotator;
import ai.hual.labrador.nlu.annotators.NumAnnotator;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.resourceFormats.*;
import com.google.common.collect.ListMultimap;
import javafx.util.Pair;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ai.hual.labrador.nlu.GrammarType.INTENT_SYNTPL;

@Component("templateExtendIntentMatcher")
public class TemplateExtendIntentMatcher implements IntentMatcher {


    public Forest forest;
    private List<HashMap> templateMapList;
    private MainTool mainTool=new MainTool();

    public static final String KEY_TEMPLATEEXTENDINTENTMATCHER_EXTEND = "nlu.TemplateExtendIntentMatcher.extend";
    public static final String DEFAULT_TEMPLATEEXTENDINTENTMATCHER_CLASS_DICT = String.valueOf(true);

    public TemplateExtendIntentMatcher(@Autowired GrammarModel grammarModel,@Autowired Properties properties) throws IOException {

        Logger log = Logger.getLogger("testTemplate");

        MainTool mainTool=new MainTool();
        List<HashMap> templateMapListGen=new ArrayList<>();



        this.forest=mainTool.getForest();

        TemplateList templateList=new TemplateList();

//        从数据库中读取模板
        List<HashMap> templateMapList=new ArrayList<>();
        for(int i=0;i<grammarModel.getGrammars().size();i++){

            if(INTENT_SYNTPL==grammarModel.getGrammars().get(i).getType()){
                HashMap templateMap=new HashMap();
                String templateStr=grammarModel.getGrammars().get(i).getContent();

                List<Pair<String,String>> template=templateList.toTemplateFromStr(templateStr);

                String intent=grammarModel.getGrammars().get(i).getLabel();



                templateMap.put("templateStr",templateStr);
                templateMap.put("template",template);
                templateMap.put("templateRawIntent",intent);
                templateMap.put("templateRaw",template);
                templateMap.put("templateRawStr",templateStr);
                templateMapList.add(templateMap);
                log.info("templateList:@"+templateMap.get("template").toString());
            }
        }

        if (Boolean.parseBoolean(properties.getProperty(KEY_TEMPLATEEXTENDINTENTMATCHER_EXTEND, DEFAULT_TEMPLATEEXTENDINTENTMATCHER_CLASS_DICT))) {
            templateMapListGen=mainTool.genTemplate(templateMapList);
        }

        templateMapListGen.addAll(templateMapList);
        this.templateMapList=templateMapListGen;
    }



    @Override
    public List<QueryAct> matchIntent(List<QueryAct> queryActs) {

        Logger log = Logger.getLogger("testTest");

        long startTime=System.currentTimeMillis();   //获取开始时间
        List<HashMap> queryMapList=new ArrayList<>();
        HashMap queryMap=new HashMap();

//        获得了NER处理过的queryAct
        QueryElement queryElement=new QueryElement();
        QueryAct queryAct=new QueryAct();
        List<List> queryElementList=new ArrayList<>();

        for(int i=0;i<queryActs.size();i++){

            queryAct=queryActs.get(i);
            ListMultimap<String, SlotValue> slots=queryAct.getSlots();
            List<String> keyList=new ArrayList<>(slots.keySet());

            for(int j=0;j<keyList.size();j++){
                List<SlotValue> slotValues=slots.get((String)keyList.get(j));
                for(int k=0;k<slotValues.size();k++){
                    SlotValue slotValue=slotValues.get(k);
                    List singleElement=new ArrayList();
                    singleElement.add(slotValue.getRealStart());
                    singleElement.add(slotValue.getRealEnd());
                    singleElement.add((String)keyList.get(j));
                    singleElement.add("entity");
                    singleElement.add(slotValue);
                    if(queryElement.isContain(queryElementList,singleElement)==false){
                        queryElementList.add(singleElement);
                    }
                }
            }
        }
        long endTime=System.currentTimeMillis(); //获取结束时间
        log.info("添加实体element程序运行时间： "+(endTime-startTime)+"ms");

        startTime=System.currentTimeMillis();   //获取开始时间
        //        将一个query分词
        String queryStr = queryActs.get(0).getQuery();
        List<List> querySplitAnntator=new ArrayList<>();
        for(int j=0;j<queryElementList.size();j++){
            String segQuery1=queryStr.substring(0,(int)queryElementList.get(j).get(0));
            List<List> querySplitAnntatorPre=queryElement.getQuerySplitElement(segQuery1,forest,0);
            String segQuery2=queryStr.substring((int)queryElementList.get(j).get(1),queryStr.length());
            List<List> querySplitAnntatorBeh=queryElement.getQuerySplitElement(segQuery2,forest,(int)queryElementList.get(j).get(1));
//            实体前面部分分词
            querySplitAnntator.addAll(querySplitAnntatorPre);
//            实体后面的部分分词
            querySplitAnntator.addAll(querySplitAnntatorBeh);
        }

//        query整个分词
        List<List> querySplitAnntatorAll=queryElement.getQuerySplitElement(queryStr,forest,0);

        queryElementList.addAll(querySplitAnntatorAll);

        queryElementList.addAll(querySplitAnntator);
//            第二次去重
        queryElementList=queryElement.wipeRepetite(queryElementList);
//            排序



        queryElementList=mainTool.queryElementSort(queryElementList);

//        将queryElement打印出来
        endTime=System.currentTimeMillis(); //获取结束时间
        log.info("添加所有element程序运行时间： "+(endTime-startTime)+"ms");
        log.info("queryElementList："+queryElementList.toString());

        queryMap.put("queryStr",queryAct.getQuery());
        queryMap.put("queryElement",queryElementList);

        queryMapList.add(queryMap);


//        queryMapList=MainTool.getQueryElement(queryMapList,ner,na,forest);


        startTime=System.currentTimeMillis();   //获取开始时间
        queryMapList=mainTool.matchedMultipleTemplateRaw(templateMapList,queryMapList);
        endTime=System.currentTimeMillis(); //获取结束时间
        log.info("匹配时间： "+(endTime-startTime)+"ms");

        startTime=System.currentTimeMillis();   //获取开始时间


        List<QueryAct> newQueryActs=new ArrayList<>();
//        将意图取出
        for(int i=0;i<queryMapList.size();i++){

            List<HashMap> matchedList=(List)queryMapList.get(i).get("matchedTemplateList");
            String intent="";
            String regex="";
            List pathList;
            String PQuery="";
            List<SlotValue> slotList=new ArrayList();
            if(matchedList!=null&&matchedList.size()>0){
                HashMap matchedMap=matchedList.get(0);
                intent=(String)matchedMap.get("templateRawIntent");
                regex=(String)matchedMap.get("templateRawStr");
                PQuery=(String)matchedMap.get("templateRawStr");
                pathList=(List)matchedMap.get("possiblePathList");
                for(int j=0;j<pathList.size();j++){
                    HashMap pathMap=(HashMap)pathList.get(i);
                    List pathElementList=(List)pathMap.get("pathElementList");
                    for(int k=0;k<pathElementList.size();k++){
                        List Element=(List)pathElementList.get(k);
                        if(Element.size()>4){
                            slotList.add((SlotValue)Element.get(4));
                        }
                    }
                }
            }

            if("".equals(intent)==false){
                QueryAct newQueryAct=new QueryAct();
                newQueryAct.setIntent(intent);
                newQueryAct.setScore(1);
                newQueryAct.setRegex(regex);
                newQueryAct.setQuery(queryStr);
                newQueryAct.setPQuery(PQuery);
                for(SlotValue slotValue : slotList){
                    newQueryAct.getSlots().put(slotValue.key, slotValue);
                }
                newQueryActs.add(newQueryAct);
            }
        }

        if(newQueryActs.size()==0){
            queryAct=new QueryAct(queryActs.get(0));
            queryAct.setScore(0.0);
            queryAct.setIntent(SystemIntents.UNKNOWN);
            newQueryActs.add(queryAct);
        }

        endTime=System.currentTimeMillis(); //获取结束时间
        log.info("queryAct生成匹配时间： "+(endTime-startTime)+"ms");
        return newQueryActs;
    }


}