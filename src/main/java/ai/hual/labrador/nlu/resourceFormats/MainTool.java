package ai.hual.labrador.nlu.resourceFormats;

import ai.hual.labrador.nlu.*;
import ai.hual.labrador.nlu.annotators.NERAnnotator;
import ai.hual.labrador.nlu.annotators.NumAnnotator;
import javafx.util.Pair;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainTool {


    private InputStream synonymyTableTxt=getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyTable.txt");
    private InputStream stopWordTableTxt=getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/stopWords.txt");

    //        读取停用词表
    List<String> stopWordsList = readStopWords(stopWordTableTxt);
    //        读取同义词表
    Pair<HashMap<String,String>,HashMap<String,String>> synonymyDict=readSynonymyTable(synonymyTableTxt);
    //        初始化同义词表
    Forest forest=new Forest();
    SynonymyTable synonymyTable=new SynonymyTable(forest,synonymyDict);
    //        初始化停用词表
    StopWordsTable stopWordsTable=new StopWordsTable(stopWordsList);



    private static DictModel dictModel;
    private static GrammarModel grammarModel;
    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix","ambiguous_suffix","kernel","limited","prefix","suffix","properties","standard_entity","plans","skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited","tail_minus_one_and_tail_cut_tail_patterns","tail_minus_one_and_tail_stop_patterns","tail_plus_one_patterns","adjust_segment_patterns");

    public MainTool() throws IOException {
    }


    private void genDictModelAndGrammarModel() {
        List<List<String>> total = Arrays.asList(PRODUCTNER_WORDLIST, PRODUCTNER_REGEXLIST);
        List<Dict> dicts = new ArrayList<>();
        List<Grammar> grammars = new ArrayList<>();
        for (int i = 0; i < total.size(); ++i) {
            List<String> list = total.get(i);
            for (String wordlist : list) {
                try {
                    InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/" + wordlist + ".txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    if(i==0){
                        br.lines().forEach(line -> {
                            line = line.trim();
                            if (line.length() == 0) //ignore blank line
                                return;
                            dicts.add(new Dict("__"+wordlist+"__",line));
                        });
                        br.close();
                    }
                    if(i == 1)
                    {
                        br.lines().forEach(line -> {
                            line = line.trim();
                            if (line.length() == 0) //ignore blank line
                                return;
                            grammars.add(new Grammar(GrammarType.INTENT_REGEX, wordlist,
                                    line, 1.0f));
                        });
                        br.close();
                    }

                    br.close();
                } catch (IOException | NullPointerException e) {
                    //            throw new NLUException("Error, fail to read xlsx data file");
                }
            }
        }
        dictModel = new DictModel(dicts);
        grammarModel = new GrammarModel(grammars);
    }



    public EncodingDecoding encodingDecoding=new EncodingDecoding();

    public List<HashMap> genTemplate(List<HashMap> templateMapList) throws IOException {

        InputStream synonymyTableTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyTable.txt");
//        InputStream readSegDictTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_2.txt");
        InputStream readSegDictVTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_v.txt");
        InputStream readSegDictNTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_n.txt");
        InputStream readSegDictATxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_a.txt");

        InputStream fileNameClassTxt = this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyStructure_self_classify0.txt");
        InputStream fileNameExtendTxt =this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyStructure_self_extend0.txt");

//        读取同义词表
        Pair<HashMap<String,String>,HashMap<String,String>> synonymyDict=readSynonymyTable(synonymyTableTxt);


//        读取分词字典
//        List<String> segDict=readSegDict(readSegDictTxt);
        //        读取分词字典_只有词的字典
        List<String> segDictV=readSegDictEasy(readSegDictVTxt);
        List<String> segDict_n=readSegDictEasy(readSegDictNTxt);
        List<String> segDict_a=readSegDictEasy(readSegDictATxt);



//        初始化自定义分词树
        Forest forest=new Forest();
//        将分词词典添加在分词字典中


        for(int i=0;i<segDictV.size();i++){
//            System.out.println("keyWordList.get(i):"+i+keyWordList.get(i));
            Library.insertWord( forest, new Value(segDictV.get(i), "v", String.valueOf(1000)));
        }
        for(int i=0;i<segDict_n.size();i++){
//            System.out.println("keyWordList.get(i):"+i+keyWordList.get(i));
            Library.insertWord( forest, new Value(segDict_n.get(i), "n", String.valueOf(1000)));
        }

        for(int i=0;i<segDict_a.size();i++){
//            System.out.println("keyWordList.get(i):"+i+keyWordList.get(i));
            Library.insertWord( forest, new Value(segDict_a.get(i), "n", String.valueOf(1000)));
        }
//        初始化同义词表
        SynonymyTable synonymyTable=new SynonymyTable(forest,synonymyDict);
//        初始化停用词表

        Pair<HashMap<String,List<List<Pair<String,String>>>>,List<Pair<List<Pair<String, String>>, List<Pair<String, String>>>>>
                synonymyStructuredata=readSynonymyStructure(fileNameClassTxt,forest);
        SynonymyStructure synonymyStructureClass = new SynonymyStructure(synonymyStructuredata,synonymyTable);

        synonymyStructuredata=readSynonymyStructure(fileNameExtendTxt,forest);
        SynonymyStructure synonymyStructureExtend = new SynonymyStructure(synonymyStructuredata,synonymyTable);


//        生成的模板
        List<HashMap> templateMapGenList=new ArrayList<>();

//        模板分类
        for (int i = 0; i < templateMapList.size(); i++) {
            List<HashMap> templateGenerated = synonymyStructureClass.matchSysnonymyStructure(templateMapList.get(i),encodingDecoding);
            for(int j=0;j<templateGenerated.size();j++){
                HashMap tempalteGen=new HashMap();
                tempalteGen.put("templateGen",templateGenerated.get(j).get("templateGen"));
                tempalteGen.put("templateGenStr",templateGenerated.get(j).get("templateGenStr"));
                tempalteGen.put("template",templateGenerated.get(j).get("templateGen"));
                tempalteGen.put("templateStr",templateGenerated.get(j).get("templateGenStr"));
                tempalteGen.put("synonymyStructureFirst",templateGenerated.get(j).get("synonymyStructure"));
                tempalteGen.put("synonymyStructureStrFirst",templateGenerated.get(j).get("synonymyStructureStr"));

                tempalteGen.put("synonymyStructureSecend",null);
                tempalteGen.put("synonymyStructureStrSecend",null);

                tempalteGen.put("templateRaw",templateMapList.get(i).get("templateRaw"));
                tempalteGen.put("templateRawStr",templateMapList.get(i).get("templateRawStr"));
                tempalteGen.put("queryTrainStr",templateMapList.get(i).get("queryTrainStr"));
                tempalteGen.put("templateRawIntent",templateMapList.get(i).get("templateRawIntent"));
                templateMapGenList.add(tempalteGen);
            }
        }
//        System.out.println("完成第一波扩展");

//        System.out.println("templateMapGenList.size():"+templateMapGenList.size());

        int size=templateMapGenList.size();
//        在生成的句式上做扩展
        for (int i = 0; i < size; i++) {
            List<HashMap> templateGenerated = synonymyStructureExtend.matchSysnonymyStructureExtend(templateMapGenList.get(i),encodingDecoding);
            for(int j=0;j<templateGenerated.size();j++){
                HashMap tempalteGen=new HashMap();
                tempalteGen.put("template",templateGenerated.get(j).get("templateGen"));
                tempalteGen.put("templateStr",templateGenerated.get(j).get("templateGenStr"));
                tempalteGen.put("synonymyStructureSecend",templateGenerated.get(j).get("synonymyStructure"));
                tempalteGen.put("synonymyStructureStrSecend",templateGenerated.get(j).get("synonymyStructureStr"));

                tempalteGen.put("templateGen",templateMapGenList.get(i).get("templateGen"));
                tempalteGen.put("templateGenStr",templateMapGenList.get(i).get("templateGenStr"));
                tempalteGen.put("synonymyStructureFirst",templateMapGenList.get(i).get("synonymyStructureFirst"));
                tempalteGen.put("synonymyStructureStrFirst",templateMapGenList.get(i).get("synonymyStructureFirst"));
                tempalteGen.put("templateRaw",templateMapGenList.get(i).get("templateRaw"));
                tempalteGen.put("templateRawStr",templateMapGenList.get(i).get("templateRawStr"));
                tempalteGen.put("queryTrainStr",templateMapGenList.get(i).get("queryTrainStr"));
                tempalteGen.put("templateRawIntent",templateMapGenList.get(i).get("templateRawIntent"));
                templateMapGenList.add(tempalteGen);
            }
        }
//        System.out.println("完成第二波扩展");

        //        在原有的句式上做扩展
        for (int i = 0; i < templateMapList.size(); i++) {

            List<HashMap> templateGenerated = synonymyStructureExtend.matchSysnonymyStructure(templateMapList.get(i),encodingDecoding);
            for(int j=0;j<templateGenerated.size();j++){
                HashMap tempalteGen=new HashMap();
                tempalteGen.put("templateGen",templateGenerated.get(j).get("templateGen"));
                tempalteGen.put("templateGenStr",templateGenerated.get(j).get("templateGenStr"));
                tempalteGen.put("template",templateGenerated.get(j).get("templateGen"));
                tempalteGen.put("templateStr",templateGenerated.get(j).get("templateGenStr"));
                tempalteGen.put("synonymyStructureFirst",templateGenerated.get(j).get("synonymyStructure"));
                tempalteGen.put("synonymyStructureStrFirst",templateGenerated.get(j).get("synonymyStructureStr"));

                tempalteGen.put("synonymyStructureSecend",null);
                tempalteGen.put("synonymyStructureStrSecend",null);

                tempalteGen.put("templateRaw",templateMapList.get(i).get("templateRaw"));
                tempalteGen.put("templateRawStr",templateMapList.get(i).get("templateRawStr"));
                tempalteGen.put("queryTrainStr",templateMapList.get(i).get("queryTrainStr"));
                tempalteGen.put("templateRawIntent",templateMapList.get(i).get("templateRawIntent"));
                templateMapGenList.add(tempalteGen);
            }
        }
//        System.out.println("完成第三波扩展");
//        System.out.println("扩充后的模板数是:"+templateMapGenList.size());

        return templateMapGenList;
    }


//    读取标注query，并转换成模板
    public List<HashMap> getTemplate() throws IOException {
        InputStream trainQueryLabeledTxt = this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/query_intent_labeled.txt");
        int trainQueryLabeledReadNum=5000;
        InputStream synonymyTableTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyTable.txt");
        InputStream stopWordTableTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/stopWords.txt");
//        InputStream readSegDictTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_2.txt");
        InputStream readSegDictVTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_v.txt");
        InputStream readSegDictNTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_n.txt");
        InputStream readSegDictATxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_a.txt");
        InputStream dictModelTableTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/dict.txt");

//        读取训练问题列表
        List<HashMap> trainQueryMapList=readTrainQueryLabeledTemp(trainQueryLabeledTxt,trainQueryLabeledReadNum);
//        读取同义词表
        Pair<HashMap<String,String>,HashMap<String,String>> synonymyDict=readSynonymyTable(synonymyTableTxt);
//        读取停用词表
        List<String> stopWordsList = readStopWords(stopWordTableTxt);


//        读取分词字典
//        List<String> segDict=readSegDict(readSegDictTxt);
        //        读取分词字典_只有词的字典
        List<String> segDict_v=readSegDictEasy(readSegDictVTxt);
        List<String> segDict_n=readSegDictEasy(readSegDictNTxt);
        List<String> segDict_a=readSegDictEasy(readSegDictATxt);



//        初始化自定义分词树
        Forest forest=new Forest();
//        将分词词典添加在分词字典中
//        for(int i=0;i<synonymyDict.getValue().size();i++){
//            Library.insertWord( forest, new Value(segDict.get(i), "n", String.valueOf(1000)));
//        }

        for(int i=0;i<segDict_v.size();i++){
            Library.insertWord( forest, new Value(segDict_v.get(i), "v", String.valueOf(1000)));
        }
        for(int i=0;i<segDict_n.size();i++){
            Library.insertWord( forest, new Value(segDict_n.get(i), "n", String.valueOf(1000)));
        }

        for(int i=0;i<segDict_a.size();i++){
            Library.insertWord( forest, new Value(segDict_a.get(i), "n", String.valueOf(1000)));
        }


//        读取实体字典
        DictModel dictModel = getDictModel(dictModelTableTxt);

//        初始化同义词表
        SynonymyTable synonymyTable=new SynonymyTable(forest,synonymyDict);
        this.synonymyTable=new SynonymyTable(forest,synonymyDict);
//        初始化停用词表
        StopWordsTable stopWordsTable=new StopWordsTable(stopWordsList);

//        初始化序列化反序列化对象
        encodingDecoding=new EncodingDecoding(synonymyTable);

        genDictModelAndGrammarModel();
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator ner = new NERAnnotator(this.dictModel,grammarModel,properties);

        List<HashMap> templateMapList = toTemplateListTrainLabeledSec(trainQueryMapList,ner,forest,stopWordsTable,dictModel);
        return templateMapList;
    }


    public Forest getForest() throws IOException {



        InputStream synonymyTableTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/synonymyTable.txt");
//        InputStream readSegDictTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_2.txt");
        InputStream readSegDictVTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_v.txt");
        InputStream readSegDictNTxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_n.txt");
        InputStream readSegDictATxt=this.getClass().getClassLoader().getResourceAsStream("nlu/Resources_synonymyStructure/segDict_a.txt");


//        读取同义词表
        Pair<HashMap<String,String>,HashMap<String,String>> synonymyDict=readSynonymyTable(synonymyTableTxt);



//        读取分词字典
//        List<String> segDict=readSegDict(readSegDictTxt);
        //        读取分词字典_只有词的字典
        List<String> segDict_v=readSegDictEasy(readSegDictVTxt);
        List<String> segDict_n=readSegDictEasy(readSegDictNTxt);
        List<String> segDict_a=readSegDictEasy(readSegDictATxt);



//        初始化自定义分词树
        Forest forest=new Forest();

        Set<String> keys=synonymyDict.getValue().keySet();

        Iterator<String> ketIter=keys.iterator();

        while (ketIter.hasNext()){
            Library.insertWord( forest, new Value(ketIter.next(), "n", String.valueOf(1000)));
        }

//        for(int i=0;i<segDict.size();i++){
//            Library.insertWord( forest, new Value(segDict.get(i), "n", String.valueOf(1000)));
//        }

        for(int i=0;i<segDict_v.size();i++){
            Library.insertWord( forest, new Value(segDict_v.get(i), "v", String.valueOf(1000)));
        }
        for(int i=0;i<segDict_n.size();i++){
            Library.insertWord( forest, new Value(segDict_n.get(i), "n", String.valueOf(1000)));
        }

        for(int i=0;i<segDict_a.size();i++){
            Library.insertWord( forest, new Value(segDict_a.get(i), "n", String.valueOf(1000)));
        }

        return forest;
    }

    public List<HashMap> matchedMultipleTemplateRaw(List<HashMap> templateMapList,List<HashMap> queryMapList){


//        一个模板多个query
        for(int i=0;i<templateMapList.size();i++){


            //        拿出来一个模板
            HashMap templateMap=templateMapList.get(i);
            List<Pair<String,String>> template=(List<Pair<String, String>>) templateMap.get("template");
//            System.out.println(template.get("templateRaw"));
            //        一条模板匹配多个query
            for(int j=0;j<queryMapList.size();j++){
                HashMap queryMap=queryMapList.get(j);
//                System.out.println("要比较的是："+j+"  "+template+"  "+queryMap);
                List<HashMap> matchedResultList=new ArrayList<>();
                List queryElementList=(List)queryMap.get("queryElement");
                if(queryElementList.size()>0){
                    matchedResultList=matchedSingleTemplate(template,queryMap,stopWordsTable,synonymyTable);
                }

//                System.out.println(matchedResultList);

                List<HashMap> matchedTemplateList=new ArrayList<>();
                if(matchedResultList.size()>0){
//                    matchedTemplateList=(List<HashMap>) queryMapList.get(j).get("matchedTemplateList");

                    HashMap matchedTemplate=new HashMap();
                    matchedTemplate.put("matchedTemplateStr",templateMap.get("templateStr"));
                    matchedTemplate.put("matchedTemplate",templateMap.get("template"));
                    matchedTemplate.put("synonymyStructure",templateMap.get("synonymyStructure"));
                    matchedTemplate.put("synonymyStructureStr",templateMap.get("synonymyStructureStr"));
                    matchedTemplate.put("templateRawStr",templateMap.get("templateRawStr"));
                    matchedTemplate.put("templateRawIntent",templateMap.get("templateRawIntent"));
                    matchedTemplate.put("queryTrainStr",templateMap.get("queryTrainStr"));

                    List<HashMap> possiblePathList=new ArrayList<>();
                    for(int k=0;k<matchedResultList.size();k++) {
                        HashMap possiblePath=new HashMap();
                        possiblePath.put("pathElementList",matchedResultList.get(k));
                        possiblePathList.add(possiblePath);
                    }

                    matchedTemplate.put("possiblePathList",possiblePathList);
                    matchedTemplateList.add(matchedTemplate);
                    queryMapList.get(j).put("matchedTemplateList",matchedTemplateList);
                }
            }
        }
        return queryMapList;
    }



    public List matchedSingleTemplate(List<Pair<String,String>> template,HashMap query,StopWordsTable stopWordsTable,SynonymyTable synonymyTable){
        List queryElement=(List) query.get("queryElement");
        List templateRaw=template;
        int queryLength=query.get("queryStr").toString().length();
        List<List> resultList=judge(template,0,queryElement,0,queryLength);
//        如果出的结果长度不对,就删除
        for(int i=resultList.size();i>0;i--){
//            System.out.println(i);
//            System.out.println("结果和模板的长度:"+resultList.get(i-1).size()+"   "+templateRaw.size());
            if(resultList.get(i-1).size()!=templateRaw.size()){
                resultList.remove(i-1);
            }
        }
        List queryElementNew=stopWordsTable.wipeStopElement(queryElement);
        int queryElementSize= queryElementNew.size();

//        结果数组长度是0，索引-1获取不到元素
        List lastElement;
        if(queryElementSize==0){
//            如果数组长度是0直接返回这个空的列表
            return queryElementNew;
        }else {
            lastElement=(List)queryElementNew.get(queryElementSize-1);
        }

        int lastElementEnd=(int)lastElement.get(1);

//        判断是否到了结尾
        for(int i=resultList.size()-1;i>=0;i--){
            List singleElement=(List)resultList.get(i).get(resultList.get(i).size()-1);
            if((int)singleElement.get(1)!=lastElementEnd){
                resultList.remove(i);
            }
        }
        return resultList;
    }


    public List judge(List<Pair<String,String>> template,int templateIndex,List<List> queryElement,int queryCharIndex,int queryLength){
        List<List> matchedPathList=new ArrayList<>();
//        获取在当前点下的可能的路径
        List<List> possibleList=getNextPathInt(queryElement,queryCharIndex);
        for(int i=0;i<possibleList.size();i++){
//            List template2=template;
            List element=possibleList.get(i);

//            判断停用词
            if(stopWordsTable.stopWordList.contains(element.get(2).toString())){
                List<List> resultList=judge(template,templateIndex,queryElement,(int)element.get(1),queryLength);
                for(int j=0;j<resultList.size();j++){
                    List singleResult=new ArrayList();
//                    singleResult.add(possibleList.get(i));
                    singleResult.addAll(resultList.get(j));
                    matchedPathList.add(singleResult);
                }
            }

            int endIndex=(int)possibleList.get(i).get(1);

//            结束了
            if(templateIndex==template.size()-1){

                Pair<String,String> templateElement=(Pair<String, String>) template.get(templateIndex);
//            首先要判断当前元素是否相同
                boolean singleResultBool=false;
                List singleResult=new ArrayList();
                for(int k=0;k<possibleList.size();k++){
                    if(matchedSingleElement(templateElement,possibleList.get(k))){
                        singleResultBool=true;
                        singleResult=new ArrayList();
                        singleResult.add(possibleList.get(k));
                        matchedPathList.add(singleResult);
                    }
                }
                return matchedPathList;
            }

            //            模板的当前元素
            Pair<String,String> templateElement=(Pair<String, String>) template.get(templateIndex);
//            首先要判断当前元素是否相同
            boolean singleResultBool=matchedSingleElement(templateElement,element);



            if(endIndex==queryLength){
                if(singleResultBool==true){
                    List path=new ArrayList();
                    path.add(possibleList.get(i));
                    matchedPathList.add(path);
                    return matchedPathList;
                }
//                return matchedPathList;
            }

            if(singleResultBool==true){
//                System.out.println("模板的索引是:"+templateIndex);
                List<List> resultList=judge(template,templateIndex+1,queryElement,(int)element.get(1),queryLength);
//                如果resultList中有元素的话就将其放在列表中，反向生长，直到到头。
                for(int j=0;j<resultList.size();j++){
                    List singleResult=new ArrayList();
                    singleResult.add(possibleList.get(i));
                    singleResult.addAll(resultList.get(j));
                    matchedPathList.add(singleResult);
                }
            }
        }
        return matchedPathList;
    }





    public boolean matchedSingleElement(Pair<String,String> templateElement,List queryElement){


        List<Boolean> resultList=new ArrayList<>();
        if("entity".equals(templateElement.getValue())){
            boolean resultStr=templateElement.getKey().equals((String)queryElement.get(2));
            boolean resultPro=templateElement.getValue().equals((String)queryElement.get(3));
            resultList.add(resultStr);
            resultList.add(resultPro);
        }

        boolean strJudgeResult=false;
        if("str".equals(templateElement.getValue())){
            strJudgeResult=templateElement.getKey().equals((String)queryElement.get(2));
        }

        boolean resultSet=false;
        if(this.synonymyTable.synonymyDictSet.get(templateElement.getKey())!=null&&this.synonymyTable.synonymyDictSet.get((String)queryElement.get(2))!=null){

            String templateElementSetName=this.synonymyTable.synonymyDictSet.get(templateElement.getKey());
            String queryElementSetsNamme=this.synonymyTable.synonymyDictSet.get((String)queryElement.get(2));

            resultSet=templateElementSetName.equals(queryElementSetsNamme);
        }

        if (strJudgeResult){
            return true;
        }else if (resultList.contains(false)==false&&resultList.size()!=0){
            return true;
        }else if(resultSet==true) {
            return true;
        }else {
            return false;
        }
    }



    public List<List> getNextPathInt(List<List> queryElement,int indexStart) {
//        在queryElement中找到以possible结尾索引开头的几个可能的路径
        List<List> possiblePathList = new ArrayList<>();
        for (int i = 0; i < queryElement.size(); i++) {
            if ((int) queryElement.get(i).get(0) == indexStart) {
                possiblePathList.add(queryElement.get(i));
            }
        }
        return possiblePathList;
    }



    public NERAnnotator getNer(){
        genDictModelAndGrammarModel();
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator ner = new NERAnnotator(dictModel,grammarModel,properties);
        return ner;
    }

//    public List<HashMap> getQueryElement(List<HashMap> queryMapList, NERAnnotator ner, NumAnnotator na, Forest forest){
//        QueryElement queryElement = new QueryElement();
//        List<List> queryElementList;
//        for (int i = 0; i < queryMapList.size(); i++) {
//            queryElementList=new ArrayList<>();
//            String query = (String)queryMapList.get(i).get("queryStr");
//
//
//            List<List> queryEntityAnntator = new ArrayList<>();
//
//            List<List> nerElementDic=queryElement.getQueryEntityAnntatorFromNer(query,ner);
//            List<List> naElementDic=queryElement.getQueryEntityAnntatorFromNa(query,na);
//
//
//            List<List> nerElement=getShort(nerElementDic);
//            List<List> naElement=getShort(naElementDic);
//
//            if(nerElement.size()>0){
//                queryElementList.add(nerElement);
//            }
//
//            if(naElement.size()>0){
//                queryElementList.add(naElement);
//            }
//
//            //            排序
//            if(queryElementList.size()>1){
//                queryElementList=queryElementSort(queryElementList);
//                if((int)queryElementList.get(1).get(0)<(int)queryElementList.get(0).get(1)){
//                    queryElementList.remove(1);
//                }
//            }
//
//
//            List<List> querySplitAnntator;
//            if(queryElementList.size()==0){
//                querySplitAnntator=queryElement.getQuerySplitElement(query,forest,0);
//            }else {
//                String segQuery=query.substring(0,(int)queryElementList.get(0).get(0));
//
//                querySplitAnntator=queryElement.getQuerySplitElement(segQuery,forest,0);
//
//                for(int j=0;j<queryElementList.size()-1;j++){
//                    segQuery=query.substring((int)queryElementList.get(j).get(1),(int)queryElementList.get(j+1).get(0));
//                    querySplitAnntator.addAll(queryElement.getQuerySplitElement(segQuery,forest,(int)queryElementList.get(j).get(1)));
//                }
//                segQuery=query.substring((int)queryElementList.get(queryElementList.size()-1).get(1),query.length());
//                querySplitAnntator.addAll(queryElement.getQuerySplitElement(segQuery,forest,(int)queryElementList.get(queryElementList.size()-1).get(1)));
//            }
//
//
//            queryEntityAnntator.addAll(querySplitAnntator);
//            queryEntityAnntator.addAll(queryElementList);
//            queryEntityAnntator.addAll(nerElementDic);
//            queryEntityAnntator.addAll(naElementDic);
//            queryEntityAnntator=queryElementSort(queryEntityAnntator);
//            queryMapList.get(i).put("queryElement",queryEntityAnntator);
//        }
//
//
//        return queryMapList;
//    }


    public List<List> getShort(List<List> queryElementList){
        int lengthOfEntity=50;
        List<List> queryElement=new ArrayList<>();
        for(int i=0;i<queryElementList.size();i++){
            if(((int)queryElementList.get(i).get(1)-(int)queryElementList.get(i).get(0))<lengthOfEntity){
                queryElement=queryElementList.get(i);
                lengthOfEntity=(int)queryElementList.get(i).get(1)-(int)queryElementList.get(i).get(0);
            }
        }
        return queryElement;
    }

    public List<HashMap> readTrainQueryLabeledTemp(InputStream is,int numQuery) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        List<HashMap> trainQueryMapList = new ArrayList<>();

        for (int i = 0; i < numQuery; i++) {
            String singleLine = reader.readLine();
            String[] singleQuery="".split("");//暂时
            if(singleLine!=null){
                singleQuery = singleLine.split("\t");
            }
            HashMap trainQueryMap=new HashMap();
//            意图可能为空，如果为空直接赋值为空字符串
            if (singleQuery.length >= 3) {
                trainQueryMap.put("queryTrainStr",singleQuery[0]);
                trainQueryMap.put("labeledData",singleQuery[1]);
                trainQueryMap.put("templateRawIntent",singleQuery[2]);
                trainQueryMapList.add(trainQueryMap);
            } else if (singleQuery.length>=2){
                trainQueryMap.put("queryTrainStr",singleQuery[0]);
                trainQueryMap.put("labeledData",singleQuery[1]);
                trainQueryMap.put("templateRawIntent","");
                trainQueryMapList.add(trainQueryMap);
            }else if(singleQuery.length>=1){
                trainQueryMap.put("queryTrainStr",singleQuery[0]);
                trainQueryMap.put("labeledData","");
                trainQueryMap.put("templateRawIntent","");
                trainQueryMapList.add(trainQueryMap);
            }else{
                trainQueryMap.put("queryTrainStr","");
                trainQueryMap.put("labeledData","");
                trainQueryMap.put("templateRawIntent","");
                trainQueryMapList.add(trainQueryMap);
            }
        }
        reader.close();
        return trainQueryMapList;
    }

    //    读取分词词典
    public List<String> readSegDictEasy(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        List<String> templateList = new ArrayList<>();
        String singleLine = reader.readLine();
        while (singleLine != null) {
            templateList.add(singleLine);
            singleLine = reader.readLine();
        }

        List<String> queryList=new ArrayList<>();

        for (int i = 0; i < templateList.size(); i++) {
            queryList.add(templateList.get(i));
        }
        reader.close();
        return queryList;
    }

//    读取同义词表
    public Pair<HashMap<String,String>,HashMap<String,String>> readSynonymyTable(InputStream is) throws IOException {
        
    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
    BufferedReader reader = new BufferedReader(isr);
    List<String> templateList = new ArrayList<>();
    String singleLine = reader.readLine();
    while (singleLine != null) {
        templateList.add(singleLine);
        singleLine = reader.readLine();
    }

    HashMap<String,String> synonymyDictSet=new HashMap<>();
    HashMap<String,String> synonymyDictPro=new HashMap<>();

    String[] templateSplit;
    for (int i = 0; i < templateList.size(); i++) {
        String str = templateList.get(i);

        templateSplit = str.split("\t");

        String[] setsElement = templateSplit[2].split(",");
        for (int j = 0; j < setsElement.length; j++) {
            synonymyDictSet.put(setsElement[j], templateSplit[1]);
            synonymyDictPro.put(setsElement[j], templateSplit[0]);
        }
        synonymyDictSet.put(templateSplit[1], templateSplit[1]);
        synonymyDictPro.put(templateSplit[1], templateSplit[0]);
    }
    reader.close();
    return new Pair<>(synonymyDictSet,synonymyDictPro);
}

    public List<String> readStopWords(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        List<String> stopWordList = new ArrayList<>();
        String singleLine = reader.readLine();
        while (singleLine != null) {
            stopWordList.add(singleLine);
            singleLine = reader.readLine();
        }
        reader.close();
        return stopWordList;
    }

    //    这个函数通过指定的dict建立一个dictModel
    public DictModel getDictModel(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);

        List<Dict> dictList = new ArrayList<Dict>();

        String singleLine = reader.readLine();
        String[] singleDictSplit;
        while (singleLine != null) {

            singleDictSplit = singleLine.split("\t");
            Dict dict;
            if (singleDictSplit.length == 3) {
                dict = new Dict(singleDictSplit[0], singleDictSplit[1], singleDictSplit[2]);
            } else {
                dict = new Dict(singleDictSplit[0], singleDictSplit[1]);
            }
            dictList.add(dict);
//            处理完成后再读取一行数据
            singleLine = reader.readLine();
        }
//        建立dictModel对象
        DictModel dictModel;
        dictModel = new DictModel(dictList);
        reader.close();

        return dictModel;
    }


    //    用标注好的语料生成模板
    public List<HashMap> toTemplateListTrainLabeledSec(List<HashMap> trainQueryMapList, NERAnnotator ner, Forest forest, StopWordsTable stopWordsTable, DictModel dictModel) throws IOException {

        TemplateList templateList = new TemplateList();
        for (int i = 0; i < trainQueryMapList.size(); i++) {
//            转变为模板，将意图添加进模板列表templateList中
            List<Pair<String,String>> templateRaw=templateList.toTemplateLabeled((String)trainQueryMapList.get(i).get("queryTrainStr"),(String)trainQueryMapList.get(i).get("labeledData"),forest);
//            List<Pair<String,String>> templateRawFromAnntator=templateList.toTemplateNer((String)trainQueryMapList.get(i).get("queryTrainStr"),ner,forest);
//            去停用词之后的模板
            templateRaw=stopWordsTable.wipeStopWord(templateRaw);

            trainQueryMapList.get(i).put("template",templateRaw);
            trainQueryMapList.get(i).put("templateStr",encodingDecoding.encodingTemplate(templateRaw));

            trainQueryMapList.get(i).put("templateGen",null);
            trainQueryMapList.get(i).put("templateGenStr",null);
            trainQueryMapList.get(i).put("synonymyStructureFirst",null);
            trainQueryMapList.get(i).put("synonymyStructureStrFirst",null);
            trainQueryMapList.get(i).put("synonymyStructureSecend",null);
            trainQueryMapList.get(i).put("synonymyStructureStrSecend",null);

            trainQueryMapList.get(i).put("templateRaw",templateRaw);
            trainQueryMapList.get(i).put("templateRawStr",encodingDecoding.encodingTemplate(templateRaw));
            trainQueryMapList.get(i).put("queryTrainStr",trainQueryMapList.get(i).get("queryTrainStr"));
            trainQueryMapList.get(i).put("templateRawIntent",trainQueryMapList.get(i).get("templateRawIntent"));
        }
        return trainQueryMapList;
    }


    //    按照第一个参数排序
    public List<List> queryElementSort(List<List> queryElement) {
        Collections.sort(queryElement, new Comparator<List>() {
            public int compare(List o1, List o2) {
                return (int) o1.get(0) - (int) o2.get(0);
            }
        });//使用Collections的sort方法，并且重写compare方法
        return queryElement;
    }



    public Pair<HashMap<String,List<List<Pair<String,String>>>>,List<Pair<List<Pair<String, String>>, List<Pair<String, String>>>>>
    readSynonymyStructure(InputStream is,Forest forest) throws IOException {

        HashMap<String,List<List<Pair<String,String>>>> combineRuleMap=new HashMap<>();
        List<Pair<List<Pair<String, String>>, List<Pair<String,String>>>> transformRuleList=new ArrayList<>();
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String singleLine = reader.readLine();
        if(singleLine!=null){
            singleLine = singleLine.substring(1, singleLine.length());
        }
        while (singleLine != (null)) {

            if(singleLine.substring(0,1).equals(":")||singleLine.substring(0,1).equals("：")){
//                解析复合规则
                Pair<String,List<List<Pair<String,String>>>> combineRule=decodeCombineRule(singleLine,forest);
                combineRuleMap.put(combineRule.getKey(),combineRule.getValue());
            }else {
//                解析转换结构
                Pair<List<Pair<String, String>>, List<Pair<String, String>>> trainformRule=decodeTransformRule(singleLine,forest);
                transformRuleList.add(trainformRule);
            }
            singleLine = reader.readLine();
        }
        Pair<HashMap<String,List<List<Pair<String,String>>>>,List<Pair<List<Pair<String, String>>, List<Pair<String, String>>>>>
                synonymyStructure=new Pair(combineRuleMap,transformRuleList);
        reader.close();
        return synonymyStructure;
    }





    public Pair<String,List<List<Pair<String,String>>>> decodeCombineRule(String combineRuleStr,Forest forest) {

        String combineRuleName = combineRuleStr.substring(0, combineRuleStr.indexOf("="));
        String combineRuleValueStr = combineRuleStr.substring(combineRuleStr.indexOf("=") + 1, combineRuleStr.length());
        String[] combineRuleValuestrList=combineRuleValueStr.split("\\|");
        List<List<Pair<String,String>>> combineRuleValue=new ArrayList<>();


        for (int i=0;i<combineRuleValuestrList.length;i++){
            List<Pair<String,String>> combineRuleValueElement=decodeCombineRuleElement(combineRuleValuestrList[i].substring(1,combineRuleValuestrList[i].length()-1),forest);
            combineRuleValue.add(combineRuleValueElement);
        }
        Pair<String,List<List<Pair<String,String>>>> combineRule = new Pair<String, List<List<Pair<String, String>>>>(combineRuleName, combineRuleValue);
        return combineRule;
    }


    public List<Pair<String,String>> decodeCombineRuleElement(String combineRuleValueElementStr,Forest forest){

        List<Pair<String,String>> combineRuleValueElement=new ArrayList<>();

        for (int i = 0; i < combineRuleValueElementStr.length(); ) {

            int startIndex;

            switch (combineRuleValueElementStr.charAt(i)) {
                case '{':
                    startIndex = i;
                    while (combineRuleValueElementStr.charAt(startIndex) != '}') {
                        startIndex++;
                    }
                    startIndex += 2;
                    combineRuleValueElement.add(new Pair<>(combineRuleValueElementStr.substring(i, startIndex),"entity"));
                    i = startIndex;
                    break;
                case '[':
                    startIndex = i;
                    while (combineRuleValueElementStr.charAt(startIndex) != ']') {
                        startIndex++;
                    }
                    startIndex++;
                    combineRuleValueElement.add(new Pair<String,String>(combineRuleValueElementStr.substring(i, startIndex),"sets"));
                    i = startIndex;
                    break;
                default:
                    startIndex = i;
//                    if(startIndex==behStructureStr.length()){break;}
                    while (combineRuleValueElementStr.charAt(startIndex) != '(' & combineRuleValueElementStr.charAt(startIndex) !=
                            '{' & combineRuleValueElementStr.charAt(startIndex) != '"' & combineRuleValueElementStr.charAt(startIndex) !=
                            '/' & combineRuleValueElementStr.charAt(startIndex) != '[' | (startIndex == combineRuleValueElementStr.length() - 1)) {
                        startIndex++;
                        if (startIndex == combineRuleValueElementStr.length()) {
                            break;
                        }
                    }

                    String behElement = combineRuleValueElementStr.substring(i, startIndex);
//                    将后项中字符串部分分词后添加在后项列表中
//                    List<Term> splitWordList = segAnsj(behElement);
                    Result result = ToAnalysis.parse(behElement,forest); //分词结果的一个封装，主要是一个List<Term>的terms
                    List<Term> splitWordList = result.getTerms(); //拿到terms
                    List<Pair<String,String>> wordList = new ArrayList<>();
                    for (int j = 0; j < splitWordList.size(); j++) {
                        String word = splitWordList.get(j).toString();
                        if (word.length() > 1) {
                            wordList.add(new Pair<>(word.substring(0, word.indexOf("/")),"str"));
                        }
                    }
                    combineRuleValueElement.addAll(wordList);
                    i = startIndex;
            }
        }
        return combineRuleValueElement;
    }




    public Pair<List<Pair<String, String>>, List<Pair<String, String>>> decodeTransformRule(String transformRule,Forest forest) {
        String preStructureStr = transformRule.substring(0, transformRule.indexOf("-"));
        String behStructureStr = transformRule.substring(transformRule.indexOf(">") + 1, transformRule.length());
//        解析前项
        List preStructureList = new ArrayList();

        for (int i = 0; i < preStructureStr.length(); ) {
//            System.out.println(i);
            int startIndex;
            Pair<String, String> preElement;

            switch (preStructureStr.charAt(i)) {
                case '(':
                    startIndex = i;
                    while (preStructureStr.charAt(startIndex) != ')') {
                        startIndex++;
                    }
                    startIndex++;

                    String preElementStr = preStructureStr.substring(i, startIndex);
//                    System.out.println("preElementStr"+preElementStr);
                    preElement = new Pair<String, String>
                            (preElementStr.substring(preElementStr.indexOf("：") + 1, preElementStr.length() - 1),preElementStr.substring(1, preElementStr.indexOf("：")));

                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '{':
                    startIndex = i;
                    while (preStructureStr.charAt(startIndex) != '}') {
                        startIndex++;
                    }
                    startIndex += 2;
                    preElement = new Pair<>( preStructureStr.substring(i, startIndex),"entity");
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '"':
                    startIndex = i + 1;
                    while (preStructureStr.charAt(startIndex) != '"') {
                        startIndex++;
                    }
                    startIndex++;
                    preElement = new Pair<>( preStructureStr.substring(i, startIndex),null);
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '/':
                    startIndex = i + 1;
                    while (preStructureStr.charAt(startIndex) != '/') {
                        startIndex++;
                    }
                    startIndex++;
                    preElement = new Pair<>(preStructureStr.substring(i, startIndex),"regex");
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '[':
                    startIndex = i;
                    while (preStructureStr.charAt(startIndex) != ']') {
                        startIndex++;
                    }
                    startIndex++;
                    preElement = new Pair<>( preStructureStr.substring(i, startIndex),"sets");
                    preStructureList.add(preElement);

                    i = startIndex;
                    break;
                default:
                    startIndex = i;
                    while (preStructureStr.charAt(startIndex) != '(' && preStructureStr.charAt(startIndex) !=
                            '{' && preStructureStr.charAt(startIndex) != '"' && preStructureStr.charAt(startIndex) !=
                            '/' && preStructureStr.charAt(startIndex) != '[') {
                        startIndex++;
                        if (startIndex == preStructureStr.length()) {
                            break;
                        }
                    }

                    String thisStr = preStructureStr.substring(i, startIndex);
//                        System.out.println("这个位置的字符串是："+thisStr);
                    Result result = ToAnalysis.parse(thisStr,forest); //分词结果的一个封装，主要是一个List<Term>的terms
                    List<Term> subStrTerm = result.getTerms(); //拿到terms
//                    List<Term> subStrTerm = segAnsj(thisStr);
                    for (int j = 0; j < subStrTerm.size(); j++) {
//                            System.out.println(subStrTerm.get(j));
                        if (subStrTerm.get(j).toString().contains("/")) {
                            preElement = new Pair<>(subStrTerm.get(j).toString().substring(0, subStrTerm.get(j).toString().indexOf("/")),"Str");
                            preStructureList.add(preElement);
                        } else {
                            preElement = new Pair<>( subStrTerm.get(j).toString(),"Str");
                            preStructureList.add(preElement);
                        }
                    }

                    i = startIndex;
            }
        }



        List<Pair<String,String>> behStructureList = new ArrayList();

        for (int i = 0; i < behStructureStr.length(); ) {
            int startIndex;
            switch (behStructureStr.charAt(i)) {
                case '(':
                    startIndex = i;
                    while (behStructureStr.charAt(startIndex) != ')') {
                        startIndex++;
                    }
                    startIndex++;

                    String preElementStr = behStructureStr.substring(i, startIndex);
                    behStructureList.add(new Pair<String,String>(preElementStr,"variable"));
                    i = startIndex;

                    break;
                case '{':
                    startIndex = i;
                    while (behStructureStr.charAt(startIndex) != '}') {
                        startIndex++;
                    }
                    startIndex += 2;
                    behStructureList.add(new Pair<String,String>(behStructureStr.substring(i, startIndex),"entity"));
                    i = startIndex;
                    break;
                case '"':
                    startIndex = i;
                    while (behStructureStr.charAt(startIndex) != '"') {
                        startIndex++;
                    }
                    startIndex++;
                    behStructureList.add(new Pair<String,String>(behStructureStr.substring(i, startIndex),"yaohao"));
                    i = startIndex;
                    break;
                case '/':
                    startIndex = i;
                    while (behStructureStr.charAt(startIndex) != '/') {
                        startIndex++;
                    }
                    startIndex++;
                    behStructureList.add(new Pair<String,String>(behStructureStr.substring(i, startIndex),"regex"));
                    i = startIndex;
                    break;
                case '[':
                    startIndex = i;
                    while (behStructureStr.charAt(startIndex) != ']') {
                        startIndex++;
                    }
                    startIndex++;
                    behStructureList.add(new Pair<String,String>(behStructureStr.substring(i, startIndex),"sets"));
                    i = startIndex;
                    break;
                default:
                    startIndex = i;

                    while (behStructureStr.charAt(startIndex) != '(' & behStructureStr.charAt(startIndex) !=
                            '{' & behStructureStr.charAt(startIndex) != '"' & behStructureStr.charAt(startIndex) !=
                            '/' & behStructureStr.charAt(startIndex) != '[' | (startIndex == behStructureStr.length() - 1)) {
                        startIndex++;
                        if (startIndex == behStructureStr.length()) {
                            break;
                        }
                    }

                    String behElement = behStructureStr.substring(i, startIndex);
//                    将后项中字符串部分分词后添加在后项列表中
                    Result result = ToAnalysis.parse(behElement,forest); //分词结果的一个封装，主要是一个List<Term>的terms
                    List<Term> splitWordList = result.getTerms(); //拿到terms

                    List<Pair<String,String>> wordList = new ArrayList<>();
                    for (int j = 0; j < splitWordList.size(); j++) {
                        String word = splitWordList.get(j).toString();

                        if (word.contains("/")) {
                            wordList.add(new Pair<String,String> (word.substring(0, word.indexOf("/")),"str"));
                        }else{
                            wordList.add(new Pair<>(word,"str"));
                        }
                    }
                    behStructureList.addAll(wordList);
                    i = startIndex;
            }
        }
        Pair<List<Pair<String, String>>, List<Pair<String, String>>> synonymyStructure =
                new Pair<List<Pair<String, String>>, List<Pair<String, String>>>(preStructureList, behStructureList);
        return synonymyStructure;
    }
}
