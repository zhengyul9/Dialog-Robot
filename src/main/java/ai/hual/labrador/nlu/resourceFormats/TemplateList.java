package ai.hual.labrador.nlu.resourceFormats;

import javafx.util.Pair;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nlpcn.commons.lang.tire.domain.Forest;

import java.util.*;

public class TemplateList {


    public List<Pair<String,String>> toTemplateFromStr(String templateStr){
        String[] templateArr=templateStr.split("/");
        List<Pair<String,String>> templateStrList=new ArrayList();
        for(int i=0;i<templateArr.length;i++){
            String templateElementStr=templateArr[i];
            Pair<String,String> templatePair;
            if("{".equals(templateElementStr.substring(0,1))){
                templatePair=new Pair<>(templateElementStr.substring(2,templateElementStr.length()-2),"entity");
            }else if("[".equals(templateElementStr.substring(0,1))){
                templatePair=new Pair<>(templateElementStr.substring(1,templateElementStr.length()-1),"sets");
            }else {
                templatePair=new Pair<>(templateElementStr,"str");
            }
            templateStrList.add(templatePair);
        }
        return templateStrList;
    }

    public List<Pair<String,String>> toTemplateFromStr(String query,Forest forest){


//        首先去除"﻿\uFEFF﻿"

        if(query.contains("\uFEFF")){
            query=query.substring(0,query.indexOf('\uFEFF'))+query.substring(query.indexOf('\uFEFF')+1,query.length());
        }


        List preStructureList = new ArrayList();

        for (int i = 0; i < query.length(); ) {
//            System.out.println(i);
            int startIndex;
            Pair<String, String> preElement;

            switch (query.charAt(i)) {
                case '(':
                    startIndex = i;
                    while (query.charAt(startIndex) != ')') {
                        startIndex++;
                    }
                    startIndex++;

                    String preElementStr = query.substring(i, startIndex);
//                    System.out.println("preElementStr"+preElementStr);
                    preElement = new Pair<String, String>
                            (preElementStr.substring(preElementStr.indexOf("：") + 1, preElementStr.length() - 1),preElementStr.substring(1, preElementStr.indexOf("：")));

                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '{':
                    startIndex = i;
                    while (query.charAt(startIndex) != '}') {
                        startIndex++;
                    }
                    startIndex += 2;


                    String elementStr=query.substring(i, startIndex);
                    preElement = new Pair<>( elementStr.substring(2,elementStr.length()-2),"entity");
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '"':
                    startIndex = i + 1;
                    while (query.charAt(startIndex) != '"') {
                        startIndex++;
                    }
                    startIndex++;
                    preElement = new Pair<>( query.substring(i, startIndex),null);
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '/':
                    startIndex = i + 1;
                    while (query.charAt(startIndex) != '/') {
                        startIndex++;
                    }
                    startIndex++;
                    preElement = new Pair<>(query.substring(i, startIndex),"regex");
                    preStructureList.add(preElement);
                    i = startIndex;
                    break;
                case '[':
                    startIndex = i;


                    while (query.charAt(startIndex) != ']') {
                        startIndex++;
                    }
                    startIndex++;

                    String elementStr0=query.substring(i, startIndex);

                    preElement = new Pair<>( elementStr0.substring(1,elementStr0.length()-1),"sets");
                    preStructureList.add(preElement);

                    i = startIndex;
                    break;
                default:
                    startIndex = i;
                    while (query.charAt(startIndex) != '(' && query.charAt(startIndex) !=
                            '{' && query.charAt(startIndex) != '"' && query.charAt(startIndex) !=
                            '/' && query.charAt(startIndex) != '[') {
                        startIndex++;
                        if (startIndex == query.length()) {
                            break;
                        }
                    }

                    String thisStr = query.substring(i, startIndex);
//                        System.out.println("这个位置的字符串是："+thisStr);
                    Result result = ToAnalysis.parse(thisStr,forest); //分词结果的一个封装，主要是一个List<Term>的terms
                    List<Term> subStrTerm = result.getTerms(); //拿到terms
//                    List<Term> subStrTerm = segAnsj(thisStr);
                    for (int j = 0; j < subStrTerm.size(); j++) {
//                            System.out.println(subStrTerm.get(j));
                        if (subStrTerm.get(j).toString().contains("/")) {
                            String resultStr=subStrTerm.get(j).toString();
                            preElement = new Pair<>(resultStr.substring(0, resultStr.indexOf("/")), resultStr.substring(resultStr.indexOf("/" )+1,resultStr.length()));
                            preStructureList.add(preElement);
                        } else {
                            preElement = new Pair<>( subStrTerm.get(j).toString(),"Str");
                            preStructureList.add(preElement);
                        }
                    }
                    i = startIndex;
            }
        }
        return preStructureList;
    }

    public List<List> queryElementSort(List<List> queryElement) {
        Collections.sort(queryElement, new Comparator<List>() {
            public int compare(List o1, List o2) {
                return (int) o1.get(0) - (int) o2.get(0);
            }
        });
        return queryElement;
    }

    public List<Pair<String,String>> toTemplateLabeled(String query, String labeledStr, Forest forest){

        List<Pair<String,String>> template=new ArrayList<>();
        if(labeledStr.length()>=2){
            JSONArray queryAnntatorJsonObjArray = new JSONArray(labeledStr);

            List<List> entityAnntatorList=new ArrayList();
            List entityAnntator;

            for(int i=0;i<queryAnntatorJsonObjArray.length();i++){
                entityAnntator=new ArrayList();
                JSONObject queryAnntatorJsonObj=new JSONObject(queryAnntatorJsonObjArray.get(i).toString());
                entityAnntator.add(queryAnntatorJsonObj.get("from"));
                entityAnntator.add(queryAnntatorJsonObj.get("to"));
                entityAnntator.add(queryAnntatorJsonObj.get("type"));
                entityAnntator.add("entity");
                entityAnntatorList.add(entityAnntator);
//                System.out.println(entityAnntator);
            }

            entityAnntatorList=queryElementSort(entityAnntatorList);


            List<Integer> indexOfPostion=new ArrayList<>();
            indexOfPostion.add(0);
            for (int i=0;i<entityAnntatorList.size();i++){
                indexOfPostion.add((Integer) entityAnntatorList.get(i).get(0));
                indexOfPostion.add((Integer) entityAnntatorList.get(i).get(1));
            }
            indexOfPostion.add(query.length());
//        排序
            Collections.sort(indexOfPostion);
//            System.out.println(indexOfPostion);

            for(int i=0;i<indexOfPostion.size()-1;i++){
                if(i%2==0){
//                是偶数
                    List<Term> splitList=new ArrayList<>();
                    if(indexOfPostion.get(i)>=query.length()){
//                        超出范围
//                        System.out.println(query);
//                        splitList=segAnsj(query.substring(indexOfPostion.get(i),indexOfPostion.get(i)));
                    }else{
//                        System.out.println(query);
                        String str=query.substring(indexOfPostion.get(i),indexOfPostion.get(i+1));
//                        System.out.println(str);
                        Result result = ToAnalysis.parse(str,forest); //分词结果的一个封装，主要是一个List<Term>的terms
                        splitList = result.getTerms(); //拿到terms
//                        splitList=segAnsj();
                    }
                    for(int j=0;j<splitList.size();j++){
                        String split=splitList.get(j).toString();
                        if(split.length()>=2){
                            String wordStr=split.substring(0,split.indexOf("/"));
                            String wordPro=split.substring(split.indexOf("/")+1,split.length());
                            template.add(new Pair<>(wordStr,wordPro));
                        }
                    }
                }else{
//                是奇数取出实体
                    int index=i/2;
                    String typeStr=(String)entityAnntatorList.get(index).get(2);
                    template.add(new Pair<>(typeStr,"entity"));
                }
            }
        }
        return template;
    }
}
