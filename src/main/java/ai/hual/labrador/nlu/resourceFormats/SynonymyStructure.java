package ai.hual.labrador.nlu.resourceFormats;

//import org.nlpcn.commons.lang.util.tuples.Pair;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SynonymyStructure {

    public Pair<HashMap<String,List<List<Pair<String,String>>>>,List<Pair<List<Pair<String, String>>, List<Pair<String, String>>>>> combineRule_SynonymyStructure=new Pair<>(null,null);

    public List< Pair<List<Pair<String, String>>, List<Pair<String,String>>>> synonymyStructureList = new ArrayList<>();


//    (a：：X101)(c：[怎么])(d：[v])->(a)(d)方式
//    [：X101=a,[怎么]=c,[v]=d]=[a,d,方式]

    HashMap<String,List<List<Pair<String,String>>>> combineRule;
//    例子：：X001=（[n]）|（[n][n]）
//    ：X001=[["n"="sets"],["n"="sets","n"="sets"]]
//
//    ：X002=（{{entity}}）|（{{entity}}[n]）|（{{entity}}里）
//    ：X002=[["entity"="entity"],["entity"="entity","n"="sets"],["entity"="entity","里"="str"]]

    SynonymyTable synonymyTable;




    public SynonymyStructure(Pair<HashMap<String,List<List<Pair<String,String>>>>,List<Pair<List<Pair<String, String>>, List<Pair<String, String>>>>>
                                     combineRule_SynonymyStructure, SynonymyTable synonymyTable) throws IOException {
        this.synonymyTable=synonymyTable;
        this.combineRule_SynonymyStructure=combineRule_SynonymyStructure;
        combineRule=this.combineRule_SynonymyStructure.getKey();
        synonymyStructureList=this.combineRule_SynonymyStructure.getValue();
    }


    public int combineRuleMatch(String combineRuleName,List<Pair<String,String>> template,int indexStart) {
//        System.out.println(indexStart);

//        布尔运算匹配
//        按照最长匹配原则匹配

//        如果成功，并且更长，就更新复合规则元素的长度。
        int indexEnd=0;
        List<List<Pair<String,String>>> combineRuleValue=this.combineRule.get(combineRuleName);
//        System.out.println("这个复合规则是："+combineRuleValue);

        for(int i=0;i<combineRuleValue.size();i++){
//            indexEnd=0;
            int lengthCombineRuleElement=combineRuleValue.get(i).size();
//            System.out.println("lengthCombineRuleElement:"+lengthCombineRuleElement);
            List<Boolean> resultList=new ArrayList<>();
            for (int j=0;j<lengthCombineRuleElement;j++){
                boolean result=false;

//                元素的元素
                String combineRuleElementOfElementStr=combineRuleValue.get(i).get(j).getKey();
//                System.out.println("combineRuleElementOfElementStr:"+combineRuleElementOfElementStr);
//                String combineRuleElementOfElementStr=combineRuleElementOfElementStrAll.substring(1,combineRuleElementOfElementStrAll.length()-1);
                String combineRuleElementOfElementClass=combineRuleValue.get(i).get(j).getValue();

//                取模板元素

                String templateElementStr="";
                String tempalteElementClass="";
                if(template.size()>indexStart+j){
//                    System.out.println(indexStart+j);
//                    System.out.println("这个位置的额模板是："+template);
                    templateElementStr=template.get(indexStart+j).getKey();
                    tempalteElementClass=template.get(indexStart+j).getValue();
                }



                if(combineRuleElementOfElementClass.equals("entity")){
//                    实体，直接比较复合规则类别和模板的类别
                    result=combineRuleElementOfElementClass.equals(tempalteElementClass);
                }else if(combineRuleElementOfElementClass.equals("sets")){

                    String setsName = synonymyTable.synonymyDictSet.get(templateElementStr);
                    String proNameSynonymyTable=synonymyTable.synonymyDictPro.get(templateElementStr);

//                    String setsName = "";
//                    String proNameSynonymyTable="";

                    List<Boolean> resultListList=new ArrayList<>();

                    String combineRuleElementOfElementStrSplit=combineRuleElementOfElementStr.substring(1,combineRuleElementOfElementStr.length()-1);

                    resultListList.add(combineRuleElementOfElementStrSplit.equals(proNameSynonymyTable));
                    resultListList.add(combineRuleElementOfElementStrSplit.equals(setsName));
                    resultListList.add(combineRuleElementOfElementStrSplit.equals(tempalteElementClass));
//                    System.out.println("setsName   proNameSynonymyTable:"+setsName+"  "+proNameSynonymyTable);
//                    System.out.println(combineRuleElementOfElementStrSplit+"    "+tempalteElementClass);

//        resultList.add(preElement.substring(1, preElement.length() - 1).equals(templateElement.getKey()));

//                    两个都是true才匹配成功
                    if(resultListList.contains(true)==true){
                        result= true;
                    }else {
                        result=false;
                    }
                }else {
//                    字符串
//                    System.out.println("字符串部分："+combineRuleElementOfElementStr+"   "+templateElementStr);
                    if(combineRuleElementOfElementStr.equals(templateElementStr)){
                        result=true;
                    }
                }
//                System.out.println(result);
                resultList.add(result);
            }
//            System.out.println("resultList:"+resultList);


//            不包含false，全部成功，才算成功
            if(resultList.contains(false)==false){
//                更新indexEnd
                if(indexEnd<lengthCombineRuleElement){
//                    如果更长，那么更新indexEnd
//                    indexEnd=indexStart+lengthCombineRuleElement;
                    indexEnd=lengthCombineRuleElement;
                }
            }else {

            }
//            System.out.println("复合元素匹配的结果是："+indexEnd);
        }
//        如果匹配成功，就返回结束的索引，如果匹配失败，就返回0
//        System.out.println(indexEnd);
        return indexEnd;
    }



    //编写六个函数
    public int strRuleMatch(String preElement, Pair<String, String> templateElement) {
        if (preElement.equals(templateElement.getKey())) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean quotationRuleMacth() {
//        直接匹配
        return false;
    }

//    比较同义词集合中的元素
    public int setsRuleMatch(String preElement, Pair<String, String> templateElement) {
        String setsName = synonymyTable.synonymyDictSet.get(templateElement.getKey());
        String proNameSynonymyTable=synonymyTable.synonymyDictPro.get(templateElement.getKey());
        String proNameSplit=templateElement.getValue();

        List<Boolean> resultList=new ArrayList<>();
        resultList.add(preElement.substring(1, preElement.length() - 1).equals(proNameSynonymyTable));
        resultList.add(preElement.substring(1, preElement.length() - 1).equals(setsName));
//        为了保证只要是词性的大类相同就能匹配上，取二级类别的第一个类别作为词性
        resultList.add(preElement.substring(1, preElement.length() - 1).equals(proNameSplit.substring(0,1)));

        if(resultList.contains(true)==true){
            return 1;
        }
        return 0;
    }



    public int entityRuleMatch(String preElement, Pair<String, String> templateElement) {
//        是否式实体
//        好像得查图谱
//        由于句式列表中是带上一个实体名字的，所以，可以直接比较实体的名字是否相同
//        目前只要是是实体都返回true，因为没有查询实体的图谱
//        System.out.println("现在的前项中的实体的字符串是："+preElement);
//        System.out.println("现在的模板中的实体的名字是："+templateElement);

        String entityClassName = preElement.substring(2, preElement.length() - 2);

//        如果是Thing直接返回true
        if (entityClassName.equals("Thing")) {
            return 1;
        }
        if (entityClassName.equals(templateElement.getKey())) {
            return 1;
        }
        if (entityClassName.equals(templateElement.getValue())) {
            return 1;
        }
        return 0;
    }

    public int regexRuleMatch(String preElement, Pair<String, String> templateElement) {
//        System.out.println(preElement);
//        System.out.println(templateElement);
//        匹配正则表达式
        return 0;
    }



    //    用同一条模板，所有的同义结构批量扩充模板
    public List<HashMap> matchSysnonymyStructure(HashMap template, EncodingDecoding EncodingDecoding) {
//        一条模板：《模板，同义结构编号》
        List<HashMap> templateList=new ArrayList();
        for(int i=0;i<synonymyStructureList.size();i++){
            HashMap generateElement=new HashMap();
            List<Pair<String,String>> templateSingleSyS=matchSysnonymyStructureSingle(i,(List<Pair<String,String>>)template.get("templateRaw"));
            if(templateSingleSyS.size()>0){
                generateElement.put("templateGen",templateSingleSyS);
                generateElement.put("templateGenStr",EncodingDecoding.encodingTemplate(templateSingleSyS));
                generateElement.put("synonymyStructure",synonymyStructureList.get(i));
                generateElement.put("synonymyStructureStr",EncodingDecoding.toStringSynonymyStructure(synonymyStructureList.get(i)));
                boolean isChange=template.get("templateRawStr").equals(EncodingDecoding.encodingTemplate(templateSingleSyS));
                generateElement.put("isChange",!isChange);
                templateList.add(generateElement);
            }
        }
        return templateList;
    }

    //    用同一条模板，所有的同义结构批量扩充模板
    public List<HashMap> matchSysnonymyStructureExtend(HashMap template, EncodingDecoding EncodingDecoding) {
//        一条模板：《模板，同义结构编号》
        List<HashMap> templateList=new ArrayList();
        for(int i=0;i<synonymyStructureList.size();i++){
            HashMap generateElement=new HashMap();
            if(template.get("templateGen")!=null){
                List<Pair<String,String>> templateSingleSyS=matchSysnonymyStructureSingle(i,(List<Pair<String,String>>)template.get("templateGen"));
                if(templateSingleSyS.size()>0){
                    generateElement.put("templateGen",templateSingleSyS);
                    generateElement.put("templateGenStr",EncodingDecoding.encodingTemplate(templateSingleSyS));
                    generateElement.put("synonymyStructure",synonymyStructureList.get(i));
                    generateElement.put("synonymyStructureStr",EncodingDecoding.toStringSynonymyStructure(synonymyStructureList.get(i)));
//                    boolean isChange=template.get("templateRawStr").equals(EncodingDecoding.encodingTemplate(templateSingleSyS));
//                    generateElement.put("isChange",!isChange);
                    templateList.add(generateElement);
                }
            }
        }
        return templateList;
    }


//    用一条模板一条同义结构匹配一条模板

    public List<Pair<String,String>> matchSysnonymyStructureSingle(int indexSynonymyStructure,List<Pair<String,String>> template){
//        System.out.println("正在匹配");

        List<Pair<String,String>> behElementList=synonymyStructureList.get(indexSynonymyStructure).getValue();
        List<Pair<String,String>> preElementList=synonymyStructureList.get(indexSynonymyStructure).getKey();
//        匹配
        int sizePreElement=synonymyStructureList.get(indexSynonymyStructure).getKey().size();

        int indexPreElement=0;
        List<Integer> resultList=new ArrayList<>();
        HashMap<String,List<Pair<String,String>>> resultMap=new HashMap<>();
        List<Pair<String,String>> resultTemplateList=new ArrayList<>();

//        匹配前项元素
//        模板元素的标号，如果标号等于模板的长度就停止匹配，如果超出模板长度，直接放回false，当前项元素匹配完成时，模板编号还是小于句式长度，那么返回false，正好等于才返回true

        for(int i=0;i<sizePreElement;i++){
//            逐个判断前项元素是否复合规则
            int result=0;
//            System.out.println("template    indexPreElement"+template+"   "+indexPreElement);
            if(template.size()>0){

                result=mtachPreElement(synonymyStructureList.get(indexSynonymyStructure).getKey().get(i),template,indexPreElement);
//                System.out.println("匹配的结果："+result);
            }
            resultList.add(result);


//            根据前项和句式生成结果map
            List<Pair<String,String>> segList=splitListTool(template,indexPreElement,indexPreElement+result);

//            前项的结构是：[：实体=a, [n]=sets, [v]=sets, 最=Str, 大=Str, 是=Str, 多少=Str]
            String preVaribale;
            if(preElementList.get(i).getValue().equals("sets")){
                preVaribale=preElementList.get(i).getKey();
            }else {
                preVaribale=preElementList.get(i).getValue();
            }


            resultMap.put(preVaribale,segList);
            indexPreElement=indexPreElement+result;
//            System.out.println("indexPreElement:"+indexPreElement);
//            System.out.println("template.size():"+template.size());

            if(indexPreElement==template.size()){
//                System.out.println("resultList"+resultList);
//                System.out.println("resultMap:"+resultMap);
                if(resultList.contains(0)==false&&i==sizePreElement-1){
//                    用后项生成结果
                    for(int j=0;j<behElementList.size();j++){
                        String behElementVariableName=behElementList.get(j).getKey();
//                        System.out.println("behElementVariableName:"+behElementVariableName);
                        if(judgeClass(behElementVariableName).equals("variable")){
                            resultTemplateList.addAll(resultMap.get(behElementVariableName.substring(1,behElementVariableName.length()-1)));
                        }else if(judgeClass(behElementVariableName).equals("sets")){
//                            System.out.println("behElementList:"+behElementList);

//                            如果包含同义词集合名字，那么就直接加上，如果不包含，就说明是词性，取前面的值，放在生成的模板中
                            String setsWord=behElementList.get(j).getKey();

                            if(synonymyTable.synonymyDictSet.containsValue(setsWord.substring(1,setsWord.length()-1))){
                                Pair<String,String> pair=new Pair<>(behElementList.get(j).getKey(),"sets");
                                resultTemplateList.add(pair);
                            }else if(resultMap.containsKey(behElementList.get(j).getKey())){
                                resultTemplateList.addAll(resultMap.get(behElementList.get(j).getKey()));
                            }

//                            if(resultMap.containsKey(behElementList.get(j).getKey())){
//                                resultTemplateList.addAll(resultMap.get(behElementList.get(j).getKey()));
//                            }else {
//                                Pair<String,String> pair=new Pair<>(behElementList.get(j).getKey(),"sets");
//                                resultTemplateList.add(pair);
//                            }
                        }else {
                            List<Pair<String,String>> strList=new ArrayList<>();
                            strList.add(behElementList.get(j));
                            resultTemplateList.addAll(strList);
                        }
                    }

                    List<Pair<String,String>> newResultTemplateList=new ArrayList<>();
                    for(int k=0;k<resultTemplateList.size();k++){
                        if("sets".equals(resultTemplateList.get(k).getValue())){
                            String word=resultTemplateList.get(k).getKey();
                            if(word.length()>1){
                                newResultTemplateList.add(new Pair<String,String>(word.substring(1,word.length()-1),"sets"));
                            }else {
                                newResultTemplateList.add(new Pair<String,String>(word,"sets"));
                            }
                        }else {
                            newResultTemplateList.add(resultTemplateList.get(k));
                        }
                    }

                    return newResultTemplateList;
                }else{
                    return new ArrayList<>();
                }
            }
        }
        return new ArrayList<>();
    }


    public List<Pair<String,String>> splitListTool(List<Pair<String,String>> oldList,int Start,int End){
        List<Pair<String,String>> newList=new ArrayList<>();
        for(int i=Start;i<End;i++){
            newList.add(oldList.get(i));
        }
        return newList;
    }


    public int mtachPreElement(Pair<String,String> preElement,List<Pair<String,String>> template,int indexPreElement){
//        System.out.println(template);
//        System.out.println("前项元素和模板分别是："+preElement+"    "+template.get(indexPreElement)+"    "+indexPreElement);

        String preElementStr = preElement.getKey();


        //                    判断类别
        String classResult = judgeClass(preElementStr);


        int isMatched=0;

        if (classResult == "entity") {
            isMatched = entityRuleMatch(preElementStr, template.get(indexPreElement));
            return isMatched;
        } else if (classResult == "sets") {
            isMatched = setsRuleMatch(preElementStr, template.get(indexPreElement));
            return isMatched;
        } else if (classResult == "regex") {
            isMatched = regexRuleMatch(preElementStr, template.get(indexPreElement));
            return isMatched;
        } else if (classResult == "combine") {
//            System.out.println("是复合规则");
//            需要传入复合规则的名字
            isMatched = combineRuleMatch(preElementStr, template,indexPreElement);
            return isMatched;
        } else {
//            字符串的匹配
            isMatched = strRuleMatch(preElementStr, template.get(indexPreElement));
            return isMatched;
        }
//        return 1;
    }



    private String judgeClass(String preElement) {

        String matchResult;
        switch (preElement.charAt(0)) {
            case '(':
                matchResult = "variable";
                break;
            case '{':
                matchResult = "entity";
                break;
            case '[':
                matchResult = "sets";
                break;
            case '/':
                matchResult = "regex";
                break;
            case ':':
                matchResult = "combine";
                break;
            case '：':
                matchResult = "combine";
                break;
            default:
                matchResult = "Str";
                break;
        }
        return matchResult;
    }
}
